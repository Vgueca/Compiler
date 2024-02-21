package compiler.Exceptions.SemanticException;

public class InvalidOperation extends SemanticException {

    public InvalidOperation(String var1, String type1, String var2, String type2){
        super("Invalid operation",
                "The operation between " + var1 + " ("+type1 +") and " + var2 + " (" + type2 + ") is not valid.");
    }
}
