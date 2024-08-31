package io.pality.runtime;

import io.pality.api.MethodReplacerProvider;
import io.pality.api.Replace;
import jdk.vm.ci.code.CompiledCode;
import jdk.vm.ci.hotspot.HotSpotCompiledCode;
import jdk.vm.ci.hotspot.HotSpotCompiledNmethod;
import jdk.vm.ci.hotspot.HotSpotResolvedJavaMethod;
import jdk.vm.ci.meta.Assumptions;
import jdk.vm.ci.meta.ResolvedJavaMethod;
import jdk.vm.ci.meta.ResolvedJavaType;
import jdk.vm.ci.runtime.JVMCI;
import jdk.vm.ci.runtime.JVMCIBackend;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static io.pality.runtime.HexDecoder.hexStringToByteArray;
import static io.pality.runtime.ReplaceAnnotationSelector.selectReplaceAnnotation;
import static jdk.vm.ci.runtime.JVMCICompiler.INVOCATION_ENTRY_BCI;

public final class RuntimeMethodReplacerProvider implements MethodReplacerProvider {
    private static final JVMCIBackend JVMCI_BACKEND = JVMCI.getRuntime().getHostJVMCIBackend();

    @Override
    public void register(Class<?> classWithReplacements) {
        for (Method m : classWithReplacements.getDeclaredMethods()) {
            register(m);
        }
    }

    @Override
    public void register(Method annotatedMethod) {
        final Replace[] annotations = annotatedMethod.getAnnotationsByType(Replace.class);
        if (annotations.length == 0) {
            return;
        }
        final Replace replaceAnnotation = selectReplaceAnnotation(annotations);
        replaceMethod(annotatedMethod, replaceAnnotation);
    }

    private void replaceMethod(Method annotatedMethod, Replace replaceAnnotation) {
        final String machineCodeHex = replaceAnnotation.value();
        final ResolvedJavaMethod resolvedJavaMethod = JVMCI_BACKEND.getMetaAccess().lookupJavaMethod(annotatedMethod);
        final ResolvedJavaType resolvedJavaType = JVMCI_BACKEND.getMetaAccess().lookupJavaType(annotatedMethod.getDeclaringClass());
        final byte[] machineCode = hexStringToByteArray(machineCodeHex);
        final CompiledCode compiledCode = compileCode(resolvedJavaMethod, resolvedJavaType, machineCode);
        installMethod(resolvedJavaMethod, compiledCode);
    }

    private CompiledCode compileCode(ResolvedJavaMethod method, ResolvedJavaType type, byte[] machineCode) {
        // God save the JVMCI developers
        try {
            final Class<?> siteClass = Class.forName("jdk.vm.ci.code.site.Site");
            final Class<?> dataPatchClass = Class.forName("jdk.vm.ci.code.site.DataPatch");
            final Object sites = Array.newInstance(siteClass, 0);
            final Object dataSectionPatches = Array.newInstance(dataPatchClass, 0);
            final Assumptions.Assumption[] assumptions = createAssumptions(method, type);
            final Constructor<?> constructor = HotSpotCompiledNmethod.class.getDeclaredConstructors()[0];
            return (HotSpotCompiledNmethod) constructor.newInstance(
                    method.getName(),
                    machineCode,
                    machineCode.length,
                    sites,
                    assumptions,
                    // Expect the machine code to match the method's content
                    new ResolvedJavaMethod[] { method },
                    new HotSpotCompiledCode.Comment[0],
                    // NOTE: Data section support is not implemented
                    new byte[0],
                    8,
                    dataSectionPatches,
                    true,
                    0,
                    null,
                    method,
                    INVOCATION_ENTRY_BCI,
                    ((HotSpotResolvedJavaMethod) method).allocateCompileId(INVOCATION_ENTRY_BCI),
                    0L,
                    false
            );
        } catch (Exception e) {
            throw new RuntimeException("Cannot compile machine code: " + e.getMessage(), e);
        }
    }

    private Assumptions.Assumption[] createAssumptions(ResolvedJavaMethod method, ResolvedJavaType type) {
        final Assumptions.Assumption assumption = new Assumptions.ConcreteMethod(method, type, method);
        return new Assumptions.Assumption[] { assumption };
    }

    private void installMethod(ResolvedJavaMethod method, CompiledCode compiledCode) {
        JVMCI_BACKEND.getCodeCache().setDefaultCode(method, compiledCode);
    }

}
