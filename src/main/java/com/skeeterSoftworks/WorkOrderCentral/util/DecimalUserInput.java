package com.skeeterSoftworks.WorkOrderCentral.util;

/**
 * Normalizes user-entered decimals: optional {@code ,} or {@code .} as the only decimal separator
 * (no thousand grouping); comma is stored as a dot for persistence.
 */
public final class DecimalUserInput {

    private DecimalUserInput() {
    }

    public static String normalizeToDotDecimal(String raw) {
        if (raw == null) {
            return null;
        }
        String cleaned = raw.replaceAll("[^\\d.,]", "");
        int first = indexOfSeparator(cleaned);
        if (first < 0) {
            return cleaned;
        }
        String before = cleaned.substring(0, first).replaceAll("[.,]", "");
        String rest = cleaned.substring(first + 1);
        int second = indexOfSeparator(rest);
        if (second >= 0) {
            rest = rest.substring(0, second);
        }
        rest = rest.replaceAll("[.,]", "");
        if (rest.isEmpty()) {
            return before;
        }
        String merged = before + '.' + rest;
        if (merged.startsWith(".")) {
            merged = "0" + merged;
        }
        return merged;
    }

    private static int indexOfSeparator(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == ',' || c == '.') {
                return i;
            }
        }
        return -1;
    }
}
