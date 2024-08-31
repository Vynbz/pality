package io.pality.api;

import java.lang.reflect.Method;

public interface MethodReplacerProvider {
    void register(Class<?> classWithReplacements);

    void register(Method annotatedMethod);

}
