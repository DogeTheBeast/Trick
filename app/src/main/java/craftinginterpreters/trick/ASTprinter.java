package craftinginterpreters.trick;

import java.util.List;

// Temporary AST printer for each expression type using the visitor pattern
class ASTprinter implements Expr.Visitor<String>, Stmt.Visitor<String>{

    String print(List<Stmt> statements) {
        StringBuilder sb = new StringBuilder();
        for(Stmt statement:statements)
            sb.append(statement.accept(this));
        return sb.toString();
    }

    @Override
    public String visitAssignExpr(Expr.Assign expr) {
        return parenthesize(expr.name.lexeme, expr.value);
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return parenthesize("group", expr.expression);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if(expr.value == null) return "nil";
        return expr.value.toString();
    }

    @Override
    public String visitLogicalExpr(Expr.Logical expr) {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return parenthesize(expr.operator.lexeme, expr.right);
    }

    @Override
    public String visitVariableExpr(Expr.Variable expr) {
        return expr.name.lexeme;
    }

    @Override
    public String visitBlockStmt(Stmt.Block stmt) {
        StringBuilder builder = new StringBuilder();
        builder.append("(").append("BLOCK");
        List<Stmt> statements = stmt.statements;
        for(Stmt statement: statements) {
            builder.append(" ");
            builder.append(statement.accept(this));
        }
        builder.append(")");
        return builder.toString();
    }
    
    @Override
    public String visitExpressionStmt(Stmt.Expression stmt) {
        return stmt.expression.accept(this);
    }

    @Override
    public String visitIfStmt(Stmt.If stmt) {
        StringBuilder builder = new StringBuilder();
        builder.append("(").append("IF");
        builder.append(" ");
        builder.append(stmt.condition.accept(this));
        builder.append(" ");
        builder.append(stmt.thenBranch.accept(this));
        builder.append(" ");
        if(stmt.elseBranch != null)
            builder.append(stmt.elseBranch.accept(this));
        builder.append(")");
        return builder.toString();
    }

    @Override
    public String visitPrintStmt(Stmt.Print stmt) {
        return parenthesize("PRINT", stmt.expression);
    }

    @Override
    public String visitVarStmt(Stmt.Var stmt) {
        return parenthesize(stmt.name.lexeme, stmt.initializer);
    }
    
    @Override
    public String visitWhileStmt(Stmt.While stmt) {
        StringBuilder builder = new StringBuilder();
        builder.append("(").append("WHILE");
        builder.append(" ");
        builder.append(stmt.condition.accept(this));
        builder.append(" ");
        builder.append(stmt.body.accept(this));
        builder.append(")");
        return builder.toString();
    }

    @Override
    public String visitCallExpr(Expr.Call call) {
        return "HI";
    }

    private String parenthesize(String name, Expr... exprs) {
        StringBuilder builder = new StringBuilder();
        builder.append("(").append(name);
        if(exprs[0] != null) {
            for (Expr expr:exprs) {
                builder.append(" ");
                builder.append(expr.accept(this));
            }
        }
        builder.append(")");
        return builder.toString();
    }

}