package mybar.events.consumers;

import lombok.extern.slf4j.Slf4j;
import mybar.dto.RateDto;
import mybar.events.common.api.RecordObject;
import mybar.service.rates.RatesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Component
public class RatesEventConsumer {

    private final RatesService ratesService;

    @Autowired
    public RatesEventConsumer(RatesService ratesService) {
        this.ratesService = ratesService;
    }

    @KafkaListener(
            topics = "${my-bar.events.rates-topic-name}",
            containerFactory = "kafkaListenerContainerFactory",
            groupId = "mb-events"
    )
    public void consume(@Header(KafkaHeaders.RECEIVED_KEY) String key, @Payload RecordObject<RateDto> recordObject) {
        log.info("Received payload='{}'", recordObject);
        try {
            RateDto rateDto = recordObject.getValue();
            List<String> keyStrings = Pattern.compile("@")
                    .splitAsStream(key)
                    .toList();
            Iterator<String> stringsIt = keyStrings.iterator();
            String userId = stringsIt.next();
            String cocktailId = stringsIt.next();
            LocalDateTime dateTime =
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(recordObject.timestamp), ZoneId.systemDefault());
            rateDto.setRatedAt(dateTime);
            rateDto.setCocktailId(cocktailId);

            ratesService.persistRate(userId, rateDto);
        } catch (Exception throwable) {
            log.error("Could not consume event for the record with key = [{}].", key, throwable);
        }
    }

}