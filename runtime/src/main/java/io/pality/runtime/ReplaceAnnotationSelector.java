package io.pality.runtime;

import io.pality.api.Architecture;
import io.pality.api.OperatingSystem;
import io.pality.api.Replace;
import jdk.vm.ci.code.RegisterConfig;
import jdk.vm.ci.runtime.JVMCI;
import jdk.vm.ci.services.Services;

final class ReplaceAnnotationSelector {
    private static final RegisterConfig REGISTER_CONFIG = JVMCI.getRuntime().getHostJVMCIBackend().getCodeCache().getRegisterConfig();

    public static Replace selectReplaceAnnotation(Replace[] annotations) {
        final Architecture currentArch = determineCurrentArchitecture();
        final boolean windowsOs = Services.getSavedProperty("os.name", "").startsWith("Windows");
        Replace bestMatch = null;
        int bestMatchScore = -1;
        for (Replace annotation : annotations) {
            int score = 0;
            // Check architecture match
            if (annotation.arch() == currentArch) {
                score += 2;
            } else if (annotation.arch() == Architecture.ANY) {
                score += 1;
            } else {
                continue; // Architecture mismatch, skip this annotation
            }
            // Check OS match
            if ((windowsOs && annotation.os() == OperatingSystem.WINDOWS) ||
                (!windowsOs && annotation.os() == OperatingSystem.UNIX_LIKE)) {
                score += 2;
            } else if (annotation.os() == OperatingSystem.ANY) {
                score += 1;
            } else {
                continue; // OS mismatch, skip this annotation
            }
            if (score > bestMatchScore) {
                bestMatch = annotation;
                bestMatchScore = score;
            }
        }
        if (bestMatch == null) {
            throw new IllegalStateException("No suitable @Replace annotation found for the current system");
        }
        return bestMatch;
    }

    private static Architecture determineCurrentArchitecture() {
        final String registerConfigName = REGISTER_CONFIG.getClass().getName();
        switch (registerConfigName) {
            case "jdk.vm.ci.hotspot.amd64.AMD64HotSpotRegisterConfig":
                return Architecture.AMD64;
            case "jdk.vm.ci.hotspot.amd64.AArch64HotSpotRegisterConfig":
                return Architecture.AARCH64;
            default:
                return Architecture.ANY;
        }
    }

}
