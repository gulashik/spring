package ru.gulash.actuatordemo.actuator;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Пользовательский Actuator эндпоинт.
 * Аннотация @Endpoint регистрирует новый эндпоинт под названием "custom"
 * для доступа по URL: /actuator/custom
 */
@Component
@Endpoint(id = "custom")
public class CustomActuatorEndpoint {

    private final Map<String, Object> features = new ConcurrentHashMap<>();

    public CustomActuatorEndpoint() {
        // Добавляем несколько значений по умолчанию
        features.put("feature1", true);
        features.put("feature2", false);
        features.put("maxConnections", 100);
    }

    /**
     * Операция чтения (HTTP GET) для всех значений
     * Доступно по: GET /actuator/custom
     */
    @ReadOperation
    public Map<String, Object> features() {
        return new HashMap<>(features);
    }

    /**
     * Операция чтения для конкретного значения
     * Доступно по: GET /actuator/custom/{featureName}
     *
     * @param name имя запрашиваемого параметра
     * @return значение параметра или сообщение об ошибке
     */
    @ReadOperation
    public Object feature(@Selector String name) {
        if (features.containsKey(name)) {
            return features.get(name);
        } else {
            return Map.of("error", "Feature not found: " + name);
        }
    }

    /**
     * Операция записи (HTTP POST) для установки значения
     * Доступно по: POST /actuator/custom/{featureName}
     * С телом запроса, содержащим значение
     *
     * @param name имя параметра
     * @param value значение параметра
     * @return результат операции
     */
    @WriteOperation
    public Object configureFeature(@Selector String name, Object value) {
        features.put(name, value);
        return Map.of("result", "Feature '" + name + "' set to: " + value);
    }
}