package com.banda.httphandlers;



import com.banda.annotations.ResponseStatus;
import com.banda.parser.IntegratedFileParser;
import com.banda.response.HttpStatus;
import com.banda.service.Filesharer;
//import com.banda.parser.MultipartParser;
import com.banda.parser.ParseResult;
import com.banda.response.ResponseHelper;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.UUID;

public class FileUploadHandler implements HttpHandler {
    private final Filesharer filesharer;
    private final String uploadDir;
    private final ResponseHelper responseHelper;
    private static Logger log = LoggerFactory.getLogger(FileUploadHandler.class);
    private final IntegratedFileParser fileParser;
    public FileUploadHandler(Filesharer filesharer, String uploadDir, IntegratedFileParser fileParser) {
        this.filesharer = filesharer;
        this.uploadDir = uploadDir;
        this.fileParser = fileParser;
        this.responseHelper = new ResponseHelper();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        log.debug("Handling upload request: {}", exchange.getRequestURI().getPath());
        responseHelper.setCORSHeaders(exchange);

        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            log.warn("Unsupported HTTP method: {}", exchange.getRequestMethod());
            responseHelper.sendErrorResponse(exchange, HttpStatus.METHOD_NOT_ALLOWED.code(), HttpStatus.METHOD_NOT_ALLOWED.reason());
            return;
        }
        log.debug("Request method is POST, proceeding with upload handling");
        if (!isValidContentType(exchange)) {
            log.warn("Invalid Content-Type header: {}", exchange.getRequestHeaders().getFirst("Content-Type"));
            responseHelper.sendErrorResponse(exchange, HttpStatus.BAD_REQUEST.code(), "INVALID CONTENT TYPE");
            return;
        }
        log.debug("Content-Type is valid, proceeding with file upload processing");

        try {
            log.info("Processing file upload request");
            processUpload(exchange);
        } catch (Exception e) {
            log.error("Error processing upload request: {}", e.getMessage(), e);
            // 1) Look for @ResponseStatus on the exception class
            ResponseStatus rs = e.getClass().getAnnotation(ResponseStatus.class);



            if (rs != null) {
                int status = rs.code();
                String message =  e.getMessage() != null ? e.getMessage() : rs.reason();
                log.error("Sending error response with status {}: {}", status, message);
                responseHelper.sendErrorResponse(exchange,status, message);
            }else {
                log.error("Sending internal server error response", e);
                responseHelper.sendErrorResponse(exchange, HttpStatus.INTERNAL_SERVER_ERROR.code(), HttpStatus.INTERNAL_SERVER_ERROR.reason());
            }
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
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        IOUtils.copy(exchange.getRequestBody(), baos);
//
//        //use the simple factory to get parser
//        MultipartParser parser = new MultipartParser(baos.toByteArray(), boundary);
//        ParseResult result = parser.parse();


        ParseResult result = fileParser.parseMultipartFile(exchange.getRequestBody(), boundary, uploadDir);


        if (result == null) {
            log.error("Failed to parse multipart data");
            responseHelper.sendErrorResponse(exchange, 400, "Bad Request Cannot parse multipart data");
            return;
        }

        String savedFilePath = result.filePath().toString();
        int port = filesharer.offerFile(savedFilePath);



        // Start file server in background
        log.info("Starting file server on port: {}", port);
        new Thread(() -> filesharer.startFileServer(port)).start();

        String response = "{\"port\":" + port + "}";
        log.debug("Sending response: {}", response);
        responseHelper.sendJsonResponse(exchange, HttpStatus.OK.code(), response);
    }

    private String extractBoundary(String contentType) {
        return contentType.substring(contentType.indexOf("boundary=") + 9);
    }


}
