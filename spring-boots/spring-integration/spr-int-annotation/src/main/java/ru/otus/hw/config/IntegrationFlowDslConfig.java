package ru.otus.hw.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import ru.otus.hw.model.Product;
import ru.otus.hw.service.ProductProcessorService;

import java.util.Collection;

/**
 * Вариант 2 Конфигурация Spring Integration Flow DSL.
 * Определяет Endpoint-ы для потока обработки продуктов
 */
@ConditionalOnProperty(name = "flow.dsl.enabled", havingValue = "true", matchIfMissing = false/*true*/)
@RequiredArgsConstructor
@Configuration
public class IntegrationFlowDslConfig {

    private final ProductProcessorService productProcessorService;

    @Bean
    public IntegrationFlow productFlow() {
        return IntegrationFlow.from("inputChannel")
            .split()
            .<Product, String>route(
                product ->
                    switch (product.type()) {
                        case FISH -> "fishChannel";
                        case DAIRY -> "dairyChannel";
                        case MEAT -> "meatChannel";
                        case PASTRY -> "pastryChannel";
                        case GROCERY -> "groceryChannel";
                        case BEVERAGE -> "beverageChannel";
                    },
                mapping ->
                    mapping
                        .subFlowMapping(
                            "fishChannel",
                            c -> c.handle(
                                m -> productProcessorService.processProduct(
                                    (Product) m.getPayload(), "FISH")
                            )
                        )
                        .subFlowMapping(
                            "dairyChannel",
                            c -> c.handle(
                                m ->
                                    productProcessorService.processProduct(
                                        (Product) m.getPayload(), "DAIRY"
                                    )
                            )
                        )
                        .subFlowMapping(
                            "meatChannel",
                            c -> c.handle(
                                m ->
                                    productProcessorService.processProduct(
                                        (Product) m.getPayload(), "MEAT"
                                    )
                            )
                        )
                        .subFlowMapping(
                            "pastryChannel",
                            c -> c.handle(
                                m ->
                                    productProcessorService.processProduct(
                                        (Product) m.getPayload(), "PASTRY"
                                    )
                            )
                        )
                        .subFlowMapping(
                            "groceryChannel",
                            c -> c.handle(
                                m -> productProcessorService.processProduct(
                                    (Product) m.getPayload(), "GROCERY"
                                )
                            )
                        )
                        .subFlowMapping(
                            "beverageChannel",
                            c ->
                                c.handle(
                                    m -> productProcessorService.processProduct(
                                        (Product) m.getPayload(), "BEVERAGE"
                                    )
                                )
                        )
            )
            .get();
    }
}