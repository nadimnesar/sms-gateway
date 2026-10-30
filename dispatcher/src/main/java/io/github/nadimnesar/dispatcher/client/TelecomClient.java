package io.github.nadimnesar.dispatcher.client;

import io.github.nadimnesar.common.enums.TelecomOperator;
import io.github.nadimnesar.common.model.SmsMessage;

public interface TelecomClient {

    /**
     * Operator handled by this client.
     */
    TelecomOperator getSupportedOperator();

    /**
     * Sends SMS to the telecom network.
     *
     * @param message SMS payload
     * @return true if successfully accepted by the telecom operator gateway
     */
    boolean sendSms(SmsMessage message);
}
