package compiler.Exceptions.SemanticException;

public class TypeMismatch extends SemanticException {
    public TypeMismatch(String exp1, String exp1_type, String exp2, String exp2_type) {
        super("Type mismatch",
                "The types of '" + exp1 + "' (" + exp1_type + ") and '" + exp2 + "' (" + exp2_type + ") don't match.");
    }
}
