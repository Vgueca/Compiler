package compiler.Lexer;

import compiler.Exceptions.LexerException;

import java.io.IOException;
import java.io.Reader;

public class Lexer {
    Reader input;
    String content;

    public Lexer(Reader input) {
        this.input = input;
        content = "";
    }

    public Symbol getNextSymbol() throws LexerException {
        boolean alreadyRead = false;
        Symbol bestMatch = null;
        int bestLength = 0;

        try {
            while (input.ready()) {
                if (alreadyRead || content.length() == 0) {
                    int read = input.read();
                    if (read == -1 && content.length() == 0)
                        return new Symbol(Token.EOF, "<EOF>");
                    content += (char) read;
                }

                Token found = Symbol.findMatch(content);
                if (found == null) {
                    content = content.substring(bestLength);
                    break; // longest match
                }
                alreadyRead = true;
                bestMatch = new Symbol(found, content);
                bestLength++;
            }
        } catch (IOException e) {
            System.err.println("Error in lexer: " + e.getMessage());
            return bestMatch;
        }

        // if the match is a separator, get next token
        if (bestMatch != null && (bestMatch.token() == Token.SEPARATOR || bestMatch.token() == Token.COMMENT))
            return getNextSymbol();

        // return bestMatch if it isn't null, else send the EOF symbol.
        return bestMatch != null ? bestMatch : new Symbol(Token.EOF, "<EOF>");
    }
}
