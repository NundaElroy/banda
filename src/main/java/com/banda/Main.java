package com.banda;


import com.banda.controller.BandaFileController;
import com.banda.controller.FileController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) {
             try {
                 BandaFileController fileController = new BandaFileController(8080);
                 fileController.start();
                 log.info("Banda Server started successfully on port 8080");
                 log.info("UI is available at http://localhost:8080");
                 log.info("you can now share files with your friends!");
                 Runtime.getRuntime().addShutdownHook(
                            new Thread(() -> {
                                log.info("Banda Server is stopping...");
                                fileController.stop();
                                log.info("Banda Server stopped successfully.");
                            })
                 );

                 //command to kill the server
             } catch (IOException e) {
                 log.error("Failed to start Banda Server: {}", e.getMessage());
                 e.printStackTrace();
             }

            // Keep the main thread alive to allow the server to run
            try {
                Thread.currentThread().join();  // wait forever until Ctrl‑C
            } catch (InterruptedException ignored) {
                /* main thread interrupted → fall through, JVM will exit →
                   shutdown‐hook runs */
            }
    }
}