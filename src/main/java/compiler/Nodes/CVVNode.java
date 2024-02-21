package compiler.Nodes;

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
import java.util.Objects;

import static compiler.CodeGenerator.CodeGenerator.nodeToASMType;
import static org.objectweb.asm.Opcodes.*;

/**
 * CVVNode is a Constant-Value-Variable node that represents any of a const, val, or var.
 */
public abstract class CVVNode extends ASTNode {
    public IdentifierNode identifier;
    public TypeNode type;
    public Expr expression;

    private CVVNode(IdentifierNode identifier, TypeNode type, Expr expression) {
        this.identifier = identifier;
        this.type = type;
        this.expression = expression;
    }

    @Override
    public void accept(PrintVisitor visitor, int depth) {
        visitor.visit(this, depth);
        identifier.accept(visitor, depth + 1);
        type.accept(visitor, depth + 1);
        expression.accept(visitor, depth + 1);
    }

    @Override
    public void accept(SemanticVisitor visitor, SymbolTable st) throws ParseException, SemanticException {
        visitor.visit(this, st);
        identifier.accept(visitor, st);
        type.accept(visitor, st);
        expression.accept(visitor, st);
    }

    public static class Const extends CVVNode {
        public Const(IdentifierNode identifier, TypeNode.Base baseType, Expr expression) {
            super(identifier, baseType, expression);
        }

        @Override
        public String toString() {
            return "(const " + identifier + " " + type + " " + expression + ")";
        }

        @Override
        public void accept(Object o, Scope scope) throws CodeGeneratorException {
            if (o instanceof ClassWriter cw) {
                Scope.CVVDeclaration var = scope.declareCVV(identifier.name, this, true);

                cw.visitField(ACC_PUBLIC | ACC_FINAL | ACC_STATIC, identifier.name, type.getDescriptor(),
                        null, expression.getValue(scope));
            } else
                throw new WrongASMObject("Wrong argument", "Function called with not a ClassWriter as argument.");
        }
    }

    public static class Val extends CVVNode {
        public Val(IdentifierNode identifier, TypeNode.Base baseType, Expr expression) {
            super(identifier, baseType, expression);
        }

        @Override
        public String toString() {
            return "(val " + identifier + " " + type + " " + expression + ")";
        }

        @Override
        public void accept(Object o, Scope scope) throws CodeGeneratorException {
            if (o instanceof ClassWriter cw) {
                Scope.CVVDeclaration var = scope.declareCVV(identifier.name, this, true);

                cw.visitField(ACC_PUBLIC | ACC_FINAL | ACC_STATIC, identifier.name, type.getDescriptor(), null,
                        expression.getValue(scope));

            } else if (o instanceof MethodVisitor mv) {
                Scope.CVVDeclaration cvv = scope.declareCVV(identifier.name, this, false);

                expression.accept(o, scope);
                mv.visitVarInsn(nodeToASMType(type).getOpcode(ISTORE), cvv.index); // save stack value to local variable table
            } else
                throw new WrongASMObject("Wrong argument", "Function called with not a ClassWriter as argument.");
        }
    }

    public static class Var extends CVVNode {
        public Var(IdentifierNode identifier, TypeNode type, Expr expression) {
            super(identifier, type, expression);
        }

        @Override
        public String toString() {
            return "(var " + identifier + " " + type + " " + expression + ")";
        }

        @Override
        public void accept(Object o, Scope scope) throws CodeGeneratorException {
            if (o instanceof ClassWriter cw) {

                Scope.CVVDeclaration cvv = scope.declareCVV(identifier.name, this, true);

                if (type instanceof TypeNode.Array) { //it is an array
                    cw.visitField(ACC_PUBLIC | ACC_STATIC, identifier.name, type.getDescriptor(), null, null);

                } else if (type instanceof TypeNode.Identifier) { //it is a record
                    //TODO new record inside the ClassWriter (once the RecordDeclaration is fixed)
                    //example >>> var d Person = Person("me", Point(3,7), int[](a*2));+
                    cw.visitField(ACC_PUBLIC | ACC_STATIC, identifier.name, type.getDescriptor(), null, null);
                } else { //it is a basetype
                    if (type instanceof TypeNode.Base) {
                        // TODO the value isn't pushed here but should be inside a "static {}" block...
                        cw.visitField(ACC_PUBLIC | ACC_STATIC, identifier.name, type.getDescriptor(), null, null);

                        //                        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
                        //
                        //                        mv.visitCode();
                        //
                        //                        mv.visitLdcInsn(expression.getValue(scope));
                        //                        mv.visitFieldInsn(Opcodes.PUTSTATIC, "Program", identifier.name, type.getDescriptor()); // store the value into the static variable

                    }
                }

            } else if (o instanceof MethodVisitor mv) {

                Scope.CVVDeclaration cvv = scope.declareCVV(identifier.name, this, false);

                // push expression to the stack
                expression.accept(o, scope);
                if (type instanceof TypeNode.Array) { // it's an array
                    mv.visitVarInsn(ASTORE, cvv.index);

                } else if (type instanceof TypeNode.Identifier) { // it's a record

                    mv.visitVarInsn(ASTORE, cvv.index);

                } else { //if it is a basetype
                    if (type instanceof TypeNode.Base) // if it is a basetype
                        mv.visitVarInsn(nodeToASMType(type).getOpcode(ISTORE), cvv.index); // save stack value to local variable table
                }

            } else
                throw new WrongASMObject("Wrong argument",
                        "Function called with not a MethodVisitor or a ClassWriter as argument.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        CVVNode cvvNode = (CVVNode) o;
        return Objects.equals(identifier, cvvNode.identifier) && Objects.equals(type, cvvNode.type)
                && Objects.equals(expression, cvvNode.expression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier, type, expression);
    }

}