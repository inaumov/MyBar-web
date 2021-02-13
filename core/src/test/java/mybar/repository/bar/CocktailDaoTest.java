package mybar.repository.bar;

import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import com.github.springtestdbunit.assertion.DatabaseAssertionMode;
import mybar.api.bar.Measurement;
import mybar.domain.bar.Cocktail;
import mybar.domain.bar.CocktailToIngredient;
import mybar.domain.bar.Menu;
import mybar.domain.bar.ingredient.Additive;
import mybar.domain.bar.ingredient.Beverage;
import mybar.domain.bar.ingredient.Drink;
import mybar.domain.bar.ingredient.Ingredient;
import mybar.repository.BaseDaoTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static com.github.springtestdbunit.assertion.DatabaseAssertionMode.NON_STRICT_UNORDERED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Deep Tests of Cocktail DAO.
 */
@DatabaseSetup("classpath:datasets/dataset.xml")
public class CocktailDaoTest extends BaseDaoTest {

    private static final String COCKTAIL_WITH_INGREDIENTS_ID = "cocktail-000001";
    private static final String COCKTAIL_WITH_NO_INGREDIENTS_ID = "cocktail-000009";

    @Autowired
    private MenuDao menuDao;
    @Autowired
    private CocktailDao cocktailDao;
    @Autowired
    private IngredientDao ingredientDao;

    @Test
    @ExpectedDatabase(value = "classpath:datasets/dataset.xml", assertionMode = DatabaseAssertionMode.NON_STRICT_UNORDERED)
    public void testPreconditions() throws Exception {
        // do nothing, just load and check dataSet
    }

    @ExpectedDatabase(value = "classpath:datasets/expected/cocktails-create-all-ingredients.xml",
            assertionMode = NON_STRICT_UNORDERED,
            columnFilters = {
                    EntityIdExclusionFilter.class
            },
            table = "COCKTAIL"
    )
    @ExpectedDatabase(value = "classpath:datasets/expected/cocktails-create-all-ingredients.xml",
            assertionMode = NON_STRICT_UNORDERED,
            columnFilters = {
                    EntityIdExclusionFilter.class
            },
            table = "COCKTAIL_TO_INGREDIENT"
    )
    @ExpectedDatabase(value = "classpath:datasets/expected/ingredients.xml",
            table = "INGREDIENT"
    )
    @Test
    @Transactional
    public void testCreateCocktail_WithAllIngredients() throws Exception {

        Cocktail cocktail = new Cocktail();
        cocktail.setName("New Cocktail");
        cocktail.setImageUrl("http://img.path.jpg");
        cocktail.setDescription("some description");
        cocktail.setMenuId(2);

        List<Ingredient> all = ingredientDao.findAll();
        assertThat("Number of ingredients should be 18.", all, hasSize(18));

        for (Ingredient ingredient : all) {
            // add cocktail to ingredients relation
            CocktailToIngredient cocktailToIngredient = new CocktailToIngredient();
            cocktailToIngredient.setIngredient(ingredient);
            cocktailToIngredient.setVolume(ingredient.getId() * 10);
            cocktailToIngredient.setMeasurement(Measurement.ML);
            cocktail.addCocktailToIngredient(cocktailToIngredient);
        }
        Cocktail created = cocktailDao.create(cocktail);
        em.flush();

        // refresh
        created = cocktailDao.read(created.getId());
        assertNotNull(created);
        assertTrue(created.getId().contains("cocktail-"));
        assertEquals(all.size(), created.getCocktailToIngredientList().size());
    }

