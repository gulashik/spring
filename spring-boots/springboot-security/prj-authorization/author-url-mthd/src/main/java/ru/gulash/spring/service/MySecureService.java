package ru.gulash.spring.service;

import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MySecureService {

    // todo @PreAuthorize - проверяет условие до выполнения метода.
    //  Можно использовать SpEL
    //  Если условие не выполняется, метод не будет вызван, и Spring Security выбросит исключение AccessDeniedException.
    //@PreAuthorize("hasRole('ROLE_USER') && {new java.util.Random().nextBoolean()}")
    @PreAuthorize("hasRole('USER')")
    //@PreAuthorize("hasRole('USER')")
    public String onlyUser() {
        return "Congratulations! @PreAuthorize has access to the user";
    }
    // #userId == authentication.principal.id — метод доступен только тому пользователю, чей ID совпадает с переданным параметром userId.
    @PreAuthorize("#userId == authentication.principal.id")
    public void userSpecificMethod(Long userId) {
        System.out.println("This method is only for the user with ID: " + userId);
    }


    // todo @PostAuthorize - проверяет условие после выполнения метода.
    //  Можно использовать SpEL
    //  Если условие не выполняется, результат метода не будет возвращён, и Spring Security выбросит исключение AccessDeniedException.
    // authentication.principal.username - текущий пользователь
    @PostAuthorize("returnObject.get(0) == authentication.principal.username")
    public List<String> getValidUsers(Long someId) {
        // Получаем объект аутентификации
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Логируем информацию об аутентификации
        System.out.println("Authentication: " + authentication);
        System.out.println("Principal: " + authentication.getPrincipal());
        System.out.println("Username: " + authentication.getName());

        // что-то возвращаем
        return List.of("user") ;
    }

    // todo @Secured и @RolesAllowed - проверяет, имеет ли пользователь указанную роль. Она проще, чем @PreAuthorize, и поддерживает только проверку ролей.
    @Secured("ADMIN")
    public void onlyAdmin() {
    }
}
