package mybar.web.rest.bar;

import mybar.api.bar.ingredient.*;
import mybar.dto.bar.ingredient.AdditiveDto;
import mybar.dto.bar.ingredient.BeverageDto;
import mybar.dto.bar.ingredient.DrinkDto;
import mybar.service.bar.IngredientService;
import mybar.web.rest.ARestControllerTest;
import mybar.web.rest.bar.exception.BarRestExceptionProcessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IngredientsController.class)
@ContextConfiguration(
        classes = {
                IngredientsController.class,
                BarRestExceptionProcessor.class
        }
)
public class IngredientsRestControllerTest extends ARestControllerTest {
    public static final String basePath = "/v1/ingredients";

    public static final String ADDITIVES = IAdditive.GROUP_NAME;
    public static final String BEVERAGES = IBeverage.GROUP_NAME;
    public static final String DRINKS = IDrink.GROUP_NAME;
    public static final String UNKNOWN = "unknown";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IngredientService ingredientServiceMock;

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void tearDown() {
        Mockito.reset(ingredientServiceMock);
    }

    @Test
    public void test_findAll_notAuthorized() throws Exception {
        when(ingredientServiceMock.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get(basePath))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void test_findAll_wrongRole() throws Exception {

        when(ingredientServiceMock.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(asAdmin(get(basePath)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void findAll_Should_ReturnAll() throws Exception {

        AdditiveDto additive = new AdditiveDto();
        additive.setId(1);
        additive.setKind("Lime");

        DrinkDto drink = new DrinkDto();
        drink.setId(2);
        drink.setKind("Black tea");
        drink.setDrinkType(DrinkType.TEA);

        BeverageDto beverage = new BeverageDto();
        beverage.setId(3);
        beverage.setKind("Whiskey");
        beverage.setBeverageType(BeverageType.DISTILLED);

        when(ingredientServiceMock.findAll()).thenReturn(Arrays.asList(additive, drink, beverage));

        mockMvc.perform(asUser(get(basePath)))

                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))

                .andExpect(jsonPath("additives.items", hasSize(1)))
                .andExpect(jsonPath("additives.items.[0].id", is(1)))
                .andExpect(jsonPath("additives.items.[0].kind", is("Lime")))

                .andExpect(jsonPath("drinks.items", hasSize(1)))
                .andExpect(jsonPath("drinks.items.[0].id", is(2)))
                .andExpect(jsonPath("drinks.items.[0].kind", is("Black tea")))
                .andExpect(jsonPath("drinks.items.[0].drinkType", is(DrinkType.TEA.name())))

                .andExpect(jsonPath("beverages.items", hasSize(1)))
                .andExpect(jsonPath("beverages.items.[0].id", is(3)))
                .andExpect(jsonPath("beverages.items.[0].kind", is("Whiskey")))
                .andExpect(jsonPath("beverages.items.[0].beverageType", is(BeverageType.DISTILLED.name())));

        verify(ingredientServiceMock, times(1)).findAll();
        verifyNoMoreInteractions(ingredientServiceMock);
    }

    @ParameterizedTest(name = "[{index}] {0}: {1}")
    @MethodSource("provideIngredientsTestData")
    public void findByGroupName_Should_ReturnFiltered(String ingredientType, List<IIngredient> ingredients) throws Exception {

        when(ingredientServiceMock.findByGroupName(ingredientType)).thenReturn(ingredients);

        String items = String.format("%s.items", ingredientType);
        var resultActions = mockMvc.perform(asUser(get(basePath))
                .param("filter", ingredientType));
        resultActions
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))

                .andExpect(jsonPath("%s.measurements", ingredientType).exists())
                .andExpect(jsonPath(items, hasSize(2)));

        for (int i = 0; i < ingredients.size(); i++) {
            var ingredient = ingredients.get(i);
            String item = String.format(items + "[%s]", i);
            resultActions
                    .andExpect(jsonPath(item + ".id", is(ingredient.getId())))
                    .andExpect(jsonPath(item + ".kind", is(ingredient.getKind())));
        }

        verify(ingredientServiceMock, times(1)).findByGroupName(ingredientType);
        verifyNoMoreInteractions(ingredientServiceMock);
    }

    @Test
    public void findByGroupName_Should_ReturnEmptyResponse_When_FilterByGroup() throws Exception {

        when(ingredientServiceMock.findByGroupName(DRINKS)).thenReturn(Collections.emptyList());

        mockMvc.perform(asUser(get(basePath))
                        .param("filter", DRINKS))

                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))

                .andExpect(jsonPath("drinks.measurements", empty()))
                .andExpect(jsonPath("drinks.items", empty()))
                .andExpect(jsonPath("drinks.isLiquid").doesNotExist());

        verify(ingredientServiceMock, times(1)).findByGroupName("drinks");
        verifyNoMoreInteractions(ingredientServiceMock);
    }

    @Test
    public void findByGroupName_Should_ThrowIllegalArgumentException_When_FilterByUnknown() throws Exception {

        when(ingredientServiceMock.findByGroupName(UNKNOWN)).thenThrow(new IllegalArgumentException("Unknown group name: " + UNKNOWN));

        mockMvc.perform(asUser(get(basePath))
                        .param("filter", UNKNOWN))

                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("errorMessage", equalTo("Unknown group name: " + UNKNOWN)));

        verify(ingredientServiceMock, times(1)).findByGroupName("unknown");
        verifyNoMoreInteractions(ingredientServiceMock);
    }

    private static Stream<Arguments> provideIngredientsTestData() {
        AdditiveDto additive1 = new AdditiveDto();
        additive1.setId(1);
        additive1.setKind("Ice");

        AdditiveDto additive2 = new AdditiveDto();
        additive2.setId(2);
        additive2.setKind("Lime");

        BeverageDto beverage3 = new BeverageDto();
        beverage3.setId(3);
        beverage3.setKind("Whiskey");
        beverage3.setBeverageType(BeverageType.DISTILLED);

        BeverageDto beverage7 = new BeverageDto();
        beverage7.setId(7);
        beverage7.setKind("Rum");
        beverage7.setBeverageType(BeverageType.DISTILLED);

        DrinkDto drink2 = new DrinkDto();
        drink2.setId(2);
        drink2.setKind("Black tea");
        drink2.setDrinkType(DrinkType.TEA);

        DrinkDto drink11 = new DrinkDto();
        drink11.setId(11);
        drink11.setKind("Golden tips");
        drink11.setDrinkType(DrinkType.TEA);

        return Stream.of(
                Arguments.of(ADDITIVES, Arrays.asList(additive1, additive2)),
                Arguments.of(BEVERAGES, Arrays.asList(beverage3, beverage7)),
                Arguments.of(DRINKS, Arrays.asList(drink11, drink2))
        );
    }

}