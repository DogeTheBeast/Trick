package craftinginterpreters.trick;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
public class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void>{
    private final Interpreter interpreter;
    private final Stack<Map<String,Boolean>> scopes = new Stack<>();

    /*Constructor to have an active interpreter object when Resolver object created
    * @param: interpreter object
    * @return: none*/
    Resolver(Interpreter interpreter){
        this.interpreter = interpreter;
    }

    /*Run through statements resolving all of them
    * @param: list of statement objects
    * @return: none*/
    void resolve(List<Stmt> statements){
        for (Stmt statement : statements){
            resolve(statement);
        }
    }
    /*binds function name and binds parameters to body's scope
    * @param: Function object of the Stmt class
    * @return: none*/
    private void resolveFunction(Stmt.Function function){
        beginScope();
        for(Token param: function.params){
            declare(param);
            define(param);
        }
        resolve(function.body);
        endScope();
    }

    /*Local Stack Management: push and pop stack items of program expr/stmt keys
    * @param: none
    * @return: none*/
    private void beginScope(){
        scopes.push(new HashMap<String,Boolean>());
    }
    /*^^*/
    private void endScope(){
        scopes.pop();
    }

    /*Declare we have a variable being defined but not ready for use
    * @param: token name of variable
    * @return: none*/
    private void declare(Token name){
        if(scopes.isEmpty()) return;

        Map<String,Boolean> scope = scopes.peek();
        scope.put(name.lexeme,false);
    }
    /*After initializer evaluation is confirmed we identify the variable to ready for use
    * @param: name of variable
    * @return: none*/
    private void define(Token name){
        if(scopes.isEmpty()) return;
        scopes.peek().put(name.lexeme,true);
    }

    private void resolveLocal(Expr expr, Token name){
        for(int i = scopes.size() - 1; i >= 0; i--){
            if(scopes.get(i).containsKey(name.lexeme)){
                interpreter.resolve(expr,scopes.size() - 1 - i);
                return;
            }
        }
    }

    /*visit like the interpreter without actual implementation
    * @param: Block Stmt object
    * @return: null*/
    @Override
    public Void visitBlockStmt(Stmt.Block stmt){
        beginScope();
        resolve(stmt.statements);
        endScope();
        return null;
    }
    /*^^*/
    @Override
    public Void visitFunctionStmt(Stmt.Function stmt){
        declare(stmt.name);
        define(stmt.name);

        resolveFunction(stmt);
        return null;
    }
    /* ^^ */
    @Override
    public Void visitVarStmt(Stmt.Var stmt){
        declare(stmt.name);
        if(stmt.initializer != null){
            resolve(stmt.initializer);
        }
        define(stmt.name);
        return null;
    }
    /*Requirment to implement all other abstract classes by Stmt*/
    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt){
        resolve(stmt.expression);
        return null;
    }
    @Override
    public Void visitIfStmt(Stmt.If stmt){
        resolve(stmt.condition);
        resolve(stmt.thenBranch);
        if(stmt.elseBranch != null) resolve(stmt.elseBranch);
        return null;
    }
    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        resolve(stmt.expression);
        return null;
    }
    @Override
    public Void visitReturnStmt(Stmt.Return stmt){
        if(stmt.value != null){
            resolve(stmt.value);
        }

        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        resolve(stmt.condition);
        resolve(stmt.body);
        return null;
    }

    /*With the stack defined if variable is initialized or in progress,
    * we tell the user if they can use that variable or not
    * i.e: var a = a is not  allowed, as a is not yet defined in the stack
    * @param: variable expression object
    * @return: null*/
    @Override
    public Void visitVariableExpr(Expr.Variable expr){
        if(!scopes.isEmpty() &&
            scopes.peek().get(expr.name.lexeme) == Boolean.FALSE){
            Trick.error(expr.name,"Can't read local variable in its own initializer.");
        }
        resolveLocal(expr,expr.name);
        return null;
    }
    /*^^*/
    @Override
    public Void visitAssignExpr(Expr.Assign expr){
        resolve(expr.value);
        resolveLocal(expr,expr.name);
        return null;
    }
    /*Requirement to implement all abstract classes by Expr*/
    @Override
    public Void visitBinaryExpr(Expr.Binary expr){
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitCallExpr(Expr.Call expr) {
        resolve(expr.callee);

        for (Expr argument : expr.arguements){
            resolve(argument);
        }

        return null;
    }

    @Override
    public Void visitGroupingExpr(Expr.Grouping expr) {
        resolve(expr.expression);
        return null;
    }

    @Override
    public Void visitLiteralExpr(Expr.Literal expr) {
        return null;
    }
    @Override
    public Void visitLogicalExpr(Expr.Logical expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitUnaryExpr(Expr.Unary expr) {
        resolve(expr.right);
        return null;
    }

    /*Sends stmts or exprs to acceptors implementing visitor pattern
    * @param: stmt/expr object
    * @return: none*/
    private void resolve(Stmt stmt){
        stmt.accept(this);
    }
    /*^^*/
    private void resolve(Expr expr){
        expr.accept(this);
    }
}
