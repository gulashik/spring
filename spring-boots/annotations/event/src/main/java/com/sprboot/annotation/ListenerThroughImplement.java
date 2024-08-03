package com.sprboot.annotation;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class ListenerThroughImplement
        // реализуем с нужным типом Event
        implements ApplicationListener<EntityEvent>
{

    @Override
    public void onApplicationEvent(EntityEvent event/*с нужным типом Event*/) {
        // Нужные действия
        System.out.println("Application Event 3 Received " + event);
        EntityCls entityCls = (EntityCls) event.getSource(); // Может получить Entity
        System.out.println(entityCls);
    }
}
