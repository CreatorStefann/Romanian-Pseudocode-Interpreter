package com.interpreter.rpdc;

import java.util.List;

public class RpdcFunction implements RpdcCallable{
    private final Stmt.Function declaration;
    private final Environment closure;
    private final boolean isInitializer;

    RpdcFunction(Stmt.Function declaration, Environment closure,
                boolean isInitializer) {
        this.isInitializer = isInitializer;
        this.closure = closure;
        this.declaration = declaration;
    }

    RpdcFunction bind(RpdcInstance instance) {
        Environment environment = new Environment(closure);
        environment.define("acesta", instance);
        return new RpdcFunction(declaration, environment,
                isInitializer);
    }

    @Override
    public int arity() {
        return declaration.params.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(closure);
        for (int i = 0; i < declaration.params.size(); i++) {
            environment.define(declaration.params.get(i).lexeme,
                    arguments.get(i));
        }

        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch (Return returnValue) {
            if (isInitializer) return closure.getAt(0, "acesta");
            return returnValue.value;
        }

        if (isInitializer) return closure.getAt(0, "acesta");
        return null;
    }

    @Override
    public String toString() {
        return "<fn " + declaration.name.lexeme + ">";
    }
}
