package ru.gulash.spring.security.acl;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;

@Configuration
public class AclConfig {
    // todo MethodSecurityExpressionHandler для добавления своих реализаций по ACL
    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        // todo своя реализация hasPermission для @Pre/Post аннотаций
        expressionHandler.setPermissionEvaluator(new CustomPermissionEvaluator());

        return expressionHandler;
    }
}
