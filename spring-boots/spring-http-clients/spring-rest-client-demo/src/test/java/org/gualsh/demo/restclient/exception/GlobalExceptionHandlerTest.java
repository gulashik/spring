package org.gualsh.demo.restclient.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.gualsh.demo.restclient.controller.RestClientController;
import org.gualsh.demo.restclient.dto.CreateUserRequest;
import org.gualsh.demo.restclient.service.RestClientService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Тесты глобального обработчика исключений.
 *
 * Проверяют правильность обработки различных типов исключений
 * и возврата соответствующих HTTP ответов с корректной структурой.
 *
 * @author Demo Author
 */
@WebMvcTest({RestClientController.class, GlobalExceptionHandler.class})
@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RestClientService restClientService;

    // =================================
    // Тесты RestClientResponseException
    // =================================

    @Test
    @DisplayName("Должен обработать RestClientResponseException с 404 статусом")
    void shouldHandleRestClientResponseException404() throws Exception {
        // Arrange
        RestClientResponseException exception = new RestClientResponseException(
            "Not Found", HttpStatus.NOT_FOUND, "Not Found", null, null, null);
        when(restClientService.getAllUsers()).thenThrow(exception);

        // Act & Assert
        mockMvc.perform(get("/api/demo/users"))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.message").value("Ошибка при обращении к внешнему сервису"))
            .andExpect(jsonPath("$.path").value("uri=/api/demo/users"));
    }

    @Test
    @DisplayName("Должен обработать RestClientResponseException с 500 статусом")
    void shouldHandleRestClientResponseException500() throws Exception {
        // Arrange
        RestClientResponseException exception = new RestClientResponseException(
            "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal Server Error", null, null, null);
        when(restClientService.getAllUsers()).thenThrow(exception);

        // Act & Assert
        mockMvc.perform(get("/api/demo/users"))
            .andExpect(status().isServiceUnavailable())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(503))
            .andExpect(jsonPath("$.error").value("Server Error"))
            .andExpect(jsonPath("$.message").value("Ошибка при обращении к внешнему сервису"));
    }

    @Test
    @DisplayName("Должен обработать RestClientResponseException с 400 статусом")
    void shouldHandleRestClientResponseException400() throws Exception {
        // Arrange
        RestClientResponseException exception = new RestClientResponseException(
            "Bad Request", HttpStatus.BAD_REQUEST, "Bad Request", null, null, null);
        when(restClientService.getAllUsers()).thenThrow(exception);

        // Act & Assert
        mockMvc.perform(get("/api/demo/users"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    @DisplayName("Должен обработать RestClientResponseException с телом ответа")
    void shouldHandleRestClientResponseExceptionWithBody() throws Exception {
        // Arrange
        String responseBody = "{\"error\":\"External service error\"}";
        RestClientResponseException exception = new RestClientResponseException(
            "Service Error", HttpStatus.BAD_GATEWAY, "Bad Gateway",
            null, responseBody.getBytes(), null);
        when(restClientService.getAllUsers()).thenThrow(exception);

        // Act & Assert
        mockMvc.perform(get("/api/demo/users"))
            .andExpect(status().isServiceUnavailable())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(503))
            .andExpect(jsonPath("$.externalResponse").value(responseBody));
    }

    @Test
    @DisplayName("Должен обработать RestClientResponseException с 401 статусом")
    void shouldHandleUnauthorizedStatus() throws Exception {
        // Arrange
        RestClientResponseException exception = new RestClientResponseException(
            "Unauthorized", HttpStatus.UNAUTHORIZED, "Unauthorized", null, null, null);
        when(restClientService.getAllUsers()).thenThrow(exception);

        // Act & Assert
        mockMvc.perform(get("/api/demo/users"))
            .andExpect(status().isBadGateway()) // 401 -> 502 по логике маппинга
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(502));
    }

    // =================================
    // Тесты RestClientException
    // =================================

    @Test
    @DisplayName("Должен обработать общий RestClientException")
    void shouldHandleRestClientException() throws Exception {
        // Arrange
        RestClientException exception = new RestClientException("Connection timeout");
        when(restClientService.getAllUsers()).thenThrow(exception);

        // Act & Assert
        mockMvc.perform(get("/api/demo/users"))
            .andExpect(status().isServiceUnavailable())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.status").value(503))
            .andExpect(jsonPath("$.error").value("Service Unavailable"))
            .andExpect(jsonPath("$.message").value("Внешний сервис временно недоступен"))
            .andExpect(jsonPath("$.details").value("Connection timeout"))
            .andExpect(jsonPath("$.retryAdvice").value("Попробуйте повторить запрос позже"));
    }

    @Test
    @DisplayName("Должен обработать RestClientException с null сообщением")
    void shouldHandleRestClientExceptionWithNullMessage() throws Exception {
        // Arrange
        RestClientException exception = new RestClientException((String) null);
        when(restClientService.getAllUsers()).thenThrow(exception);

        // Act & Assert
        mockMvc.perform(get("/api/demo/users"))
            .andExpect(status().isServiceUnavailable())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(503))
            .andExpect(jsonPath("$.details").value("null"));
    }

    // =================================
    // Тесты MethodArgumentNotValidException (валидация)
    // =================================

    @Test
    @DisplayName("Должен обработать ошибки валидации")
    void shouldHandleValidationErrors() throws Exception {
        // Arrange - отправляем невалидные данные
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
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Validation Error"))
            .andExpect(jsonPath("$.message").value("Ошибка валидации входных данных"))
            .andExpect(jsonPath("$.fieldErrors").isMap());
    }

    @Test
    @DisplayName("Должен обработать валидацию с одной ошибкой поля")
    void shouldHandleValidationWithSingleFieldError() throws Exception {
        // Arrange - только email невалиден
        String partiallyInvalidJson = """
                {
                    "name": "Valid Name",
                    "username": "validuser",
                    "email": "invalid-email-format"
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/demo/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(partiallyInvalidJson))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.fieldErrors").isMap())
            .andExpect(jsonPath("$.fieldErrors.email").exists());
    }

    // =================================
    // Тесты RuntimeException
    // =================================

    @Test
    @DisplayName("Должен обработать RuntimeException")
    void shouldHandleRuntimeException() throws Exception {
        // Arrange
        RuntimeException exception = new RuntimeException("Unexpected error occurred");
        when(restClientService.getAllUsers()).thenThrow(exception);

        // Act & Assert
        mockMvc.perform(get("/api/demo/users"))
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.status").value(500))
            .andExpect(jsonPath("$.error").value("Internal Server Error"))
            .andExpect(jsonPath("$.message").value("Внутренняя ошибка сервера"))
            .andExpect(jsonPath("$.details").value("Unexpected error occurred"));
    }

    @Test
    @DisplayName("Должен обработать NullPointerException")
    void shouldHandleNullPointerException() throws Exception {
        // Arrange
        NullPointerException exception = new NullPointerException("Null pointer encountered");
        when(restClientService.getAllUsers()).thenThrow(exception);

        // Act & Assert
        mockMvc.perform(get("/api/demo/users"))
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(500))
            .andExpect(jsonPath("$.error").value("Internal Server Error"))
            .andExpect(jsonPath("$.details").value("Null pointer encountered"));
    }

    @Test
    @DisplayName("Должен обработать IllegalArgumentException")
    void shouldHandleIllegalArgumentException() throws Exception {
        // Arrange
        IllegalArgumentException exception = new IllegalArgumentException("Invalid argument provided");
        when(restClientService.getAllUsers()).thenThrow(exception);

        // Act & Assert
        mockMvc.perform(get("/api/demo/users"))
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(500))
            .andExpect(jsonPath("$.details").value("Invalid argument provided"));
    }

    // =================================
    // Тесты общего Exception
    // =================================

    @Test
    @DisplayName("Должен обработать общее Exception")
    void shouldHandleGenericException() throws Exception {
        // Arrange
        Exception exception = new Exception("Generic exception");
        when(restClientService.getAllUsers()).thenThrow(exception);

        // Act & Assert
        mockMvc.perform(get("/api/demo/users"))
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.status").value(500))
            .andExpect(jsonPath("$.error").value("Internal Server Error"))
            .andExpect(jsonPath("$.message").value("Произошла непредвиденная ошибка"))
            .andExpect(jsonPath("$.details").value("Обратитесь к администратору системы"));
    }

    // =================================
    // Тесты различных HTTP статусов
    // =================================

    @Test
    @DisplayName("Должен правильно мапить 403 статус")
    void shouldMapForbiddenStatus() throws Exception {
        // Arrange
        RestClientResponseException exception = new RestClientResponseException(
            "Forbidden", HttpStatus.FORBIDDEN, "Forbidden", null, null, null);
        when(restClientService.getAllUsers()).thenThrow(exception);

        // Act & Assert
        mockMvc.perform(get("/api/demo/users"))
            .andExpect(status().isBadGateway()) // 403 -> 502 по логике маппинга
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(502));
    }

    @Test
    @DisplayName("Должен правильно мапить 502 статус")
    void shouldMapBadGatewayStatus() throws Exception {
        // Arrange
        RestClientResponseException exception = new RestClientResponseException(
            "Bad Gateway", HttpStatus.BAD_GATEWAY, "Bad Gateway", null, null, null);
        when(restClientService.getAllUsers()).thenThrow(exception);

        // Act & Assert
        mockMvc.perform(get("/api/demo/users"))
            .andExpect(status().isServiceUnavailable()) // 502 -> 503 по логике маппинга
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(503));
    }

    // =================================
    // Тесты ошибок в разных endpoints
    // =================================

    @Test
    @DisplayName("Должен обработать ошибку в POST endpoint")
    void shouldHandleErrorInPostEndpoint() throws Exception {
        // Arrange
        RestClientResponseException exception = new RestClientResponseException(
            "Validation failed", HttpStatus.UNPROCESSABLE_ENTITY,
            "Unprocessable Entity", null, null, null);
        when(restClientService.createUser(any())).thenThrow(exception);

        CreateUserRequest request = CreateUserRequest.builder()
            .name("Test User")
            .username("testuser")
            .email("test@example.com")
            .build();

        // Act & Assert
        mockMvc.perform(post("/api/demo/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadGateway()) // 422 -> 502 по логике маппинга
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(502));
    }

    @Test
    @DisplayName("Должен обработать ошибку в DELETE endpoint")
    void shouldHandleErrorInDeleteEndpoint() throws Exception {
        // Arrange
        RestClientException exception = new RestClientException("Network error");
        when(restClientService.deleteUser(any())).thenThrow(exception);

        // Act & Assert
        mockMvc.perform(delete("/api/demo/users/1"))
            .andExpect(status().isServiceUnavailable())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(503))
            .andExpect(jsonPath("$.message").value("Внешний сервис временно недоступен"));
    }

    // =================================
    // Тесты содержимого ответов об ошибках
    // =================================

    @Test
    @DisplayName("Ответ об ошибке должен содержать все необходимые поля")
    void shouldIncludeAllRequiredFieldsInErrorResponse() throws Exception {
        // Arrange
        RestClientException exception = new RestClientException("Test error");
        when(restClientService.getAllUsers()).thenThrow(exception);

        // Act & Assert
        mockMvc.perform(get("/api/demo/users"))
            .andExpect(status().isServiceUnavailable())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.status").exists())
            .andExpect(jsonPath("$.error").exists())
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.details").exists())
            .andExpect(jsonPath("$.path").exists())
            .andExpect(jsonPath("$.retryAdvice").exists());
    }

    @Test
    @DisplayName("Путь запроса должен быть корректно указан в ошибке")
    void shouldIncludeCorrectPathInErrorResponse() throws Exception {
        // Arrange
        Long userId = 123L;
        RestClientException exception = new RestClientException("User service error");
        when(restClientService.getUserById(userId)).thenThrow(exception);

        // Act & Assert
        mockMvc.perform(get("/api/demo/users/{id}", userId))
            .andExpect(status().isServiceUnavailable())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.path").value("uri=/api/demo/users/" + userId));
    }

    // =================================
    // Тесты edge cases
    // =================================

    @Test
    @DisplayName("Должен обработать исключение с пустым сообщением")
    void shouldHandleExceptionWithEmptyMessage() throws Exception {
        // Arrange
        RestClientException exception = new RestClientException("");
        when(restClientService.getAllUsers()).thenThrow(exception);

        // Act & Assert
        mockMvc.perform(get("/api/demo/users"))
            .andExpect(status().isServiceUnavailable())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(503))
            .andExpect(jsonPath("$.details").value(""));
    }

    @Test
    @DisplayName("Должен обработать исключение с очень длинным сообщением")
    void shouldHandleExceptionWithLongMessage() throws Exception {
        // Arrange
        String longMessage = "A".repeat(1000);
        RestClientException exception = new RestClientException(longMessage);
        when(restClientService.getAllUsers()).thenThrow(exception);

        // Act & Assert
        mockMvc.perform(get("/api/demo/users"))
            .andExpect(status().isServiceUnavailable())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(503))
            .andExpect(jsonPath("$.details").value(longMessage));
    }

    @Test
    @DisplayName("Должен правильно обрабатывать специальные символы в сообщениях об ошибках")
    void shouldHandleSpecialCharactersInErrorMessages() throws Exception {
        // Arrange
        String messageWithSpecialChars = "Error with special chars: <script>alert('xss')</script>";
        RestClientException exception = new RestClientException(messageWithSpecialChars);
        when(restClientService.getAllUsers()).thenThrow(exception);

        // Act & Assert
        mockMvc.perform(get("/api/demo/users"))
            .andExpect(status().isServiceUnavailable())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.details").value(messageWithSpecialChars));
    }
}