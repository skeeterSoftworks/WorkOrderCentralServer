package com.skeeterSoftworks.WorkOrderCentral.util;

import java.time.LocalDateTime;
import java.util.function.Predicate;

/**
 * Generates order codes: {@code prefix} + {@code ddMMyyyyHHmm}
 * (e.g. {@code NK310520261101} for prefix {@code NK} on 31 May 2026 11:01).
 */
public final class TimestampedOrderCodeGenerator {

    private static final int MAX_COLLISION_SUFFIX = 99;

    private TimestampedOrderCodeGenerator() {
    }

    public static String formatBase(String prefix, LocalDateTime at) {
        return String.format(
                "%s%02d%02d%04d%02d%02d",
                prefix,
                at.getDayOfMonth(),
                at.getMonthValue(),
                at.getYear(),
                at.getHour(),
                at.getMinute());
    }

    public static String resolveUnique(String prefix, LocalDateTime at, Predicate<String> codeExists) {
        String base = formatBase(prefix, at);
        if (!codeExists.test(base)) {
            return base;
        }
        for (int seq = 2; seq <= MAX_COLLISION_SUFFIX; seq++) {
            String candidate = base + "-" + seq;
            if (!codeExists.test(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Unable to allocate unique order code for " + base);
    }
}
