package io.github.nadimnesar.common.model;

import io.github.nadimnesar.common.enums.DeliveryStatus;
import io.github.nadimnesar.common.enums.SmsType;
import io.github.nadimnesar.common.enums.TelecomOperator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private String messageId = UUID.randomUUID().toString();

    private String recipientPhoneNumber;
    private String messageBody;
    private SmsType smsType;
    private TelecomOperator operator;

    @Builder.Default
    private DeliveryStatus status = DeliveryStatus.PENDING;

    @Builder.Default
    private Instant createdAt = Instant.now();

    @Builder.Default
    private int retryCount = 0;

    @Builder.Default
    private Map<String, String> metadata = new HashMap<>();
}
