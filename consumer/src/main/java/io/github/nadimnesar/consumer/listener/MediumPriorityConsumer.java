package io.github.nadimnesar.consumer.listener;

import io.github.nadimnesar.common.constant.KafkaConstants;
import io.github.nadimnesar.common.model.SmsMessage;
import io.github.nadimnesar.dispatcher.service.SmsDispatcherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Medium Priority Consumer Group:
 * Dedicated to INFO topic with 2 concurrent consumers (matching 2 partitions P0-P1).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MediumPriorityConsumer {

    private final SmsDispatcherService dispatcherService;

    @KafkaListener(
            topics = KafkaConstants.TOPIC_INFO,
            groupId = KafkaConstants.GROUP_MEDIUM,
            concurrency = "2"
    )
    public void consumeInfo(SmsMessage message) {
        log.info("[MEDIUM PRIORITY CONSUMER] Received INFO message ID: {} for {}",
                message.getMessageId(), message.getRecipientPhoneNumber());
        dispatcherService.dispatch(message);
    }
}
