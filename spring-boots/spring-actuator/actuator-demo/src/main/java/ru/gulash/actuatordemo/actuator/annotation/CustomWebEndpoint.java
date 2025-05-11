package ru.gulash.actuatordemo.actuator.annotation;

import org.springframework.boot.actuate.endpoint.annotation.*;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Пользовательский Actuator эндпоинт для web-интерфейса.
 * Позволяет управлять набором параметров (features) через HTTP.
 * 
 * Аннотация {@code @Endpoint} регистрирует новый эндпоинт под названием "custom"
 * для доступа по URL: /actuator/custom
 */
@Component
@Endpoint(id = "custom")
public class CustomWebEndpoint {

    /**
     * Хранилище параметров в виде ключ-значение.
     * Используется потокобезопасная реализация для возможности
     * одновременного доступа из разных потоков.
     */
    private final Map<String, Object> features = new ConcurrentHashMap<>();

    /**
     * Конструктор, инициализирующий хранилище параметров
     * несколькими значениями по умолчанию.
     */
    public CustomWebEndpoint() {
        features.put("feature1", true);
        features.put("feature2", false);
        features.put("maxConnections", 100);
    }

    /**
     * Операция чтения (HTTP GET) для получения всех параметров.
     * Создает и возвращает копию хранилища параметров.
     * 
     * Доступно по: GET /actuator/custom
     * 
     * @return копия карты всех параметров
     */
    @ReadOperation
    public Map<String, Object> features() {
        return new HashMap<>(features);
    }

    /**
     * Операция чтения для получения конкретного параметра по имени.
     * 
     * Доступно по: GET /actuator/custom/{featureName}
     * 
     * @param name имя запрашиваемого параметра
     * @return значение параметра или сообщение об ошибке, если параметр не найден
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
     * Операция записи (HTTP POST) для установки значения параметра.
     * 
     * Доступно по: POST /actuator/custom/{featureName}
     * С телом запроса, содержащим значение
     * 
     * @param name имя параметра
     * @param value значение параметра
     * @return результат операции в виде карты с сообщением
     */
    @WriteOperation
    public Object configureFeature(@Selector String name, Object value) {
        features.put(name, value);
        return Map.of("result", "Feature '" + name + "' set to: " + value);
    }
}