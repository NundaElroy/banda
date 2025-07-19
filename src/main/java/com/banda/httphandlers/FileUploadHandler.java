package com.banda.httphandlers;



import com.banda.response.HttpStatus;
import com.banda.service.Filesharer;
import com.banda.parser.MultipartParser;
import com.banda.parser.ParseResult;
import com.banda.response.ResponseHelper;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.UUID;

public class FileUploadHandler implements HttpHandler {
    private final Filesharer filesharer;
    private final String uploadDir;
    private final ResponseHelper responseHelper;

    public FileUploadHandler(Filesharer filesharer, String uploadDir) {
        this.filesharer = filesharer;
        this.uploadDir = uploadDir;
        this.responseHelper = new ResponseHelper();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        responseHelper.setCORSHeaders(exchange);

        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            responseHelper.sendErrorResponse(exchange, HttpStatus.METHOD_NOT_ALLOWED.code(), HttpStatus.METHOD_NOT_ALLOWED.reason());
            return;
        }

        if (!isValidContentType(exchange)) {
            responseHelper.sendErrorResponse(exchange, HttpStatus.BAD_REQUEST.code(), "INVALID CONTENT TYPE");
            return;
        }

        try {
            processUpload(exchange);
        } catch (Exception e) {
            System.err.println("Error processing upload: " + e.getMessage());
            responseHelper.sendErrorResponse(exchange, HttpStatus.INTERNAL_SERVER_ERROR.code(), HttpStatus.INTERNAL_SERVER_ERROR.reason());
        }
    }

    private boolean isValidContentType(HttpExchange exchange) {
        Headers requestHeaders = exchange.getRequestHeaders();
        String contentType = requestHeaders.getFirst("Content-Type");
        return requestHeaders.containsKey("Content-Type") &&
                contentType.startsWith("multipart/form-data");
    }

    private void processUpload(HttpExchange exchange) throws IOException {
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        String boundary = extractBoundary(contentType);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IOUtils.copy(exchange.getRequestBody(), baos);

        MultipartParser parser = new MultipartParser(baos.toByteArray(), boundary);
        ParseResult result = parser.parse();

        if (result == null) {
            responseHelper.sendErrorResponse(exchange, 400, "Bad Request Cannot parse multipart data");
            return;
        }

        String savedFilePath = saveFile(result);
        int port = filesharer.offerFile(savedFilePath);

        // Start file server in background
        new Thread(() -> filesharer.startFileServer(port)).start();

        String response = "{\"port\":" + port + "}";
        responseHelper.sendJsonResponse(exchange, HttpStatus.OK.code(), response);
    }

    private String extractBoundary(String contentType) {
        return contentType.substring(contentType.indexOf("boundary=") + 9);
    }

    private String saveFile(ParseResult result) throws IOException {
        String fileName = result.fileName().isEmpty() ? "uploaded_unknown_file" : result.fileName();
        String uniqueFilename = UUID.randomUUID().toString() + "_" + new File(fileName).getName();
        String filePath = uploadDir + File.separator + uniqueFilename;

        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(result.fileContent());
        }

        return filePath;
    }
}
