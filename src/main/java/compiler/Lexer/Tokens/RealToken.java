package compiler.Lexer.Tokens;

import compiler.Lexer.TokenMatching;

//REMARK: IT DOESNT TAKE IN COUNT THE SYMBOL + OR -
public class RealToken implements TokenMatching {
    public static boolean isTokenMatched(String code) {
        if (code.startsWith("+") || code.startsWith("-"))
            return false;
        if (code.startsWith(" ") || code.startsWith("\n") || code.startsWith("\t"))
            return false;
        if (code.endsWith(" ") || code.endsWith("\n") || code.endsWith("\t"))
            return false;

        // Verify if the string is a valid double
        try {
            Double.parseDouble(code);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}