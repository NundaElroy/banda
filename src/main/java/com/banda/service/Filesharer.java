package com.banda.service;

import com.banda.utils.UploadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Filesharer {
    private HashMap<Integer, String> availableFiles;
    private static final Logger log = LoggerFactory.getLogger(Filesharer.class);

    public Filesharer(){
        availableFiles = new HashMap<>();
    }

    public int offerFile(String filePath) {
        log.info("Offering file: {}", filePath);
        int attempts = 0;
        while(attempts < 10000) {
            int port = UploadUtils.generateCode();
            if (!availableFiles.containsKey(port)) {
                availableFiles.put(port, filePath);
                log.info("File {} offered on port: {}", port, filePath);
                return port;
            }
            attempts++;
        }
        log.error("No available ports for file: {}", filePath);
        throw new RuntimeException("No available ports found");
    }

    public void startFileServer(int port){
        log.info("Starting file server on port: {}", port);
        String filePath = availableFiles.get(port);
        if (filePath == null) {
            log.error("No file associated with port: {}", port);
            //TODO: Handle this case appropriately, maybe throw a custom exception or log an error
            throw new IllegalArgumentException("No file associated with the given port: " + port);
        }
        try(ServerSocket serverSocket = new ServerSocket(port)) {
            log.info("File server started on port: {} for file: {}", port, filePath);
            Socket clientSocket = serverSocket.accept();
            log.info("Client connected: {}", clientSocket.getInetAddress());
            new Thread(new FileSenderHandler(clientSocket, filePath)).start();

        } catch (Exception e) {
            log.error("Error starting file server on port {}: {}", port, e.getMessage());
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
                log.debug("Sending file: {} to client: {}", filePath, clientSocket.getInetAddress());
                OutputStream outputStream = clientSocket.getOutputStream();
                String fileName = new File(filePath).getName();
                String header = "Filename: " + fileName + "\n";
                log.debug("Sending header: {}", header);
                outputStream.write(header.getBytes());
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                log.info("Sending file: {} to client: {}", filePath, clientSocket.getInetAddress());
            }catch (IOException e){
                log.error("Error sending file: {} to client: {} - {}", filePath, clientSocket.getInetAddress(), e.getMessage());
            }finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    log.error("Error closing client socket: {} - {}", clientSocket.getInetAddress(), e.getMessage());
                }
            }
        }

    }
}



