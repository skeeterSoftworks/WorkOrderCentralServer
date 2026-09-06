package com.skeeterSoftworks.WorkOrderCentral.util;

/**
 * Display helpers for server-generated order codes with id fallback.
 */
public final class OrderCodeDisplay {

    private OrderCodeDisplay() {
    }

    public static String of(String code, Long id) {
        if (code != null && !code.isBlank()) {
            return code.trim();
        }
        if (id != null && id > 0) {
            return "#" + id;
        }
        return "—";
    }

    public static String of(String code, long id) {
        return of(code, Long.valueOf(id));
    }
}
