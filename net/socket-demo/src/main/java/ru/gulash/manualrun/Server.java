package ru.gulash.manualrun;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Простой однопоточный TCP-сервер для демонстрации основ сетевого программирования.
 * 
 * <p>Этот класс реализует базовый сервер, который принимает текстовые сообщения
 * от клиентов и возвращает их в верхнем регистре. Сервер обрабатывает клиентов
 * последовательно (по одному за раз) и поддерживает команду "exit" для
 * корректного отключения клиентов.</p>
 * 
 * <h3>Образовательный момент:</h3>
 * <p>Данная реализация демонстрирует фундаментальные концепции серверного программирования:
 * <ul>
 *   <li><strong>ServerSocket:</strong> Класс для создания серверного сокета, который
 *       прослушивает определенный порт и принимает входящие соединения</li>
 *   <li><strong>Блокирующий I/O:</strong> Метод accept() блокирует выполнение потока
 *       до тех пор, пока не поступит новое соединение</li>
 *   <li><strong>Последовательная обработка:</strong> Сервер может обрабатывать только
 *       одного клиента за раз, что подходит для простых случаев, но ограничивает
 *       производительность</li>
 *   <li><strong>Протокол взаимодействия:</strong> Простой текстовый протокол с командой
 *       завершения для graceful shutdown</li>
 *   <li><strong>DataInputStream/DataOutputStream:</strong> Обеспечивают надежную передачу
 *       UTF-8 строк с информацией о длине</li>
 * </ul></p>
 * 
 * <p><strong>Ограничения текущей реализации:</strong></p>
 * <ul>
 *   <li>Может обслуживать только одного клиента одновременно</li>
 *   <li>Отсутствует graceful shutdown самого сервера</li>
 *   <li>Нет обработки timeout'ов соединений</li>
 *   <li>Отсутствует логирование и мониторинг</li>
 * </ul>
 * 
 * <p><strong>Пример использования:</strong></p>
 * <pre>{@code
 * // Запуск сервера (блокирующий вызов)
 * Server.main(new String[]{});
 * }</pre>
 *
 * @see ServerSocket
 * @see DataInputStream
 * @see DataOutputStream
 * @see ru.gulash.manualrun.utils.ExampleClient
 */
public class Server {
    
    /**
     * Порт, на котором сервер принимает соединения.
     * 
     * <p><strong>Образовательный момент:</strong><br>
     * Порт 8080 часто используется для разработки веб-приложений и тестирования.
     * Это альтернативный HTTP порт, который не требует административных привилегий
     * (в отличие от стандартного порта 80). Диапазон портов 1024-49151 доступен
     * для пользовательских приложений без специальных прав.</p>
     */
    private static final int SERVER_PORT = 8080;
    
    /**
     * Команда для корректного завершения соединения с клиентом.
     * 
     * <p><strong>Образовательный момент:</strong><br>
     * Использование константы для команд протокола - хорошая практика, которая:
     * <ul>
     *   <li>Предотвращает опечатки в коде</li>
     *   <li>Облегчает изменение протокола в будущем</li>
     *   <li>Улучшает читаемость и понимание кода</li>
     *   <li>Позволяет IDE находить все использования команды</li>
     * </ul></p>
     */
    private static final String EXIT_COMMAND = "exit";

