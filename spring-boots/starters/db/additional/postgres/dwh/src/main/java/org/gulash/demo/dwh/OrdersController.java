package org.gulash.demo.dwh;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * Демонстрационный REST-контроллер.
 *
 * <h2>Что делает</h2>
 * <ul>
 *   <li>{@code GET /api/orders} — заказы из основной dwh.</li>
 *   <li>{@code POST /api/orders} — создаёт заказ в dwh; параллельно валидирует валюту
 *       по справочной БД {@code dictionary} и пишет событие в БД {@code history}.</li>
 *   <li>{@code GET /api/dictionary/currencies} — список валют.</li>
 *   <li>{@code GET /api/history/recent?limit=10} — недавние события.</li>
 * </ul>
 *
 * <h2>Подводный камень</h2>
 * Здесь намеренно НЕТ распределённой транзакции: запись в три БД одной транзакцией
 * требует XA или паттерна Saga/Outbox. В демо мы записываем в каждую БД отдельно —
 * это иллюстрирует естественное ограничение «нескольких источников данных».
 */
@RestController
@RequestMapping("/api")
public class OrdersController {

    private final OrderDao orders;
    private final DictionaryDao dictionary;
    private final HistoryDao history;

    public OrdersController(OrderDao orders, DictionaryDao dictionary, HistoryDao history) {
        this.orders = orders;
        this.dictionary = dictionary;
        this.history = history;
    }

    @GetMapping("/orders")
    public List<OrderDao.Order> all() {
        return orders.findAll();
    }

    @PostMapping("/orders")
    public ResponseEntity<Map<String, Object>> create(@RequestBody CreateOrderRequest req) {
        boolean validCurrency = dictionary.findCurrencies().stream()
                .anyMatch(c -> c.code().equalsIgnoreCase(req.currency()));
        if (!validCurrency) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "unknown currency",
                    "currency", req.currency()));
        }
        long id = orders.create(req.customer(), req.currency().toUpperCase(), req.amount());
        history.record(id, "CREATED");
        return ResponseEntity.created(URI.create("/api/orders/" + id))
                .body(Map.of("id", id));
    }

    @GetMapping("/dictionary/currencies")
    public List<DictionaryDao.Currency> currencies() {
        return dictionary.findCurrencies();
    }

    @GetMapping("/history/recent")
    public List<HistoryDao.Event> recent(@RequestParam(defaultValue = "10") int limit) {
        return history.recent(limit);
    }

    /**
     * Запрос на создание заказа.
     */
    public record CreateOrderRequest(String customer, String currency, BigDecimal amount) {}
}
