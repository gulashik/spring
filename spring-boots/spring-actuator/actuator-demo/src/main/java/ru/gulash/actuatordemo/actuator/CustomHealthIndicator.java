package ru.gulash.actuatordemo.actuator;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Пользовательский индикатор здоровья (HealthIndicator).
 * Spring Boot Actuator автоматически регистрирует все бины типа HealthIndicator
 * для отображения в health эндпоинте.
 */
@Component
public class CustomHealthIndicator
    implements HealthIndicator /* todo имплементируем */{

    private final Random random = new Random();

    /**
     * Метод возвращает состояние здоровья компонента.
     *
     * @return объект Health с информацией о здоровье компонента
     */
    @Override
    public Health /*todo возврашщаем Health*/ health() {
        // Для демонстрации создаем случайное состояние здоровья
        if (random.nextInt(10) > 8) {
            return Health.down()
                .withDetail("reason", "Случайная проблема для демонстрации")
                .withDetail("time", System.currentTimeMillis())
                .build();
        }

        // todo Дополнительная детальная информация о состоянии
        Map<String, Object> details = new HashMap<>();
        details.put("version", "1.0.0");
        details.put("memory", Runtime.getRuntime().freeMemory());
        details.put("availableProcessors", Runtime.getRuntime().availableProcessors());

        return Health.up()
            .withDetails(details)
            .build();
    }
}