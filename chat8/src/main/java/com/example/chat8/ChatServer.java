package com.example.chat8;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 8888;
    private static HashSet<PrintWriter> writers = new HashSet<>();

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("服务端口 " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("新用户已连接");

                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
                writers.add(writer);

                ClientHandler clientHandler = new ClientHandler(clientSocket, writer);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private PrintWriter writer;
        private String clientName;

        public ClientHandler(Socket clientSocket, PrintWriter writer) {
            this.clientSocket = clientSocket;
            this.writer = writer;
        }

        public void run() {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                // Prompt the client to enter a name
                writer.println("请输入你的姓名");
                clientName = reader.readLine();
                broadcast(clientName + " 已经加入聊天");

                String message;
                while ((message = reader.readLine()) != null) {
                    System.out.println("收到消息 " + clientName + ": " + message);
                    broadcast(clientName + ": " + message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                writers.remove(writer);
                broadcast(clientName + "已经离开聊天");
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void broadcast(String message) {
            for (PrintWriter writer : writers) {
                writer.println(message);
                writer.flush();
            }
        }
    }
}


