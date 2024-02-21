package compiler.Exceptions.CodeGeneratorException;


/**
 *  To throw an exception when accepts functions get a wrong argument (wrong ASM object) ex.: MethodVisitor or ClassWriter
 */
public class WrongASMObject extends CodeGeneratorException{
    public WrongASMObject(String name, String description) {
        super(name, description);
    }
}
