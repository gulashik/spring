package ru.gulash.actuatordemo.actuator.annotation;

import org.springframework.boot.actuate.endpoint.annotation.DeleteOperation;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.boot.actuate.endpoint.jmx.annotation.JmxEndpoint;
import org.springframework.stereotype.Component;

/**
 * Пользовательский JMX эндпоинт для Spring Boot Actuator.
 * Позволяет управлять текстовым сообщением через JMX-интерфейс.
 * 
 * Аннотация {@code @JmxEndpoint} регистрирует эндпоинт с идентификатором "jmxcustom",
 * который будет доступен только через JMX и не будет доступен через HTTP.
 */
@Component
@JmxEndpoint(id = "jmxcustom")
public class CustomJmxEndpoint {

    /**
     * Текстовое сообщение, которым можно управлять через JMX.
     * Значение по умолчанию: "initial".
     */
    private String message = "initial";

    /**
     * Получает текущее значение сообщения.
     * Этот метод вызывается при операции чтения через JMX.
     * 
     * @return текущее значение сообщения
     */
    @ReadOperation
    public String getMessage() {
        return message;
    }

    /**
     * Устанавливает новое значение сообщения.
     * Этот метод вызывается при операции записи через JMX.
     * 
     * @param msg новое значение сообщения
     */
    @WriteOperation
    public void setMessage(@Selector String msg) {
        this.message = msg;
    }

    /**
     * Сбрасывает значение сообщения до начального состояния "initial".
     * Этот метод вызывается при операции удаления через JMX.
     */
    @DeleteOperation
    public void reset() {
        this.message = "initial";
    }
}