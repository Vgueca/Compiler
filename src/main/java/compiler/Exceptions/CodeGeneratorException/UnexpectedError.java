package compiler.Exceptions.CodeGeneratorException;

/**
 * To Handle unexpected errors such as unexpected access on records or arrays or unexpected functions' calls.
 */
public class UnexpectedError extends CodeGeneratorException{
    public UnexpectedError(String name, String description) {
        super(name, description);
    }
}
