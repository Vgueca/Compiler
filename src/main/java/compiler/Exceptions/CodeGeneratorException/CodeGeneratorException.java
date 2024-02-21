package compiler.Exceptions.CodeGeneratorException;

public class CodeGeneratorException extends Exception {
    private final String name;
    private final String description;

    public CodeGeneratorException(String name, String description) {
        super();
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "CodeGenerator Exception: " + name + "\n" + description;
    }
}