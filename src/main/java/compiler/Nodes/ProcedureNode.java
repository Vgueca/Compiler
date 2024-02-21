package compiler.Nodes;

import compiler.CodeGenerator.Scope;
import compiler.Exceptions.CodeGeneratorException.CodeGeneratorException;
import compiler.Exceptions.CodeGeneratorException.UnexpectedError;
import compiler.Exceptions.CodeGeneratorException.WrongASMObject;
import compiler.Exceptions.CodeGeneratorException.WrongType;
import compiler.Exceptions.SemanticException.SemanticException;
import compiler.SemanticAnalyzer.SType;
import compiler.SemanticAnalyzer.SymbolTable;
import compiler.Visitors.PrintVisitor;
import compiler.Visitors.SemanticVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;

public class ProcedureNode extends ASTNode {
    public IdentifierNode identifier;
    public ArrayList<ParameterNode> params;
    public TypeNode returnType;
    public BlockNode block;

    public ProcedureNode(IdentifierNode identifier, ArrayList<ParameterNode> params, TypeNode returnType,
            BlockNode block) {
        this.identifier = identifier;
        this.params = params == null ? new ArrayList<>() : params;
        this.returnType = returnType;
        this.block = block;
    }

    @Override
    public String toString() {
        return "(proc " + identifier + " " + params + " " + returnType + " " + block + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ProcedureNode that = (ProcedureNode) o;
        return Objects.equals(identifier, that.identifier) && Objects.equals(params, that.params)
                && Objects.equals(returnType, that.returnType) && Objects.equals(block, that.block);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier, params, returnType, block);
    }

    // -------------------------------------------------------------------------
    @Override
    public void accept(PrintVisitor visitor, int depth) {
        visitor.visit(this, depth);
        depth++;
        identifier.accept(visitor, depth);
        for (ASTNode p : params)
            p.accept(visitor, depth);
        returnType.accept(visitor, depth);
        block.accept(visitor, depth);
    }

    @Override
    public void accept(SemanticVisitor visitor, SymbolTable st) throws ParseException, SemanticException {
        visitor.visit(this, st);
        identifier.accept(visitor, st);

        SymbolTable newST = new SymbolTable.ProcST(st, identifier + " proc", SType.getSType(returnType, st));
        for (ParameterNode p : params)
            newST.add(p.identifier, p.type);

        for (ParameterNode p : params)
            p.accept(visitor, newST);
        returnType.accept(visitor, st);
        block.accept(visitor, newST);

        if (!(returnType instanceof TypeNode.Void) && !block.hasReturn())
            throw new SemanticException("Missing return statement",
                    "A non-void procedure should always return a value.");
    }

    @Override
    public void accept(Object o, Scope scope) throws CodeGeneratorException {
        if (!(o instanceof ClassWriter cw))
            throw new WrongASMObject("Wrong argument", "Function called with not a ClassWriter as argument.");

        Scope.ProcDeclaration pro = scope.declareProc(identifier.name, this);

        // public static {returnType} main ( {params} ) {}
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC | ACC_STATIC, this.identifier.name, getDescriptors(),
                null, null);

        mv.visitCode();

        Scope newScope = new Scope(block, scope, scope.getClassName());
        for (ParameterNode p : params)
            p.accept(mv, newScope);

        block.accept(mv, newScope);

        if (returnType.getDescriptor().equals("V") && !block.hasReturn()) // call void return (typing "return;" isn't required)
            mv.visitInsn(RETURN);

        mv.visitEnd();
        mv.visitMaxs(-1, -1); // ERROR outofbounds: often caused by missing return statement
    }

    // -------------------------------------------------------------------------

    /**
     * Method that allows us to concatenate the descriptor for each parameter of the procedure in the correct format
     * @return Acceptable descriptor for the procedure's visitMethod.
     */
    public String getDescriptors() throws WrongType, UnexpectedError {

        StringBuilder descriptor = new StringBuilder("(");
        for (ParameterNode param : this.params)
            descriptor.append(param.type.getDescriptor());
        String returnTypeDescriptor = this.returnType.getDescriptor();
        descriptor.append(")").append(returnTypeDescriptor);

        return descriptor.toString();
    }
}