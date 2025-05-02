package ru.otus.hw.model;

import java.math.BigDecimal;

/**
 * Иммутабельнуя структура данных с информацией о продукте.
 *
 * @param type тип продукта из перечисления ProductType
 * @param name название продукта
 * @param price стоимость продукта
 */
public record Product(ProductType type, String name, BigDecimal price) {

    @Override
    public String toString() {
        return String.format("%s: %s - %.2f", type, name, price);
    }
}