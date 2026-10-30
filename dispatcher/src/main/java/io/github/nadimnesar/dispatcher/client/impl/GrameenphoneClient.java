package io.github.nadimnesar.dispatcher.client.impl;

import io.github.nadimnesar.common.enums.TelecomOperator;
import io.github.nadimnesar.common.model.SmsMessage;
import io.github.nadimnesar.dispatcher.client.TelecomClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GrameenphoneClient implements TelecomClient {

    @Override
    public TelecomOperator getSupportedOperator() {
        return TelecomOperator.GRAMEENPHONE;
    }

    @Override
    public boolean sendSms(SmsMessage message) {
        log.info("[GP-GATEWAY] Delivering SMS ID: {} to {} via Grameenphone: '{}'",
                message.getMessageId(), message.getRecipientPhoneNumber(), message.getMessageBody());
        return true;
    }
}
