package compiler.Lexer.Tokens;

import compiler.Lexer.TokenMatching;

public class IdentifierToken implements TokenMatching {
    public static boolean isTokenMatched(String code) {
        // Verify if the first element of the string is a valid symbol for an Identifier
        if (!Character.isJavaIdentifierStart(code.charAt(0))) {
            return false;
        }
        // Verify if the rest of the elements of the string are valid symbol for an
        // Identifier
        for (int i = 1; i < code.length(); i++) {
            if (!Character.isJavaIdentifierPart(code.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
