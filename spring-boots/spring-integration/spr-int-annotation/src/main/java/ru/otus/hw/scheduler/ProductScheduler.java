package ru.otus.hw.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.otus.hw.config.ProductGateway;
import ru.otus.hw.model.Product;
import ru.otus.hw.service.ProductGeneratorService;

import java.util.List;

/**
 * Компонент для периодической генерации и отправки продуктов.
 */
@RequiredArgsConstructor
@Slf4j
@Component
/*для возможности отключения планировщика для тестов*/
@ConditionalOnProperty(name = "scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class ProductScheduler {

    private final ProductGeneratorService generatorService;

    private final ProductGateway productGateway;

    @Value("${product.batch.size:5}")
    private int batchSize;

    /**
     * Метод периодически генерирует продукты и отправляет их во входной канал.
     */
    @Scheduled(fixedRate = 5000) // Запуск каждые 5 секунд
    public void generateAndSendProducts() {
        List<Product> products = generatorService.generateProducts(batchSize);
        log.info("Generated products {} ", products);

        productGateway.sendProduct(products);
    }
}
