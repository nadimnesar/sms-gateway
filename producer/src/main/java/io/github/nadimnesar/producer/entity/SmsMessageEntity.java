package io.github.nadimnesar.producer.entity;

import io.github.nadimnesar.common.enums.DeliveryStatus;
import io.github.nadimnesar.common.enums.SmsType;
import io.github.nadimnesar.common.enums.TelecomOperator;
import io.github.nadimnesar.common.model.SmsMessage;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "sms_messages")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsMessageEntity {

    @Id
    @Column(name = "message_id", nullable = false, updatable = false, length = 64)
    private String messageId;

    @Column(name = "recipient_phone_number", nullable = false, length = 32)
    private String recipientPhoneNumber;

    @Column(name = "message_body", nullable = false, length = 1000)
    private String messageBody;

    @Enumerated(EnumType.STRING)
    @Column(name = "sms_type", nullable = false, length = 32)
    private SmsType smsType;

    @Enumerated(EnumType.STRING)
    @Column(name = "operator", nullable = false, length = 32)
    private TelecomOperator operator;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private DeliveryStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "sms_metadata", joinColumns = @JoinColumn(name = "message_id"))
    @MapKeyColumn(name = "meta_key", length = 100)
    @Column(name = "meta_value", length = 500)
    @Builder.Default
    private Map<String, String> metadata = new HashMap<>();

    public SmsMessage toSmsMessage() {
        return SmsMessage.builder()
                .messageId(this.messageId)
                .recipientPhoneNumber(this.recipientPhoneNumber)
                .messageBody(this.messageBody)
                .smsType(this.smsType)
                .operator(this.operator)
                .status(this.status)
                .createdAt(this.createdAt)
                .retryCount(this.retryCount)
                .metadata(this.metadata != null ? new HashMap<>(this.metadata) : new HashMap<>())
                .build();
    }

    public static SmsMessageEntity fromSmsMessage(SmsMessage message) {
        if (message == null) {
            return null;
        }
        return SmsMessageEntity.builder()
                .messageId(message.getMessageId())
                .recipientPhoneNumber(message.getRecipientPhoneNumber())
                .messageBody(message.getMessageBody())
                .smsType(message.getSmsType())
                .operator(message.getOperator())
                .status(message.getStatus())
                .createdAt(message.getCreatedAt())
                .retryCount(message.getRetryCount())
                .metadata(message.getMetadata() != null ? new HashMap<>(message.getMetadata()) : new HashMap<>())
                .build();
    }
}
