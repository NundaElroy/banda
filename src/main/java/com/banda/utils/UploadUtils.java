package com.banda.utils;

import java.util.Random;

public class UploadUtils {

    public static int generateCode(){
        int DYNAMIC_STARTING_PORT = 49152;
        int DYNAMIC_ENDING_PORT   = 85535;

        Random random = new Random();

        // Generate a random port number between DYNAMIC_STARTING_PORT and DYNAMIC_ENDING_PORT
        return random.nextInt(DYNAMIC_ENDING_PORT - DYNAMIC_STARTING_PORT) + DYNAMIC_STARTING_PORT;
    }
}
