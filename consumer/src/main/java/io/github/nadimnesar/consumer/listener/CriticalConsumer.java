package io.github.nadimnesar.consumer.listener;

import io.github.nadimnesar.common.constant.KafkaConstants;
import io.github.nadimnesar.common.model.SmsMessage;
import io.github.nadimnesar.dispatcher.service.SmsDispatcherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Critical Consumer Group:
 * Dedicated to ALERT topic with 4 concurrent consumers (matching 4 partitions P0-P3).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CriticalConsumer {

    private final SmsDispatcherService dispatcherService;

    @KafkaListener(
            topics = KafkaConstants.TOPIC_ALERT,
            groupId = KafkaConstants.GROUP_CRITICAL,
            concurrency = "4"
    )
    public void consumeAlert(SmsMessage message) {
        log.info("[CRITICAL CONSUMER] Received ALERT message ID: {} [Priority: {}] for {}",
                message.getMessageId(), message.getPriority(), message.getRecipientPhoneNumber());
        dispatcherService.dispatch(message);
    }
}
