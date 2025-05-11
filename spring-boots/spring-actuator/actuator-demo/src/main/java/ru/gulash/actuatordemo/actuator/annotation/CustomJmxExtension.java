package ru.gulash.actuatordemo.actuator.annotation;

import org.springframework.boot.actuate.endpoint.annotation.DeleteOperation;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.boot.actuate.endpoint.jmx.annotation.EndpointJmxExtension;
import org.springframework.stereotype.Component;

/**
 * Расширение для CustomJmxEndpoint, которое изменяет поведение базового JMX эндпоинта.
 * 
 * Класс использует аннотацию {@code @EndpointJmxExtension} для указания, что он расширяет
 * функциональность {@link CustomJmxEndpoint}. Это позволяет модифицировать данные
 * при чтении/записи, не изменяя оригинальный эндпоинт.
 */
@Component
@EndpointJmxExtension(endpoint = CustomJmxEndpoint.class)
public class CustomJmxExtension {

    /**
     * Ссылка на оригинальный JMX эндпоинт, функциональность которого расширяется.
     */
    private final CustomJmxEndpoint delegate;

    /**
     * Конструктор для внедрения зависимости оригинального эндпоинта.
     * 
     * @param delegate экземпляр оригинального JMX эндпоинта
     */
    public CustomJmxExtension(CustomJmxEndpoint delegate) {
        this.delegate = delegate;
    }

    /**
     * Получает модифицированное сообщение, добавляя префикс "[JMX]".
     * Этот метод вызывается при операции чтения через JMX.
     * 
     * @return модифицированное сообщение с префиксом
     */
    @ReadOperation
    public String getModifiedMessage() {
        return "[JMX] " + delegate.getMessage();
    }

    /**
     * Устанавливает модифицированное сообщение, добавляя префикс "Modified: ".
     * Этот метод вызывается при операции записи через JMX.
     * 
     * @param msg исходное сообщение, которое будет модифицировано
     */
    @WriteOperation
    public void setModifiedMessage(@Selector String msg) {
        delegate.setMessage("Modified: " + msg);
    }

    /**
     * Сбрасывает значение сообщения до начального состояния,
     * используя метод сброса оригинального эндпоинта.
     * Этот метод вызывается при операции удаления через JMX.
     */
    @DeleteOperation
    public void clearMessage() {
        delegate.reset();
    }
}