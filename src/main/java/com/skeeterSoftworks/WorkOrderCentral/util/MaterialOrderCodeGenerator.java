package com.skeeterSoftworks.WorkOrderCentral.util;

import java.time.LocalDateTime;
import java.util.function.Predicate;

/**
 * Generates material order codes: {@code NM} + {@code ddMMyyyyHHmm}
 * (e.g. {@code NM310520261101} for 31 May 2026 11:01).
 */
public final class MaterialOrderCodeGenerator {

    public static final String PREFIX = "NM";

    private MaterialOrderCodeGenerator() {
    }

    public static String formatBase(LocalDateTime at) {
        return TimestampedOrderCodeGenerator.formatBase(PREFIX, at);
    }

    public static String resolveUnique(LocalDateTime at, Predicate<String> codeExists) {
        return TimestampedOrderCodeGenerator.resolveUnique(PREFIX, at, codeExists);
    }
}
