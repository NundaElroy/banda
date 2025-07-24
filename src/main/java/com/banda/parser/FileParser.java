package com.banda.parser;


import com.banda.exceptions.ParsingException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class FileParser {
    private static final int CHUNK_SIZE = 8192;// 8KB chunks
    private static final String FILENAME_KEY = "filename=\"";
    private static final byte[] HEADER_END_MARKER = "\r\n\r\n".getBytes(StandardCharsets.ISO_8859_1);



    /**
     * Extracts the filename from the headers of a multipart file.
     *
     * @param inputStream The InputStream containing the multipart data.
     * @return An Optional containing the filename if found, or empty if not.
     * @throws IOException If an I/O error occurs while reading the stream.
     * @throws ParsingException If the headers are malformed or no filename is found.
     */
    public Optional<String> extractFilename(InputStream inputStream) throws IOException {
        String headerText = extractHeaders(inputStream)
                .orElseThrow(() -> new ParsingException("No headers found"));
        int filenameStart = headerText.indexOf(FILENAME_KEY);
        if (filenameStart == -1) {
            return Optional.empty(); // No filename found
        }
        filenameStart += FILENAME_KEY.length();
        int filenameEnd = headerText.indexOf('"', filenameStart);
        if (filenameEnd == -1) {
            throw new ParsingException("Malformed headers: No closing quote for filename");
        }

        String filename = headerText.substring(filenameStart, filenameEnd);

        if (filename.isEmpty()) {
            throw new ParsingException("Malformed headers: Empty filename");
        }

        return  Optional.of(filename);

    }

    /**
     * Extracts headers from the InputStream until the end of headers is reached.
     *
     * @param inputStream The InputStream containing the multipart data.
     * @return An Optional containing the headers as a String if found, or empty if not.
     * @throws IOException If an I/O error occurs while reading the stream.
     */
    public Optional<String> extractHeaders(InputStream inputStream) throws IOException {
        ByteArrayOutputStream headerBuffer = new ByteArrayOutputStream();
        byte[] chunk = new byte[CHUNK_SIZE];
        int bytesRead;
        //read size should not exceed 16KB
        while(true){
            bytesRead = inputStream.read(chunk);
            if (bytesRead == -1) break; // End of stream
            headerBuffer.write(chunk, 0, bytesRead);

            String headerText = headerBuffer.toString(StandardCharsets.ISO_8859_1);
            int headerEnd = headerText.indexOf("\r\n\r\n");
            if (headerEnd != -1) {
                return Optional.of(headerText.substring(0, headerEnd));
            }

            // Prevent headers from growing too large (security)
            if (headerBuffer.size() > 16384) { // 16KB limit
                throw new IOException("Headers too large");
            }
        }
        return Optional.empty();
    }




    private static class CircularBuffer {
        private final byte[] buffer;
        private int head = 0;
        private int size = 0;

        public CircularBuffer(int capacity) {
            this.buffer = new byte[capacity];
        }

        public void add(byte b) {
            buffer[(head + size) % buffer.length] = b;
            if (size < buffer.length) {
                size++;
            } else {
                head = (head + 1) % buffer.length;
            }
        }

        public boolean containsPattern(byte[] pattern) {
            if (size < pattern.length) return false;

            // Check if the last 'pattern.length' bytes match
            for (int i = 0; i < pattern.length; i++) {
                int bufferIndex = (head + size - pattern.length + i) % buffer.length;
                if (buffer[bufferIndex] != pattern[i]) {
                    return false;
                }
            }
            return true;
        }

        public byte removeOldest() {
            if (size == 0) throw new IllegalStateException("Buffer empty");
            byte result = buffer[head];
            head = (head + 1) % buffer.length;
            size--;
            return result;
        }
    }


}
