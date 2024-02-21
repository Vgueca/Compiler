package compiler.Nodes;

import compiler.CodeGenerator.CodeGenerator;
import compiler.CodeGenerator.Scope;
import compiler.Exceptions.CodeGeneratorException.CodeGeneratorException;
import compiler.Exceptions.CodeGeneratorException.UnexpectedError;
import compiler.Exceptions.CodeGeneratorException.WrongASMObject;
import compiler.Exceptions.CodeGeneratorException.WrongType;
import compiler.Exceptions.SemanticException.SemanticException;
import compiler.Lexer.Token;
import compiler.SemanticAnalyzer.SymbolTable;
import compiler.Visitors.PrintVisitor;
import compiler.Visitors.SemanticVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.text.ParseException;
import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;

public class AssignmentNode extends ASTNode {
    public Expr left;
    public Expr right;

    public AssignmentNode(Expr left, Expr right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public String toString() {
        return "(assignment " + left + " = " + right + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AssignmentNode that = (AssignmentNode) o;
        return Objects.equals(left, that.left) && Objects.equals(right, that.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, right);
    }

    // -------------------------------------------------------------------------
    @Override
    public void accept(PrintVisitor visitor, int depth) {
        visitor.visit(this, depth);
        left.accept(visitor, depth + 1);
        right.accept(visitor, depth + 1);
    }

    @Override
    public void accept(SemanticVisitor visitor, SymbolTable st) throws ParseException, SemanticException {
        visitor.visit(this, st);
        left.accept(visitor, st);
        right.accept(visitor, st);
    }

    @Override
    public void accept(Object o, Scope scope) throws CodeGeneratorException {
        if (!(o instanceof MethodVisitor mv))
            throw new WrongASMObject("Wrong argument", "Function called with not a MethodVisitor as argument.");

        /**
         * In an assigment we can encounter:
         * valentin.age = 99; In the left a RecordAccess
         * list[3] = "yes"; In the left a ArrayAccess
         * variable = true; In the left a variable (identifier of it)
         *
         *
         * For the right part we just have to use the visitor and push the value encountered onto the stack. It is true that in the
         * right part we can find also ArrayAccess, RecordAccess or even other things but the Visitor will be in charge of push it properly
         * onto the stack.
         */

        //for convenience, we push the right part when we access a array after pushing the left part
        if (!(left instanceof ArrayAccessNode))
            right.accept(mv, scope);

        if (left instanceof RecordAccessNode ran) {

            //TODO test this part (finish records)
            if (ran.record instanceof IdentifierNode idn) {


                //not sure of the following lane
                Scope.CVVDeclaration cvv = scope.cvvLookup(idn.name);

                //we firstly push the object reference onto the stack
                mv.visitVarInsn(Opcodes.ALOAD, cvv.index);

                //we change the order of the stack (the right part was pushed first but should be at the top)
                mv.visitInsn(Opcodes.SWAP);
                // Put field value on stack
                mv.visitFieldInsn(Opcodes.PUTFIELD, ((IdentifierNode) ran.record).name, ran.field.name, ran.field.getDescriptor(scope));


            } else if (ran.record instanceof ArrayAccessNode aan) {

                //TODO implement this( valentin.surnames[0] = "Guerrero";)

            } else {
                throw new UnexpectedError("Wrong record access", "Fail while accessing a record.");
            }
            //TODO we should get the name of the record (valentin in the example provided)

        } else if (left instanceof ArrayAccessNode aan) {

            Scope.CVVDeclaration cvv = scope.cvvLookup(aan.identifier.name);

            mv.visitVarInsn(ALOAD, cvv.index);
            mv.visitIntInsn(BIPUSH, (int) aan.index.getValue(scope));
            right.accept(mv, scope);

            //swap
            //swapStack(mv,1,2);

            if (cvv.declaration.type instanceof TypeNode.Array typebase) {

                if (typebase.baseType.token == Token.INTTYPE) {
                    mv.visitInsn(IASTORE);
                } else if (typebase.baseType.token == Token.REALTYPE) {
                    mv.visitInsn(FASTORE);
                } else if (typebase.baseType.token == Token.BOOLTYPE) {
                    mv.visitInsn(BASTORE);
                } else if (typebase.baseType.token == Token.STRINGTYPE) {
                    mv.visitTypeInsn(CHECKCAST, "java/lang/String"); //checks that in the top of the stack we have a string object
                    mv.visitInsn(AASTORE);
                }
            } else
                throw new UnexpectedError("Wrong array access", "Fail while accessing an array.");

        } else if (left instanceof IdentifierNode idn) {
            Scope.CVVDeclaration cvv = scope.cvvLookup(idn.name); // get the declaration of this identifier

            if (cvv.declaration.type instanceof TypeNode.Base) {
                if (cvv.isGlobal) // field
                    mv.visitFieldInsn(PUTSTATIC, scope.getClassName(), idn.name,
                            cvv.declaration.expression.descriptor.getDescriptor());
                else // local variable
                    mv.visitVarInsn(CodeGenerator.nodeToASMType(cvv.declaration.type).getOpcode(ISTORE), cvv.index);
            } else
                throw new WrongType("Wrong type", "The identifier has not a base type.");

        } else {
            throw new UnexpectedError("Wrong assigment", "The assigment does not involve the correct members.");
        }

    }

}
