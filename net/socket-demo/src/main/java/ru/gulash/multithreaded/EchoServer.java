package ru.gulash.multithreaded;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Многопоточный эхо-сервер для демонстрации работы с Java Socket API.
 * <p>
 * Этот класс реализует TCP-сервер, способный обслуживать множество клиентов
 * одновременно, используя пул потоков. Сервер принимает текстовые сообщения
 * от клиентов и отправляет их обратно с префиксом "Эхо:".
 * 
 * <h3>Образовательный момент:</h3>
 * <p>
 * Многопоточность в серверных приложениях критически важна для производительности.
 * Использование ExecutorService позволяет:
 * <ul>
 * <li>Ограничить количество одновременных потоков</li>
 * <li>Переиспользовать потоки для новых клиентов</li>
 * <li>Корректно завершать работу сервера</li>
 * <li>Избежать создания неограниченного количества потоков</li>
 * </ul>
 * 
 * <h3>Принципы работы:</h3>
 * <ol>
 * <li>Создается ServerSocket для прослушивания указанного порта</li>
 * <li>В цикле принимаются новые соединения через accept()</li>
 * <li>Каждое соединение передается в отдельный поток через ExecutorService</li>
 * <li>ClientHandler обрабатывает взаимодействие с конкретным клиентом</li>
 * </ol>
 */
public class EchoServer {
    
    /**
     * Порт, на котором сервер прослушивает входящие соединения.
     * <p>
     * <strong>Образовательный момент:</strong>
     * Хранение порта как final поля обеспечивает неизменность конфигурации
     * сервера после создания объекта. Это важный принцип immutable design.
     * Рекомендуется использовать порты 1024-49151 для избежания конфликтов
     */
    private final int port;
    
    /**
     * Пул потоков для обработки клиентских соединений.
     * <p>
     * <strong>Образовательный момент:</strong>
     * ExecutorService абстрагирует управление потоками и предоставляет:
     * <ul>
     * <li>Контролируемое создание и уничтожение потоков</li>
     * <li>Очередь задач для выполнения</li>
     * <li>Возможность корректного завершения работы</li>
     * <li>Различные стратегии управления потоками (cached, fixed, scheduled)</li>
     * </ul>
     * Использование newCachedThreadPool() создает потоки по требованию
     * и переиспользует существующие для новых задач.
     */
    private final ExecutorService executorService;
    
    /**
     * Серверный сокет для принятия входящих соединений.
     * <p>
     * <strong>Образовательный момент:</strong>
     * ServerSocket - это специализированный класс для серверной стороны
     * TCP-соединения. Он связывается с портом и прослушивает входящие
     * подключения. Каждое принятое соединение создает новый Socket
     * для взаимодействия с конкретным клиентом.
     */
    private ServerSocket serverSocket;
    
    /**
     * Флаг состояния сервера для корректного завершения работы.
     * <p>
     * <strong>Образовательный момент:</strong>
     * Модификатор volatile критически важен в многопоточной среде.
     * Он гарантирует, что изменения этого поля будут видны всем потокам
     * немедленно, без кэширования в регистрах процессора. Это обеспечивает
     * корректную остановку сервера из разных потоков.
     */
    private volatile boolean isRunning = false;

    /**
     * Создает новый экземпляр эхо-сервера.
     * <p>
     * Инициализирует сервер с указанным портом и создает кэшированный
     * пул потоков для обработки клиентских соединений.
     * 
     * <h3>Образовательный момент:</h3>
     * <p>
     * Конструктор следует принципу "fail-fast" - все необходимые ресурсы
     * инициализируются сразу. ExecutorService создается здесь, а не в методе
     * start(), что обеспечивает предсказуемое поведение и упрощает тестирование.
     * 
     * @param port порт для прослушивания входящих соединений.
     *             Рекомендуется использовать порты 1025-49151 для избежания
     *             конфликтов с системными службами
     * @throws IllegalArgumentException если порт находится вне допустимого диапазона
     * 
     * @see java.util.concurrent.Executors#newCachedThreadPool()
     */
    public EchoServer(int port) {
        if (port <= 1024 || port >= 49151) {
            throw new IllegalArgumentException("Порт должен быть в диапазоне 1025-49151");
        }
        this.port = port;
        // Создаем кэшированный пул потоков для обработки клиентов
        this.executorService = Executors.newCachedThreadPool();
    }

