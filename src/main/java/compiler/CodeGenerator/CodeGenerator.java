package compiler.CodeGenerator;

import compiler.Exceptions.CodeGeneratorException.CodeGeneratorException;
import compiler.Exceptions.CodeGeneratorException.UnexpectedError;
import compiler.Exceptions.CodeGeneratorException.WrongType;
import compiler.Exceptions.LexerException;
import compiler.Exceptions.SemanticException.SemanticException;
import compiler.Nodes.*;
import compiler.Parser.Parser;
import compiler.SemanticAnalyzer.SType;
import compiler.SemanticAnalyzer.SymbolTable;
import compiler.Visitors.SemanticVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.text.ParseException;
import java.util.ArrayList;

import static org.objectweb.asm.Opcodes.*;

public class CodeGenerator {
    ClassWriter cw;
    final ASTNode ast;

    public CodeGenerator(Parser parser) throws ParseException, LexerException, SemanticException {
        this(parser, true);
    }

    public CodeGenerator(Parser parser, boolean semanticChecks)
            throws ParseException, LexerException, SemanticException {
        this.ast = parser.getAST();

        // call the semantic analyzer
        if (semanticChecks) {
            SymbolTable st = new SymbolTable();
            ast.accept(new SemanticVisitor(), st);
        }
    }

    //--------------------------------- AUXILIARY FUNCTIONS -----------------------------------

    public void callStatic(ClassWriter cw, Scope scope) throws CodeGeneratorException {

        MethodVisitor mv = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);

        mv.visitCode();

