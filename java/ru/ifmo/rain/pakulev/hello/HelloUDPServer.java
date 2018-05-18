package ru.ifmo.rain.pakulev.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;
import sun.nio.cs.UTF_32;

import java.io.IOException;
import java.net.*;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class HelloUDPServer implements HelloServer {
    private DatagramSocket socket;
    private ExecutorService workers;
    private AtomicBoolean end = new AtomicBoolean(false);

    public void start(int port, int threadsNumber) {

        workers = Executors.newFixedThreadPool(threadsNumber);

        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            System.err.println(e.toString());
        }

        for (int i = 0; i < threadsNumber; ++i) {
            workers.submit(() -> {
                while (!end.get()) {
                    try {
                        byte[] getBuffer = new byte[socket.getReceiveBufferSize()];
                        DatagramPacket getPacket = new DatagramPacket(getBuffer, getBuffer.length);
                        socket.receive(getPacket);

                        String sendMessage = "Hello, " + new String(getPacket.getData(), 0, getPacket.getLength(), "UTF-8");

                        byte[] sendBuffer = sendMessage.getBytes("UTF-8");
                        DatagramPacket packetToSend = new DatagramPacket(sendBuffer, 0, sendBuffer.length, getPacket.getSocketAddress());
                        socket.send(packetToSend);
                    } catch (IOException e) {
                        if (!end.get()) {
                            System.err.println(e.toString());
                        }
                    }
                }
            });
        }
    }

    public void close() {
        end.set(true);
        workers.shutdown();
        socket.close();
    }

    public static void main(String[] args) {
        if (args == null) {
            System.err.println("No arguments");
            return;
        }
        if (args.length != 2) {
            System.err.println("Two arguments excepted");
            return;
        }
        for (String s : args) {
            if (s == null) {
                System.err.println("Null argument");
                return;
            }
        }

        HelloUDPServer server = new HelloUDPServer();
        try {
            int port = Integer.parseInt(args[0]);
            int threadsNumber = Integer.parseInt(args[1]);
            server.start(port, threadsNumber);
        } catch (NumberFormatException e) {
            System.err.println(e.toString());
        }
    }
}