    @ExpectedDatabase(value = "classpath:datasets/expected/cocktails-remove.xml",
            assertionMode = NON_STRICT_UNORDERED,
            table = "COCKTAIL"
    )
    @ExpectedDatabase(value = "classpath:datasets/expected/cocktails-remove.xml",
            assertionMode = NON_STRICT_UNORDERED,
            table = "COCKTAIL_TO_INGREDIENT"
    )
    @ExpectedDatabase(value = "classpath:datasets/expected/ingredients.xml",
            table = "INGREDIENT"
    )
    @Test
    @Transactional
    public void testRemoveCocktailFromMenuWhenNoLikes() throws Exception {
        cocktailDao.delete("cocktail-000007");
        em.flush();

        List<Menu> menuList = menuDao.findAll();
        Iterator<Menu> it = menuList.iterator();

        // test first menu
        Collection<Cocktail> cocktails = it.next().getCocktails();
        assertEquals(4, cocktails.size(), MessageFormat.format("Number of cocktails in the first [{0}] menu should be same.", menuList.get(0).getName()));

        // test second menu
        cocktails = it.next().getCocktails();
        assertEquals(6, cocktails.size(), MessageFormat.format("Number of cocktails in the second [{0}] menu should be same.", menuList.get(1).getName()));

        // test third menu
        cocktails = it.next().getCocktails();
        assertEquals(1, cocktails.size(), MessageFormat.format("Number of cocktails in the third [{0}] menu should be same.", menuList.get(2).getName()));
    }

    @Test
    public void testThrowLikesExistExceptionWhenRemove() throws Exception {
        // TODO not for the first release
    }

    @ExpectedDatabase(value = "classpath:datasets/expected/cocktails-update-add-ingredients.xml",
            assertionMode = NON_STRICT_UNORDERED,
            table = "COCKTAIL"
    )
    @ExpectedDatabase(value = "classpath:datasets/expected/cocktails-update-add-ingredients.xml",
            assertionMode = NON_STRICT_UNORDERED,
            table = "COCKTAIL_TO_INGREDIENT"
    )
    @ExpectedDatabase(value = "classpath:datasets/expected/ingredients.xml",
            table = "INGREDIENT"
    )
    @Test
    @Transactional
    public void testUpdateCocktail_when_AddNewIngredients() throws Exception {
        // Edit 'Mai Tai' cocktail and put it into 'smoothie' menu
        Cocktail cocktail = new Cocktail();
        cocktail.setId(COCKTAIL_WITH_NO_INGREDIENTS_ID);
        cocktail.setName("Random smoothie name");
        cocktail.setDescription("Random smoothie description");
        cocktail.setImageUrl("http://test-url.image.jpg");
        cocktail.setMenuId(3);

        // add cocktail to ingredients relation
        CocktailToIngredient juice = prepareCocktailToDrinkRel(13, "Orange Juice");
        CocktailToIngredient grenadine = prepareCocktailToAdditiveRel(12, "Grenadine");

        cocktail.addCocktailToIngredient(juice);
        cocktail.addCocktailToIngredient(grenadine);

        cocktailDao.update(cocktail);
        em.flush();
    }

    @ExpectedDatabase(value = "classpath:datasets/expected/cocktails-update-change-ingredients.xml",
            assertionMode = NON_STRICT_UNORDERED,
            table = "COCKTAIL"
    )
    @ExpectedDatabase(value = "classpath:datasets/expected/cocktails-update-change-ingredients.xml",
            assertionMode = NON_STRICT_UNORDERED,
            table = "COCKTAIL_TO_INGREDIENT"
    )
    @ExpectedDatabase(value = "classpath:datasets/expected/ingredients.xml",
            table = "INGREDIENT"
    )
    @Test
    @Transactional
    public void testUpdateCocktail_when_ChangeIngredients() throws Exception {
        // Edit 'B52' cocktail and add more ingredients
        Cocktail cocktail = cocktailDao.read(COCKTAIL_WITH_INGREDIENTS_ID);
        assertNotNull(cocktail);

        Cocktail cocktailForUpdate = new Cocktail();
        cocktailForUpdate.setId(COCKTAIL_WITH_INGREDIENTS_ID);
        cocktailForUpdate.setName(cocktail.getName());
        cocktailForUpdate.setDescription(cocktail.getDescription());
        cocktailForUpdate.setImageUrl(cocktail.getImageUrl());
        cocktailForUpdate.setMenuId(cocktail.getMenuId());

        // add cocktail to ingredients relation
        CocktailToIngredient juice = prepareCocktailToDrinkRel(13, "Orange Juice");
        CocktailToIngredient grenadine = prepareCocktailToAdditiveRel(12, "Grenadine");
        cocktailForUpdate.addCocktailToIngredient(juice);
        cocktailForUpdate.addCocktailToIngredient(grenadine);

        cocktailDao.update(cocktailForUpdate);
        em.flush();
    }

