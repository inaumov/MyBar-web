package mybar.repository.bar;

import mybar.domain.bar.Cocktail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CocktailsRepository extends JpaRepository<Cocktail, String> {

    List<Cocktail> findByMenuId(int menuId);

    boolean existsByName(String cocktailName);

}