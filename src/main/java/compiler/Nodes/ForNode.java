package compiler.Nodes;

import compiler.CodeGenerator.CodeGenerator;
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

import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.ISTORE;

public class ForNode extends ASTNode {
    public IdentifierNode i;
//    public LiteralNode.Int from;
//    public LiteralNode.Int to;
    public Expr from;
    public Expr to;
    public BlockNode block;

    public ForNode(IdentifierNode i, LiteralNode.Int from, LiteralNode.Int to, BlockNode block) {
        this.i = i;
        this.from = from;
        this.to = to;
        this.block = block;
    }

    public ForNode(IdentifierNode i, Expr from, Expr to, BlockNode block) {
        this.i = i;
        this.from = from;
        this.to = to;
        this.block = block;
    }

    @Override
    public String toString() {
        return "(for " + i + "=" + from + " to " + to + block + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ForNode forNode = (ForNode) o;
        return Objects.equals(i, forNode.i) && Objects.equals(from, forNode.from) && Objects.equals(to, forNode.to)
                && Objects.equals(block, forNode.block);
    }

    @Override
    public int hashCode() {
        return Objects.hash(i, from, to, block);
    }

    @Override
    public boolean hasReturn() {
        return block.hasReturn();
    }

    // -------------------------------------------------------------------------
    @Override
    public void accept(PrintVisitor visitor, int depth) {
        visitor.visit(this, depth);
        i.accept(visitor, depth + 1);
        from.accept(visitor, depth + 1);
        to.accept(visitor, depth + 1);
        block.accept(visitor, depth + 1);
    }

    @Override
    public void accept(SemanticVisitor visitor, SymbolTable st) throws ParseException, SemanticException {
        visitor.visit(this, st);

        SymbolTable newST = new SymbolTable(st, "for");
        i.accept(visitor, newST);
        from.accept(visitor, newST);
        to.accept(visitor, newST);
        block.accept(visitor, newST);
    }

    @Override
    public void accept(Object o, Scope scope) throws CodeGeneratorException {
        if (!(o instanceof MethodVisitor mv))
            throw new WrongASMObject("Wrong argument", "Function called with not a MethodVisitor as argument.");

        //we push the value from we start
        from.accept(mv,scope);

        //we store the value of the stack onto our variable idx
        Scope.CVVDeclaration var = scope.cvvLookup(i.name);
        mv.visitVarInsn(CodeGenerator.nodeToASMType(var.declaration.type).getOpcode(ISTORE), var.index);


        // Label for the beginning of the loop
        Label loopStart = new Label();
        mv.visitLabel(loopStart);

        //we push the current value
        i.accept(mv,scope);


        //we visit the bound of the loop in order to push it onto the stack
        to.accept(mv,scope);

        // Visit the loop condition expression and jump to the end of the loop if it is false
        //at this point we should have onto the stack our variable i with its new value and the bound of the loop
        Label loopEnd = new Label();
        mv.visitJumpInsn(Opcodes.IF_ICMPGE, loopEnd);  // then we compare.  We jump to loopEnd label if i >= TO

        // Visit the loop body
        block.accept(mv,scope);

        // Increment the loop variable and jump back to the beginning of the loop

        mv.visitIincInsn(var.index, 1);
        mv.visitJumpInsn(GOTO, loopStart);

        // Label for the end of the loop
        mv.visitLabel(loopEnd);

    }

    // -------------------------------------------------------------------------
    public static class By extends ForNode {
        public LiteralNode.Int by;

        public By(IdentifierNode i, LiteralNode.Int from, LiteralNode.Int to, LiteralNode.Int by, BlockNode block) {
            super(i, from, to, block);
            this.by = by;
        }

        @Override
        public String toString() {
            return "(for " + i + "=" + from + " to " + to + " by " + by + block + ")";
        }

        // -------------------------------------------------------------------------
        @Override
        public void accept(PrintVisitor visitor, int depth) {
            visitor.visit(this, depth);
            i.accept(visitor, depth + 1);
            from.accept(visitor, depth + 1);
            to.accept(visitor, depth + 1);
            by.accept(visitor, depth + 1);
            block.accept(visitor, depth + 1);
        }

        @Override
        public void accept(SemanticVisitor visitor, SymbolTable st) throws ParseException, SemanticException {
            visitor.visit(this, st);
            i.accept(visitor, st);
            from.accept(visitor, st);
            to.accept(visitor, st);
            by.accept(visitor, st);

            SymbolTable newST = new SymbolTable(st, "for");
            block.accept(visitor, newST);
        }

        @Override
        public void accept(Object o, Scope scope) throws CodeGeneratorException {
            if (!(o instanceof MethodVisitor mv))
                throw new WrongASMObject("Wrong argument", "Function called with not a MethodVisitor as argument.");

            //we push the value from we start
            from.accept(mv,scope);

            //we store the value of the stack onto our variable idx
            Scope.CVVDeclaration var = scope.cvvLookup(i.name);
            mv.visitVarInsn(CodeGenerator.nodeToASMType(var.declaration.type).getOpcode(ISTORE), var.index);


            // Label for the beginning of the loop
            Label loopStart = new Label();
            mv.visitLabel(loopStart);

            //we push the current value
            i.accept(mv,scope);


            //we visit the bound of the loop in order to push it onto the stack
            to.accept(mv,scope);

            // Visit the loop condition expression and jump to the end of the loop if it is false
            //at this point we should have onto the stack our variable i with its new value and the bound of the loop
            Label loopEnd = new Label();
            mv.visitJumpInsn(Opcodes.IF_ICMPGE, loopEnd);  // then we compare.  We jump to loopEnd label if i >= TO

            // Visit the loop body
            block.accept(mv,scope);

            // Increment the loop variable and jump back to the beginning of the loop

            mv.visitIincInsn(var.index, by.content);
            mv.visitJumpInsn(GOTO, loopStart);

            // Label for the end of the loop
            mv.visitLabel(loopEnd);

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
            By by1 = (By) o;
            return Objects.equals(by, by1.by);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), by);
        }
    }
}
