package com.interpreter.rpdc;

import java.util.List;

public interface RpdcCallable {
    int arity();
    Object call(Interpreter interpreter, List<Object> arguments);
}
