package mybar.domain.bar.ingredient;

import lombok.Getter;
import lombok.Setter;
import mybar.api.bar.ingredient.BeverageType;
import mybar.api.bar.ingredient.IBeverage;
import mybar.domain.bar.Bottle;

import jakarta.persistence.*;
import java.util.List;

@Getter
@Setter
@Entity
@DiscriminatorValue(value = "Beverage")
@NamedQuery(
        name = "Beverage.findBeverageById",
        query = "SELECT b FROM Beverage b WHERE TYPE(b) = :type and b.id = :id"
)
public class Beverage extends Ingredient implements IBeverage {

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private BeverageType beverageType;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "beverage", fetch = FetchType.LAZY)
    private List<Bottle> bottles;

    public Beverage() {
    }

    public Beverage(int beverageId) {
        setId(beverageId);
    }

}