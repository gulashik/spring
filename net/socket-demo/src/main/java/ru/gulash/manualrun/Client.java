package ru.gulash.manualrun;

import ru.gulash.manualrun.utils.ExampleClient;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

/**
 * Клиентское приложение для взаимодействия с сервером через TCP-соединение.
 * 
 * <p>Этот класс реализует простой интерактивный клиент, который позволяет
 * пользователю отправлять текстовые сообщения серверу и получать ответы.
 * Клиент устанавливает новое соединение для каждого сообщения.</p>
 * 
 * <h3>Образовательный момент:</h3>
 * <p>Данная реализация демонстрирует основные концепции клиентского программирования:
 * <ul>
 *   <li><strong>Socket:</strong> Создание клиентского TCP-соединения с сервером</li>
 *   <li><strong>Try-with-resources:</strong> Автоматическое управление ресурсами
 *       гарантирует закрытие сокета после каждого использования</li>
 *   <li><strong>Интерактивный интерфейс:</strong> Использование Scanner для
 *       получения пользовательского ввода</li>
 *   <li><strong>Паттерн "connection per request":</strong> Новое соединение для
 *       каждого сообщения (простой, но не самый эффективный подход)</li>
 *   <li><strong>Команды протокола:</strong> Специальная команда "exit" для
 *       завершения работы</li>
 * </ul></p>
 * 
 * <p><strong>Архитектурные особенности:</strong></p>
 * <ul>
 *   <li>Создание нового соединения для каждого сообщения</li>
 *   <li>Синхронная обработка запросов (блокирующий I/O)</li>
 *   <li>Простая обработка ошибок с выбросом RuntimeException</li>
 *   <li>Использование ExampleClient для инкапсуляции сетевой логики</li>
 * </ul>
 * 
 * <p><strong>Пример использования:</strong></p>
 * <pre>{@code
 * // Запуск клиента
 * Client.main(new String[]{});
 * 
 * // Взаимодействие:
 * // Введи сообщение: hello
 * // HELLO
 * // Введи сообщение: exit
 * // Сервер закрыл соединение.
 * }</pre>
 *
 * @see ru.gulash.manualrun.utils.ExampleClient
 * @see java.net.Socket
 * @see Server
 */
public class Client {
    
    /**
     * Адрес сервера для подключения.
     * 
     * <p><strong>Образовательный момент:</strong><br>
     * "localhost" - специальное имя хоста, которое всегда указывает на
     * локальную машину (IP-адрес 127.0.0.1). Это удобно для разработки
     * и тестирования, когда клиент и сервер работают на одном компьютере.</p>
     */
    private static final String SERVER_HOST = "localhost";
    
    /**
     * Порт сервера для подключения.
     * 
     * <p><strong>Образовательный момент:</strong><br>
     * Порт должен совпадать с портом, на котором работает сервер.
     * Использование константы предотвращает ошибки и облегчает
     * изменение конфигурации.</p>
     */
    private static final int SERVER_PORT = 8080;
    
    /**
     * Команда для завершения работы клиента.
     * 
     * <p><strong>Образовательный момент:</strong><br>
     * Определение команд протокола как констант обеспечивает
     * согласованность между клиентом и сервером.</p>
     */
    private static final String EXIT_COMMAND = "exit";

    /**
     * Точка входа в клиентское приложение.
     * 
     * <p>Запускает интерактивный цикл, в котором пользователь может
     * вводить сообщения для отправки серверу. Каждое сообщение отправляется
     * в новом TCP-соединении.</p>
     * 
     * <h3>Образовательный момент:</h3>
     * <p>Метод демонстрирует типичную структуру клиентского приложения:
     * <ul>
     *   <li><strong>Интерактивный цикл:</strong> Непрерывное чтение пользовательского
     *       ввода до команды завершения</li>
     *   <li><strong>Try-with-resources для Socket:</strong> Автоматическое закрытие
     *       соединения после каждого запроса</li>
     *   <li><strong>Делегирование сетевой логики:</strong> Использование ExampleClient
     *       для инкапсуляции деталей протокола</li>
     *   <li><strong>Обработка команд:</strong> Специальная логика для команды exit</li>
     *   <li><strong>Преобразование checked exceptions:</strong> IOException
     *       оборачивается в RuntimeException для упрощения</li>
     * </ul></p>
     * 
     * <p><strong>Поток выполнения:</strong></p>
     * <ol>
     *   <li>Создание Scanner для чтения пользовательского ввода</li>
     *   <li>Запуск бесконечного цикла взаимодействия</li>
     *   <li>Для каждой итерации:
     *     <ul>
     *       <li>Создание нового Socket соединения</li>
     *       <li>Создание ExampleClient с потоками сокета</li>
     *       <li>Запрос ввода от пользователя</li>
     *       <li>Проверка команды exit и завершение при необходимости</li>
     *       <li>Отправка сообщения через ExampleClient</li>
     *       <li>Автоматическое закрытие соединения</li>
     *     </ul>
     *   </li>
     * </ol>
     * 
     * <p><strong>Важные особенности реализации:</strong></p>
     * <ul>
     *   <li>Scanner не закрывается, так как это закроет System.in</li>
     *   <li>ExampleClient создается внутри try-with-resources для Socket</li>
     *   <li>Команда exit обрабатывается case-insensitive</li>
     *   <li>IOException преобразуется в RuntimeException для простоты</li>
     * </ul>
     * 
     * @param args аргументы командной строки (не используются)
     * @throws RuntimeException если происходит ошибка сетевого взаимодействия
     * 
     * @see ExampleClient#send(String)
     * @see Socket#Socket(String, int)
     * @see Scanner#nextLine()
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== TCP Клиент ===");
        System.out.println("Подключение к серверу " + SERVER_HOST + ":" + SERVER_PORT);
        System.out.println("Введите 'exit' для завершения работы");
        System.out.println();
        
        while (true) {
            try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT)) {
                // Создаем клиент для взаимодействия с сервером
                ExampleClient client = new ExampleClient(
                    socket.getInputStream(),
                    socket.getOutputStream()
                );
                
                System.out.print("Введи сообщение: ");
                String userMessage = scanner.nextLine();
                
                // Проверяем команду завершения
                if (userMessage.equalsIgnoreCase(EXIT_COMMAND)) {
                    System.out.println("Отправляем команду завершения серверу...");
                    client.send(userMessage);
                    System.out.println("Завершение работы клиента.");
                    break;
                }
                
                // Отправляем обычное сообщение
                client.send(userMessage);
                
            } catch (IOException e) {
                System.err.println("Ошибка подключения к серверу: " + e.getMessage());
                System.err.println("Убедитесь, что сервер запущен на " + SERVER_HOST + ":" + SERVER_PORT);
                throw new RuntimeException("Не удалось подключиться к серверу", e);
            }
        }
        
        // Примечание: Scanner намеренно не закрываем, так как это закроет System.in
        System.out.println("Клиент завершил работу.");
    }
}