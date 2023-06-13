package Soodustused.model;

import jakarta.persistence.*;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "Product")
public class Product {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    private Double origPrice;

    private Double price;

    private String url;

    private String image;

    @ManyToOne
    @JoinColumn(name="shop_id")
    private Shop shop;


    public Product() {

    }

    public Product(String name, Double origPrice, Double price, String url, String image) {
        this.name = name;
        this.origPrice = origPrice;
        this.price = price;
        this.url = url;
        this.image = image;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getOrigPrice() {
        return origPrice;
    }

    public void setOrigPrice(Double origPrice) {
        this.origPrice = origPrice;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Shop getShop() {
        return shop;
    }

    public void setShop(Shop shop) {
        this.shop = shop;
    }
}
