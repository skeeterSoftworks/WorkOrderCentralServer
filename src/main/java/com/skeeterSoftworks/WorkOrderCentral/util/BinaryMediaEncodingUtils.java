package com.skeeterSoftworks.WorkOrderCentral.util;

import java.util.Base64;

public final class BinaryMediaEncodingUtils {

    private static final int MAX_CERTIFICATE_BYTES = 10 * 1024 * 1024;

    private BinaryMediaEncodingUtils() {
    }

    public static byte[] decodeBase64Payload(String b64) {
        if (b64 == null || b64.isBlank()) {
            return null;
        }
        String s = b64.trim();
        int comma = s.indexOf(',');
        if (s.startsWith("data:") && comma > 0) {
            s = s.substring(comma + 1);
        }
        return Base64.getDecoder().decode(s);
    }

    public static String encodeToDataUrl(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        String mime = detectMimeType(bytes);
        return "data:" + mime + ";base64," + Base64.getEncoder().encodeToString(bytes);
    }

    public static void validateCertificateSize(byte[] bytes) throws Exception {
        if (bytes == null || bytes.length == 0) {
            throw new Exception("MATERIAL_ORDER_CERTIFICATE_REQUIRED");
        }
        if (bytes.length > MAX_CERTIFICATE_BYTES) {
            throw new Exception("MATERIAL_ORDER_CERTIFICATE_TOO_LARGE");
        }
    }

    private static String detectMimeType(byte[] bytes) {
        if (bytes.length >= 4
                && bytes[0] == '%'
                && bytes[1] == 'P'
                && bytes[2] == 'D'
                && bytes[3] == 'F') {
            return "application/pdf";
        }
        if (bytes.length >= 8
                && bytes[0] == (byte) 0x89
                && bytes[1] == 'P'
                && bytes[2] == 'N'
                && bytes[3] == 'G') {
            return "image/png";
        }
        if (bytes.length >= 2 && (bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8) {
            return "image/jpeg";
        }
        if (bytes.length >= 6
                && bytes[0] == 'G'
                && bytes[1] == 'I'
                && bytes[2] == 'F') {
            return "image/gif";
        }
        if (bytes.length >= 12
                && bytes[0] == 'R'
                && bytes[1] == 'I'
                && bytes[2] == 'F'
                && bytes[3] == 'F') {
            return "image/webp";
        }
        return "application/octet-stream";
    }
}
