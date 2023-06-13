package Soodustused.repository;

import Soodustused.model.Shop;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopRepository extends JpaRepository<Shop, Integer> {
    // custom crud functionality
}