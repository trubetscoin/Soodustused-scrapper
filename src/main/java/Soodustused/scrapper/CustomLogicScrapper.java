package Soodustused.scrapper;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static Soodustused.scrapper.Scrapper.getElementValue;

public class CustomLogicScrapper {
    /**
     * Calculates custom property's value
     * @param shopName String
     * @param property String
     * @param item Webelement
     * @param selectors String[]
     * @return custom property's value
     */
    static String shopCustomLogicCalculation(String shopName, String property, WebElement item, String[] selectors) {
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

        if (shopName.equalsIgnoreCase("Prisma") && property.equalsIgnoreCase("price")) {
            String wholePrice = getElementValue(item, new By.ByCssSelector(selectors[0]), selectors[2]);
            String decimalPrice = getElementValue(item, new By.ByCssSelector(selectors[1]), selectors[2]);
            return wholePrice + "." + decimalPrice;
        }

        return null;
    }
}
