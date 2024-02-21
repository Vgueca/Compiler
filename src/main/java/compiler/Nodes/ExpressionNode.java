package compiler.Nodes;

import compiler.CodeGenerator.CodeGenerator;
import compiler.CodeGenerator.Scope;
import compiler.Exceptions.CodeGeneratorException.CodeGeneratorException;
import compiler.Exceptions.CodeGeneratorException.UnexpectedError;
import compiler.Exceptions.CodeGeneratorException.WrongASMObject;
import compiler.Exceptions.CodeGeneratorException.WrongType;
import compiler.Exceptions.SemanticException.SemanticException;
import compiler.SemanticAnalyzer.SemanticAnalyzer;
import compiler.SemanticAnalyzer.SymbolTable;
import compiler.Visitors.PrintVisitor;
import compiler.Visitors.SemanticVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.text.ParseException;
import java.util.Objects;

public class ExpressionNode extends BoolTermNode {
    public ExpressionNode right;

    public ExpressionNode(BoolFactorNode left, ExpressionNode right) {
        super(left);
        this.right = right;
    }

    public ExpressionNode(Expr left) {
        super(left);
    }

    public static class Or extends ExpressionNode {
        public Or(BoolTermNode left, ExpressionNode right) {
            super(left, right);
        }

        @Override
        public String toString() {
            return "(" + left + " or " + right + ")";
        }

        @Override
        public Object getValue(Scope scope) throws WrongType, UnexpectedError {
            Object lv = super.getValue(scope);
            Object rv = right.getValue(scope);

            if (isBool(lv) && isBool(rv))
                return (boolean) lv || (boolean) rv;
            else
                throw new WrongType("Wrong type", "getValue() from ExpressionNode called on non-numeric value.");
        }

        @Override
        public void accept(Object o, Scope scope) throws CodeGeneratorException {
            if (!(o instanceof MethodVisitor mv))
                throw new WrongASMObject("Wrong argument", "Function called with not a MethodVisitor as argument.");

            left.accept(o, scope);
            right.accept(o, scope);
            // TODO maybe IOR (only 1 stack slot) is not always the best e.g. a double takes two slots -> LOR?
            mv.visitInsn(Opcodes.IOR); // OR operation between the two first values on the stack
        }
    }

    public static class And extends ExpressionNode {
        public And(BoolTermNode left, ExpressionNode right) {
            super(left, right);
        }

        @Override
        public String toString() {
            return "(" + left + " and " + right + ")";
        }

        @Override
        public Object getValue(Scope scope) throws WrongType, UnexpectedError {
            Object lv = super.getValue(scope);
            Object rv = right.getValue(scope);

            if (isBool(lv) && isBool(rv))
                return (boolean) lv && (boolean) rv;
            else
                throw new WrongType("Wrong type", "getValue() from ExpressionNode called on non-numeric value.");
        }

        @Override
        public void accept(Object o, Scope scope) throws CodeGeneratorException {
            if (!(o instanceof MethodVisitor mv))
                throw new WrongASMObject("Wrong argument", "Function called with not a MethodVisitor as argument.");

            left.accept(o, scope);
            right.accept(o, scope);
            // TODO maybe IAND (only 1 stack slot) is not always the best e.g. a double takes two slots -> LAND?
            mv.visitInsn(Opcodes.IAND); // AND operation between the two first values on the stack
        }
    }

    @Override
    public String getDescriptor(Scope scope) throws WrongType, UnexpectedError {
        if (right == null)
            return left.getDescriptor(scope);
        if (left.isBool(scope) && right.isBool(scope))
            return "Z";
        throw new WrongType("Wrong type", "getDescriptor() from ExpressionNode called on non-boolean value.");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (right == null)
            return left.equals(o); //  Comparing parents' expressions.
        if (o == null || getClass() != o.getClass())
            return false;
        ExpressionNode that = (ExpressionNode) o;
        return Objects.equals(left, that.left) && Objects.equals(right, that.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, right);
    }

    // -------------------------------------------------------------------------
    @Override
    public void accept(PrintVisitor visitor, int depth) {
        if (right == null) {
            left.accept(visitor, depth);
            return;
        }
        visitor.visit(this, depth);
        left.accept(visitor, depth + 1);
        right.accept(visitor, depth + 1);
    }

    @Override
    public void accept(SemanticVisitor visitor, SymbolTable st) throws ParseException, SemanticException {
        visitor.visit(this, st);

        // TODO check if this updates correctly when the SV is run
        descriptor = CodeGenerator.nodeToASMType(SemanticAnalyzer.getType(this, st));

        left.accept(visitor, st);
        if (right != null)
            right.accept(visitor, st);
    }

    @Override
    public void accept(Object o, Scope scope) throws CodeGeneratorException {
        if (!(o instanceof MethodVisitor || o instanceof ClassWriter))
            throw new WrongASMObject("Wrong argument", "Function called with not a MethodVisitor or a ClassWriter as argument.");

        left.accept(o, scope);
        // there's no right part in an ExpressionNode that is not an And / Or.
    }

    // -------------------------------------------------------------------------
}
