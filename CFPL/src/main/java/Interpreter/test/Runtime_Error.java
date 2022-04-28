package Interpreter.test;

import Interpreter.Token;

public class Runtime_Error extends RuntimeException  {
    public Token token;

    public Runtime_Error(Token token, String message) {
        super(message);
        this.token = token;
    }

    public Runtime_Error(String message) {
        super(message);
    }
}
