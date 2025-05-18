package org.gualsh.demo.micromet.metrics;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;

/**
 * Компонент для демонстрации создания нестандартных метрик с Micrometer.
 * <p>
 * Этот класс показывает, как создавать собственные метрики, которые не предоставляются
 * встроенными биндерами Micrometer, например метрики файловой системы и пользовательские
 * метрики JVM.
 * </p>
 */
@Component
@Slf4j
public class CustomMetricsBinder implements MeterBinder {

    private static final String[] DISK_PATHS = {"/", "/tmp", "/home"};

    /**
     * Регистрирует нестандартные метрики в реестре метрик.
     *
     * @param registry реестр метрик для регистрации
     */
    @Override
    public void bindTo(MeterRegistry registry) {
        log.info("Registering custom metrics");

        // Регистрируем метрики файловой системы
        registerFileSystemMetrics(registry);

        // Регистрируем дополнительные метрики JVM
        registerJvmExtendedMetrics(registry);

        // Регистрируем метрики операционной системы
        registerOsMetrics(registry);

        log.info("Custom metrics registered");
    }

    /**
     * Регистрирует метрики файловой системы.
     *
     * @param registry реестр метрик
     */
    private void registerFileSystemMetrics(MeterRegistry registry) {
        // Для каждого пути создаем метрики диска
        Arrays.stream(DISK_PATHS).forEach(path -> {
            File file = new File(path);

            // Пропускаем несуществующие пути
            if (!file.exists()) {
                return;
            }

            Tags tags = Tags.of(
                Tag.of("path", path),
                Tag.of("fs_type", "unknown") // В реальном приложении можно определить тип ФС
            );

            // Регистрируем метрику общего размера диска
            Gauge.builder("disk.total", file, f -> f.getTotalSpace())
                .tags(tags)
                .description("Total disk space")
                .baseUnit("bytes")
                .register(registry);

            // Регистрируем метрику свободного места на диске
            Gauge.builder("disk.free", file, f -> f.getFreeSpace())
                .tags(tags)
                .description("Free disk space")
                .baseUnit("bytes")
                .register(registry);

            // Регистрируем метрику используемого места на диске
            Gauge.builder("disk.used", file, f -> f.getTotalSpace() - f.getFreeSpace())
                .tags(tags)
                .description("Used disk space")
                .baseUnit("bytes")
                .register(registry);

            // Регистрируем процент использования диска
            Gauge.builder("disk.usage.ratio", file,
                    f -> {
                        long total = f.getTotalSpace();
                        return total == 0 ? 0 : (double) (total - f.getFreeSpace()) / total;
                    })
                .tags(tags)
                .description("Disk usage ratio")
                .register(registry);
        });
    }

    /**
     * Регистрирует расширенные метрики JVM.
     *
     * @param registry реестр метрик
     */
    private void registerJvmExtendedMetrics(MeterRegistry registry) {
        // Получаем MXBeans для сбора данных о JVM
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

        // Метрики использования памяти JVM, более детальные чем стандартные
        Gauge.builder("jvm.memory.used.percentage", memoryBean,
                bean -> {
                    long max = bean.getHeapMemoryUsage().getMax();
                    long used = bean.getHeapMemoryUsage().getUsed();
                    return max > 0 ? (double) used / max * 100 : 0;
                })
            .description("JVM heap memory usage percentage")
            .baseUnit("percent")
            .register(registry);

        // Метрика количества GC циклов и времени
        // Обратите внимание, что Micrometer уже предоставляет подобные метрики,
        // но здесь показан пример создания собственной метрики для GC

        // Счетчик активных потоков по типам
        Gauge.builder("jvm.threads.daemon", threadBean, ThreadMXBean::getDaemonThreadCount)
            .description("JVM daemon thread count")
            .register(registry);

        Gauge.builder("jvm.threads.peak", threadBean, ThreadMXBean::getPeakThreadCount)
            .description("JVM peak thread count")
            .register(registry);

        // Время CPU для потоков
        if (threadBean.isCurrentThreadCpuTimeSupported()) {
            Gauge.builder("jvm.threads.cpu.time", threadBean,
                    bean -> {
                        long[] ids = bean.getAllThreadIds();
                        long totalCpuTime = 0;
                        for (long id : ids) {
                            totalCpuTime += bean.getThreadCpuTime(id);
                        }
                        return totalCpuTime / 1_000_000_000.0; // Конвертируем из наносекунд в секунды
                    })
                .description("JVM total thread CPU time")
                .baseUnit("seconds")
                .register(registry);
        }
    }

    /**
     * Регистрирует метрики операционной системы.
     *
     * @param registry реестр метрик
     */
    private void registerOsMetrics(MeterRegistry registry) {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();

        // Базовые метрики ОС
        Gauge.builder("system.cpu.count", osBean, OperatingSystemMXBean::getAvailableProcessors)
            .description("Number of available processors")
            .register(registry);

        Gauge.builder("system.load.average", osBean, bean -> {
                double loadAverage = bean.getSystemLoadAverage();
                return loadAverage < 0 ? 0 : loadAverage;
            })
            .description("System load average")
            .register(registry);

        // Расширенные метрики ОС с использованием отражения для доступа к закрытым методам
        // Обратите внимание, что это зависит от реализации и может не работать на всех JVM
        try {
            // Метрика использования CPU системой - требует Sun/Oracle JVM
            registerOsMetricIfAvailable(registry, osBean, "system.cpu.usage",
                "getCpuLoad", "System CPU usage");

            // Метрика использования CPU процессом
            registerOsMetricIfAvailable(registry, osBean, "process.cpu.usage",
                "getProcessCpuLoad", "Process CPU usage");

            // Метрика общей физической памяти
            registerOsMetricIfAvailable(registry, osBean, "system.memory.total",
                "getTotalPhysicalMemorySize", "Total physical memory");

            // Метрика свободной физической памяти
            registerOsMetricIfAvailable(registry, osBean, "system.memory.free",
                "getFreePhysicalMemorySize", "Free physical memory");

        } catch (Exception e) {
            log.warn("Failed to register some extended OS metrics: {}", e.getMessage());
        }
    }

    /**
     * Регистрирует метрику операционной системы, если соответствующий метод доступен.
     *
     * @param registry реестр метрик
     * @param osBean bean операционной системы
     * @param metricName имя метрики
     * @param methodName имя метода для вызова через reflection
     * @param description описание метрики
     */
    private void registerOsMetricIfAvailable(MeterRegistry registry, OperatingSystemMXBean osBean,
                                             String metricName, String methodName, String description) {
        try {
            // Используем отражение для доступа к закрытым методам API
            java.lang.reflect.Method method = osBean.getClass().getDeclaredMethod(methodName);
            method.setAccessible(true);

            // Создаем функцию для получения значения метрики
            ToDoubleFunction<OperatingSystemMXBean> function = bean -> {
                try {
                    Object value = method.invoke(bean);
                    if (value instanceof Number) {
                        return ((Number) value).doubleValue();
                    }
                    return 0.0;
                } catch (Exception e) {
                    return 0.0;
                }
            };

            // Регистрируем метрику
            Gauge.builder(metricName, osBean, function)
                .description(description)
                .register(registry);
        } catch (Exception e) {
            log.debug("Method {} not available on OperatingSystemMXBean", methodName);
        }
    }
}