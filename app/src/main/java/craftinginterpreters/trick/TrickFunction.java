package craftinginterpreters.trick;

import java.util.List;

public class TrickFunction implements TrickCallable{
    private final Stmt.Function declaration;
    private final Environment closure;

    TrickFunction(Stmt.Function declaration, Environment closure) {
        this.closure = closure;
        this.declaration = declaration;
    }

    @Override
    public int arity() {
        return declaration.params.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguements) {
        Environment environment = new Environment(closure);
        for(int i=0; i<declaration.params.size();i++) {
            environment.define(declaration.params.get(i).lexeme, arguements.get(i));
        }
        interpreter.executeBlock(declaration.body, environment);

        try{
            interpreter.executeBlock(declaration.body, environment);
        } catch(Return returnValue) {
            return returnValue.value;
        }
        return null;
    }

    @Override
    public String toString() {
        return "<fn " + declaration.name.lexeme + ">";
    }
}
