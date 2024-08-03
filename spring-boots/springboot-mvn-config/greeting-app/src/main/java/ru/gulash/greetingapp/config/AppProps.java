package ru.gulash.greetingapp.config;

import lombok.Getter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import java.util.Locale;

@ToString
//@EnableConfigurationProperties(IOConfig.class) // или так
@ConfigurationProperties(prefix = "gulash.custom")
public class AppProps {

    @Getter
    private final boolean ioEnabled;
    private final Locale locale;

    @ConstructorBinding
    public AppProps(boolean ioEnabled, String locale) {
        this.ioEnabled = ioEnabled;
        this.locale = Locale.forLanguageTag(locale);
    }
}
