package ru.otus.hw;

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
 * Многопоточный сервер, способный обслуживать несколько клиентов одновременно
 *
 * Принципы работы:
 * - Создает ServerSocket для прослушивания порта
 * - Для каждого клиента создает отдельный поток обработки
 * - Использует ExecutorService для управления пулом потоков
 */
public class EchoServer {
    private final int port;
    private final ExecutorService executorService;
    private ServerSocket serverSocket;
    private volatile boolean isRunning = false;

    /**
     * Конструктор сервера
     * @param port порт для прослушивания (рекомендуется использовать порты > 1024)
     */
    public EchoServer(int port) {
        this.port = port;
        // Создаем кэшированный пул потоков для обработки клиентов
        this.executorService = Executors.newCachedThreadPool();
    }

    /**
     * Запуск сервера
     *
     * Best Practice: Использование try-with-resources для автоматического
     * управления ресурсами и обработка исключений
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
        }
    }

    /**
     * Остановка сервера
     *
     * Важно: Корректное завершение работы с освобождением ресурсов
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
                executorService.shutdownNow();
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Ошибка при остановке сервера: " + e.getMessage());
        }
    }

    /**
     * Обработчик клиентских соединений
     *
     * Каждый экземпляр работает в отдельном потоке и обслуживает одного клиента
     */
    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            // Try-with-resources автоматически закроет ресурсы
            try (
                // Создаем потоки для чтения и записи
                BufferedReader in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(
                    clientSocket.getOutputStream(), true);
            ) {

                String inputLine;
                // Читаем сообщения от клиента до получения "bye"
                while ((inputLine = in.readLine()) != null) {
                    System.out.println("Получено от клиента: " + inputLine);

                    // Команда для завершения соединения
                    if ("bye".equalsIgnoreCase(inputLine.trim())) {
                        out.println("До свидания!");
                        break;
                    }

                    // Эхо - отправляем сообщение обратно с префиксом
                    out.println("Эхо: " + inputLine);
                }

            } catch (IOException e) {
                System.err.println("Ошибка при обработке клиента: " + e.getMessage());
            } finally {
                // Закрываем сокет клиента
                try {
                    clientSocket.close();
                    System.out.println("Клиент отключен: " +
                        clientSocket.getRemoteSocketAddress());
                } catch (IOException e) {
                    System.err.println("Ошибка при закрытии сокета: " + e.getMessage());
                }
            }
        }
    }
}

