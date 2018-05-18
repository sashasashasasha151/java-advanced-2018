package ru.ifmo.rain.pakulev.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HelloUDPClient implements HelloClient {

    public void run(String name, int port, String message, int threadsNumber, int mNumber) {

        ExecutorService workers = Executors.newFixedThreadPool(threadsNumber);

        InetAddress IP = null;
        try {
            IP = InetAddress.getByName(name);
        } catch (UnknownHostException e) {
            System.err.println(e.toString());
        }

        for (int i = 0; i < threadsNumber; ++i) {
            InetAddress finalIP = IP;
            int finalI = i;

            workers.submit(() -> {
                try {
                    DatagramSocket socket = new DatagramSocket();
                    socket.setSoTimeout(100);

                    for (int j = 0; j < mNumber; ++j) {
                        String sendMessage = message + finalI + "_" + j;
                        byte[] sendBuffer = sendMessage.getBytes("UTF-8");
                        DatagramPacket sendPacket = new DatagramPacket(sendBuffer, 0, sendBuffer.length, finalIP, port);

                        byte[] getBuffer = new byte[socket.getReceiveBufferSize()];
                        DatagramPacket getPacket = new DatagramPacket(getBuffer, getBuffer.length);

                        while (true) {
                            try {
                                socket.send(sendPacket);

                                socket.receive(getPacket);
                                String getMessage = new String(getPacket.getData(), getPacket.getOffset(), getPacket.getLength(), "UTF-8");

                                System.out.println("Sent message: " + sendMessage);
                                System.out.println("Get message: " + getMessage);

                                if (!getMessage.contains(sendMessage)/*!("Hello, " + sendMessage).equals(getMessage)*/) {
                                    System.out.println("Wrong message from server\n");
                                    continue;
                                }

                                System.out.println();
                                break;
                            } catch (IOException e) {
                                System.err.println(e.toString());
                            }
                        }
                    }
                    socket.close();
                } catch (SocketException | UnsupportedEncodingException e) {
                    System.err.println(e.toString());
                }
            });
        }

        workers.shutdown();
        try {
            workers.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            System.err.println(e.toString());
        }
    }

    public static void main(String[] args) {
        if (args == null) {
            System.err.println("No arguments");
            return;
        }
        if (args.length != 5) {
            System.err.println("Five arguments excepted");
            return;
        }
        for (String s:args) {
            if(s== null) {
                System.err.println("Null argument");
                return;
            }
        }

        HelloUDPClient client = new HelloUDPClient();
        try {
            String adress = args[0];
            int port = Integer.parseInt(args[1]);
            String message = args[2];
            int threadsNumber = Integer.parseInt(args[3]);
            int mNumber = Integer.parseInt(args[4]);
            client.run(adress, port, message, threadsNumber, mNumber);
        } catch (NumberFormatException e) {
            System.err.println(e.toString());
        }
    }
}