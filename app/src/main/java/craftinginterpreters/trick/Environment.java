package craftinginterpreters.trick;


import java.util.HashMap;
import java.util.Map;

public class Environment {
    final Environment enclosing;
    final Map<String,Object> values = new HashMap<>();

    Environment(){
        enclosing = null;
    }

    Environment(Environment enclosing){
        this.enclosing = enclosing;
    }

    /*Get variable value if found otherwise throws run time error
    * @param: Token object of variable
    * @return: Object value of the found variable
    */
    Object get(Token name){
        if(values.containsKey(name.lexeme)){
            return values.get(name.lexeme);
        }
        if(enclosing != null) return enclosing.get(name);

        throw new RuntimeError(name,"Undefined variable '" + name.lexeme + "'.");
    }
    /*Similar to define a new variable except we cant make new ones only assign existing ones
    * @param: Token name of var, Object the object value of var
    * @return: none*/
    void assign(Token name, Object value){
        if(values.containsKey(name.lexeme)){
            values.put(name.lexeme,value);
            return;
        }
        if(enclosing != null){
            enclosing.assign(name, value);
            return;
        }

        throw new RuntimeError(name,
                "Undefined variable '" + name.lexeme + "'.");
    }

    /*Puts the name and value in the hashmap environment. SEMANTIC CHOICE:
    * this method allows us to define and redefine without errors (i.e var a = 1, then var a = 2 is valid
    * there are different approaches to this, but we will follow Scheme's lexical variable scoping and follow this rule
    * @param: String name of variable, Object value of assigned variable
    * @return: none*/
    void define(String name, Object value){
        values.put(name,value);
    }
    /*Looks up the variable at the specific distance wanted - rather than blind walk up chain
    * Note: this assumes resolver ensures the variable exists, hence a deep coupling
    * with interpreter and resolver
    * @param: distance between defined and call, name of the variable
    * @return: object value of variable*/
    Object getAt(int distance, String name){
        return ancestor(distance).values.get(name);
    }
    /*Same as ^^ but for assigning vars
    * @param: distance of environments, name of var, object val of var */
    void assignAt(int distance, Token name, Object value){
        ancestor(distance).values.put(name.lexeme,value);
    }
    Environment ancestor(int distance){
        Environment environment = this;
        for(int i = 0; i < distance; i++){
            environment = environment.enclosing;
        }

        return environment;
    }
}
