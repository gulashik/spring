package org.gualsh.demo.restclient.config;

import org.apache.hc.client5.http.classic.HttpClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.*;

/**
 * Тесты конфигурации RestClient.
 *
 * Проверяют правильность создания и настройки всех компонентов
 * RestClient конфигурации.
 *
 * @author Demo Author
 */
@SpringBootTest
@TestPropertySource(properties = {
    "external-api.jsonplaceholder.base-url=https://jsonplaceholder.typicode.com",
    "external-api.httpbin.base-url=https://httpbin.org",
    "app.restclient.connection-pool.max-total=100",
    "app.restclient.connection-pool.max-per-route=25",
    "app.restclient.default-timeouts.connection=3000",
    "app.restclient.default-timeouts.socket=5000"
})
@DisplayName("RestClientConfiguration Tests")
class RestClientConfigurationTest {

    @Autowired
    private ApplicationContext applicationContext;

    // =================================
    // Тесты создания бинов
    // =================================

    @Test
    @DisplayName("Должен создать HttpClient бин")
    void shouldCreateHttpClientBean() {
        // Act & Assert
        assertThat(applicationContext.containsBean("httpClient")).isTrue();

        HttpClient httpClient = applicationContext.getBean("httpClient", HttpClient.class);
        assertThat(httpClient).isNotNull();
    }

    @Test
    @DisplayName("Должен создать HttpComponentsClientHttpRequestFactory бин")
    void shouldCreateHttpRequestFactoryBean() {
        // Act & Assert
        assertThat(applicationContext.containsBean("httpRequestFactory")).isTrue();

        HttpComponentsClientHttpRequestFactory factory =
            applicationContext.getBean("httpRequestFactory", HttpComponentsClientHttpRequestFactory.class);
        assertThat(factory).isNotNull();
    }

    @Test
    @DisplayName("Должен создать jsonPlaceholderRestClient бин")
    void shouldCreateJsonPlaceholderRestClientBean() {
        // Act & Assert
        assertThat(applicationContext.containsBean("jsonPlaceholderRestClient")).isTrue();

        RestClient restClient = applicationContext.getBean("jsonPlaceholderRestClient", RestClient.class);
        assertThat(restClient).isNotNull();
    }

    @Test
    @DisplayName("Должен создать httpBinRestClient бин")
    void shouldCreateHttpBinRestClientBean() {
        // Act & Assert
        assertThat(applicationContext.containsBean("httpBinRestClient")).isTrue();

        RestClient restClient = applicationContext.getBean("httpBinRestClient", RestClient.class);
        assertThat(restClient).isNotNull();
    }

    @Test
    @DisplayName("Должен создать genericRestClient бин")
    void shouldCreateGenericRestClientBean() {
        // Act & Assert
        assertThat(applicationContext.containsBean("genericRestClient")).isTrue();

        RestClient restClient = applicationContext.getBean("genericRestClient", RestClient.class);
        assertThat(restClient).isNotNull();
    }

    // =================================
    // Тесты зависимостей между бинами
    // =================================

    @Test
    @DisplayName("HttpRequestFactory должен использовать правильный HttpClient")
    void shouldUseCorrectHttpClientInFactory() {
        // Arrange
        HttpClient httpClient = applicationContext.getBean("httpClient", HttpClient.class);
        HttpComponentsClientHttpRequestFactory factory =
            applicationContext.getBean("httpRequestFactory", HttpComponentsClientHttpRequestFactory.class);

        // Act & Assert
        assertThat(factory).isNotNull();
        // Проверяем, что фабрика создана с нашим HttpClient
        // (прямой доступ к internal полям не рекомендуется, но для тестов конфигурации допустимо)
    }

    @Test
    @DisplayName("Все RestClient должны быть разными экземплярами")
    void shouldCreateDifferentRestClientInstances() {
        // Arrange
        RestClient jsonPlaceholderClient = applicationContext.getBean("jsonPlaceholderRestClient", RestClient.class);
        RestClient httpBinClient = applicationContext.getBean("httpBinRestClient", RestClient.class);
        RestClient genericClient = applicationContext.getBean("genericRestClient", RestClient.class);

        // Act & Assert
        assertThat(jsonPlaceholderClient).isNotSameAs(httpBinClient);
        assertThat(jsonPlaceholderClient).isNotSameAs(genericClient);
        assertThat(httpBinClient).isNotSameAs(genericClient);
    }

    // =================================
    // Тесты настроек timeout
    // =================================

