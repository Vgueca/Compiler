package compiler.Parser;

import compiler.Exceptions.LexerException;
import compiler.Lexer.LB;
import compiler.Lexer.Lexer;
import compiler.Lexer.Symbol;
import compiler.Lexer.Token;
import compiler.Nodes.*;
import compiler.Visitors.PrintVisitor;

import java.text.ParseException;
import java.util.ArrayList;

public class Parser {
    private final LB lb;

    public Parser(Lexer lexer) {
        this.lb = new LB(lexer);
    }

    public ProgramNode getAST() throws ParseException, LexerException {
        return parseProgram();
    }

    public static void printAST(ProgramNode node) {
        PrintVisitor visitor = new PrintVisitor();
        System.out.print("------   AST   ------\n");
        node.accept(visitor, 0);
    }

    private ProgramNode parseProgram() throws ParseException, LexerException {
        ArrayList<CVVNode.Const> constants = new ArrayList<>();
        while (lb.peek() != null && lb.peek().token() == Token.CONST)
            constants.add(parseConstDeclaration());

        ArrayList<RecordDeclarationNode> records = new ArrayList<>();
        while (lb.peek() != null && lb.peek().token() == Token.RECORD)
            records.add(parseRecordDeclaration());

        ArrayList<CVVNode> valVar = new ArrayList<>();
        while (lb.peek() != null && (lb.peek().token() == Token.VAL || lb.peek().token() == Token.VAR)) {
            if (lb.peek().token() == Token.VAL)
                valVar.add(parseValue());
            else
                valVar.add(parseVariable());
        }

        ArrayList<ProcedureNode> procedures = new ArrayList<>();
        while (lb.peek() != null && lb.peek().token() == Token.PROC)
            procedures.add(parseProcedure());

        Symbol next = lb.peek();
        try {
            lb.consume(Token.EOF); // end of the program
        } catch (ParseException e) {
            throw new ParseException(
                    "The parser encountered an unexpected " + next.token() + " token: '" + next.content() + "'.",
                    0);
        }

        return new ProgramNode(constants, records, valVar, procedures);
    }

    private CVVNode.Const parseConstDeclaration() throws ParseException, LexerException {
        lb.consume(Token.CONST);
        IdentifierNode identifier = new IdentifierNode(lb.match(Token.IDENTIFIER).content());
        TypeNode.Base baseType = parseBaseType();
        lb.consume(Token.ASSIGNMENT);
        Expr expression = parseExpression();
        lb.consume(Token.SEMICOLON);
        return new CVVNode.Const(identifier, baseType, expression);
    }

    private CVVNode.Val parseValue() throws ParseException, LexerException {
        lb.consume(Token.VAL);
        IdentifierNode identifier = new IdentifierNode(lb.match(Token.IDENTIFIER).content());
        TypeNode.Base baseType = parseBaseType();
        lb.consume(Token.ASSIGNMENT);
        Expr expression = parseExpression();
        lb.consume(Token.SEMICOLON);
        return new CVVNode.Val(identifier, baseType, expression);
    }

    private CVVNode.Var parseVariable() throws ParseException, LexerException {
        lb.consume(Token.VAR);
        IdentifierNode identifier = new IdentifierNode(lb.match(Token.IDENTIFIER).content());
        TypeNode type = parseType();
        lb.consume(Token.ASSIGNMENT);
        Expr expression = parseExpression();
        lb.consume(Token.SEMICOLON);
        return new CVVNode.Var(identifier, type, expression);
    }

    private RecordDeclarationNode parseRecordDeclaration() throws ParseException, LexerException {
        lb.consume(Token.RECORD);
        IdentifierNode identifier = new IdentifierNode(lb.match(Token.IDENTIFIER).content());
        lb.consume(Token.OPENCURLYBRACKETS);

        ArrayList<FieldDeclarationNode> fields = new ArrayList<>();
        while (lb.peek().token() != Token.CLOSECURLYBRACKETS) {
            fields.add(parseFieldDeclaration());
        }

        lb.consume(Token.CLOSECURLYBRACKETS);
        return new RecordDeclarationNode(identifier, fields);
    }

    private FieldDeclarationNode parseFieldDeclaration() throws ParseException, LexerException {
        IdentifierNode identifier = new IdentifierNode(lb.match(Token.IDENTIFIER).content());
        TypeNode type = parseType();
        lb.consume(Token.SEMICOLON);
        return new FieldDeclarationNode(identifier, type);
    }

