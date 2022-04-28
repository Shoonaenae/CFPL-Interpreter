package Interpreter;


import Interpreter.test.Parse_Error;
import Interpreter.Source.Expr;
import Interpreter.Source.Stmt;

import java.util.ArrayList;
import java.util.List;


class Parser {
    private final List<Token> tokens;
    private int CNT = 0;
    boolean Err = false;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            List<Stmt> declarations = declareMany();
            if(declarations != null && declarations.size() > 0){
                statements.addAll(declarations);
            }
        }

        return statements;
    }

    private Expr expression() {
        return assignment();
    }


    private Stmt declaration() {
        try {
            if (match(TokenType.VAR)) return varDeclaration();
            return statement();
        } catch (Parse_Error error) {
            synchronize();
            return null;
        }
    }

    private List<Stmt> declareMany(){
        List<Stmt> stmts =  new ArrayList<>();
        try {
            if (match(TokenType.VAR)){
                List <Stmt.Var> vStmts = varDeclarations();
                for(Stmt.Var var : vStmts){
                    stmts.add(var);
                }
            }else{
                stmts.add(statement());
            }
            return stmts;
        } catch (Parse_Error error) {
            synchronize();
            return null;
        }
    }

    private Stmt statement() {
        if (match(TokenType.IF)) return ifStatement();
//        if (match(TokenType.PRINT)) return printStatement();
//        if (match(TokenType.LEFT_BRACE)) return new Stmt.Block(block());
        if (match(TokenType.START)) return new Stmt.Block(executable());
        Err = true;
        throw error(peek(),"Invalid Statement");
    }

    private List<Stmt> executable() {
        List<Stmt> statements = new ArrayList<>();
        if(!match(TokenType.EOL)){
            throw error(peek(), "MISSING break");
        }

        while (!check(TokenType.STOP) && !isAtEnd()) {
            statements.add(startStop());
        }

        consume(TokenType.STOP, "MISSING STOP");
        return statements;
    }

    private Stmt startStop() {
        try {
            if (match(TokenType.VAR)) return varDeclaration();
            if (match(TokenType.IF)) return ifStatement();
            if (match(TokenType.PRINT)) return printStatement();
            if (match(TokenType.LEFT_BRACE)) return new Stmt.Block(block());
            if (match(TokenType.START)) return new Stmt.Block(executable());
            return expressionStatement();
        } catch (Parse_Error error) {
            synchronize();
            return null;
        }
    }

    private Stmt ifStatement() {
        consume(TokenType.LEFT_PARENTHESIS, "MISSING '('");
        Expr condition = expression();
        consume(TokenType.RIGHT_PARENTHESIS, "MISSING ')'");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (match(TokenType.ELSE)) {
            elseBranch = statement();
        }

        return new Stmt.If(condition, thenBranch, elseBranch);
    }
    private Stmt printStatement() {
        Expr value = expression();
        consume(TokenType.EOL, "MISSING line after block.");
        return new Stmt.Print(value);
    }


    private List<Stmt.Var> varDeclarations() {
        Token name = consume(TokenType.IDENTIFIER, "MISSING VAR Name.");

        List <Stmt.Var> tempVars = new ArrayList<>();

        DataType dataType = DataType.NULL;;
        Expr initializer = null;



        if (match(TokenType.EQUAL)) {
            initializer = expression();
        }
        tempVars.add(new Stmt.Var(name, initializer, dataType));

        while(match(TokenType.COMMA)){
            name = consume(TokenType.IDENTIFIER, "MISSING VAR Name");
            initializer = null;
            if(match(TokenType.EQUAL)){

                initializer = expression();

            }
            tempVars.add(new Stmt.Var(name, initializer, dataType));
        }

        consume(TokenType.AS, "MISSING 'AS' before Data Types");

        switch(peek().type){
            case INT:
                dataType = DataType.INT;
                break;
            case CHAR:
                dataType = DataType.CHAR;
                break;

            case BOOLEAN:
                dataType = DataType.BOOLEAN;
                break;

            case FLOAT:
                dataType = DataType.FLOAT;
                break;
            //case STRING:
                //dataType = DataType.STRING;
                //break;
            default:
                break;
        }

        if(!match(TokenType.INT,TokenType.FLOAT,TokenType.CHAR,TokenType.BOOLEAN,TokenType.STRING)){
            throw error(peek(), "MISSING Data Type");
        }

        List<Stmt.Var> vars = new ArrayList<>();
        for(Stmt.Var v : tempVars){
            vars.add(new Stmt.Var(v.name,v.initializer,dataType));
        }

        consume(TokenType.EOL, "MISSING line after VAR");
        return vars;
    }


    private Stmt varDeclaration() {
        Token name = consume(TokenType.IDENTIFIER, "MISSING VAR Name.");
        DataType dataType = DataType.INT;;
        Expr initializer = null;
        if (match(TokenType.EQUAL)) {
            initializer = expression();
        }

        consume(TokenType.AS, "MISSING 'AS' after VAR.");

        switch(peek().type){
            case INT:
                dataType = DataType.INT;
                break;
            case CHAR:
                dataType = DataType.CHAR;
                break;

            case BOOLEAN:
                dataType = DataType.BOOLEAN;
                break;

            case FLOAT:
                dataType = DataType.FLOAT;
                break;
            //case STRING:
               // dataType = DataType.STRING;
                //break;
            default:
                break;
        }

        if(!match(TokenType.INT,TokenType.FLOAT,TokenType.CHAR,TokenType.BOOLEAN,TokenType.STRING)){
            throw error(peek(), "MISSING Data Type");
        }

        consume(TokenType.EOL, "MISSING line after VAR");
        return new Stmt.Var(name, initializer, dataType);
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(TokenType.EOL, "MISSING line after Expression.");
        return new Stmt.Expression(expr);
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();

        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(TokenType.RIGHT_BRACE, "MISSING '}'");
        return statements;
    }

    private Expr assignment() {
        Expr expr = equality();

        if (match(TokenType.EQUAL)) {
            Token equals = previous();
            Expr value = assignment();

            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable)expr).name;
                return new Expr.Assign(name, value);
            }

            error(equals, "Invalid Statement");
        }

        return expr;
    }

    private Expr equality() {
        Expr expr = comparison();

        while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }



    private Expr comparison() {
        Expr expr = term();

        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr term() {
        Expr expr = factor();

        while (match(TokenType.MINUS, TokenType.PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = unary();

        while (match(TokenType.SLASH, TokenType.STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return primary();
    }

    private Expr primary() {
        if (match(TokenType.FALSE)) return new Expr.Literal(false);
        if (match(TokenType.TRUE)) return new Expr.Literal(true);
        if (match(TokenType.NIL)) return new Expr.Literal(null);

        if (match(TokenType.NUMBER, TokenType.STRING, TokenType.CHAR)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(TokenType.IDENTIFIER)) {
            return new Expr.Variable(previous());
        }

        if (match(TokenType.LEFT_PARENTHESIS)) {
            Expr expr = expression();
            consume(TokenType.RIGHT_PARENTHESIS, "MISSING ')'");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "MISSING expression.");
    }



    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }
    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();

        throw error(peek(), message);
    }



    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd()) CNT++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(CNT);
    }

    private Token previous() {
        return tokens.get(CNT - 1);
    }

    private Parse_Error error(Token token, String message) {
        boolean isReservedWord = Lexer.getReservedWords().get(token.lexeme) != null;
        if(isReservedWord && !Err){
            Main.error(token, "Reserved Word");
        }else{
            Main.error(token, message);
        }
        return new Parse_Error();
    }
    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == TokenType.EOL) return;

            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            advance();
        }
    }


}