package compiler.Nodes;

import compiler.CodeGenerator.CodeGenerator;
import compiler.CodeGenerator.Scope;
import compiler.Exceptions.CodeGeneratorException.*;
import compiler.Exceptions.SemanticException.SemanticException;
import compiler.SemanticAnalyzer.SemanticAnalyzer;
import compiler.SemanticAnalyzer.SymbolTable;
import compiler.Visitors.PrintVisitor;
import compiler.Visitors.SemanticVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import java.text.ParseException;
import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;

public class ArithTermNode extends ArithFactorNode {
    public ArithTermNode right;

    private ArithTermNode(ArithFactorNode left, ArithTermNode right) {
        super(left);
        this.right = right;
    }

    public ArithTermNode(Expr left) {
        super(left);
    }

    public static class Multiplication extends ArithTermNode {
        public Multiplication(ArithFactorNode left, ArithTermNode right) {
            super(left, right);
        }

        @Override
        public String toString() {
            return "(" + left + " * " + right + ")";
        }

        @Override
        public Object getValue(Scope scope) throws WrongType, UnexpectedError {
            Object lv = super.getValue(scope);
            Object rv = right.getValue(scope);

            if (isReal(scope))
                return ((Number) lv).doubleValue() * ((Number) rv).doubleValue();
            else if (isInt(scope))
                return (int) lv * (int) rv;
            else
                throw new WrongType("Wrong type", "getValue() from ArithTermNode called on non-numeric value.");
        }

        @Override
        public void accept(Object o, Scope scope) throws CodeGeneratorException {
            if (!(o instanceof MethodVisitor mv))
                throw new WrongASMObject("Wrong argument", "Function called with not a MethodVisitor as argument.");

            int op = implicitConversion(mv, left, right, IMUL, DMUL, scope, o);
            mv.visitInsn(op); // MUL operation between the two values on the stack
        }
    }

    public static class Division extends ArithTermNode {
        public Division(ArithFactorNode left, ArithTermNode right) {
            super(left, right);
        }

        @Override
        public String toString() {
            return "(" + left + " / " + right + ")";
        }

        @Override
        public Object getValue(Scope scope) throws WrongType, UnexpectedError {
            Object lv = super.getValue(scope);
            Object rv = right.getValue(scope);

            if (isReal(scope))
                return ((Number) lv).doubleValue() / ((Number) rv).doubleValue();
            else if (isInt(scope))
                return (int) lv / (int) rv;
            else
                throw new WrongType("Wrong type", "getValue() from ArithTermNode called on non-numeric value.");
        }

        @Override
        public void accept(Object o, Scope scope) throws CodeGeneratorException {
            if (!(o instanceof MethodVisitor mv))
                throw new WrongASMObject("Wrong argument", "Function called with not a MethodVisitor as argument.");

            int op = implicitConversion(mv, left, right, IDIV, DDIV, scope, o);
            mv.visitInsn(op); // DIV operation between the two values on the stack
        }
    }

    public static class Modulo extends ArithTermNode {
        public Modulo(ArithFactorNode left, ArithTermNode right) {
            super(left, right);
        }

        @Override
        public String toString() {
            return "(" + left + " % " + right + ")";
        }

        @Override
        public Object getValue(Scope scope) throws WrongType, UnexpectedError {
            Object lv = super.getValue(scope);
            Object rv = right.getValue(scope);

            if (isInt(scope))
                return (int) lv % (int) rv;
            else
                throw new WrongType("Wrong type", "getValue() from ArithTermNode called on non-numeric value.");
        }

        @Override
        public String getDescriptor(Scope scope) throws WrongType, UnexpectedError {
            if (right == null)
                return left.getDescriptor(scope);
            if (left.isInt(scope) && right.isInt(scope))
                return "I";
            throw new WrongType("Wrong type", "Module (%) operation called with wrong types.");
        }

        @Override
        public void accept(Object o, Scope scope) throws CodeGeneratorException {
            if (!(o instanceof MethodVisitor mv))
                throw new WrongASMObject("Wrong argument", "Function called with not a MethodVisitor as argument.");

            int op = implicitConversion(mv, left, right, IREM, DREM, scope, o);
            mv.visitInsn(op); // REM operation between the two values on the stack
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (right == null)
            return super.equals(o); //  Comparing parents' expressions.
        if (o == null || getClass() != o.getClass())
            return false;
        ArithTermNode that = (ArithTermNode) o;
        return Objects.equals(left, that.left) && Objects.equals(right, that.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, right);
    }

    @Override
    public String getDescriptor(Scope scope) throws WrongType, UnexpectedError {
        if (right == null)
            return left.getDescriptor(scope);
        if (left.isReal(scope) || right.isReal(scope))
            return "D";
        else if (left.isInt(scope) && right.isInt(scope))
            return "I";
        throw new WrongType("Wrong type", "getDescriptor() from ArithTermNode called on non-numeric value.");
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
        if (!(o instanceof ClassWriter || o instanceof MethodVisitor))
            throw new WrongASMObject("Wrong argument",
                    "Function called with not a MethodVisitor or a ClassWriter as argument.");

        left.accept(o, scope);
        // there's no right part in a BoolFactorNode that is not an Multiplication / Division / Modulo.
    }
    // -------------------------------------------------------------------------
}
