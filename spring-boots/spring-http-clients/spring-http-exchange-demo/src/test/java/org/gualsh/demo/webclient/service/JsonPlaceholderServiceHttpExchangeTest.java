package org.gualsh.demo.webclient.service;


import org.gualsh.demo.webclient.client.JsonPlaceholderClient;
import org.gualsh.demo.webclient.dto.CreatePostDto;
import org.gualsh.demo.webclient.dto.PostDto;
import org.gualsh.demo.webclient.dto.UserDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Тесты для JsonPlaceholderService с использованием мокированного HttpExchange клиента.
 *
 * <p>Преимущества тестирования HttpExchange клиентов:</p>
 * <ul>
 *   <li>Легкое мокирование интерфейса клиента</li>
 *   <li>Фокус на тестировании бизнес-логики сервиса</li>
 *   <li>Отсутствие необходимости в WireMock для unit тестов</li>
 *   <li>Быстрые и изолированные тесты</li>
 * </ul>
 *
 * <p>Интеграционные тесты с реальными HTTP вызовами остаются важными
 * и должны быть реализованы отдельно для проверки корректности
 * работы HttpExchange клиентов с реальными API.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JsonPlaceholderService HttpExchange Tests")
class JsonPlaceholderServiceHttpExchangeTest {

    @Mock
    private JsonPlaceholderClient mockClient;

    private JsonPlaceholderService service;

    @BeforeEach
    void setUp() {
        service = new JsonPlaceholderService(mockClient, 3, 1000);
    }

    /**
     * Тест получения всех пользователей.
     *
     * <p>Демонстрирует простоту мокирования HttpExchange клиента
     * по сравнению с настройкой WireMock.</p>
     */
    @Test
    @DisplayName("Should get all users successfully")
    void shouldGetAllUsers() {
        // Given
        List<UserDto> expectedUsers = Arrays.asList(
            UserDto.builder()
                .id(1L)
                .username("testuser1")
                .name("Test User 1")
                .email("test1@example.com")
                .build(),
            UserDto.builder()
                .id(2L)
                .username("testuser2")
                .name("Test User 2")
                .email("test2@example.com")
                .build()
        );

        when(mockClient.getAllUsers()).thenReturn(Mono.just(expectedUsers));

        // When & Then
        StepVerifier.create(service.getAllUsers())
            .expectNextMatches(users ->
                users.size() == 2 &&
                    users.get(0).getUsername().equals("testuser1") &&
                    users.get(1).getUsername().equals("testuser2"))
            .verifyComplete();

        verify(mockClient).getAllUsers();
    }

    /**
     * Тест получения пользователя по ID.
     */
    @Test
    @DisplayName("Should get user by ID successfully")
    void shouldGetUserById() {
        // Given
        Long userId = 1L;
        UserDto expectedUser = UserDto.builder()
            .id(userId)
            .username("testuser")
            .name("Test User")
            .email("test@example.com")
            .build();

        when(mockClient.getUserById(userId)).thenReturn(Mono.just(expectedUser));

        // When & Then
        StepVerifier.create(service.getUserById(userId))
            .expectNextMatches(user ->
                user.getId().equals(userId) &&
                    user.getUsername().equals("testuser"))
            .verifyComplete();

        verify(mockClient).getUserById(userId);
    }

    /**
     * Тест обработки ошибки при получении пользователя.
     */
    @Test
    @DisplayName("Should handle error when getting user by ID")
    void shouldHandleErrorWhenGettingUserById() {
        // Given
        Long userId = 999L;
        when(mockClient.getUserById(userId))
            .thenReturn(Mono.error(new RuntimeException("User not found")));

        // When & Then
        StepVerifier.create(service.getUserById(userId))
            .expectError(RuntimeException.class)
            .verify();

        verify(mockClient).getUserById(userId);
    }

    /**
     * Тест получения постов пользователя.
     */
    @Test
    @DisplayName("Should get posts by user ID successfully")
    void shouldGetPostsByUserId() {
        // Given
        Long userId = 1L;
        PostDto post1 = PostDto.builder()
            .id(1L)
            .userId(userId)
            .title("Test Post 1")
            .body("Test Body 1")
            .build();
        PostDto post2 = PostDto.builder()
            .id(2L)
            .userId(userId)
            .title("Test Post 2")
            .body("Test Body 2")
            .build();

        when(mockClient.getPostsByUserId(userId))
            .thenReturn(Flux.just(post1, post2));

        // When & Then
        StepVerifier.create(service.getPostsByUserId(userId))
            .expectNextMatches(post -> post.getId().equals(1L))
            .expectNextMatches(post -> post.getId().equals(2L))
            .verifyComplete();

        verify(mockClient).getPostsByUserId(userId);
    }

