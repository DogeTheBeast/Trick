package craftinginterpreters.trick;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.teavm.jso.JSExport;

/*Main Class
 * Runs source code files, and line by line inputs, holds the main method to run
 */
public class Trick{
    private  static  final Interpreter interpreter = new Interpreter();
    static boolean hadError = false;
    static boolean hadRuntimeError = false;

    public static void main(String[] args) throws IOException{
	System.out.println("Thanks for using Trick");
	runPrompt();
        // if(args.length > 1){
        //     System.out.println("Usage: trick [script]");
        //     System.exit(64);
        // } else if (args.length == 1){
        //     runFile(args[0]);
        // } else {
        //     runPrompt();
        // }
    }

    /*Runs a source code file to interpret
     * @param: path of string, i.e the code itself
     * @return: none
     */

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes,Charset.defaultCharset()));

        if(hadError) System.exit(65);
        if(hadRuntimeError) System.exit(70);
    }

    /*Runs code interpretation line by line
     * @param:no internal code, but takes stdin
     * @return: none
     */
    private static void runPrompt() throws IOException{
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for(;;){
            System.out.print("> ");
            String line = reader.readLine();
            if(line == null || line.equals("/0")) break;
            run(line);
            hadError = false;
        }

    }

    /*What happens once we pass some code and want to "run the interpeter" - FOR NOW JUST OUTPUTTING TOKENS
     * @param: source code
     * @return: none
     */
    @JSExport
    public static void run(String source){
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        //print tokens for debugging
        /*for(Token token : tokens){
            System.out.println(token);
        }*/

        //stop all parsing if error occurs for now
        if(hadError) return;

        Resolver resolver = new Resolver(interpreter);
        resolver.resolve(statements);

        //this is where the API from Interpreter is implemented
        interpreter.interpret(statements);

        //printing AST for debugging
        //System.out.println(new ASTprinter().print(statements));
    }

    /*Basic error handling method and its helper, tells your there is an error and where - stdout
     * @param: code line - int, where in the code - string, whats the error - string
     * @return: none
     */
    static void error(int line, String message){
        report(line, "", message);
    }
    /*|************************| Include more advanced error reporting, rather than just the line
    specify where excatly in the line with a string message*/
    static void error(Token token, String message){
        if(token.type == TokenType.EOF){
            report(token.line, " at end", message);
        } else{
            report(token.line," at '" + token.lexeme +"'", message);
        }
    }
    private static void report(int line, String where, String message){
        System.err.println("[line " + line + "] Error" + where + ":" + message);
        hadError = true;
    }

    static void runtimeError(RuntimeError error){
        System.err.println(error.getMessage() + "\n[line " + error.token.line + "]");
        hadRuntimeError = true;
    }
}
