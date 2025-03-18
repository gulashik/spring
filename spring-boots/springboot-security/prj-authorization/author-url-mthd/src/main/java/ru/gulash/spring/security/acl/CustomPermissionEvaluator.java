package ru.gulash.spring.security.acl;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;

import java.io.Serializable;

// todo Для того чтобы использовать hasPermission, необходимо настроить PermissionEvaluator.
@Configuration
public class CustomPermissionEvaluator implements PermissionEvaluator/*todo наследуемся*/ {

    // todo будет вызываться, например через @PreAuthorize("hasRole('USER') && hasPermission(#userDetails,'READ')")
    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        // Логика проверки прав доступа
        // authentication - информация о текущем пользователе
        // targetDomainObject - объект, к которому проверяется доступ (в данном случае customer)
        // permission - право доступа (например 'READ')

        // Пример логики:
//        if (targetDomainObject instanceof Customer) {
//            Customer customer = (Customer) targetDomainObject;
//            // Проверка прав доступа
//            return checkPermission(authentication, customer, permission.toString());
//        }
//        return false;

        return true;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        // Этот метод используется, если проверка прав осуществляется по ID объекта
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
