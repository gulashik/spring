package org.gualsh.demo.micromet.config;

import io.micrometer.core.aop.CountedAspect;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Конфигурационный класс для настройки Micrometer в приложении.
 * <p>
 * Этот класс содержит различные настройки и кастомизации для регистров метрик Micrometer,
 * включая общие теги, фильтры и аспекты для тайминга методов.
 * </p>
 */
@Configuration
public class MicrometerConfig {

    /**
     * Имя приложения из конфигурации.
     */
    @Value("${spring.application.name}")
    private String applicationName;

    /**
     * Регистрирует TimedAspect для поддержки аннотации @Timed на методах.
     * <p>
     * Этот бин позволяет использовать аннотацию @Timed для автоматического
     * измерения времени выполнения методов.
     * </p>
     *
     * @param registry основной регистр метрик
     * @return настроенный TimedAspect
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    /**
     * Регистрирует CountedAspect для поддержки аннотации @Counted на методах.
     * <p>
     * Этот бин позволяет использовать аннотацию @Counted для автоматического
     * подсчета количества вызовов методов. Аннотация может включать дополнительные
     * теги и описание для более точной идентификации точек подсчета.
     * </p>
     * <p>
     * Без регистрации этого аспекта аннотация @Counted не будет работать.
     * </p>
     *
     * @param registry основной регистр метрик
     * @return настроенный CountedAspect
     */
    @Bean
    public CountedAspect countedAspect(MeterRegistry registry) {
        return new CountedAspect(registry);
    }


    /**
     * Кастомизирует MeterRegistry, добавляя общие теги ко всем метрикам.
     * <p>
     * Общие теги помогают сегментировать и фильтровать метрики в системах мониторинга.
     * </p>
     *
     * @return MeterRegistryCustomizer для применения общих тегов
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> {
            registry.config()
                .commonTags("application", applicationName)
                .commonTags("region", "demo-region")
                .commonTags("env", "demo");

            registry.config().meterFilter(MeterFilter.acceptNameStartsWith("http"));
            registry.config().meterFilter(MeterFilter.deny(id ->
                id.getName().startsWith("http.server.requests")
                    && id.getTag("uri") != null
                    && id.getTag("uri").startsWith("/actuator")));
        };
    }

    /**
     * Настраивает конфигурацию статистики распределения для HTTP-запросов.
     * <p>
     * Эта конфигурация определяет, как будут рассчитываться гистограммы и процентили
     * для метрик времени запросов. Реализует интерфейс MeterFilter для фильтрации
     * и настройки метрик HTTP-запросов.
     * </p>
     *
     * @return реализация MeterFilter с настроенной конфигурацией статистики распределения
     */
    @Bean
    public MeterFilter httpRequestsDistributionConfig() {
        return new MeterFilter() {
            @Override
            public DistributionStatisticConfig configure(Meter.Id id, DistributionStatisticConfig config) {
                if (id.getName().startsWith("http.server.requests")) {
                    return DistributionStatisticConfig.builder()
                        .percentilesHistogram(true)
                        .percentiles(0.5, 0.75, 0.95, 0.99) // Настраиваем процентили
                        .minimumExpectedValue(Duration.ofMillis(1).toNanos())
                        .maximumExpectedValue(Duration.ofSeconds(10).toNanos())
                        .build()
                        .merge(config);
                }
                return config;
            }
        };
    }
}