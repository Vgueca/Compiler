package compiler.Nodes;

import compiler.Visitors.Visitable;

/**
 * Represents a node of an AST.
 */
public abstract class ASTNode implements Visitable {
    public abstract String toString();

    public abstract boolean equals(Object obj);

    public abstract int hashCode();

    /**
     * Checks whether the node contains a return statement.
     * @return if this node is a return statement or contains one (as the last line of code)
     */
    public boolean hasReturn() { return false; }
}
