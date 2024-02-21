package compiler.Nodes;

import compiler.CodeGenerator.Scope;
import compiler.Exceptions.CodeGeneratorException.CodeGeneratorException;
import compiler.Exceptions.CodeGeneratorException.UnexpectedError;
import compiler.Exceptions.CodeGeneratorException.WrongType;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.Objects;

import static org.objectweb.asm.Opcodes.I2D;

public abstract class Expr extends ASTNode {
    public Type descriptor;

    public Expr(Type descriptor) {
        this.descriptor = descriptor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Expr expr = (Expr) o;
        return Objects.equals(descriptor, expr.descriptor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(descriptor);
    }

    public abstract Object getValue(Scope scope) throws WrongType, UnexpectedError;

    public abstract String getDescriptor(Scope scope) throws WrongType, UnexpectedError;

    protected boolean isInt(Object o) {
        return o instanceof Integer;
    }

    protected boolean isInt(Scope scope) throws WrongType, UnexpectedError {
        return getDescriptor(scope).equals("I");
    }

    protected boolean isReal(Object o) {
        return o instanceof Double;
    }

    protected boolean isReal(Scope scope) throws WrongType, UnexpectedError {
        return getDescriptor(scope).equals("D");
    }

    protected boolean isString(Object o) {
        return o instanceof String;
    }

    protected boolean isString(Scope scope) throws WrongType, UnexpectedError {
        return getDescriptor(scope).equals("Ljava/lang/String;");
    }

    protected boolean isBool(Object o) {
        return o instanceof Boolean;
    }

    protected boolean isBool(Scope scope) throws WrongType, UnexpectedError {
        return getDescriptor(scope).equals("Z");
    }

    protected int implicitConversion(MethodVisitor mv, Expr left, Expr right, int IOP, int DOP, Scope scope, Object o) throws CodeGeneratorException {
        int op = isInt(scope) ? IOP : (isReal(scope) ? DOP : -42);
        if (op == -42)
            throw new RuntimeException("Invalid implicit conversion");
        left.accept(o, scope);
        if (op == DOP && left.isInt(scope))
            mv.visitInsn(I2D);
        right.accept(o, scope);
        if (op == DOP && right.isInt(scope))
            mv.visitInsn(I2D);
        return op;
    }
}
