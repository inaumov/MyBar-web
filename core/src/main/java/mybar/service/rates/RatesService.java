package mybar.service.rates;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import mybar.api.rates.IRate;
import mybar.domain.bar.Cocktail;
import mybar.domain.rates.Rate;
import mybar.domain.users.User;
import mybar.dto.RateDto;
import mybar.exception.CocktailNotFoundException;
import mybar.messaging.IMessageProducer;
import mybar.repository.bar.CocktailDao;
import mybar.repository.rates.RatesDao;
import mybar.repository.users.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Range;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

@Slf4j
@Service
public class RatesService {

    @Autowired(required = false)
    private RatesDao ratesDao;

    @Autowired(required = false)
    private UserDao userDao;

    @Autowired(required = false)
    private CocktailDao cocktailDao;

    private static final Range<Integer> starsRange = new Range<>(1, 10);

    private IMessageProducer messageProducer;

    @Autowired
    public RatesService(IMessageProducer messageProducer) {
        this.messageProducer = messageProducer;
    }

    public IRate rateCocktail(String username, String cocktailId, Integer stars) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(username), "Username is required.");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(cocktailId), "Cocktail id is required.");
        Preconditions.checkArgument(stars != null && starsRange.contains(stars), "Stars number should be from 1 to 10.");
        checkCocktailExists(cocktailId);

        RateDto rateDto = new RateDto();
        rateDto.setCocktailId(cocktailId);
        rateDto.setRatedAt(new Date());
        rateDto.setStars(stars);
        Gson gson = new Gson();
        messageProducer.send(toCacheKey(username, cocktailId), gson.toJson(rateDto));
        return rateDto;
    }

    public void removeCocktailFromRates(String userId, String cocktailId) {
        Rate rate = ratesDao.findBy(userId, cocktailId);
        ratesDao.delete(rate);
    }

    private String toCacheKey(String userId, String cocktailId) {
        return userId + "@" + cocktailId;
    }

    public Collection<IRate> getRatedCocktails(String userId) {
        List<IRate> userRates = new ArrayList<>();
        List<Rate> allRatesForUser = ratesDao.findAllRatesForUser(userDao.findOne(userId));
        for (Rate rate : allRatesForUser) {
            RateDto rateDto = new RateDto();
            rateDto.setCocktailId(rate.getCocktail().getId());
            rateDto.setRatedAt(rate.getRatedAt());
            rateDto.setStars(rate.getStars());
            userRates.add(rateDto);
        }
        return Collections.unmodifiableCollection(userRates);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void persistRates(String cacheKey, String object) {
        Gson gson = new Gson();
        IRate rateDto = gson.fromJson(object, RateDto.class);
        String[] strings = StringUtils.split(cacheKey, "@");
        User user = userDao.findOne(strings[0]);
        Cocktail cocktail = cocktailDao.read(strings[1]);
        if (user != null && cocktail != null) {
            Rate rate = new Rate();
            rate.setCocktail(cocktail);
            rate.setStars(rateDto.getStars());
            rate.setRatedAt(rateDto.getRatedAt());
            rate.setUser(user);
            ratesDao.update(rate);
        } else {
            log.error("Could not persist rate for [{}]. It is either user or cocktail is unknown.", cacheKey);
        }
    }

    private void checkCocktailExists(String cocktailId) {
        Cocktail cocktail = cocktailDao.read(cocktailId);
        if (cocktail == null) {
            throw new CocktailNotFoundException(cocktailId);
        }
    }

    public Map<String, Double> findAllAverageRates() {
        return ratesDao.findAllAverageRates();
    }

}