package io.github.nadimnesar.consumer.listener;

import io.github.nadimnesar.common.constant.KafkaConstants;
import io.github.nadimnesar.common.model.SmsMessage;
import io.github.nadimnesar.dispatcher.service.SmsDispatcherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * High Priority Consumer Group:
 * Dedicated to OTP topic with 3 concurrent consumers (matching 3 partitions P0-P2).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HighPriorityConsumer {

    private final SmsDispatcherService dispatcherService;

    @KafkaListener(
            topics = KafkaConstants.TOPIC_OTP,
            groupId = KafkaConstants.GROUP_HIGH,
            concurrency = "3"
    )
    public void consumeOtp(SmsMessage message) {
        log.info("[HIGH PRIORITY CONSUMER] Received OTP message ID: {} [Priority: {}] for {}",
                message.getMessageId(), message.getPriority(), message.getRecipientPhoneNumber());
        dispatcherService.dispatch(message);
    }
}
