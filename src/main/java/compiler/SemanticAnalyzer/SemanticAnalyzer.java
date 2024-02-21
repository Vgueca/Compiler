package compiler.SemanticAnalyzer;

import compiler.Exceptions.SemanticException.SemanticException;
import compiler.Nodes.*;

import java.text.ParseException;

public class SemanticAnalyzer {

    /**
     * The idea here is to check all the nodes from the return ProgramNode of the Parser getAST() function. We take this node and we use the SType getTypes in order to
     * see if all is semantically right. If not, we throw an error from the possible ones with a description of the error. We have to develop for each type of node a STypegetType"
     * function and then they will be called recursively. Also for implementing SType getType function we will need to define the symboltable, that will be a global variable of our
     * semantic analyzer.
     */

    //    /**
    //     * We analyze the AST obtained from the Paser
    //     * @param node
    //     */
    //    public static void analyze(ProgramNode node, SymbolTable st) throws ParseException {
    //        symboltable = new SymbolTable(null);
    //        node.accept(new PrintVisitor(), 0);
    //    }

    /**
     * We return the exact type of the node passed as argument. It should be one of the available types of the Symbol Table : Int, Real, String, Bool, Void, Id, Array, Function, Record
     * @param node
     * @param st
     * @return
     * @throws SemanticException
     * @throws ParseException
     */
    public static SType getType(ASTNode node, SymbolTable st) throws SemanticException, ParseException {
        if (node instanceof ExpressionNode n)
            return getType(n, st);
        else if (node instanceof BoolTermNode n)
            return getType(n, st);
        else if (node instanceof BoolFactorNode n)
            return getType(n, st);
        else if (node instanceof ArithTermNode n)
            return getType(n, st);
        else if (node instanceof ArithFactorNode n)
            return getType(n, st);
        else if (node instanceof ArrayAccessNode n)
            return getType(n, st);
        else if (node instanceof FunctionCallNode n)
            return getType(n, st);
        else if (node instanceof IdentifierNode n)
            return getType(n, st);
        else if (node instanceof LiteralNode n)
            return getType(n, st);
        else if (node instanceof RecordAccessNode n)
            return getType(n, st);
        else if (node instanceof ParameterNode n)
            return getType(n, st);
        else if (node instanceof TypeNode.Base n)
            return getType(n, st);
        else if (node instanceof TypeNode.Array n)
            return getType(n, st);
        else if (node instanceof TypeNode.Identifier n)
            return getType(n, st);
        else if (node instanceof TypeNode.Void n)
            return getType(n, st);
        else if (node instanceof ArrayInitNode n)
            return getType(n, st);
        throw new SemanticException("Unhandled type", "Tried to get an unhandled type (" + node.getClass() + ").");
    }

    /**
     * We return the type of the ArithFactorNode passed as argument but checking before if there is any mistake with the signs of the expression
     * @param node Node from ASTNode class.
     * @param st SymbolTable of the Semantic Analyzer
     * @return SType of node
     * @throws SemanticException
     * @throws ParseException
     */
    public static SType getType(ArithFactorNode node, SymbolTable st) throws SemanticException, ParseException {
        SType prim = getType(node.left, st);

        if (node instanceof ArithFactorNode.Positive) {
            if (prim instanceof SType.Int || prim instanceof SType.String || prim instanceof SType.Real) {
                return prim;
            } else {
                throw new SemanticException("No adequately type",
                        "Only Int, real or string admitted for positive factors but got " + prim + ".");
            }
        } else if (node instanceof ArithFactorNode.Negative) {
            if (prim instanceof SType.Int || prim instanceof SType.Real) {
                return prim;
            } else {
                throw new SemanticException("No adequately type",
                        "Only Int or real admitted for negative factors but got " + prim + ".");
            }
        } else
            return getType(node.left, st); // instead of throwing an error, this means it's not +/- but a Primary
        //        throw new SemanticException("Unhandled ArithFactor", "You called getType on the wrong type.");
    }

    /**
     * We return the type of the ArithTermNode passed as argument but checking before if there is any mistake with the admitted type for the expression in these operations
     * @param node Node from ASTNode class.
     * @param st SymbolTable of the Semantic Analyzer
     * @return SType of node
     * @throws ParseException
     * @throws SemanticException
     */
    public static SType getType(ArithTermNode node, SymbolTable st) throws ParseException, SemanticException {
        if (node.right == null)
            return getType(node.left, st);

        SType left = getType(node.left, st);
        SType right = getType(node.right, st);

        if (node instanceof ArithTermNode.Modulo) {
            if (left instanceof SType.Int && right instanceof SType.Int) {
                return left;
            } else { //Only int type is admitted for the module operation
                throw new SemanticException("No adequately type",
                        "Only Int  admitted for module operation but got " + left + " and " + right + ".");
            }

        } else if (node instanceof ArithTermNode.Division || node instanceof ArithTermNode.Multiplication) {
            //The only possible combinations for the division and multiplication are: int-int, int-real, real-int or real-real.
            if (((left instanceof SType.Int || left instanceof SType.Real)
                    && (right instanceof SType.Int || right instanceof SType.Real))) {
                if (right instanceof SType.Real)
                    return right;
                return left;
            } else {
                throw new SemanticException("No adequately type",
                        "Only Int or real admitted for multiplication and division operation but got " + left + " and "
                                + right + ".");
            }
        }
        throw new SemanticException("Not equal types", "The two variables  have different types");
    }

