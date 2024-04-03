package mybar.service.bar;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import mybar.api.bar.ICocktail;
import mybar.api.bar.ICocktailIngredient;
import mybar.api.bar.IMenu;
import mybar.domain.EntityFactory;
import mybar.domain.bar.Cocktail;
import mybar.domain.bar.Menu;
import mybar.domain.bar.ingredient.Ingredient;
import mybar.exception.CocktailNotFoundException;
import mybar.exception.UniqueCocktailNameException;
import mybar.exception.UnknownIngredientsException;
import mybar.exception.UnknownMenuException;
import mybar.repository.bar.CocktailsRepository;
import mybar.repository.bar.IngredientRepository;
import mybar.repository.bar.MenuRepository;
import mybar.repository.rates.RatesRepository;
import mybar.utils.Preconditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.function.SupplierUtils;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class CocktailsService {

    private final MenuRepository menuRepository;

    private final CocktailsRepository cocktailsRepository;

    private final IngredientRepository ingredientRepository;

    private final RatesRepository ratesRepository;

    private Supplier<List<IMenu>> allMenusCached;

    private final Cache<String, List<ICocktail>> cocktailsCache = Caffeine.newBuilder()
            .expireAfterAccess(30, TimeUnit.MINUTES)
            .maximumSize(100)
            .build();

    @Autowired
    public CocktailsService(MenuRepository menuRepository, CocktailsRepository cocktailsRepository, IngredientRepository ingredientRepository, RatesRepository ratesRepository) {
        this.menuRepository = menuRepository;
        this.cocktailsRepository = cocktailsRepository;
        this.ingredientRepository = ingredientRepository;
        this.ratesRepository = ratesRepository;
    }

    @PostConstruct
    public void initAllIngredients() {
        log.info("Post construct [menus] cache");
        List<IMenu> menus = this.loadAllMenus();
        allMenusCached = () -> menus;
    }

    // menu

    public Collection<IMenu> getAllMenuItems() {
        return SupplierUtils.resolve(allMenusCached);
    }

    private List<IMenu> loadAllMenus() {
        List<Menu> all = menuRepository.findAll();
        return all
                .stream()
                .map(Menu::toDto)
                .collect(Collectors.toList());
    }

    // cocktails

    private void ensureAllCocktailsLoaded() {
        if (cocktailsCache.estimatedSize() == 0) {
            List<Menu> all = menuRepository.findAll();
            for (final Menu menu : all) {
                final String menuName = menu.getName();
                cocktailsCache.put(menuName, cocktailsToDtoList(menu, menuName));
            }
        }
    }

    private static List<ICocktail> cocktailsToDtoList(Menu menu, final String menuName) {
        return menu.getCocktails()
                .stream()
                .map(cocktail -> cocktail.toDto(menuName))
                .collect(Collectors.toList());
    }

    private IMenu findMenuById(final int menuId) {
        return allMenusCached.get()
                .stream()
                .filter(menu -> menu.getId() == menuId)
                .findFirst()
                .orElseThrow(() -> new UnknownMenuException(String.valueOf(menuId)));
    }

    private IMenu findMenuByName(final String menuName) {
        return allMenusCached.get()
                .stream()
                .filter(menu -> Objects.equals(menu.getName(), menuName))
                .findFirst()
                .orElseThrow(() -> new UnknownMenuException(menuName));
    }

    // cocktails per menu

    public List<ICocktail> getAllCocktailsForMenu(final String menuName) {

        if (cocktailsCache.asMap().containsKey(menuName)) {
            return cocktailsCache.asMap().get(menuName);
        }
        IMenu menu = findMenuByName(menuName);
        List<ICocktail> cocktailDtoList = cocktailsToDtoList(menuRepository.getOne(menu.getId()), menuName);
        cocktailsCache.put(menuName, cocktailDtoList);
        return cocktailDtoList;
    }

    // all cocktails per menu

    public Map<String, List<ICocktail>> getAllCocktails() {
        ensureAllCocktailsLoaded();
        return Collections.unmodifiableMap(cocktailsCache.asMap());
    }

    public ICocktail saveCocktail(ICocktail cocktail) throws UniqueCocktailNameException {
        Preconditions.checkArgument(StringUtils.hasText(cocktail.getName()), "Cocktail name is required.");
        Preconditions.checkArgument(StringUtils.hasText(cocktail.getMenuName()), "Menu name is required.");
        checkCocktailExists(cocktail.getName());

        return performSaveOrUpdate(cocktail);
    }

    public ICocktail updateCocktail(ICocktail cocktail) throws CocktailNotFoundException {
        Preconditions.checkArgument(StringUtils.hasText(cocktail.getId()), "Cocktail id is required.");
        Preconditions.checkArgument(StringUtils.hasText(cocktail.getName()), "Cocktail name is required.");
        Preconditions.checkArgument(StringUtils.hasText(cocktail.getMenuName()), "Menu name is required.");

        return performSaveOrUpdate(cocktail);
    }

    private ICocktail performSaveOrUpdate(ICocktail cocktail) {
        String menuName = cocktail.getMenuName();
        IMenu menu = findMenuByName(menuName);
        checkIngredientsExist(cocktail.getIngredients());

        Cocktail cocktailEntity = EntityFactory.from(cocktail, menu.getId());
        try {
            Cocktail result = cocktailsRepository.save(cocktailEntity);
            return result.toDto(menuName);
        } finally {
            cocktailsCache.invalidateAll();
        }
    }

    private void checkIngredientsExist(Map<String, Collection<ICocktailIngredient>> ingredients) {
        List<ICocktailIngredient> newCombinedList = new ArrayList<>();
        for (Collection<ICocktailIngredient> cocktailIngredients : ingredients.values()) {
            newCombinedList.addAll(cocktailIngredients);
        }

        List<Integer> asInputIds = newCombinedList.stream()
                .map(ICocktailIngredient::getIngredientId)
                .collect(Collectors.toList());

        List<Integer> existedIngredientIds = getExistedIngredientIds(asInputIds);
        if (asInputIds.size() != existedIngredientIds.size()) {
            List<Integer> copy = new ArrayList<>(asInputIds);
            copy.removeAll(existedIngredientIds);
            throw new UnknownIngredientsException(copy);
        }
    }

    private List<Integer> getExistedIngredientIds(List<Integer> asIds) {
        List<Ingredient> ingredients = ingredientRepository.findIn(asIds);
        return ingredients
                .stream()
                .map(Ingredient::getId)
                .collect(Collectors.toList());
    }

    private void checkCocktailExists(String name) throws UniqueCocktailNameException {
        if (cocktailsRepository.existsByName(name)) {
            throw new UniqueCocktailNameException(name);
        }
    }

    public void removeCocktail(ICocktail cocktail) throws Exception {
        boolean hasRef = isCocktailInHistory(cocktail);
        if (hasRef) {
            Cocktail entity = EntityFactory.from(cocktail, -1); // TODO: 1/12/2018
            cocktailsRepository.save(entity);
        } else {
            cocktailsRepository.deleteById(cocktail.getId());
        }
    }

    public boolean isCocktailExist(ICocktail cocktail) {
        try {
            checkCocktailExists(cocktail.getName());
        } catch (UniqueCocktailNameException e) {
            return true;
        }
        return false;
    }

    public ICocktail findCocktailById(String id) throws CocktailNotFoundException {
        Preconditions.checkArgument(StringUtils.hasText(id), "Cocktail id is required.");
        Optional<Cocktail> cocktail = cocktailsRepository.findById(id);
        return cocktail
                .map(c -> c.toDto(findMenuById(c.getMenuId()).getName()))
                .orElseThrow(() -> new CocktailNotFoundException(id));
    }

    public boolean isCocktailInHistory(ICocktail cocktail) {
        return ratesRepository.checkRateExistsForCocktail(cocktail.getId());
    }

    public void deleteCocktailById(String id) throws CocktailNotFoundException {
        Preconditions.checkArgument(StringUtils.hasText(id), "Cocktail id is required.");
        cocktailsRepository.deleteById(id);
        if (cocktailsCache.estimatedSize() == 0) {
            return;
        }
        for (String menuName : cocktailsCache.asMap().keySet()) {
            List<ICocktail> cocktails = cocktailsCache.getIfPresent(menuName);
            if (CollectionUtils.isEmpty(cocktails)) {
                continue;
            }
            Iterator<ICocktail> iterator = cocktails.iterator();
            while (iterator.hasNext()) {
                ICocktail next = iterator.next();
                if (Objects.equals(next.getId(), id)) {
                    iterator.remove();
                    break;
                }
            }
        }
    }

}