package ru.gulash.multithreaded;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Клиент для подключения к серверу и обмена сообщениями.
 * 
 * <p><strong>Образовательный момент:</strong><br>
 * Данный класс демонстрирует основы сетевого программирования в Java:
 * <ul>
 *   <li>Использование класса {@link Socket} для создания TCP-соединения</li>
 *   <li>Работу с потоками ввода-вывода для передачи данных по сети</li>
 *   <li>Паттерн try-with-resources для автоматического управления ресурсами</li>
 *   <li>Обработку сетевых исключений</li>
 * </ul>
 * 
 * <p><strong>Пример использования:</strong></p>
 * <pre>{@code
 * EchoClient client = new EchoClient("localhost", 8080);
 * client.connectAndInteractWithServerAndUser();
 * }</pre>
 *
 */
public class EchoClient {
    
    /**
     * Имя хоста или IP-адрес сервера для подключения.
     * 
     * <p><strong>Образовательный момент:</strong><br>
     * Hostname может быть как IP-адресом (например, "192.168.1.100"),
     * так и доменным именем (например, "localhost" или "example.com").
     * Java автоматически разрешает доменные имена в IP-адреса через DNS.</p>
     */
    private final String hostname;
    
    /**
     * Номер порта сервера для подключения.
     * 
     * <p><strong>Образовательный момент:</strong><br>
     * Порт - это числовой идентификатор (1024-49151), который позволяет
     * различать разные сервисы на одном хосте. Порты 1-1023 зарезервированы
     * для системных служб, поэтому для пользовательских приложений
     * используется диапазон 1024-49151, а порты 49152-65535 предназначены
     * для динамического выделения операционной системой.</p>
     */
    private final int port;

    /**
     * Создает новый экземпляр клиента с указанными параметрами подключения.
     * 
     * <p><strong>Образовательный момент:</strong><br>
     * Конструктор не устанавливает соединение сразу - это позволяет
     * создать объект клиента и позже решить, когда именно подключаться.
     * Такой подход дает больше гибкости в управлении жизненным циклом соединения.</p>
     * 
     * @param hostname адрес сервера (IP-адрес или доменное имя), не может быть {@code null}
     * @param port     номер порта сервера (должен быть в диапазоне 1024-49151)
     * 
     * @throws IllegalArgumentException если hostname равен {@code null} или пустой строке,
     *                                  или если port находится вне допустимого диапазона
     */
    public EchoClient(String hostname, int port) {
        if (hostname == null || hostname.trim().isEmpty()) {
            throw new IllegalArgumentException("Hostname не может быть null или пустым");
        }
        if (port < 1024 || port > 49151) {
            throw new IllegalArgumentException("Port должен быть в диапазоне 1024-49151");
        }
        
        this.hostname = hostname;
        this.port = port;
    }

    /**
     * Устанавливает соединение с сервером и запускает интерактивный обмен сообщениями.
     * 
     * <p><strong>Образовательный момент:</strong><br>
     * Этот метод демонстрирует несколько важных концепций:
     * <ul>
     *   <li><strong>Try-with-resources:</strong> Автоматическое управление ресурсами.
     *       Все объекты, реализующие {@link AutoCloseable}, будут автоматически
     *       закрыты при выходе из блока try, даже если произойдет исключение.</li>
     *   <li><strong>Буферизация:</strong> {@link BufferedReader} и {@link PrintWriter}
     *       обеспечивают эффективное чтение и запись данных, минимизируя
     *       количество системных вызовов.</li>
     *   <li><strong>Протокол общения:</strong> Простой текстовый протокол "запрос-ответ",
     *       где каждая строка - отдельное сообщение.</li>
     *   <li><strong>Graceful shutdown:</strong> Команда "bye" позволяет корректно
     *       завершить сессию.</li>
     * </ul></p>
     * 
     * <p><strong>Поток выполнения:</strong></p>
     * <ol>
     *   <li>Создание сокетного соединения</li>
     *   <li>Настройка потоков ввода-вывода</li>
     *   <li>Информирование пользователя об успешном подключении</li>
     *   <li>Цикл чтения пользовательского ввода и обмена с сервером</li>
     *   <li>Автоматическое закрытие всех ресурсов</li>
     * </ol>
     * 
     * @throws UnknownHostException если указанный hostname не может быть разрешен
     * @throws IOException          если возникает ошибка при создании сокета или
     *                             работе с потоками ввода-вывода
     * 
     * @see Socket
     * @see PrintWriter
     * @see BufferedReader
     */
    public void connectAndInteractWithServerAndUser() {
        try ( // try-with-resources
            // Создаем сокет и подключаемся к серверу
            Socket socket = new Socket(hostname, port);

            // Создаем потоки для общения с сервером
            PrintWriter out = new PrintWriter(
                socket.getOutputStream(), true
            );
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
            );

            // Для чтения ввода пользователя
            BufferedReader stdIn = new BufferedReader(
                new InputStreamReader(System.in)
            )
        ) {

            System.out.println("Подключен к серверу " + hostname + ":" + port);
            System.out.println("Введите сообщения (введите 'bye' для выхода):");

            String userInput;
            // Цикл отправки сообщений
            while ((userInput = stdIn.readLine()) != null) {
                // Отправляем сообщение серверу
                out.println(userInput);

                // Читаем ответ от сервера
                String response = in.readLine();
                System.out.println("Ответ сервера: " + response);

                // Выходим если отправили "bye"
                if ("bye".equalsIgnoreCase(userInput.trim())) {
                    break;
                }
            }

        } catch (UnknownHostException e) {
            System.err.println("Неизвестный хост: " + hostname);
        } catch (IOException e) {
            System.err.println("Ошибка подключения к серверу: " + e.getMessage());
        }
    }
    
    /**
     * Возвращает адрес сервера, к которому будет подключаться клиент.
     * 
     * @return hostname адрес сервера
     */
    public String getHostname() {
        return hostname;
    }
    
    /**
     * Возвращает номер порта сервера, к которому будет подключаться клиент.
     * 
     * @return port номер порта
     */
    public int getPort() {
        return port;
    }
}