    public static SType getType(ArrayInitNode node, SymbolTable st) throws ParseException, SemanticException {
        SType stype = getType(node.size, st);
        if (!(stype instanceof SType.Int))
            throw new SemanticException("Array init requires int",
                    "The array size initializer should be an integer value (not: " + stype + ").");

        return getType(node.type, st);
    }

    /**
     * We return the type of the ArrayAccessNode passed as argument but checking before if it is truly an array.
     * @param node Node from ASTNode class.
     * @param st SymbolTable of the Semantic Analyzer
     * @return SType of node
     * @throws SemanticException
     * @throws ParseException
     */
    public static SType getType(ArrayAccessNode node, SymbolTable st) throws ParseException, SemanticException {
        SType atype = st.get(node.identifier);
        if (!(atype instanceof SType.Array))
            throw new SemanticException("Not an array",
                    "Array accesses can only be done on arrays (not: " + atype.getClass().getSimpleName() + ").");
        return ((SType.Array) atype).type;
    }

    //    public static SType getType(AssignmentNode node, SymbolTable st) throws ParseException { return null; }
    //    public static SType getType(BlockNode node, SymbolTable st) throws ParseException { return null; }

    /**
     * We return the type of the BoolFactorNode passed as argument but checking before if the types of the expression are right
     * @param node Node from ASTNode class.
     * @param st SymbolTable of the Semantic Analyzer
     * @return SType of node
     * @throws SemanticException
     * @throws ParseException
     */
    public static SType getType(BoolFactorNode node, SymbolTable st) throws ParseException, SemanticException {
        if (node.right == null)
            return getType(node.left, st);

        SType left = getType(node.left, st);
        SType right = getType(node.right, st);

        if (node instanceof BoolFactorNode.Subtraction) {
            if (left instanceof SType.Real || right instanceof SType.Real) {
                return new SType.Real();
            } else if (left instanceof SType.Int && right instanceof SType.Int)
                return new SType.Int();
            throw new SemanticException("No adequately type",
                    "Only Int or real admitted for subtraction and got " + left + " and " + right + ".");
        } else if (node instanceof BoolFactorNode.Addition) {
            // only types admitted for the addition are int, real or string
            if (left instanceof SType.String || right instanceof SType.String) {
                return new SType.String();
            } else if (left instanceof SType.Real || right instanceof SType.Real) {
                return new SType.Real();
            } else if (left instanceof SType.Int && right instanceof SType.Int)
                return new SType.Int();
            else
                throw new SemanticException("No adequately type",
                        "Only Int, real or string admitted for addition and got " + left + " and " + right + ".");
        }
        throw new SemanticException("Not equal types",
                "The two variables  have different types: " + left + " and " + right + ".");
    }

    /**
     * We return the type of the BoolTermNode passed as argument but checking before if the types of the expressions are right. Once the type checking have been done
     * we return, in case of being all right, a boolean type as these are all conditions
     * @param node Node from ASTNode class.
     * @param st SymbolTable of the Semantic Analyzer
     * @return SType.Bool
     * @throws SemanticException
     * @throws ParseException
     */
    public static SType getType(BoolTermNode node, SymbolTable st) throws ParseException, SemanticException {
        if (node.right == null)
            return getType(node.left, st);

        SType left = getType(node.left, st);
        SType right = getType(node.right, st);

        // These operators <, >, <=, >= are just for reals and integers.
        if (node instanceof BoolTermNode.LEQ || node instanceof BoolTermNode.Lower
                || node instanceof BoolTermNode.Greater || node instanceof BoolTermNode.GEQ) {

            if (((left instanceof SType.Int || left instanceof SType.Real)
                    && (right instanceof SType.Int || right instanceof SType.Real))) {
                return new SType.Bool(); // we just return bool
            } else {
                throw new SemanticException("Not equal types",
                        "The two variables  have different types but got " + left + " and " + right + ".");
            }
        } else { // There operators == <> are for strings, booleans, integers and reals.
            if (((left instanceof SType.Int || left instanceof SType.Real || left instanceof SType.Bool
                    || left instanceof SType.String) &&
                    (right instanceof SType.Int || right instanceof SType.Real || right instanceof SType.Bool
                            || right instanceof SType.String))) {
                return new SType.Bool(); // we just return bool
            } else {
                throw new SemanticException("No adequately type",
                        "Only Int, real, string or boolean admitted but got " + left + " and " + right + ".");
            }

        }
    }

    /**
     * @param node Node from ASTNode class.
     * @param st SymbolTable of the Semantic Analyzer
     * @return The type of the node
     * @throws ParseException
     */
    public static SType getType(CVVNode.Const node, SymbolTable st) throws ParseException {
        return SType.getSType(node.type, st);
    }

