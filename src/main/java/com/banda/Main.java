package com.banda;


import com.banda.controller.BandaFileController;
import com.banda.controller.FileController;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
             try {
                 BandaFileController fileController = new BandaFileController(8080);
                 fileController.start();
                 System.out.println("Banda Server started running  on port 8080");
                 System.out.println("UI is available at http://localhost:3030");
                 System.out.println("You can now share files with your friends!");
                 Runtime.getRuntime().addShutdownHook(
                            new Thread(() -> {
                                System.out.println("Shutting down Banda Server...");
                                fileController.stop();
                                System.out.println("Banda Server stopped.");
                            })
                 );

                 //command to kill the server
             } catch (IOException e) {
                 System.err.println("Error initializing FileController: " + e.getMessage());
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