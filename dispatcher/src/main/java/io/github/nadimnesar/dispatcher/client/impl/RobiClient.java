package io.github.nadimnesar.dispatcher.client.impl;

import io.github.nadimnesar.common.enums.TelecomOperator;
import io.github.nadimnesar.common.model.SmsMessage;
import io.github.nadimnesar.dispatcher.client.TelecomClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RobiClient implements TelecomClient {

    @Override
    public TelecomOperator getSupportedOperator() {
        return TelecomOperator.ROBI;
    }

    @Override
    public boolean sendSms(SmsMessage message) {
        log.info("[ROBI-GATEWAY] Delivering [{}] SMS ID: {} to {} via Robi Axiata: '{}'",
                message.getPriority(), message.getMessageId(), message.getRecipientPhoneNumber(), message.getMessageBody());
        return true;
    }
}
