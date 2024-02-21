package compiler.Nodes;

import compiler.CodeGenerator.Scope;
import compiler.Exceptions.CodeGeneratorException.WrongASMObject;
import compiler.SemanticAnalyzer.SymbolTable;
import compiler.Visitors.PrintVisitor;
import compiler.Visitors.SemanticVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.text.ParseException;
import java.util.Objects;

public abstract class LiteralNode extends PrimaryNode {
    public LiteralNode(Type descriptor) {
        super(descriptor);
    }

    public static class Int extends LiteralNode {
        int content;

        public Int(java.lang.String content) {
            super(Type.INT_TYPE);
            this.content = Integer.parseInt(content);
        }

        @Override
        public java.lang.String toString() {
            return java.lang.String.valueOf(content) + "_I";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o instanceof ArithFactorNode af)
                return af.equals(this);
            if (o == null || getClass() != o.getClass())
                return false;
            Int anInt = (Int) o;
            return content == anInt.content;
        }

        @Override
        public int hashCode() {
            return Objects.hash(content);
        }

        @Override
        public Object getValue(Scope scope) {
            return content;
        }

        @Override
        public java.lang.String getDescriptor(Scope scope) {
            return "I";
        }

        // -------------------------------------------------------------------------
        @Override
        public void accept(PrintVisitor visitor, int depth) {
            visitor.visit(this, depth);
            System.out.println("\t".repeat(depth + 1) + content);
        }

        @Override
        public void accept(SemanticVisitor visitor, SymbolTable st) throws ParseException {
            visitor.visit(this, st);
        }

        @Override
        public void accept(Object o, Scope scope) throws WrongASMObject {
            if (o instanceof MethodVisitor mv)
                mv.visitLdcInsn(content);
            else if (o instanceof ClassWriter cw)
                cw.newConst(content);
            else
                throw new WrongASMObject("Wrong argument", "Function called with not a MethodVisitor or a ClassWriter as argument.");

        }

        // -------------------------------------------------------------------------
    }

    public static class Real extends LiteralNode {
        double content;

        public Real(java.lang.String content) {
            super(Type.DOUBLE_TYPE);
            this.content = Double.parseDouble(content);
        }

        @Override
        public java.lang.String toString() {
            return java.lang.String.valueOf(content) + "_R";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o instanceof ArithFactorNode af)
                return af.equals(this);
            if (o == null || getClass() != o.getClass())
                return false;
            Real real = (Real) o;
            return Double.compare(real.content, content) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(content);
        }

        @Override
        public Object getValue(Scope scope) {
            return content;
        }

        @Override
        public java.lang.String getDescriptor(Scope scope) {
            return "D";
        }

        // -------------------------------------------------------------------------
        @Override
        public void accept(PrintVisitor visitor, int depth) {
            visitor.visit(this, depth);
            System.out.println("\t".repeat(depth + 1) + content);
        }

        @Override
        public void accept(SemanticVisitor visitor, SymbolTable st) throws ParseException {
            visitor.visit(this, st);
        }

        @Override
        public void accept(Object o, Scope scope) throws WrongASMObject {
            if (!(o instanceof MethodVisitor mv))
                throw new WrongASMObject("Wrong argument", "Function called with not a MethodVisitor as argument.");


            mv.visitLdcInsn(content);
        }

        // -------------------------------------------------------------------------
    }

    public static class String extends LiteralNode {
        java.lang.String content;

        public String(java.lang.String content) {
            super(Type.CHAR_TYPE);
            this.content = content;
        }

        @Override
        public java.lang.String toString() {
            return content + "_S";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o instanceof ArithFactorNode af)
                return af.equals(this);
            if (o == null || getClass() != o.getClass())
                return false;
            String string = (String) o;
            return Objects.equals(content, string.content);
        }

        @Override
        public int hashCode() {
            return Objects.hash(content);
        }

        @Override
        public Object getValue(Scope scope) {
            return content;
        }

        @Override
        public java.lang.String getDescriptor(Scope scope) {
            return "Ljava/lang/String;";
        }

        // -------------------------------------------------------------------------
        @Override
        public void accept(PrintVisitor visitor, int depth) {
            visitor.visit(this, depth);
            System.out.println("\t".repeat(depth + 1) + content);
        }

        @Override
        public void accept(SemanticVisitor visitor, SymbolTable st) throws ParseException {
            visitor.visit(this, st);
        }

        @Override
        public void accept(Object o, Scope scope) throws WrongASMObject {
            if (!(o instanceof MethodVisitor mv))
                throw new WrongASMObject("Wrong argument", "Function called with not a MethodVisitor as argument.");

            //TODO is this wrong?
            mv.visitLdcInsn(content);
        }

        // -------------------------------------------------------------------------
    }

    public static class Bool extends LiteralNode {
        boolean content;

        public Bool(java.lang.String bool) {
            super(Type.BOOLEAN_TYPE);
            this.content = Boolean.parseBoolean(bool);
        }

        @Override
        public java.lang.String toString() {
            return java.lang.String.valueOf(content) + "_B";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o instanceof ArithFactorNode af)
                return af.equals(this);
            if (o == null || getClass() != o.getClass())
                return false;
            Bool bool = (Bool) o;
            return content == bool.content;
        }

        @Override
        public int hashCode() {
            return Objects.hash(content);
        }

        @Override
        public Object getValue(Scope scope) {
            return content;
        }

        @Override
        public java.lang.String getDescriptor(Scope scope) {
            return "Z";
        }

        // -------------------------------------------------------------------------
        @Override
        public void accept(PrintVisitor visitor, int depth) {
            visitor.visit(this, depth);
            System.out.println("\t".repeat(depth + 1) + content);
        }

        @Override
        public void accept(SemanticVisitor visitor, SymbolTable st) throws ParseException {
            visitor.visit(this, st);
        }

        @Override
        public void accept(Object o, Scope scope) throws WrongASMObject {
            if (!(o instanceof MethodVisitor mv))
                throw new WrongASMObject("Wrong argument", "Function called with not a MethodVisitor as argument.");

            mv.visitLdcInsn(content);
        }

        // -------------------------------------------------------------------------
    }

    @Override
    public java.lang.String toString() {
        return "{LiteralNode}";
    }

    @Override
    public boolean equals(Object obj) {
        if (this instanceof LiteralNode.Int lnI)
            return lnI.equals(obj);
        if (this instanceof LiteralNode.Bool lnB)
            return lnB.equals(obj);
        if (this instanceof LiteralNode.Real lnR)
            return lnR.equals(obj);
        if (this instanceof LiteralNode.String lnS)
            return lnS.equals(obj);

        return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