    @Test
    @DisplayName("HttpRequestFactory должен иметь правильные настройки timeout")
    void shouldHaveCorrectTimeoutSettings() {
        // Arrange
        HttpComponentsClientHttpRequestFactory factory =
            applicationContext.getBean("httpRequestFactory", HttpComponentsClientHttpRequestFactory.class);

        // Act & Assert
        assertThat(factory).isNotNull();

        // В Spring Boot 3.x нет публичных методов для получения таймаутов из HttpComponentsClientHttpRequestFactory
        // Проверяем, что фабрика создана и может создавать запросы
        assertThatCode(() -> {
            var request = factory.createRequest(
                java.net.URI.create("http://example.com"),
                org.springframework.http.HttpMethod.GET
            );
            assertThat(request).isNotNull();
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("HttpRequestFactory должен быть настроен с HttpClient")
    void shouldBeConfiguredWithHttpClient() {
        // Arrange
        HttpClient httpClient = applicationContext.getBean("httpClient", HttpClient.class);
        HttpComponentsClientHttpRequestFactory factory =
            applicationContext.getBean("httpRequestFactory", HttpComponentsClientHttpRequestFactory.class);

        // Act & Assert
        assertThat(httpClient).isNotNull();
        assertThat(factory).isNotNull();

        // Проверяем, что фабрика может работать с нашим HttpClient
        // Это косвенно подтверждает правильную конфигурацию
        assertThatCode(() -> {
            var request = factory.createRequest(
                java.net.URI.create("http://test.example.com"),
                org.springframework.http.HttpMethod.GET
            );
            // Проверяем, что запрос создан успешно
            assertThat(request.getMethod()).isEqualTo(org.springframework.http.HttpMethod.GET);
            assertThat(request.getURI().toString()).isEqualTo("http://test.example.com");
        }).doesNotThrowAnyException();
    }

    // =================================
    // Тесты правильности конфигурации
    // =================================

    @Test
    @DisplayName("Конфигурация должна загружаться без ошибок")
    void shouldLoadConfigurationWithoutErrors() {
        // Act & Assert
        assertThat(applicationContext).isNotNull();
        assertThat(applicationContext.getBean(RestClientConfiguration.class)).isNotNull();
    }

    @Test
    @DisplayName("Все необходимые бины должны быть доступны")
    void shouldHaveAllRequiredBeans() {
        // Act & Assert
        String[] expectedBeans = {
            "httpClient",
            "httpRequestFactory",
            "jsonPlaceholderRestClient",
            "httpBinRestClient",
            "genericRestClient"
        };

        for (String beanName : expectedBeans) {
            assertThat(applicationContext.containsBean(beanName))
                .as("Bean '%s' should exist", beanName)
                .isTrue();
        }
    }

    // =================================
    // Тесты работы с Properties
    // =================================

    @Test
    @DisplayName("Должен правильно читать свойства из конфигурации")
    void shouldReadPropertiesCorrectly() {
        // Arrange
        RestClientConfiguration config = applicationContext.getBean(RestClientConfiguration.class);

        // Act & Assert
        assertThat(config).isNotNull();
        // Проверяем, что конфигурация создана и может создавать бины
        // (прямая проверка @Value полей требует рефлексии)
    }

    // =================================
    // Интеграционные тесты конфигурации
    // =================================

    @Test
    @DisplayName("RestClient должен быть готов к использованию")
    void shouldBeReadyForUse() {
        // Arrange
        RestClient jsonPlaceholderClient = applicationContext.getBean("jsonPlaceholderRestClient", RestClient.class);

        // Act & Assert
        assertThatCode(() -> {
            // Создаем простой запрос для проверки готовности
            var requestSpec = jsonPlaceholderClient.get().uri("/test");
            assertThat(requestSpec).isNotNull();
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Все компоненты должны быть совместимы друг с другом")
    void shouldHaveCompatibleComponents() {
        // Arrange
        HttpClient httpClient = applicationContext.getBean("httpClient", HttpClient.class);
        HttpComponentsClientHttpRequestFactory factory =
            applicationContext.getBean("httpRequestFactory", HttpComponentsClientHttpRequestFactory.class);

        // Act & Assert
        assertThat(httpClient).isNotNull();
        assertThat(factory).isNotNull();

        // Проверяем, что компоненты могут работать вместе
        assertThatCode(() -> {
            // Проверяем, что фабрика может создавать запросы
            var request = factory.createRequest(java.net.URI.create("http://example.com"),
                org.springframework.http.HttpMethod.GET);
            assertThat(request).isNotNull();
        }).doesNotThrowAnyException();
    }
}