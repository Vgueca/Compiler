package compiler.Exceptions.CodeGeneratorException;

/**
 * To handle exceptions related with binary operations. Ex.: division by zero.
 *
 */
public class WrongOperation extends CodeGeneratorException{
    public WrongOperation(String name, String description) {
        super(name, description);
    }
}
