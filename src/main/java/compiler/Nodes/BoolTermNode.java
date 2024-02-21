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
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.text.ParseException;
import java.util.Objects;

import static compiler.CodeGenerator.CodeGenerator.swapStack;
import static org.objectweb.asm.Opcodes.*;

public class BoolTermNode extends BoolFactorNode {
    public BoolTermNode right;

    private BoolTermNode(BoolFactorNode left, BoolTermNode right) {
        super(left);
        this.right = right;
    }

    public BoolTermNode(Expr left) {
        super(left);
    }

    public void convertToBool(MethodVisitor mv, int OP, Scope scope) throws WrongType, UnexpectedError {
        // if true, go to true label (and push 1), else run false (push 0 and skip true)
        Label trueLabel = new Label();

        if (left.isInt(scope) && right.isInt(scope))
            mv.visitJumpInsn(OP, trueLabel);
        else {
            // implicit conversion to doubles
            if (left.isReal(scope) && right.isInt(scope))
                mv.visitInsn(I2D);
            else if (left.isInt(scope) && right.isReal(scope)) {
                swapStack(mv, 1, 2);
                mv.visitInsn(I2D);
                swapStack(mv, 2, 2);
            }

            if (OP == IF_ICMPGT || OP == IF_ICMPGE) {
                mv.visitInsn(DCMPG); // compare doubles and push 1 if left > right, -1 if left < right , 0 if equal
                mv.visitJumpInsn(OP == IF_ICMPGT ? IFGT : IFGE, trueLabel);
            } else {
                mv.visitInsn(DCMPL); // compare doubles and push 1 if left < right, -1 if left > right , 0 if equal
                if (OP == IF_ICMPLT)
                    mv.visitJumpInsn(IFLT, trueLabel);
                else if (OP == IF_ICMPLE)
                    mv.visitJumpInsn(IFLE, trueLabel);
                else if (OP == IF_ICMPEQ)
                    mv.visitJumpInsn(IFEQ, trueLabel);
                else if (OP == IF_ICMPNE)
                    mv.visitJumpInsn(IFNE, trueLabel);
            }
        }

        // false
        mv.visitLdcInsn(false); // pushes 0 to the stack
        Label endLabel = new Label();
        mv.visitJumpInsn(GOTO, endLabel); // jump to end

        // true
        mv.visitLabel(trueLabel);
        mv.visitLdcInsn(true); // pushes 1 to the stack

        // end
        mv.visitLabel(endLabel);
    }

    @Override
    public String getDescriptor(Scope scope) throws WrongType, UnexpectedError {
        if (right != null)
            return "Z";
        return left.getDescriptor(scope);
    }

    public static class Lower extends BoolTermNode {
        public Lower(BoolFactorNode left, BoolTermNode astNode) {
            super(left, astNode);
        }

        @Override
        public String toString() {
            return "(" + left + " < " + right + ")";
        }

        @Override
        public Object getValue(Scope scope) throws WrongType, UnexpectedError {
            Object lv = super.getValue(scope);
            Object rv = right.getValue(scope);

            if (left.isReal(scope) || right.isReal(scope))
                return ((Number) lv).doubleValue() < ((Number) rv).doubleValue();
            else if (left.isInt(scope) && right.isInt(scope))
                return (int) lv < (int) rv;
            else
                throw new WrongType("Wrong type", "getValue() from BoolTermNode called on non-numeric value.");
        }

        @Override
        public void accept(Object o, Scope scope) throws CodeGeneratorException {
            if (!(o instanceof MethodVisitor mv))
                throw new WrongASMObject("Wrong argument", "Function called with not a MethodVisitor as argument.");

            left.accept(o, scope);
            right.accept(o, scope);
            convertToBool(mv, IF_ICMPLT, scope);
        }
    }

    public static class LEQ extends BoolTermNode {
        public LEQ(BoolFactorNode left, BoolTermNode astNode) {
            super(left, astNode);
        }

        @Override
        public String toString() {
            return "(" + left + " <= " + right + ")";
        }

        @Override
        public Object getValue(Scope scope) throws WrongType, UnexpectedError {
            Object lv = super.getValue(scope);
            Object rv = right.getValue(scope);

            if (left.isReal(scope) || right.isReal(scope))
                return ((Number) lv).doubleValue() <= ((Number) rv).doubleValue();
            else if (left.isInt(scope) && right.isInt(scope))
                return (int) lv <= (int) rv;
            else
                throw new WrongType("Wrong type", "getValue() from BoolTermNode called on non-numeric value.");
        }

        @Override
        public void accept(Object o, Scope scope) throws CodeGeneratorException {
            if (!(o instanceof MethodVisitor mv))
                throw new WrongASMObject("Wrong argument", "Function called with not a MethodVisitor as argument.");

            left.accept(o, scope);
            right.accept(o, scope);
            convertToBool(mv, IF_ICMPLE, scope);
        }
    }

    public static class Greater extends BoolTermNode {
        public Greater(BoolFactorNode left, BoolTermNode astNode) {
            super(left, astNode);
        }

