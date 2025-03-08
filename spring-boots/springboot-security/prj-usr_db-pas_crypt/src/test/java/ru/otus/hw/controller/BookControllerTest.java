package ru.otus.hw.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.otus.hw.model.dto.BookDto;
import ru.otus.hw.service.BookService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.otus.hw.object.TestObjects.getDbBooks;


@WebMvcTest(BookController.class)
class BookControllerTest {

    private final List<BookDto> dbBookDtos = getDbBooks();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;


    @WithMockUser(
        username = "user",
        authorities = {"ROLE_USER"}
    )
    @DisplayName("should get all books")
    @Test
    void getBook() throws Exception {
        BookDto expectedBook = dbBookDtos.get(0);

        when(bookService.findAll()).thenReturn(List.of(expectedBook));

        MvcResult result = mockMvc.perform(
                get("/")
            )
            .andExpect(status().isOk())
            .andExpect(content().contentType("text/html;charset=UTF-8"))
            .andReturn();

        List<BookDto> books = (List<BookDto>) result.getModelAndView().getModel().get("books");
        assertThat(books).isNotNull();
        assertThat(books.size()).isEqualTo(1);

        assertThat(books.get(0)).isEqualTo(expectedBook);
    }


    @WithMockUser(
        username = "user",
        authorities = {"ROLE_USER"}
    )
    @DisplayName("should edit book")
    @Test
    void editBook() throws Exception {
        BookDto expectedBook = dbBookDtos.get(0);

        when(bookService.findById(expectedBook.getId())).thenReturn(expectedBook);

        mockMvc.perform(
                get("/edit")
                    .param("id", String.valueOf(expectedBook.getId()))
            )
            .andExpect(status().isOk())
            .andExpect(content().contentType("text/html;charset=UTF-8"));

        Mockito.verify(bookService, Mockito.times(1)).findById(expectedBook.getId());
    }

    @DisplayName("should works only with authenticated request")
    @Test
    void shouldAccessOnlyAuthenticatedUser() throws Exception {
        // authenticated user - got access
        mockMvc.perform(
                get("/")
                    .with(user("user").authorities(new SimpleGrantedAuthority("ROLE_USER")))
            )
            .andExpect(status().isOk());

        // non-authenticated user - access denied
        mockMvc.perform(
                get("/")
            )
            .andExpect(status().isUnauthorized());
    }
}