package org.gulash;


import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CustomThreadFactoryScheduledExecutorService {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("\n--- Демонстрация создание с CustomThreadFactory ---");

        /*
         * Демонстрационная задача с корректной обработкой исключений для {@link ScheduledExecutorService}.
         * // ScheduledExecutorService - продолжает работу:
         * ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
         * executor.scheduleAtFixedRate( () -> {
         *     try {
         *         // логика
         *     } catch (Exception e) {
         *         // обработка исключения - выполнение продолжается
         *     }
         * }, 0, 1, TimeUnit.SECONDS);
         */
        Runnable runnableWithCorrectExceptionHandling = () -> {
            try {
                System.out.println("Thread = " + Thread.currentThread().getName() + " Задача с правильной обработкой исключений");
                if (System.currentTimeMillis() % 3 == 0) {
                    throw new RuntimeException("Симуляция ошибки");
                }
                System.out.println("Thread = " + Thread.currentThread().getName() + " Задача выполнена успешно");
            } catch (Exception e) {
                // В отличие от Timer, ScheduledExecutorService не останавливается при исключениях.
                // НО НУЖНО ОБРАБАТЫВАТЬ ОШИБКИ! Для конкретного завершения текущей задачи.
                System.out.println("Thread = " + Thread.currentThread().getName() + " Ошибка в задаче: " + e.getMessage());
            }
        };

        // С кастомной ThreadFactory
        ScheduledExecutorService customExecutor =
            Executors.newScheduledThreadPool(1, new CustomThreadFactory("Demo"));

        customExecutor.scheduleWithFixedDelay(
            runnableWithCorrectExceptionHandling,
            1, 3, TimeUnit.SECONDS
        );

        // Даем время на выполнение задач
        Thread.sleep(15000);

        // Корректное завершение работы
        shutdownExecutorGracefully(customExecutor, "Custom");

    }

    /**
     * Кастомная ThreadFactory для демонстрации настройки потоков
     *
     * <p>ThreadFactory - это функциональный интерфейс, позволяющий настроить
     * создание потоков. Основные возможности:
     * - Именование потоков для упрощения отладки
     * - Установка daemon-статуса
     * - Настройка приоритета
     * - Добавление UncaughtExceptionHandler
     * - Группировка потоков
     * <p>
     * Примечание: SecurityManager deprecated с Java 17, поэтому используем
     * ThreadGroup текущего потока напрямую.</p>
     */
    static class CustomThreadFactory implements ThreadFactory {
        private final String namePrefix;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final ThreadGroup group;

        /**
         * Создает ThreadFactory с указанным префиксом имени
         *
         * @param namePrefix префикс для имен создаваемых потоков
         */
        public CustomThreadFactory(String namePrefix) {
            this.namePrefix = namePrefix;
            // Используем ThreadGroup текущего потока (SecurityManager deprecated с Java 17)
            group = Thread.currentThread().getThreadGroup();
        }

        /**
         * Создает новый поток с настройками
         *
         * @param runnable задача для выполнения
         * @return настроенный поток
         */
        @Override
        public Thread newThread(Runnable runnable) {
            String threadName = namePrefix + "-Thread-" + threadNumber.getAndIncrement();
            Thread thread = new Thread(group, runnable, threadName, 0);

            // Настройки потока
            if (thread.isDaemon()) {
                thread.setDaemon(false); // Делаем non-daemon
            }
            if (thread.getPriority() != Thread.NORM_PRIORITY) {
                thread.setPriority(Thread.NORM_PRIORITY);
            }

            // Добавляем обработчик исключений
            thread.setUncaughtExceptionHandler((t, e) -> {
                System.out.println("Необработанное исключение в потоке " + t.getName());
            });

            System.out.println("Создан поток: " + threadName);
            return thread;
        }
    }

    /**
     * Корректно завершает работу executor-а с таймаутом
     *
     * <p>Правильное завершение ExecutorService критично для предотвращения
     * утечек ресурсов и зависших потоков. Последовательность:
     * 1. shutdown() - запрещает новые задачи
     * 2. awaitTermination() - ждет завершения текущих задач
     * 3. shutdownNow() - принудительно прерывает оставшиеся задачи</p>
     *
     * @param executor executor для завершения
     * @param name     имя для логирования
     */
    private static void shutdownExecutorGracefully(ExecutorService executor, String name) {
        final int SHUTDOWN_TIMEOUT_SECONDS = 5;

        try {
            // Graceful shutdown с ожиданием завершения
            // Метод инициирует упорядоченное завершение: shutdown()
            //  - Новые задачи больше не принимаются
            //  - Ранее запланированные задачи продолжают выполняться
            //  - Executor НЕ завершается немедленно
            System.out.println("Awaiting scheduler shutdown... ");
            executor.shutdown();

            // awaitTermination() ожидает завершения всех задач:
            //  - Блокирует текущий поток на указанное время (5 секунд)
            //  - Возвращает `true`, если все задачи завершились
            //  - Возвращает `false`, если время ожидания истекло
            System.out.println("Awaiting scheduler shutdown... " + SHUTDOWN_TIMEOUT_SECONDS + " seconds...");
            if (!executor.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                // Если задачи не завершились за отведенное время, вызывается принудительное завершение:
                //  - прерывает выполняющиеся задачи `shutdownNow()`
                //  - Отменяет ожидающие задачи
                //  - Возвращает список неначатых задач
                System.out.println(name + " executor не завершился за " + SHUTDOWN_TIMEOUT_SECONDS + "  секунд, принудительно останавливаем");
                executor.shutdownNow();

                if (!executor.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                    System.out.println(name + " executor не удалось остановить");
                }
            } else {
                System.out.println(name + " executor успешно завершен");
            }
        } catch (InterruptedException e) {
            System.out.println(name + " executor Interrupted while waiting for scheduler shutdown. Forcing shutdownNow...");
            executor.shutdownNow();
            System.out.println("Interrupted exception: " + e.getMessage());
            Thread.currentThread().interrupt();
        }

        System.out.println("Завершаем работу " + name + " executor...");
    }
}