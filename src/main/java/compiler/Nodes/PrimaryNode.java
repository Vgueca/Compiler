package compiler.Nodes;

import compiler.CodeGenerator.Scope;
import compiler.Exceptions.CodeGeneratorException.CodeGeneratorException;
import compiler.Exceptions.SemanticException.SemanticException;
import compiler.SemanticAnalyzer.SymbolTable;
import compiler.Visitors.PrintVisitor;
import compiler.Visitors.SemanticVisitor;
import org.objectweb.asm.Type;

import java.text.ParseException;

public abstract class PrimaryNode extends Expr {
    public PrimaryNode(Type descriptor) {
        super(descriptor);
    }
    // -------------------------------------------------------------------------

    @Override
    public void accept(PrintVisitor visitor, int depth) {
        visitor.visit(this, depth);
    }

    @Override
    public void accept(SemanticVisitor visitor, SymbolTable st) throws ParseException, SemanticException {
        visitor.visit(this, st);
    }

    @Override
    public void accept(Object o, Scope scope) throws CodeGeneratorException {

    }

    // -------------------------------------------------------------------------
}
