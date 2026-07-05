package com.skeeterSoftworks.WorkOrderCentral.util;

import org.springframework.util.StringUtils;

public final class AssignmentOrderCodes {

    private AssignmentOrderCodes() {
    }

    public static String normalize(String code) {
        if (!StringUtils.hasText(code)) {
            return null;
        }
        String digits = code.trim().replaceAll("\\s+", "");
        if (!digits.matches("\\d{8}")) {
            return null;
        }
        return digits;
    }
}