        for (Scope.CVVDeclaration cvv : scope.cvvDeclarations.values()) {
            if (cvv.declaration instanceof CVVNode.Var var) {
                if (cvv.isGlobal) {
                    cvv.declaration.expression.accept(mv, scope);
                    if (cvv.declaration.type instanceof TypeNode.Base) {

                        mv.visitFieldInsn(PUTSTATIC, "Program", cvv.declaration.identifier.name,
                                nodeToASMType(cvv.declaration.type).getDescriptor()); // store the value into the static variable
                    } else if (cvv.declaration.type instanceof TypeNode.Array tna) {
                        // TODO
                        mv.visitFieldInsn(PUTSTATIC, "Program", cvv.declaration.identifier.name,
                                nodeToASMType(cvv.declaration.type).getDescriptor());
                    } else if (cvv.declaration.type instanceof TypeNode.Identifier tni) {
                        Scope.RecordDeclaration rec = scope.recordLookup(tni.identifier.name);

                        mv.visitFieldInsn(PUTSTATIC, "Program", cvv.declaration.identifier.name,
                                "L" + rec.declaration.identifier.name + ";");
                    }
                }
            }
        }

        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

    }

    /**
     * This method allow us to return the ASM's type of a ASTNode.
     * @param node ASTNode
     * @return ASMType. For more information -> <a href="https://asm.ow2.io/javadoc/org/objectweb/asm/Type.html">ASM Type doc</a>
     */
    public static Type nodeToASMType(ASTNode node) throws WrongType, UnexpectedError {
        if (node instanceof TypeNode tn) { // TODO change signature to directly be TN?
            return Type.getType(tn.getDescriptor());
        }
        throw new RuntimeException("nodeToASMType error");
    }

    public static Type nodeToASMType(SType node) {
        String desc = node.getDescriptor();
        return desc != null ? Type.getType(desc) : null;
    }

    /**
     * This method allow to generalize the descriptor of a method (procedure or record)
     * @param params_types Arraylist of the types of the parameters
     * @param return_type Type of the return value (only void for record)
     * @return string representation of the descriptor of the method or record
     */
    public static String getDescriptors(ArrayList<TypeNode> params_types, TypeNode return_type)
            throws WrongType, UnexpectedError {
        StringBuilder descriptor = new StringBuilder("(");
        for (TypeNode param : params_types)
            descriptor.append(param.getDescriptor());

        String returnTypeDescriptor = return_type.getDescriptor();
        descriptor.append(")").append(returnTypeDescriptor);
        return descriptor.toString();
    }

    public static void print(MethodVisitor mv, Object var, String descriptor, boolean ln) {
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        mv.visitLdcInsn(var); // put var on the stack
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", ln ? "println" : "print", "(" + descriptor + ")V",
                false);
    }

    public static void swapStack(MethodVisitor mv, int bottomSize, int topSize) {
        if (bottomSize == 1 && topSize == 1)
            mv.visitInsn(SWAP);
        else if (bottomSize == 2 && topSize == 1) {
            mv.visitInsn(DUP_X2);
            mv.visitInsn(POP);
        } else if (bottomSize == 1 && topSize == 2) {
            mv.visitInsn(DUP2_X1);
            mv.visitInsn(POP2);
        } else if (bottomSize == 2 && topSize == 2) {
            mv.visitInsn(DUP2_X2);
            mv.visitInsn(POP2);
        } else
            throw new RuntimeException("swaps with sizes more than 2 aren't implemented.");
    }

    public static void printStack(MethodVisitor mv, String descriptor, boolean ln) {
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        swapStack(mv, Type.getType(descriptor).getSize(), 1); // swap the value to print with the PrintStream
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", ln ? "println" : "print", "(" + descriptor + ")V",
                false);
    }

    public static void readString(MethodVisitor mv) {
        // Create a new instance of the Scanner class
        mv.visitTypeInsn(NEW, "java/util/Scanner");
        mv.visitInsn(DUP);
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "in", "Ljava/io/InputStream;");
        mv.visitMethodInsn(INVOKESPECIAL, "java/util/Scanner", "<init>", "(Ljava/io/InputStream;)V", false);

        // Call the "nextLine" method of the Scanner class to read a String value from the console
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Scanner", "nextLine", "()Ljava/lang/String;", false);
    }

    public static void readInt(MethodVisitor mv) {
        // Create a new instance of the Scanner class
        mv.visitTypeInsn(NEW, "java/util/Scanner");
        mv.visitInsn(DUP);
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "in", "Ljava/io/InputStream;");
        mv.visitMethodInsn(INVOKESPECIAL, "java/util/Scanner", "<init>", "(Ljava/io/InputStream;)V", false);

        // Call the "nextInt" method of the Scanner class to read an int value from the console
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Scanner", "nextInt", "()I", false);

        // Store the int value in a local variable
        // mv.visitVarInsn(ISTORE, 1);
    }

    public static void readReal(MethodVisitor mv) {
        // Create a new instance of the Scanner class
        mv.visitTypeInsn(NEW, "java/util/Scanner");
        mv.visitInsn(DUP);
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "in", "Ljava/io/InputStream;");
        mv.visitMethodInsn(INVOKESPECIAL, "java/util/Scanner", "<init>", "(Ljava/io/InputStream;)V", false);

        // Call the "nextInt" method of the Scanner class to read an int value from the console
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Scanner", "nextDouble", "()F", false);

        // Store the int value in a local variable
        // mv.visitVarInsn(FSTORE, 1);
    }

    public static void concat(MethodVisitor mv) {
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "concat", "(Ljava/lang/String;)Ljava/lang/String;",
                false);
    }

    //----------------------------------------------------------------------------------------

    public Class<?> generateProgram() throws ParseException, LexerException, CodeGeneratorException {
        cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

        // public class <className> extends Object {}
        cw.visit(V1_8, ACC_PUBLIC, "Program", null, "java/lang/Object", null);

        Scope mainScope = new Scope(null, null, "Program");

        // traverse the AST while calling accept recursively
        ast.accept(cw, mainScope);
        callStatic(cw, mainScope);
        cw.visitEnd();

        byte[] mainClass = cw.toByteArray();

        ByteArrayClassLoader loader = new ByteArrayClassLoader();

        mainScope.recordDeclarations.forEach((name, rec) -> {
            System.out.println();
            loader.loadClass(rec.declaration.identifier.name, rec.declaration.cw.toByteArray());
        });

        return loader.loadClass("Program", mainClass);
    }

    public ArrayList<GeneratedClass> generateBytecode() throws ParseException, LexerException, CodeGeneratorException {
        cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

        // public class <className> extends Object {}
        cw.visit(V1_8, ACC_PUBLIC, "Program", null, "java/lang/Object", null);

        Scope mainScope = new Scope(null, null, "Program");

        // traverse the AST while calling accept recursively
        ast.accept(cw, mainScope);
        callStatic(cw, mainScope);
        cw.visitEnd();

        ArrayList<GeneratedClass> bytecodes = new ArrayList<>();

        bytecodes.add(new GeneratedClass("Program", cw.toByteArray()));

        mainScope.recordDeclarations.forEach((name, rec) -> {
            bytecodes.add(new GeneratedClass(name, rec.declaration.cw.toByteArray()));
        });

        return bytecodes;
    }

    public static class GeneratedClass {
        public String name;
        public byte[] bytecode;

        public GeneratedClass(String name, byte[] bytecode) {
            this.name = name;
            this.bytecode = bytecode;
        }
    }
}