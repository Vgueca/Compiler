package compiler.Nodes;

import compiler.CodeGenerator.Scope;
import compiler.Exceptions.CodeGeneratorException.UnexpectedError;
import compiler.Exceptions.CodeGeneratorException.WrongASMObject;
import compiler.Exceptions.CodeGeneratorException.WrongType;
import compiler.Exceptions.SemanticException.SemanticException;
import compiler.SemanticAnalyzer.SymbolTable;
import compiler.Visitors.PrintVisitor;
import compiler.Visitors.SemanticVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.text.ParseException;
import java.util.Objects;

public class FieldDeclarationNode extends ASTNode {
    public IdentifierNode identifier;
    public TypeNode type;

    public FieldDeclarationNode(IdentifierNode identifier, TypeNode type) {
        this.identifier = identifier;
        this.type = type;
    }

    @Override
    public String toString() {
        return "(field " + identifier + " " + type + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        FieldDeclarationNode that = (FieldDeclarationNode) o;
        return Objects.equals(identifier, that.identifier) && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier, type);
    }

    // -------------------------------------------------------------------------
    @Override
    public void accept(PrintVisitor visitor, int depth) {
        visitor.visit(this, depth);
        identifier.accept(visitor, depth + 1);
        type.accept(visitor, depth + 1);
    }

    @Override
    public void accept(SemanticVisitor visitor, SymbolTable st) throws ParseException, SemanticException {
        visitor.visit(this, st);
        identifier.accept(visitor, st);
        type.accept(visitor, st);
    }

    @Override
    public void accept(Object o, Scope scope) throws WrongASMObject, WrongType, UnexpectedError {
        if(!(o instanceof ClassWriter cw)){
            throw new WrongASMObject("Wrong argument", "Function called with not a ClassWriter as argument.");
        }

        cw.visitField(Opcodes.ACC_PUBLIC, identifier.name, type.getDescriptor(), null, null);
    }

    // -------------------------------------------------------------------------
}
