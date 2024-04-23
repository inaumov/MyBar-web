package mybar.domain.bar;

import lombok.Getter;
import lombok.Setter;
import mybar.domain.bar.ingredient.Beverage;
import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "bottles")
@GenericGenerator(name = "bottle_id", strategy = "mybar.domain.EntityIdGenerator")
public class Bottle {

    @Id
    @GeneratedValue(generator = "bottle_id")
    @Column(name = "id")
    private String id;

    @ManyToOne
    @JoinColumn(name = "ingredient_id", nullable = false)
    public Beverage beverage;

    @Column(name = "brand_name")
    private String brandName;

    @Column(name = "volume")
    private double volume;

    @Column(name = "price", scale = 2)
    private BigDecimal price;

    @Column(name = "in_shelf", columnDefinition = "boolean default false")
    private boolean inShelf;

    @Column(name = "image_url")
    private String imageUrl;

}