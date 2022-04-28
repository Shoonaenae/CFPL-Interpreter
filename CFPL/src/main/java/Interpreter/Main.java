package Interpreter;


import Interpreter.test.Runtime_Error;
import Interpreter.Source.Stmt;

import java.util.List;

public class Main {
    public static CFPL_IDE ide = new CFPL_IDE();
    private static final Interpreter interpreter = new Interpreter();

    static boolean Error = false;
    static boolean RuntimeError = false;


    private static String errorOutput = "";

    public static void runProgram(String source){
        Error = RuntimeError =false;
        errorOutput = "";
        run(source);
    }


    private static void run (String source){
        Lexer scanner = new Lexer(source);
        List<Token> tokens = scanner.scanTokens();

        for(Token tk : tokens){
            System.out.println(tk);
        }

        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();


        if (Error) return;

        interpreter.interpret(statements);
    }


    public static String getOutput(){
        String output = interpreter.getOutputList();
        interpreter.clearOutput();
        if(Error || RuntimeError){
            return errorOutput;
        }
        return output;
    }



    static void error(int line, String message) {
        report(line, "", message);
    }

    private static void report(int line, String where,
                               String message) {

        String err =  "[line " + line + "] Error" + where + ": " + message;
        System.err.println(err);

        errorOutput = err + "\n"+errorOutput;
        Error = true;
    }

    static void error(Token token, String message) {
        System.out.println("went over error");

        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    static void runtimeError(Runtime_Error error) {
        System.out.println("went over run time error");
        String err =  error.getMessage() +
                "\n[line " + error.token.line + "]";
        System.err.println(err);
        RuntimeError = true;
        errorOutput = err;
    }



}
