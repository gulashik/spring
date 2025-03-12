package ru.gulash.spring.security;

import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Service;

import java.util.List;

//@Service // можно не ставить тут пока не нужно
public class AuthorizationService {
    public AuthorizationManager<RequestAuthorizationContext> hasAuthorizationGrant(String role) {
        return (authentication, context) -> {
            // Проверяем, есть ли у пользователя нужная роль или вышестоящая роль
            String currentUserName = authentication.get().getName();
            List<String> roles = getRolesForUser(currentUserName);
            boolean hasAccess = roles.contains(role);
            return new AuthorizationDecision(hasAccess);
        };
    }

    public List<String> getRolesForUser(String username) {
        // Возвращаем список ролей для пользователя, учитывая иерархию
        if ("admin".equals(username)) {
            return List.of("ADMIN", "MANAGER", "USER");
        } else if ("manager".equals(username)) {
            return List.of("MANAGER", "USER");
        } else if ("user".equals(username)) {
            return List.of("USER");
        }
        return List.of();
    }
}
