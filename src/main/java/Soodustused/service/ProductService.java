package Soodustused.service;

import Soodustused.model.Product;
import Soodustused.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getProductsByShopId(Integer shopId) {
        return productRepository.findAll()
                .stream()
                .filter(product -> product.getShop().getId().equals(shopId))
                .collect(Collectors.toList());
    }
}