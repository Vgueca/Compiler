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
import org.objectweb.asm.Opcodes;

import java.text.ParseException;
import java.util.Objects;

public class IfNode extends ASTNode {
    public Expr condition;
    public BlockNode block;

    public IfNode(Expr condition, BlockNode ifBlock) {
        this.condition = condition;
        this.block = ifBlock;
    }

    @Override
    public String toString() {
        return "(if " + condition + " " + block + ")";
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

        SymbolTable newST = new SymbolTable(st, "if");
        block.accept(visitor, newST);
    }

    @Override
    public void accept(Object o, Scope scope) throws CodeGeneratorException {
        if (!(o instanceof MethodVisitor mv))
            throw new WrongASMObject("Wrong argument", "Function called with not a MethodVisitor as argument.");

        Label elseLabel = new Label();
        Label endLabel = new Label();
        boolean hasElse = this instanceof IfNode.Else;

        condition.accept(mv, scope);
        mv.visitJumpInsn(Opcodes.IFEQ, hasElse ? elseLabel : endLabel); //if the condition is not true, then jump to the elseLabel(line139), if it is true, continue with the following instruction
        block.accept(mv, scope);

        if (this instanceof IfNode.Else elseNode) {
            mv.visitJumpInsn(Opcodes.GOTO, endLabel); //make sure we do not run also the elseStatement
            mv.visitLabel(elseLabel);
            elseNode.elseBlock.accept(mv, scope);
        }

        mv.visitLabel(endLabel);
    }
    // -------------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        IfNode ifNode = (IfNode) o;
        return Objects.equals(condition, ifNode.condition) && Objects.equals(block, ifNode.block);
    }

    @Override
    public int hashCode() {
        return Objects.hash(condition, block);
    }

    @Override
    public boolean hasReturn() {
        return block.hasReturn();
    }

    public static class Else extends IfNode {

        BlockNode elseBlock;

        public Else(Expr condition, BlockNode ifBlock, BlockNode elseBlock) {
            super(condition, ifBlock);
            this.elseBlock = elseBlock;
        }

        @Override
        public String toString() {
            return "(if " + condition + " " + block + " else " + elseBlock + ")";
        }

        // -------------------------------------------------------------------------
        @Override
        public void accept(PrintVisitor visitor, int depth) {
            visitor.visit(this, depth);
            condition.accept(visitor, depth + 1);
            block.accept(visitor, depth + 1);
            elseBlock.accept(visitor, depth + 1);
        }

        @Override
        public void accept(SemanticVisitor visitor, SymbolTable st) throws ParseException, SemanticException {
            visitor.visit(this, st);
            condition.accept(visitor, st);

            SymbolTable newIfST = new SymbolTable(st, "if");
            block.accept(visitor, newIfST);
            SymbolTable newElseST = new SymbolTable(st, "else");
            elseBlock.accept(visitor, newElseST);
        }

        // -------------------------------------------------------------------------
        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            if (!super.equals(o))
                return false;
            Else anElse = (Else) o;
            return Objects.equals(elseBlock, anElse.elseBlock);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), elseBlock);
        }

        @Override
        public boolean hasReturn() {
            return super.hasReturn() && elseBlock.hasReturn();
        }
    }
}
