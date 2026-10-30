package io.github.nadimnesar.consumer.listener;

import io.github.nadimnesar.common.constant.KafkaConstants;
import io.github.nadimnesar.common.model.SmsMessage;
import io.github.nadimnesar.dispatcher.service.SmsDispatcherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Low Priority Consumer Group:
 * Dedicated to PROMOTIONAL and SURVEY topics with 2 concurrent consumers.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LowPriorityConsumer {

    private final SmsDispatcherService dispatcherService;

    @KafkaListener(
            topics = {KafkaConstants.TOPIC_PROMOTIONAL, KafkaConstants.TOPIC_SURVEY},
            groupId = KafkaConstants.GROUP_LOW,
            concurrency = "2"
    )
    public void consumeLowPriority(SmsMessage message) {
        log.info("[LOW PRIORITY CONSUMER] Received [{}] message ID: {} for {}",
                message.getSmsType(), message.getMessageId(), message.getRecipientPhoneNumber());
        dispatcherService.dispatch(message);
    }
}