    private TypeNode parseType() throws ParseException, LexerException { // TODO type tests still ok after <base-type>[] arrays?
        Token token = lb.peek().token();

        switch (token) {
            case INTTYPE, REALTYPE, STRINGTYPE, BOOLTYPE -> {
                TypeNode.Base baseType = parseBaseType();
                if (lb.peek().token() == Token.OPENBRACKETS) {
                    lb.consume(Token.OPENBRACKETS);
                    lb.consume(Token.CLOSEBRACKETS);
                    return new TypeNode.Array(baseType);
                } else
                    return baseType;
            }
            case IDENTIFIER -> {
                return new TypeNode.Identifier(new IdentifierNode(lb.match(Token.IDENTIFIER).content()));
            }
        }

        throw new ParseException("Unexpected type (next: " + lb.peek() + ").", 0);
    }

    private TypeNode.Base parseBaseType() throws ParseException, LexerException {
        Token token = lb.get().token();

        if (token == Token.INTTYPE || token == Token.REALTYPE || token == Token.STRINGTYPE || token == Token.BOOLTYPE)
            return new TypeNode.Base(token);

        throw new ParseException("Expected base type (int | real | string | bool) but got " + token + ".", 0);
    }

    /**
     * Builds a tree of bool-term nodes in Or, or And nodes.
     *     OR
     *   /   \
     * bt     AND
     *       /   \
     *     bt     bt
     * TODO:
     *        AND
     *       /   \
     *     OR     bt
     *   /   \
     * bt     bt
     * @return <ExpressionNode> ::= <BoolTermNode> ( ( Token.OR | Token.AND ) <ExpressionNode> )?
     * @throws ParseException if unexpected token.
     */
    private ExpressionNode parseExpression() throws ParseException, LexerException {
        BoolTermNode left = parseBoolTerm();

        if (lb.peek() == null)
            return new ExpressionNode(left);

        switch (lb.peek().token()) {
            case OR:
                lb.consume(Token.OR);
                return new ExpressionNode.Or(left, parseExpression());
            case AND:
                lb.consume(Token.AND);
                return new ExpressionNode.And(left, parseExpression());
        }
        return new ExpressionNode(left);
    }

    private BoolTermNode parseBoolTerm() throws ParseException, LexerException {
        BoolFactorNode left = parseBoolFactor();

        if (lb.peek() == null)
            return new BoolTermNode(left);

        switch (lb.peek().token()) {
            case LOWER:
                lb.consume(Token.LOWER); // <
                return new BoolTermNode.Lower(left, parseBoolTerm());
            case LEQ:
                lb.consume(Token.LEQ); // <=
                return new BoolTermNode.LEQ(left, parseBoolTerm());
            case GREATER:
                lb.consume(Token.GREATER); // >
                return new BoolTermNode.Greater(left, parseBoolTerm());
            case GEQ:
                lb.consume(Token.GEQ); // >=
                return new BoolTermNode.GEQ(left, parseBoolTerm());
            case EQUAL:
                lb.consume(Token.EQUAL); // ==
                return new BoolTermNode.Equal(left, parseBoolTerm());
            case DIFFERENT:
                lb.consume(Token.DIFFERENT); // <>
                return new BoolTermNode.Different(left, parseBoolTerm());
        }

        return new BoolTermNode(left); // no match
    }

    private BoolFactorNode parseBoolFactor() throws ParseException, LexerException {
        ArithTermNode left = parseArithTerm();

        if (lb.peek() == null)
            return new BoolFactorNode(left);

        switch (lb.peek().token()) {
            case ADDITION:
                lb.consume(Token.ADDITION);
                return new BoolFactorNode.Addition(left, parseBoolFactor());
            case SUBTRACTION:
                lb.consume(Token.SUBTRACTION);
                return new BoolFactorNode.Subtraction(left, parseBoolFactor());
        }
        return new BoolFactorNode(left);
    }

    private ArithTermNode parseArithTerm() throws ParseException, LexerException {
        ArithFactorNode left = parseFactorTerm();

        if (lb.peek() == null)
            return new ArithTermNode(left);

        switch (lb.peek().token()) {
            case MULTIPLICATION:
                lb.consume(Token.MULTIPLICATION);
                return new ArithTermNode.Multiplication(left, parseArithTerm());
            case DIVISION:
                lb.consume(Token.DIVISION);
                return new ArithTermNode.Division(left, parseArithTerm());
            case MODULO:
                lb.consume(Token.MODULO);
                return new ArithTermNode.Modulo(left, parseArithTerm());
        }
        return new ArithTermNode(left);
    }

    private ArithFactorNode parseFactorTerm() throws ParseException, LexerException {
        switch (lb.peek().token()) {
            case ADDITION:
                lb.consume(Token.ADDITION);
                return new ArithFactorNode.Positive(parsePrimary());
            case SUBTRACTION:
                lb.consume(Token.SUBTRACTION);
                return new ArithFactorNode.Negative(parsePrimary());
        }
        return new ArithFactorNode(parsePrimary());
    }

