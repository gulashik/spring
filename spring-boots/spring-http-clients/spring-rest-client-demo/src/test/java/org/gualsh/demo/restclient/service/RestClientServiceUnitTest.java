package org.gualsh.demo.restclient.service;

import org.gualsh.demo.restclient.dto.CreateUserRequest;
import org.gualsh.demo.restclient.dto.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit тесты для RestClientService с использованием Mockito.
 *
 * Эти тесты фокусируются на логике сервиса без реальных HTTP вызовов,
 * демонстрируя изолированное тестирование компонентов.
 *
 * @author Demo Author
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RestClientService Unit Tests")
class RestClientServiceUnitTest {

    @Mock
    private RestClient jsonPlaceholderClient;

    @Mock
    private RestClient httpBinClient;

    @Mock
    private RestClient genericClient;

    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RestClient.RequestBodySpec requestBodySpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private RestClientService restClientService;

    @BeforeEach
    void setUp() {
        // Вместо @InjectMocks - явное создание сервиса
        restClientService = new RestClientService(jsonPlaceholderClient, httpBinClient, genericClient);
    }

    // =================================
    // Тесты GET операций
    // =================================

    @Test
    @DisplayName("Должен правильно обработать пустой список пользователей")
    void shouldHandleEmptyUsersList() {
        // Arrange
        when(jsonPlaceholderClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/users")).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.header(anyString(), anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(List.of());

        // Act
        List<User> users = restClientService.getAllUsers();

        // Assert
        assertThat(users).isEmpty();
        verify(jsonPlaceholderClient).get();
        verify(requestHeadersUriSpec).header(eq("X-Request-ID"), anyString());
    }

    @Test
    @DisplayName("Должен обработать успешный ответ со списком пользователей")
    void shouldHandleSuccessfulUsersResponse() {
        // Arrange
        List<User> expectedUsers = List.of(
            User.builder().id(1L).name("John").username("john").email("john@test.com").build(),
            User.builder().id(2L).name("Jane").username("jane").email("jane@test.com").build()
        );

        when(jsonPlaceholderClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/users")).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.header(anyString(), anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(expectedUsers);

        // Act
        List<User> users = restClientService.getAllUsers();

        // Assert
        assertThat(users).hasSize(2);
        assertThat(users.get(0).getName()).isEqualTo("John");
        assertThat(users.get(1).getName()).isEqualTo("Jane");
    }

    @Test
    @DisplayName("Должен обработать RestClientResponseException")
    void shouldHandleRestClientResponseException() {
        // Arrange
        when(jsonPlaceholderClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/users")).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.header(anyString(), anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class)))
            .thenThrow(new RestClientResponseException("Test error", HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error", null, null, null));

        // Act & Assert
        assertThatThrownBy(() -> restClientService.getAllUsers())
            .isInstanceOf(RestClientResponseException.class)
            .hasMessageContaining("Test error");
    }

    @Test
    @DisplayName("Должен корректно получать пользователя по ID")
    void shouldGetUserById() {
        // Arrange
        Long userId = 1L;
        User expectedUser = User.builder()
            .id(userId)
            .name("John Doe")
            .username("johndoe")
            .email("john@example.com")
            .build();

        when(jsonPlaceholderClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/users/{id}", userId)).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.header(anyString(), anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.body(User.class)).thenReturn(expectedUser);

        // Act
        User user = restClientService.getUserById(userId);

        // Assert
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(userId);
        assertThat(user.getName()).isEqualTo("John Doe");

        verify(requestHeadersUriSpec).uri("/users/{id}", userId);
    }

    @Test
    @DisplayName("Должен возвращать null для несуществующего пользователя")
    void shouldReturnNullForNonExistentUser() {
        // Arrange
        Long userId = 999L;

        when(jsonPlaceholderClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/users/{id}", userId)).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.header(anyString(), anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.body(User.class)).thenReturn(null);

        // Act
        User user = restClientService.getUserById(userId);

        // Assert
        assertThat(user).isNull();
    }

    // =================================
    // Тесты POST операций
    // =================================

    @Test
    @DisplayName("Должен успешно создать пользователя")
    void shouldCreateUser() {
        // Arrange
        CreateUserRequest createRequest = CreateUserRequest.builder()
            .name("New User")
            .username("newuser")
            .email("new@example.com")
            .build();

        User createdUser = User.builder()
            .id(11L)
            .name("New User")
            .username("newuser")
            .email("new@example.com")
            .build();

        ResponseEntity<User> expectedResponse = ResponseEntity.status(HttpStatus.CREATED).body(createdUser);

        when(jsonPlaceholderClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/users")).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(CreateUserRequest.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.toEntity(User.class)).thenReturn(expectedResponse);

        // Act
        ResponseEntity<User> response = restClientService.createUser(createRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(11L);
        assertThat(response.getBody().getName()).isEqualTo("New User");

        verify(requestBodySpec).body(createRequest);
    }

    // =================================
    // Тесты DELETE операций
    // =================================

    @Test
    @DisplayName("Должен успешно удалить пользователя")
    void shouldDeleteUser() {
        // Arrange
        Long userId = 1L;
        ResponseEntity<Void> successResponse = ResponseEntity.ok().build();

        when(jsonPlaceholderClient.delete()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/users/{id}", userId)).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.header(anyString(), anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(successResponse);

        // Act
        boolean result = restClientService.deleteUser(userId);

        // Assert
        assertThat(result).isTrue();
        verify(requestHeadersUriSpec).uri("/users/{id}", userId);
    }

    @Test
    @DisplayName("Должен обработать ошибку при удалении несуществующего пользователя")
    void shouldHandleDeleteNotFound() {
        // Arrange
        Long userId = 999L;

        when(jsonPlaceholderClient.delete()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/users/{id}", userId)).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.header(anyString(), anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity())
            .thenThrow(new RestClientResponseException("Not Found", HttpStatus.NOT_FOUND,
                "Not Found", null, null, null));

        // Act
        boolean result = restClientService.deleteUser(userId);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Должен демонстрировать работу с заголовками")
    void shouldDemonstrateHeaders() {
        // Arrange
        Map<String, String> customHeaders = Map.of(
            "X-Custom-Header", "CustomValue",
            "X-Client-Version", "1.0.0"
        );

        org.gualsh.demo.restclient.dto.HttpBinResponse expectedResponse =
            org.gualsh.demo.restclient.dto.HttpBinResponse.builder()
                .url("/headers")
                .headers(Map.of(
                    "X-Custom-Header", "CustomValue",
                    "X-Request-ID", "test-id"
                ))
                .build();

        when(httpBinClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/headers")).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.header(anyString(), anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(org.gualsh.demo.restclient.dto.HttpBinResponse.class))
            .thenReturn(expectedResponse);

        // Act
        var response = restClientService.demonstrateHeaders(customHeaders);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getUrl()).isEqualTo("/headers");

        // Проверяем, что пользовательские заголовки были добавлены
        verify(requestHeadersUriSpec, atLeastOnce()).header("X-Custom-Header", "CustomValue");
        verify(requestHeadersUriSpec, atLeastOnce()).header("X-Client-Version", "1.0.0");
    }

    @Test
    @DisplayName("Должен демонстрировать работу с параметрами запроса")
    void shouldDemonstrateQueryParams() {
        // Arrange
        Map<String, String> queryParams = Map.of(
            "param1", "value1",
            "param2", "value2"
        );

        org.gualsh.demo.restclient.dto.HttpBinResponse expectedResponse =
            org.gualsh.demo.restclient.dto.HttpBinResponse.builder()
                .url("/get")
                .args(Map.of("param1", "value1", "param2", "value2"))
                .build();

        when(httpBinClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(java.util.function.Function.class))).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.header(anyString(), anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(org.gualsh.demo.restclient.dto.HttpBinResponse.class))
            .thenReturn(expectedResponse);

        // Act
        var response = restClientService.demonstrateQueryParams(queryParams);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getUrl()).isEqualTo("/get");
        verify(requestHeadersUriSpec).uri(any(java.util.function.Function.class));
    }

    // =================================
    // Тесты обработки ошибок
    // =================================

    @Test
    @DisplayName("Должен обработать различные типы исключений")
    void shouldHandleDifferentExceptionTypes() {
        // Arrange
        when(jsonPlaceholderClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/users")).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.header(anyString(), anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class)))
            .thenThrow(new RuntimeException("Connection timeout"));

        // Act & Assert
        assertThatThrownBy(() -> restClientService.getAllUsers())
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Connection timeout");
    }

    @Test
    @DisplayName("Должен корректно генерировать ID запроса")
    void shouldGenerateRequestId() {
        // Arrange
        when(jsonPlaceholderClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/users")).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.header(anyString(), anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(List.of());

        // Act
        restClientService.getAllUsers();

        // Assert - проверяем, что заголовок X-Request-ID был установлен с правильным форматом
        ArgumentCaptor<String> headerValueCaptor = ArgumentCaptor.forClass(String.class);
        verify(requestHeadersUriSpec).header(eq("X-Request-ID"), headerValueCaptor.capture());

        String requestId = headerValueCaptor.getValue();
        assertThat(requestId).isNotNull()
            .startsWith("REQ-")
            .contains("-")
            .hasSizeGreaterThan(10); // Минимальная длина ID
    }

    @Test
    @DisplayName("Должен демонстрировать обработку ошибок")
    void shouldDemonstrateErrorHandling() {
        // Arrange
        int statusCode = 500;
        org.gualsh.demo.restclient.dto.HttpBinResponse expectedResponse =
            org.gualsh.demo.restclient.dto.HttpBinResponse.builder()
                .url("/status/" + statusCode)
                .origin("error-simulation")
                .build();

        when(httpBinClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/status/{code}", statusCode)).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.header(anyString(), anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(org.gualsh.demo.restclient.dto.HttpBinResponse.class))
            .thenThrow(new RestClientResponseException("Internal Server Error",
                HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", null, null, null));

        // Act
        var response = restClientService.demonstrateErrorHandling(statusCode);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getUrl()).isEqualTo("/status/" + statusCode);
        assertThat(response.getOrigin()).isEqualTo("error-simulation");
    }


    @Test
    @DisplayName("Должен правильно обрабатывать пустые коллекции")
    void shouldHandleEmptyCollections() {
        // Arrange
        Map<String, String> emptyHeaders = Map.of();
        org.gualsh.demo.restclient.dto.HttpBinResponse expectedResponse =
            org.gualsh.demo.restclient.dto.HttpBinResponse.builder()
                .url("/headers")
                .headers(Map.of())
                .build();

        when(httpBinClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/headers")).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.header(anyString(), anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(org.gualsh.demo.restclient.dto.HttpBinResponse.class))
            .thenReturn(expectedResponse);

        // Act
        var response = restClientService.demonstrateHeaders(emptyHeaders);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getHeaders()).isEmpty();

        // Проверяем, что стандартные заголовки все равно добавлены
        verify(requestHeadersUriSpec, atLeastOnce()).header(eq("X-Request-ID"), anyString());
        verify(requestHeadersUriSpec, atLeastOnce()).header(eq("X-Demo-Header"), anyString());
    }

    // =================================
    // Тесты взаимодействия моков
    // =================================

    @Test
    @DisplayName("Должен корректно настраивать цепочку вызовов RestClient")
    void shouldCorrectlyChainRestClientCalls() {
        // Arrange
        when(jsonPlaceholderClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/users")).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.header(anyString(), anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(List.of());

        // Act
        restClientService.getAllUsers();

        // Assert - проверяем правильную последовательность вызовов
        var inOrder = inOrder(jsonPlaceholderClient, requestHeadersUriSpec, responseSpec);
        inOrder.verify(jsonPlaceholderClient).get();
        inOrder.verify(requestHeadersUriSpec).uri("/users");
        inOrder.verify(requestHeadersUriSpec, atLeastOnce()).header(anyString(), anyString());
        inOrder.verify(requestHeadersUriSpec).retrieve();
        inOrder.verify(responseSpec, times(2)).onStatus(any(), any());
        inOrder.verify(responseSpec).body(any(ParameterizedTypeReference.class));
    }
}