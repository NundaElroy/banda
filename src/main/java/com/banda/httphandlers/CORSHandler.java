package com.banda.httphandlers;

import com.banda.response.HttpStatus;
import com.banda.response.ResponseHelper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public class CORSHandler implements HttpHandler {
    private final ResponseHelper responseHelper;

    public CORSHandler() {
        this.responseHelper = new ResponseHelper();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        responseHelper.setCORSHeaders(exchange);

        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            exchange.sendResponseHeaders(HttpStatus.NO_CONTENT.code(), -1);
            return;
        }

        responseHelper.sendErrorResponse(exchange, HttpStatus.NOT_FOUND.code(), HttpStatus.NOT_FOUND.reason());
    }
}
