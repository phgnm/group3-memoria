package com.group3.memoria.api.service;

import jakarta.annotation.PostConstruct;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

// @Service
public class UdpDiscoveryService {

    private static final int DISCOVERY_PORT = 8888;
    private static final String DISCOVERY_MESSAGE = "DISCOVER_SERVER";
    private static final int HTTP_SERVER_PORT = 8080; // Port of your HTTP server

    // @Async
    // @PostConstruct
    public void startUdpServer() {
        new Thread(() -> {
            try (DatagramSocket socket = new DatagramSocket(DISCOVERY_PORT)) {
                socket.setBroadcast(true);
                System.out.println("UDP discovery service running on port " + DISCOVERY_PORT);

                while (true) {
                    byte[] buffer = new byte[256];
                    DatagramPacket requestPacket = new DatagramPacket(buffer, buffer.length);
                    socket.receive(requestPacket);

                    String received = new String(requestPacket.getData(), 0, requestPacket.getLength());
                    System.out.println("Received UDP: " + received);

                    if (DISCOVERY_MESSAGE.equals(received)) {
                        // Get the server's IP address
                        String ip = InetAddress.getLocalHost().getHostAddress();

                        // Create a JSON response with IP and port
                        JSONObject responseJson = new JSONObject();
                        responseJson.put("server_ip", ip);
                        responseJson.put("server_port", HTTP_SERVER_PORT);

                        // Send the JSON response back to the client
                        DatagramPacket responsePacket = new DatagramPacket(
                                responseJson.toString().getBytes(),
                                responseJson.toString().length(),
                                requestPacket.getAddress(),
                                requestPacket.getPort()
                        );

                        socket.send(responsePacket);
                        System.out.println("Responded with JSON: " + responseJson.toString());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
