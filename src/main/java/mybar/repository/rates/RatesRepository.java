package mybar.repository.rates;

import mybar.domain.History;
import mybar.domain.bar.Cocktail;
import mybar.domain.rates.CocktailToUserPk;
import mybar.domain.rates.Rate;
import mybar.domain.users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.persistence.Tuple;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RatesRepository extends JpaRepository<Rate, CocktailToUserPk> {

    @Query("SELECT r FROM Rate r WHERE r.pk.cocktail.id = :cocktailId")
    boolean checkRateExistsForCocktail(String cocktailId);

    @Query("SELECT r FROM Rate r WHERE r.pk.cocktail = :cocktail")
    List<Rate> findAllRatesForCocktail(Cocktail cocktail);

    @Query("SELECT r FROM Rate r WHERE r.pk.user = :user")
    List<Rate> findAllRatesForUser(User user);

    @Query("SELECT new mybar.domain.History(c.name, r.stars, r.pk.user.username) FROM Cocktail c, Rate r WHERE r.pk.cocktail.id = c.id AND r.ratedAt >= :startDate AND r.ratedAt <= :endDate order by r.ratedAt desc")
    List<History> getRatedCocktailsForPeriod(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT r.pk.cocktail.id as cocktail_id, avg (r.stars) as avg_stars FROM Rate r group by r.pk.cocktail.id")
    List<Tuple> findAllAverageRates();

    @Query("SELECT r FROM Rate r WHERE r.pk.user.username = :userId and r.pk.cocktail.id = :cocktailId")
    Rate findBy(String userId, String cocktailId);

}