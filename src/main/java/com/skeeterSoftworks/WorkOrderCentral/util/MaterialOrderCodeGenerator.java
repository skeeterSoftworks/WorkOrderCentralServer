package com.skeeterSoftworks.WorkOrderCentral.util;

import java.time.LocalDateTime;
import java.util.function.Predicate;

/**
 * Generates material order codes: {@code NM} + {@code MMddyyyyHHmm}
 * (e.g. {@code NM053120261101} for 31 May 2026 11:01).
 */
public final class MaterialOrderCodeGenerator {

    private static final int MAX_COLLISION_SUFFIX = 99;

    private MaterialOrderCodeGenerator() {
    }

    public static String formatBase(LocalDateTime at) {
        return String.format(
                "NM%02d%02d%04d%02d%02d",
                at.getMonthValue(),
                at.getDayOfMonth(),
                at.getYear(),
                at.getHour(),
                at.getMinute());
    }

    public static String resolveUnique(LocalDateTime at, Predicate<String> codeExists) {
        String base = formatBase(at);
        if (!codeExists.test(base)) {
            return base;
        }
        for (int seq = 2; seq <= MAX_COLLISION_SUFFIX; seq++) {
            String candidate = base + "-" + seq;
            if (!codeExists.test(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Unable to allocate unique material order code for " + base);
    }
}
