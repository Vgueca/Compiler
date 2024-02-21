package compiler.Nodes;

import compiler.CodeGenerator.Scope;
import compiler.Exceptions.CodeGeneratorException.CodeGeneratorException;
import compiler.Exceptions.CodeGeneratorException.WrongASMObject;
import compiler.Exceptions.SemanticException.SemanticException;
import compiler.SemanticAnalyzer.SymbolTable;
import compiler.Visitors.PrintVisitor;
import compiler.Visitors.SemanticVisitor;
import org.objectweb.asm.MethodVisitor;

import java.text.ParseException;
import java.util.Objects;

import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.RETURN;

public class ReturnNode extends ASTNode {
    public Expr returned;
    public ProcedureNode procedure;

    public ReturnNode(Expr returned) {
        this.returned = returned;
    }

    @Override
    public String toString() {
        return "(return " + (returned == null ? "void" : returned) + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ReturnNode that = (ReturnNode) o;
        return Objects.equals(returned, that.returned);
    }

    @Override
    public int hashCode() {
        return Objects.hash(returned);
    }

    @Override
    public boolean hasReturn() {
        return true;
    }

    // -------------------------------------------------------------------------
    @Override
    public void accept(PrintVisitor visitor, int depth) {
        visitor.visit(this, depth);
        if (returned != null)
            returned.accept(visitor, depth + 1);
    }

    @Override
    public void accept(SemanticVisitor visitor, SymbolTable st) throws ParseException, SemanticException {
        visitor.visit(this, st);
        if (returned != null)
            returned.accept(visitor, st);
    }

    @Override
    public void accept(Object o, Scope scope) throws CodeGeneratorException {
        if (!(o instanceof MethodVisitor mv))
            throw new WrongASMObject("Wrong argument", "Function called with not a MethodVisitor as argument.");

        if (returned == null)
            mv.visitInsn(RETURN); // return no value
        else {
            returned.accept(mv, scope);
            mv.visitInsn(returned.descriptor.getOpcode(IRETURN));
        }
    }
    // -------------------------------------------------------------------------
}