package craftinginterpreters.trick;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static craftinginterpreters.trick.TokenType.*;

class Parser {

    private static class ParseError extends RuntimeException {}

    private final List<Token> tokens;
    private int current = 0;

    private interface BinaryOperation{
        Expr opApply();
    }
    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    /*Translation of recursive descent statement parsing rule
     * If we don't match the token with a type of statement it is assumed to be an expressions statement
     * @param: none
     * @return: list of statements - List<Stmt>
     */
    public List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()){
            statements.add(declaration());
        }
        return statements;
    }

    /*Calls for var declaration or statement, and is set up to catch parse errors effectively
     * implementing the panic mode synchronization as an appropriate reset point
     * @param: none
     * @return: Stmt object
     */
    private Stmt declaration(){
        try{
            if(match(FUN)) return function("function");
            if(match(VAR)) return varDeclaration();
            return statement();
        } catch (Parser.ParseError error){
            synchronize();
            return null;
        }
    }


    private Stmt.Function function(String kind) {
        Token name = consume(IDENTIFIER, "Expect " + kind + " name.");
        consume(LEFT_PAREN, "Expect '(' after " + kind + " name.");
        List<Token> parameters = new ArrayList<>();
        if(!check(RIGHT_PAREN)) {
            do {
                if(parameters.size() >= 255) {
                    error(peek(), "Can't have more than 255 parameters.");
                }
                parameters.add(consume(IDENTIFIER, "Expect parameter name."));
            } while(match(COMMA));
        }
        consume(RIGHT_PAREN, "Expect ')' after parameters.");
        consume(LEFT_BRACE, "Expect '{' before " + kind + " body.");
        List<Stmt> body = block();
        return new Stmt.Function(name, parameters, body);
    }

    /*
     * declares a variable with an identifier name and a value if provided; otherwise null value
     * @param: none
     * @return: Stmt var object
     */
    private Stmt varDeclaration(){
        Token name = consume(IDENTIFIER,"Expect variable name.");

        Expr initializer = null;
        if(match(EQUAL)){
            initializer = expression();
        }
        consume(SEMICOLON, "Expect ';' after variable declaration.");
        return new Stmt.Var(name,initializer);
    }

    /*Match each statement to its type and make a Stmt object of it before putting it in the list
     * @param: none
     * @return: Stmt object
     */
    private Stmt statement(){
        if(match(FOR)) return forStatement();
        if(match(IF)) return  ifStatement();
        if(match(PRINT)) return printStatement();
        if(match(RETURN)) return returnStatement();
        if(match(WHILE)) return whileStatement();
        if(match(LEFT_BRACE)) return new Stmt.Block(block());

        Expr expr = expression();
        if(match(QUESTION)){
            return tertiaryStmt(expr);
        }
        return expressionStatement(expr);
    }

    private Stmt forStatement(){
        consume(LEFT_PAREN, "Expect '(' after 'for'.");

        Stmt initializer;
        if (match(SEMICOLON)){
            initializer = null;
        } else if (match(VAR)) {
            initializer = varDeclaration();
        } else{
            initializer = expressionStatement();
        }

        Expr condition = null;
        if(!check(SEMICOLON)){
            condition = expression();
        }
        consume(SEMICOLON,"Expect ';' after loop condition");

        Expr increment = null;
        if(!check(RIGHT_PAREN)){
            increment = expression();
        }
        consume(RIGHT_PAREN, "Expect ')' after for clauses.");
        Stmt body = statement();

        if(increment != null){
            body = new Stmt.Block(
                    Arrays.asList(
                            body,
                            new Stmt.Expression(increment)));
        }

        if(condition == null) condition = new Expr.Literal(true); //WARNING: allows for infinite loop
        body = new Stmt.While(condition, body);

        if(initializer != null){
            body = new Stmt.Block(Arrays.asList(initializer,body));
        }

        return body;
    }
    /*Creates our visitor pattern objects for if-else statements, to be used by Interpreter
    * @param: none
    * @return: Stmt object of innermost if-else statement*/

    private Stmt ifStatement(){
        consume(LEFT_PAREN, "Expect '(' after 'if'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after if condition.");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if(match(ELSE)){
            elseBranch = statement();
        }

        return new Stmt.If(condition,thenBranch,elseBranch);
    }

    private Stmt returnStatement() {
        Token keyword = previous();
        Expr value = null;
        if(!check(SEMICOLON))
            value = expression();

        consume(SEMICOLON, "Expect ';' after return value.");
        return new Stmt.Return(keyword, value);
    }


    /*Takes care of tertiaryStmt similar to if-else statement*/
    private Stmt tertiaryStmt(Expr condition){
        Stmt thenStmt = statement();
        Stmt elseStmt = null;
        if(match(COLON)){
            elseStmt = statement();
        }

        return new Stmt.If(condition,thenStmt,elseStmt);
    }

    private Stmt whileStatement(){
        consume(LEFT_PAREN, "Expect '(' after 'while'.");
        Expr condition = expression();
        consume(RIGHT_PAREN,"Expect ')' after condition.");
        Stmt body = statement();

        return new Stmt.While(condition,body);
    }

    /*ensure we have consumed a print statement ending with ; and returning the Stmt object
     * @param: none
     * @return: Stmt object
     */
    private Stmt printStatement(){
        Expr value = expression();
        consume(SEMICOLON,"Expect ';' after value.");
        return new Stmt.Print(value);
    }



    /*ensure we have syntactically consumed an expression and created a Stmt object for it
     * @param: none
     * @return: Stmt object
     */
    private Stmt expressionStatement(Expr expr){
        consume(SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Expression(expr);
    }

    private Stmt expressionStatement(){
        Expr expr = expression();
        consume(SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Expression(expr);
    }

    private List<Stmt> block(){
        List<Stmt> statements = new ArrayList<>();

        while (!check(RIGHT_BRACE) && !isAtEnd()){
            statements.add(declaration());
        }

        consume(RIGHT_BRACE,"Expect '}' after block.");
        return statements;
    }

    private Expr expression() {
        return assignment();
    }

    private Expr assignment() {
        Expr expr = or();

        if (match(EQUAL)) {
            Token equals = previous();
            Expr value = assignment();

            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable)expr).name;
                return new Expr.Assign(name, value);
            }
            //Not thrown as we don't need to enter panic mode  if we have not assigned properly, can easily resolve from this.
            error(equals, "Invalid assignment target.");
        }

        return expr;
    }

    /* Helper method optimizing AST build of binary expressions
    * @param:
    * @return:
    * */
    private Expr binaryOperation(BinaryOperation opApplier, TokenType... operators){
        Expr expr = opApplier.opApply();
        while(match(operators)){
            Token operator = previous();
            Expr right = opApplier.opApply();
            expr = new Expr.Binary(expr,operator,right);
        }
        return expr;
    }

    private Expr or(){ return  binaryOperation(this::and,OR); }

    private Expr and(){ return binaryOperation(this:: equality, AND); }
    private Expr equality(){
        return binaryOperation(this::comparison,BANG_EQUAL,EQUAL_EQUAL);
    }

    private Expr comparison() {
        return binaryOperation(this::term,GREATER,GREATER_EQUAL, LESS, LESS_EQUAL);
    }

    private Expr term() {
        return  binaryOperation(this::factor,MINUS,PLUS);
    }

    private Expr factor() {
        return binaryOperation(this::unary,SLASH,STAR,MODULO);
    }

    private Expr unary() {
        if(match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }
        return call();
    }

    private Expr call() {
        Expr expr = primary();

        while(true) {
            if(match(LEFT_PAREN)) {
                expr = finishCall(expr);
            } else {
                break;
            }
        }
        return expr;
    }

    private Expr finishCall(Expr callee) {
        List<Expr> arguements = new ArrayList<>();
        if(!check(RIGHT_PAREN)) {
            do {
                if(arguements.size() >= 255) 
                    error(peek(), "Can't have more than 255 arguements.");
                arguements.add(expression());
            } while(match(COMMA));
        }
        Token paren = consume(RIGHT_PAREN, "Expect ')' after arguements.");
        return new Expr.Call(callee, paren, arguements);
    }

    private Expr primary() {
        if(match(FALSE)) return new Expr.Literal(false);
        if(match(TRUE)) return new Expr.Literal(true);
        if(match(NIL)) return new Expr.Literal(null);
        if(match(NUMBER, STRING, CHAR)) return new Expr.Literal(previous().literal);
        if(match(IDENTIFIER)){
            return new Expr.Variable(previous());
        }
        if(match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }
        throw error(peek(), "Expect expression.");
    }

    private boolean match(TokenType... types) {
        for(TokenType type: types) {
            if(check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private Token consume(TokenType type, String message) {
        if(check(type)) return advance();

        throw error(peek(), message);

    }

    private boolean check(TokenType type) {
        if(isAtEnd()) return false;
        return peek().type == type;
    }

    private Token advance() {
        if(!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current-1);
    }

    private ParseError error(Token token, String message) {
        Trick.error(token, message);
        return new ParseError();
    }

    private void synchronize() {
        advance();
        while(!isAtEnd()) {
            if(previous().type == SEMICOLON) return;
            switch(peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
                default:
            }
            advance();
        }
    }
}