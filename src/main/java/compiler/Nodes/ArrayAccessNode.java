package compiler.Nodes;

import compiler.CodeGenerator.CodeGenerator;
import compiler.CodeGenerator.Scope;
import compiler.Exceptions.CodeGeneratorException.CodeGeneratorException;
import compiler.Exceptions.CodeGeneratorException.UnexpectedError;
import compiler.Exceptions.CodeGeneratorException.WrongASMObject;
import compiler.Exceptions.CodeGeneratorException.WrongType;
import compiler.Exceptions.SemanticException.SemanticException;
import compiler.SemanticAnalyzer.SemanticAnalyzer;
import compiler.SemanticAnalyzer.SymbolTable;
import compiler.Visitors.PrintVisitor;
import compiler.Visitors.SemanticVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.text.ParseException;
import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Opcodes.GETFIELD;

public class ArrayAccessNode extends PrimaryNode {
    public IdentifierNode identifier;
    public Expr index; // expression

    public ArrayAccessNode(IdentifierNode identifier, Expr index) {
        super(null);
        this.identifier = identifier;
        this.index = index;
    }

    @Override
    public String toString() {
        return "(AA " + identifier + " [" + index + "])";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ArrayAccessNode that = (ArrayAccessNode) o;
        return Objects.equals(identifier, that.identifier) && Objects.equals(index, that.index);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier, index);
    }

    @Override
    public Object getValue(Scope scope) {
        return null; // TODO
    }

    @Override
    public String getDescriptor(Scope scope) throws WrongType, UnexpectedError {
        String desc = scope.cvvLookup(identifier.name).declaration.type.getDescriptor();
        if (desc.startsWith("["))
            return desc.substring(1);
        throw new UnexpectedError("Array type expected", "The descriptor of an array should start with [.");
    }

    // -------------------------------------------------------------------------
    @Override
    public void accept(PrintVisitor visitor, int depth) {
        visitor.visit(this, depth);
        identifier.accept(visitor, depth + 1);
        index.accept(visitor, depth + 1);
    }

    @Override
    public void accept(SemanticVisitor visitor, SymbolTable st) throws ParseException, SemanticException {
        visitor.visit(this, st);

        // TODO check if this updates correctly when the SV is run
        descriptor = CodeGenerator.nodeToASMType(SemanticAnalyzer.getType(this, st));

        identifier.accept(visitor, st);
        index.accept(visitor, st);
    }

    @Override
    public void accept(Object o, Scope scope) throws CodeGeneratorException {
        if (!(o instanceof MethodVisitor mv))
            throw new WrongASMObject("Wrong argument", "Function called with not a MethodVisitor as argument.");

        Scope.CVVDeclaration cvv = scope.cvvLookup(identifier.name);

        if (cvv.isGlobal) {
            mv.visitFieldInsn(GETSTATIC, "Program", identifier.name, cvv.declaration.type.getDescriptor());
        } else {
            mv.visitVarInsn(ALOAD, cvv.index);
        }

        if (!(cvv.declaration.type instanceof TypeNode.Array tna))
            throw new WrongType("Wrong type", "The type of the identifier should be an array.");

        index.accept(o, scope);
        mv.visitInsn(CodeGenerator.nodeToASMType(tna.baseType).getOpcode(IALOAD));

        //we push the array onto the stack
//        mv.visitVarInsn(Opcodes.ALOAD, cvv.index);
//        //we push the index of the value we want to access
//        mv.visitLdcInsn(index.getValue(scope));
//        //we get the right value and pop the two last elements from the array
//        mv.visitInsn(Opcodes.IALOAD);

    }

}
