module jdk.internal.vm.compiler {
    requires jdk.internal.vm.ci;
    requires java.instrument;
    requires pality.api;
    exports io.pality.runtime;
    provides io.pality.api.MethodReplacerProvider with io.pality.runtime.RuntimeMethodReplacerProvider;
}