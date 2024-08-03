package ru.gulash.greetingapp.runner;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import ru.gulash.greetingapp.config.AppProps;

import java.util.Locale;

@Component
public class AppRunner implements ApplicationRunner {
    // todo  internationalization-я сообщений
    private final MessageSource messageSource;

    private final AppProps appProps;

    public AppRunner(
            MessageSource messageSource,
            AppProps appProps
    ) {
        this.messageSource = messageSource;
        this.appProps = appProps;
    }

    public String getMessage(String key, Object... args) {

        // todo получает сообщение на нужном языке
        return messageSource.getMessage(
                key, // todo ключ в нужном файле messages_ЛОКАЛЬ.properties
                args, // todo будут подставляться в {ИНДЕКС} по шаблону из KEY(первый аргумент)
                Locale.forLanguageTag("ru-RU") // todo нужная локаль
                //Locale.forLanguageTag("en-US")
                //Locale.forLanguageTag("error will be from default file")
        );
    }

    @Override
    public void run(ApplicationArguments args) {

        System.out.println("---------------------------------------------");
        System.out.println(appProps);
        System.out.println("---------------------------------------------");
        var greetingTarget = getMessage("greeting.target");
        var greeting = getMessage("greeting", greetingTarget);
        System.out.println(greeting);
        System.out.println("---------------------------------------------");
    }
}
