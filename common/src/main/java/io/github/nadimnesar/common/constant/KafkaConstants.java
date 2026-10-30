package io.github.nadimnesar.common.constant;

public final class KafkaConstants {

    private KafkaConstants() {
        // Prevent instantiation
    }

    // Kafka Topics matching the architecture
    public static final String TOPIC_ALERT = "ALERT";
    public static final String TOPIC_OTP = "OTP";
    public static final String TOPIC_INFO = "INFO";
    public static final String TOPIC_PROMOTIONAL = "PROMOTIONAL";
    public static final String TOPIC_SURVEY = "SURVEY";

    // Partition counts per topic
    public static final int PARTITIONS_ALERT = 4;
    public static final int PARTITIONS_OTP = 3;
    public static final int PARTITIONS_INFO = 2;
    public static final int PARTITIONS_PROMOTIONAL = 1;
    public static final int PARTITIONS_SURVEY = 1;

    // Consumer Groups
    public static final String GROUP_CRITICAL = "critical-consumer-group";
    public static final String GROUP_HIGH = "high-consumer-group";
    public static final String GROUP_MEDIUM = "medium-consumer-group";
    public static final String GROUP_LOW = "low-consumer-group";

    // Concurrency per Consumer Group
    public static final int CONCURRENCY_CRITICAL = 4;
    public static final int CONCURRENCY_HIGH = 3;
    public static final int CONCURRENCY_MEDIUM = 2;
    public static final int CONCURRENCY_LOW = 2;
}
