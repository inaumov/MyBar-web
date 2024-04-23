package mybar.domain.bar.ingredient;

import lombok.Getter;
import lombok.Setter;
import mybar.api.bar.ingredient.IIngredient;

import jakarta.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "ingredients")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "group_name")
@NamedQueries({
        @NamedQuery(
                name = "Ingredient.findAll",
                query = "SELECT i FROM Ingredient i order by i.groupName, i.kind"
        ),
        @NamedQuery(
                name = "Ingredient.findIn",
                query = "SELECT i FROM Ingredient i where i.id in :ids"
        ),
        @NamedQuery(
                name = "Ingredient.findByGroupName",
                query = "SELECT i FROM Ingredient i WHERE TYPE(i) = :type order by i.kind"
        )
})
public class Ingredient implements IIngredient {

    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "kind")
    private String kind;

    @Column(name = "group_name", insertable = false, updatable = false)
    private String groupName;

}