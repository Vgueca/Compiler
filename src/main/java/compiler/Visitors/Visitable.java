package compiler.Visitors;

import compiler.CodeGenerator.Scope;
import compiler.Exceptions.CodeGeneratorException.CodeGeneratorException;
import compiler.Exceptions.SemanticException.SemanticException;
import compiler.SemanticAnalyzer.SymbolTable;

import java.text.ParseException;

public interface Visitable {
    /**
     * The accept method lets a visitor go through all the sub-nodes and execute specific code on every type of node.
     * This visitor prints the AST structure.
     * @param visitor prints the nodes (call it on all sub-nodes).
     * @param depth the depth should increase by 1 every time we go deeper in the tree.
     */
    void accept(PrintVisitor visitor, int depth);

    /**
     * The accept method lets a visitor go through all the sub-nodes and execute specific code on every type of node.
     * This visitor checks the AST for semantic errors.
     * @param visitor checks the nodes (call it on all sub-nodes).
     * @param st the current SymbolTable (create a new one or add values with every new scope).
     */
    void accept(SemanticVisitor visitor, SymbolTable st) throws ParseException, SemanticException;

    /**
     * The accept method lets a CodeGenerator visitor go through all the sub-nodes while calling the appropriate methods
     * * to generate the bytecode.
     *
     * @param o     Classwriter / MethodWriter / etc.
     * @param scope
     */
    default void accept(Object o, Scope scope) throws CodeGeneratorException, CodeGeneratorException {
        throw new RuntimeException("Unhandled visitor (in " + this.getClass().getSimpleName() + ")");
    }
}