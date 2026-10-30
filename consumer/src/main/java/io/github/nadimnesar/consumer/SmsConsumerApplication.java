package io.github.nadimnesar.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "io.github.nadimnesar")
public class SmsConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmsConsumerApplication.class, args);
    }
}
