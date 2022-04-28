package Interpreter;

import Interpreter.test.Runtime_Error;
import Interpreter.Source.Expr;
import Interpreter.Source.Stmt;

import java.util.ArrayList;
import java.util.List;

class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void>  {

    private ArrayList<String> outputList = new ArrayList<>();
    private GenerateEn environment = new GenerateEn();


    void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (Runtime_Error error) {
            Main.runtimeError(error);
        }
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case PLUS:
                //System.out.println("visitzxvc");
                System.out.println(left instanceof Number);
                System.out.println(right instanceof Number);
                if (left instanceof Number && right instanceof Number) {
                    return (double) left + (double)right;
                }

                if (left instanceof String && right instanceof String) {
                    return (String)left + (String)right;
                }
                throw new Runtime_Error(expr.operator,
                        "Operands must be two numbers or two strings.");
            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                return (double)left - (double)right;
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                return (double)left / (double)right;
            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return (double)left * (double)right;
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return (double)left > (double)right;
            case GREATER_EQUAL:
                return (double)left >= (double)right;
            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return (double)left < (double)right;
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left <= (double)right;
            case BANG_EQUAL: return !isEqual(left, right);
            case EQUAL_EQUAL: return isEqual(left, right);
        }
        return null;
    }
    private void checkNumberOperands(Token operator,
                                     Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;

        throw new Runtime_Error(operator, "Operands must be numbers.");
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case BANG:
                return !isTruthy(right);
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return -(double)right;
        }

        // Unreachable.
        return null;
    }
    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new Runtime_Error(operator, "Operand must be a number.");
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    @Override
    public Void visitExecutableStmt(Stmt.Executable stmt) {
        executeExecutable(stmt.statements);
        return null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new GenerateEn(environment));
        return null;
    }

    void executeExecutable(List<Stmt> statements) {
            for (Stmt statement : statements) {
                execute(statement);
            }
    }

    void executeBlock(List<Stmt> statements,
                      GenerateEn environment) {
        GenerateEn previous = this.environment;
        try {
            this.environment = environment;

            for (Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }

    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean)object;
        return true;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;

        return a.equals(b);
    }
    private String stringify(Object object) {
        if (object == null) return "nil";

        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return object.toString();
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        outputList.add(stringify(value));
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            try{
                switch(stmt.dataType){
                    case INT:
                        value = (double) evaluate(stmt.initializer);
                        break;
                    case CHAR:
                        value = (char) evaluate(stmt.initializer);
                        break;

                    case BOOLEAN:
                        value = (boolean) evaluate(stmt.initializer);
                        break;
                    case FLOAT:
                        value = (double) evaluate(stmt.initializer);
                        break;

                    //case STRING:
                        //value = (String) evaluate(stmt.initializer);
                        //break;
                    default:
                        value = null;
                        break;
                }
            }catch(ClassCastException  e){
                System.out.println(e);
                Main.error(stmt.name,"Error: "+stmt.dataType+" Incorrect Datatype");
            }
        }

        environment.define(stmt.name.lexeme, value);
        return null;
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);
        environment.assign(expr.name, value);
        return value;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return environment.get(expr.name);
    }


    public String getOutputList() {
        StringBuilder sb = new StringBuilder();
        for (String s : outputList)
        {
            sb.append(s);
            sb.append("\n");
        }

        return sb.toString();
    }

    public void clearOutput() {
        outputList = new ArrayList<>();
    }
}