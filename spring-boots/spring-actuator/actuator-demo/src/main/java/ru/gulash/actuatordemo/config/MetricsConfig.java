package ru.gulash.actuatordemo.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Конфигурация метрик для Actuator.
 */
@Configuration
public class MetricsConfig {

    /**
     * Настройка реестра метрик - добавление общих тегов.
     *
     * @param environment среда для получения свойств приложения
     * @return настройщик реестра метрик
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags(Environment environment) {
        return registry -> registry.config()
            .commonTags("application", "actuator-demo")
            .commonTags("env", environment.getActiveProfiles().length > 0
                ? environment.getActiveProfiles()[0] : "default");
    }

    /**
     * Аспект для поддержки аннотации @Timed.
     *
     * @param registry реестр метрик
     * @return аспект для обработки аннотаций @Timed
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    /**
     * Метрики сборщика мусора JVM.
     *
     * @return метрики сборщика мусора
     */
    @Bean
    public JvmGcMetrics jvmGcMetrics() {
        return new JvmGcMetrics();
    }

    /**
     * Метрики памяти JVM.
     *
     * @return метрики памяти
     */
    @Bean
    public JvmMemoryMetrics jvmMemoryMetrics() {
        return new JvmMemoryMetrics();
    }

    /**
     * Метрики потоков JVM.
     *
     * @return метрики потоков
     */
    @Bean
    public JvmThreadMetrics jvmThreadMetrics() {
        return new JvmThreadMetrics();
    }

    /**
     * Метрики процессора.
     *
     * @return метрики процессора
     */
    @Bean
    public ProcessorMetrics processorMetrics() {
        return new ProcessorMetrics();
    }
}
