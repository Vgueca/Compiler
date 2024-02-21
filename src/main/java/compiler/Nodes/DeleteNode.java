package compiler.Nodes;

import compiler.CodeGenerator.Scope;
import compiler.Exceptions.CodeGeneratorException.CodeGeneratorException;
import compiler.Exceptions.CodeGeneratorException.WrongASMObject;
import compiler.SemanticAnalyzer.SymbolTable;
import compiler.Visitors.PrintVisitor;
import compiler.Visitors.SemanticVisitor;
import org.objectweb.asm.MethodVisitor;

import java.text.ParseException;
import java.util.Objects;

public class DeleteNode extends ASTNode {
    public IdentifierNode deleted;

    public DeleteNode(IdentifierNode returned) {
        this.deleted = returned;
    }

    @Override
    public String toString() {
        return "(delete " + deleted + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        DeleteNode that = (DeleteNode) o;
        return Objects.equals(deleted, that.deleted);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deleted);
    }

    // -------------------------------------------------------------------------
    @Override
    public void accept(PrintVisitor visitor, int depth) {
        visitor.visit(this, depth);
        deleted.accept(visitor, depth + 1);
    }

    @Override
    public void accept(SemanticVisitor visitor, SymbolTable st) throws ParseException {
        //TODO we just can delete some variables and not procedures
        visitor.visit(this, st);
        deleted.accept(visitor, st);
    }

    @Override
    public void accept(Object o, Scope scope) throws CodeGeneratorException {
        if (!(o instanceof MethodVisitor mv))
            throw new WrongASMObject("Wrong argument", "Function called with not a MethodVisitor as argument.");
        //TODO read comment below
        //instead of deleting the variable from the memory, we will just delete it from the scope and then It can be reused
        //the problem is that when deleting we have to delete it also from the SymbolTable

        scope.delete(deleted.name); // TODO semantic analyze if the delete doesn't delete wrong things (e.g. functions) && idk if we can delete outside of current scope

    }

    // -------------------------------------------------------------------------
}
