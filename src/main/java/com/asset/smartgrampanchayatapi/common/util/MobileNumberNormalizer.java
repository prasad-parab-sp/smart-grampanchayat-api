package com.asset.smartgrampanchayatapi.common.util;

/**
 * Normalizes Indian mobile numbers to 10 digits (strips non-digits; drops leading {@code 91} when present).
 */
public final class MobileNumberNormalizer {

    private MobileNumberNormalizer() {
    }

    public static String normalize(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        String digits = raw.replaceAll("\\D", "");
        if (digits.length() == 12 && digits.startsWith("91")) {
            digits = digits.substring(2);
        }
        if (digits.length() > 10) {
            digits = digits.substring(digits.length() - 10);
        }
        return digits;
    }

    public static boolean isValidTenDigit(String normalized) {
        return normalized != null && normalized.matches("\\d{10}");
    }
}
