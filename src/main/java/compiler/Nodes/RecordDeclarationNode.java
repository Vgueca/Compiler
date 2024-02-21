package compiler.Nodes;

import compiler.CodeGenerator.CodeGenerator;
import compiler.CodeGenerator.Scope;
import compiler.Exceptions.CodeGeneratorException.CodeGeneratorException;
import compiler.Exceptions.CodeGeneratorException.UnexpectedError;
import compiler.Exceptions.CodeGeneratorException.WrongASMObject;
import compiler.Exceptions.CodeGeneratorException.WrongType;
import compiler.Exceptions.SemanticException.SemanticException;
import compiler.SemanticAnalyzer.SymbolTable;
import compiler.Visitors.PrintVisitor;
import compiler.Visitors.SemanticVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;

public class RecordDeclarationNode extends ASTNode {
    public IdentifierNode identifier;
    public ArrayList<FieldDeclarationNode> fields;
    public ClassWriter cw;

    public RecordDeclarationNode(IdentifierNode identifier, ArrayList<FieldDeclarationNode> fields) {
        this.identifier = identifier;
        this.fields = fields;
    }

    @Override
    public String toString() {
        return "(RD " + identifier + " " + fields + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        RecordDeclarationNode that = (RecordDeclarationNode) o;
        return Objects.equals(identifier, that.identifier) && Objects.equals(fields, that.fields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier, fields);
    }

    // -------------------------------------------------------------------------
    @Override
    public void accept(PrintVisitor visitor, int depth) {
        visitor.visit(this, depth);
        depth++;
        identifier.accept(visitor, depth);
        for (FieldDeclarationNode f : fields)
            f.accept(visitor, depth);
    }

    @Override
    public void accept(SemanticVisitor visitor, SymbolTable st) throws ParseException, SemanticException {
        visitor.visit(this, st);
        identifier.accept(visitor, st);
        for (FieldDeclarationNode f : fields)
            f.accept(visitor, st);
    }

    @Override
    public void accept(Object o, Scope scope) throws CodeGeneratorException {
        if (!(o instanceof ClassWriter original_cw)) {
            throw new WrongASMObject("Wrong argument", "Function called with not a ClassWriter as argument.");
        }

        Scope.RecordDeclaration rec = scope.declareRec(this);

        cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        cw.visit(Opcodes.V1_8, ACC_RECORD, identifier.name, null, "java/lang/Object", null);

        Scope newScope = new Scope(null, scope, identifier.name);

        ArrayList<TypeNode> types = new ArrayList<>();
        for (FieldDeclarationNode f : fields) {
            f.accept(cw, newScope);
            types.add(f.type); // add the type of the array list of types
        }

        // we create the contructor
        String descriptor = CodeGenerator.getDescriptors(types, new TypeNode.Void());

        MethodVisitor init = cw.visitMethod(ACC_PUBLIC, "<init>", descriptor, null, null);

        init.visitCode();
        init.visitVarInsn(Opcodes.ALOAD, 0); // this
        init.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);

        int i = 1;
        for (FieldDeclarationNode field : fields) {
            init.visitVarInsn(ALOAD, 0);
            org.objectweb.asm.Type type = CodeGenerator.nodeToASMType(field.type);
            init.visitVarInsn(type.getOpcode(ILOAD), i);
            i += type.getSize();
            init.visitFieldInsn(PUTFIELD, identifier.name, field.identifier.name, type.getDescriptor());
        }
        init.visitInsn(RETURN);
        init.visitMaxs(-1, -1);
        init.visitEnd();

        cw.visitEnd();
    }

    /**
     * Method that allows us to concatenate the descriptor for each parameter of the procedure in the correct format
     * @return Acceptable descriptor for the procedure's visitMethod.
     */
    public String getDescriptors() throws WrongType, UnexpectedError {

        StringBuilder descriptor = new StringBuilder("(");
        for (FieldDeclarationNode field : this.fields)
            descriptor.append(field.type.getDescriptor());
        descriptor.append(")V");

        return descriptor.toString();
    }

    public String getFieldDescriptor(String fieldName) throws WrongType, UnexpectedError {
        for (FieldDeclarationNode field : this.fields) {
            if (field.identifier.name.equals(fieldName)) {
                return field.type.getDescriptor();
            }
        }
        throw new UnexpectedError("Field not found",
                "Could not find field with name " + fieldName + " in record " + this.identifier.name + ".");
    }
}
