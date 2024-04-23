package mybar.events;

import mybar.events.common.api.RecordObject;
import mybar.web.config.JacksonConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@JsonTest
@EnableAutoConfiguration
@ImportAutoConfiguration(classes = KafkaAutoConfiguration.class)
@Import({JacksonConfiguration.class})
@Testcontainers
public abstract class KafkaTestContext {

    @Container
    private static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.3.5"));

    @Autowired
    public KafkaTemplate<String, RecordObject<?>> kafkaTemplate;

    @DynamicPropertySource
    public static void properties(DynamicPropertyRegistry propertyRegistry) {
        propertyRegistry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

}