package io.pality.api;

import java.lang.reflect.Method;

public final class NoOpMethodReplacerProvider implements MethodReplacerProvider {
    @Override
    public void register(Class<?> classWithReplacements) {
    }

    @Override
    public void register(Method annotatedMethod) {
    }

}
