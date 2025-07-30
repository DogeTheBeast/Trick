package craftinginterpreters.trick;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.ParseException;


/*
 * Types of testing
 * -    Synchonisation test
 * 
 * -    Expect ';' after loop condition.
 * -    Expect ')' after for clauses.
 *  
 * -    OR test
 * -    AND test
 * -    "Invalid assignment target"
 * -    Parse error
 */


class ParserTest {

    private final PrintStream standardErr = System.err;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    private List<Stmt> parseExpression(String source){
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        return  parser.parse();
    }

    @BeforeEach
    public void setUp() {
        System.setErr(new PrintStream(outputStreamCaptor));
    }

    @AfterEach
    public void cleanUp() {
        System.setErr(standardErr);
    }

    @Test
    public void validEquality() {
        List<Stmt> statements = parseExpression("5 != 6;");
        String astBuilider = new ASTprinter().print(statements);
        Assertions.assertEquals("(!= 5.0 6.0)", astBuilider);
    }


    @Test
    public void validComparison() {
        List<Stmt> statements = parseExpression("15 != 5 > 6;");
        String astBuilider = new ASTprinter().print(statements);
        Assertions.assertEquals("(!= 15.0 (> 5.0 6.0))", astBuilider);
    }

    @Test
    public void validTerm() {
        List<Stmt> statements = parseExpression("-15 != 5 > 6;");
        String astBuilider = new ASTprinter().print(statements);
        Assertions.assertEquals("(!= (- 15.0) (> 5.0 6.0))", astBuilider);
    }

    @Test
    public void validFactor() {
        List<Stmt> statements = parseExpression("10 * -15 != 5 > 6;");
        String astBuilider = new ASTprinter().print(statements);
        Assertions.assertEquals("(!= (* 10.0 (- 15.0)) (> 5.0 6.0))", astBuilider);
    }

    @Test
    public void validAddition() {
        List<Stmt> statements = parseExpression("1 + 10 * -15 != 5 > 6;");
        String astBuilider = new ASTprinter().print(statements);
        Assertions.assertEquals("(!= (+ 1.0 (* 10.0 (- 15.0))) (> 5.0 6.0))", astBuilider);
    }

    @Test
    public void validPrimary() {
        List<Stmt> statements = parseExpression("!true;");
        String astBuilider = new ASTprinter().print(statements);
        Assertions.assertEquals("(! true)", astBuilider);
    }

    @Test
    public void validGrouping() {
        List<Stmt> statements = parseExpression("10 * (6 + 5);");
        String astBuilider = new ASTprinter().print(statements);
        Assertions.assertEquals("(* 10.0 (group (+ 6.0 5.0)))", astBuilider);
    }

    @Test
    public void validVariable() {
        List<Stmt> statements = parseExpression("var hello;");
        String astBuilder = new ASTprinter().print(statements);
        Assertions.assertEquals("(hello)",astBuilder);
    }

    @Test
    public void validVariableInitialised() {
        List<Stmt> statements = parseExpression("var hello = 5;");
        String astBuilder = new ASTprinter().print(statements);
        Assertions.assertEquals("(hello 5.0)",astBuilder);
    }

    @Test
    public void validPrint() {
        List<Stmt> statements = parseExpression("print \"Hello world\";");
        String astBuilder = new ASTprinter().print(statements);
        Assertions.assertEquals("(PRINT Hello world)",astBuilder);
    }

    @Test
    public void validBlock() {
        List<Stmt> statements = parseExpression("{hello = 1;}");
        String astBuilder = new ASTprinter().print(statements);
        Assertions.assertEquals("(BLOCK (hello 1.0))",astBuilder);
    }

    @Test
    public void validMultilineBlock() {
        List<Stmt> statements = parseExpression("{hello = 1; world = 2;}");
        String astBuilder = new ASTprinter().print(statements);
        Assertions.assertEquals("(BLOCK (hello 1.0) (world 2.0))",astBuilder);
    }

    @Test
    public void validMultiline() {
        List<Stmt> statements = parseExpression("hello = 1; \n {world = 2;}");
        String astBuilder = new ASTprinter().print(statements);
        Assertions.assertEquals("(hello 1.0)(BLOCK (world 2.0))",astBuilder);
    }

    @Test
    public void validVariableAsPrimary() {
        List<Stmt> statements = parseExpression("hello = world;");
        String astBuilder = new ASTprinter().print(statements);
        Assertions.assertEquals("(hello world)",astBuilder);
    }

    @Test
    public void invalidGrouping() {
        parseExpression("10 * (6 + 5;");
        String expectedString = "[line 1] Error at ';':Expect ')' after expression.";
        Assertions.assertEquals(expectedString, outputStreamCaptor.toString().trim());
    }

    @Test
    public void invalidTerm() {
        parseExpression("-;");
        Assertions.assertEquals("[line 1] Error at ';':Expect expression.", outputStreamCaptor.toString().trim());
    }

    @Test
    public void invalidVariableNameExpected() {
        parseExpression("var;");
        Assertions.assertEquals("[line 1] Error at ';':Expect variable name.",outputStreamCaptor.toString().trim());
    }

