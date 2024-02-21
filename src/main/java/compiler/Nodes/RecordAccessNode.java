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
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import java.text.ParseException;
import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;

public class RecordAccessNode extends PrimaryNode {
    public ASTNode record; // identifier or array access
    public IdentifierNode field;

    public RecordAccessNode(ASTNode record, IdentifierNode field) {
        super(null); // TODO transform to right type once known
        this.record = record;
        this.field = field;
    }

    @Override
    public String toString() {
        return "(RA " + record + "." + field + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o instanceof ArithFactorNode af)
            return af.equals(this);
        if (o == null || getClass() != o.getClass())
            return false;
        RecordAccessNode that = (RecordAccessNode) o;
        return Objects.equals(record, that.record)
                && Objects.equals(field, that.field);
    }

    @Override
    public int hashCode() {
        return Objects.hash(record, field);
    }

    @Override
    public Object getValue(Scope scope) {
        return null; // TODO
    }

    @Override
    public String getDescriptor(Scope scope) throws WrongType, UnexpectedError {
        // get the type (should be a record type)
        TypeNode type;
        if (record instanceof IdentifierNode idn)
            type = scope.cvvLookup(idn.name).declaration.type;
        else if (record instanceof ArrayAccessNode aan)
            type = scope.cvvLookup(aan.identifier.name).declaration.type;
        else
            throw new UnexpectedError("Not a record", "Unexpected record access node type: " + record.getClass());

        if (type instanceof TypeNode.Array tna)
            type = tna.baseType;
        if (!(type instanceof TypeNode.Identifier tni))
            throw new WrongType("Expected record", "Got " + type + " instead.");

        return scope.recordLookup(tni.identifier.name).declaration.getFieldDescriptor(field.name);
    }

    // -------------------------------------------------------------------------

    @Override
    public void accept(PrintVisitor visitor, int depth) {
        visitor.visit(this, depth);
        record.accept(visitor, depth + 1);
        field.accept(visitor, depth + 1);
    }

    @Override
    public void accept(SemanticVisitor visitor, SymbolTable st) throws ParseException, SemanticException {
        visitor.visit(this, st);

        // TODO check if this updates correctly when the SV is run
        descriptor = CodeGenerator.nodeToASMType(SemanticAnalyzer.getType(this, st));

        record.accept(visitor, st);
        field.accept(visitor, st);
    }

    @Override
    public void accept(Object o, Scope scope) throws CodeGeneratorException {

        if (o instanceof ClassWriter cw) {

            //we can have defined a global variable using a record, and then define another variable using  the record access
            if (record instanceof IdentifierNode idn) { //we are just accesing to a field from a record already define  (valentin.age) for example
                Scope.RecordDeclaration recordDeclaration = scope.recordLookup(idn.name);

                //TODO how can we push a predefined variable which is a record onto the stack with the CW?
                //mv.visitVarInsn(ALOAD, recordDeclaration.index);
                cw.visitField(GETFIELD, "java/lang/Object", field.descriptor.getDescriptor(), null,
                        null);

            } else if (record instanceof ArrayAccessNode aan) { //we are accessing to an array element which contains a record (persons[0].name) for example
                //TODO  (after implementing the array)

                cw.visitField(GETFIELD, "java/lang/Object", field.descriptor.getDescriptor(), null,
                        null);
            }

        } else if (o instanceof MethodVisitor mv) {

            if (record instanceof IdentifierNode idn) { //we are just accesing to a field from a record already define  (valentin.age) for example
                Scope.CVVDeclaration cvv = scope.cvvLookup(idn.name);

                if (!(cvv.declaration.type instanceof TypeNode.Identifier recType))
                    throw new WrongType("Wrong type", "RecordAccess node called with not a record type");
                Scope.RecordDeclaration rec = scope.recordLookup(recType.identifier.name);

                String desc = rec.declaration.getFieldDescriptor(field.name);

                if (cvv.isGlobal) {
                    mv.visitFieldInsn(GETSTATIC, "Program", idn.name, "L" + rec.declaration.identifier.name + ";");
                    mv.visitFieldInsn(GETFIELD, rec.declaration.identifier.name, field.name, desc);
                } else {
                    mv.visitVarInsn(ALOAD, cvv.index);
                    mv.visitFieldInsn(GETFIELD, rec.declaration.identifier.name, field.name, desc);
                }

            } else if (record instanceof ArrayAccessNode aan) { //we are accessing to an array element which contains a record (persons[0].name) for example

                Scope.CVVDeclaration cvv = scope.cvvLookup(aan.identifier.name);

                //we push the array onto the stack
                mv.visitVarInsn(ALOAD, cvv.index);
                //we push the index of the value we want to access
                mv.visitLdcInsn(aan.index.getValue(scope));

                //we get the right value and pop the two last elements from the array
                mv.visitInsn(aan.identifier.descriptor.getOpcode(IALOAD));

                mv.visitFieldInsn(GETFIELD, "java/lang/Object", field.name,
                        field.descriptor.getDescriptor());

            }

        } else {
            throw new WrongASMObject("Wrong argument",
                    "RecordAccess node called with not a ClassWriter or a MethodVisitor");
        }

    }

    // -------------------------------------------------------------------------

}
