package mybar.domain.rates;

import lombok.Getter;
import lombok.Setter;
import mybar.domain.bar.Cocktail;
import mybar.domain.users.User;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "rates")
@AssociationOverrides({
        @AssociationOverride(name = "pk.cocktail", joinColumns = @JoinColumn(name = "cocktail_id")),
        @AssociationOverride(name = "pk.user", joinColumns = @JoinColumn(name = "username"))
})
public class Rate {

    @Getter
    @Setter
    @EmbeddedId
    private CocktailToUserPk pk = new CocktailToUserPk();

    @Column(name = "rated_at")
    private LocalDateTime ratedAt;

    @Column(name = "stars")
    private int stars;

    @Transient
    public Cocktail getCocktail() {
        return getPk().getCocktail();
    }

    public void setCocktail(Cocktail cocktail) {
        getPk().setCocktail(cocktail);
    }

    @Transient
    public User getUser() {
        return getPk().getUser();
    }

    public void setUser(User user) {
        getPk().setUser(user);
    }

}