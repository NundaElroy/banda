package com.banda.httphandlers;

import com.banda.response.HttpStatus;
import com.banda.response.ResponseHelper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class CORSHandler implements HttpHandler {
    private final ResponseHelper responseHelper;
    private static final Logger log = LoggerFactory.getLogger(CORSHandler.class);

    public CORSHandler() {
        this.responseHelper = new ResponseHelper();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        responseHelper.setCORSHeaders(exchange);
        log.debug("Handling CORS request for method: {}", exchange.getRequestMethod());

        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            log.debug("CORS preflight request received, responding with NO_CONTENT");
            exchange.sendResponseHeaders(HttpStatus.NO_CONTENT.code(), -1);
            return;
        }
        log.warn("CORSHandler received an unsupported request method: {}", exchange.getRequestMethod());
        responseHelper.sendErrorResponse(exchange, HttpStatus.NOT_FOUND.code(), HttpStatus.NOT_FOUND.reason());
    }
}
