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
 * Конфигурация метрик Micrometer.<p>
 * Эта конфигурация настраивает регистрацию метрик и аспекты для аннотации @Timed.
 * Основные компоненты включают настройку общих тегов и применение фильтров метрик
 * для управления их сбором и отображением.
 */
@Configuration
public class MetricsConfig {

    /**
     * Включает поддержку аннотации @Timed для методов.
     * TimedAspect обеспечивает аспектно-ориентированное измерение времени выполнения
     * методов, помеченных аннотацией @Timed, и автоматически регистрирует
     * соответствующие метрики в MeterRegistry.
     *
     * @param registry Регистратор метрик для интеграции аспектов
     * @return Настроенный TimedAspect
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    /**
     * Настраивает регистратор метрик с пользовательскими тегами и фильтрами.
     * Micrometer использует концепцию "размерных" метрик, где каждая метрика может
     * иметь несколько тегов для более детального анализа данных.
     *
     * @param environment Окружение для получения свойств приложения
     * @return Настроенный MeterRegistryCustomizer
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags(Environment environment) {
        return registry -> {
            // Добавляем общие теги, которые будут включены во все метрики
            // Это позволяет группировать и фильтровать метрики в системах мониторинга
            // по имени приложения, региону и активному профилю окружения
            registry.config()
                .commonTags("application", environment.getProperty("spring.application.name", "micromet-demo"))
                .commonTags("region", "demo-region")
                .commonTags("env", environment.getActiveProfiles().length > 0 ?
                    environment.getActiveProfiles()[0] : "default");

            // Фильтр для игнорирования определенных метрик, чтобы уменьшить шум
            // MeterFilter.deny полностью исключает метрики, соответствующие предикату
            // В данном случае мы исключаем метрики пиковых значений потоков JVM,
            // так как они часто менее информативны при мониторинге
            registry.config()
                .meterFilter(MeterFilter.deny(id ->
                    id.getName().startsWith("jvm.threads.peak")));

            // Настройка преобразования тегов для метрик
            // MeterFilter.renameTag изменяет название тега для лучшей совместимости
            // с системами мониторинга или для унификации наименований
            // Здесь мы переименовываем тег "exception" в "error" для HTTP запросов,
            // что упрощает интеграцию с системами мониторинга и построение запросов
            registry.config()
                .meterFilter(MeterFilter.renameTag("http.server.requests", "exception", "error"));

            // Настройка гистограмм для определенных метрик
            // MeterFilter.replaceTagValues позволяет модифицировать значения тегов
            // В этом примере мы включаем percentilesHistogram для метрик HTTP запросов,
            // что позволяет строить гистограммы распределения времени ответа по перцентилям
            // (например p50, p90, p95, p99), необходимые для анализа SLA приложения
            registry.config()
                .meterFilter(
                    MeterFilter.replaceTagValues(
                        "http.server.requests",
                        id -> DistributionStatisticConfig.builder().percentilesHistogram(true).build().toString()
                    )
                );
        };
    }
}