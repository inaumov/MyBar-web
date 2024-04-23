package mybar.domain.bar.ingredient;

import mybar.api.bar.ingredient.IAdditive;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(value = "Additive")
public class Additive extends Ingredient implements IAdditive {

}