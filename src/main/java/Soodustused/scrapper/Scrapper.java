package Soodustused.scrapper;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Scrapper {
    public static class Shop {
        private final String name;
        private ArrayList<Product> products;

        public Shop(String Name)
        {
            name = Name;
            products = new ArrayList<>();
        }

        public String getName() {
            return name;
        }

        public List<Product> getProducts() {
            return products;
        }
    }

    static ArrayList<Shop> SHOPS = new ArrayList<>();
    public static ArrayList<Shop> getShops() { return SHOPS; }

    public static class Product {
        private String name;
        private String origPrice;
        private String price;
        private String url;
        private String image;

        public void setName(String name) {
            this.name = name;
        }

        public void setOrigPrice(String origPrice) {
            this.origPrice = origPrice;
        }

        public void setPrice(String price) {
            this.price = price;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public void setImage(String image) {
            this.image = image;
        }


        public String getName() {return name;}

        public Double getOrigPrice() {
            try {
                return Double.parseDouble(origPrice);
            }
            catch (NullPointerException | NumberFormatException e) {
                return null;
            }
        }

        public Double getPrice() {
            try {
                return Double.parseDouble(price);
            }
            catch (NullPointerException | NumberFormatException e) {
                return null;
            }
        }

        public String getUrl() {
            return url;
        }

        public String getImage() {
            return image;
        }
    }

    public static class ShopsConfigurations {

        // Name of the shop with the corresponding shop's data
        private Map<String, ShopConfiguration> shopConfigs;

        private Map<String, ShopConfiguration> getShopConfigs() {
            return shopConfigs;
        }

        public void setShopConfigs(Map<String, ShopConfiguration> shopConfigs) {
            this.shopConfigs = shopConfigs;
        }

        private ShopConfiguration getShopConfig(String shopName) {
            return shopConfigs.get(shopName);
        }
    }

    public static class ShopConfiguration {
        // url to scrape items
        private String scrapeUrl;
        // selector to scrape items
        private String itemSelector;
        private Map<String, String[]> properties;
        private String[] cookieSelectors;
        private boolean customLogic;

        private String getScrapeUrl() {
            return scrapeUrl;
        }

        public void setScrapeUrl(String scrapeUrl) {
            this.scrapeUrl = scrapeUrl;
        }

        private String getItemSelector() {
            return itemSelector;
        }

        public void setItemSelector(String itemSelector) {
            this.itemSelector = itemSelector;
        }

        private Map<String, String[]> getProperties() {
            return properties;
        }

        public void setProperties(Map<String, String[]> properties) {
            this.properties = properties;
        }

        private boolean hasCustomLogic() {
            return customLogic;
        }

        public void setCustomLogic(Boolean customLogic) {
            this.customLogic = customLogic;
        }

        private String[] getCookieSelectors() {
            return cookieSelectors;
        }

        public void setCookieSelectors(String[] cookieSelectors) {
            this.cookieSelectors = cookieSelectors;
        }
    }

    public static WebDriver driver = setUpWebDriver();
    private static final Logger logger = LoggerFactory.getLogger(Scrapper.class);
    private static final ShopsConfigurations config = loadConfiguration();

    // TODO method desc
    public static WebDriver setUpWebDriver(){
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        return new ChromeDriver(options);
    }

    // TODO method desc
    public static void connect(String link) {
        driver.get(link);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
    }

    // TODO method desc
    public static String getElementValue(WebElement element, By selector, String attribute) {
        try {
            WebElement foundElement = element.findElement(selector);
            if (attribute.equalsIgnoreCase("text")) {
                return foundElement.getText();
            } else {
                return foundElement.getAttribute(attribute);
            }
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    // TODO method desc
    public static boolean itemsExists(String shopName, By selector) {
        try {
            WebElement item = driver.findElement(selector);
            logger.info("Items to scrape in " + shopName + " are found");
            return true;
        } catch (NoSuchElementException e) {
            logger.warn("No items were found in " + shopName + " to scrape");
            return false;
        }
    }

    // TODO method desc
    private static String shopCustomLogicCalculation(String shopName, String property, WebElement item, String[] selectors) {
        if (shopName.equalsIgnoreCase("Lidl") && property.equalsIgnoreCase("image")) {
            String image = getElementValue(item, new By.ByCssSelector(selectors[0]), selectors[1]);
            if (image == null) return null;
            String[] urls = image.split(",\\s+");
            return urls[0].trim().split("\\s+")[0];
        }

        if (shopName.equalsIgnoreCase("Prisma") && property.equalsIgnoreCase("origPrice")) {
            String origPrice = getElementValue(item, new By.ByCssSelector(selectors[0]), selectors[1]);
            if (origPrice == null) origPrice = getElementValue(item, new By.ByCssSelector("div.unit-price"), selectors[1]);
            return origPrice;
        }

        return null;
    }

    // TODO method desc
    private static String shopPropertyCalculation(String shopName, ShopConfiguration shopConfiguration, String property, WebElement item, String[] selectors) {
        if (selectors == null) {
            return null;
        }

        if (Arrays.stream(selectors).anyMatch(Objects::isNull)) {
            return null;
        }

        if (shopConfiguration.hasCustomLogic()) {
            String result = shopCustomLogicCalculation(shopName, property, item, selectors);
            if (result != null) {
                return result;
                //TODO customLogic should be array of properties with custom logic
            }
        }

        if (selectors.length == 2) {
            return getElementValue(item, By.cssSelector(selectors[0]), selectors[1]);
        }

        if (selectors.length == 1) {
            return item.getAttribute(selectors[0]);
        }

        return null;
    }

    /**
     * Makes prices look better separated by a dot
     * @param price String
     * @return prettified String price
     */
    private static String pricePrettify(String price) {
        if (price == null) return null;

        StringBuilder sb = new StringBuilder();
        for (char c : price.toCharArray()) {
            if (Character.isDigit(c) || c == ',' || c == '.') {
                sb.append(c);
            }
        }
        return sb.toString().replace(",", ".").replace("€", "").trim();
    }

    /**
     * Scrapes a shop and add it to ArrayList of Shop class
     *
     */
    private static void scrapeShop(String shopName, ShopConfiguration shopConfiguration) {
        logger.info("Scraping " + shopName);
        if (shopConfiguration == null) {
            logger.error("An empty shop configuration received. Skipping to next shop...");
            return;
        }

        connect(shopConfiguration.getScrapeUrl());

        String[] cookieSelectors = shopConfiguration.getCookieSelectors();
        if (cookieSelectors != null) {
            if (cookieSelectors.length > 0) {
                elementsClick(stringToByCss(cookieSelectors));
            }
        }

        By itemSelector = By.cssSelector(shopConfiguration.getItemSelector());
        Map<String, String[]> elementSelectors = shopConfiguration.getProperties();

        if (!itemsExists(shopName, itemSelector)) return;

        List<WebElement> itemElements = driver.findElements(itemSelector);

        Shop shop = new Shop(shopName);
        SHOPS.add(shop);

        for (WebElement item : itemElements) {
            Product product = new Product();

            for (Map.Entry<String, String[]> entry : elementSelectors.entrySet()) {
                String property = entry.getKey();
                String[] selectors = entry.getValue();
                String value = shopPropertyCalculation(shopName, shopConfiguration, property, item, selectors);

                switch (property) {
                    case "url":
                        product.setUrl(value);
                        break;
                    case "image":
                        product.setImage(value);
                        break;
                    case "name":
                        product.setName(value);
                        break;
                    case "origPrice":
                        product.setOrigPrice(pricePrettify(value));
                        break;
                    case "price":
                        product.setPrice(pricePrettify(value));
                        break;
                }
            }

            if (product.origPrice == null ||
                    product.origPrice.equals("0.00") ||
                    product.price == null ||
                    product.price.equals("0.00") ||
                    product.name == null
            ) continue;
            shop.products.add(product);
        }



        logger.info("All items in " + shopName + " were successfully scraped");
    }

    /**
     * Loads JSON shops configurations from resources
     * @return
     */
    private static ShopsConfigurations loadConfiguration() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            ClassLoader classLoader = Scrapper.class.getClassLoader();
            File configFile = new File(Objects.requireNonNull(classLoader.getResource("json/shopsConfig.json")).getFile());
            return objectMapper.readValue(configFile, ShopsConfigurations.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Convert String[] to ByCss[] selectors
     * @param stringSelectors
     */
    public static By[] stringToByCss(String[] stringSelectors) {
        By[] selectors = new By[stringSelectors.length];
        for (int i = 0; i < stringSelectors.length; i++) {
            selectors[i] = By.cssSelector(stringSelectors[i]);
        }
        return selectors;
    }

    /**
     * Waits for elements to load and click on every in order
     * @param selectors
     * By css selectors
     */
    public static void elementsClick(By[] selectors) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            for (By selector : selectors) {
                WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(selector));
                element.click();
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * DEBUGGING
     * Prints all shops data
     */
    public static void debugPrintShopsItems() {
        System.out.println("PRINTING SHOPS AND THEIR ITEMS.");
        for (Shop shop : SHOPS) {
            System.out.println("###############################");
            System.out.println(shop.name);
            System.out.println("###############################");
            System.out.println("SHOP PRODUCTS: ");
            List<Product> items = shop.products;
            for (Product item : items) {
                System.out.println("Name:" + item.name);
                System.out.println("Orig Price: " + item.origPrice);
                System.out.println("Price: " + item.price);
                System.out.println("Image: " + item.image);
                System.out.println("Url: " + item.url);
            }
        }
    }

    /**
     * Main method
     * @return
     * ArrayList of Shops with ArrayList of Products
     */
    public static ArrayList<Shop> scrapeShops() {
        if (config == null) {
            logger.error("Shops Configs are empty");
            return null;
        }

        Map<String, ShopConfiguration> configs = config.getShopConfigs();

        for (Map.Entry<String, ShopConfiguration> entry : configs.entrySet()) {
            String shopName = entry.getKey();
            if (shopName.equals("Prisma")) continue;
            ShopConfiguration shopConfiguration = entry.getValue();
            scrapeShop(shopName, shopConfiguration);
        }

        //debugPrintShopsItems();
        return getShops();
    }

    public static void main(String[] args) {
        scrapeShops();
    }
}
