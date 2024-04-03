package mybar.events.consumer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import mybar.config.kafka.KafkaConsumerConfiguration;
import mybar.dto.RateDto;
import mybar.events.KafkaTestContext;
import mybar.events.common.api.RecordObject;
import mybar.events.consumers.RatesEventConsumer;
import mybar.service.rates.RatesService;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Import(KafkaConsumerConfiguration.class)
class RatesEventConsumerTest extends KafkaTestContext {

    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private RatesService ratesService;

    @Captor
    private ArgumentCaptor<String> keyArgumentCaptor;
    @Captor
    private ArgumentCaptor<RecordObject<RateDto>> rateDtoArgumentCaptor;

    @SpyBean
    private RatesEventConsumer target;

    @Value("${my-bar.events.rates-topic-name}")
    private String topic;

    @Test
    void testConsumeRatesEvent() throws IOException {
        File json = ResourceUtils.getFile("classpath:rated_cocktail_event.json");

        var recordObject = objectMapper.readValue(json, new TypeReference<RecordObject<RateDto>>() {
        });

        String key = "testUser@cocktailId";

        var expected = new RateDto();
        expected.setCocktailId("cocktailId");
        expected.setRatedAt(LocalDateTime.ofInstant(Instant.ofEpochMilli(1639939320588L), ZoneId.systemDefault()));
        expected.setStars(10);

        Mockito.doNothing()
                .when(ratesService)
                .persistRate("testUser", expected);

        kafkaTemplate.send(topic, key, recordObject);

        // test consumer
        Awaitility.await().pollInterval(Duration.ofMillis(500L))
                .atMost(Duration.ofSeconds(5L))
                .untilAsserted(() -> Mockito.verify(ratesService).persistRate("testUser", expected));

        verifyConsumedData(key, expected);
    }

    private void verifyConsumedData(String key, RateDto expected) {
        Mockito.verify(target)
                .consume(
                        keyArgumentCaptor.capture(),
                        rateDtoArgumentCaptor.capture()
                );

        Assertions.assertThat(keyArgumentCaptor.getValue())
                .isEqualTo(key);
        RecordObject<RateDto> payload = rateDtoArgumentCaptor.getValue();
        Assertions.assertThat(payload)
                .isNotNull();

        testResults(expected, payload.getValue());
    }

    private void testResults(RateDto expected, RateDto record) {
        Assertions.assertThat(record)
                .isNotNull();
        Assertions.assertThat(record)
                .isEqualTo(expected);
    }

}
