package com.banda.utils;

import java.io.IOException;
import java.nio.file.*;
import org.apache.tika.Tika;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypes;
import org.apache.tika.mime.MimeTypeException;
import org.apache.commons.io.FilenameUtils;

/**
 * Utility to rename temporary files based on detected MIME type using Apache Tika.
 */
public class FileExtensionHelper {
    private static final Tika tika = new Tika();
    private static final MimeTypes mimeRegistry = MimeTypes.getDefaultMimeTypes();

    private FileExtensionHelper() {
        // utility class
    }

    /**
     * Renames the given temporary file to have the correct extension based on its content type.
     * Uses Apache Tika for detection and recommended extensions. Leaves .tmp if no mapping found.
     *
     * @param tmpFile Path to the .tmp file
     * @return Path to the renamed file (may still end in .tmp)
     * @throws IOException if probing or moving fails
     */
    public static Path renameWithDetectedExtension(Path tmpFile) throws IOException {
        // 1) Detect MIME type via Tika
        String mime = tika.detect(tmpFile);
        if (mime == null || mime.isBlank()) {
            mime = "application/octet-stream";
        }

        // 2) Lookup recommended extension, default to ".tmp"
        String ext;
        try {
            MimeType mimeType = mimeRegistry.forName(mime);
            ext = mimeType.getExtension();
            if (ext == null || ext.isEmpty()) {
                ext = ".tmp";
            }
        } catch (MimeTypeException e) {
            // Leave .tmp if unrecognized
            ext = ".tmp";
        }

        // (strip existing extension)
        String baseName = FilenameUtils.getBaseName(tmpFile.getFileName().toString());


        Path finalPath = tmpFile.resolveSibling(baseName + ext);

        //   Rename (move) atomically within the same directory
        return Files.move(tmpFile, finalPath, StandardCopyOption.ATOMIC_MOVE);
    }
}
