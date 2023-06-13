package Soodustused.model;

import jakarta.persistence.*;


import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "Shop")
public class Shop {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL)
    private List<Product> products = new ArrayList<>();

    public Shop() {
    }

    public Shop(String name) {
        this.name = name;
        this.products = new ArrayList<>();
    }

    public Shop(String name, ArrayList<Product> products) {
        this.name = name;
        this.products = products;
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

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }
}
