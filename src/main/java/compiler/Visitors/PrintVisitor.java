package compiler.Visitors;

import compiler.Nodes.ASTNode;

public class PrintVisitor {
    public void visit(ASTNode node, int depth) {
        System.out.println("\t".repeat(depth) + node.getClass().getSimpleName());
    }
}
