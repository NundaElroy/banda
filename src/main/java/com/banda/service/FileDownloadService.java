package com.banda.service;

import com.banda.httphandlers.FileMetadata;
import com.banda.response.HttpStatus;
import com.banda.response.ResponseHelper;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.Socket;

public class FileDownloadService {
    private final ResponseHelper responseHelper;

    public FileDownloadService() {
        this.responseHelper = new ResponseHelper();
    }

    public void downloadAndStreamFile(HttpExchange exchange , int port){
        try(Socket clientSocket = new Socket("localhost",port)) {
            InputStream inputStream = clientSocket.getInputStream();
            File tempFile = File.createTempFile("downloaded_file", ".tmp");

            try {
                FileMetadata metadata = downloadFile(inputStream, tempFile);
                streamFileToClient(exchange, tempFile, metadata);
            } finally {
                tempFile.delete();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private  FileMetadata downloadFile(InputStream inputStream, File tempFile) throws IOException {
        String filename = tempFile.getName();


        try (FileOutputStream fileOutputStream = new FileOutputStream(tempFile)) {
            // Read header to get filename
            filename = readFileHeader(inputStream);

            // Read file content
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }
        }
        return  new FileMetadata(filename, tempFile.length());

    }

    private String readFileHeader(InputStream inputStream) throws IOException {
        ByteArrayOutputStream headerBaos = new ByteArrayOutputStream();
        int b;
        while ((b = inputStream.read()) != -1) {
            if (b == '\n') break;
            headerBaos.write(b);
        }

        String header = headerBaos.toString().trim();
        if (header.startsWith("Filename: ")) {
            return header.substring("Filename: ".length());
        }
        return "downloaded-file";
    }


    private void streamFileToClient(HttpExchange exchange, File tempFile, FileMetadata metadata) throws IOException {
        Headers headers = exchange.getResponseHeaders();
        headers.add("Content-Disposition", "attachment; filename=\"" + metadata.fileName() + "\"");
        headers.add("Content-Type", "application/octet-stream");

        exchange.sendResponseHeaders(HttpStatus.OK.code(), metadata.fileSize());

        try (OutputStream os = exchange.getResponseBody();
             FileInputStream fis = new FileInputStream(tempFile)) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        }
    }
}
