package org.gualsh.demo.restclient.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.gualsh.demo.restclient.dto.CreateUserRequest;
import org.gualsh.demo.restclient.dto.User;
import org.gualsh.demo.restclient.dto.UpdateUserRequest;
import org.gualsh.demo.restclient.dto.Post;
import org.gualsh.demo.restclient.dto.HttpBinResponse;
import org.gualsh.demo.restclient.service.RestClientService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Тесты REST контроллера с использованием MockMvc.
 *
 * Проверяют корректность работы HTTP endpoints,
 * валидацию входных данных и обработку различных сценариев.
 *
 * @author Demo Author
 */
@WebMvcTest(RestClientController.class)
@DisplayName("RestClientController Tests")
class RestClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RestClientService restClientService;

    // =================================
    // Тесты GET endpoints
    // =================================

    @Test
    @DisplayName("GET /api/demo/users - должен вернуть список пользователей")
    void shouldReturnAllUsers() throws Exception {
        // Arrange
        List<User> users = List.of(
            User.builder().id(1L).name("John Doe").username("johndoe").email("john@example.com").build(),
            User.builder().id(2L).name("Jane Smith").username("janesmith").email("jane@example.com").build()
        );
        when(restClientService.getAllUsers()).thenReturn(users);

        // Act & Assert
        mockMvc.perform(get("/api/demo/users"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].name").value("John Doe"));

        verify(restClientService).getAllUsers();
    }

    @Test
    @DisplayName("GET /api/demo/users/{id} - должен вернуть пользователя по ID")
    void shouldReturnUserById() throws Exception {
        // Arrange
        Long userId = 1L;
        User user = User.builder()
            .id(userId)
            .name("John Doe")
            .username("johndoe")
            .email("john@example.com")
            .build();
        when(restClientService.getUserById(userId)).thenReturn(user);

        // Act & Assert
        mockMvc.perform(get("/api/demo/users/{id}", userId))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("John Doe"));

        verify(restClientService).getUserById(userId);
    }

    @Test
    @DisplayName("GET /api/demo/users/{id} - должен вернуть 404 для несуществующего пользователя")
    void shouldReturn404ForNonExistentUser() throws Exception {
        // Arrange
        Long userId = 999L;
        when(restClientService.getUserById(userId)).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/api/demo/users/{id}", userId))
            .andExpect(status().isNotFound());

        verify(restClientService).getUserById(userId);
    }

    @Test
    @DisplayName("GET /api/demo/users/{id}/posts - должен вернуть посты пользователя")
    void shouldReturnUserPosts() throws Exception {
        // Arrange
        Long userId = 1L;
        List<Post> posts = List.of(
            Post.builder()
                .id(1L)
                .userId(userId)
                .title("Test Post")
                .body("Test post body")
                .build()
        );
        when(restClientService.getUserPosts(userId)).thenReturn(posts);

        // Act & Assert
        mockMvc.perform(get("/api/demo/users/{id}/posts", userId))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].title").value("Test Post"));

        verify(restClientService).getUserPosts(userId);
    }

    // =================================
    // Тесты POST endpoints
    // =================================

    @Test
    @DisplayName("POST /api/demo/users - должен создать пользователя")
    void shouldCreateUser() throws Exception {
        // Arrange
        CreateUserRequest createRequest = CreateUserRequest.builder()
            .name("New User")
            .username("newuser")
            .email("newuser@example.com")
            .build();

        User createdUser = User.builder()
            .id(11L)
            .name("New User")
            .username("newuser")
            .email("newuser@example.com")
            .build();

        when(restClientService.createUser(any(CreateUserRequest.class)))
            .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(createdUser));

        // Act & Assert
        mockMvc.perform(post("/api/demo/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(11))
            .andExpect(jsonPath("$.name").value("New User"));

        verify(restClientService).createUser(any(CreateUserRequest.class));
    }

    @Test
    @DisplayName("POST /api/demo/users - должен вернуть 400 при невалидных данных")
    void shouldReturn400ForInvalidData() throws Exception {
        // Arrange - невалидные данные
        String invalidJson = """
                {
                    "name": "",
                    "username": "a",
                    "email": "invalid-email"
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/demo/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
            .andExpect(status().isBadRequest());

        verify(restClientService, never()).createUser(any());
    }

    @Test
    @DisplayName("POST /api/demo/form-data - должен отправить данные формы")
    void shouldSendFormData() throws Exception {
        // Arrange
        Map<String, String> formData = Map.of(
            "field1", "value1",
            "field2", "value2"
        );

        HttpBinResponse response = HttpBinResponse.builder()
            .url("/post")
            .build();

        when(restClientService.sendFormData(anyMap())).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/demo/form-data")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(formData)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(restClientService).sendFormData(anyMap());
    }

    // =================================
    // Тесты PUT/PATCH endpoints
    // =================================

    @Test
    @DisplayName("PUT /api/demo/users/{id} - должен обновить пользователя")
    void shouldUpdateUser() throws Exception {
        // Arrange
        Long userId = 1L;
        UpdateUserRequest updateRequest = UpdateUserRequest.builder()
            .name("Updated Name")
            .email("updated@example.com")
            .build();

        User updatedUser = User.builder()
            .id(userId)
            .name("Updated Name")
            .username("johndoe")
            .email("updated@example.com")
            .build();

        when(restClientService.updateUser(eq(userId), any(UpdateUserRequest.class)))
            .thenReturn(updatedUser);

        // Act & Assert
        mockMvc.perform(put("/api/demo/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.name").value("Updated Name"));

        verify(restClientService).updateUser(eq(userId), any(UpdateUserRequest.class));
    }

    @Test
    @DisplayName("PATCH /api/demo/users/{id} - должен частично обновить пользователя")
    void shouldPatchUser() throws Exception {
        // Arrange
        Long userId = 1L;
        Map<String, Object> partialUpdate = Map.of("name", "Patched Name");

        User patchedUser = User.builder()
            .id(userId)
            .name("Patched Name")
            .username("johndoe")
            .email("john@example.com")
            .build();

        when(restClientService.patchUser(eq(userId), anyMap())).thenReturn(patchedUser);

        // Act & Assert
        mockMvc.perform(patch("/api/demo/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(partialUpdate)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.name").value("Patched Name"));

        verify(restClientService).patchUser(eq(userId), anyMap());
    }

    // =================================
    // Тесты DELETE endpoints
    // =================================

    @Test
    @DisplayName("DELETE /api/demo/users/{id} - должен удалить пользователя")
    void shouldDeleteUser() throws Exception {
        // Arrange
        Long userId = 1L;
        when(restClientService.deleteUser(userId)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(delete("/api/demo/users/{id}", userId))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true));

        verify(restClientService).deleteUser(userId);
    }

    @Test
    @DisplayName("DELETE /api/demo/users/{id} - должен вернуть 404 для несуществующего пользователя")
    void shouldReturn404WhenDeletingNonExistentUser() throws Exception {
        // Arrange
        Long userId = 999L;
        when(restClientService.deleteUser(userId)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(delete("/api/demo/users/{id}", userId))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false));

        verify(restClientService).deleteUser(userId);
    }

    // =================================
    // Тесты асинхронных endpoints
    // =================================

    @Test
    @DisplayName("GET /api/demo/users/{id}/async - должен асинхронно получить пользователя")
    void shouldGetUserAsync() throws Exception {
        // Arrange
        Long userId = 1L;
        User user = User.builder()
            .id(userId)
            .name("Async User")
            .username("asyncuser")
            .email("async@example.com")
            .build();

        when(restClientService.getUserByIdAsync(userId))
            .thenReturn(CompletableFuture.completedFuture(user));

        // Act & Assert
        mockMvc.perform(get("/api/demo/users/{id}/async", userId))
            .andExpect(status().isOk());

        verify(restClientService).getUserByIdAsync(userId);
    }

    @Test
    @DisplayName("POST /api/demo/users/batch-async - должен получить несколько пользователей асинхронно")
    void shouldGetMultipleUsersAsync() throws Exception {
        // Arrange
        List<Long> userIds = List.of(1L, 2L, 3L);
        List<User> users = List.of(
            User.builder().id(1L).name("User 1").username("user1").email("user1@example.com").build(),
            User.builder().id(2L).name("User 2").username("user2").email("user2@example.com").build()
        );

        when(restClientService.getMultipleUsersAsync(userIds))
            .thenReturn(CompletableFuture.completedFuture(users));

        // Act & Assert
        mockMvc.perform(post("/api/demo/users/batch-async")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userIds)))
            .andExpect(status().isOk());

        verify(restClientService).getMultipleUsersAsync(userIds);
    }

    // =================================
    // Тесты демонстрационных endpoints
    // =================================

    @Test
    @DisplayName("POST /api/demo/headers - должен демонстрировать работу с заголовками")
    void shouldDemonstrateHeaders() throws Exception {
        // Arrange
        Map<String, String> headers = Map.of("X-Custom", "Value");
        HttpBinResponse response = HttpBinResponse.builder()
            .url("/headers")
            .build();

        when(restClientService.demonstrateHeaders(anyMap())).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/demo/headers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(headers)))
            .andExpect(status().isOk());

        verify(restClientService).demonstrateHeaders(anyMap());
    }

    @Test
    @DisplayName("GET /api/demo/query-params - должен демонстрировать параметры запроса")
    void shouldDemonstrateQueryParams() throws Exception {
        // Arrange
        HttpBinResponse response = HttpBinResponse.builder()
            .url("/get")
            .build();

        when(restClientService.demonstrateQueryParams(anyMap())).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/demo/query-params")
                .param("param1", "value1")
                .param("param2", "value2"))
            .andExpect(status().isOk());

        verify(restClientService).demonstrateQueryParams(anyMap());
    }

    @Test
    @DisplayName("GET /api/demo/error/{code} - должен демонстрировать обработку ошибок")
    void shouldDemonstrateErrorHandling() throws Exception {
        // Arrange
        int statusCode = 500;
        HttpBinResponse response = HttpBinResponse.builder()
            .url("/status/" + statusCode)
            .build();

        when(restClientService.demonstrateErrorHandling(statusCode)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/demo/error/{code}", statusCode))
            .andExpect(status().isOk());

        verify(restClientService).demonstrateErrorHandling(statusCode);
    }

    // =================================
    // Тесты информационных endpoints
    // =================================

    @Test
    @DisplayName("GET /api/demo/info - должен вернуть информацию об API")
    void shouldReturnApiInfo() throws Exception {
        mockMvc.perform(get("/api/demo/info"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.name").value("Spring RestClient Demo API"))
            .andExpect(jsonPath("$.version").value("1.0.0"));

        verifyNoInteractions(restClientService);
    }

    @Test
    @DisplayName("GET /api/demo/health - должен вернуть статус здоровья")
    void shouldReturnHealthStatus() throws Exception {
        mockMvc.perform(get("/api/demo/health"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value("UP"));

        verifyNoInteractions(restClientService);
    }

    // =================================
    // Тесты обработки ошибок
    // =================================

    @Test
    @DisplayName("Должен обработать исключение сервиса")
    void shouldHandleServiceException() throws Exception {
        // Arrange
        when(restClientService.getAllUsers())
            .thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        mockMvc.perform(get("/api/demo/users"))
            .andExpect(status().is5xxServerError());

        verify(restClientService).getAllUsers();
    }

    @Test
    @DisplayName("Должен проверить Content-Type")
    void shouldValidateContentType() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/demo/users")
                .contentType(MediaType.TEXT_PLAIN)
                .content("invalid content"))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(restClientService);
    }

    @Test
    @DisplayName("Должен обработать некорректный JSON")
    void shouldHandleInvalidJson() throws Exception {
        // Act & Assert - некорректный JSON синтаксис
        mockMvc.perform(post("/api/demo/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"invalid\": json}")) // Некорректный JSON - отсутствуют кавычки вокруг json
            .andExpect(status().isBadRequest());

        verifyNoInteractions(restClientService);
    }

    @Test
    @DisplayName("Должен обработать пустой запрос")
    void shouldHandleEmptyRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/demo/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(restClientService);
    }

    @Test
    @DisplayName("Должен обработать полностью некорректный JSON")
    void shouldHandleCompletelyInvalidJson() throws Exception {
        // Act & Assert - совершенно некорректный JSON
        mockMvc.perform(post("/api/demo/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ not a valid json at all }"))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(restClientService);
    }

    @Test
    @DisplayName("Должен обработать JSON с отсутствующими скобками")
    void shouldHandleJsonWithMissingBraces() throws Exception {
        // Act & Assert - JSON без закрывающей скобки
        mockMvc.perform(post("/api/demo/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"test\""))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(restClientService);
    }
}