    private CocktailToIngredient prepareCocktailToAdditiveRel(int id, String kind) {
        CocktailToIngredient cocktailToIngredient = new CocktailToIngredient();
        Additive additive = new Additive();
        additive.setId(id);
        additive.setKind(kind);
        cocktailToIngredient.setIngredient(additive);
        cocktailToIngredient.setVolume(10);
        cocktailToIngredient.setMeasurement(Measurement.ML);
        return cocktailToIngredient;
    }

    private CocktailToIngredient prepareCocktailToDrinkRel(int id, String kind) {
        CocktailToIngredient cocktailToIngredient = new CocktailToIngredient();
        Drink ingredient = new Drink();
        ingredient.setId(id);
        ingredient.setKind(kind);
        cocktailToIngredient.setIngredient(ingredient);
        cocktailToIngredient.setVolume(250);
        cocktailToIngredient.setMeasurement(Measurement.ML);
        return cocktailToIngredient;
    }

    @Test
    public void testGetIngredientsForCocktail() throws Exception {
        Cocktail longIsland = cocktailDao.read("cocktail-000005");
        assertNotNull(longIsland);

        List<CocktailToIngredient> ingredients = longIsland.getCocktailToIngredientList();
        assertEquals(8, ingredients.size());

        assertCocktailToIngredient(1, 20, Measurement.ML, Beverage.class, findCocktailToIngredientByIngredientName(ingredients, "Vodka"));
        assertCocktailToIngredient(2, 20, Measurement.ML, Beverage.class, findCocktailToIngredientByIngredientName(ingredients, "Gin"));
        assertCocktailToIngredient(3, 20, Measurement.ML, Beverage.class, findCocktailToIngredientByIngredientName(ingredients, "Rum"));
        assertCocktailToIngredient(4, 20, Measurement.ML, Beverage.class, findCocktailToIngredientByIngredientName(ingredients, "Tequila"));
        assertCocktailToIngredient(6, 20, Measurement.ML, Beverage.class, findCocktailToIngredientByIngredientName(ingredients, "Whisky"));

        assertCocktailToIngredient(17, 150, Measurement.ML, Drink.class, findCocktailToIngredientByIngredientName(ingredients, "Coca Cola"));

        assertCocktailToIngredient(14, 5, Measurement.PCS, Additive.class, findCocktailToIngredientByIngredientName(ingredients, "Ice"));
        assertCocktailToIngredient(18, 5, Measurement.PCS, Additive.class, findCocktailToIngredientByIngredientName(ingredients, "Lime"));
    }

    private static void assertCocktailToIngredient(int ingredientId, int expectedVolume, Measurement measurement, Class type, CocktailToIngredient cocktailToIngredient) {
        assertTrue(type.isInstance(cocktailToIngredient.getIngredient()), "Ingredient class type should be same.");
        assertEquals(ingredientId, cocktailToIngredient.getIngredient().getId().intValue(), "Ingredient ID should be same.");
        assertEquals(expectedVolume, cocktailToIngredient.getVolume(), "Volume of ingredient should be same.");
        assertEquals(measurement, cocktailToIngredient.getMeasurement(), "Measurement value of ingredient should be same.");
    }

    public static CocktailToIngredient findCocktailToIngredientByIngredientName(List<CocktailToIngredient> cocktailToIngredientList,
                                                                                final String ingredientName) {
        Optional<CocktailToIngredient> cocktailToIngredientOptional = cocktailToIngredientList
                .stream()
                .filter(cocktailToIngredient -> cocktailToIngredient.getIngredient().getKind().equals(ingredientName))
                .findFirst();
        assertTrue(cocktailToIngredientOptional.isPresent(), MessageFormat.format("Cocktail to ingredient should be present for ingredientName = {0}.", ingredientName));
        return cocktailToIngredientOptional.get();
    }

    @Test
    public void testFindCocktailByName_when_exists() throws Exception {
        assertTrue(cocktailDao.findCocktailByName("Mai Tai"));
    }

    @Test
    public void testFindCocktailByName_when_not_found() throws Exception {
        assertFalse(cocktailDao.findCocktailByName("Blue Lagoon"));
    }

}