        @Override
        public String toString() {
            return "(" + left + " > " + right + ")";
        }

        @Override
        public Object getValue(Scope scope) throws WrongType, UnexpectedError {
            Object lv = super.getValue(scope);
            Object rv = right.getValue(scope);

            if (left.isReal(scope) || right.isReal(scope))
                return ((Number) lv).doubleValue() > ((Number) rv).doubleValue();
            else if (left.isInt(scope) && right.isInt(scope))
                return (int) lv > (int) rv;
            else
                throw new WrongType("Wrong type", "getValue() from BoolTermNode called on non-numeric value.");
        }

        @Override
        public void accept(Object o, Scope scope) throws CodeGeneratorException {
            if (!(o instanceof MethodVisitor mv))
                throw new WrongASMObject("Wrong argument", "Function called with not a MethodVisitor as argument.");

            left.accept(o, scope);
            right.accept(o, scope);
            convertToBool(mv, IF_ICMPGT, scope);
        }
    }

    public static class GEQ extends BoolTermNode {
        public GEQ(BoolFactorNode left, BoolTermNode astNode) {
            super(left, astNode);
        }

        @Override
        public String toString() {
            return "(" + left + " >= " + right + ")";
        }

        @Override
        public Object getValue(Scope scope) throws WrongType, UnexpectedError {
            Object lv = super.getValue(scope);
            Object rv = right.getValue(scope);

            if (left.isReal(scope) || right.isReal(scope))
                return ((Number) lv).doubleValue() >= ((Number) rv).doubleValue();
            else if (left.isInt(scope) && right.isInt(scope))
                return (int) lv >= (int) rv;
            else
                throw new WrongType("Wrong type", "getValue() from BoolTermNode called on non-numeric value.");
        }

        @Override
        public void accept(Object o, Scope scope) throws CodeGeneratorException {
            if (!(o instanceof MethodVisitor mv))
                throw new WrongASMObject("Wrong argument", "Function called with not a MethodVisitor as argument.");

            left.accept(o, scope);
            right.accept(o, scope);
            convertToBool(mv, IF_ICMPGE, scope);
        }
    }

    public static class Equal extends BoolTermNode {
        public Equal(BoolFactorNode left, BoolTermNode astNode) {
            super(left, astNode);
        }

        @Override
        public String toString() {
            return "(" + left + " == " + right + ")";
        }

        @Override
        public Object getValue(Scope scope) throws WrongType, UnexpectedError {
            Object lv = super.getValue(scope);
            Object rv = right.getValue(scope);

            if (left.isReal(scope) || right.isReal(scope))
                return ((Number) lv).doubleValue() == ((Number) rv).doubleValue();
            else if (left.isInt(scope) && right.isInt(scope))
                return (int) lv == (int) rv;
            else if (left.isBool(scope) && right.isBool(scope))
                return (boolean) lv == (boolean) rv;
            else
                throw new WrongType("Wrong type", "getValue() from BoolTermNode called on non-numeric value.");
        }

        @Override
        public void accept(Object o, Scope scope) throws CodeGeneratorException {
            if (!(o instanceof MethodVisitor mv))
                throw new WrongASMObject("Wrong argument", "Function called with not a MethodVisitor as argument.");

            left.accept(o, scope);
            right.accept(o, scope);
            convertToBool(mv, IF_ICMPEQ, scope);
        }
    }

    public static class Different extends BoolTermNode {
        public Different(BoolFactorNode left, BoolTermNode astNode) {
            super(left, astNode);
        }

        @Override
        public String toString() {
            return "(" + left + " <> " + right + ")";
        }

        @Override
        public Object getValue(Scope scope) throws WrongType, UnexpectedError {
            Object lv = super.getValue(scope);
            Object rv = right.getValue(scope);

            if (left.isReal(scope) || right.isReal(scope))
                return ((Number) lv).doubleValue() != ((Number) rv).doubleValue();
            else if (left.isInt(scope) && right.isInt(scope))
                return (int) lv != (int) rv;
            else if (left.isBool(scope) && right.isBool(scope))
                return (boolean) lv != (boolean) rv;
            else
                throw new WrongType("Wrong type", "getValue() from BoolTermNode called on non-numeric value.");
        }

        @Override
        public void accept(Object o, Scope scope) throws CodeGeneratorException {
            if (!(o instanceof MethodVisitor mv))
                throw new WrongASMObject("Wrong argument", "Function called with not a MethodVisitor as argument.");

            left.accept(o, scope);
            right.accept(o, scope);
            convertToBool(mv, IF_ICMPNE, scope);
        }
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
        BoolTermNode that = (BoolTermNode) o;
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
        if (!(o instanceof ClassWriter || o instanceof MethodVisitor))
            throw new WrongASMObject("Wrong argument", "Function called with not a MethodVisitor or a ClassWriter as argument.");

        left.accept(o, scope);
        // there's no right part in a BoolFactorNode that is not an Addition / Subtraction.
    }

    // -------------------------------------------------------------------------
}
