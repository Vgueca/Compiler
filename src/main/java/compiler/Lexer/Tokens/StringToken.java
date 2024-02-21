package compiler.Lexer.Tokens;

import compiler.Lexer.TokenMatching;

public class StringToken implements TokenMatching {
    public static boolean isTokenMatched(String code) {
        if (code.equals("\""))
            return true;
        if (code.startsWith("\"") && !code.substring(1).contains("\""))
            return true;
        return code.startsWith("\"") && code.endsWith("\"") && !code.substring(1, code.length() - 1).contains("\"");
    }
}