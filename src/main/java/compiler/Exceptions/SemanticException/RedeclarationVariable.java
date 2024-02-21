package compiler.Exceptions.SemanticException;

public class RedeclarationVariable extends  SemanticException{

    public RedeclarationVariable(String variable1, String type){

        super("Variable already declared",
                "The variable " + variable1 + "("+type+") has already been declared.");
    }
}
