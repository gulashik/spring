package org.gulash;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Демонстрационный класс для изучения работы с {@link ScheduledExecutorService}.
 *
 * <p>Класс демонстрирует два подхода к корректному завершению:</p>
 * <ul>
 *   <li><b>Ручное управление</b>: последовательность shutdown() → awaitTermination() → shutdownNow()</li>
 *   <li><b>Автоматическое управление</b>: использование wrapper-класса {@link ManagedScheduler} с try-with-resources</li>
 * </ul>
 */
public class SimpleScheduledExecutorService {

    public static void main(String[] args) throws InterruptedException {
        // Константы для конфигурации
        final int THREAD_POOL_SIZE = 3;
        final long EXECUTION_TIME_SECONDS = 10;
        final long SHUTDOWN_TIMEOUT_SECONDS = 5;

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
                System.out.println("Задача с правильной обработкой исключений");
                if (System.currentTimeMillis() % 3 == 0) {
                    throw new RuntimeException("Симуляция ошибки");
                }
                System.out.println("Задача выполнена успешно");
            } catch (Exception e) {
                // В отличие от Timer, ScheduledExecutorService не останавливается при исключениях.
                // НО НУЖНО ОБРАБАТЫВАТЬ ОШИБКИ! Для конкретного завершения текущей задачи.
                System.out.println("Ошибка в задаче: " + e.getMessage());
            }
        };

        try (
            // Используем try-with-resources + Комбинацию с ожиданием завершения shutdown() → awaitTermination() → shutdownNow()
            //  т.к. try-with-resources использует только shutdown т.е. может зависнуть.
            // Пул потоков
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(THREAD_POOL_SIZE);
            // Один поток
            ScheduledExecutorService schedulerSingle = Executors.newSingleThreadScheduledExecutor();
            // Для обёртки с "правильной" имплементацией closable можно использовать только try-with-resources
            ManagedScheduler managedScheduler = new ManagedScheduler(1, SHUTDOWN_TIMEOUT_SECONDS);
        ) {
            // Одноразовое выполнение задачи
            ScheduledFuture<?> oneTimeTask = scheduler.schedule(
                runnableWithCorrectExceptionHandling,
                2, TimeUnit.SECONDS
            );

            // FixedDelay: через 2 сек после ОКОНЧАНИЯ предыдущей задачи - вроде как удобно, чтобы не зависало.
            ScheduledFuture<?> fixedDelayTask = scheduler.scheduleWithFixedDelay(
                runnableWithCorrectExceptionHandling,
                1, 2, TimeUnit.SECONDS
            );

            // FixedRate: каждые 2 сек от НАЧАЛА предыдущей задачи
            ScheduledFuture<?> fixedRateTask = schedulerSingle.scheduleAtFixedRate(
                runnableWithCorrectExceptionHandling,
                1, 2, TimeUnit.SECONDS
            );

            managedScheduler.getScheduler().scheduleAtFixedRate(
                runnableWithCorrectExceptionHandling,
                1, 2, TimeUnit.SECONDS
            );

            // Ожидаем выполнения задач
            Thread.sleep(EXECUTION_TIME_SECONDS * 1000);

            // СО СТОРОНЫ ЗАДАЧИ. Два варианта отмены (Впринципе удобней будет shutdown):
            // мягкая" отмена: cancel(false)
            //  - Не прерывает уже выполняющуюся задачу
            //  - Только предотвращает запуск будущих выполнений
            //  - Текущее выполнение завершится естественным образом
            //  - Безопасно для задач, которые не проверяют статус прерывания
            //
            // cancel(true) - жесткая" отмена
            //  - Прерывает ТЕКУЩУЮ выполняющуюся задачу через `Thread.interrupt()`
            //  - Также предотвращает будущие выполнения
            //  - Задача должна правильно обрабатывать `InterruptedException`
            //  - Может привести к неожиданному состоянию, если задача не готова к прерыванию
            // Отменяем периодические задачи перед завершением
            fixedDelayTask.cancel(false);
            fixedRateTask.cancel(false);


            List<ScheduledExecutorService> schedulers = List.of(scheduler, schedulerSingle);

            // СО СТОРОНЫ ПЛАНИРОВЩИКА
            // Используем try-with-resources + Комбинацию с ожиданием завершения shutdown() → awaitTermination() → shutdownNow()
            //  т.к. try-with-resources использует только shutdown т.е. может зависнуть.
            for (ScheduledExecutorService currScheduler : schedulers) {
                try {
                    // Graceful shutdown с ожиданием завершения
                    // Метод инициирует упорядоченное завершение: shutdown()
                    //  - Новые задачи больше не принимаются
                    //  - Ранее запланированные задачи продолжают выполняться
                    //  - Executor НЕ завершается немедленно
                    System.out.println("Awaiting scheduler shutdown... ");
                    currScheduler.shutdown();

                    // awaitTermination() ожидает завершения всех задач:
                    //  - Блокирует текущий поток на указанное время (5 секунд)
                    //  - Возвращает `true`, если все задачи завершились
                    //  - Возвращает `false`, если время ожидания истекло
                    System.out.println("Awaiting scheduler shutdown... " + SHUTDOWN_TIMEOUT_SECONDS + " seconds...");
                    if (!currScheduler.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                        // Если задачи не завершились за отведенное время, вызывается принудительное завершение:
                        //  - прерывает выполняющиеся задачи `shutdownNow()`
                        //  - Отменяет ожидающие задачи
                        //  - Возвращает список неначатых задач
                        System.out.println("Forcing shutdownNow... " + SHUTDOWN_TIMEOUT_SECONDS + " seconds...");
                        currScheduler.shutdownNow();

                        if (!currScheduler.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                            System.out.println("Unsuccessful call for 'shutdownNow'");
                        }
                    } else {
                        System.out.println("Scheduler shutdown successfully");
                    }
                } catch (InterruptedException e) {
                    System.out.println("Interrupted while waiting for scheduler shutdown. Forcing shutdownNow...");
                    currScheduler.shutdownNow();
                    System.out.println("Interrupted exception: " + e.getMessage());
                    Thread.currentThread().interrupt();
                }
            }
        }
    }


    /**
     * Обёртка для {@link ScheduledExecutorService}, реализующая {@link AutoCloseable}.
     *
     * <p>Этот класс предоставляет удобный способ работы с планировщиком задач
     * в конструкции try-with-resources, гарантируя корректное завершение работы
     * даже при возникновении исключений.</p>
     *
     * <h3>Преимущества использования:</h3>
     * <ul>
     *   <li>Автоматическое управление жизненным циклом планировщика</li>
     *   <li>Гарантированное корректное завершение через {@link #close()}</li>
     *   <li>Обработка прерываний и таймаутов</li>
     *   <li>Логирование процесса завершения для отладки</li>
     * </ul>
     *
     * <h3>Пример использования:</h3>
     * <pre>{@code
     * try (ManagedScheduler managedScheduler = new ManagedScheduler(2, 5)) {
     *     ScheduledExecutorService scheduler = managedScheduler.getScheduler();
     *     scheduler.scheduleAtFixedRate(() -> {
     *         // Ваша задача
     *     }, 0, 1, TimeUnit.SECONDS);
     * } // Автоматическое корректное завершение
     * }</pre>
     *
     * @see AutoCloseable
     * @see ScheduledExecutorService
     */
    static class ManagedScheduler implements AutoCloseable {

        /**
         * Планировщик задач, управляемый данным экземпляром
         */
        private final ScheduledExecutorService scheduler;

        /**
         * Таймаут ожидания завершения в секундах
         */
        private final long timeoutSeconds;

        /**
         * Создает управляемый планировщик с указанными параметрами.
         *
         * @param poolSize       размер пула потоков для планировщика
         * @param timeoutSeconds максимальное время ожидания завершения в секундах
         * @throws IllegalArgumentException если poolSize меньше 1 или timeoutSeconds меньше 0
         * @see Executors#newScheduledThreadPool(int)
         */
        public ManagedScheduler(int poolSize, long timeoutSeconds) {
            this.scheduler = Executors.newScheduledThreadPool(poolSize);
            this.timeoutSeconds = timeoutSeconds;
        }

        /**
         * Возвращает планировщик задач.
         *
         * @return экземпляр {@link ScheduledExecutorService}
         */
        public ScheduledExecutorService getScheduler() {
            return scheduler;
        }

        /**
         * Корректно завершает работу планировщика с обработкой таймаутов и прерываний.
         *
         * <p>Последовательность завершения:</p>
         * <ol>
         *   <li>Вызов {@link ScheduledExecutorService#shutdown()} - запрет новых задач</li>
         *   <li>Ожидание завершения текущих задач в течение {@link #timeoutSeconds}</li>
         *   <li>При превышении таймаута - принудительное завершение через {@link ScheduledExecutorService#shutdownNow()}</li>
         *   <li>Обработка {@link InterruptedException} с восстановлением статуса прерывания</li>
         * </ol>
         *
         * @see ScheduledExecutorService#shutdown()
         * @see ScheduledExecutorService#awaitTermination(long, TimeUnit)
         * @see ScheduledExecutorService#shutdownNow()
         */
        @Override
        public void close() {
            try {
                // Graceful shutdown с ожиданием завершения
                // Метод инициирует упорядоченное завершение: shutdown()
                //  - Новые задачи больше не принимаются
                //  - Ранее запланированные задачи продолжают выполняться
                //  - Executor НЕ завершается немедленно
                System.out.println("Awaiting ManagedScheduler shutdown... ");
                scheduler.shutdown();

                // awaitTermination() ожидает завершения всех задач:
                //  - Блокирует текущий поток на указанное время (5 секунд)
                //  - Возвращает `true`, если все задачи завершились
                //  - Возвращает `false`, если время ожидания истекло
                System.out.println("Awaiting ManagedScheduler shutdown... " + timeoutSeconds + " seconds...");
                if (!scheduler.awaitTermination(timeoutSeconds, TimeUnit.SECONDS)) {
                    // Если задачи не завершились за отведенное время, вызывается принудительное завершение:
                    //  - прерывает выполняющиеся задачи `shutdownNow()`
                    //  - Отменяет ожидающие задачи
                    //  - Возвращает список неначатых задач
                    System.out.println("Forcing shutdownNow... " + timeoutSeconds + " seconds...");
                    scheduler.shutdownNow();

                    if (!scheduler.awaitTermination(timeoutSeconds, TimeUnit.SECONDS)) {
                        System.out.println("Unsuccessful call for 'shutdownNow'");
                    }
                } else {
                    System.out.println("ManagedScheduler shutdown successfully");
                }
            } catch (InterruptedException e) {
                System.out.println("Interrupted while waiting for ManagedScheduler shutdown. Forcing shutdownNow...");
                scheduler.shutdownNow();
                System.out.println("Interrupted exception: " + e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
    }
}