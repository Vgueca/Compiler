package compiler.Lexer;

public enum Token {
    SEPARATOR, // " ", "\t", "\n"
    COMMENT, // "// bla bla bla"
    IDENTIFIER, // function and variable names
    CONST, // const
    RECORD, // record
    VAR, // var
    VAL, // val
    PROC, // proc
    FOR, // for
    TO, // to
    BY, // by
    WHILE, // while
    IF, // if
    ELSE, // else
    RETURN, // return
    AND, // and
    OR, // or
    DELETE, // delete
    INTTYPE, // int
    REALTYPE, // real
    STRINGTYPE, // string
    BOOLTYPE, // bool
    VOIDTYPE, // void
    INT, // integer value (19)
    REAL, // real value (3.14)
    STRING, // string value ("yo!")
    BOOL, // boolean value (true)
    ASSIGNMENT, // =
    ADDITION, // +
    SUBTRACTION, // -
    MULTIPLICATION, // *
    DIVISION, // /
    MODULO, // %
    EQUAL, // =
    DIFFERENT, // <>
    LOWER, // <
    GREATER, // >
    LEQ, // <=
    GEQ, // >=
    OPENPARENTHESIS, // (
    CLOSEPARENTHESIS, // )
    OPENCURLYBRACKETS, // {
    CLOSECURLYBRACKETS, // }
    OPENBRACKETS, // [
    CLOSEBRACKETS, // ]
    DOT, // .
    SEMICOLON, // ;
    COMMA, // ,
    EOF, // end of file
}
