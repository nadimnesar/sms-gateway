package io.github.nadimnesar.common.model;

import io.github.nadimnesar.common.enums.DeliveryStatus;
import io.github.nadimnesar.common.enums.Priority;
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
public class SmsMessage implements Serializable, Comparable<SmsMessage> {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private String messageId = UUID.randomUUID().toString();

    private String recipientPhoneNumber;
    private String messageBody;
    private SmsType smsType;
    private TelecomOperator operator;

    @Builder.Default
    private Priority priority = Priority.MEDIUM;

    @Builder.Default
    private DeliveryStatus status = DeliveryStatus.PENDING;

    @Builder.Default
    private Instant createdAt = Instant.now();

    @Builder.Default
    private int retryCount = 0;

    @Builder.Default
    private Map<String, String> metadata = new HashMap<>();

    @Override
    public int compareTo(SmsMessage other) {
        if (other == null) {
            return -1;
        }

        // 1. Higher priority first (lower level number has higher precedence: 1 < 2 < 3 < 4)
        Priority thisPriority = this.priority != null ? this.priority : Priority.LOW;
        Priority otherPriority = other.priority != null ? other.priority : Priority.LOW;
        int priorityComparison = Integer.compare(thisPriority.getLevel(), otherPriority.getLevel());
        if (priorityComparison != 0) {
            return priorityComparison;
        }

        // 2. FIFO order for same priority based on creation timestamp
        if (this.createdAt != null && other.createdAt != null) {
            int timeComparison = this.createdAt.compareTo(other.createdAt);
            if (timeComparison != 0) {
                return timeComparison;
            }
        }

        // 3. Fallback tie-breaker by messageId
        if (this.messageId != null && other.messageId != null) {
            return this.messageId.compareTo(other.messageId);
        }

        return 0;
    }
}
