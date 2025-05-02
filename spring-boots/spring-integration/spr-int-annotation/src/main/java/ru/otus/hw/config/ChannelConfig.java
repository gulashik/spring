package ru.otus.hw.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.MessageChannel;

/**
 * Конфигурация Spring Integration.
 * Определяет Channel-ы для потока обработки продуктов.
 */
@Configuration
public class ChannelConfig {
    /**
     * Входной канал для получения всех продуктов.
     */
    @Bean
    public MessageChannel inputChannel() {
        return new DirectChannel();
    }

    /**
     * Канал для уплощённых заказов
     */
    @Bean
    public MessageChannel splitProductChannel() {
        return new DirectChannel();
    }

    /**
     * Канал для продуктов типа FISH.
     */
    @Bean
    public MessageChannel fishChannel() {
        return new DirectChannel();
    }

    /**
     * Канал для продуктов типа DAIRY.
     */
    @Bean
    public MessageChannel dairyChannel() {
        return new DirectChannel();
    }

    /**
     * Канал для продуктов типа MEAT.
     */
    @Bean
    public MessageChannel meatChannel() {
        return new DirectChannel();
    }

    /**
     * Канал для продуктов типа PASTRY.
     */
    @Bean
    public MessageChannel pastryChannel() {
        return new DirectChannel();
    }

    /**
     * Канал для продуктов типа GROCERY.
     */
    @Bean
    public MessageChannel groceryChannel() {
        return new DirectChannel();
    }

    /**
     * Канал для продуктов типа BEVERAGE.
     */
    @Bean
    public MessageChannel beverageChannel() {
        return new DirectChannel();
    }
}