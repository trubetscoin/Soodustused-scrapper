package Soodustused.service;


import Soodustused.scrapper.Scrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import Soodustused.model.Product;
import Soodustused.model.Shop;


import java.util.ArrayList;
import java.util.List;

@Service
public class ScrappingService {
    private final ShopService shopService;

    @Autowired
    public ScrappingService(ShopService shopService) {
        this.shopService = shopService;
    }

    public void scrapeShops() {
        ArrayList<Scrapper.Shop> scrapperShops = Scrapper.scrapeShops();
        for (Scrapper.Shop scrapperShop : scrapperShops) {
            Shop shop = new Shop(scrapperShop.getName());
            List<Scrapper.Product> scrapperProducts = scrapperShop.getProducts();
            List<Product> products = new ArrayList<>();
            for (Scrapper.Product scrapperProduct : scrapperProducts) {
                Product product = new Product(
                        scrapperProduct.getName(),
                        scrapperProduct.getOrigPrice(),
                        scrapperProduct.getPrice(),
                        scrapperProduct.getUrl(),
                        scrapperProduct.getImage()
                );
                product.setShop(shop);
                products.add(product);
            }
            shop.setProducts(products);
            shopService.saveShopWithProducts(shop);
        }
    }
}