package org.gualsh.demo.micromet.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Конфигурация метрик Micrometer.
 * Эта конфигурация настраивает регистрацию метрик и аспекты для аннотации @Timed.
 */
@Configuration
public class MetricsConfig {

    /**
     * Настраивает регистратор метрик с пользовательскими тегами и фильтрами.
     *
     * @param environment Окружение для получения свойств приложения
     * @return Настроенный MeterRegistryCustomizer
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags(Environment environment) {
        return registry -> {
            // Добавляем общие теги, которые будут включены во все метрики
            registry.config()
                .commonTags("application", environment.getProperty("spring.application.name", "micromet-demo"))
                .commonTags("region", "demo-region")
                .commonTags("env", environment.getActiveProfiles().length > 0 ?
                    environment.getActiveProfiles()[0] : "default");

            // Фильтр для игнорирования определенных метрик, чтобы уменьшить шум
            registry.config()
                .meterFilter(MeterFilter.deny(id ->
                    id.getName().startsWith("jvm.threads.peak")));

            // Настройка единицы измерения для метрик
            registry.config()
                .meterFilter(MeterFilter.renameTag("http.server.requests", "exception", "error"));

            // Настройка гистограмм для определенных метрик
            registry.config()
                .meterFilter(
                    MeterFilter.replaceTagValues(
                        "http.server.requests",
                        id -> DistributionStatisticConfig.builder().percentilesHistogram(true).build().toString()
                    )
                );
        };
    }
    /**
     * Включает поддержку аннотации @Timed для методов.
     *
     * @param registry Регистратор метрик для интеграции аспектов
     * @return Настроенный TimedAspect
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
}