package ru.otus.hw.config;

import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import ru.otus.hw.model.Product;

import java.util.Collection;

/**
 * Messaging gateway входная точка для отправки {@link Product} в integration flow.
 */
@MessagingGateway
public interface ProductGateway {
    @Gateway(requestChannel = "inputChannel")
    void sendProduct(Collection<Product> products);
}