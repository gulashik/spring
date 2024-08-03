package ru.gulash.example.shelldemo;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;

@ShellComponent(value = "Commands for old shell variant ")
@RequiredArgsConstructor
public class OldShellVariant {

    private final AppServiceAction appServiceAction;

    private final InMemoryLoginContext loginContext;

    @ShellMethod(
        value = "Login command", // todo Описание команды в Help
        key = {"l", "login"} // todo Реагируем на команды
    )
    // todo если "key" нет, то будет использоваться название метода и cammelCase будет переделан в kebab-case
    public String login(
        // defaultValue - если не передали
        @ShellOption(defaultValue = "AnyUser") String userName
    ) {
        loginContext.login(userName);
        return String.format("Добро пожаловать: %s", userName);
    }

    @ShellMethod(
            value = "Publish event command",
            key = {"a", "access", "get-access"}
    )
    // todo Указать метод, который вернёт доступность команды.
    @ShellMethodAvailability(value = "isLogged")
    public String getAccess() {
        // "Доступ получен"
        return appServiceAction.executeAction();
    }

    private Availability isLogged() {
        // todo нужно что-то одно вернуть Availability.available() или Availability.unavailable("причина")
        return loginContext.isUserLoggedIn()
                ? Availability.available()
                : Availability.unavailable("Сначала нужно login");
    }
}
