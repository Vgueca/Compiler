package compiler.Exceptions;

public class LexerException extends Exception {
    private final String name;
    private final String description;

    public LexerException(String name, String description) {
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
        return "SemanticException: " + name + "\n" + description;
    }
}
