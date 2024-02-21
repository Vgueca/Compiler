package compiler.Nodes;

import compiler.CodeGenerator.CodeGenerator;
import compiler.CodeGenerator.Scope;
import compiler.Exceptions.CodeGeneratorException.*;
import compiler.Exceptions.SemanticException.SemanticException;
import compiler.SemanticAnalyzer.SemanticAnalyzer;
import compiler.SemanticAnalyzer.SymbolTable;
import compiler.Visitors.PrintVisitor;
import compiler.Visitors.SemanticVisitor;
import org.objectweb.asm.MethodVisitor;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Objects;

import static compiler.CodeGenerator.CodeGenerator.*;
import static org.objectweb.asm.Opcodes.*;

public class FunctionCallNode extends PrimaryNode {
    public IdentifierNode identifier;

    public ArrayList<Expr> args; // expressions

    public FunctionCallNode(IdentifierNode identifier, ArrayList<Expr> args) {
        super(null); // TODO transform to right type once known
        this.identifier = identifier;
        this.args = args;
    }

    @Override
    public String toString() {
        return "(call " + identifier + " (" + args + "))";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o instanceof ArithFactorNode af)
            return af.equals(this);
        if (o == null || getClass() != o.getClass())
            return false;
        FunctionCallNode that = (FunctionCallNode) o;
        return Objects.equals(identifier, that.identifier) && Objects.equals(args, that.args);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier, args);
    }

    @Override
    public Object getValue(Scope scope) {
        return null; // TODO or not?
    }

    @Override
    public String getDescriptor(Scope scope) throws WrongType, UnexpectedError {
        return scope.procLookup(identifier.name).declaration.returnType.getDescriptor();
    }

    // -------------------------------------------------------------------------
    @Override
    public void accept(PrintVisitor visitor, int depth) {
        visitor.visit(this, depth);
        depth++;
        identifier.accept(visitor, depth);
        for (ASTNode a : args) {
            a.accept(visitor, depth);
        }
    }

    @Override
    public void accept(SemanticVisitor visitor, SymbolTable st) throws ParseException, SemanticException {
        visitor.visit(this, st);

        // TODO check if this updates correctly when the SV is run
        descriptor = CodeGenerator.nodeToASMType(SemanticAnalyzer.getType(this, st));

        identifier.accept(visitor, st);

        for (ASTNode a : args)
            a.accept(visitor, st);
    }

    @Override
    public void accept(Object o, Scope scope) throws CodeGeneratorException {
        if (!(o instanceof MethodVisitor mv))
            throw new WrongASMObject("Wrong argument", "Function called with not a MethodVisitor as argument.");

        switch (identifier.name) {
            case "write", "writeInt", "writeReal", "writeBool", "writeln":
                args.get(0).accept(o, scope);
                printStack(mv, args.get(0).descriptor.getDescriptor(), identifier.name.equals("writeln"));
                return;
            case "readInt":
                readInt(mv);
                return;
            case "readReal":
                readReal(mv);
                return;
            case "readString":
                readString(mv);
                return;
            case "not":
                args.get(0).accept(o, scope);
                mv.visitInsn(ICONST_1);
                mv.visitInsn(IXOR);
                return;
        }

        // Scope.CVVDeclaration cvv = scope.cvvLookup(this.identifier.name);
        Scope.ProcDeclaration pro = scope.procLookup(identifier.name);
        Scope.RecordDeclaration rec = scope.recordLookup(identifier.name); // TODO would it be a record or cvv? -> add else-if

        if (pro != null) {
            for (Expr arg : args)
                arg.accept(o, scope);

            mv.visitMethodInsn(INVOKESTATIC, scope.getClassName(), identifier.name, pro.declaration.getDescriptors(),
                    false);
        } else if (rec != null) {
            // create new empty record object
            mv.visitTypeInsn(NEW, identifier.name);
            // duplicate the object reference on the stack (to store it later because INVOKESPECIAL pops it)
            mv.visitInsn(DUP);

            // push arguments onto stack
            for (Expr arg : args)
                arg.accept(o, scope);

            // call constructor with stack arguments
            mv.visitMethodInsn(INVOKESPECIAL, identifier.name, "<init>", rec.declaration.getDescriptors(), false);
        } else
            throw new UndefinedCall("Wrong function-call", "Call on a undefined function/record or variable.");
    }
    // -------------------------------------------------------------------------
}
