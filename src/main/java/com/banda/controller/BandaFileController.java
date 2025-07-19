package com.banda.controller;

import com.banda.httphandlers.CORSHandler;
import com.banda.httphandlers.FileDownloadHandler;
import com.banda.service.FileDownloadService;
import com.banda.httphandlers.FileUploadHandler;
import com.banda.service.Filesharer;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BandaFileController {
    private final Filesharer filesharer;
    private final HttpServer httpServer;
    private final String uploadDir;
    private final ExecutorService executorService;

    public BandaFileController(int port) throws IOException {
        this.filesharer = new Filesharer();
        this.httpServer = HttpServer.create(new InetSocketAddress(port), 0);
        this.uploadDir = System.getProperty("java.io.tmpdir") + File.separator + "banda-uploads";
        this.executorService = Executors.newFixedThreadPool(10);
        setupUploadDirectory();
        setupRoutes();
    }

    private void setupUploadDirectory() {
        File uploadDir = new File(this.uploadDir);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
    }

    private void setupRoutes() {
        FileDownloadService downloadService = new FileDownloadService();

        httpServer.createContext("/", new CORSHandler());
        httpServer.createContext("/download", new FileDownloadHandler(downloadService));
        httpServer.createContext("/upload", new FileUploadHandler(filesharer, uploadDir));
        httpServer.setExecutor(executorService);
    }

    public void start() {
        this.httpServer.start();
        System.out.println("Server started running on port: " + httpServer.getAddress().getPort());
    }

    public void stop() {
        this.httpServer.stop(0);
        this.executorService.shutdown();
        System.out.println("Server stopping .........");
    }
}
