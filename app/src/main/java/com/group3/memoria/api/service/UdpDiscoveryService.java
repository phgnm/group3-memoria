package com.group3.memoria.api.service;

import jakarta.annotation.PostConstruct;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;

import java.net.*;
import java.util.Enumeration;

@Service
public class UdpDiscoveryService {

    private static final int DISCOVERY_PORT = 8888;
    private static final String DISCOVERY_MESSAGE = "DISCOVER_SERVER";
    private static final int HTTP_SERVER_PORT = 8080;

    @Async
    @PostConstruct
    public void startUdpServer() {
        new Thread(() -> {
            try (DatagramSocket socket = new DatagramSocket(DISCOVERY_PORT)) {
                socket.setBroadcast(true);
                System.out.println("UDP discovery service running on port " + DISCOVERY_PORT);

                while (true) {
                    byte[] buffer = new byte[256];
                    DatagramPacket requestPacket = new DatagramPacket(buffer, buffer.length);
                    socket.receive(requestPacket);

                    String received = new String(requestPacket.getData(), 0, requestPacket.getLength()).trim();
                    System.out.println("Received UDP: " + received);

                    if (DISCOVERY_MESSAGE.equals(received)) {
                        // Get the correct reachable IP address
                        String serverIp = getReachableLocalIp(requestPacket.getAddress());
                        
                        // Create response
                        JSONObject responseJson = new JSONObject();
                        responseJson.put("server_ip", serverIp);
                        responseJson.put("server_port", HTTP_SERVER_PORT);

                        // Send response
                        byte[] responseData = responseJson.toString().getBytes();
                        DatagramPacket responsePacket = new DatagramPacket(
                            responseData,
                            responseData.length,
                            requestPacket.getAddress(),
                            requestPacket.getPort()
                        );
                        socket.send(responsePacket);
                        System.out.println("Responded to " + requestPacket.getAddress() + 
                                         " with IP: " + serverIp);
                    }
                }
            } catch (Exception e) {
                System.err.println("UDP server error: ");
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Gets the most appropriate local IP address that the requesting client can reach
     */
    private String getReachableLocalIp(InetAddress clientAddress) throws SocketException {
        // If client is on same machine (for testing)
        if (clientAddress.isLoopbackAddress()) {
            return "127.0.0.1";
        }

        // Check network interfaces for matching subnet
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            
            // Skip loopback and inactive interfaces
            if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                continue;
            }

            // Check all interface addresses
            for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                InetAddress address = interfaceAddress.getAddress();
                if (address instanceof Inet4Address) { // Prefer IPv4
                    // If client is in same subnet
                    if (isInSameSubnet(clientAddress, address, interfaceAddress.getNetworkPrefixLength())) {
                        return address.getHostAddress();
                    }
                }
            }
        }

        // Fallback to first non-loopback IPv4 address
        return getFirstNonLoopbackIpv4Address();
    }

    private boolean isInSameSubnet(InetAddress client, InetAddress server, short prefixLength) {
        byte[] clientIp = client.getAddress();
        byte[] serverIp = server.getAddress();
        
        for (int i = 0; i < prefixLength / 8; i++) {
            if (clientIp[i] != serverIp[i]) {
                return false;
            }
        }
        
        return true;
    }

    private String getFirstNonLoopbackIpv4Address() throws SocketException {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                continue;
            }

            for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                InetAddress address = interfaceAddress.getAddress();
                if (address instanceof Inet4Address) {
                    return address.getHostAddress();
                }
            }
        }
        return "127.0.0.1"; // Fallback
    }
}