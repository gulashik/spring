package org.gulash;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Демонстрирует создание системы мониторинга с помощью ScheduledExecutorService
 *
 * <h4>Подводные камни:</h4>
 * <ul>
 *   <li>Накопление задач при медленном выполнении - используйте scheduleWithFixedDelay</li>
 *   <li>Утечки памяти при неправильном завершении</li>
 *   <li>Блокировка потоков долгими операциями</li>
 *   <li>Необработанные исключения останавливают периодические задачи</li>
 * </ul>
 */
public class DaemonScheduledExecutor {

    private static final Logger LOGGER = Logger.getLogger(DaemonScheduledExecutor.class.getName());
    private final ScheduledExecutorService scheduler;
    private final AtomicLong systemLoadChecks = new AtomicLong(0);

    public DaemonScheduledExecutor() {
        // Создаём планировщик с ThreadFactory и указываем там setDaemon(true)
        this.scheduler = Executors.newSingleThreadScheduledExecutor(
            new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "MonitoringThread");
                    t.setDaemon(true); // Указываем, что это Daemon поток
                    return t;
                }
            }
        );
    }

    /**
     * Запускает мониторинг системы
     */
    public void startMonitoring() {
        // Проверка загрузки системы каждые 30 секунд
        scheduler.scheduleAtFixedRate(this::checkSystemLoad, 0, 30, TimeUnit.SECONDS);

        // Очистка старых данных каждый час
        scheduler.scheduleAtFixedRate(this::cleanupOldData, 1, 1, TimeUnit.HOURS);

        // Генерация отчета каждые 5 минут
        scheduler.scheduleWithFixedDelay(this::generateReport, 5, 5, TimeUnit.MINUTES);
    }

    private void checkSystemLoad() {
        try {
            long checkNumber = systemLoadChecks.incrementAndGet();
            double cpuLoad = getSystemCpuLoad();
            LOGGER.info(String.format("Проверка #%d: загрузка CPU %.2f%%", checkNumber, cpuLoad));

            if (cpuLoad > 80.0) {
                LOGGER.warning("Высокая загрузка CPU: " + cpuLoad + "%");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Ошибка при проверке загрузки системы", e);
        }
    }

    private void cleanupOldData() {
        LOGGER.info("Очистка старых данных мониторинга");
        // Имитация очистки
        /*
         * try {
         *      // логика
         * } catch (Exception e) {
         *     // обработка исключения - выполнение продолжается
         *  }
         */
    }

    private void generateReport() {
        LOGGER.info("Генерация отчета мониторинга");
        // Имитация генерации отчета
        /*
         * try {
         *      // логика
         * } catch (Exception e) {
         *     // обработка исключения - выполнение продолжается
         *  }
         */
    }

    private double getSystemCpuLoad() {
        // Имитация получения загрузки CPU
        return Math.random() * 100;
    }

    public void shutdown() {
        // Можно особо не заморачиваться, т.к. это Демон процесс будет завершён автоматически при завершении приложения.
        scheduler.shutdown();
    }
}