    @Test
    public void invalidVariableMissingSemicolon() {
        parseExpression("var hello");
        Assertions.assertEquals("[line 1] Error at end:Expect ';' after variable declaration.",outputStreamCaptor.toString().trim());
    }

    @Test
    public void validForStatement() {
        List<Stmt> statements = parseExpression("for(var i = 0;i<5;i = i + 1) {print i;}");
        String astOutput = new ASTprinter().print(statements);
        String expectedString = "(BLOCK (i 0.0) (WHILE (< i 5.0) (BLOCK (BLOCK (PRINT i)) (i (+ i 1.0)))))";
        Assertions.assertEquals(expectedString, astOutput);
    }

    @Test
    public void validForStatementNoInitialisationAndConditionAndIncrement() {
        List<Stmt> statements = parseExpression("for(;;) {print i;}");
        String astOutput = new ASTprinter().print(statements);
        String expectedString = "(WHILE true (BLOCK (PRINT i)))";
        Assertions.assertEquals(expectedString, astOutput);
    }
    
    //Not sure if this is correct
    @Test
    public void validForStatementWithExpression() {
        List<Stmt> statements = parseExpression("for(1+2;i<5;i = i + 1) {print i;}");
        String astOutput = new ASTprinter().print(statements);
        String expectedString = "(BLOCK (+ 1.0 2.0) (WHILE (< i 5.0) (BLOCK (BLOCK (PRINT i)) (i (+ i 1.0)))))";
        Assertions.assertEquals(expectedString, astOutput);
    }

    //Too many domino errors
    @Test
    public void invalidForStatementOpeningBrac() {
        parseExpression("for 1+2;i<5;i = i + 1) {print i;}");
        String expectedString = "[line 1] Error at '1':Expect '(' after 'for'.\n[line 1] Error at ')':Expect ';' after expression.\n[line 1] Error at '}':Expect expression.";
        Assertions.assertEquals(expectedString, outputStreamCaptor.toString().trim());
    }

    @Test
    public void validIfStatement() {
        List<Stmt> statements = parseExpression("if(5 == 5) hello = 1;");
        String astBuilder = new ASTprinter().print(statements);
        Assertions.assertEquals("(IF (== 5.0 5.0) (hello 1.0) )",astBuilder);
    }

    @Test
    public void validIfElseStatement() {
        List<Stmt> statements = parseExpression("if(5 == 5) hello = 1; else hello = 2;");
        String astBuilder = new ASTprinter().print(statements);
        Assertions.assertEquals("(IF (== 5.0 5.0) (hello 1.0) (hello 2.0))",astBuilder);
    }

    @Test
    public void invalidIfStatementOpenBrac() {
        parseExpression("if 5==5) hello = world;");
        String expectedString = "[line 1] Error at '5':Expect '(' after 'if'.";
        Assertions.assertEquals(expectedString, outputStreamCaptor.toString().trim());
    }

    @Test
    public void invalidIfStatementCloseBrac() {
        parseExpression("if (5==5 hello = world;");
        String expectedString = "[line 1] Error at 'hello':Expect ')' after if condition.";
        Assertions.assertEquals(expectedString, outputStreamCaptor.toString().trim());
    }

    @Test
    public void validWhileStatement() {
        List<Stmt> statements = parseExpression("while(i<5) {print \"hi\";}");
        String astString = new ASTprinter().print(statements);
        String expectedString = "(WHILE (< i 5.0) (BLOCK (PRINT hi)))";
        Assertions.assertEquals(expectedString, astString);
    }

    @Test
    public void invalidWhileStatementOpenBrac() {
        parseExpression("while i<5) {print \"hi\";}");
        String expectedString = "[line 1] Error at 'i':Expect '(' after 'while'.\n[line 1] Error at '}':Expect expression.";
        Assertions.assertEquals(expectedString, outputStreamCaptor.toString().trim());
    }

    @Test
    public void invalidWhileStatementCloseBrac() {
        parseExpression("while(i<5 {print \"hi\";}");
        String expectedString = "[line 1] Error at '{':Expect ')' after condition.\n[line 1] Error at '}':Expect expression.";
        Assertions.assertEquals(expectedString, outputStreamCaptor.toString().trim());
    }


    //FOR NOW: We can only test +-/* operations, later we will expand when compiler can handle more
    @Test
    public void simpleBinaryTest(){
        List<Stmt> statements = parseExpression("1+1/3;");
        String astBuild = new ASTprinter().print(statements);
        Assertions.assertEquals("(+ 1.0 (/ 1.0 3.0))",astBuild);
    }

    @Test
    public void complexBinaryTest(){
        List<Stmt> statements = parseExpression("3.4+4.6/8*(4*3+1);");
        String astBuild = new ASTprinter().print(statements);
        Assertions.assertEquals("(+ 3.4 (* (/ 4.6 8.0) (group (+ (* 4.0 3.0) 1.0))))",astBuild);
    }

    

    //BONUS FEATURE TESTS
}