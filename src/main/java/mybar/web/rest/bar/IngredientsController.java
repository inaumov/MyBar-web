package mybar.web.rest.bar;

import lombok.extern.slf4j.Slf4j;
import mybar.api.bar.Measurement;
import mybar.api.bar.ingredient.IAdditive;
import mybar.api.bar.ingredient.IBeverage;
import mybar.api.bar.ingredient.IDrink;
import mybar.api.bar.ingredient.IIngredient;
import mybar.app.RestBeanConverter;
import mybar.app.bean.bar.ingredient.GroupedIngredientsBean;
import mybar.service.bar.IngredientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping(value = "/ingredients")
public class IngredientsController {

    private final IngredientService ingredientService;

    @Autowired
    public IngredientsController(IngredientService ingredientService) {
        this.ingredientService = ingredientService;
    }

    //-------------------Retrieve Ingredients--------------------------------------------------------

    @GetMapping
    public ResponseEntity<Map<String, GroupedIngredientsBean>> listIngredients(
            @RequestParam(value = "filter", required = false) String groupNameParam) {

        if (!StringUtils.hasText(groupNameParam)) {
            List<IIngredient> allIngredients = ingredientService.findAll();
            return new ResponseEntity<>(toMapByGroup(allIngredients), HttpStatus.OK);
        }

        List<IIngredient> ingredientsByGroupName = ingredientService.findByGroupName(groupNameParam);
        Map<String, GroupedIngredientsBean> groupedIngredientsBean = toMapByGroup(groupNameParam, ingredientsByGroupName);
        return new ResponseEntity<>(groupedIngredientsBean, HttpStatus.OK);
    }

    private static Map<String, GroupedIngredientsBean> toMapByGroup(List<IIngredient> ingredients) {

        List<IBeverage> beverages = filter(ingredients, IBeverage.class);
        List<IDrink> drinks = filter(ingredients, IDrink.class);
        List<IAdditive> additives = filter(ingredients, IAdditive.class);

        Map<String, GroupedIngredientsBean> map = new HashMap<>();
        putIfPresent(map, IBeverage.GROUP_NAME, true, beverages);
        putIfPresent(map, IDrink.GROUP_NAME, true, drinks);
        putIfPresent(map, IAdditive.GROUP_NAME, false, additives);

        return map;
    }

    private static Map<String, GroupedIngredientsBean> toMapByGroup(String groupNameParam, List<IIngredient> ingredients) {

        Map<String, GroupedIngredientsBean> map = new HashMap<>();

        switch (groupNameParam) {
            case IBeverage.GROUP_NAME: {
                List<IBeverage> beverages = filter(ingredients, IBeverage.class);
                putIfPresent(map, IBeverage.GROUP_NAME, true, beverages);
                break;
            }
            case IDrink.GROUP_NAME: {
                List<IDrink> drinks = filter(ingredients, IDrink.class);
                putIfPresent(map, IDrink.GROUP_NAME, true, drinks);
                break;
            }
            case IAdditive.GROUP_NAME: {
                List<IAdditive> additives = filter(ingredients, IAdditive.class);
                putIfPresent(map, IAdditive.GROUP_NAME, false, additives);
                break;
            }
        }

        return map;
    }

    private static <T extends IIngredient> List<T> filter(List<IIngredient> ingredients, Class<T> aClass) {
        return ingredients
                .stream()
                .filter(aClass::isInstance)
                .map(aClass::cast)
                .collect(Collectors.toList());
    }

    private static void putIfPresent(Map<String, GroupedIngredientsBean> builder,
                                     String groupName, boolean isLiquid, List<? extends IIngredient> filtered) {
        if (!filtered.iterator().hasNext()) {
            builder.put(groupName, GroupedIngredientsBean.of(Collections.emptyList(), Collections.emptyList(), null));
            return;
        }
        List<IIngredient> items = filtered
                .stream()
                .map(IngredientsController::toRestEntity)
                .collect(Collectors.toList());

        builder.put(groupName, GroupedIngredientsBean.of(isLiquid ? Measurement.liquids() : Measurement.solidComponents(), items, isLiquid));
    }

    private static <T> IIngredient toRestEntity(T input) {
        if (input instanceof IAdditive) {
            return RestBeanConverter.from((IAdditive) input);
        } else if (input instanceof IDrink) {
            return RestBeanConverter.from((IDrink) input);
        } else if (input instanceof IBeverage) {
            return RestBeanConverter.from((IBeverage) input);
        }
        return null;
    }

}