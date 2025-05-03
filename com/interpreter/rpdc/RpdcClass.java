package com.interpreter.rpdc;

import java.util.List;
import java.util.Map;

public class RpdcClass implements RpdcCallable{
    final String name;
    final RpdcClass superclass;
    private final Map<String, RpdcFunction> methods;

    RpdcClass(String name, RpdcClass superclass,
             Map<String, RpdcFunction> methods) {
        this.superclass = superclass;
        this.name = name;
        this.methods = methods;
    }

    RpdcFunction findMethod(String name) {
        if (methods.containsKey(name)) {
            return methods.get(name);
        }

        if (superclass != null) {
            return superclass.findMethod(name);
        }

        return null;
    }

    @Override
    public Object call(Interpreter interpreter,
                       List<Object> arguments) {
        RpdcInstance instance = new RpdcInstance(this);
        RpdcFunction initializer = findMethod("init");
        if (initializer != null) {
            initializer.bind(instance).call(interpreter, arguments);
        }

        return instance;
    }

    @Override
    public int arity() {
        RpdcFunction initializer = findMethod("init");
        if (initializer == null) return 0;
        return initializer.arity();
    }

    @Override
    public String toString() {
        return name;
    }
}
