package mybar.app.bean.bar;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.google.common.base.MoreObjects;
import mybar.api.bar.IBottle;
import mybar.app.bean.bar.ingredient.BeverageBean;

public class BottleBean implements IBottle {

    @JsonView(View.Shelf.class)
    private int id;

    @JsonView(View.Shelf.class)
    @JsonProperty("ingredient")
    private BeverageBean beverage;

    @JsonView(View.Shelf.class)
    private String brandName;

    @JsonView(View.Shelf.class)
    private double volume;

    @JsonView(View.Shelf.class)
    private double price;

    @JsonView(View.Shelf.class)
    private boolean inShelf;

    @JsonView(View.Shelf.class)
    private String imageUrl;

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public BeverageBean getBeverage() {
        return beverage;
    }

    public void setBeverage(BeverageBean beverage) {
        this.beverage = beverage;
    }

    @Override
    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    @Override
    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    @Override
    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public boolean isInShelf() {
        return inShelf;
    }

    public void setInShelf(boolean inShelf) {
        this.inShelf = inShelf;
    }

    @Override
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass())
                .add("beverage", beverage)
                .add("brandName", brandName)
                .add("volume", volume)
                .add("price", price)
                .toString();
    }

}