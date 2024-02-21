package compiler.Nodes;

import compiler.CodeGenerator.Scope;
import compiler.Exceptions.CodeGeneratorException.CodeGeneratorException;
import compiler.Exceptions.SemanticException.SemanticException;
import compiler.SemanticAnalyzer.SymbolTable;
import compiler.Visitors.PrintVisitor;
import compiler.Visitors.SemanticVisitor;
import org.objectweb.asm.MethodVisitor;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Objects;

public class BlockNode extends ASTNode {
    ArrayList<ASTNode> statements;

    public BlockNode(ArrayList<ASTNode> statements) {
        this.statements = statements;
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder("{");
        for (ASTNode s : statements)
            ret.append("\n").append(s);
        return ret + "\n}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        BlockNode blockNode = (BlockNode) o;
        return Objects.equals(statements, blockNode.statements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(statements);
    }

    @Override
    public boolean hasReturn() {
        return !statements.isEmpty() && statements.get(statements.size() - 1).hasReturn();
    }

    // -------------------------------------------------------------------------
    @Override
    public void accept(PrintVisitor visitor, int depth) {
        visitor.visit(this, depth);
        depth++;
        for (ASTNode s : statements) {
            s.accept(visitor, depth);
        }
    }

    @Override
    public void accept(SemanticVisitor visitor, SymbolTable st) throws ParseException, SemanticException {
        visitor.visit(this, st);

        for (ASTNode s : statements)
            s.accept(visitor, st);
    }

    public void accept(MethodVisitor mv, Scope scope) throws CodeGeneratorException {
        for (ASTNode s : statements){
            s.accept(mv, scope);

        }

    }

    // -------------------------------------------------------------------------

}