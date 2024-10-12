package com.gulash.springmvc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gulash.springmvc.entity.User;
import com.gulash.springmvc.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)/*Нужный контроллер*/
// @WebMvcTest + @Import(НужныйBean.class) + @Autowired
//  @WebMvcTest - Поднимается только минимально необходимый контекст и Не подходит для тестов, требующих полноценного контекста приложения
// или
// @SpringBootTest @AutoConfigureMockMvc
//  @SpringBootTest + @AutoConfigureMockMvc
//      @SpringBootTest - Поднимает полноценный контекст Spring, включая все бины, сервисы и репозитории.
//      @AutoConfigureMockMvc - Автоматически конфигурирует MockMvc и делает его доступным для внедрения через @Autowired.
//      В итоге получаем MockMvc для работы с полноценным контекстом Spring, включая все бины, сервисы и репозитории.
class UserControllerTest {
    @Autowired
    private ApplicationContext applicationContext;

    // MockMvc позволяет тестировать веб-слой, не поднимая сервер, что ускоряет выполнение тестов.
    // Запросы и ответы обрабатываются в памяти, моделируя реальное поведение сервера.
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void getUser() throws Exception {
        // Arrange
        long userId = 3L;
        User expectedResultVariable = new User(
            userId,
            "username"+userId,
            "firstName"+userId,
            "lastName"+userId,
            LocalDate.now()
        );
        when(userService.findUserById(userId)).thenReturn(expectedResultVariable);

        // Act & Assert
        // mockMvc.perform() - Отправляет имитированный HTTP-запрос.
        // get() - является статическим методом из класса MockMvcRequestBuilders и используется для создания HTTP-запроса типа GET в тесте.
        // post() — для POST-запросов.
        // put() — для PUT-запросов.
        // delete() — для DELETE-запросов.
        // patch() — для PATCH-запросов.
        // options() — для OPTIONS-запросов.
        String jsonContent = mapper.writeValueAsString(expectedResultVariable);
        mockMvc.perform(
                get("/common_path/user/{id}", userId)
                    // передаём параметры
                    //.param("name", "John").param("age", "30")
                    // заголовки
                    //.header("Authorization", "Bearer token")
                    // 'Content-Type' header
                    .contentType(MediaType.APPLICATION_JSON)
                    //.contentType(MediaType.APPLICATION_JSON).content("{\"key\":\"value\"}")
                    // нужен spring-boot-starter-security
                    //.with(user("user").password("password").roles("USER"))
            )
            // andExpect() - Проверяет ожидаемые результаты, такие как статус ответа и содержимое.
            // status()
            //  status().isOk()
            // content()
            //  content().json(mapper.writeValueAsString(expectedResultVariable))
            //  content().string("Hello, World!")
            // jsonPath().value() - смотрим на полученный JSON
            //  jsonPath("$.id").value(userId)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(userId))
            .andExpect(jsonPath("$.firstName").value("firstName"+userId))
            .andExpect(content().json(jsonContent));
    }
}