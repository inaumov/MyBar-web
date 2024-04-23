package mybar.domain.bar;

import lombok.Getter;
import lombok.Setter;
import mybar.api.bar.ICocktail;
import mybar.dto.DtoFactory;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;
import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "cocktails")
@GenericGenerator(name = "cocktail_id", strategy = "mybar.domain.EntityIdGenerator")
public class Cocktail {

    @Id
    @GeneratedValue(generator = "cocktail_id")
    @Column(name = "id")
    private String id;

    @Column(name = "name")
    private String name;

    /**
     * Here is the annotation to add in order to
     * Hibernate to automatically insert and update
     * CocktailToIngredientList (if any)
     */
    @OneToMany(mappedBy = "pk.cocktail", fetch = FetchType.LAZY,
            orphanRemoval = true, cascade = {CascadeType.PERSIST, CascadeType.MERGE}
    )
    @Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
    private List<CocktailToIngredient> cocktailToIngredientList = new LinkedList<>();

    @Column(name = "menu_id")
    private int menuId;

    @Column(name = "description")
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    public void addCocktailToIngredient(CocktailToIngredient cocktailToIngredient) {
        if (!getCocktailToIngredientList().contains(cocktailToIngredient)) {
            getCocktailToIngredientList().add(cocktailToIngredient);
            cocktailToIngredient.setCocktail(this);
        }
    }

    public ICocktail toDto(String menuName) {
        return DtoFactory.toDto(this, menuName);
    }

}