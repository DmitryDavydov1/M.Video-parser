package com.example.kinopoiskparser.service;

import com.example.kinopoiskparser.model.Phone;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class MVideoParserService {

    // URL-основа для пагинации
    private static final String BASE_URL = "https://www.mvideo.ru/smartfony-i-svyaz-10/smartfony-205?f_tolko-v-nalichii=da&from=under_search&page=";
    private static final int MAX_SCROLLS = 15;   // Максимальное количество прокруток для подгрузки товаров


    /**
     * Главный метод парсинга: проходит по страницам и собирает данные о смартфонах
     */
    public List<Phone> parseAllPhones() {
        List<Phone> phones = new ArrayList<>();

        // Установка пути к ChromeDriver
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\Student Free\\Downloads\\chromedriver-win64\\chromedriver.exe");
        WebDriver driver = initDriver();

        try {
            for (int page = 1; page <= 15; page++) {
                String url = BASE_URL + page;
                System.out.println("Парсинг страницы: " + url);
                driver.get(url);
                Thread.sleep(10000); // Ждем полной загрузки страницы

                closePopupIfExists(driver);      // Закрытие всплывающего окна (если есть)
                scrollToLoadAllProducts(driver); // Прокрутка страницы для подгрузки товаров

                List<WebElement> productElements = driver.findElements(By.cssSelector("div.product-cards-layout__item"));
                System.out.println("Найдено товаров на странице " + page + ": " + productElements.size());

                for (WebElement element : productElements) {
                    Phone phone = extractPhoneInfo(element); // Извлекаем информацию о смартфоне
                    phones.add(phone);
                    System.out.println("Добавлен: " + phone.getTitle());
                }
            }

            saveToJson(phones); // Сохраняем список в JSON
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit(); // Закрываем браузер
        }

        return phones;
    }

    /**
     * Инициализация и настройка драйвера Chrome
     */
    private WebDriver initDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments(
                "--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
                "--disable-blink-features=AutomationControlled",
                "--no-sandbox",
                "--disable-dev-shm-usage",
                "--disable-gpu",
                "window-size=1920,1080",
                "start-maximized"
        );
        options.setExperimentalOption("excludeSwitches", List.of("enable-automation"));
        options.setExperimentalOption("useAutomationExtension", false);
        return new ChromeDriver(options);
    }

    /**
     * Проверка и закрытие всплывающего окна на странице
     */
    private void closePopupIfExists(WebDriver driver) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
            WebElement tooltip = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.location-tooltip-content")));
            if (tooltip.isDisplayed()) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].style.display='none';", tooltip);
                System.out.println("Закрыто всплывающее окно");
            }
        } catch (Exception e) {
            System.out.println("Всплывающее окно не найдено: " + e.getMessage());
        }
    }

    /**
     * Плавная прокрутка страницы вниз для загрузки всех товаров
     */
    private void scrollToLoadAllProducts(WebDriver driver) throws InterruptedException {
        JavascriptExecutor js = (JavascriptExecutor) driver;

        for (int i = 0; i < MAX_SCROLLS; i++) {
            js.executeScript("window.scrollBy(0, 1000);");
            Thread.sleep(1000); // Ожидание для подгрузки товаров
        }
    }

    /**
     * Извлечение информации о телефоне из элемента страницы
     */
    private Phone extractPhoneInfo(WebElement element) {
        Phone phone = new Phone();

        // Название телефона
        try {
            phone.setTitle(element.findElement(By.cssSelector("a.product-title__text")).getText().trim());
        } catch (Exception e) {
            phone.setTitle("N/A");
        }

        // Характеристика "Экран"
        try {
            List<WebElement> features = element.findElements(By.cssSelector("ul.product-feature-list li"));
            for (WebElement feature : features) {
                WebElement nameEl = feature.findElement(By.cssSelector(".product-feature-list__name"));
                if (nameEl.getText().contains("Экран")) {
                    String screen = feature.findElement(By.cssSelector(".product-feature-list__value")).getText();
                    phone.setScreen(screen);
                    break;
                }
            }
        } catch (Exception e) {
            phone.setScreen("N/A");
        }

        // Цена
        try {
            String price = element.findElement(By.cssSelector("span.price__main-value"))
                    .getText().replaceAll("[^0-9]", "");
            phone.setPrice(price.isEmpty() ? "N/A" : price);
        } catch (Exception e) {
            phone.setPrice("N/A");
        }

        // Кэшбэк
        try {
            String cashback = element.findElement(By.className("mbonus-block__count-br"))
                    .getText().replaceAll("[^0-9]", "");
            phone.setCashback(cashback.isEmpty() ? "N/A" : cashback);
        } catch (Exception e) {
            phone.setCashback("N/A");
        }

        // Кол-во отзывов
        try {
            String reviews = element.findElement(By.cssSelector("span[class*='product-rating__feedback']"))
                    .getText().replaceAll("[^0-9]", "");
            phone.setReviewCount(reviews.isEmpty() ? "N/A" : reviews);
        } catch (Exception e) {
            phone.setReviewCount("N/A");
        }

        // Рейтинг
        try {
            String rating = element.findElement(By.cssSelector("span[class*='value ng-star-inserted']"))
                    .getText().replaceAll("[^0-9.]", "");
            phone.setRating(rating.isEmpty() ? "N/A" : rating);
        } catch (Exception e) {
            phone.setRating("N/A");
        }

        return phone;
    }

    /**
     * Сохранение списка телефонов в JSON-файл
     */
    private void saveToJson(List<Phone> phones) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File("phones.json"), phones);
            System.out.println("JSON успешно записан в phones.json");
        } catch (Exception e) {
            System.out.println("Ошибка записи JSON: " + e.getMessage());
        }
    }
}
