package mybar.domain.rates;

import lombok.Getter;
import lombok.Setter;
import mybar.domain.bar.Cocktail;
import mybar.domain.users.User;

import jakarta.persistence.Embeddable;
import jakarta.persistence.ManyToOne;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class CocktailToUserPk implements Serializable {

    @Getter
    @Setter
    @ManyToOne
    private Cocktail cocktail;

    @Getter
    @Setter
    @ManyToOne
    private User user;

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CocktailToUserPk that = (CocktailToUserPk) o;

        return Objects.equals(cocktail, that.cocktail)
                && Objects.equals(user, that.user);
    }

    public int hashCode() {
        int result;
        result = (cocktail != null ? cocktail.hashCode() : 0);
        result = 31 * result + (user != null ? user.hashCode() : 0);
        return result;
    }

}