package io.pality.runtime;

import io.pality.api.MethodReplacer;

import java.lang.instrument.Instrumentation;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public final class RuntimeAgent {
    public static void premain(String args, Instrumentation instrumentation) {
        exportJVMCIModulePackages(instrumentation);
        if (args != null) {
            final String[] classes = args.split(",");
            for (String c : classes) {
                try {
                    MethodReplacer.register(Class.forName(c));
                } catch (Exception e) {
                }
            }
        }
    }

    private static void exportJVMCIModulePackages(Instrumentation instrumentation) {
        try {
            final ModuleLayer bootLayer = ModuleLayer.boot();
            final Module jvmciModule = bootLayer.findModule("jdk.internal.vm.ci").orElseThrow(
                    () -> new RuntimeException("jdk.internal.vm.ci module not found. Enable it via -XX:+UnlockExperimentalVMOptions -XX:+EnableJVMCI")
            );
            final Map<String, Set<Module>> extraExports = getExtraExports();
            instrumentation.redefineModule(
                    jvmciModule,
                    Collections.emptySet(),
                    extraExports,
                    Collections.emptyMap(),
                    Collections.emptySet(),
                    Collections.emptyMap()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to enable JVMCI modules: " + e.getMessage(), e);
        }
    }

    private static Map<String, Set<Module>> getExtraExports() {
        final Set<Module> modulesToExport = Set.of(
                ClassLoader.getPlatformClassLoader().getUnnamedModule(),
                ClassLoader.getSystemClassLoader().getUnnamedModule()
        );
        return Map.of(
                "jdk.vm.ci.services", modulesToExport,
                "jdk.vm.ci.runtime", modulesToExport,
                "jdk.vm.ci.meta", modulesToExport,
                "jdk.vm.ci.code", modulesToExport,
                "jdk.vm.ci.hotspot", modulesToExport
        );
    }

}
