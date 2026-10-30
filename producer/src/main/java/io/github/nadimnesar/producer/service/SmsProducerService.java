package io.github.nadimnesar.producer.service;

import io.github.nadimnesar.common.constant.KafkaConstants;
import io.github.nadimnesar.common.enums.DeliveryStatus;
import io.github.nadimnesar.common.enums.SmsType;
import io.github.nadimnesar.common.enums.TelecomOperator;
import io.github.nadimnesar.common.model.SmsMessage;
import io.github.nadimnesar.common.model.SmsRequest;
import io.github.nadimnesar.common.model.SmsResponse;
import io.github.nadimnesar.common.util.PhoneNumberUtil;
import io.github.nadimnesar.producer.entity.SmsMessageEntity;
import io.github.nadimnesar.producer.repository.SmsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsProducerService {

    private final KafkaTemplate<String, SmsMessage> kafkaTemplate;
    private final SmsRepository smsRepository;

    /**
     * Ingests an SMS request, determines the appropriate Kafka topic, saves it to PostgreSQL, and produces the message.
     */
    @Transactional
    public SmsResponse sendSms(SmsRequest request) {
        if (request == null || request.getRecipientPhoneNumber() == null || request.getMessageBody() == null) {
            return SmsResponse.failed(null, "Invalid SMS request: Recipient and body are required");
        }

        String normalizedPhone = PhoneNumberUtil.normalizePhoneNumber(request.getRecipientPhoneNumber());
        if (normalizedPhone == null || !PhoneNumberUtil.isValidBdPhoneNumber(normalizedPhone)) {
            return SmsResponse.failed(null, "Invalid Bangladeshi phone number: " + request.getRecipientPhoneNumber());
        }

        SmsType smsType = request.getSmsType() != null ? request.getSmsType() : SmsType.INFO;
        TelecomOperator operator = PhoneNumberUtil.resolveOperator(normalizedPhone);

        String topic = resolveTopicForType(smsType);
        String messageId = UUID.randomUUID().toString();

        SmsMessage message = SmsMessage.builder()
                .messageId(messageId)
                .recipientPhoneNumber(normalizedPhone)
                .messageBody(request.getMessageBody())
                .smsType(smsType)
                .operator(operator)
                .status(DeliveryStatus.PENDING)
                .createdAt(Instant.now())
                .build();

        // Persist message record in PostgreSQL database via Spring Data JPA
        smsRepository.save(SmsMessageEntity.fromSmsMessage(message));

        log.info("Publishing SMS [{}] of type [{}] to topic [{}] for recipient [{}] ({})",
                messageId, smsType, topic, normalizedPhone, operator);

        // Use recipient phone number as the partition key to guarantee FIFO ordering per recipient
        kafkaTemplate.send(topic, normalizedPhone, message);

        return SmsResponse.accepted(messageId, "SMS successfully queued in topic " + topic);
    }

    /**
     * Retrieves the stored details of an SMS from PostgreSQL by its unique message ID.
     */
    @Transactional(readOnly = true)
    public Optional<SmsMessage> getSmsDetails(String messageId) {
        if (messageId == null || messageId.trim().isEmpty()) {
            return Optional.empty();
        }
        return smsRepository.findById(messageId).map(SmsMessageEntity::toSmsMessage);
    }

    public String resolveTopicForType(SmsType smsType) {
        return switch (smsType) {
            case ALERT -> KafkaConstants.TOPIC_ALERT;
            case OTP -> KafkaConstants.TOPIC_OTP;
            case INFO -> KafkaConstants.TOPIC_INFO;
            case PROMOTIONAL -> KafkaConstants.TOPIC_PROMOTIONAL;
            case SURVEY -> KafkaConstants.TOPIC_SURVEY;
        };
    }
}
