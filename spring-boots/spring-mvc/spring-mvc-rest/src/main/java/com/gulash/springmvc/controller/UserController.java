package com.gulash.springmvc.controller;

import com.gulash.springmvc.service.UserService;
import com.gulash.springmvc.entity.User;
import com.gulash.springmvc.exception.UserException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/common_path") // Устанавливает общие префиксы путей для методов;
public class UserController {
    @Autowired
    UserService userService; // Сервис(работа с бизнес логикой). Можно получать через конструктор

    // Указывает, что URL путь будет ассоциирован с POST запросом
    // POST http://localhost:7070/layerapp/common_path/user
    // {
    //    "id": 111,
    //    "username": "gulman",
    //    "firstName": "Max",
    //    "lastName": "Gulash"
    // }
    // Обязательное использование потокобезопасных(thread-safe) объектов, чтобы избежать неожиданных изменений.
    @PostMapping("/user")
    public User saveUser(@RequestBody /*связывает Аргумет с BODY запроса*/ User user /*Преобразованный JSON объект*/) {
        //
        User createdUser = userService.save( new User( user.getId(), user.getUsername(), user.getFirstName(), user.getLastName(), user.getHireDate()) );

        return createdUser;
    }

    // Указывает, что URL путь будет ассоциирован с POST запросом
    //  Шаблон - <api-path>?<param>=<value>&<param>=<value>
    //  Пример - POST http://localhost:7070/layerapp/common_path/userLink?id=2&username=kudri&firstName=Seniya&lastName=Ars
    // Обязательное использование потокобезопасных(thread-safe) объектов, чтобы избежать неожиданных изменений.
    @PostMapping("/userLink")
    public User saveUser(
            @RequestParam("id") long id, // @RequestParam("имя_из_POST") связывает переменные из POST с аргументом метода
            @RequestParam("username") String username,
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestHeader(value = "accept", required = false) String acceptHeader, /*Получаем информацию из HTTP Header-а*/
            @CookieValue(value = "JSESSIONID", required = false) String cookieValue, /*Значение cookie*/
            HttpServletRequest httpRequest // Bean свойства текущего запроса
            )
    {
        System.out.println(acceptHeader);
        System.out.println(cookieValue);
        // HttpServletRequest
        Object name = httpRequest.getAttribute("org.springframework.web.servlet.HandlerMapping.bestMatchingPattern");
        String requestParameter = httpRequest.getParameter("firstName"); // Аналог @RequestParam("firstName")
        String accept = httpRequest.getHeader("accept"); // Аналог @RequestHeader("accept")
        Cookie[] cookies = httpRequest.getCookies(); // Аналог  @CookieValue("ХХХХ")

        User createdUser = userService.save( new User( id, username, firstName, lastName, LocalDate.now()) );

        return createdUser;
    }

    // Указывает, что URL путь будет ассоциирован с POST запросом
    //  Шаблон - <api-path>?<param>=<value>&<param>=<value>
    //  Пример - POST http://localhost:7070/layerapp/common_path/userModelTest?id=2&username=kudri&firstName=Seniya&lastName=Ars&hireDate=2020-10-10
    // Обязательное использование потокобезопасных(thread-safe) объектов, чтобы избежать неожиданных изменений.
    @PostMapping("/userModelTest")
    public User userModel(
            @ModelAttribute("nameInModel") User userModel, // Аннотацию можно опустить. @ModelAttribute - по входным параметрам создаёт экземпляр класса(?id=2&username=kudri&firstName=Seniya&lastName=Ars)
            HttpServletRequest httpRequest, // Bean свойства текущего запроса
            Model model
    )
    {
        // На вход получили смапленый экземпляр класса по входному типу
        User createdUser = userModel;
        Object name = model.getAttribute("nameInModel"); // Можно увидеть в моделе сущность.

        return createdUser;
    }

    // Указывает, что URL путь будет ассоциирован с GET запросом
    //  Шаблон - <api-path>/{param}
    // Пример GET http://localhost:7070/layerapp/common_path/user/ЧИСЛО
    @GetMapping("/user/{id}")
    public User getUser(@PathVariable/*cвязываем параметр {some_param} c аргументом метода*/ long id) {

        // ResponseStatusException - Не требует дополнительных классов. Возвращает статус и ответ.
        if (id == 1) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ответ по HTTP коду " + id);

        // @ResponseStatus + Custom Exception - Требует реализации дополнительного класса. Возвращает статус и ответ.
        if (id == 2) throw new UserException("Ответ по HTTP коду " + id);
        return userService.findUserById(id);
    }

    // Пример GET http://localhost:7070/layerapp/common_path/users
    @GetMapping("/users")
    public List<User> getUsers(HttpServletRequest httpRequest) {

        return userService.findAll();
    }
}

