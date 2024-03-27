package mybar.repository.rates;

import com.github.springtestdbunit.annotation.*;
import com.github.springtestdbunit.assertion.DatabaseAssertionMode;
import lombok.SneakyThrows;
import mybar.domain.History;
import mybar.domain.bar.Cocktail;
import mybar.domain.rates.Rate;
import mybar.domain.users.User;
import mybar.repository.BaseDaoTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import jakarta.persistence.Tuple;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DatabaseSetups({
        @DatabaseSetup("classpath:datasets/dataset.xml"),
        @DatabaseSetup("classpath:datasets/usersDataSet.xml"),
        @DatabaseSetup("classpath:datasets/ratesDataSet.xml")
})
@DatabaseTearDown(type = DatabaseOperation.TRUNCATE_TABLE, value = "classpath:datasets/ratesDataSet.xml")
@ContextConfiguration(classes = RatesDao.class)
public class RatesDaoTest extends BaseDaoTest {

    @Autowired
    private RatesDao ratesDao;

    private static final String START_DATE_STR = "2013-08-25";
    private static final String RATED_AT_DATETIME_STR = "2018-01-24T23:05:19";

    @Test
    @ExpectedDatabase(value = "classpath:datasets/ratesDataSet.xml", assertionMode = DatabaseAssertionMode.NON_STRICT_UNORDERED)
    public void testPreconditions() {
        // do nothing, just load and check dataSet context loads
        assertThat(ratesDao).isNotNull();
    }

    @Test
    public void testGetHistoryForPeriod() {
        LocalDate startDate = LocalDate.parse(START_DATE_STR, DateTimeFormatter.ISO_DATE);
        LocalDateTime endDate = LocalDateTime.now();

        List<History> result = ratesDao.getRatedCocktailsForPeriod(startDate.atStartOfDay(), endDate);
        assertFalse(result.isEmpty());

        Iterator<History> it = result.iterator();
        assertHistory(it.next(), "B52", 10);
        assertHistory(it.next(), "Blow Job", 5);
        assertHistory(it.next(), "Long Island Iced Tea", 7);
    }

    private void assertHistory(History history, String name, int stars) {
        assertEquals(name, history.getName());
        assertEquals(stars, history.getStars());
    }

    @Test
    public void testFindAllRatesForCocktail() {
        Cocktail cocktail = new Cocktail();
        cocktail.setId("cocktail-000001");
        List<Rate> allRatesForCocktail = ratesDao.findAllRatesForCocktail(cocktail);

        assertThat(allRatesForCocktail)
                .hasSize(2);
    }

    @Test
    public void testFindAllRatesForUser() {
        User user = new User();
        user.setUsername("JohnDoe");
        List<Rate> allRatesForCocktail = ratesDao.findAllRatesForUser(user);

        assertThat(allRatesForCocktail)
                .hasSize(5);
    }

    @SneakyThrows
    @ExpectedDatabase(
            assertionMode = DatabaseAssertionMode.NON_STRICT_UNORDERED,
            value = "classpath:datasets/expected/rates-save.xml", table = "RATES")
    @Test
    public void testSaveRate() {
        User user = testEntityManager.find(User.class, "JohnDoe");
        Cocktail cocktail = testEntityManager.find(Cocktail.class, "cocktail-000011");
        LocalDateTime rateAtDateTime = LocalDateTime.parse(RATED_AT_DATETIME_STR, DateTimeFormatter.ISO_DATE_TIME);

        Rate rate = new Rate();
        rate.setCocktail(cocktail);
        rate.setUser(user);
        rate.setStars(9);
        rate.setRatedAt(rateAtDateTime);
        ratesDao.save(rate);
        commit();
    }

    @Test
    public void testFindAllAverageRates() {
        List<Tuple> allAverageRates = ratesDao.findAllAverageRates();

        assertThat(allAverageRates)
                .hasSize(5);
        assertThat(allAverageRates.getFirst().get("avg_stars", Double.class))
                .isEqualTo(Double.parseDouble("9.5"));
    }

}