    /**
     * @param node Node from ASTNode class.
     * @param st SymbolTable of the Semantic Analyzer
     * @return The type of the node
     * @throws ParseException
     */
    public static SType getType(CVVNode.Val node, SymbolTable st) throws ParseException {
        return SType.getSType(node.type, st);
    }

    /**
     * @param node Node from ASTNode class.
     * @param st SymbolTable of the Semantic Analyzer
     * @return The type of the node
     * @throws ParseException
     */
    public static SType getType(CVVNode.Var node, SymbolTable st) throws ParseException {
        return SType.getSType(node.type, st);
    }

    //    public static SType getType(DeleteNode node, SymbolTable st) throws ParseException { return null; }

    /**
     *  We check that the types of the expressions are right. Once the type checking have been done we return, in case of being all right,
     *  a boolean type as these are all conditions (and, or).
     * @param node Node from ASTNode class.
     * @param st SymbolTable of the Semantic Analyzer
     * @return SType.Bool
     * @throws ParseException
     * @throws SemanticException
     */
    public static SType getType(ExpressionNode node, SymbolTable st) throws ParseException, SemanticException {
        if (node.right == null)
            return getType(node.left, st);

        SType left = getType(node.left, st);
        SType right = getType(node.right, st);

        if (!(right.equals(null))) {
            if (left instanceof SType.Bool && right instanceof SType.Bool) {
                return new SType.Bool();
            } else {
                throw new SemanticException("Not booleans",
                        "Only booleans are admitted but got " + left + " and " + right + ".");
            }
        } else {
            return left;
        }
    }

    //    public static SType getType(FieldDeclarationNode node, SymbolTable st) throws ParseException { return null; }
    //    public static SType getType(ForNode node, SymbolTable st) throws ParseException { return null; }

    /**
     * We check if the node's type is a SType.Function or SType.Record. We only return when correct and throw an error otherwise.
     * @param node Node from ASTNode class.
     * @param st SymbolTable of the Semantic Analyzer
     * @return SType.Function or SType.Record
     * @throws ParseException
     * @throws SemanticException
     */
    public static SType getType(FunctionCallNode node, SymbolTable st) throws ParseException, SemanticException {
        SType ftype = st.get(node.identifier);
        if (ftype instanceof SType.Function func) // proc call, e.g. sum(10, 15)
            return func.returnType;
        else if (ftype instanceof SType.Record rec) // record init, e.g. Person("John", 32)
            return rec;

        throw new SemanticException("Not a procedure or record",
                "Function calls can only be done on procedures or records (not: " + ftype.getClass().getSimpleName()
                        + ").");
    }

    public static SType getType(IdentifierNode node, SymbolTable st) throws ParseException {
        return st.get(node);
    }

    //    public static SType getType(IfNode node, SymbolTable st) throws ParseException { return null; }

    public static SType getType(LiteralNode node, SymbolTable st) throws ParseException, SemanticException {
        if (node instanceof LiteralNode.Int)
            return new SType.Int();
        else if (node instanceof LiteralNode.Real)
            return new SType.Real();
        else if (node instanceof LiteralNode.String)
            return new SType.String();
        else if (node instanceof LiteralNode.Bool)
            return new SType.Bool();
        throw new SemanticException("Instance error", "The node isn't an instance of int/real/string/bool.");
    }

    public static SType getType(ParameterNode node, SymbolTable st) throws ParseException {
        return SType.getSType(node.type, st);
    }

    //    public static SType getType(PrimaryNode node, SymbolTable st) throws ParseException { return null; }
    //    public static SType getType(ProcedureNode node, SymbolTable st) throws ParseException { return null; }
    //    public static SType getType(ProgramNode node, SymbolTable st) throws ParseException { return null; }

    public static SType getType(RecordAccessNode node, SymbolTable st) throws ParseException, SemanticException {
        SType rtype;
        if (node.record instanceof IdentifierNode rec_id) { // identifier of a record variable, e.g. me.name
            rtype = st.get(rec_id);
        } else if (node.record instanceof ArrayAccessNode arr_acc) { // access in a record array, e.g. students[3].name
            rtype = st.get(arr_acc.identifier);
        } else
            throw new SemanticException("Record can be a var or an array access",
                    "You accessed the field of a record but not on a record.");

        if (!(rtype instanceof SType.Id rec_type
                && st.get(new IdentifierNode(rec_type.identifier)) instanceof SType.Record))
            throw new SemanticException("Not a record",
                    "Record accesses can only be done on records (not: " + rtype + ").");

        return st.get(new IdentifierNode(node.record + "." + node.field.name));
    }

    //    public static SType getType(RecordDeclarationNode node, SymbolTable st) throws ParseException { return null; }
    //    public static SType getType(ReturnNode node, SymbolTable st) throws ParseException { return null; }

    public static SType getType(TypeNode node, SymbolTable st) throws ParseException {
        return SType.getSType(node, st);
    }

    //    public static SType getType(WhileNode node, SymbolTable st) throws ParseException { return null; }
}
