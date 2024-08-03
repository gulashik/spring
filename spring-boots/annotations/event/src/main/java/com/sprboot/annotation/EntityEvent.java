package com.sprboot.annotation;

import org.springframework.context.ApplicationEvent;

// Сущность-Event
public class EntityEvent
    // наследуемся, чтобы подключиться в систему Event-listener-ов Spring
    extends ApplicationEvent // или EventObject
{
    // Любые нужные по смыслу поля
    private final String eventType; // Можно сделать Enum

    // Конструктор. Получаем 1 Entity 2 Нужные атрибуты(если нужно что-то)
    public EntityEvent(EntityCls source, String eventType) {
        super(source);/*Entity-Event-а передаём в систему Event-listener-ов Spring*/
        this.eventType = eventType;
    }

    // Создаём Getter-ы для возможности получения в Listener-е
    public String getEventType() {
        return eventType;
    }
}
