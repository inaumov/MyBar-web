package mybar.repository.bar;

import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import com.github.springtestdbunit.assertion.DatabaseAssertionMode;
import mybar.api.bar.ingredient.BeverageType;
import mybar.domain.bar.Bottle;
import mybar.domain.bar.ingredient.Beverage;
import mybar.repository.DbUnitBaseTest;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DatabaseSetup("classpath:datasets/dataset.xml")
@ContextConfiguration(classes = BottleRepository.class)
public class BottleRepositoryTest extends DbUnitBaseTest {

    private static final BigDecimal PRICE = new BigDecimal("119.00");
    @Autowired
    private BottleRepository bottleRepository;

    @Test
    @Order(Integer.MIN_VALUE)
    public void testPreconditions() {
        // do nothing, just check context loads
        assertThat(bottleRepository).isNotNull();
    }

    @Test
    public void testFindAll() {
        List<Bottle> all = bottleRepository.findAll();
        assertEquals(7, all.size());

        AtomicInteger id = new AtomicInteger();
        all.forEach(bottle -> assertEquals("bottle-00000" + id.incrementAndGet(), bottle.getId()));
    }

    @Test
    public void testDeleteAll() {
        bottleRepository.deleteAll();
        commit();

        assertEquals(0, countRowsInTable("BOTTLES"));
    }

    @Test
    public void testReadById() {
        Bottle bottle = bottleRepository.getReferenceById("bottle-000007");

        assertLast(bottle);
    }

    @Test
    public void testGetBottlesByBeverage() {
        Bottle bottle = bottleRepository.getReferenceById("bottle-000003");

        assertEquals(3, bottle.getBeverage().getId().intValue());
        assertEquals("Rum", bottle.getBeverage().getKind());
        assertEquals(BeverageType.DISTILLED, bottle.getBeverage().getBeverageType());

        List<Bottle> bottlesByBeverage = bottle.getBeverage().getBottles();
        assertEquals(2, bottlesByBeverage.size());

        Optional<Bottle> bacardi = bottlesByBeverage
                .stream()
                .filter(b -> b.getBrandName().equals("Bacardi"))
                .findAny();
        assertTrue(bacardi.isPresent());

        Optional<Bottle> second = bottlesByBeverage
                .stream()
                .filter(b -> b.getBrandName().equals("Havana Club"))
                .findAny();
        assertTrue(second.isPresent());
    }

    @ExpectedDatabase(
            assertionMode = DatabaseAssertionMode.NON_STRICT_UNORDERED,
            columnFilters = {
                    EntityIdExclusionFilter.class
            },
            value = "classpath:datasets/expected/bottles-create.xml", table = "BOTTLES")
    @Test
    public void testCreateBottle() {
        Bottle bottle = new Bottle();
        bottle.setBrandName("Johny Walker");
        bottle.setPrice(new BigDecimal(289));
        bottle.setInShelf(true);
        bottle.setImageUrl("http://whiskey.last.jpg");
        bottle.setVolume(1.5);
        Beverage beverageRef = testEntityManager.find(Beverage.class, 6);
        bottle.setBeverage(beverageRef);

        Bottle saved = bottleRepository.save(bottle);
        commit();

        assertTrue(StringUtils.hasText(saved.getId()));
    }

    @ExpectedDatabase(value = "classpath:datasets/expected/bottles-update.xml", table = "BOTTLES")
    @Test
    public void testUpdateBottle() {
        Bottle bottle = new Bottle();

        // update retrieved bottle
        Beverage beverage = new Beverage();
        beverage.setId(6);
        bottle.setId("bottle-000007");
        bottle.setBeverage(beverage);
        bottle.setBrandName("Johny Walker");
        bottle.setPrice(new BigDecimal(289));
        bottle.setVolume(1.5);
        bottle.setInShelf(true);
        bottle.setImageUrl("http://whiskey.last.jpg");

        // assert updated bottle
        Bottle updated = bottleRepository.save(bottle);
        commit();

        assertEquals("Johny Walker", updated.getBrandName());
        assertEquals(updated.getBeverage().getId().intValue(), 6);
        assertTrue(updated.isInShelf());
        assertTrue(updated.getImageUrl().contains("whiskey"));
    }

    @ExpectedDatabase(value = "classpath:datasets/expected/bottles-delete.xml", table = "BOTTLES")
    @Test
    public void testDeleteBottle() {
        bottleRepository.deleteById("bottle-000001");
        commit();
    }

    private void assertLast(Bottle bottle) {
        assertEquals("bottle-000007", bottle.getId());
        assertEquals(3, bottle.getBeverage().getId().intValue());
        assertEquals("Havana Club", bottle.getBrandName());
        assertEquals(0.5, bottle.getVolume());
        assertThat(PRICE).isEqualByComparingTo(bottle.getPrice());
        assertFalse(bottle.isInShelf());
        assertTrue(bottle.getImageUrl().contains("rum"));
    }

}