package io.github.nadimnesar.common.model;

import io.github.nadimnesar.common.enums.DeliveryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private String messageId;
    private DeliveryStatus status;
    private String message;

    @Builder.Default
    private Instant timestamp = Instant.now();

    public SmsResponse(String messageId, DeliveryStatus status, String message) {
        this(messageId, status, message, Instant.now());
    }

    public static SmsResponse accepted(String messageId, String message) {
        return SmsResponse.builder()
                .messageId(messageId)
                .status(DeliveryStatus.QUEUED)
                .message(message)
                .build();
    }

    public static SmsResponse failed(String messageId, String message) {
        return SmsResponse.builder()
                .messageId(messageId)
                .status(DeliveryStatus.FAILED)
                .message(message)
                .build();
    }

    public static SmsResponse of(String messageId, DeliveryStatus status, String message) {
        return SmsResponse.builder()
                .messageId(messageId)
                .status(status)
                .message(message)
                .build();
    }
}
