package mybar.events.common.producer;

import lombok.extern.slf4j.Slf4j;
import mybar.events.common.api.IEventProducer;
import mybar.events.common.api.RecordObject;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;

@Slf4j
public class MyBarEventProducer<T> implements IEventProducer<T> {

    private final KafkaTemplate<String, RecordObject<?>> kafkaTemplate;

    public MyBarEventProducer(KafkaTemplate<String, RecordObject<?>> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public Instant send(String topicName, String userId, String entityId, T dto) {
        long time = System.currentTimeMillis();
        String key = asKey(userId, entityId);
        var recordObject = RecordObject.of(time, dto);

        kafkaTemplate.send(topicName, 0, time, key, recordObject)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Unable to send an event record for the key: {} due to : {}", key, ex.getMessage());
                    } else {
                        var producerRecord = result.getProducerRecord();
                        var metadata = result.getRecordMetadata();
                        var timestamp = metadata.timestamp();
                        long elapsedTime = timestamp - time;
                        log.info("Event sent: record(key={}, value={}) meta(partition={}, offset={}) elapsed time={}.",
                                producerRecord.key(), producerRecord.value(), metadata.partition(),
                                metadata.offset(), elapsedTime);
                    }
                });
        return Instant.ofEpochMilli(time);
    }

    private static String asKey(String userId, String entityId) {
        return userId + "@" + entityId;
    }

}