    private PrimaryNode parsePrimary() throws ParseException, LexerException {
        switch (lb.peek().token()) {
            case INT, REAL, STRING, BOOL:
                return parseLiteral();
            case IDENTIFIER:
                return switch (lb.peek2().token()) {
                    case OPENPARENTHESIS -> parseFunctionCall();
                    case OPENBRACKETS -> parseArrayAccess();
                    case DOT -> parseRecordAccess();
                    default -> new IdentifierNode(lb.match(Token.IDENTIFIER).content());
                };
            case OPENPARENTHESIS:
                lb.consume(Token.OPENPARENTHESIS);
                ExpressionNode exp = parseExpression();
                lb.consume(Token.CLOSEPARENTHESIS);
                return exp; // CHECK maybe the parenthesis are important
            case INTTYPE, REALTYPE, STRINGTYPE, BOOLTYPE:
                return parseArrayInit();
        }
        throw new ParseException("Unexpected token while parsing a Primary: " + lb.peek() + ".", 0);
    }

    private LiteralNode parseLiteral() throws ParseException, LexerException {
        return switch (lb.peek().token()) {
            case INT -> new LiteralNode.Int(lb.match(Token.INT).content());
            case REAL -> new LiteralNode.Real(lb.match(Token.REAL).content());
            case STRING -> {
                String lit = lb.match(Token.STRING).content();
                if (lit.charAt(0) != '"' || lit.charAt(lit.length() - 1) != '"')
                    throw new ParseException("A string should be serrounded by '\"' quotes.", 0);

                yield new LiteralNode.String(lit.substring(1, lit.length() - 1));
            }
            case BOOL -> new LiteralNode.Bool(lb.match(Token.BOOL).content());
            default -> throw new ParseException("Unexpected token while parsing literal.", 0);
        };
    }

    private FunctionCallNode parseFunctionCall() throws ParseException, LexerException {
        IdentifierNode identifier = new IdentifierNode(lb.match(Token.IDENTIFIER).content());
        lb.consume(Token.OPENPARENTHESIS);

        ArrayList<Expr> args = new ArrayList<>();
        while (lb.peek().token() != Token.CLOSEPARENTHESIS) {
            if (args.size() > 0 && lb.peek().token() == Token.COMMA)
                lb.consume(Token.COMMA);
            args.add(parseExpression());
        }

        lb.consume(Token.CLOSEPARENTHESIS);
        return new FunctionCallNode(identifier, args);
    }

    private ArrayInitNode parseArrayInit() throws ParseException, LexerException {
        TypeNode type = parseType();
        if (!(type instanceof TypeNode.Array))
            throw new ParseException("An array type was expected.", 0);
        lb.consume(Token.OPENPARENTHESIS);
        ExpressionNode size = parseExpression(); // expression
        lb.consume(Token.CLOSEPARENTHESIS);
        return new ArrayInitNode((TypeNode.Array) type, size);
    }

    private ArrayAccessNode parseArrayAccess() throws ParseException, LexerException {
        IdentifierNode identifier = new IdentifierNode(lb.match(Token.IDENTIFIER).content());
        lb.consume(Token.OPENBRACKETS);
        ExpressionNode index = parseExpression(); // expression
        lb.consume(Token.CLOSEBRACKETS);
        return new ArrayAccessNode(identifier, index);
    }

    private RecordAccessNode parseRecordAccess() throws ParseException, LexerException {
        ASTNode record; // Identifier or array access

        if (lb.peek2().token() == Token.DOT)
            record = new IdentifierNode(lb.match(Token.IDENTIFIER).content());
        else
            record = parseArrayAccess();

        lb.consume(Token.DOT);
        IdentifierNode fieldIdentifier = new IdentifierNode(lb.match(Token.IDENTIFIER).content());
        return new RecordAccessNode(record, fieldIdentifier);
    }

    private RecordAccessNode parseRecordAccess(ArrayAccessNode record) throws ParseException, LexerException {
        lb.consume(Token.DOT);
        IdentifierNode fieldIdentifier = new IdentifierNode(lb.match(Token.IDENTIFIER).content());
        return new RecordAccessNode(record, fieldIdentifier);
    }

    private ProcedureNode parseProcedure() throws ParseException, LexerException {
        lb.consume(Token.PROC);
        IdentifierNode identifier = new IdentifierNode(lb.match(Token.IDENTIFIER).content());
        lb.consume(Token.OPENPARENTHESIS);

        ArrayList<ParameterNode> params = new ArrayList<>();
        while (lb.peek().token() != Token.CLOSEPARENTHESIS) {
            if (params.size() > 0 && lb.peek().token() == Token.COMMA)
                lb.consume(Token.COMMA);
            params.add(parseParameter());
        }

        lb.consume(Token.CLOSEPARENTHESIS);
        return new ProcedureNode(identifier, params, parseReturnType(), parseBlock());
    }

