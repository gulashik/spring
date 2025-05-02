package ru.otus.hw.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.otus.hw.model.Product;
import ru.otus.hw.model.ProductType;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Сервис обработки продуктов из каналов и вывода накопительной статистики.
 */
@Slf4j
@Service
public class ProductProcessorService {

    /** Счетчик общего количества обработанных продуктов */
    private final AtomicInteger totalProcessed = new AtomicInteger(0);

    /** Map хранения суммы по каждому типу продуктов */
    private final Map<ProductType, BigDecimal> typeTotals = new EnumMap<>(ProductType.class);

    /** Общая сумма по всем типам продуктов */
    private BigDecimal grandTotal = BigDecimal.ZERO;

    /**
     * Конструктор, инициализирующий суммы для всех типов продуктов нулями.
     */
    public ProductProcessorService() {
        for (ProductType type : ProductType.values()) {
            typeTotals.put(type, BigDecimal.ZERO);
        }
    }

    /**
     * Обрабатывает продукт любого типа, обновляет суммы и выводит статистику.
     *
     * @param product обрабатываемый продукт
     * @param channelName название канала для логирования
     */
    public synchronized void processProduct(Product product, String channelName) {

        int count = totalProcessed.incrementAndGet();

        BigDecimal typeTotal = typeTotals.get(product.type()).add(product.price());
        typeTotals.put(product.type(), typeTotal);

        grandTotal = grandTotal.add(product.price());

        log.info("Channel [{}] received: {} - {}, Price: {}",
            channelName, product.type(), product.name(), product.price());

        // Выводим накопительные итоги
        log.info("=== Running Totals (after {} products) ===", count);
        for (Map.Entry<ProductType, BigDecimal> entry : typeTotals.entrySet()) {
            if (entry.getValue().compareTo(BigDecimal.ZERO) > 0) {
                log.info("{} channel total: {}", entry.getKey(), entry.getValue());
            }
        }
        log.info("GRAND TOTAL: {}", grandTotal);
        log.info("==========================================");
    }
}
