package compiler.Nodes;

import compiler.CodeGenerator.Scope;
import compiler.Exceptions.CodeGeneratorException.UnexpectedError;
import compiler.Exceptions.CodeGeneratorException.WrongASMObject;
import compiler.Exceptions.CodeGeneratorException.WrongType;
import compiler.Lexer.Token;
import compiler.SemanticAnalyzer.SymbolTable;
import compiler.Visitors.PrintVisitor;
import compiler.Visitors.SemanticVisitor;
import org.objectweb.asm.MethodVisitor;

import java.text.ParseException;
import java.util.Objects;

public abstract class TypeNode extends ASTNode {
    public static class Base extends TypeNode {
        public Token token;

        // -------------------------------------------------------------------------
        @Override
        public void accept(PrintVisitor visitor, int depth) {
            visitor.visit(this, depth);
            System.out.println("\t".repeat(depth + 1) + token);
        }

        @Override
        public void accept(SemanticVisitor visitor, SymbolTable st) throws ParseException {
            visitor.visit(this, st);
        }

        @Override
        public void accept(Object o, Scope scope) {

        }

        // -------------------------------------------------------------------------

        public String getDescriptor() throws WrongType {
            return switch (token) {
                case INTTYPE -> "I";
                case REALTYPE -> "D";
                case STRINGTYPE -> "Ljava/lang/String;";
                case BOOLTYPE -> "Z";
                default -> throw new WrongType("Wrong type", "getDescriptor() from TypeNode called on non right value.");
            };
        }

        /**
         * The token can be INTTYPE, REALTYPE, STRINGTYPE, or BOOLTYPE.
         *
         * @param token type of literal.
         * @throws ParseException if it's not the right token.
         */
        public Base(Token token) throws ParseException {
            switch (token) {
                case INTTYPE, REALTYPE, STRINGTYPE, BOOLTYPE -> this.token = token;
                default ->
                    throw new ParseException("Basic TypeNode created but not with int, real, string or bool.", 0);
            }
        }

        @Override
        public String toString() {
            return token.toString() + "_base";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Base base = (Base) o;
            return token == base.token;
        }

        @Override
        public int hashCode() {
            return Objects.hash(token);
        }

    }

    public static class Identifier extends TypeNode {
        public IdentifierNode identifier;

        // -------------------------------------------------------------------------
        @Override
        public void accept(PrintVisitor visitor, int depth) {
            visitor.visit(this, depth);
            depth++;
            identifier.accept(visitor, depth);
        }

        @Override
        public void accept(SemanticVisitor visitor, SymbolTable st) throws ParseException {
            visitor.visit(this, st);
        }

        @Override
        public void accept(Object o, Scope scope) {

        }
        // -------------------------------------------------------------------------

        public String getDescriptor() throws UnexpectedError {
            return "L" + identifier.name + ";"; // This is only for a record I think.
        }

        public Identifier(IdentifierNode identifier) {
            this.identifier = identifier;
        }

        @Override
        public String toString() {
            return identifier.toString() + "_id";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Identifier that = (Identifier) o;
            return Objects.equals(identifier, that.identifier);
        }

        @Override
        public int hashCode() {
            return Objects.hash(identifier);
        }
    }

    public static class Array extends TypeNode {
        public TypeNode.Base baseType;

        public Array(TypeNode.Base baseType) {
            this.baseType = baseType;
        }

        // -------------------------------------------------------------------------
        @Override
        public void accept(PrintVisitor visitor, int depth) {
            visitor.visit(this, depth);
            depth++;
            baseType.accept(visitor, depth);
        }

        @Override
        public void accept(SemanticVisitor visitor, SymbolTable st) throws ParseException {
            visitor.visit(this, st);
        }

        @Override
        public void accept(Object o, Scope scope) throws WrongASMObject {
            if (!(o instanceof MethodVisitor mv))
                throw new WrongASMObject("Wrong argument","Function called with not a MethodVisitor as argument.");

            /*
            int storeOpcode = AASTORE;
            
            switch ( this.baseType.token ){
                case INTTYPE -> {
                    mv.visitIntInsn(NEWARRAY, T_LONG);
                    storeOpcode = LASTORE;
                }
            }*/

        }

        // -------------------------------------------------------------------------

        public String getDescriptor() throws WrongType {
            return "[" + baseType.getDescriptor();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Array array = (Array) o;
            return Objects.equals(baseType, array.baseType);
        }

        @Override
        public String toString() {
            return baseType + "[]_ar";
        }

        @Override
        public int hashCode() {
            return Objects.hash(baseType);
        }
    }

    public static class Void extends TypeNode {

        public Void() {
        }

        public String getDescriptor() {
            return "V";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            return o != null && getClass() == o.getClass();
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }

        // -------------------------------------------------------------------------
        @Override
        public void accept(PrintVisitor visitor, int depth) {
            visitor.visit(this, depth);
        }

        @Override
        public void accept(SemanticVisitor visitor, SymbolTable st) throws ParseException {
            visitor.visit(this, st);
        }

        @Override
        public void accept(Object o, Scope scope) {

        }

        // -------------------------------------------------------------------------

    }

    public String getDescriptor() throws WrongType, UnexpectedError {
        return "???";
    }

    @Override
    public String toString() {
        return "{TypeNode}";
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }
}