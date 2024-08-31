package io.pality.api;

import java.lang.reflect.Method;
import java.util.ServiceLoader;

public final class MethodReplacer {
    private static final MethodReplacerProvider METHOD_REPLACER_PROVIDER;

    static {
        METHOD_REPLACER_PROVIDER = ServiceLoader.load(MethodReplacerProvider.class, ClassLoader.getSystemClassLoader())
                .findFirst()
                .orElseGet(NoOpMethodReplacerProvider::new);
    }

    public static void register(Class<?> classWithReplacements) {
        METHOD_REPLACER_PROVIDER.register(classWithReplacements);
    }

    public static void register(Method annotatedMethod) {
        METHOD_REPLACER_PROVIDER.register(annotatedMethod);
    }

}
