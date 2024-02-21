package compiler.Nodes;

import compiler.CodeGenerator.Scope;
import compiler.Exceptions.CodeGeneratorException.CodeGeneratorException;
import compiler.Exceptions.CodeGeneratorException.UnexpectedError;
import compiler.Exceptions.CodeGeneratorException.WrongASMObject;
import compiler.Exceptions.CodeGeneratorException.WrongType;
import compiler.Exceptions.SemanticException.SemanticException;
import compiler.SemanticAnalyzer.SymbolTable;
import compiler.Visitors.PrintVisitor;
import compiler.Visitors.SemanticVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import java.text.ParseException;
import java.util.Objects;

import static org.objectweb.asm.Opcodes.INEG;

public class ArithFactorNode extends PrimaryNode {
    public Expr left;

    public ArithFactorNode(Expr left) {
        super(left.descriptor);
        this.left = left;
    }

    @Override
    public int hashCode() {
        return Objects.hash(left);
    }

    @Override
    public Object getValue(Scope scope) throws WrongType, UnexpectedError {
        return left.getValue(scope);
    }

    @Override
    public String getDescriptor(Scope scope) throws WrongType, UnexpectedError {
        return left.getDescriptor(scope);
    }

    // -------------------------------------------------------------------------

    @Override
    public void accept(PrintVisitor visitor, int depth) {
        left.accept(visitor, depth); // we don't visit this if the instance isn't a Positive or Negative.
    }

    @Override
    public void accept(SemanticVisitor visitor, SymbolTable st) throws ParseException, SemanticException {
        left.accept(visitor, st);
    }

    public void accept(Object o, Scope scope) throws CodeGeneratorException {
        if (!(o instanceof MethodVisitor || o instanceof ClassWriter))
            throw new WrongASMObject("Wrong argument","Function called with not a MethodVisitor as argument.");

        left.accept(o, scope);
    }

    // -------------------------------------------------------------------------

    public static class Positive extends ArithFactorNode {
        public Positive(PrimaryNode prim) {
            super(prim);
        }

        @Override
        public String toString() {
            return "+" + left.toString();
        }

        @Override
        public void accept(PrintVisitor visitor, int depth) {
            visitor.visit(this, depth);
            left.accept(visitor, depth + 1);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || getClass() != obj.getClass())
                return false;
            return left.equals(obj);
        }
    }

    public static class Negative extends ArithFactorNode {
        public Negative(PrimaryNode prim) {
            super(prim);
        }

        @Override
        public String toString() {
            return "-" + left.toString();
        }

        @Override
        public Object getValue(Scope scope) throws WrongType, UnexpectedError {
            Object val = super.getValue(scope);
            if (val instanceof Integer i)
                return -i;
            else if (val instanceof Double d)
                return -d;
            else
                throw new WrongType("Wrong type", "getValue() from ArithTermNode called on non-numeric value.");
        }

        @Override
        public void accept(PrintVisitor visitor, int depth) {
            visitor.visit(this, depth);
            left.accept(visitor, depth + 1);
        }

        @Override
        public void accept(Object o, Scope scope) throws CodeGeneratorException {
            if (!(o instanceof MethodVisitor mv))
                throw new WrongASMObject("Wrong argument", "Function called with not a MethodVisitor as argument.");

            left.accept(mv, scope);
            mv.visitInsn(left.descriptor.getOpcode(INEG)); // pops the top of the stack and pushes the opposite value back
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || getClass() != obj.getClass())
                return false;
            return left.equals(obj);
        }
    }

    @Override
    public boolean equals(Object o) {
        return left.equals(o);
    }

    @Override
    public String toString() {
        return left.toString();
    }
}
