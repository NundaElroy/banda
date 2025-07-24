package com.banda.parser;


import com.banda.exceptions.BadRequestException;
import com.banda.exceptions.ParsingException;
import com.banda.utils.FileExtensionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class IntegratedFileParser {
    private static final int CHUNK_SIZE = 8192; // 8KB chunks
    private static final int MAX_HEADER_SIZE = 16384; // 16KB limit
    private static final String FILENAME_KEY = "filename=\"";
    private static final int MAX_FILENAME_SIZE = 100 * 1024 * 1024; // 100 MB , Adjust as needed for your system
    private static final  Logger log = LoggerFactory.getLogger(IntegratedFileParser.class);


    // Parser states
    private enum ParseState {
        READING_HEADERS,
        READING_CONTENT,
        FOUND_BOUNDARY,
        DONE
    }

    public ParseResult parseMultipartFile(InputStream inputStream, String boundary,String uploadDir)
            throws IOException, ParsingException {
        log.debug("Starting multipart file parsing with boundary: ");
        Path uploadPath = Paths.get(uploadDir);
        ChunkedMultipartParser parser = new ChunkedMultipartParser(boundary);
        return parser.parse(inputStream, uploadPath);
    }

    private static class ChunkedMultipartParser {
        private final byte[] boundaryPattern;
        private final byte[] endBoundaryPattern;
        private final CircularBuffer patternBuffer;

        private ParseState state = ParseState.READING_HEADERS;
        private ByteArrayOutputStream headerBuffer = new ByteArrayOutputStream();
        private Path tempContentFile;
        private FileOutputStream contentOutput;

        private String filename;
        private static final Logger log = LoggerFactory.getLogger(ChunkedMultipartParser.class);


        public ChunkedMultipartParser(String boundary) {
            // Patterns to detect
            this.boundaryPattern = ("\r\n--" + boundary).getBytes(StandardCharsets.UTF_8);
            this.endBoundaryPattern = ("\r\n--" + boundary + "--").getBytes(StandardCharsets.UTF_8);
            // Buffer to handle patterns spanning chunks
            int maxPatternLength = Math.max(boundaryPattern.length, endBoundaryPattern.length);
            this.patternBuffer = new CircularBuffer(maxPatternLength * 2);
        }

        public ParseResult parse(InputStream inputStream,Path uploadPath) throws IOException, ParsingException {
            try {
                // Create temporary file for content
                log.debug("Creating temporary file for content in upload directory via thread {}", Thread.currentThread().getName());
                String tmpName = "upload_" + UUID.randomUUID() + ".tmp";
                tempContentFile = uploadPath.resolve(tmpName);
                Files.createFile(tempContentFile);               // create the empty file
                contentOutput = new FileOutputStream(tempContentFile.toFile());
                log.debug("Temporary file created via thread {}", Thread.currentThread().getName());

                byte[] chunk = new byte[CHUNK_SIZE];
                int bytesRead;
                log.debug("Starting to read input stream in chunks via thread {}", Thread.currentThread().getName());
                while ((bytesRead = inputStream.read(chunk)) != -1 && state != ParseState.DONE) {
                    processChunk(chunk, bytesRead);
                }

                if (state != ParseState.FOUND_BOUNDARY && state != ParseState.DONE) {
                    throw new ParsingException("Incomplete multipart data - no end boundary found");
                }
                log.debug("Finished reading input stream in chunks via thread {}", Thread.currentThread().getName());
                return buildResult();

            } catch (IOException e) {
                log.error("Error reading input stream: {}", e.getMessage());
                throw new ParsingException("Error reading input stream", e);
            }
        }

        //read a chunk then process it
        private void processChunk(byte[] chunk, int bytesRead) throws IOException, ParsingException {
            log.debug("Processing chunk via thread {}", Thread.currentThread().getName());
            for (int i = 0; i < bytesRead; i++) {
                byte currentByte = chunk[i];
                patternBuffer.add(currentByte);

                switch (state) {
                    case READING_HEADERS:
                        processHeaderByte(currentByte);
                        break;

                    case READING_CONTENT:
                        processContentByte(currentByte);
                        break;

                    case FOUND_BOUNDARY:
                    case DONE:
                        return;
                }
            }
        }

        private void processHeaderByte(byte currentByte) throws IOException, ParsingException {
            log.debug("Processing headers via thread {}", Thread.currentThread().getName());
            headerBuffer.write(currentByte);

            // Check for end of headers
            if (patternBuffer.containsPattern("\r\n\r\n".getBytes(StandardCharsets.UTF_8))) {
                // Parse headers
                String headerText = headerBuffer.toString(StandardCharsets.ISO_8859_1);
                parseHeaders(headerText);

                // Switch to content reading
                state = ParseState.READING_CONTENT;
                headerBuffer = null; // Free memory
                return;
            }

            // Security check
            if (headerBuffer.size() > MAX_HEADER_SIZE) {
                throw new BadRequestException("Headers exceed maximum size limit");
            }
        }

        private void processContentByte(byte currentByte) throws IOException {
            log.debug("Processing content via thread {}", Thread.currentThread().getName());
            // Check for boundary patterns before writing
            if (patternBuffer.containsPattern(endBoundaryPattern)) {
                state = ParseState.DONE;
                removeTrailingBoundaryFromContent();
                return;
            } else if (patternBuffer.containsPattern(boundaryPattern)) {
                state = ParseState.FOUND_BOUNDARY;
                removeTrailingBoundaryFromContent();
                return;
            }

            // Safe to write this byte to content or temporary file
            contentOutput.write(currentByte);
            // Security check for content size
            if (tempContentFile.toFile().length() > MAX_FILENAME_SIZE) {
                throw new BadRequestException("Content exceeds maximum size limit");
            }
        }

        private void parseHeaders(String headerText) throws ParsingException {
            log.debug("Parsing headers via thread {}", Thread.currentThread().getName());
            // Extract filename
            this.filename = extractFilename(headerText)
                    .orElseThrow(() -> new BadRequestException("No filename found in headers"));

        }

        private Optional<String> extractFilename(String headerText) {
            log.debug("Extracting filename from headers via thread {}", Thread.currentThread().getName());
            int filenameStart = headerText.indexOf(FILENAME_KEY);
            if (filenameStart == -1) return Optional.empty();

            filenameStart += FILENAME_KEY.length();
            int filenameEnd = headerText.indexOf('"', filenameStart);
            if (filenameEnd == -1) return Optional.empty();

            String filename = headerText.substring(filenameStart, filenameEnd);
            return filename.isEmpty() ? Optional.empty() : Optional.of(filename);
        }

        private Optional<String> extractContentType(String headerText) {
            String contentTypeKey = "Content-Type: ";
            int start = headerText.indexOf(contentTypeKey);
            if (start == -1) return Optional.empty();

            start += contentTypeKey.length();
            int end = headerText.indexOf('\r', start);
            if (end == -1) end = headerText.indexOf('\n', start);
            if (end == -1) end = headerText.length();

            return Optional.of(headerText.substring(start, end).trim());
        }

        private void removeTrailingBoundaryFromContent() throws IOException {
            // Remove the boundary bytes that were written to content
            // This requires truncating the temp file
            contentOutput.close();

            long fileSize = tempContentFile.toFile().length();
            long newSize = fileSize - boundaryPattern.length;

            if (newSize >= 0) {
                try (RandomAccessFile raf = new RandomAccessFile(tempContentFile.toFile(), "rw")) {
                    raf.setLength(newSize);
                }
            }
        }

        private ParseResult buildResult() throws IOException {
            contentOutput.close();
            log.debug("Building parse result via thread {}", Thread.currentThread().getName());
            //rename with detected extension
            log.debug("applying appropiate extension to file via thread {}", Thread.currentThread().getName());
            Path finalFile = FileExtensionHelper.renameWithDetectedExtension(tempContentFile);
            return new ParseResult(filename, finalFile);
        }


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

            for (int i = 0; i < pattern.length; i++) {
                int bufferIndex = (head + size - pattern.length + i) % buffer.length;
                if (buffer[bufferIndex] != pattern[i]) {
                    return false;
                }
            }
            return true;
        }
    }
}
