import compiler.Lexer.Lexer;
import compiler.Exceptions.LexerException;
import compiler.Lexer.Token;
import compiler.Nodes.*;
import compiler.Nodes.ASTNode;
import compiler.Nodes.Expr;
import compiler.Parser.Parser;
import org.junit.Test;

import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class TestParser {
    private static final boolean VERBOSE = false;

    /**
     * Create a lexer and a parser working on the given input.
     * 
     * @param input the code string the parser should read.
     * @return a new Parser of the input.
     */
    public static Parser initParser(String input) {
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        return new Parser(lexer);
    }

    @Test
    public void testConstDeclaration_int() throws ParseException, LexerException {
        Parser parser = initParser("const life int = 42;");

        ArrayList<CVVNode.Const> constants = new ArrayList<>();
        constants.add(new CVVNode.Const(new IdentifierNode("life"), new TypeNode.Base(Token.INTTYPE),
                new LiteralNode.Int("42")));
        ProgramNode ast = parser.getAST();
        if (VERBOSE)
            Parser.printAST(ast);
        assertEquals(new ProgramNode(constants, null, null, null), ast);
    }

    @Test
    public void testConstDeclaration_real() throws ParseException, LexerException {
        Parser parser = initParser("const PI real = 3.1415;");

        ArrayList<CVVNode.Const> constants = new ArrayList<>();
        constants.add(new CVVNode.Const(new IdentifierNode("PI"), new TypeNode.Base(Token.REALTYPE),
                new LiteralNode.Real("3.1415")));

        ProgramNode ast = parser.getAST();
        if (VERBOSE)
            Parser.printAST(ast);
        assertEquals(new ProgramNode(constants, null, null, null), ast);
    }

    @Test
    public void testConstDeclaration_string() throws ParseException, LexerException {
        Parser parser = initParser("const hello_world string = \"Hola mundo ;-)\";");

        ArrayList<CVVNode.Const> constants = new ArrayList<>();
        constants.add(new CVVNode.Const(new IdentifierNode("hello_world"), new TypeNode.Base(Token.STRINGTYPE),
                new LiteralNode.String("Hola mundo ;-)")));
        ProgramNode ast = parser.getAST();
        if (VERBOSE)
            Parser.printAST(ast);
        assertEquals(new ProgramNode(constants, null, null, null), ast);
    }

    @Test
    public void testConstDeclaration_bool() throws ParseException, LexerException {
        Parser parser = initParser("const isGood2Go bool = true;");

        ArrayList<CVVNode.Const> constants = new ArrayList<>();
        constants.add(new CVVNode.Const(new IdentifierNode("isGood2Go"), new TypeNode.Base(Token.BOOLTYPE),
                new LiteralNode.Bool("true")));
        ProgramNode ast = parser.getAST();
        if (VERBOSE)
            Parser.printAST(ast);
        assertEquals(new ProgramNode(constants, null, null, null), ast);
    }

    @Test
    public void testVarDeclaration_RecordInit() throws ParseException, LexerException {
        Parser parser = initParser("var ben Person = Person(\"Ben\", 20);");

        ArrayList<Expr> params = new ArrayList<>();
        params.add(new LiteralNode.String("Ben"));
        params.add(new LiteralNode.Int("20"));

        ArrayList<CVVNode> valVar = new ArrayList<>();
        valVar.add(new CVVNode.Var(new IdentifierNode("ben"),
                new TypeNode.Identifier(new IdentifierNode("Person")),
                new FunctionCallNode(new IdentifierNode("Person"), params)));

        ProgramNode ast = parser.getAST();
        if (VERBOSE)
            Parser.printAST(ast);
        assertEquals(new ProgramNode(null, null, valVar, null), ast);
    }

    @Test
    public void testVarDeclaration_ArrayInit() throws ParseException, LexerException {
        Parser parser = initParser("var myScores real[] = real[](10);");

        ArrayList<CVVNode> valVar = new ArrayList<>();
        valVar.add(new CVVNode.Var(new IdentifierNode("myScores"),
                new TypeNode.Array(new TypeNode.Base(Token.REALTYPE)),
                new ArrayInitNode(new TypeNode.Array(new TypeNode.Base(Token.REALTYPE)),
                        new LiteralNode.Int("10"))));

        ProgramNode ast = parser.getAST();
        if (VERBOSE)
            Parser.printAST(ast);
        assertEquals(new ProgramNode(null, null, valVar, null), ast);
    }

    @Test
    public void testRecordDeclaration_string() throws ParseException, LexerException {
        Parser parser = initParser("record Person { name string; }");

        ArrayList<RecordDeclarationNode> records = new ArrayList<>();
        ArrayList<FieldDeclarationNode> fields = new ArrayList<>();
        fields.add(new FieldDeclarationNode(new IdentifierNode("name"), new TypeNode.Base(Token.STRINGTYPE)));
        records.add(new RecordDeclarationNode(new IdentifierNode("Person"), fields));

        ProgramNode ast = parser.getAST();
        if (VERBOSE)
            Parser.printAST(ast);
        assertEquals(new ProgramNode(null, records, null, null), ast);
    }

    @Test
    public void testRecordDeclaration_multi() throws ParseException, LexerException {
        Parser parser = initParser("record Person { name string; age int; smart bool; gpa real; }");

        ArrayList<RecordDeclarationNode> records = new ArrayList<>();
        ArrayList<FieldDeclarationNode> fields = new ArrayList<>();
        fields.add(new FieldDeclarationNode(new IdentifierNode("name"), new TypeNode.Base(Token.STRINGTYPE)));
        fields.add(new FieldDeclarationNode(new IdentifierNode("age"), new TypeNode.Base(Token.INTTYPE)));
        fields.add(new FieldDeclarationNode(new IdentifierNode("smart"), new TypeNode.Base(Token.BOOLTYPE)));
        fields.add(new FieldDeclarationNode(new IdentifierNode("gpa"), new TypeNode.Base(Token.REALTYPE)));
        records.add(new RecordDeclarationNode(new IdentifierNode("Person"), fields));

        ProgramNode ast = parser.getAST();
        if (VERBOSE)
            Parser.printAST(ast);
        assertEquals(new ProgramNode(null, records, null, null), ast);
    }

    @Test
    public void testRecordDeclaration_intArray() throws ParseException, LexerException {
        Parser parser = initParser("record Person { age int[]; }");

        ArrayList<RecordDeclarationNode> records = new ArrayList<>();
        ArrayList<FieldDeclarationNode> fields = new ArrayList<>();
        fields.add(new FieldDeclarationNode(new IdentifierNode("age"),
                new TypeNode.Array(new TypeNode.Base(Token.INTTYPE))));
        records.add(new RecordDeclarationNode(new IdentifierNode("Person"), fields));

        ProgramNode ast = parser.getAST();
        if (VERBOSE)
            Parser.printAST(ast);
        assertEquals(new ProgramNode(null, records, null, null), ast);
    }

    @Test
    public void testRecordDeclaration_multiArray() throws ParseException, LexerException {
        Parser parser = initParser("record Person { name string[]; age int[]; smart bool[]; gpa real[]; }");

        ArrayList<RecordDeclarationNode> records = new ArrayList<>();
        ArrayList<FieldDeclarationNode> fields = new ArrayList<>();
        fields.add(new FieldDeclarationNode(new IdentifierNode("name"),
                new TypeNode.Array(new TypeNode.Base(Token.STRINGTYPE))));
        fields.add(new FieldDeclarationNode(new IdentifierNode("age"),
                new TypeNode.Array(new TypeNode.Base(Token.INTTYPE))));
        fields.add(new FieldDeclarationNode(new IdentifierNode("smart"),
                new TypeNode.Array(new TypeNode.Base(Token.BOOLTYPE))));
        fields.add(new FieldDeclarationNode(new IdentifierNode("gpa"),
                new TypeNode.Array(new TypeNode.Base(Token.REALTYPE))));
        records.add(new RecordDeclarationNode(new IdentifierNode("Person"), fields));

        ProgramNode ast = parser.getAST();
        if (VERBOSE)
            Parser.printAST(ast);
        assertEquals(new ProgramNode(null, records, null, null), ast);
    }

    @Test
    public void testBaseType()
            throws ParseException, NoSuchMethodException, InvocationTargetException,
            IllegalAccessException {
        Method method = Parser.class.getDeclaredMethod("parseBaseType");
        method.setAccessible(true);

        Parser int_parser = initParser("int");
        Parser real_parser = initParser("real");
        Parser string_parser = initParser("string");
        Parser bool_parser = initParser("bool");

        TypeNode.Base int_result = (TypeNode.Base) method.invoke(int_parser);
        TypeNode.Base real_result = (TypeNode.Base) method.invoke(real_parser);
        TypeNode.Base string_result = (TypeNode.Base) method.invoke(string_parser);
        TypeNode.Base bool_result = (TypeNode.Base) method.invoke(bool_parser);

        assertEquals(new TypeNode.Base(Token.INTTYPE), int_result);
        assertEquals(new TypeNode.Base(Token.REALTYPE), real_result);
        assertEquals(new TypeNode.Base(Token.STRINGTYPE), string_result);
        assertEquals(new TypeNode.Base(Token.BOOLTYPE), bool_result);
    }

    @Test
    public void testExpressions_random_bool() throws ParseException, LexerException {
        Parser parser = initParser("val wow bool = ok and notOk or nok + bok;");

        ArrayList<CVVNode> valVar = new ArrayList<>();
        valVar.add(new CVVNode.Val(new IdentifierNode("wow"), new TypeNode.Base(Token.BOOLTYPE),
                new ExpressionNode.And(
                        new BoolTermNode(new IdentifierNode("ok")),
                        new ExpressionNode.Or(new BoolTermNode(new IdentifierNode("notOk")),
                                new ExpressionNode(
                                        new BoolFactorNode.Addition(new BoolFactorNode(new IdentifierNode("nok")),
                                                new BoolFactorNode(new IdentifierNode("bok"))))))));

        ProgramNode ast = parser.getAST();
        if (VERBOSE)
            Parser.printAST(ast);
        assertEquals(new ProgramNode(null, null, valVar, null), ast);
    }

    @Test
    public void testProcedure_level1() throws ParseException, LexerException {
        Parser parser = initParser(
                "proc procedure(variable1 int, variable2 int) int { if 4 < variable1 { return variable1; } else { return variable2; } }");

        ArrayList<ParameterNode> param = new ArrayList<>();
        param.add(new ParameterNode(new IdentifierNode("variable1"), new TypeNode.Base(Token.INTTYPE)));
        param.add(new ParameterNode(new IdentifierNode("variable2"), new TypeNode.Base(Token.INTTYPE)));

        TypeNode.Base ret = new TypeNode.Base(Token.INTTYPE);

        ArrayList<ASTNode> block1 = new ArrayList<>();
        block1.add(new ReturnNode(new IdentifierNode("variable1")));
        ArrayList<ASTNode> block2 = new ArrayList<>();
        block2.add(new ReturnNode(new IdentifierNode("variable2")));

        ArrayList<ASTNode> statements = new ArrayList<>();
        statements.add(new IfNode.Else(new BoolTermNode.Lower(
                new BoolFactorNode(new LiteralNode.Int("4")), new BoolTermNode(new IdentifierNode("variable1"))),
                new BlockNode(block1), new BlockNode(block2)));

        BlockNode block = new BlockNode(statements);

        ArrayList<ProcedureNode> procedures = new ArrayList<>();
        procedures.add(new ProcedureNode(new IdentifierNode("procedure"), param, ret, block));

        ProgramNode ast = parser.getAST();
        if (VERBOSE)
            Parser.printAST(ast);
        assertEquals(new ProgramNode(null, null, null, procedures), ast);
    }

    @Test
    public void testProcedure_level2() throws ParseException, LexerException {
        Parser parser = initParser("proc run() void { getExample ( 4 - 5 , variable == variable ); }");

        ArrayList<Expr> args = new ArrayList<>();
        args.add(new BoolFactorNode.Subtraction(new ArithTermNode(new LiteralNode.Int("4")),
                new BoolFactorNode(new LiteralNode.Int("5"))));
        args.add(new BoolTermNode.Equal(new BoolFactorNode(new IdentifierNode("variable")),
                new BoolTermNode(new IdentifierNode("variable"))));

        ArrayList<ASTNode> statements = new ArrayList<>();
        statements.add(new FunctionCallNode(new IdentifierNode("getExample"), args));

        ArrayList<ProcedureNode> procedures = new ArrayList<>();
        procedures.add(
                new ProcedureNode(new IdentifierNode("run"), null, new TypeNode.Void(),
                        new BlockNode(statements)));

        ProgramNode ast = parser.getAST();
        if (VERBOSE)
            Parser.printAST(ast);
        assertEquals(new ProgramNode(null, null, null, procedures), ast);
    }

    @Test
    public void testRecordAccess() throws ParseException, LexerException {
        Parser parser = initParser("proc main() void { variable_aux = variable.x; }");

        ArrayList<ASTNode> statements = new ArrayList<>();
        statements.add(new AssignmentNode(new IdentifierNode("variable_aux"),
                new RecordAccessNode(new IdentifierNode("variable"), new IdentifierNode("x"))));

        ArrayList<ProcedureNode> procedures = new ArrayList<>();
        procedures.add(
                new ProcedureNode(new IdentifierNode("main"), null, new TypeNode.Void(),
                        new BlockNode(statements)));

        ProgramNode ast = parser.getAST();
        if (VERBOSE)
            Parser.printAST(ast);

        assertEquals(new ProgramNode(null, null, null, procedures), ast);
    }

    @Test
    public void testDelete() throws ParseException, LexerException {
        Parser parser = initParser("proc main() void { delete variable; }");

        ArrayList<ASTNode> statements = new ArrayList<>();
        statements.add(new DeleteNode(new IdentifierNode("variable")));

        ArrayList<ProcedureNode> procedures = new ArrayList<>();
        procedures.add(
                new ProcedureNode(new IdentifierNode("main"), null, new TypeNode.Void(),
                        new BlockNode(statements)));

        ProgramNode ast = parser.getAST();
        if (VERBOSE)
            Parser.printAST(ast);
        assertEquals(new ProgramNode(null, null, null, procedures), ast);
    }

    @Test
    public void testWhile() throws ParseException, LexerException {
        Parser parser = initParser(
                "proc procedure(variable1 int) int { while 4 < variable1 { return variable1; } }");

        ArrayList<ParameterNode> param = new ArrayList<>();
        param.add(new ParameterNode(new IdentifierNode("variable1"), new TypeNode.Base(Token.INTTYPE)));

        TypeNode.Base ret = new TypeNode.Base(Token.INTTYPE);

        ArrayList<ASTNode> block1 = new ArrayList<>();
        block1.add(new ReturnNode(new IdentifierNode("variable1")));

        ArrayList<ASTNode> statements = new ArrayList<>();
        statements
                .add(new WhileNode(new BoolTermNode.Lower(
                        new BoolFactorNode(new LiteralNode.Int("4")),
                        new BoolTermNode(new IdentifierNode("variable1"))),
                        new BlockNode(block1)));

        BlockNode block = new BlockNode(statements);

        ArrayList<ProcedureNode> procedures = new ArrayList<>();
        procedures.add(new ProcedureNode(new IdentifierNode("procedure"), param, ret, block));

        ProgramNode ast = parser.getAST();
        if (VERBOSE)
            Parser.printAST(ast);
        assertEquals(new ProgramNode(null, null, null, procedures), ast);
    }
}