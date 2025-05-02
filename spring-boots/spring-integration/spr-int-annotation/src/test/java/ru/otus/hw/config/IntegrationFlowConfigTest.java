package ru.otus.hw.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;
import ru.otus.hw.model.Product;
import ru.otus.hw.model.ProductType;
import ru.otus.hw.service.ProductGeneratorService;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(properties = "scheduler.enabled=false")
class IntegrationFlowConfigTest {

    @Autowired
    private ProductGeneratorService generatorService;

    @Autowired
    private MessageChannel inputChannel;

    @Autowired
    private ProductGateway productGateway;

    @Test
    void contextLoads() {
        // Проверяем, что контекст загружается корректно
        assertNotNull(generatorService);
        assertNotNull(inputChannel);
        assertNotNull(productGateway);
    }

    @Test
    void testProductGenerator() {
        // Проверяем генерацию продуктов
        List<Product> products = generatorService.generateProducts(5);
        assertEquals(5, products.size());
    }

    @Test
    void testSendToInputChannel() {
        // Создаем тестовый продукт
        var products = List.of(
            new Product(ProductType.BEVERAGE, "Test Cola", BigDecimal.valueOf(2.50)),
            new Product(ProductType.BEVERAGE, "Test Fish", BigDecimal.valueOf(12.50)),
            new Product(ProductType.BEVERAGE, "Test Beef", BigDecimal.valueOf(22.50))
        );

        inputChannel.send(new GenericMessage<>(products));
        productGateway.sendProduct(products);
    }
}