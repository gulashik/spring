package ru.gulash.manualrun;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) {
        try (ServerSocket socket = new ServerSocket(8080)) {
            System.out.println("SERVER APPLICATION RUN!");
            while (true) {
                Socket client = socket.accept();
                DataInputStream inputStream = new DataInputStream(client.getInputStream());
                DataOutputStream outputStream = new DataOutputStream(client.getOutputStream());
                String userInput = inputStream.readUTF();
                System.out.println("userInput = " + userInput);
                if(userInput.equalsIgnoreCase("exit")){
                    System.out.println("клиент с портом : "+client.getPort() +" отключился!");
                    client.close();
                    continue;
                }
                String result = transformToUpperCase(userInput);
                outputStream.writeUTF(result);
                outputStream.flush();
                System.out.println("result = " + result);
            }
        } catch (IOException e) {
            System.out.println("Сервер не поднялся");
        }
    }

    private static String transformToUpperCase(String userInput) {
        System.out.println("выполняем трансформацию!");
        return userInput.toUpperCase();
    }

}