    /**
     * Точка входа в серверное приложение.
     * 
     * <p>Запускает сервер на порту 8080 и обрабатывает клиентские соединения
     * в бесконечном цикле. Каждый клиент обслуживается последовательно.</p>
     * 
     * <h3>Образовательный момент:</h3>
     * <p>Метод main демонстрирует стандартную структуру серверного приложения:
     * <ul>
     *   <li><strong>Try-with-resources:</strong> Автоматическое управление ресурсами
     *       гарантирует закрытие ServerSocket даже при исключениях</li>
     *   <li><strong>Бесконечный цикл:</strong> while(true) обеспечивает непрерывную
     *       работу сервера до принудительного завершения</li>
     *   <li><strong>Централизованная обработка ошибок:</strong> IOException обрабатывается
     *       на верхнем уровне для всех сетевых операций</li>
     *   <li><strong>Информативные сообщения:</strong> Логирование состояния сервера
     *       помогает при отладке и мониторинге</li>
     * </ul></p>
     * 
     * <p><strong>Поток выполнения:</strong></p>
     * <ol>
     *   <li>Создание ServerSocket на порту 8080</li>
     *   <li>Вывод сообщения о запуске сервера</li>
     *   <li>Бесконечный цикл ожидания клиентов</li>
     *   <li>Для каждого клиента: создание потоков I/O</li>
     *   <li>Чтение сообщения от клиента</li>
     *   <li>Обработка команды exit или трансформация сообщения</li>
     *   <li>Отправка ответа клиенту</li>
     *   <li>Закрытие соединения при exit или переход к следующему клиенту</li>
     * </ol>
     * 
     * @param args аргументы командной строки (не используются)
     * 
     * @see #transformToUpperCase(String)
     * @see ServerSocket#accept()
     * @see DataInputStream#readUTF()
     * @see DataOutputStream#writeUTF(String)
     */
    public static void main(String[] args) {
        try (
            // Создаём ServerSocket
            ServerSocket socket = new ServerSocket(SERVER_PORT)
        ) {
            System.out.println("SERVER APPLICATION RUN!");
            System.out.println("Сервер запущен на порту " + SERVER_PORT);
            System.out.println("Ожидание подключений...");
            
            while (true) {
                // Принимаем новое соединение (блокирующий вызов)
                Socket client = socket.accept();
                System.out.println("Подключен клиент: " + client.getRemoteSocketAddress());
                
                // Создаем потоки для обмена данными с клиентом
                DataInputStream inputStream = new DataInputStream(client.getInputStream());
                DataOutputStream outputStream = new DataOutputStream(client.getOutputStream());
                
                // Читаем сообщение от клиента
                String userInput = inputStream.readUTF();
                System.out.println("userInput = " + userInput);
                
                // Проверяем команду завершения
                if (userInput.equalsIgnoreCase(EXIT_COMMAND)) {
                    System.out.println("Клиент с портом: " + client.getPort() + " отключился!");
                    client.close();
                    continue;
                }
                
                // Обрабатываем сообщение и отправляем ответ
                String result = transformToUpperCase(userInput);
                outputStream.writeUTF(result);
                outputStream.flush();
                System.out.println("result = " + result);
                
                // Закрываем соединение после обработки
                client.close();
                System.out.println("Соединение с клиентом закрыто");
            }
        } catch (IOException e) {
            System.err.println("Ошибка сервера: " + e.getMessage());
            System.out.println("Сервер не поднялся");
        }
    }

    /**
     * Трансформирует входящее сообщение в верхний регистр.
     * 
     * <p>Этот метод реализует основную бизнес-логику сервера - преобразование
     * текста в верхний регистр. Метод является статическим, так как не требует
     * состояния объекта и может быть легко протестирован изолированно.</p>
     * 
     * <h3>Образовательный момент:</h3>
     * <p>Выделение бизнес-логики в отдельный метод демонстрирует важные принципы:
     * <ul>
     *   <li><strong>Единственная ответственность (SRP):</strong> Метод выполняет только
     *       одну задачу - трансформацию текста</li>
     *   <li><strong>Тестируемость:</strong> Метод можно легко покрыть unit-тестами
     *       без необходимости создания сетевых соединений</li>
     *   <li><strong>Переиспользование:</strong> Логику трансформации можно использовать
     *       в других частях приложения</li>
     *   <li><strong>Читаемость:</strong> Основной метод main остается сосредоточенным
     *       на сетевом взаимодействии</li>
     * </ul></p>
     * 
     * <p><strong>Примечание по локализации:</strong><br>
     * Метод toUpperCase() без параметров использует локаль по умолчанию.
     * Для международных приложений рекомендуется использовать
     * {@code userInput.toUpperCase(Locale.ENGLISH)} для предсказуемого поведения.</p>
     * 
     * @param userInput входящее сообщение от клиента, не должно быть {@code null}
     * @return сообщение, преобразованное в верхний регистр
     * @throws NullPointerException если userInput равен {@code null}
     * 
     * @see String#toUpperCase()
     * @see java.util.Locale
     */
    private static String transformToUpperCase(String userInput) {
        System.out.println("Выполняем трансформацию текста в верхний регистр");
        
        if (userInput == null) {
            throw new NullPointerException("Входящее сообщение не может быть null");
        }
        
        return userInput.toUpperCase();
    }
}