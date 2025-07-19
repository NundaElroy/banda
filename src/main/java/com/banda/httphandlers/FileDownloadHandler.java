package com.banda.httphandlers;


import com.banda.response.HttpStatus;
import com.banda.response.ResponseHelper;
import com.banda.service.FileDownloadService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;


public class FileDownloadHandler implements HttpHandler {
    private final FileDownloadService downloadService;
    private final ResponseHelper responseHelper;

    public FileDownloadHandler(FileDownloadService downloadService) {
        this.downloadService = downloadService;
        this.responseHelper = new ResponseHelper();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        responseHelper.setCORSHeaders(exchange);

        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            responseHelper.sendErrorResponse(exchange, HttpStatus.METHOD_NOT_ALLOWED.code(), HttpStatus.METHOD_NOT_ALLOWED.reason());
            return;
        }

        try {
            String path = exchange.getRequestURI().getPath();
            String portParam = path.substring(path.lastIndexOf("/") + 1);
            int port = Integer.parseInt(portParam);

            downloadService.downloadAndStreamFile(exchange, port);
        } catch (Exception e) {
            System.err.println("Error processing download: " + e.getMessage());
            responseHelper.sendErrorResponse(exchange, HttpStatus.INTERNAL_SERVER_ERROR.code(), HttpStatus.INTERNAL_SERVER_ERROR.reason());
        }
    }
}