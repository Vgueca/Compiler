package compiler.Exceptions.SemanticException;

public class UndeclaredVariable extends SemanticException {
    public UndeclaredVariable(String variable1, String type) {
        super("Undeclared variable",
                "The identifier '" + variable1 + "' (" + type + ") hasn't been declared yet.");
    }
}
