package compiler.Lexer.Tokens;

import compiler.Lexer.TokenMatching;

// IN THE STATEMENT: It will be much more convenient for the parser if getNextSymbol does not return
// symbols for whitespaces and comments.SO MAYBE WE SHOULD NOT IMPLEMENT THIS SUBCLASS
public class SeparatorToken implements TokenMatching {
    public static boolean isTokenMatched(String code) {
        for (char ch : code.toCharArray()) {
            if (ch != ' ' && ch != '\n' && ch != '\t')
                return false;
        }
        return true;
    }
}