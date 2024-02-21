package compiler.Nodes;

import compiler.CodeGenerator.Scope;
import compiler.Exceptions.CodeGeneratorException.CodeGeneratorException;
import compiler.Exceptions.CodeGeneratorException.WrongASMObject;
import compiler.Exceptions.SemanticException.SemanticException;
import compiler.SemanticAnalyzer.SymbolTable;
import compiler.Visitors.PrintVisitor;
import compiler.Visitors.SemanticVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.text.ParseException;
import java.util.Objects;

public class WhileNode extends ASTNode {
    public Expr condition;
    public BlockNode block;

    public WhileNode(Expr condition, BlockNode block) {
        this.condition = condition;
        this.block = block;
    }

    @Override
    public String toString() {
        return "(while " + condition + " " + block + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        WhileNode whileNode = (WhileNode) o;
        return Objects.equals(condition, whileNode.condition) && Objects.equals(block, whileNode.block);
    }

    @Override
    public int hashCode() {
        return Objects.hash(condition, block);
    }

    @Override
    public boolean hasReturn() {
        return block.hasReturn();
    }

    // -------------------------------------------------------------------------
    @Override
    public void accept(PrintVisitor visitor, int depth) {
        visitor.visit(this, depth);
        condition.accept(visitor, depth + 1);
        block.accept(visitor, depth + 1);
    }

    @Override
    public void accept(SemanticVisitor visitor, SymbolTable st) throws ParseException, SemanticException {
        visitor.visit(this, st);
        condition.accept(visitor, st);

        SymbolTable newST = new SymbolTable(st, "while");
        block.accept(visitor, newST);
    }

    @Override
    public void accept(Object o, Scope scope) throws CodeGeneratorException {
        if (!(o instanceof MethodVisitor mv))
            throw new WrongASMObject("Wrong argument","Function called with not a MethodVisitor as argument.");

        Label startLabel = new Label();
        Label endLabel = new Label();

        mv.visitLabel(startLabel);
        this.condition.accept(mv, scope);
        mv.visitJumpInsn(org.objectweb.asm.Opcodes.IFEQ, endLabel); //if the condition is not true, then go to the endLabel
        this.block.accept(mv, scope);
        mv.visitJumpInsn(org.objectweb.asm.Opcodes.GOTO, startLabel); //once the block is run the code jumps to the startLabel to iterate again
        mv.visitLabel(endLabel);
    }
    // -------------------------------------------------------------------------
}
