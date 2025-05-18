package org.gualsh.demo.micromet.config;

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
import java.util.function.ToDoubleFunction;

/**
 * Компонент для демонстрации создания нестандартных метрик с Micrometer.
 * <p>
 * Этот класс реализует интерфейс {@link MeterBinder}, который позволяет автоматически
 * регистрировать пользовательские метрики при запуске приложения. Он демонстрирует, как
 * создавать собственные метрики, которые не предоставляются встроенными биндерами
 * Micrometer, например:
 * </p>
 * <ul>
 *   <li>Метрики файловой системы (доступное пространство, занятое место)</li>
 *   <li>Расширенные метрики JVM (использование памяти, активность потоков)</li>
 *   <li>Метрики операционной системы (загрузка CPU, использование памяти)</li>
 * </ul>
 * <p>
 * Класс использует Java Management Extensions (JMX) для получения системной информации
 * и преобразует её в метрики Micrometer, которые могут быть экспортированы в различные
 * системы мониторинга, такие как Prometheus, Graphite или Datadog.
 * </p>
 * <p>
 * Примечание: Некоторые метрики зависят от конкретной реализации JVM и могут
 * быть недоступны в некоторых средах выполнения.
 * </p>
 *
 * @see MeterBinder
 * @see MeterRegistry
 * @see Gauge
 */
@Component
@Slf4j
public class CustomMetricsConfig implements MeterBinder {

    /**
     * Пути к дисковым разделам, для которых будут собираться метрики.
     * <p>
     * Включает корневой каталог ("/"), временный каталог ("/tmp") и домашний
     * каталог пользователей ("/home"). В реальных приложениях этот список может
     * быть настраиваемым через конфигурацию или определяться динамически.
     * </p>
     */
    private static final String[] DISK_PATHS = {"/", "/tmp", "/home"};

    /**
     * Регистрирует нестандартные метрики в реестре метрик.
     * <p>
     * Этот метод вызывается автоматически фреймворком Spring при инициализации
     * контекста приложения. Он последовательно регистрирует различные группы
     * пользовательских метрик.
     * </p>
     *
     * @param registry реестр метрик Micrometer, куда будут добавлены все
     *                пользовательские метрики
     */
    @Override
    public void bindTo(MeterRegistry registry) {
        log.info("Регистрация пользовательских метрик");

        // Регистрируем метрики файловой системы
        registerFileSystemMetrics(registry);

        // Регистрируем дополнительные метрики JVM
        registerJvmExtendedMetrics(registry);

        // Регистрируем метрики операционной системы
        registerOsMetrics(registry);

        log.info("Пользовательские метрики зарегистрированы");
    }

    /**
     * Регистрирует метрики файловой системы.
     * <p>
     * Для каждого указанного пути в {@link #DISK_PATHS} создаются следующие метрики:
     * <ul>
     *   <li>disk.total - общий размер дискового раздела в байтах</li>
     *   <li>disk.free - свободное место на разделе в байтах</li>
     *   <li>disk.used - используемое место на разделе в байтах</li>
     *   <li>disk.usage.ratio - коэффициент использования (0.0-1.0)</li>
     * </ul>
     * </p>
     * <p>
     * Каждая метрика снабжается тегами для идентификации пути и типа файловой системы.
     * </p>
     *
     * @param registry реестр метрик Micrometer для регистрации метрик файловой системы
     */
    private void registerFileSystemMetrics(MeterRegistry registry) {
        // Для каждого пути создаем метрики диска
        Arrays.stream(DISK_PATHS).forEach(path -> {
                File file = new File(path);

                // Пропускаем несуществующие пути
                if (!file.exists()) {
                    log.debug("Пропускаем несуществующий путь: {}", path);
                    return;
                }

                Tags tags = Tags.of(
                    Tag.of("path", path),
                    Tag.of("fs_type", "unknown") // В реальном приложении можно определить тип ФС
                );

                // Регистрируем метрику общего размера диска
                Gauge.builder("disk.total", file, File::getTotalSpace)
                    .tags(tags)
                    .description("Общий размер дискового пространства")
                    .baseUnit("bytes")
                    .register(registry);

                // Регистрируем метрику свободного места на диске
                Gauge.builder("disk.free", file, File::getFreeSpace)
                    .tags(tags)
                    .description("Свободное дисковое пространство")
                    .baseUnit("bytes")
                    .register(registry);

                // Регистрируем метрику используемого места на диске
                Gauge.builder("disk.used", file, f -> f.getTotalSpace() - f.getFreeSpace())
                    .tags(tags)
                    .description("Используемое дисковое пространство")
                    .baseUnit("bytes")
                    .register(registry);

                // Регистрируем процент использования диска
                Gauge.builder("disk.usage.ratio", file,
                        f -> {
                            long total = f.getTotalSpace();
                            return total == 0 ? 0 : (double) (total - f.getFreeSpace()) / total;
                        })
                    .tags(tags)
                    .description("Коэффициент использования диска")
                    .register(registry);

                log.debug("Зарегистрированы метрики для пути: {}", path);
            }
        );
    }

