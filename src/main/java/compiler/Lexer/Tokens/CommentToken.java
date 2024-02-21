package compiler.Lexer.Tokens;

import compiler.Lexer.TokenMatching;

public class CommentToken implements TokenMatching {
    public static boolean isTokenMatched(String code) {
        if (code.equals("/"))
            return true;
        return code.startsWith("//") && !code.contains("\n");
    }
}
