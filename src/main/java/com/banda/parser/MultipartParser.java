package com.banda.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultipartParser {
    private final byte[] data;
    private final String boundary;
    private static final Logger log = LoggerFactory.getLogger(MultipartParser.class);

    public MultipartParser(byte[] data, String boundary) {
        this.data = data;
        this.boundary = boundary;
    }

    public ParseResult parse() {
        try {
            String dataAsString = new String(data);
         ;
            String filename = extractFilename(dataAsString);
            if (filename == null) return null;
            log.debug("Extracted filename: {}", filename);
            String contentType = extractContentType(dataAsString);
            byte[] fileContent = extractFileContent(dataAsString);

            if (fileContent == null) return null;

            return new ParseResult(filename, fileContent, contentType);
        } catch (Exception e) {
            log.error("Error parsing multipart data", e);
            return null;
        }
    }

    private String extractFilename(String dataAsString) {
        String filenameMarker = "filename=\"";
        int filenameStart = dataAsString.indexOf(filenameMarker);
        if (filenameStart == -1) return null;

        int filenameEnd = dataAsString.indexOf("\"", filenameStart + filenameMarker.length());
        return dataAsString.substring(filenameStart + filenameMarker.length(), filenameEnd);
    }

    private String extractContentType(String dataAsString) {
        String contentTypeMarker = "Content-Type: ";
        int contentTypeStart = dataAsString.indexOf(contentTypeMarker);

        if (contentTypeStart == -1) return "application/octet-stream";

        contentTypeStart += contentTypeMarker.length();
        int contentTypeEnd = dataAsString.indexOf("\r\n", contentTypeStart);
        return dataAsString.substring(contentTypeStart, contentTypeEnd);
    }

    private byte[] extractFileContent(String dataAsString) {
        String headerEndMarker = "\r\n\r\n";
        int headerEnd = dataAsString.indexOf(headerEndMarker);
        if (headerEnd == -1) return null;

        int contentStart = headerEnd + headerEndMarker.length();
        int contentEnd = findContentEnd(contentStart);

        if (contentEnd == -1 || contentEnd <= contentStart) return null;

        byte[] fileContent = new byte[contentEnd - contentStart];
        System.arraycopy(data, contentStart, fileContent, 0, fileContent.length);
        return fileContent;
    }

    private int findContentEnd(int contentStart) {
        byte[] boundaryBytes = ("\r\n--" + boundary + "--").getBytes();
        int contentEnd = findSequence(data, boundaryBytes, contentStart);

        if (contentEnd == -1) {
            boundaryBytes = ("\r\n--" + boundary).getBytes();
            contentEnd = findSequence(data, boundaryBytes, contentStart);
        }

        return contentEnd;
    }

    private static int findSequence(byte[] data, byte[] sequence, int startPos) {
        outer:
        for (int i = startPos; i <= data.length - sequence.length; i++) {
            for (int j = 0; j < sequence.length; j++) {
                if (data[i + j] != sequence[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }
}