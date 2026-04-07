package com.stegacrypt.service;

import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * CompressionService.java
 * Handles GZIP compression and decompression of messages.
 * 
 * BENEFITS:
 * - Reduces message size by 50-70% (depending on content)
 * - Allows embedding longer messages in same image
 * - Adds entropy to data (slightly improves security)
 */
@Service
public class CompressionService {

    /**
     * Compresses a string using GZIP.
     * 
     * @param message Original message
     * @return Compressed byte array
     * @throws IOException if compression fails
     */
    public byte[] compress(String message) throws IOException {
        if (message == null || message.isEmpty()) {
            return new byte[0];
        }

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        
        try (GZIPOutputStream gzipStream = new GZIPOutputStream(byteStream)) {
            gzipStream.write(message.getBytes("UTF-8"));
        }
        
        byte[] compressed = byteStream.toByteArray();
        
        // Log compression ratio
        double ratio = 100.0 * compressed.length / message.getBytes("UTF-8").length;
        System.out.printf("[Compression] Original: %d bytes → Compressed: %d bytes (%.1f%%)%n",
            message.getBytes("UTF-8").length, compressed.length, ratio);
        
        return compressed;
    }

    /**
     * Decompresses GZIP data back to string.
     * 
     * @param compressedData Compressed byte array
     * @return Original message
     * @throws IOException if decompression fails
     */
    public String decompress(byte[] compressedData) throws IOException {
        if (compressedData == null || compressedData.length == 0) {
            return "";
        }

        ByteArrayInputStream byteStream = new ByteArrayInputStream(compressedData);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        try (GZIPInputStream gzipStream = new GZIPInputStream(byteStream)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzipStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, len);
            }
        }
        
        return outputStream.toString("UTF-8");
    }

    /**
     * Checks if compression would be beneficial.
     * For very short messages, compression overhead may increase size.
     * 
     * @param message Message to check
     * @return true if compression is recommended
     */
    public boolean shouldCompress(String message) {
        // GZIP adds ~20 bytes overhead, so only compress if message > 100 bytes
        return message != null && message.getBytes().length > 100;
    }

    /**
     * Gets compression ratio as a percentage.
     * 
     * @param original Original data
     * @param compressed Compressed data
     * @return Compression ratio (0-100%)
     */
    public double getCompressionRatio(byte[] original, byte[] compressed) {
        if (original == null || original.length == 0) return 0;
        return 100.0 * compressed.length / original.length;
    }
}
