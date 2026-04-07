package com.stegacrypt.model;

/**
 * Extract result data.
 */
public class ExtractResult {

    private String message;
    private int encryptedSize;
    private int decompressedSize;

    public ExtractResult() {}

    public ExtractResult(String message, int encryptedSize, int decompressedSize) {
        this.message = message;
        this.encryptedSize = encryptedSize;
        this.decompressedSize = decompressedSize;
    }

    // Getters and Setters
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public int getEncryptedSize() { return encryptedSize; }
    public void setEncryptedSize(int encryptedSize) { this.encryptedSize = encryptedSize; }

    public int getDecompressedSize() { return decompressedSize; }
    public void setDecompressedSize(int decompressedSize) { this.decompressedSize = decompressedSize; }
}
