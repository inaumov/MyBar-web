package mybar.events.common.producer;

import mybar.events.KafkaTestContext;
import mybar.events.common.api.RecordObject;
import org.apache.kafka.clients.consumer.Consumer;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.time.Instant;
import java.util.Collections;

class MyBarEventProducerTest extends KafkaTestContext {

    private final String TOPIC = "my_bar_test.ANY_TOPIC";

    @Autowired
    private ConsumerFactory<String, RecordObject<TestEvent>> consumerFactory;

    private MyBarEventProducer<TestEvent> target;

    @BeforeEach
    void setUp() {
        target = new MyBarEventProducer<>(kafkaTemplate);
    }

    @Test
    void testProducer() {
        Instant timeSent = target.send(TOPIC, "userX", "testId", new TestEvent("aaa"));

        var singleRecord = KafkaTestUtils.getSingleRecord(createTestEventConsumer(), TOPIC);
        var recordValue = singleRecord.value();

        Assertions.assertThat(timeSent)
                .isNotNull();
        Assertions.assertThat(timeSent.toEpochMilli())
                .isEqualTo(recordValue.getTimestamp());

        TestEvent testEvent = recordValue.getValue();
        Assertions.assertThat(testEvent.getTestData())
                .isEqualTo("aaa");
        Assertions.assertThat(singleRecord.key()).
                isEqualTo("userX@testId");
    }

    private Consumer<String, RecordObject<TestEvent>> createTestEventConsumer() {
        Consumer<String, RecordObject<TestEvent>> consumer = consumerFactory.createConsumer();
        consumer.subscribe(Collections.singleton(TOPIC));
        return consumer;
    }

}