package compiler.Nodes;

import compiler.CodeGenerator.CodeGenerator;
import compiler.CodeGenerator.Scope;
import compiler.Exceptions.CodeGeneratorException.UnexpectedError;
import compiler.Exceptions.CodeGeneratorException.WrongASMObject;
import compiler.Exceptions.CodeGeneratorException.WrongType;
import compiler.SemanticAnalyzer.SymbolTable;
import compiler.Visitors.PrintVisitor;
import compiler.Visitors.SemanticVisitor;
import org.objectweb.asm.MethodVisitor;

import java.text.ParseException;
import java.util.Objects;

import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.ILOAD;

public class IdentifierNode extends PrimaryNode {
    public String name;

    public IdentifierNode(String name) {
        super(null); // TODO transform to right type once known
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o instanceof ArithFactorNode af)
            return af.equals(this);
        if (o == null || getClass() != o.getClass())
            return false;
        IdentifierNode that = (IdentifierNode) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public Object getValue(Scope scope) throws WrongType, UnexpectedError {
        return scope.cvvLookup(name).declaration.expression.getValue(scope);
    }

    @Override
    public String getDescriptor(Scope scope) throws WrongType, UnexpectedError {
        return scope.cvvLookup(name).declaration.type.getDescriptor();
    }

    // -------------------------------------------------------------------------
    @Override
    public void accept(PrintVisitor visitor, int depth) {
        visitor.visit(this, depth);
        System.out.println("\t".repeat(depth + 1) + name);
    }

    @Override
    public void accept(SemanticVisitor visitor, SymbolTable st) throws ParseException {
        visitor.visit(this, st);
        // TODO check if this updates correctly when the SV is run
        //IS THIS ENOUGH? I think yes
        //descriptor = CodeGenerator.nodeToASMType(SemanticAnalyzer.getType(this, st));
    }

    @Override
    public void accept(Object o, Scope scope) throws WrongASMObject, WrongType, UnexpectedError {

        if (!(o instanceof MethodVisitor mv))
            throw new WrongASMObject("Wrong argument", "Function called with not a MethodVisitor as argument.");

        Scope.CVVDeclaration cvv = scope.cvvLookup(name); //from here we get the idx of the variable with this identifier (that should be already declared)

        if (cvv.isGlobal) // field
            mv.visitFieldInsn(GETSTATIC, scope.getClassName(), name, cvv.declaration.type.getDescriptor());
        else // local variable
            mv.visitVarInsn(CodeGenerator.nodeToASMType(cvv.declaration.type).getOpcode(ILOAD), cvv.index);
    }
}
