package com.sprboot.annotation;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class EventPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;

    public EventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    // Метод для создания события
    public void publishEvent() {
        // Entity как-то работаем с ним или получаем
        EntityCls myEntity = new EntityCls("my entity");
        // Дополнительная нужная инфо
        String someNeededInfo = "some needed info";

        // Кладём Entity в Event
        EntityEvent event = new EntityEvent(
                myEntity,
                someNeededInfo
        );
        // и публикуем
        applicationEventPublisher.publishEvent(event);
    }
}
