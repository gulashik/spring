package ru.gulash.example.shelldemo;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.shell.CommandNotCurrentlyAvailable;
import org.springframework.shell.InputProvider;
import org.springframework.shell.ResultHandlerService;
import org.springframework.shell.Shell;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@DisplayName("Тест команд shell ")
// или можно над классом @DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest
class OldShellVariantTest {

    private static final String GREETING_PATTERN = "Добро пожаловать: %s";
    private static final String DEFAULT_LOGIN = "AnyUser";
    private static final String CUSTOM_LOGIN = "Вася";
    private static final String COMMAND_LOGIN = "login";
    private static final String COMMAND_LOGIN_SHORT = "l";
    private static final String COMMAND_GET_ACCESS = "get-access";
    private static final String COMMAND_GET_ACCESS_RESULT = "Доступ получен";
    private static final String COMMAND_LOGIN_PATTERN = "%s %s";

    // provide a "line" of user input, whether interactively or by batch
    @Mock
    private InputProvider inputProvider;

    // a shell loop
    @Autowired
    private Shell shell;

    // result handling
    @SpyBean
    private ResultHandlerService resultHandlerService;

    @SpyBean
    private AppServiceAction appServiceAction;

    private ArgumentCaptor<Object> argumentCaptor;

    @BeforeEach
    void setUp() {
        //вместо @Mock или так inputProvider = mock(InputProvider.class);
        argumentCaptor = ArgumentCaptor.forClass(Object.class);
    }

    @DisplayName("должен возвращать приветствие для всех форм команды логина")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    @Test
    void shouldReturnExpectedGreetingAfterLoginCommandEvaluated() throws Exception {
        // подаём на stdin
        when(inputProvider.readInput())
                .thenReturn(() -> COMMAND_LOGIN)
                .thenReturn(() -> COMMAND_LOGIN_SHORT)
                .thenReturn(() -> String.format(COMMAND_LOGIN_PATTERN, COMMAND_LOGIN_SHORT, CUSTOM_LOGIN))
                .thenReturn(null);

        // запуск shell
        shell.run(inputProvider);

        // capture the arguments from resultHandlerService into argumentCaptor
        verify(resultHandlerService, times(3)).handle(argumentCaptor.capture());

        List<Object> results = argumentCaptor.getAllValues();
        assertThat(results).containsExactlyInAnyOrder(String.format(GREETING_PATTERN, DEFAULT_LOGIN),
                String.format(GREETING_PATTERN, DEFAULT_LOGIN),
                String.format(GREETING_PATTERN, CUSTOM_LOGIN));
    }

    @DisplayName("должен возвращать CommandNotCurrentlyAvailable если при попытке выполнения команды get-access пользователь неизвестен")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    @Test
    void shouldReturnCommandNotCurrentlyAvailableObjectWhenUserDoesNotLoginAfterPublishCommandEvaluated() {
        // подаём на stdin
        when(inputProvider.readInput())
                .thenReturn(() -> COMMAND_GET_ACCESS)
                .thenReturn(null);

        // должны получить exception
        assertThatCode(
                // запуск shell
                () -> shell.run(inputProvider)
        ).isInstanceOf(CommandNotCurrentlyAvailable.class);
    }

    @DisplayName("должен возвращать статус выполнения команды publish и вызвать соответствующий метод сервиса если команда выполнена после входа")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    @Test
    void shouldReturnExpectedMessageAndFirePublishMethodAfterPublishCommandEvaluated() throws Exception {
        // подаём на stdin
        when(inputProvider.readInput())
                .thenReturn(() -> COMMAND_LOGIN)
                .thenReturn(() -> COMMAND_GET_ACCESS)
                .thenReturn(null);

        // запуск shell
        shell.run(inputProvider);

        // capture the arguments from resultHandlerService into argumentCaptor
        verify(resultHandlerService, times(2)).handle(argumentCaptor.capture());

        assertThat(argumentCaptor.getValue()).isEqualTo(COMMAND_GET_ACCESS_RESULT);

        // метод executeAction() вызван у appServiceAction ровно 1 раз
        verify(appServiceAction, times(1)).executeAction();
    }
}