package ru.gulash.manualrun.utils;

import java.io.*;

/**
 * Утилитарный класс для упрощения клиентского взаимодействия с TCP-сервером.
 * 
 * <p>Этот класс инкапсулирует логику отправки сообщений серверу и получения
 * ответов, используя протокол на основе DataInputStream/DataOutputStream.
 * Класс реализует AutoCloseable для автоматического управления ресурсами.</p>
 * 
 * <h3>Образовательный момент:</h3>
 * <p>ExampleClient демонстрирует важные принципы разработки:
 * <ul>
 *   <li><strong>Инкапсуляция:</strong> Скрывает детали сетевого протокола от клиентского кода</li>
 *   <li><strong>Adapter Pattern:</strong> Адаптирует низкоуровневые потоки к удобному API</li>
 *   <li><strong>Resource Management:</strong> Правильное управление сетевыми ресурсами</li>
 *   <li><strong>Exception Handling:</strong> Обработка специфичных сетевых исключений</li>
 *   <li><strong>DataInputStream/DataOutputStream:</strong> Надежная передача структурированных данных</li>
 * </ul></p>
 * 
 * <p><strong>Протокол взаимодействия:</strong></p>
 * <ul>
 *   <li>Отправка: сообщение кодируется в UTF-8 с prefixed length</li>
 *   <li>Получение: ответ декодируется из UTF-8</li>
 *   <li>Flush: принудительная отправка данных</li>
 *   <li>EOF handling: корректная обработка закрытия соединения сервером</li>
 * </ul>
 * 
 * <p><strong>Пример использования:</strong></p>
 * <pre>{@code
 * try (Socket socket = new Socket("localhost", 8080);
 *      ExampleClient client = new ExampleClient(
 *          socket.getInputStream(), 
 *          socket.getOutputStream())) {
 *     
 *     client.send("Hello Server");
 *     // Ответ будет выведен в консоль
 * }
 * }</pre>
 *
 * @see DataInputStream
 * @see DataOutputStream
 * @see AutoCloseable
 */
public class ExampleClient implements AutoCloseable {
    
    /**
     * Поток для чтения данных от сервера.
     * 
     * <p><strong>Образовательный момент:</strong><br>
     * DataInputStream обеспечивает надежное чтение примитивных типов данных
     * и UTF-8 строк из байтового потока. Он автоматически обрабатывает
     * информацию о длине строки, что делает протокол self-describing
     * и защищает от проблем с кодировкой.</p>
     */
    private final DataInputStream inputStream;
    
    /**
     * Поток для отправки данных серверу.
     * 
     * <p><strong>Образовательный момент:</strong><br>
     * DataOutputStream обеспечивает надежную запись примитивных типов данных
     * и UTF-8 строк в байтовый поток. При записи строк автоматически
     * добавляется информация о длине, что позволяет получателю точно
     * знать, сколько байт нужно прочитать.</p>
     */
    private final DataOutputStream outputStream;

    /**
     * Создает новый экземпляр клиента с указанными потоками ввода-вывода.
     * 
     * <p>Конструктор принимает базовые потоки и оборачивает их в
     * DataInputStream/DataOutputStream для удобной работы со структурированными данными.</p>
     * 
     * <h3>Образовательный момент:</h3>
     * <p>Использование базовых InputStream/OutputStream в параметрах обеспечивает гибкость:
     * <ul>
     *   <li><strong>Принцип подстановки Лисков:</strong> Любая реализация потоков
     *       может быть использована (файлы, сеть, память)</li>
     *   <li><strong>Decorator Pattern:</strong> DataInputStream/DataOutputStream
     *       добавляют функциональность к базовым потокам</li>
     *   <li><strong>Разделение ответственности:</strong> Клиент не знает источник
     *       потоков (сокет, файл, etc.)</li>
     *   <li><strong>Тестируемость:</strong> Можно легко создать mock-потоки для тестирования</li>
     * </ul></p>
     * 
     * @param inputStream  поток для чтения ответов от сервера, не может быть {@code null}
     * @param outputStream поток для отправки сообщений серверу, не может быть {@code null}
     * @throws IllegalArgumentException если любой из потоков равен {@code null}
     * 
     * @see DataInputStream#DataInputStream(InputStream)
     * @see DataOutputStream#DataOutputStream(OutputStream)
     */
    public ExampleClient(InputStream inputStream, OutputStream outputStream) {
        if (inputStream == null) {
            throw new IllegalArgumentException("InputStream не может быть null");
        }
        if (outputStream == null) {
            throw new IllegalArgumentException("OutputStream не может быть null");
        }
        
        this.inputStream = new DataInputStream(inputStream);
        this.outputStream = new DataOutputStream(outputStream);
    }