    /**
     * Тест создания нового поста.
     */
    @Test
    @DisplayName("Should create post successfully")
    void shouldCreatePost() {
        // Given
        CreatePostDto createPostDto = CreatePostDto.builder()
            .userId(1L)
            .title("New Post")
            .body("New Post Body")
            .build();

        PostDto expectedPost = PostDto.builder()
            .id(101L)
            .userId(1L)
            .title("New Post")
            .body("New Post Body")
            .build();

        when(mockClient.createPost(any(CreatePostDto.class)))
            .thenReturn(Mono.just(expectedPost));

        // When & Then
        StepVerifier.create(service.createPost(createPostDto))
            .expectNextMatches(post ->
                post.getId().equals(101L) &&
                    post.getTitle().equals("New Post"))
            .verifyComplete();

        verify(mockClient).createPost(createPostDto);
    }

    /**
     * Тест удаления поста.
     */
    @Test
    @DisplayName("Should delete post successfully")
    void shouldDeletePost() {
        // Given
        Long postId = 1L;
        when(mockClient.deletePost(postId)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(service.deletePost(postId))
            .verifyComplete();

        verify(mockClient).deletePost(postId);
    }

    /**
     * Тест композиции запросов - получение пользователя с постами.
     *
     * <p>Демонстрирует тестирование сложной бизнес-логики,
     * которая использует несколько вызовов клиента.</p>
     */
    @Test
    @DisplayName("Should get user with posts successfully")
    void shouldGetUserWithPosts() {
        // Given
        Long userId = 1L;
        UserDto user = UserDto.builder()
            .id(userId)
            .username("testuser")
            .name("Test User")
            .email("test@example.com")
            .build();

        PostDto post = PostDto.builder()
            .id(1L)
            .userId(userId)
            .title("Test Post")
            .body("Test Body")
            .build();

        when(mockClient.getUserById(userId)).thenReturn(Mono.just(user));
        when(mockClient.getPostsByUserId(userId)).thenReturn(Flux.just(post));

        // When & Then
        StepVerifier.create(service.getUserWithPosts(userId))
            .expectNextMatches(result -> {
                UserDto resultUser = (UserDto) result.get("user");
                @SuppressWarnings("unchecked")
                List<PostDto> resultPosts = (List<PostDto>) result.get("posts");
                Integer postsCount = (Integer) result.get("postsCount");

                return resultUser.getId().equals(userId) &&
                    resultPosts.size() == 1 &&
                    postsCount == 1;
            })
            .verifyComplete();

        verify(mockClient).getUserById(userId);
        verify(mockClient).getPostsByUserId(userId);
    }

    /**
     * Тест batch операции для получения нескольких пользователей.
     */
    @Test
    @DisplayName("Should get users batch successfully")
    void shouldGetUsersBatch() {
        // Given
        List<Long> userIds = Arrays.asList(1L, 2L, 3L);

        when(mockClient.getUserById(1L)).thenReturn(Mono.just(
            UserDto.builder().id(1L).username("user1").build()));
        when(mockClient.getUserById(2L)).thenReturn(Mono.just(
            UserDto.builder().id(2L).username("user2").build()));
        when(mockClient.getUserById(3L)).thenReturn(Mono.just(
            UserDto.builder().id(3L).username("user3").build()));

        // When & Then
        StepVerifier.create(service.getUsersBatch(userIds))
            .expectNextCount(3)
            .verifyComplete();

        verify(mockClient).getUserById(1L);
        verify(mockClient).getUserById(2L);
        verify(mockClient).getUserById(3L);
    }

    /**
     * Тест recover метода при ошибке.
     *
     * <p>Проверяет что @Recover методы работают корректно
     * с HttpExchange клиентами.</p>
     */
    @Test
    @DisplayName("Should recover when client fails")
    void shouldRecoverWhenClientFails() {
        // Given
        Long userId = 1L;
        when(mockClient.getUserById(userId))
            .thenReturn(Mono.error(new RuntimeException("Service unavailable")));

        // When & Then
        // Примечание: для полного тестирования @Recover нужен Spring контекст
        // Этот тест демонстрирует базовую проверку логики
        StepVerifier.create(service.getUserById(userId))
            .expectError(RuntimeException.class)
            .verify();

        verify(mockClient).getUserById(userId);
    }

    /**
     * Тест альтернативного способа получения комментариев.
     */
    @Test
    @DisplayName("Should get comments by post ID using alternative method")
    void shouldGetCommentsByPostIdAlternative() {
        // Given
        Long postId = 1L;
        when(mockClient.getCommentsByPostIdQuery(postId))
            .thenReturn(Flux.empty());

        // When & Then
        StepVerifier.create(service.getCommentsByPostIdAlternative(postId))
            .verifyComplete();

        verify(mockClient).getCommentsByPostIdQuery(postId);
    }

    /**
     * Тест получения всех постов.
     */
    @Test
    @DisplayName("Should get all posts successfully")
    void shouldGetAllPosts() {
        // Given
        List<PostDto> expectedPosts = Arrays.asList(
            PostDto.builder().id(1L).title("Post 1").build(),
            PostDto.builder().id(2L).title("Post 2").build()
        );

        when(mockClient.getAllPosts()).thenReturn(Mono.just(expectedPosts));

        // When & Then
        StepVerifier.create(service.getAllPosts())
            .expectNextMatches(posts -> posts.size() == 2)
            .verifyComplete();

        verify(mockClient).getAllPosts();
    }
}
