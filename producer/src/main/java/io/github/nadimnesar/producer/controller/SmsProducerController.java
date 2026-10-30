package io.github.nadimnesar.producer.controller;

import io.github.nadimnesar.common.enums.DeliveryStatus;
import io.github.nadimnesar.common.model.SmsMessage;
import io.github.nadimnesar.common.model.SmsRequest;
import io.github.nadimnesar.common.model.SmsResponse;
import io.github.nadimnesar.producer.service.SmsProducerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/sms")
@RequiredArgsConstructor
public class SmsProducerController {

    private final SmsProducerService producerService;

    /**
     * REST API 1: Send SMS
     * POST /api/v1/sms/send or POST /api/v1/sms
     */
    @PostMapping({"", "/send"})
    public ResponseEntity<SmsResponse> sendSms(@RequestBody SmsRequest request) {
        SmsResponse response = producerService.sendSms(request);
        if (response.getStatus() == DeliveryStatus.FAILED) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    /**
     * REST API 2: View SMS Details
     * GET /api/v1/sms/{messageId} or GET /api/v1/sms/details/{messageId}
     */
    @GetMapping({"/{messageId}", "/details/{messageId}"})
    public ResponseEntity<SmsMessage> getSmsDetails(@PathVariable String messageId) {
        return producerService.getSmsDetails(messageId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "sms-producer"
        ));
    }
}