    /**
     * Отправляет сообщение серверу и выводит полученный ответ в консоль.
     * 
     * <p>Метод реализует простой протокол "запрос-ответ": отправляет сообщение,
     * принудительно сбрасывает буферы и ожидает ответ от сервера.</p>
     * 
     * <h3>Образовательный момент:</h3>
     * <p>Реализация метода демонстрирует несколько важных концепций:
     * <ul>
     *   <li><strong>writeUTF/readUTF:</strong> Автоматическая обработка кодировки
     *       и длины сообщения для надежной передачи</li>
     *   <li><strong>flush():</strong> Принудительная отправка буферизованных данных
     *       критически важна в сетевых протоколах</li>
     *   <li><strong>EOFException handling:</strong> Graceful обработка закрытия
     *       соединения сервером</li>
     *   <li><strong>Синхронная модель:</strong> Блокирующее ожидание ответа
     *       подходит для простых запрос-ответ протоколов</li>
     * </ul></p>
     * 
     * <p><strong>Последовательность операций:</strong></p>
     * <ol>
     *   <li>Отправка сообщения через DataOutputStream.writeUTF()</li>
     *   <li>Принудительный сброс буферов через flush()</li>
     *   <li>Ожидание ответа через DataInputStream.readUTF()</li>
     *   <li>Вывод результата в консоль</li>
     *   <li>Обработка EOFException при закрытии соединения</li>
     * </ol>
     * 
     * <p><strong>Важные детали протокола:</strong></p>
     * <ul>
     *   <li>UTF-8 кодировка обеспечивает поддержку Unicode</li>
     *   <li>Length-prefixed формат предотвращает проблемы с границами сообщений</li>
     *   <li>flush() гарантирует немедленную отправку</li>
     *   <li>EOFException указывает на нормальное закрытие соединения</li>
     * </ul>
     * 
     * @param message сообщение для отправки серверу, не может быть {@code null}
     * @throws IOException              если произошла ошибка сетевого взаимодействия
     * @throws IllegalArgumentException если message равно {@code null}
     * 
     * @see DataOutputStream#writeUTF(String)
     * @see DataInputStream#readUTF()
     * @see java.io.EOFException
     */
    public void send(String message) throws IOException {
        if (message == null) {
            throw new IllegalArgumentException("Сообщение не может быть null");
        }
        
        // Отправляем сообщение серверу
        outputStream.writeUTF(message);
        // Принудительно отправляем данные (важно для сетевых протоколов!)
        outputStream.flush();
        
        try {
            // Ожидаем ответ от сервера
            String result = inputStream.readUTF();
            System.out.println("Ответ сервера: " + result);
        } catch (EOFException e) {
            // EOFException - нормальная ситуация при закрытии соединения сервером
            System.out.println("Сервер закрыл соединение.");
        }
    }

    /**
     * Закрывает потоки ввода-вывода и освобождает связанные ресурсы.
     * 
     * <p>Метод реализует интерфейс AutoCloseable, что позволяет использовать
     * экземпляры класса в try-with-resources конструкциях для автоматического
     * управления ресурсами.</p>
     * 
     * <h3>Образовательный момент:</h3>
     * <p>Правильная реализация close() метода критически важна:
     * <ul>
     *   <li><strong>Resource cleanup:</strong> Гарантирует освобождение системных ресурсов</li>
     *   <li><strong>Exception handling:</strong> Не должен выбрасывать исключения
     *       при повторном вызове</li>
     *   <li><strong>Best effort cleanup:</strong> Попытка закрыть все ресурсы,
     *       даже если некоторые операции завершились с ошибкой</li>
     *   <li><strong>Идемпотентность:</strong> Безопасность повторного вызова</li>
     * </ul></p>
     * 
     * <p><strong>Детали реализации:</strong></p>
     * <ul>
     *   <li>Сначала закрывается входной поток</li>
     *   <li>Затем закрывается выходной поток</li>
     *   <li>Исключения подавляются для обеспечения best-effort cleanup</li>
     *   <li>Метод безопасен для повторного вызова</li>
     * </ul>
     * 
     * @throws Exception если произошла ошибка при закрытии ресурсов
     *                   (обычно IOException, но AutoCloseable допускает любые исключения)
     * 
     * @see AutoCloseable#close()
     * @see DataInputStream#close()
     * @see DataOutputStream#close()
     */
    @Override
    public void close() throws Exception {
        Exception firstException = null;
        
        // Пытаемся закрыть входной поток
        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (Exception e) {
            firstException = e;
        }
        
        // Пытаемся закрыть выходной поток
        try {
            if (outputStream != null) {
                outputStream.close();
            }
        } catch (Exception e) {
            if (firstException != null) {
                // Подавляем второе исключение, если уже есть первое
                firstException.addSuppressed(e);
            } else {
                firstException = e;
            }
        }
        
        // Перебрасываем первое исключение, если оно было
        if (firstException != null) {
            throw firstException;
        }
    }
}