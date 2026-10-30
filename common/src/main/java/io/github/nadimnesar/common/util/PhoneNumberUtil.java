package io.github.nadimnesar.common.util;

import io.github.nadimnesar.common.enums.TelecomOperator;
import lombok.experimental.UtilityClass;

import java.util.regex.Pattern;

@UtilityClass
public class PhoneNumberUtil {

    private final Pattern CLEAN_PATTERN = Pattern.compile("[^0-9+]");
    private final Pattern BD_PHONE_PATTERN = Pattern.compile("^(\\+?88)?01[3-9]\\d{8}$");

    /**
     * Normalizes a phone number to standard 11-digit local format: 01XXXXXXXXX.
     * Returns null if phone number cannot be formatted.
     */
    public String normalizePhoneNumber(String rawNumber) {
        if (rawNumber == null || rawNumber.trim().isEmpty()) {
            return null;
        }

        String cleaned = CLEAN_PATTERN.matcher(rawNumber.trim()).replaceAll("");

        if (cleaned.startsWith("+8801") && cleaned.length() == 14) {
            return cleaned.substring(3);
        } else if (cleaned.startsWith("8801") && cleaned.length() == 13) {
            return cleaned.substring(2);
        } else if (cleaned.startsWith("01") && cleaned.length() == 11) {
            return cleaned;
        }

        return cleaned;
    }

    /**
     * Checks if a phone number matches Bangladesh mobile format.
     */
    public boolean isValidBdPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return false;
        }
        return BD_PHONE_PATTERN.matcher(phoneNumber.trim()).matches();
    }

    /**
     * Resolves the TelecomOperator based on standard Bangladeshi phone number prefix.
     */
    public TelecomOperator resolveOperator(String phoneNumber) {
        String normalized = normalizePhoneNumber(phoneNumber);
        if (normalized == null || normalized.length() < 3) {
            return TelecomOperator.UNKNOWN;
        }

        String prefix3 = normalized.substring(0, 3);

        return switch (prefix3) {
            case "017", "013" -> TelecomOperator.GRAMEENPHONE;
            case "018", "016" -> TelecomOperator.ROBI;
            case "019", "014" -> TelecomOperator.BANGLALINK;
            case "015" -> TelecomOperator.TELETALK;
            default -> TelecomOperator.UNKNOWN;
        };
    }
}
