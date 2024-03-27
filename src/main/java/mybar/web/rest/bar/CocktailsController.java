package mybar.web.rest.bar;

import com.fasterxml.jackson.annotation.JsonView;
import mybar.common.providers.availability.IAvailabilityCalculator;
import lombok.extern.slf4j.Slf4j;
import mybar.api.bar.ICocktail;
import mybar.app.RestBeanConverter;
import mybar.app.bean.bar.CocktailBean;
import mybar.app.bean.bar.View;
import mybar.service.bar.CocktailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/cocktails")
@Slf4j
public class CocktailsController {

    private final CocktailsService cocktailsService;
    private final IAvailabilityCalculator<CocktailBean> availabilityCalculator;

    @Autowired
    public CocktailsController(CocktailsService cocktailsService, @Qualifier("CocktailAvailabilityCalculator") IAvailabilityCalculator<CocktailBean> availabilityCalculator) {
        this.cocktailsService = cocktailsService;
        this.availabilityCalculator = availabilityCalculator;
    }

    //-------------------Retrieve All Cocktails--------------------------------------------------------

    @JsonView(View.Cocktail.class)
    @GetMapping
    public ResponseEntity<Map<String, List<CocktailBean>>> allCocktails(@RequestParam(value = "filter", required = false) String menuNameParam) {
        if (StringUtils.hasText(menuNameParam)) {
            Map<String, List<CocktailBean>> cocktailsForMenu = findCocktailsForMenu(menuNameParam);
            return new ResponseEntity<>(cocktailsForMenu, HttpStatus.OK);
        }
        Map<String, List<ICocktail>> cocktailsMap = cocktailsService.getAllCocktails();
        if (cocktailsMap.isEmpty()) {
            log.info("Cocktail list is empty.");
            return new ResponseEntity<>(Collections.emptyMap(), HttpStatus.OK);
        }
        Map<String, List<CocktailBean>> responseMap = convertAndCalculateAvailability(cocktailsMap);
        return new ResponseEntity<>(responseMap, HttpStatus.OK);
    }

    private Map<String, List<CocktailBean>> findCocktailsForMenu(String menuName) {
        log.info("Fetching cocktails for menu = {}...", menuName);
        List<ICocktail> cocktailsList = cocktailsService.getAllCocktailsForMenu(menuName);
        if (cocktailsList.isEmpty()) {
            log.info("Cocktails list for menu = {} is empty.", menuName);
            return Collections.emptyMap();
        }

        List<CocktailBean> converted = convertAndCalculateAvailability(cocktailsList);
        log.info("Found {} cocktails for menu = {}", converted.size(), menuName);
        return Collections.singletonMap(menuName, converted);
    }

    private Map<String, List<CocktailBean>> convertAndCalculateAvailability(Map<String, List<ICocktail>> cocktailsMap) {
        Map<String, List<CocktailBean>> newConvertedMap = new HashMap<>();
        for (String menuName : cocktailsMap.keySet()) {
            List<ICocktail> cocktails = cocktailsMap.get(menuName);
            List<CocktailBean> converted = convertAndCalculateAvailability(cocktails);
            newConvertedMap.put(menuName, converted);
            log.info("Found {} cocktails for menu {}", cocktails.size(), menuName);
        }
        return newConvertedMap;
    }

    private List<CocktailBean> convertAndCalculateAvailability(List<ICocktail> cocktails) {
        return cocktails
                .stream()
                .map(RestBeanConverter::toCocktailBean)
                .peek(availabilityCalculator::doUpdate)
                .collect(Collectors.toList());
    }

    //-------------------Retrieve a cocktail with details--------------------------------------------------------

    @JsonView(View.CocktailWithDetails.class)
    @GetMapping("/{id}")
    public ResponseEntity<CocktailBean> getCocktail(@PathVariable("id") String id) {
        log.info("Fetching cocktail with id = {}", id);

        ICocktail cocktail = cocktailsService.findCocktailById(id);
        CocktailBean cocktailBeanResponse = RestBeanConverter.toCocktailBean(cocktail);

        return new ResponseEntity<>(cocktailBeanResponse, HttpStatus.OK);
    }

    //-------------------Create a Cocktail--------------------------------------------------------

    @PostMapping
    public ResponseEntity<CocktailBean> addCocktail(@RequestBody CocktailBean cocktailBean, UriComponentsBuilder ucBuilder) {
        log.info("Creating a new cocktail = {}", cocktailBean);

        ICocktail saved = cocktailsService.saveCocktail(cocktailBean);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(ucBuilder.path("/cocktails/{id}").buildAndExpand(cocktailBean.getId()).toUri());
        CocktailBean cocktailBeanResponse = RestBeanConverter.toCocktailBean(saved);

        return new ResponseEntity<>(cocktailBeanResponse, headers, HttpStatus.CREATED);
    }

    //------------------- Update a Cocktail --------------------------------------------------------

    @PutMapping
    public ResponseEntity<CocktailBean> updateCocktail(@RequestBody CocktailBean cocktailBean) {
        log.info("Updating a cocktail = {}", cocktailBean);

        ICocktail updated = cocktailsService.updateCocktail(cocktailBean);
        CocktailBean cocktailBeanResponse = RestBeanConverter.toCocktailBean(updated);

        return new ResponseEntity<>(cocktailBeanResponse, HttpStatus.ACCEPTED);
    }

    //------------------- Delete a Cocktail --------------------------------------------------------

    @DeleteMapping("/{id}")
    public ResponseEntity<CocktailBean> deleteCocktail(@PathVariable("id") String id) {
        log.info("Deleting a cocktail with id = {}", id);

        cocktailsService.deleteCocktailById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}