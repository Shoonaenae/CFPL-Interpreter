package Interpreter;

import Interpreter.test.Runtime_Error;

import java.util.HashMap;
import java.util.Map;

class GenerateEn {
    private final Map<String, Object> values = new HashMap<>();
    final GenerateEn enclosing;

    GenerateEn() {
        enclosing = null;
    }

    GenerateEn(GenerateEn enclosing) {
        this.enclosing = enclosing;
    }

    void define(String name, Object value) {
        values.put(name, value);
    }

    Object get(Token name) {
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }

        if (enclosing != null) return enclosing.get(name);

        throw new Runtime_Error(name,
                "Undefined variable '" + name.lexeme + "'.");
    }

    void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, value);
            return;
        }

        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }

        throw new Runtime_Error(name,
                "Undefined variable '" + name.lexeme + "'.");
    }
}