package mybar.web.rest.rates;

import lombok.extern.slf4j.Slf4j;
import mybar.api.rates.IRate;
import mybar.app.bean.rates.RateBean;
import mybar.service.rates.RatesEventService;
import mybar.service.rates.RatesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/rates")
public class RatesController {

    private final RatesService ratesService;
    private final RatesEventService ratesEventService;

    @Autowired
    public RatesController(RatesService ratesService, RatesEventService ratesEventService) {
        this.ratesService = ratesService;
        this.ratesEventService = ratesEventService;
    }

    @GetMapping("/average")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, BigDecimal> findAllAverageRates() {
        return ratesService.findAllAverageRates();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RateBean rate(@RequestBody RateBean rateBean) {
        String principal = getPrincipalName();
        IRate rate = ratesEventService.rateCocktail(principal, rateBean.getCocktailId(), rateBean.getStars());
        return new RateBean(rate);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public RateBean update(@RequestBody RateBean rateBean) {
        String principal = getPrincipalName();
        IRate rate = ratesEventService.rateCocktail(principal, rateBean.getCocktailId(), rateBean.getStars());
        return new RateBean(rate);
    }

    @DeleteMapping("/{cocktailId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable String cocktailId) {
        ratesService.removeCocktailFromRates(getPrincipalName(), cocktailId);
    }

    @GetMapping("/ratedCocktails")
    @ResponseStatus(HttpStatus.OK)
    public List<RateBean> getRates() {
        Collection<IRate> ratedCocktails = ratesService.getRatedCocktails(getPrincipalName());
        return ratedCocktails
                .stream()
                .map(RateBean::new)
                .collect(Collectors.toList());
    }

    private String getPrincipalName() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

}