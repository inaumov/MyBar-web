package mybar.service.rates;

import lombok.extern.slf4j.Slf4j;
import mybar.api.rates.IRate;
import mybar.domain.bar.Cocktail;
import mybar.domain.rates.Rate;
import mybar.domain.users.User;
import mybar.dto.RateDto;
import mybar.exception.CocktailNotFoundException;
import mybar.repository.bar.CocktailsRepository;
import mybar.repository.rates.RatesRepository;
import mybar.repository.users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.Tuple;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RatesService {

    private final RatesRepository ratesRepository;

    private final UserRepository userRepository;

    private final CocktailsRepository cocktailsRepository;

    @Autowired
    public RatesService(RatesRepository ratesRepository, UserRepository userRepository, CocktailsRepository cocktailsRepository) {
        this.ratesRepository = ratesRepository;
        this.userRepository = userRepository;
        this.cocktailsRepository = cocktailsRepository;
    }

    public void removeCocktailFromRates(String userId, String cocktailId) {
        Rate rate = ratesRepository.findBy(userId, cocktailId);
        ratesRepository.delete(rate);
    }

    public Collection<IRate> getRatedCocktails(String userId) {
        List<IRate> userRates = new ArrayList<>();
        User user = userRepository.getReferenceById(userId);
        List<Rate> allRatesForUser = ratesRepository.findAllRatesForUser(user);
        for (Rate rateEntity : allRatesForUser) {
            RateDto rateDto = new RateDto();
            rateDto.setCocktailId(rateEntity.getCocktail().getId());
            rateDto.setStars(rateEntity.getStars());

            rateDto.setRatedAt(rateEntity.getRatedAt()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime());
            userRates.add(rateDto);
        }
        return Collections.unmodifiableCollection(userRates);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void persistRate(String userId, IRate iRate) {

        Cocktail cocktail = cocktailsRepository.findById(iRate.getCocktailId())
                .orElseThrow(() -> new CocktailNotFoundException(iRate.getCocktailId()));

        Rate rate = new Rate();
        rate.setCocktail(cocktail);
        rate.setStars(iRate.getStars());

        rate.setRatedAt(iRate.getRatedAt());
        User user = userRepository.getReferenceById(userId);
        rate.setUser(user);
        ratesRepository.save(rate);
    }

    void checkCocktailExists(String cocktailId) {
        cocktailsRepository.findById(cocktailId)
                .orElseThrow(() -> new CocktailNotFoundException(cocktailId));
    }

    public Map<String, BigDecimal> findAllAverageRates() {
        List<Tuple> allAverageRates = ratesRepository.findAllAverageRates();
        return allAverageRates.stream()
                .collect(Collectors.toMap(x -> x.get("cocktail_id", String.class), x -> BigDecimal.valueOf(x.get("avg_stars", Double.class))));
    }

}