package ru.gulash.actuatordemo.actuator.annotation;

import org.springframework.boot.actuate.endpoint.annotation.DeleteOperation;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.boot.actuate.endpoint.web.annotation.EndpointWebExtension;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Расширение для CustomWebEndpoint, которое изменяет поведение базового web-эндпоинта.
 * 
 * Класс использует аннотацию {@code @EndpointWebExtension} для указания, что он расширяет
 * функциональность {@link CustomWebEndpoint}. Это позволяет изменить формат возвращаемых 
 * данных при различных HTTP-операциях.
 */
@Component
@EndpointWebExtension(endpoint = CustomWebEndpoint.class)
public class CustomWebExtension {

    /**
     * Ссылка на оригинальный web-эндпоинт, функциональность которого расширяется.
     */
    private final CustomWebEndpoint delegate;

    /**
     * Конструктор для внедрения зависимости оригинального эндпоинта.
     * 
     * @param delegate экземпляр оригинального web-эндпоинта
     */
    public CustomWebExtension(CustomWebEndpoint delegate) {
        this.delegate = delegate;
    }

    /**
     * Операция чтения, которая оборачивает результат оригинального метода features()
     * в дополнительную структуру с ключом "web-counter-value".
     * 
     * Доступно по: GET /actuator/custom
     * 
     * @return данные в модифицированном формате
     */
    @ReadOperation
    public Map<String, Object> read() {
        return Map.of("web-counter-value", delegate.features());
    }

    /**
     * Операция записи, которая получает и возвращает значение конкретного параметра.
     * 
     * Доступно по: POST /actuator/custom/{paramName}
     * 
     * @param paramName имя параметра для получения
     * @return значение параметра в модифицированном формате
     */
    @WriteOperation
    public Map<String, Object> getParameter(@Selector String paramName) {
        return Map.of("web-parameter-value", delegate.feature(paramName));
    }

    /**
     * Операция удаления, которая устанавливает специальное значение
     * параметра "Delete" и возвращает результат в модифицированном формате.
     * 
     * Доступно по: DELETE /actuator/custom
     * 
     * @return результат операции в модифицированном формате
     */
    @DeleteOperation
    public Map<String, Object> reset() {
        return Map.of("web-reset-result", delegate.configureFeature("Delete", "Reset by @DeleteOperation"));
    }
}