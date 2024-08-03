package com.sprboot.annotation;

import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
public class ListenerThroughAnnotation {

    @EventListener
    @Order(1) // Можно указывать порядок срабатывания
    public void onApplicationEvent1(/*Класс-Event*/EntityEvent event) {
        // Нужные действия
        System.out.println("Application Event 1 Received " + event);
        EntityCls entityCls = (EntityCls) event.getSource(); // Может получить Entity
        System.out.println(entityCls);
    }

    @EventListener
    @Order(2) // Можно указывать порядок срабатывания
    public void onApplicationEvent2(/*Класс-Event*/EntityEvent event) {
        // Нужные действия
        System.out.println("Application Event 2 Received " + event);
        EntityCls entityCls = (EntityCls) event.getSource(); // Может получить Entity
        System.out.println(entityCls);
    }
}
