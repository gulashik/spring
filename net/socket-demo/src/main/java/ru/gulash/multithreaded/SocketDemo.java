package ru.gulash.multithreaded;

public class SocketDemo {

    public static void main(String[] args) {
        final int PORT = 8080;

        // Запускаем сервер в отдельном потоке
        EchoServer server = new EchoServer(PORT);

        Thread serverThread = new Thread(server::start);
        serverThread.start();

        // Даем серверу время на запуск
/*        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }*/

        // Создаем и запускаем клиент
        EchoClient client = new EchoClient("localhost", PORT);

        // В реальном приложении клиент бы запускался отдельно
        System.out.println("Для демонстрации введите несколько сообщений:");
        client.connectAndInteractWithServerAndUser();

        // Останавливаем сервер
        server.stop();
    }
}