    /**
     * Регистрирует расширенные метрики JVM.
     * <p>
     * Дополняет стандартные метрики JVM, предоставляемые Micrometer,
     * следующими более детальными метриками:
     * <ul>
     *   <li>jvm.memory.used.percentage - процент использования кучи</li>
     *   <li>jvm.threads.daemon - количество фоновых потоков</li>
     *   <li>jvm.threads.peak - пиковое количество потоков</li>
     *   <li>jvm.threads.cpu.time - суммарное время CPU для всех потоков (если поддерживается)</li>
     * </ul>
     * </p>
     *
     * @param registry реестр метрик Micrometer для регистрации расширенных метрик JVM
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
            .description("Процент использования кучи JVM")
            .baseUnit("percent")
            .register(registry);

        // Счетчик активных потоков по типам
        Gauge.builder("jvm.threads.daemon", threadBean, ThreadMXBean::getDaemonThreadCount)
            .description("Количество демон-потоков JVM")
            .register(registry);

        Gauge.builder("jvm.threads.peak", threadBean, ThreadMXBean::getPeakThreadCount)
            .description("Пиковое количество потоков JVM")
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
                        // Конвертируем из наносекунд в секунды
                        return totalCpuTime / 1_000_000_000.0;
                    })
                .description("Общее время CPU для всех потоков JVM")
                .baseUnit("seconds")
                .register(registry);
        } else {
            log.info("Измерение времени CPU для потоков не поддерживается на этой JVM");
        }
    }

    /**
     * Регистрирует метрики операционной системы.
     * <p>
     * Собирает информацию об операционной системе через {@link OperatingSystemMXBean},
     * регистрируя следующие метрики:
     * <ul>
     *   <li>system.cpu.count - количество доступных процессоров</li>
     *   <li>system.load.average - средняя загрузка системы</li>
     *   <li>system.cpu.usage - использование CPU системой (если доступно)</li>
     *   <li>process.cpu.usage - использование CPU текущим процессом (если доступно)</li>
     *   <li>system.memory.total - общий объем физической памяти (если доступно)</li>
     *   <li>system.memory.free - свободный объем физической памяти (если доступно)</li>
     * </ul>
     * </p>
     * <p>
     * Примечание: некоторые метрики доступны только на определенных JVM 
     * и требуют использования рефлексии для доступа к внутренним методам.
     * </p>
     *
     * @param registry реестр метрик Micrometer для регистрации метрик операционной системы
     */
    private void registerOsMetrics(MeterRegistry registry) {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();

        // Базовые метрики ОС
        Gauge.builder("system.cpu.count", osBean, OperatingSystemMXBean::getAvailableProcessors)
            .description("Количество доступных процессоров")
            .register(registry);

        Gauge.builder("system.load.average", osBean, bean -> {
                double loadAverage = bean.getSystemLoadAverage();
                return loadAverage < 0 ? 0 : loadAverage;
            })
            .description("Средняя загрузка системы")
            .register(registry);

        // Расширенные метрики ОС с использованием отражения для доступа к закрытым методам
        // Обратите внимание, что это зависит от реализации и может не работать на всех JVM
        try {
            // Метрика использования CPU системой - требует Sun/Oracle JVM
            registerOsMetricIfAvailable(registry, osBean, "system.cpu.usage",
                "getCpuLoad", "Использование CPU системой");

            // Метрика использования CPU процессом
            registerOsMetricIfAvailable(registry, osBean, "process.cpu.usage",
                "getProcessCpuLoad", "Использование CPU процессом");

            // Метрика общей физической памяти
            registerOsMetricIfAvailable(registry, osBean, "system.memory.total",
                "getTotalPhysicalMemorySize", "Общий объем физической памяти");

            // Метрика свободной физической памяти
            registerOsMetricIfAvailable(registry, osBean, "system.memory.free",
                "getFreePhysicalMemorySize", "Свободный объем физической памяти");

        } catch (Exception e) {
            log.warn("Не удалось зарегистрировать некоторые расширенные метрики ОС: {}", e.getMessage());
        }
    }

    /**
     * Регистрирует метрику операционной системы, если соответствующий метод доступен.
     * <p>
     * Использует рефлексию для вызова неэкспортируемых методов {@link OperatingSystemMXBean},
     * которые могут отличаться в разных реализациях JVM. Этот метод безопасно обрабатывает
     * случаи, когда запрошенный метод недоступен, просто пропуская регистрацию метрики.
     * </p>
     *
     * @param registry    реестр метрик Micrometer
     * @param osBean      экземпляр {@link OperatingSystemMXBean}
     * @param metricName  имя регистрируемой метрики
     * @param methodName  имя метода в {@link OperatingSystemMXBean} для вызова через рефлексию
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
                    log.debug("Ошибка при получении значения для метрики {}: {}", 
                            metricName, e.getMessage());
                    return 0.0;
                }
            };

            // Регистрируем метрику
            Gauge.builder(metricName, osBean, function)
                .description(description)
                .register(registry);
            
            log.debug("Зарегистрирована метрика ОС: {}", metricName);
        } catch (NoSuchMethodException e) {
            log.debug("Метод {} недоступен в OperatingSystemMXBean", methodName);
        } catch (Exception e) {
            log.warn("Не удалось зарегистрировать метрику {}: {}", metricName, e.getMessage());
        }
    }
}