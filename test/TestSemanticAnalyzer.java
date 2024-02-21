import compiler.Exceptions.LexerException;
import compiler.Nodes.ProgramNode;
import compiler.Parser.Parser;
import compiler.Visitors.SemanticVisitor;
import compiler.Exceptions.SemanticException.SemanticException;
import compiler.SemanticAnalyzer.SymbolTable;
import org.junit.Test;

import java.text.ParseException;

import static org.junit.Assert.*;

public class TestSemanticAnalyzer {
    private static final boolean VERBOSE_AST = false;
    private static final boolean VERBOSE_TABLE = false;

    public void runSemanticAnalyzer(String code) throws ParseException, LexerException, SemanticException {
        Parser parser = TestParser.initParser(code);
        ProgramNode ast = parser.getAST();
        if (VERBOSE_AST)
            Parser.printAST(ast);

        SymbolTable st = new SymbolTable();
        ast.accept(new SemanticVisitor(), st);
        if (VERBOSE_TABLE)
            System.out.println(st);
    }

    public void assertThrows(String code) {
        try {
            runSemanticAnalyzer(code);
            fail("No error was thrown.");
        } catch (ParseException | LexerException e) {
            fail("The error thrown was not a SemanticException.");
        } catch (SemanticException e) {
            // this is ok
        }
    }

    @Test
    public void completeProgram() throws ParseException, SemanticException, LexerException {
        runSemanticAnalyzer("""
                const PI real = 3.1415;
                const a real = 10 % 2;
                var rand int = 17 * 15 / 17 % 4;

                proc hello (name string) string {
                    val start string = "Hola";
                    return start + name + ", Como estas?";
                }

                proc main (argc int, argv string[]) real {
                    val total real = PI * a + rand;
                    hello(" Ben ");
                    return total;
                }
                """);
        //TODO print the symbol table in the right order. (Maybe an array in the add procedure of SymbolTable)
    }

    @Test
    public void variableDeclaration() throws ParseException, LexerException, SemanticException {
        runSemanticAnalyzer("""
                var x int = 10;
                var y real = 3.14;
                var z real = 75;
                """);
    }

    @Test
    public void constantDeclaration() throws ParseException, LexerException, SemanticException {
        runSemanticAnalyzer("""
                const PI real = 3.1415;
                const INTEGER int = 3;
                const NOTREAL real = 5;
                """);
    }

    @Test
    public void noReturnTypeMissMatch() throws ParseException, LexerException, SemanticException {
        runSemanticAnalyzer("""
                proc shadowing(a int) int {
                    // var a int = 10; // -> not allowed
                    if (a > 5) {
                        a = 15;
                        var a int = 20;
                        var b int = 100;
                        return a + b;
                    } else {
                        a = 30;
                        var a int = 50;
                        var b int = 200;
                        return 1000 - (a + b);
                    }
                }

                proc foo() int {
                    return 5 * shadowing(0) * shadowing(10);
                }
                """);
    }

    @Test
    public void binaryOperators() throws ParseException, LexerException, SemanticException {
        runSemanticAnalyzer("""
                    const PI real = 3.1415*2;
                    const a real = 10 % 2;
                    const b real = 10 % 6;
                    var rand int = 17 * 15 / 17 % 4;
                    var condition bool = false == true;
                    var condition2 bool = a > PI;
                    var condition3 bool = condition or condition2;
                    var name string = "Juan" + " <3 " + "Valentin";
                """);
    }

    @Test
    public void recordDeclaration() throws ParseException, LexerException, SemanticException {
        runSemanticAnalyzer("""
                record Person {
                    name string;
                    age int;
                }

                var John Person = Person("John", 23);
                var hola string = "Hello " + John.name;
                """);
    }

    @Test(expected = ParseException.class)
    public void ERROR_usageOfUndeclaredVariable() throws ParseException, LexerException, SemanticException {
        runSemanticAnalyzer("var x int = y + 10;");
    }

    @Test(expected = ParseException.class)
    public void ERROR_usageOfUndeclaredProcedure() throws ParseException, LexerException, SemanticException {
        runSemanticAnalyzer("proc foo() void { bar(); }");
    }

    @Test(expected = SemanticException.class)
    public void ERROR_wrongRecordInitializationArgumentCount() throws ParseException, SemanticException, LexerException {
        runSemanticAnalyzer("""
                record Person {
                    age int;
                    name string;
                }
                var John Person = Person("John", 23, 24);
                """);
    }

    @Test(expected = SemanticException.class)
    public void ERROR_variableRedeclaration() throws ParseException, LexerException, SemanticException {
        runSemanticAnalyzer("""
                proc shadowing(a int, b int) int {
                    b = 10;
                    var a int = 10; // -> not allowed
                }
                """);
    }

    @Test
    public void allLoops() throws SemanticException, ParseException, LexerException {
        runSemanticAnalyzer("""
                proc sum(a int, b int) int { return a + b; }

                proc main() void {
                    var value int = 5;

                    var i int = 1;
                    for i=1 to 100 by 2 {
                        while sum(value,i) <> 3 {
                            value = value + 1;
                        }
                    }
                    i = (i+2)*2;
                }
                """);
    }

    @Test(expected = SemanticException.class)
    public void ERROR_functionRedeclaration() throws SemanticException, ParseException, LexerException {
        runSemanticAnalyzer("""
                proc sum(a int, b int) int { return a + b; }
                proc sum(a real, b real) real { return a + b; }
                """);
    }

    @Test
    public void returns() throws SemanticException, ParseException, LexerException {
        runSemanticAnalyzer("""
                proc i() int { return 10; }
                proc r() real { return 1.; }
                proc s() string { return "wow"; }
                proc b() bool { return false; }
                proc fi() bool { if true { return false; } }
                proc esle() bool { if true { return false; } else { return false; } }
                proc elihw() bool { while true { return false; } }
                proc rof(i int) bool { for i=1 to 100 { return false; } }
                proc yb(i int) bool { for i=1 to 100 by 2 { return false; } }
                """);
    }

    @Test
    public void missingReturns() {
        assertThrows("proc i() int {}");
        assertThrows("proc r() real {}");
        assertThrows("proc s() string {}");
        assertThrows("proc b() bool {}");
        assertThrows("proc fi() bool { if true {} }");
        assertThrows("proc esle() bool { if true {} else { return false; } }");
        assertThrows("proc esle() bool { if true { return false; } else {} }");
        assertThrows("proc esle() bool { if true {} else {} }");
        assertThrows("proc elihw() bool { while true {} }");
        assertThrows("proc rof(i int) bool { for i=1 to 100 {} }");
        assertThrows("proc yb(i int) bool { for i=1 to 100 by 2 {} }");
    }
}
