package compiler.Nodes;

import compiler.CodeGenerator.CodeGenerator;
import compiler.CodeGenerator.Scope;
import compiler.Exceptions.CodeGeneratorException.CodeGeneratorException;
import compiler.Exceptions.CodeGeneratorException.WrongASMObject;
import compiler.Exceptions.CodeGeneratorException.WrongType;
import compiler.Exceptions.SemanticException.SemanticException;
import compiler.Lexer.Token;
import compiler.SemanticAnalyzer.SemanticAnalyzer;
import compiler.SemanticAnalyzer.SymbolTable;
import compiler.Visitors.PrintVisitor;
import compiler.Visitors.SemanticVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.text.ParseException;
import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;

public class ArrayInitNode extends PrimaryNode {
    public TypeNode.Array type;
    public ASTNode size; // expression

    public ArrayInitNode(TypeNode.Array type, ASTNode size) {
        super(null); // TODO transform to right type once known
        this.type = type;
        this.size = size;
    }

    @Override
    public String toString() {
        return "(AI " + type + " (" + size + "))";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o instanceof ArithFactorNode af)
            return af.equals(this);
        if (o == null || getClass() != o.getClass())
            return false;
        ArrayInitNode that = (ArrayInitNode) o;
        return Objects.equals(type, that.type) && Objects.equals(size, that.size);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, size);
    }

    @Override
    public Object getValue(Scope scope) {
        return null; // TODO?
    }

    @Override
    public String getDescriptor(Scope scope) {
        return null; // TODO?
    }

    // -------------------------------------------------------------------------
    @Override
    public void accept(PrintVisitor visitor, int depth) {
        visitor.visit(this, depth);
        type.accept(visitor, depth + 1);
        size.accept(visitor, depth + 1);
    }

    @Override
    public void accept(SemanticVisitor visitor, SymbolTable st) throws ParseException, SemanticException {
        visitor.visit(this, st);

        // TODO check if this updates correctly when the SV is run
        descriptor = CodeGenerator.nodeToASMType(SemanticAnalyzer.getType(this, st));

        type.accept(visitor, st);
        size.accept(visitor, st);
    }

    @Override
    public void accept(Object o, Scope scope) throws CodeGeneratorException {
        if (o instanceof MethodVisitor mv) {

            //we push the size onto the stack
            size.accept(mv, scope);

            Token tok = this.type.baseType.token;
            if (tok == Token.BOOLTYPE) {

                mv.visitIntInsn(NEWARRAY, T_BOOLEAN);

            } else if (tok == Token.INTTYPE) {

                mv.visitIntInsn(NEWARRAY, T_INT);

            } else if (tok == Token.REALTYPE) {

                mv.visitIntInsn(NEWARRAY, T_DOUBLE);

            } else if (tok == Token.STRINGTYPE) {

                mv.visitTypeInsn(ANEWARRAY, "java/lang/String");

            } else {
                throw new WrongType("Wrong type", "Error while initializing an array. Invalid type.");
            }

        } else {
            throw new WrongASMObject("Wrong argument", "Function called with not a MethodVisitor as argument.");
        }
    }

    // -------------------------------------------------------------------------
}
