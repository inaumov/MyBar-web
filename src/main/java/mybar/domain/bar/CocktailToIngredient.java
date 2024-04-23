package mybar.domain.bar;

import lombok.Getter;
import lombok.Setter;
import mybar.api.bar.Measurement;
import mybar.domain.bar.ingredient.Beverage;
import mybar.domain.bar.ingredient.Drink;
import mybar.domain.bar.ingredient.Ingredient;

import jakarta.persistence.*;

@Entity
@Table(name = "cocktails_to_ingredients")
@AssociationOverrides({
        @AssociationOverride(name = "pk.cocktail", joinColumns = @JoinColumn(name = "cocktail_id")),
        @AssociationOverride(name = "pk.ingredient", joinColumns = @JoinColumn(name = "ingredient_id"))
})
public class CocktailToIngredient {

    @Getter
    @Setter
    @EmbeddedId
    private CocktailToIngredientPk pk = new CocktailToIngredientPk();

    @Getter
    @Setter
    @Column(name = "volume")
    private double volume;

    @Getter
    @Setter
    @Column(name = "measurement")
    @Enumerated(EnumType.STRING)
    private Measurement measurement;

    @PrePersist
    @PreUpdate
    public void setDefaults() {
        if (measurement == null) {
            boolean isLiquid = pk.getIngredient() instanceof Beverage || pk.getIngredient() instanceof Drink;
            measurement = isLiquid ? Measurement.ML : Measurement.PCS;
        }
    }

    @Transient
    public Cocktail getCocktail() {
        return getPk().getCocktail();
    }

    public void setCocktail(Cocktail cocktail) {
        getPk().setCocktail(cocktail);
    }

    @Transient
    public Ingredient getIngredient() {
        return getPk().getIngredient();
    }

    public void setIngredient(Ingredient ingredient) {
        getPk().setIngredient(ingredient);
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CocktailToIngredient that = (CocktailToIngredient) o;

        return getPk() != null ? getPk().equals(that.getPk()) : that.getPk() == null;
    }

    public int hashCode() {
        return (getPk() != null ? getPk().hashCode() : 0);
    }

}