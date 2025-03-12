package ru.gulash.spring.service;

import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.prepost.PreFilter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MySecureService {

    // todo @PreAuthorize - проверяет условие до выполнения метода.
    //  Можно использовать SpEL, Встроенные выражения Spring Security
    //  Если условие не выполняется, метод не будет вызван, и Spring Security выбросит исключение AccessDeniedException.
    /*
    SpEL
        #userId == authentication.principal.id
        returnObject.get(0) == authentication.principal.username
        Java
            {new java.util.Random().nextBoolean()}
    Доступ к параметрам метода
        Например
            @PreAuthorize("#id == authentication.principal.id")
            public void someMethod(Long id) {
    Кастомные проверки
        @PreAuthorize("@mySecurityService.hasAccess(#user)")
        public void someMethod(User user) {
    Встроенные выражения Spring Security(org.springframework.security.access.expression.SecurityExpressionRoot):
        hasRole('ROLE') — проверяет, есть ли у пользователя указанная роль.
        hasAnyRole('ROLE1', 'ROLE2') — проверяет, есть ли у пользователя хотя бы одна из указанных ролей.
        hasAuthority('AUTHORITY') — проверяет, есть ли у пользователя указанное право (без автоматического добавления ROLE_).
        hasAnyAuthority('AUTHORITY1', 'AUTHORITY2') — проверяет, есть ли у пользователя хотя бы одно из указанных прав.
        isAuthenticated() — проверяет, аутентифицирован ли пользователь.
        isAnonymous() — проверяет, является ли пользователь анонимным.
        isFullyAuthenticated() — проверяет, аутентифицирован ли пользователь без использования "запомнить меня".
        permitAll() — разрешает доступ всем.
        denyAll() — запрещает доступ всем.
        principal — предоставляет доступ к объекту текущего аутентифицированного пользователя.
        authentication — предоставляет доступ к объекту Authentication.
    */
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
    //  Можно использовать SpEL(см. выше примеры из @PreAuthorize)
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

    // todo @PreFilter - используется для предварительно фильтрации коллекции
    //  Обязательно НА ВХОД нужна MUTABLE коллекция, т.к. она будет модифицироваться
    //  filterObject представляет собой каждый элемент коллекции
    //  @myService.methodName(X,Y) используем внешний сервис
    @PreFilter("filterObject.equals(authentication.name)")
    // @PreFilter("filterObject.owner == authentication.name") // если сложный тип можно использовать поля filterObject.filedName
    // @PreFilter("hasRole('ADMIN') or filterObject.owner == authentication.name") // если сложный тип можно использовать поля filterObject.filedName
    // @PreFilter("@mySecurityService.canAccessDocument(filterObject, authentication.principal)")
    public List<String> preFilter(List<String> lst) {
        System.out.println("Income this method: " + lst);
        return lst;
    }
    /*
        listBefore before: [user, admin, user]
        Income this method: [user, user]
        listAfter: [user, user]
    */

    // todo @PostFilter - используется для фильтрации коллекций после выполнения метода.
    //  Она позволяет удалить из возвращаемой коллекции элементы, которые не соответствуют заданному условию.
    //  Обязательно НА ВХОД нужна MUTABLE коллекция, т.к. она будет модифицироваться
    //  filterObject представляет собой каждый элемент коллекции
    //  filterIndex — это индекс текущего элемента в коллекции (начинается с 0).
    //  @myService.methodName(X,Y) используем внешний сервис
    @PostFilter("filterObject.equals(authentication.name)")
    // @PostFilter("filterIndex < 10")
    // @PostFilter("hasRole('ADMIN') or filterObject.owner == authentication.name")
    // @PostFilter("filterIndex < 10")
    // @PostFilter("@mySecurityService.canAccessDocument(filterObject, authentication.principal)")
    public List<String> postFilter(List<String> lst) {
        System.out.println("Income this method: " + lst);
        return lst;
    }
    /*
        listBeforePost: [user, admin, user]
        Income this method: [user, admin, user]
        listAfterPost: [user, user]
    */
}
