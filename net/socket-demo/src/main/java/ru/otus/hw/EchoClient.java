package ru.otus.hw;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Клиент для подключения к серверу
 *
 * Демонстрирует:
 * - Установление соединения с сервером
 * - Отправку и получение данных
 * - Корректное закрытие соединения
 */
public class EchoClient {
    private final String hostname;
    private final int port;

    /**
     * Конструктор клиента
     * @param hostname адрес сервера (IP или доменное имя)
     * @param port порт сервера
     */
    public EchoClient(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    /**
     * Подключение к серверу и отправка сообщений
     *
     * Образовательный момент:
     * Клиент инициирует соединение, после чего может читать/писать данные
     */
    public void connect() {
        try (
            // Создаем сокет и подключаемся к серверу
            Socket socket = new Socket(hostname, port);

            // Создаем потоки для общения с сервером
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));

            // Для чтения ввода пользователя
            BufferedReader stdIn = new BufferedReader(
                new InputStreamReader(System.in))
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
}

