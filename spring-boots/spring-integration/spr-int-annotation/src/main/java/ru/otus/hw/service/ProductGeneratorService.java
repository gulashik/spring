package ru.otus.hw.service;


import org.springframework.stereotype.Service;
import ru.otus.hw.model.Product;
import ru.otus.hw.model.ProductType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Сервис для генерации случайных данных о продуктах.
 * Генерирует различные типы продуктов с названиями и случайными ценами.
 */
@Service
public class ProductGeneratorService {

    private final Random random = new Random();

    /**
     * Примеры названий продуктов для каждого типа
     */
    private final String[][] productNames = {
        // FISH
        {"Salmon", "Tuna", "Cod", "Trout", "Mackerel"},
        // DAIRY
        {"Milk", "Cheese", "Yogurt", "Butter", "Cream"},
        // MEAT
        {"Beef", "Chicken", "Pork", "Lamb", "Turkey"},
        // PASTRY
        {"Bread", "Croissant", "Cake", "Cookies", "Pie"},
        // GROCERY
        {"Rice", "Pasta", "Cereal", "Flour", "Sugar"},
        // BEVERAGE
        {"Cola", "Water", "Juice", "Tea", "Coffee"}
    };

    /**
     * Генерирует список случайных продуктов.
     *
     * @param count количество продуктов для генерации
     * @return список сгенерированных продуктов
     */
    public List<Product> generateProducts(int count) {
        List<Product> products = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            products.add(generateRandomProduct());
        }
        return products;
    }

    /**
     * Генерирует один случайный продукт.
     *
     * @return случайно сгенерированный продукт
     */
    public Product generateRandomProduct() {
        ProductType type = ProductType.values()[random.nextInt(ProductType.values().length)];
        String name = getRandomProductName(type);
        BigDecimal price = generateRandomPrice();

        return new Product(type, name, price);
    }

    /**
     * Получает случайное название продукта для заданного типа.
     *
     * @param type тип продукта
     * @return случайное название продукта
     */
    private String getRandomProductName(ProductType type) {
        String[] names = productNames[type.ordinal()];
        return names[random.nextInt(names.length)];
    }

    /**
     * Генерирует случайную цену в диапазоне от 0.50 до 100.00.
     *
     * @return случайная цена продукта
     */
    private BigDecimal generateRandomPrice() {
        return BigDecimal
            .valueOf(random.nextDouble(0.5, 100.01))
            .setScale(2, RoundingMode.HALF_UP);
    }
}
