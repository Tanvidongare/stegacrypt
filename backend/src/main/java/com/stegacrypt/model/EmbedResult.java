package com.stegacrypt.model;

/**
 * Embed result data.
 */
public class EmbedResult {

    private int originalSize;
    private int compressedSize;
    private int encryptedSize;
    private double compressionRatio;
    private int imageCapacity;
    private String imageInfo;

    // Getters and Setters
    public int getOriginalSize() { return originalSize; }
    public void setOriginalSize(int originalSize) { this.originalSize = originalSize; }

    public int getCompressedSize() { return compressedSize; }
    public void setCompressedSize(int compressedSize) { this.compressedSize = compressedSize; }

    public int getEncryptedSize() { return encryptedSize; }
    public void setEncryptedSize(int encryptedSize) { this.encryptedSize = encryptedSize; }

    public double getCompressionRatio() { return compressionRatio; }
    public void setCompressionRatio(double compressionRatio) { this.compressionRatio = compressionRatio; }

    public int getImageCapacity() { return imageCapacity; }
    public void setImageCapacity(int imageCapacity) { this.imageCapacity = imageCapacity; }

    public String getImageInfo() { return imageInfo; }
    public void setImageInfo(String imageInfo) { this.imageInfo = imageInfo; }
}
