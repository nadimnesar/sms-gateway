package io.github.nadimnesar.dispatcher.service;

import io.github.nadimnesar.common.enums.DeliveryStatus;
import io.github.nadimnesar.common.enums.TelecomOperator;
import io.github.nadimnesar.common.model.SmsMessage;
import io.github.nadimnesar.common.util.PhoneNumberUtil;
import io.github.nadimnesar.dispatcher.client.TelecomClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class SmsDispatcherService {

    private final Map<TelecomOperator, TelecomClient> clientMap = new EnumMap<>(TelecomOperator.class);

    public SmsDispatcherService(List<TelecomClient> clients) {
        for (TelecomClient client : clients) {
            clientMap.put(client.getSupportedOperator(), client);
        }
    }

    /**
     * Routes and dispatches the SMS message to the respective telecom provider.
     */
    public boolean dispatch(SmsMessage message) {
        if (message == null) {
            log.warn("Received null SMS message for dispatch");
            return false;
        }

        TelecomOperator operator = message.getOperator();
        if (operator == null || operator == TelecomOperator.UNKNOWN) {
            operator = PhoneNumberUtil.resolveOperator(message.getRecipientPhoneNumber());
            message.setOperator(operator);
        }

        TelecomClient client = clientMap.get(operator);
        if (client == null) {
            log.error("No telecom client available for operator: {} (recipient: {})",
                    operator, message.getRecipientPhoneNumber());
            message.setStatus(DeliveryStatus.FAILED);
            return false;
        }

        message.setStatus(DeliveryStatus.PROCESSING);
        try {
            boolean success = client.sendSms(message);
            if (success) {
                message.setStatus(DeliveryStatus.SENT);
                log.info("Successfully dispatched SMS ID: {} via {}", message.getMessageId(), operator);
                return true;
            } else {
                message.setStatus(DeliveryStatus.FAILED);
                log.error("Failed to dispatch SMS ID: {} via {}", message.getMessageId(), operator);
                return false;
            }
        } catch (Exception ex) {
            message.setStatus(DeliveryStatus.FAILED);
            log.error("Exception while dispatching SMS ID: {} via {}: {}",
                    message.getMessageId(), operator, ex.getMessage(), ex);
            return false;
        }
    }

    public TelecomClient getClientForOperator(TelecomOperator operator) {
        return clientMap.get(operator);
    }
}
