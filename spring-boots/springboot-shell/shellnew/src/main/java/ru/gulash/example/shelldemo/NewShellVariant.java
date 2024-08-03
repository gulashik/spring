package ru.gulash.example.shelldemo;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.CommandAvailability;
import org.springframework.shell.command.annotation.Option;

// todo @Command - используется как для класса и метода. Без ключей просто помечает класс.
@Command(group = "Application Events Commands New Way")
@RequiredArgsConstructor
public class NewShellVariant {

    private final AppServiceAction appServiceAction;

    private final InMemoryLoginContext loginContext;

    // todo @Command - используется как для класса и метода.
    @Command(
            description = "Login command new way", // todo Описание команды в Help
            command = "login", // todo указывает на какую команду реагировать
            alias = {"l","l2"} // todo дополнительные команды
    )
    public String login(
            // todo @Option - аргумент команды привязка к аргументу метода
            // todo defaultValue - если не передали
            @Option(defaultValue = "AnyUser") String userName
    ) {
        loginContext.login(userName);
        return String.format("Добро пожаловать: %s", userName);
    }

    @Command(description = "Publish event command new way", command = "access", alias = {"a", "get-access"})
    // todo доступность команды, через указание Bean, который возвращает Availability
    @CommandAvailability(provider = "publishEventCommandAvailabilityProvider")
    public String getAccess() {
        // "Доступ получен"
        return appServiceAction.executeAction();
    }
}
