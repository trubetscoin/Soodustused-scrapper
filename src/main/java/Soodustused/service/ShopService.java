package Soodustused.service;

import Soodustused.model.Product;
import Soodustused.model.Shop;
import Soodustused.repository.ProductRepository;
import Soodustused.repository.ShopRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public List<Shop> searchShopsByName(String query) {
        String[] queryWords = query.toLowerCase().split("\\s+");
        Stream<Shop> shopStream = shopRepository.findAll()
                .stream()
                .filter(shop -> {
                    String shopName = shop.getName().toLowerCase();
                    for (String queryWord : queryWords) {
                        if (!shopName.contains(queryWord)) {
                            return false;
                        }
                    }
                    return true;
                });

        return shopStream.collect(Collectors.toList());
    }
}