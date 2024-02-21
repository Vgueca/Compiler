package compiler.Exceptions.CodeGeneratorException;

/**
 * Wrong type exception. Mainly use for getValues functions when the type is wrong.
 */
public class WrongType extends CodeGeneratorException{
    public WrongType(String name, String description) {
        super(name, description);
    }
}
