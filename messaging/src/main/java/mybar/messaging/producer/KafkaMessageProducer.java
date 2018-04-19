package mybar.messaging.producer;

import lombok.Setter;
import mybar.messaging.IMessageProducer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class KafkaMessageProducer implements IMessageProducer {

    private final String TOPIC;
    private final String BOOTSTRAP_SERVERS;
    private final String PRODUCER_CLIENT_ID;

    @Setter
    public Producer<String, String> producer;

    public KafkaMessageProducer(String topic, String servers, String producerClientId) {
        this.TOPIC = topic;
        this.PRODUCER_CLIENT_ID = producerClientId;
        this.BOOTSTRAP_SERVERS = servers;
    }

    private Producer<String, String> createProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, PRODUCER_CLIENT_ID);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        return producer == null ? new KafkaProducer<>(props) : producer;
    }

    @Override
    public void send(final String key, final String object) {
        final Producer<String, String> producer = createProducer();
        long time = System.currentTimeMillis();
        try {
            final ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, key, object);
            RecordMetadata metadata = producer.send(record).get();
            long elapsedTime = System.currentTimeMillis() - time;
            System.out.printf("sent record(key=%s value=%s) meta(partition=%d, offset=%d) time=%d\n",
                    record.key(), record.value(), metadata.partition(),
                    metadata.offset(), elapsedTime);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            producer.flush();
            producer.close();
        }
    }

}