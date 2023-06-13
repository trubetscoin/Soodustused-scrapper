package Soodustused.repository;

import Soodustused.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    // not necessary, unless to change items inside a shop
    // in this current state not useful

    // custom crud functionality
}