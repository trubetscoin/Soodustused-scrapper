package Soodustused.scrapper;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    static ArrayList<Shop> SHOPS = new ArrayList<>();

    public static class Shop {
        private final String name;
        private List<Product> products;

        public Shop(String Name)
        {
            name = Name;
            products = new ArrayList<>();
        }

        public String getName() {return name;}

        public List<Product> getProducts() {return products;}
    }

    public static class Product {
        private String name;
        private String origPrice;
        private String price;
        private String url;
        private String image;

        public void setName(String name) {this.name = name;}

        public void setOrigPrice(String origPrice) {this.origPrice = origPrice;}

        public void setPrice(String price) {this.price = price;}

        public void setUrl(String url) {this.url = url;}

        public void setImage(String image) {this.image = image;}


        public String getName() {return name;}

        public Double getOrigPrice() {
            try {
                return Double.parseDouble(origPrice);
            }
            catch (NumberFormatException | NullPointerException e) {
                return null;
            }
        }

        public Double getPrice() {
            try {
                return Double.parseDouble(price);
            }
            catch (NumberFormatException | NullPointerException e) {
                return null;
            }
        }

        public String getUrl() {return url;}

        public String getImage() {return image;}
    }

    public static WebDriver driver = setUpWebDriver();
    private static final Logger logger = LoggerFactory.getLogger(Scrapper.class);

    public static WebDriver setUpWebDriver(){
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        return new ChromeDriver(options);
    }

    public static Map<String, String> parseLinks = new HashMap<String, String>() {
        {
            put("Prisma", "https://www.prismamarket.ee/products/collection/eripakkumised");
            put("Maxima", "https://www.maxima.ee/pakkumised");
            put("Lidl", "https://www.lidl.ee/et/naedalapakkumised");
        }
    };

    public static void con(String link) {
        driver.get(link);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
    }

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

    private static String shopPropertyCalculation(String shopName, String property, WebElement item, String[] selectors) {
        if (shopName.equalsIgnoreCase("Lidl") && property.equalsIgnoreCase("image")) {
            String image = getElementValue(item, new By.ByCssSelector(selectors[0]), selectors[1]);
            String[] urls = image.split(",\\s+");
            return urls[0].trim().split("\\s+")[0];
        }

        if (shopName.equalsIgnoreCase("Prisma") && property.equalsIgnoreCase("origPrice")) {
            String origPrice = getElementValue(item, new By.ByCssSelector(selectors[0]), selectors[1]);
            if (origPrice == null) origPrice = getElementValue(item, new By.ByCssSelector("div.unit-price"), selectors[1]);
            return origPrice;
        }

        if (Arrays.stream(selectors).anyMatch(Objects::isNull)) return null;
        else if (selectors.length == 1) return item.getAttribute(selectors[0]);
        else if (selectors.length == 3) {
            String firstPart = getElementValue(item, By.cssSelector(selectors[0]), selectors[2]);
            String secondPart = getElementValue(item, By.cssSelector(selectors[1]), selectors[2]);
            return (firstPart != null && secondPart != null) ? firstPart + "." + secondPart : null;
        }
        else { return getElementValue(item, By.cssSelector(selectors[0]), selectors[1]); }
    }

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

    public static void scrapeShop(String shopName, By itemSelector, Map<String, String[]> elementSelectors) {
        logger.info("Scraping " + shopName);
        if (!itemsExists(shopName, itemSelector)) return;

        List<WebElement> itemElements = driver.findElements(itemSelector);

        Shop shop = new Shop(shopName);
        SHOPS.add(shop);

        for (WebElement item : itemElements) {
            Product product = new Product();

            for (Map.Entry<String, String[]> entry : elementSelectors.entrySet()) {
                String property = entry.getKey();
                String[] selectors = entry.getValue();
                String value = shopPropertyCalculation(shopName, property, item, selectors);

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

            if (
                    product.origPrice == null ||
                    product.origPrice.equals("0.00") ||
                    product.price == null ||
                    product.price.equals("0.00") ||
                    product.name == null
            ) continue;

            shop.products.add(product);
        }

        logger.info("All items in " + shopName + " were successfully scraped");
    }


    public static void cookieDecline(By[] selectors) {
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

    public static ArrayList<Shop> getShops() { return SHOPS; }

    public static ArrayList<Shop> scrapeShops(){
        con(parseLinks.get("Prisma"));
        scrapeShop("Prisma", By.cssSelector("ul > li.item"), Stream.of(
                new AbstractMap.SimpleEntry<>("url", new String[]{"a", "href"}),
                new AbstractMap.SimpleEntry<>("image", new String[]{"div > img", "src"}),
                new AbstractMap.SimpleEntry<>("name", new String[]{"div.name", "text"}),
                new AbstractMap.SimpleEntry<>("origPrice", new String[]{"div.discount-price > span", "text"}),
                new AbstractMap.SimpleEntry<>("price", new String[]{"span.whole-number", "span.decimal", "text"})
        ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));


        con(parseLinks.get("Maxima"));
        scrapeShop("Maxima", By.cssSelector("div.col-third"), Stream.of(
                new AbstractMap.SimpleEntry<>("url", new String[]{null}),
                new AbstractMap.SimpleEntry<>("image", new String[]{"div.img > img", "src"}),
                new AbstractMap.SimpleEntry<>("name", new String[]{"div.title", "text"}),
                new AbstractMap.SimpleEntry<>("origPrice", new String[]{"div.t2 > span.value", "text"}),
                new AbstractMap.SimpleEntry<>("price", new String[]{"data-price"})
        ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));


        con(parseLinks.get("Lidl"));
        cookieDecline(new By[] {
                By.cssSelector("button.cookie-alert-decline-button"),
                By.cssSelector("button.overlay__closer")
        });
        scrapeShop("Lidl", By.cssSelector("article.ret-o-card"), Stream.of(
                new AbstractMap.SimpleEntry<>("url", new String[]{"a.ret-o-card__link", "href"}),
                new AbstractMap.SimpleEntry<>("image", new String[]{"source.nuc-a-source", "srcset"}),
                new AbstractMap.SimpleEntry<>("name", new String[]{"data-name"}),
                new AbstractMap.SimpleEntry<>("origPrice", new String[]{"span.lidl-m-pricebox__discount-price", "text"}),
                new AbstractMap.SimpleEntry<>("price", new String[]{"span.lidl-m-pricebox__price", "text"})
        ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

        //debugPrintShopsItems();
        return getShops();
    }

    public static void main(String[] args) {
        scrapeShops();
    }
}
