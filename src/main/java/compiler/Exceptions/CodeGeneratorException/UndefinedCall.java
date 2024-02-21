package compiler.Exceptions.CodeGeneratorException;

/**
 * Expecific error when calling a procedure, variable or record not yet defined.
 */
public class UndefinedCall extends CodeGeneratorException{
    public UndefinedCall(String name, String description) {
        super(name, description);
    }
}
