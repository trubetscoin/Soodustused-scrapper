package Soodustused.service;

import Soodustused.model.Shop;
import Soodustused.repository.ProductRepository;
import Soodustused.repository.ShopRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShopService {
    private final ShopRepository shopRepository;

    @Autowired
    public ShopService(ShopRepository shopRepository, ProductRepository productRepository) {
        this.shopRepository = shopRepository;
    }

    public void saveShopWithProducts(Shop shop) {
        shopRepository.save(shop);
    }

    public List<Shop> getAllShops() {
        return shopRepository.findAll();
    }

    public Shop getShopById(Integer id) {
        return shopRepository.findById(id).orElse(null);
    }
}