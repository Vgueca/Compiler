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

import java.text.ParseException;
import java.util.Objects;

import static compiler.CodeGenerator.CodeGenerator.concat;
import static org.objectweb.asm.Opcodes.*;

public class BoolFactorNode extends ArithTermNode {
    public BoolFactorNode right;

    private BoolFactorNode(ArithTermNode left, BoolFactorNode right) {
        super(left);
        this.right = right;
    }

    public BoolFactorNode(Expr left) {
        super(left);
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
        // there's no right part in a BoolFactorNode that is not an Addition / Subtraction.
    }
    // -------------------------------------------------------------------------

    public static class Addition extends BoolFactorNode {
        public Addition(ArithTermNode left, BoolFactorNode right) {
            super(left, right);
        }

        @Override
        public String toString() {
            return "(" + left + " + " + right + ")";
        }

        @Override
        public String getDescriptor(Scope scope) throws WrongType, UnexpectedError {
            if (left.isString(scope) || right.isString(scope))
                return "Ljava/lang/String;";
            return super.getDescriptor(scope);
        }

        @Override
        public Object getValue(Scope scope) throws WrongType, UnexpectedError {
            Object lv = super.getValue(scope);
            Object rv = right.getValue(scope);

            if (isString(scope))
                return lv.toString() + rv.toString(); // concat
            else if (isInt(scope))
                return (int) lv + (int) rv;
            else if (isReal(scope))
                return ((Number) lv).doubleValue() + ((Number) rv).doubleValue();
            throw new WrongType("Wrong type", "getValue() from BoolFactorNode called on non-numeric value.");
        }

        @Override
        public void accept(Object o, Scope scope) throws CodeGeneratorException {
            if (!(o instanceof MethodVisitor mv))
                throw new RuntimeException("CG: Procedure node called with wrong type.");

            if (isString(scope)) {
                left.accept(o, scope);
                if (!left.isString(scope))
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/String", "valueOf",
                            "(" + left.getDescriptor(scope) + ")Ljava/lang/String;", false);
                right.accept(o, scope);
                if (!right.isString(scope))
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/String", "valueOf",
                            "(" + right.getDescriptor(scope) + ")Ljava/lang/String;", false);
                concat(mv);
                return;
            }

            int op = implicitConversion(mv, left, right, IADD, DADD, scope, o);
            mv.visitInsn(op); // ADD operation between the two values on the stack
        }
    }

    public static class Subtraction extends BoolFactorNode {
        public Subtraction(ArithTermNode left, BoolFactorNode right) {
            super(left, right);
        }

        @Override
        public String toString() {
            return "(" + left + " - " + right + ")";
        }

        @Override
        public Object getValue(Scope scope) throws WrongType, UnexpectedError {
            Object lv = super.getValue(scope);
            Object rv = right.getValue(scope);

            if (isInt(scope))
                return (int) lv - (int) rv;
            else if (isReal(scope))
                return ((Number) lv).doubleValue() - ((Number) rv).doubleValue();
            throw new WrongType("Wrong type", "getValue() from BoolFactorNode called on non-numeric value.");
        }

        @Override
        public void accept(Object o, Scope scope) throws CodeGeneratorException {
            if (!(o instanceof MethodVisitor mv))
                throw new WrongASMObject("Wrong argument","Function called with not a MethodVisitor as argument.");

            int op = implicitConversion(mv, left, right, ISUB, DSUB, scope, o);
            mv.visitInsn(op); // SUB operation between the two values on the stack
        }
    }

    @Override
    public String getDescriptor(Scope scope) throws WrongType, UnexpectedError {
        if (right == null)
            return left.getDescriptor(scope);

        if (left.isReal(scope) || right.isReal(scope))
            return "D";
        else if (left.isInt(scope) || right.isInt(scope))
            return "I";
        throw new WrongType("Wrong type", "getDescriptor() from BoolFactorNode called on non-numeric value.");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (right == null)
            return super.equals(o); //  Comparing parents' expressions.
        if (o == null)
            return false;
        if (getClass() != o.getClass())
            return o.equals(this);
        BoolFactorNode that = (BoolFactorNode) o;
        return Objects.equals(left, that.left) && Objects.equals(right, that.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, right);
    }
}