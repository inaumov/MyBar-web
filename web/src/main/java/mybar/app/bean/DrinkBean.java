package mybar.app.bean;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import mybar.ActiveStatus;
import mybar.api.IBasis;
import mybar.api.IDrink;

import java.sql.Blob;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DrinkBean implements IDrink {

    @JsonView(View.Drink.class)
    private int id;

    @JsonView(View.Drink.class)
    private String name;

    private MenuBean menu;

    @JsonView(View.Drink.class)
    private double price;

    private ActiveStatus activeStatus;

    private Blob picture;

    @JsonView(View.DrinkWithDetails.class)
    @JsonProperty("ingredients")
    private Collection<BasisBean> basisList;

    @JsonView(View.DrinkWithDetails.class)
    private String description;

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Collection<BasisBean> getBasisList() {
        return basisList;
    }

    public void setBasisList(Collection<BasisBean> basisList) {
        this.basisList = basisList;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public MenuBean getMenu() {
        return menu;
    }

    public void setMenu(MenuBean menu) {
        this.menu = menu;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public ActiveStatus getActiveStatus() {
        return activeStatus;
    }

    public void setActiveStatus(ActiveStatus activeStatus) {
        this.activeStatus = activeStatus;
    }

    @Override
    public Blob getPicture() {
        return picture;
    }

    public void setPicture(Blob picture) {
        this.picture = picture;
    }

    public static DrinkBean from(IDrink drink) {
        DrinkBean bean = new DrinkBean();
        bean.setId(drink.getId());
        bean.setName(drink.getName());
        bean.setPrice(drink.getPrice());
        bean.setDescription(drink.getDescription());
        List<BasisBean> basisBeans = new ArrayList<>();
        for (IBasis basis : drink.getBasisList()) {
            basisBeans.add(BasisBean.from(basis));
        }
        bean.setBasisList(basisBeans);
        bean.setActiveStatus(drink.getActiveStatus());

        return bean;
    }

}