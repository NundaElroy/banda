package com.banda.service;

import com.banda.utils.UploadUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Filesharer {
    private HashMap<Integer, String> availableFiles;

    public Filesharer(){
        availableFiles = new HashMap<>();
    }

    public int offerFile(String filePath) {
        int attempts = 0;
        while(attempts < 10000) {
            int port = UploadUtils.generateCode();
            if (!availableFiles.containsKey(port)) {
                availableFiles.put(port, filePath);
                return port;
            }
            attempts++;
        }
        throw new RuntimeException("No available ports found");
    }

    public void startFileServer(int port){
        String filePath = availableFiles.get(port);
        if (filePath == null) {
            System.out.println("No file associated with port: " + port);
            //TODO: Handle this case appropriately, maybe throw a custom exception or log an error
            throw new IllegalArgumentException("No file associated with the given port: " + port);
        }
        try(ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("File server started on port: " + port + " for file: " + filePath);
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected: " + clientSocket.getInetAddress());
            new Thread(new FileSenderHandler(clientSocket, filePath)).start();

        } catch (Exception e) {
            System.err.println("Error starting file server on port " + port + ": " + e.getMessage());
        }
    }

    public static class  FileSenderHandler implements Runnable {
        private final Socket clientSocket;
        private final String filePath;

        public FileSenderHandler(Socket clientSocket, String filePath) {
            this.clientSocket = clientSocket;
            this.filePath = filePath;
        }

        @Override
        public void run() {
            try(FileInputStream fis = new FileInputStream(filePath)) {
                OutputStream outputStream = clientSocket.getOutputStream();
                String fileName = new File(filePath).getName();
                String header = "Filename: " + fileName + "\n";
                outputStream.write(header.getBytes());
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                System.out.println("Sending file: " + filePath + " to client: " + clientSocket.getInetAddress());
            }catch (IOException e){
                System.err.println("Error sending file: " + filePath + " to client: " + clientSocket.getInetAddress() + " - " + e.getMessage());
            }finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.err.println("Error closing client socket: " + e.getMessage());
                }
            }
        }

    }
}



