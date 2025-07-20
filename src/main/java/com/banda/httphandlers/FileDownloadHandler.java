package com.banda.httphandlers;


import com.banda.response.HttpStatus;
import com.banda.response.ResponseHelper;
import com.banda.service.FileDownloadService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


public class FileDownloadHandler implements HttpHandler {
    private final FileDownloadService downloadService;
    private final ResponseHelper responseHelper;
    private final static Logger log = LoggerFactory.getLogger(FileDownloadHandler.class);

    public FileDownloadHandler(FileDownloadService downloadService) {
        this.downloadService = downloadService;
        this.responseHelper = new ResponseHelper();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        responseHelper.setCORSHeaders(exchange);
        log.debug("Handling download request: {}", exchange.getRequestURI().getPath());
        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            log.warn("Unsupported HTTP method: {}", exchange.getRequestMethod());
            responseHelper.sendErrorResponse(exchange, HttpStatus.METHOD_NOT_ALLOWED.code(), HttpStatus.METHOD_NOT_ALLOWED.reason());
            return;
        }

        try {

            String path = exchange.getRequestURI().getPath();
            String portParam = path.substring(path.lastIndexOf("/") + 1);
            int port = Integer.parseInt(portParam);
            log.info("Received download request for port: {}", port);

            downloadService.downloadAndStreamFile(exchange, port);
        } catch (Exception e) {
            log.error("Error processing download request: {}", e.getMessage(), e);
            responseHelper.sendErrorResponse(exchange, HttpStatus.INTERNAL_SERVER_ERROR.code(), HttpStatus.INTERNAL_SERVER_ERROR.reason());
        }
    }
}