package io.github.nadimnesar.common.enums;

/**
 * Status of an SMS message throughout the gateway lifecycle.
 */
public enum DeliveryStatus {
    PENDING,
    QUEUED,
    PROCESSING,
    SENT,
    DELIVERED,
    FAILED
}
