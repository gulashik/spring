package com.sprboot.ann.condition.config.spring;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class ConditionImpl
        /*todo Не очень удобно. Condition это от Spring, не SpringBoot*/
        implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        // context - доступ к элементам контекста
        context.getBeanFactory(); // Все Bean-ы
        context.getRegistry(); // Bean definition
        context.getResourceLoader(); //
        context.getEnvironment(); // Переменные среды

        // metadata - meta-информация по аннотации @Conditional и где она установлена
        // metadata.xxx();
        //((SimpleAnnotationMetadata) metadata).getClassName()

        return true; // todo по результатам нужно boolean
    }
}
