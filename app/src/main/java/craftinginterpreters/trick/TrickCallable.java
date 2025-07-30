package craftinginterpreters.trick;

import java.util.List;

interface TrickCallable {
    int arity();
    Object call(Interpreter interpreter, List<Object> arguements);
}
