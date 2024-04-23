package mybar.domain.bar.ingredient;

import lombok.Getter;
import lombok.Setter;
import mybar.api.bar.ingredient.DrinkType;
import mybar.api.bar.ingredient.IDrink;

import jakarta.persistence.*;

@Getter
@Setter
@Entity
@DiscriminatorValue(value = "Drink")
public class Drink extends Ingredient implements IDrink {

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private DrinkType drinkType;

}