    /**
     * Запускает сервер и начинает прослушивание указанного порта.
     * <p>
     * Метод создает ServerSocket, связывает его с портом и в цикле
     * принимает новые клиентские соединения. Каждое соединение
     * обрабатывается в отдельном потоке.
     * 
     * <h3>Образовательный момент:</h3>
     * <p>
     * Метод демонстрирует важные концепции серверного программирования:
     * <ul>
     * <li><strong>Блокирующий I/O:</strong> accept() блокирует выполнение до получения соединения</li>
     * <li><strong>Обработка исключений:</strong> различные типы IOException требуют разной обработки</li>
     * <li><strong>Graceful shutdown:</strong> проверка isRunning позволяет корректно завершить работу</li>
     * <li><strong>Делегирование:</strong> создание отдельного потока для каждого клиента</li>
     * </ul>
     * 
     * <h3>Важные моменты реализации:</h3>
     * <ul>
     * <li>Сервер не использует try-with-resources для ServerSocket, 
     *     так как его нужно закрыть в методе stop()</li>
     * <li>Каждая ошибка accept() не останавливает сервер, только логируется</li>
     * <li>Проверка isRunning в catch блоке предотвращает лишние сообщения об ошибках</li>
     * </ul>
     * 
     * @throws RuntimeException если не удалось создать ServerSocket
     * 
     * @see #stop()
     * @see ServerSocket#accept()
     * @see ExecutorService#submit(Runnable)
     */
    public void start() {
        try {
            // Создаем серверный сокет и привязываем к порту
            serverSocket = new ServerSocket(port);
            isRunning = true;

            System.out.println("Сервер запущен на порту " + port);
            System.out.println("Ожидание подключений...");

            // Основной цикл сервера - принимаем соединения
            while (isRunning) {
                try {
                    // accept() блокирует выполнение до поступления соединения
                    Socket clientSocket = serverSocket.accept();

                    System.out.println("Подключен клиент: " +
                        clientSocket.getRemoteSocketAddress());

                    // Каждого клиента обрабатываем в отдельном потоке
                    executorService.submit(new ClientHandler(clientSocket));

                } catch (IOException e) {
                    if (isRunning) {
                        System.err.println("Ошибка при принятии соединения: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Не удалось запустить сервер: " + e.getMessage());
            throw new RuntimeException("Ошибка запуска сервера на порту " + port, e);
        }
    }

    /**
     * Корректно останавливает сервер и освобождает все ресурсы.
     * <p>
     * Метод выполняет graceful shutdown: устанавливает флаг остановки,
     * закрывает ServerSocket, завершает работу пула потоков и ожидает
     * завершения всех активных задач.
     * 
     * <h3>Образовательный момент:</h3>
     * <p>
     * Корректное завершение работы сервера - критически важный аспект:
     * <ul>
     * <li><strong>Graceful shutdown:</strong> позволяет завершить обработку текущих клиентов</li>
     * <li><strong>Управление ресурсами:</strong> предотвращает утечки памяти и дескрипторов</li>
     * <li><strong>Таймаут завершения:</strong> предотвращает зависание при завершении</li>
     * <li><strong>Forced shutdown:</strong> принудительное завершение как fallback</li>
     * </ul>
     * 
     * <h3>Последовательность завершения:</h3>
     * <ol>
     * <li>Установка флага isRunning = false</li>
     * <li>Закрытие ServerSocket (прерывает accept())</li>
     * <li>Вызов shutdown() на ExecutorService</li>
     * <li>Ожидание завершения потоков до 5 секунд</li>
     * <li>Принудительное завершение при превышении таймаута</li>
     * </ol>
     * 
     * @see ExecutorService#shutdown()
     * @see ExecutorService#awaitTermination(long, TimeUnit)
     * @see ExecutorService#shutdownNow()
     */
    public void stop() {
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            executorService.shutdown();

            // Ждем завершения всех задач максимум 5 секунд
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                System.out.println("Принудительное завершение потоков...");
                executorService.shutdownNow();
            }
            System.out.println("Сервер успешно остановлен");
        } catch (IOException | InterruptedException e) {
            System.err.println("Ошибка при остановке сервера: " + e.getMessage());
            // Восстанавливаем статус прерывания потока
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Внутренний класс для обработки клиентских соединений в отдельном потоке.
     * <p>
     * Каждый экземпляр ClientHandler работает с одним клиентским соединением
     * и реализует протокол эхо-сервера: читает сообщения от клиента и
     * отправляет их обратно с префиксом "Эхо:".
     * 
     * <h3>Образовательный момент:</h3>
     * <p>
     * Статический вложенный класс используется здесь по следующим причинам:
     * <ul>
     * <li><strong>Инкапсуляция:</strong> логика обработки клиента изолирована</li>
     * <li><strong>Независимость:</strong> не нужен доступ к экземпляру внешнего класса</li>
     * <li><strong>Производительность:</strong> отсутствие скрытой ссылки на внешний объект</li>
     * <li><strong>Читаемость:</strong> четкое разделение ответственности</li>
     * </ul>
     * 
     * <h3>Протокол взаимодействия:</h3>
     * <ul>
     * <li>Клиент отправляет текстовое сообщение</li>
     * <li>Сервер отвечает "Эхо: [сообщение]"</li>
     * <li>Команда "bye" завершает соединение</li>
     * <li>Соединение автоматически закрывается при ошибке</li>
     * </ul>
     * 
     * @see Runnable
     * @see Socket
     */
    private static class ClientHandler implements Runnable {
        
        /**
         * Сокет для взаимодействия с конкретным клиентом.
         * <p>
         * <strong>Образовательный момент:</strong>
         * Socket представляет двунаправленное TCP-соединение между
         * сервером и клиентом. Он предоставляет InputStream для чтения
         * данных от клиента и OutputStream для отправки данных клиенту.
         * Каждый Socket инкапсулирует всю информацию о соединении:
         * IP-адреса, порты, состояние соединения.
         */
        private final Socket clientSocket;

        /**
         * Создает новый обработчик для клиентского соединения.
         * 
         * <h3>Образовательный момент:</h3>
         * <p>
         * Конструктор принимает Socket, который уже установлен методом accept().
         * Это демонстрирует принцип dependency injection - объект получает
         * все необходимые зависимости через конструктор, что упрощает
         * тестирование и обеспечивает неизменность состояния.
         * 
         * @param clientSocket установленное соединение с клиентом,
         *                    не должно быть null или закрытым
         * @throws IllegalArgumentException если clientSocket равен null
         */
        public ClientHandler(Socket clientSocket) {
            if (clientSocket == null) {
                throw new IllegalArgumentException("Client socket не может быть null");
            }
            this.clientSocket = clientSocket;
        }

        /**
         * Основной метод обработки клиентского соединения.
         * <p>
         * Реализует протокол эхо-сервера: читает строки от клиента,
         * отправляет их обратно с префиксом "Эхо:" до получения команды "bye".
         * 
         * <h3>Образовательный момент:</h3>
         * <p>
         * Метод демонстрирует несколько важных концепций:
         * <ul>
         * <li><strong>Try-with-resources:</strong> автоматическое управление ресурсами</li>
         * <li><strong>Буферизованный I/O:</strong> BufferedReader для эффективного чтения</li>
         * <li><strong>Автоматический flush:</strong> PrintWriter с autoFlush=true</li>
         * <li><strong>Протокол завершения:</strong> команда "bye" для graceful disconnect</li>
         * <li><strong>Обработка исключений:</strong> разделение бизнес-логики и cleanup кода</li>
         * </ul>
         * 
         * <h3>Важные детали реализации:</h3>
         * <ul>
         * <li>BufferedReader оборачивает InputStream для построчного чтения</li>
         * <li>PrintWriter с autoFlush немедленно отправляет данные клиенту</li>
         * <li>Проверка на null предотвращает обработку разорванных соединений</li>
         * <li>trim() удаляет лишние пробелы перед сравнением команд</li>
         * <li>Finally блок гарантирует закрытие сокета даже при исключениях</li>
         * </ul>
         * 
         * @see BufferedReader#readLine()
         * @see PrintWriter#println(String)
         * @see Socket#getInputStream()
         * @see Socket#getOutputStream()
         */
        @Override
        public void run() {
            String clientAddress = clientSocket.getRemoteSocketAddress().toString();
            System.out.println("Начинаем обработку клиента: " + clientAddress);
            
            // Try-with-resources автоматически закроет ресурсы
            try (
                // Создаем потоки для чтения и записи
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            ) {

                String inputLine;
                // Читаем сообщения от клиента до получения "bye"
                while ((inputLine = in.readLine()) != null) {
                    System.out.println("Получено от " + clientAddress + ": " + inputLine);

                    // Команда для завершения соединения
                    if ("bye".equalsIgnoreCase(inputLine.trim())) {
                        out.println("До свидания!");
                        System.out.println("Клиент " + clientAddress + " отправил команду завершения");
                        break;
                    }

                    // Эхо - отправляем сообщение обратно с префиксом
                    out.println("Эхо: " + inputLine);
                }

            } catch (IOException e) {
                System.err.println("Ошибка при обработке клиента " + clientAddress + ": " + e.getMessage());
            } finally {
                // Закрываем сокет клиента
                try {
                    if (!clientSocket.isClosed()) {
                        clientSocket.close();
                        System.out.println("Клиент отключен: " + clientAddress);
                    }
                } catch (IOException e) {
                    System.err.println("Ошибка при закрытии сокета " + clientAddress + ": " + e.getMessage());
                }
            }
        }
    }
}