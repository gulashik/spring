package ru.otus.spring.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.spring.rest.PagesController;
import ru.otus.spring.security.SecurityConfiguration;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PagesController.class)
// todo настройки по проекту класс с security-конфигом
@Import(SecurityConfiguration.class)
public class PagesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("should works only with authenticated request")
    // todo Использовать аутентифицированного пользователя Вариант 1
/*    @WithMockUser(
        username = "admin",
        authorities = {"ROLE_ADMIN"}
    )*/
    public void testAuthenticatedOnAdmin() throws Exception {
        mockMvc.perform(
                get("/authenticated")
                // todo Использовать аутентифицированного пользователя Вариант 2
                .with(user("admin").authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
            )
            .andExpect(status().isOk());
    }
}
