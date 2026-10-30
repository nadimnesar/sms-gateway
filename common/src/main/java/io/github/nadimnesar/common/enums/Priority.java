package io.github.nadimnesar.common.enums;

/**
 * Priority levels for SMS messages.
 * Lower level number represents higher processing priority.
 */
public enum Priority {
    CRITICAL(1),
    HIGH(2),
    MEDIUM(3),
    LOW(4);

    private final int level;

    Priority(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    /**
     * Determines the default priority based on SMS type.
     */
    public static Priority fromSmsType(SmsType smsType) {
        if (smsType == null) {
            return MEDIUM;
        }
        return switch (smsType) {
            case ALERT -> CRITICAL;
            case OTP -> HIGH;
            case INFO -> MEDIUM;
            case PROMOTIONAL, SURVEY -> LOW;
        };
    }
}