    private ParameterNode parseParameter() throws ParseException, LexerException {
        return new ParameterNode(new IdentifierNode(lb.match(Token.IDENTIFIER).content()), parseType());
    }

    private TypeNode parseReturnType() throws ParseException, LexerException {
        if (lb.peek().token() == Token.VOIDTYPE) {
            lb.consume(Token.VOIDTYPE);
            return new TypeNode.Void();
        }
        return parseType();
    }

    private BlockNode parseBlock() throws ParseException, LexerException {
        lb.consume(Token.OPENCURLYBRACKETS);

        ArrayList<ASTNode> statements = new ArrayList<>();
        while (lb.peek().token() != Token.CLOSECURLYBRACKETS)
            statements.add(parseStatement());

        lb.consume(Token.CLOSECURLYBRACKETS);
        return new BlockNode(statements);
    }

    private ASTNode parseStatement() throws ParseException, LexerException {
        switch (lb.peek().token()) {
            case VAL:
                return parseValue();
            case VAR:
                return parseVariable();
            case IF:
                return parseIf();
            case WHILE:
                return parseWhile();
            case FOR:
                return parseFor();
            case RETURN:
                return parseReturn();
            case DELETE:
                return parseDelete();
        }

        switch (lb.peek2().token()) {
            case ASSIGNMENT:
                return parseAssignment(null);
            case OPENPARENTHESIS:
                FunctionCallNode fc = parseFunctionCall();
                lb.consume(Token.SEMICOLON);
                return fc;
            case DOT:
                RecordAccessNode recordAccess = parseRecordAccess();
                if (lb.peek().token() == Token.ASSIGNMENT)
                    return parseAssignment(recordAccess);
                return recordAccess;
            case OPENBRACKETS:
                ArrayAccessNode arrayAccess = parseArrayAccess();
                if (lb.peek().token() == Token.DOT)
                    return parseRecordAccess(arrayAccess);
                if (lb.peek().token() == Token.ASSIGNMENT)
                    return parseAssignment(arrayAccess);
                return arrayAccess;
        }
        throw new ParseException("Couldn't parse statement (next: " + lb.peek() + ").\n" + lb.getProgress(), 0);
    }

    private IfNode parseIf() throws ParseException, LexerException {
        lb.consume(Token.IF);
        Expr condition = parseExpression();
        BlockNode block = parseBlock();

        if (lb.peek().token() == Token.ELSE) {
            lb.consume(Token.ELSE);
            return new IfNode.Else(condition, block, parseBlock());
        }

        return new IfNode(condition, block);
    }

    private WhileNode parseWhile() throws ParseException, LexerException {
        lb.consume(Token.WHILE);
        return new WhileNode(parseExpression(), parseBlock());
    }

    private ForNode parseFor() throws ParseException, LexerException {
        lb.consume(Token.FOR);
        IdentifierNode i = new IdentifierNode(lb.match(Token.IDENTIFIER).content());
        lb.consume(Token.ASSIGNMENT);
        LiteralNode.Int from = new LiteralNode.Int(lb.match(Token.INT).content());
        lb.consume(Token.TO);
        LiteralNode.Int to = new LiteralNode.Int(lb.match(Token.INT).content());

        if (lb.peek().token() == Token.BY) {
            lb.consume(Token.BY);
            LiteralNode.Int by = new LiteralNode.Int(lb.match(Token.INT).content());
            return new ForNode.By(i, from, to, by, parseBlock());
        }

        return new ForNode(i, from, to, parseBlock());
    }

    private AssignmentNode parseAssignment(Expr left) throws ParseException, LexerException {
        if (left == null) {
            left = switch (lb.peek2().token()) {
                case OPENBRACKETS -> parseArrayAccess();
                case DOT -> parseRecordAccess();
                default -> new IdentifierNode(lb.match(Token.IDENTIFIER).content());
            };
        }
        lb.consume(Token.ASSIGNMENT);
        Expr right = parseExpression();
        lb.consume(Token.SEMICOLON);
        return new AssignmentNode(left, right);
    }

    private ReturnNode parseReturn() throws ParseException, LexerException {
        lb.consume(Token.RETURN);
        ReturnNode ret;
        if (lb.peek().token() == Token.SEMICOLON)
            ret = new ReturnNode(null);
        else
            ret = new ReturnNode(parseExpression());
        lb.consume(Token.SEMICOLON);
        return ret;
    }

    private DeleteNode parseDelete() throws ParseException, LexerException {
        lb.consume(Token.DELETE);
        DeleteNode del = new DeleteNode(new IdentifierNode(lb.match(Token.IDENTIFIER).content()));
        lb.consume(Token.SEMICOLON);
        return del;
    }
}
