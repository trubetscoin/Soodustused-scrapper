package Soodustused.scrapper;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.type.TypeReference;
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
        private final ArrayList<Product> products;

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

    static ArrayList<Shop> shops = new ArrayList<>();
    public static ArrayList<Shop> getShops() { return shops; }

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

    public static class ShopConfiguration {
        // name of the shop
        private String shopName;
        // url to scrape items
        private String scrapeUrl;
        // selector to scrape items
        private String itemSelector;
        private Map<String, String[]> properties;
        private String[] cookieSelectors;
        private String[] customLogicProperties;

        public String getShopName() {
            return shopName;
        }

        public void setShopName(String shopName) {
            this.shopName = shopName;
        }

        public String getScrapeUrl() {
            return scrapeUrl;
        }

        public void setScrapeUrl(String scrapeUrl) {
            this.scrapeUrl = scrapeUrl;
        }

        public String getItemSelector() {
            return itemSelector;
        }

        public void setItemSelector(String itemSelector) {
            this.itemSelector = itemSelector;
        }

        public Map<String, String[]> getProperties() {
            return properties;
        }

        public void setProperties(Map<String, String[]> properties) {
            this.properties = properties;
        }

        public boolean hasCustomLogic(String property) {
            if (customLogicProperties == null) return false;
            for (String s : customLogicProperties) {
                if (s.equals(property)) return true;
            }
            return false;
        }

        public void setCustomLogicProperties(String[] customLogicProperties) {
            this.customLogicProperties = customLogicProperties;
        }

        public String[] getCustomLogicProperties() {
            return customLogicProperties;
        }

        public String[] getCookieSelectors() {
            return cookieSelectors;
        }

        public void setCookieSelectors(String[] cookieSelectors) {
            this.cookieSelectors = cookieSelectors;
        }
    }

    private static List<ShopConfiguration> shopsConfigs = loadConfiguration();
    private static final WebDriver driver = setUpWebDriver();
    private static final Logger logger = LoggerFactory.getLogger(Scrapper.class);
    private static final List<String> explicitWaitShopNames = initExplicitWaitShopNames();

    private static void loadGui() {
        Gui gui = new Gui(shopsConfigs);
        gui.showGui();
    }

    /**
     * Setups webdriver for scrapping webpages
     */
    private static WebDriver setUpWebDriver(){
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        return new ChromeDriver(options);
    }

    // in future can be replaced with either
    // checking status code of the request in network
    // or can be included in json
    private static List<String> initExplicitWaitShopNames() {
        List<String> list = new ArrayList<>();
        list.add("selver");
        return list;
    }

    /**
     * Gives html of a webpage
     * @param url url of a webpage
     */
    private static void connect(String url) {
        driver.get(url);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(webDriver -> ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"));
    }

    private static void connect(String url, int waitTimeSeconds) {
        driver.get(url);
        try {
            Thread.sleep(waitTimeSeconds * 1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Calculates a value of an element.
     * Throws NoSuchElementException if such element was not found
     * @param element Webelement
     * @param selector By selector
     * @param attribute String
     * @return value of an element
     */
    static String getElementValue(WebElement element, By selector, String attribute) {
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

    /**
     * Checks if a shop contains at least one item to scrape
     * @param shopName String
     * @param selector By
     * @return true/false
     */
    private static boolean itemsExists(String shopName, By selector) {
        try {
            WebElement item = driver.findElement(selector);
            logger.info("Items to scrape in " + shopName + " are found");
            return true;
        } catch (NoSuchElementException e) {
            logger.warn("No items were found in " + shopName + " to scrape");
            return false;
        }
    }

    /**
     * Calculates property's value
     * @param shopName String
     * @param shopConfiguration ShopConfiguration
     * @param property String
     * @param item Weblement
     * @param selectors String[]
     * @return property's value
     */
    private static String shopPropertyCalculation(String shopName, ShopConfiguration shopConfiguration, String property, WebElement item, String[] selectors) {
        if (selectors == null) {
            return null;
        }

        if (Arrays.stream(selectors).anyMatch(Objects::isNull)) {
            return null;
        }

        if (shopConfiguration.hasCustomLogic(property)) {
            return CustomLogicScrapper.shopCustomLogicCalculation(shopName, property, item, selectors);
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
        String cleanedPrice = sb.toString().replace(",", ".").replace("€", "").trim();
        int dotIndex = cleanedPrice.indexOf(".");
        if (dotIndex != -1 && dotIndex + 3 < cleanedPrice.length()) {
            cleanedPrice = cleanedPrice.substring(0, dotIndex + 3);
        }
        return cleanedPrice;
    }

    /**
     * Scrapes a shop and add it to ArrayList of Shop class
     *
     */
    private static void scrapeShop(ShopConfiguration shopConfiguration) {
        if (shopConfiguration == null) {
            logger.error("An empty shop configuration received. Skipping to next shop...");
            return;
        }

        String shopName = shopConfiguration.getShopName();
        logger.info("Scraping " + shopName);

        if (explicitWaitShopNames.contains(shopName.toLowerCase())) {
            connect(shopConfiguration.getScrapeUrl(), 10);
        }
        else {
            connect(shopConfiguration.getScrapeUrl());
        }

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
        shops.add(shop);

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

    static void updateShopConfigs() {
        shopsConfigs = loadConfiguration();
    }

    /**
     * Loads JSON shops configurations
     */
    private static List<ShopConfiguration> loadConfiguration() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Path outputPath = Paths.get("json/shopsConfig.json");
            return objectMapper.readValue(outputPath.toFile(), new TypeReference<List<ShopConfiguration>>() {});
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Convert String[] to ByCss[] selectors
     * @param stringSelectors String array
     */
    private static By[] stringToByCss(String[] stringSelectors) {
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
    private static void elementsClick(By[] selectors) {
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
    private static void debugPrintShopsItems() {
        System.out.println("PRINTING SHOPS AND THEIR ITEMS.");
        for (Shop shop : shops) {
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
        if (shopsConfigs == null || shopsConfigs.isEmpty()) {
            logger.error("Shops Configs are empty");
            return null;
        }

        for (ShopConfiguration shopConfig : shopsConfigs) {
            scrapeShop(shopConfig);
        }

//        debugPrintShopsItems();
        return getShops();
    }

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("-debug")) {
            loadGui();
        } else {
            scrapeShops();
        }
    }
}
