package compiler.Nodes;

import compiler.CodeGenerator.Scope;
import compiler.Exceptions.CodeGeneratorException.UnexpectedError;
import compiler.Exceptions.CodeGeneratorException.WrongType;
import compiler.Exceptions.SemanticException.SemanticException;
import compiler.SemanticAnalyzer.SymbolTable;
import compiler.Visitors.PrintVisitor;
import compiler.Visitors.SemanticVisitor;

import java.text.ParseException;
import java.util.Objects;

public class ParameterNode extends ASTNode {
    public IdentifierNode identifier;
    public TypeNode type;

    public ParameterNode(IdentifierNode identifier, TypeNode type) {
        this.identifier = identifier;
        this.type = type;
    }

    @Override
    public String toString() {
        return "(param " + identifier + " " + type + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ParameterNode that = (ParameterNode) o;
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
    public void accept(Object o, Scope scope) throws WrongType, UnexpectedError {
        // a field has to be declared in the procedure's new scope (but is set to null, the value is set when doing a function call by pushing the arguments to the stack)
        Scope.CVVDeclaration cvv = scope.declareCVV(identifier.name, new CVVNode.Var(identifier, type, null), false);
    }
    // -------------------------------------------------------------------------
}
