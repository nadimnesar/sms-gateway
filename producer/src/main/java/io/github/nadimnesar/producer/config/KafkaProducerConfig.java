package io.github.nadimnesar.producer.config;

import io.github.nadimnesar.common.constant.KafkaConstants;
import io.github.nadimnesar.common.model.SmsMessage;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, SmsMessage> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class);
        configProps.put(JacksonJsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, SmsMessage> kafkaTemplate(ProducerFactory<String, SmsMessage> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    // Kafka Topics matching the architecture partition requirements
    @Bean
    public NewTopic alertTopic() {
        return TopicBuilder.name(KafkaConstants.TOPIC_ALERT)
                .partitions(KafkaConstants.PARTITIONS_ALERT)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic otpTopic() {
        return TopicBuilder.name(KafkaConstants.TOPIC_OTP)
                .partitions(KafkaConstants.PARTITIONS_OTP)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic infoTopic() {
        return TopicBuilder.name(KafkaConstants.TOPIC_INFO)
                .partitions(KafkaConstants.PARTITIONS_INFO)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic promotionalTopic() {
        return TopicBuilder.name(KafkaConstants.TOPIC_PROMOTIONAL)
                .partitions(KafkaConstants.PARTITIONS_PROMOTIONAL)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic surveyTopic() {
        return TopicBuilder.name(KafkaConstants.TOPIC_SURVEY)
                .partitions(KafkaConstants.PARTITIONS_SURVEY)
                .replicas(1)
                .build();
    }
}
