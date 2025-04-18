package com.group3.memoria.api.service;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

// @Service
public class TcpServerService {

    private static final int SERVER_PORT = 8080; // Example for other services

    // @Async
    // @PostConstruct
    public static void startTcpServer() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
                System.out.println("TCP Server started on port " + SERVER_PORT);

                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    handleClientRequest(clientSocket);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static void handleClientRequest(Socket clientSocket) {
        new Thread(() -> {
            try {
                // For simplicity, echo the client input (you can replace this with actual business logic)
                BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true);

                String clientMessage = input.readLine();
                System.out.println("Received from client: " + clientMessage);
                output.println("Server response: " + clientMessage);

                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}