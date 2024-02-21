package compiler.Lexer;

import compiler.Exceptions.LexerException;

import java.text.ParseException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Lexer Buffer with symbol preview.
 */
public class LB {
    Lexer lexer;
    LinkedList<Symbol> symbols;
    List<Symbol> progress;

    public LB(Lexer lexer) {
        this.lexer = lexer;
        symbols = new LinkedList<>();
        progress = new LinkedList<>();
    }

    /**
     * Gets the next symbol and add it to the progress.
     * 
     * @return the next Symbol.
     */
    public Symbol get() throws LexerException {
        Symbol next = silentGet();
        progress.add(next);
        return next;
    }

    /**
     * Gets the next symbol without adding it to the progress (used for checking values).
     *
     * @return
     */
    private Symbol silentGet() throws LexerException {
        return symbols.isEmpty() ? lexer.getNextSymbol() : symbols.remove();
    }

    /**
     * Get the next symbol and check if the token type is right.
     * 
     * @param token the token type of the next symbol.
     * @return the next symbol.
     * @throws ParseException if the types don't match.
     */
    public Symbol match(Token token) throws ParseException, LexerException {
        return matchAny(new Token[] { token });
    }

    /**
     * Get the next symbol and check if the token type matches any one of tokens.
     * 
     * @param tokens an array of token types that the next symbol could be.
     * @return the next symbol.
     * @throws ParseException if the type don't match with any of the array.
     */
    public Symbol matchAny(Token[] tokens) throws ParseException, LexerException {
        Symbol symbol = get();

        if (!Arrays.asList(tokens).contains(symbol.token()))
            throw new ParseException(
                    "Expected a " + Arrays.toString(tokens) + " token but got a " + symbol.token(), 0);

        return symbol;
    }

    /**
     * Consume the next symbol while checking its token type.
     * 
     * @param token the token type of the next symbol.
     * @throws ParseException if the types don't match.
     */
    public void consume(Token token) throws ParseException, LexerException {
        match(token);
    }

    /**
     * Get the next symbol without consuming it.
     * 
     * @return the next symbol.
     */
    public Symbol peek() throws LexerException {
        if (symbols.isEmpty())
            symbols.add(lexer.getNextSymbol());
        return symbols.peek();
    }

    /**
     * Get the symbol after the next symbol without consuming it.
     *
     * @return the 2nd next symbol.
     */
    public Symbol peek2() throws LexerException {
        while (symbols.size() < 2)
            symbols.add(lexer.getNextSymbol());

        Symbol first = silentGet();
        Symbol second = symbols.peek();
        symbols.addFirst(first);
        return second;
    }

    /**
     * Produces a string with the code of the tokens that have already been read.
     *
     * @return a String of already parsed code.
     */
    public String getProgress() {
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < progress.size(); i++) {
            if (i > 0)
                ret.append(" ");
            ret.append(progress.get(i).content());
        }
        return ret.toString();
    }
}
