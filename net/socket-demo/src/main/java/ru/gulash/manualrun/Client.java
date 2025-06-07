package ru.gulash.manualrun;

import ru.gulash.manualrun.utils.ExampleClient;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            try (Socket socket = new Socket("localhost", 8080)) {
                ExampleClient client = new ExampleClient(
                    socket.getInputStream(),
                    socket.getOutputStream());
                System.out.println("Введи сообщение: ");
                String userMessage = scanner.nextLine();
                if(userMessage.equalsIgnoreCase("exit")){
                    client.send(userMessage);
                    break;
                }
                client.send(userMessage);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
