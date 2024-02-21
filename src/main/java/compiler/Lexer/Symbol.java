package compiler.Lexer;

import compiler.Lexer.Tokens.*;

public record Symbol(Token token, String content) {
    /**
     * Tries to match the code with each symbol class (in a defined order) and
     * returns the first matching symbol (by returning a TokenType) or null if no
     * match can be found.
     *
     * @param code The string that will be matched against the tokens.
     * @return The best matching Token.
     */
    public static Token findMatch(String code) {
        switch (code) {
            case ";":
                return Token.SEMICOLON;
            case ",":
                return Token.COMMA;
            case ".":
                return Token.DOT;
            case "[":
                return Token.OPENBRACKETS;
            case "]":
                return Token.CLOSEBRACKETS;
            case "{":
                return Token.OPENCURLYBRACKETS;
            case "}":
                return Token.CLOSECURLYBRACKETS;
            case "(":
                return Token.OPENPARENTHESIS;
            case ")":
                return Token.CLOSEPARENTHESIS;
            case "<=":
                return Token.LEQ;
            case ">=":
                return Token.GEQ;
            case "<":
                return Token.LOWER;
            case ">":
                return Token.GREATER;
            case "<>":
                return Token.DIFFERENT;
            case "==":
                return Token.EQUAL;
            case "%":
                return Token.MODULO;
            case "/":
                return Token.DIVISION;
            case "*":
                return Token.MULTIPLICATION;
            case "-":
                return Token.SUBTRACTION;
            case "+":
                return Token.ADDITION;
            case "=":
                return Token.ASSIGNMENT;
            case "int":
                return Token.INTTYPE;
            case "real":
                return Token.REALTYPE;
            case "string":
                return Token.STRINGTYPE;
            case "bool":
                return Token.BOOLTYPE;
            case "void":
                return Token.VOIDTYPE;
            case "const":
                return Token.CONST;
            case "record":
                return Token.RECORD;
            case "var":
                return Token.VAR;
            case "val":
                return Token.VAL;
            case "proc":
                return Token.PROC;
            case "for":
                return Token.FOR;
            case "to":
                return Token.TO;
            case "by":
                return Token.BY;
            case "while":
                return Token.WHILE;
            case "if":
                return Token.IF;
            case "else":
                return Token.ELSE;
            case "return":
                return Token.RETURN;
            case "and":
                return Token.AND;
            case "or":
                return Token.OR;
            case "delete":
                return Token.DELETE;
        }

        if (code.equals("true") | code.equals("false"))
            return Token.BOOL;
        else if (StringToken.isTokenMatched(code))
            return Token.STRING;
        else if (IntToken.isTokenMatched(code))
            return Token.INT;
        else if (RealToken.isTokenMatched(code))
            return Token.REAL;
        else if (IdentifierToken.isTokenMatched(code))
            return Token.IDENTIFIER;
        else if (CommentToken.isTokenMatched(code))
            return Token.COMMENT;
        else if (SeparatorToken.isTokenMatched(code))
            return Token.SEPARATOR;
        return null;
    }

    @Override
    public String toString() {
        return "(" + token + " '" + content + "')";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Symbol that = (Symbol) o;
        return this.content.equals(that.content) && this.token == that.token;
    }
}
