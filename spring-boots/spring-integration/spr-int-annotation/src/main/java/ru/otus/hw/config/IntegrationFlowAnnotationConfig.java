package ru.otus.hw.config;


import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.Router;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.annotation.Splitter;
import ru.otus.hw.model.Product;
import ru.otus.hw.service.ProductProcessorService;

import java.util.Collection;

/**
 * Вариант 1 Конфигурация Spring Integration Flow Annotation.
 * Определяет Endpoint-ы для потока обработки продуктов.
 */
@ConditionalOnProperty(name = "flow.dsl.enabled", havingValue = "false", matchIfMissing = true/*true*/)
@RequiredArgsConstructor
@Configuration
public class IntegrationFlowAnnotationConfig {

    private final ProductProcessorService productProcessorService;

    /**
     * Собирает продукты в коллекцию. Забавно, но нужно просто отдать туже коллекцию.
     *
     * @param products коллекция для разделения
     * @return список будет по элементо получена в следующем канале.
     */
    @Splitter(inputChannel = "inputChannel", outputChannel = "splitProductChannel")
    public Collection<Product> splitProduct(Collection<Product> products) {
        return products;
    }

    /**
     * Маршрутизатор для распределения продуктов по соответствующим каналам в зависимости от их типа.
     *
     * @param product продукт для маршрутизации
     * @return имя канала, в который должен быть направлен продукт
     */
    @Router(inputChannel = "splitProductChannel")
    public String routeByProductType(Product product) {
        return switch (product.type()) {
            case FISH -> "fishChannel";
            case DAIRY -> "dairyChannel";
            case MEAT -> "meatChannel";
            case PASTRY -> "pastryChannel";
            case GROCERY -> "groceryChannel";
            case BEVERAGE -> "beverageChannel";
        };
    }

    /**
     * Обрабатывает продукты из канала FISH.
     *
     * @param product продукт типа FISH
     */
    @ServiceActivator(inputChannel = "fishChannel")
    public void processFish(Product product) {
        productProcessorService.processProduct(product, "FISH");
    }

    /**
     * Обрабатывает продукты из канала DAIRY.
     *
     * @param product продукт типа DAIRY
     */
    @ServiceActivator(inputChannel = "dairyChannel")
    public void processDairy(Product product) {
        productProcessorService.processProduct(product, "DAIRY");
    }

    /**
     * Обрабатывает продукты из канала MEAT.
     *
     * @param product продукт типа MEAT
     */
    @ServiceActivator(inputChannel = "meatChannel")
    public void processMeat(Product product) {
        productProcessorService.processProduct(product, "MEAT");
    }

    /**
     * Обрабатывает продукты из канала PASTRY.
     *
     * @param product продукт типа PASTRY
     */
    @ServiceActivator(inputChannel = "pastryChannel")
    public void processPastry(Product product) {
        productProcessorService.processProduct(product, "PASTRY");
    }

    /**
     * Обрабатывает продукты из канала GROCERY.
     *
     * @param product продукт типа GROCERY
     */
    @ServiceActivator(inputChannel = "groceryChannel")
    public void processGrocery(Product product) {
        productProcessorService.processProduct(product, "GROCERY");
    }

    /**
     * Обрабатывает продукты из канала BEVERAGE.
     *
     * @param product продукт типа BEVERAGE
     */
    @ServiceActivator(inputChannel = "beverageChannel")
    public void processBeverage(Product product) {
        productProcessorService.processProduct(product, "BEVERAGE");
    }
}