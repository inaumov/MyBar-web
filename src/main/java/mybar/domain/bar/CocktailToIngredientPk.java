package mybar.domain.bar;

import lombok.Getter;
import lombok.Setter;
import mybar.domain.bar.ingredient.Ingredient;

import jakarta.persistence.Embeddable;
import jakarta.persistence.ManyToOne;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class CocktailToIngredientPk implements Serializable {

    @Getter
    @Setter
    @ManyToOne
    private Cocktail cocktail;

    @Getter
    @Setter
    @ManyToOne
    private Ingredient ingredient;

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CocktailToIngredientPk that = (CocktailToIngredientPk) o;

        return Objects.equals(cocktail, that.cocktail)
                && Objects.equals(ingredient, that.ingredient);
    }

    public int hashCode() {
        int result;
        result = (cocktail != null ? cocktail.hashCode() : 0);
        result = 31 * result + (ingredient != null ? ingredient.hashCode() : 0);
        return result;
    }

}