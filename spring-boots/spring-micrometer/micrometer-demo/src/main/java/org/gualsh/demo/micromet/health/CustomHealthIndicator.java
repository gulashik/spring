package org.gualsh.demo.micromet.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Пользовательский индикатор здоровья системы (Health Indicator).
 * Демонстрирует создание собственного компонента для Actuator Health Endpoint.
 */
@Component
public class CustomHealthIndicator implements HealthIndicator {

    private static final Logger logger = LoggerFactory.getLogger(CustomHealthIndicator.class);
    private final Random random = new Random();

    // Имитация состояния внешней системы
    private boolean externalSystemAvailable = true;
    private int responseTime = 0;
    private int errorCount = 0;
    private long lastCheckTimestamp = System.currentTimeMillis();

    @Override
    public Health health() {
        logger.debug("Checking custom health status");

        // Обновляем показатели "внешней системы"
        updateExternalSystemStatus();

        // Формируем ответ в зависимости от состояния системы
        if (externalSystemAvailable && errorCount < 5 && responseTime < 200) {
            // Система полностью работоспособна
            return buildHealthUp();
        } else if (externalSystemAvailable && (errorCount >= 5 || responseTime >= 200)) {
            // Система работает, но с проблемами
            return buildHealthDegraded();
        } else {
            // Система не работает
            return buildHealthDown();
        }
    }

    /**
     * Имитирует обновление состояния некой внешней системы.
     * В реальном приложении здесь был бы код проверки доступности
     * внешних сервисов, баз данных и т.д.
     */
    private void updateExternalSystemStatus() {
        // Периодически меняем состояние для демонстрации разных статусов
        if (System.currentTimeMillis() - lastCheckTimestamp > 30000) {
            // Меняем состояние каждые 30 секунд
            externalSystemAvailable = random.nextDouble() > 0.2; // 20% шанс недоступности
            responseTime = 50 + random.nextInt(200); // от 50 до 250 мс
            errorCount = random.nextInt(10); // от 0 до 9 ошибок
            lastCheckTimestamp = System.currentTimeMillis();

            logger.debug("Updated external system status: available={}, responseTime={}ms, errorCount={}",
                externalSystemAvailable, responseTime, errorCount);
        }
    }

    /**
     * Строит ответ о полной работоспособности.
     *
     * @return Объект Health с информацией о состоянии
     */
    private Health buildHealthUp() {
        Map<String, Object> details = new HashMap<>();
        details.put("externalSystem", "available");
        details.put("responseTime", responseTime + "ms");
        details.put("errorCount", errorCount);
        details.put("lastChecked", lastCheckTimestamp);

        return Health.up()
            .withDetails(details)
            .build();
    }

    /**
     * Строит ответ о частичной работоспособности (деградация).
     *
     * @return Объект Health с информацией о деградированном состоянии
     */
    private Health buildHealthDegraded() {
        Map<String, Object> details = new HashMap<>();
        details.put("externalSystem", "degraded");
        details.put("responseTime", responseTime + "ms");
        details.put("errorCount", errorCount);
        details.put("lastChecked", lastCheckTimestamp);

        if (responseTime >= 200) {
            details.put("warning", "High response time detected");
        }

        if (errorCount >= 5) {
            details.put("warning", "Elevated error count detected");
        }

        // Используем статус UNKNOWN для деградированного состояния
        return Health.unknown()
            .withDetails(details)
            .build();
    }

    /**
     * Строит ответ о неработоспособности.
     *
     * @return Объект Health с информацией о неработающей системе
     */
    private Health buildHealthDown() {
        Map<String, Object> details = new HashMap<>();
        details.put("externalSystem", "unavailable");
        details.put("lastChecked", lastCheckTimestamp);
        details.put("error", "External system is not responding");

        return Health.down()
            .withDetails(details)
            .build();
    }
}