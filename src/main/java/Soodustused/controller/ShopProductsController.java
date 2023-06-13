package Soodustused.controller;

import Soodustused.model.Product;
import Soodustused.model.Shop;
import Soodustused.service.ProductService;
import Soodustused.service.ShopService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class ShopProductsController {
    private final ShopService shopService;
    private final ProductService productService;

    public ShopProductsController(ShopService shopService, ProductService productService) {
        this.shopService = shopService;
        this.productService = productService;
    }

    @GetMapping("/")
    public String getShops(Model model) {
        List<Shop> shops = shopService.getAllShops();
        model.addAttribute("shops", shops);
        return "shop-list";
    }

    @GetMapping("/shop/{id}")
    public String getShopProducts(@PathVariable("id") Integer id, Model model) {
        Shop shop = shopService.getShopById(id);
        if(shop == null) {
            model.addAttribute("errorMessage", "Shop not found");
            return "error-page";
        }
        List<Product> products = productService.getProductsByShopId(id);
        model.addAttribute("shop", shop);
        model.addAttribute("products", products);
        model.addAttribute("productsCount", products.size());
        return "shop-products";
    }
}
