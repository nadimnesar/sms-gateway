package io.github.nadimnesar.common.model;

import io.github.nadimnesar.common.enums.SmsType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsRequest {
    private String recipientPhoneNumber;
    private String messageBody;
    private SmsType smsType;
}
