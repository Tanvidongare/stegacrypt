package com.stegacrypt.util;

import java.awt.image.BufferedImage;

/**
 * ValidationUtil.java
 * Utility class for validating inputs and constraints.
 */
public class ValidationUtil {

    public static final int MIN_MESSAGE_LENGTH = 1;
    public static final int MAX_MESSAGE_LENGTH = 1_000_000;
    public static final int MIN_IMAGE_DIMENSION = 50;
    public static final int MAX_IMAGE_DIMENSION = 4096;
    public static final int MAX_IMAGE_PIXELS = 12_000_000;
    public static final long MAX_IMAGE_FILE_BYTES = 12L * 1024L * 1024L;
    public static final int HEADER_BITS = 64;

    public static void validateMessage(String message) {
        if (message == null || message.isEmpty()) {
            throw new IllegalArgumentException("Message cannot be empty");
        }

        if (message.length() < MIN_MESSAGE_LENGTH) {
            throw new IllegalArgumentException("Message is too short");
        }

        if (message.length() > MAX_MESSAGE_LENGTH) {
            throw new IllegalArgumentException(
                "Message is too long (max " + MAX_MESSAGE_LENGTH + " characters)"
            );
        }
    }

    public static void validateKeyMaterial(String keyMaterial, String label) {
        if (keyMaterial == null || keyMaterial.isBlank()) {
            throw new IllegalArgumentException(label + " cannot be empty");
        }
    }

    public static void validateImageFileSize(long sizeBytes) {
        if (sizeBytes > MAX_IMAGE_FILE_BYTES) {
            throw new IllegalArgumentException(
                String.format(
                    "Image file is too large for the deployed server. Maximum upload size: %.0f MB. Please use a smaller image.",
                    MAX_IMAGE_FILE_BYTES / 1024.0 / 1024.0
                )
            );
        }
    }

    public static void validateImageDimensions(int width, int height) {
        if (width < MIN_IMAGE_DIMENSION || height < MIN_IMAGE_DIMENSION) {
            throw new IllegalArgumentException(
                "Image too small. Minimum dimensions: " +
                MIN_IMAGE_DIMENSION + "x" + MIN_IMAGE_DIMENSION
            );
        }

        if (width > MAX_IMAGE_DIMENSION || height > MAX_IMAGE_DIMENSION) {
            throw new IllegalArgumentException(
                "Image too large. Maximum dimensions: " +
                MAX_IMAGE_DIMENSION + "x" + MAX_IMAGE_DIMENSION
            );
        }

        long totalPixels = (long) width * height;
        if (totalPixels > MAX_IMAGE_PIXELS) {
            throw new IllegalArgumentException(
                String.format(
                    "Image has too many pixels for the deployed server. Maximum: %.1f MP. Please resize the image.",
                    MAX_IMAGE_PIXELS / 1_000_000.0
                )
            );
        }
    }

    public static void validateImage(BufferedImage image) {
        if (image == null) {
            throw new IllegalArgumentException("Image cannot be null");
        }

        validateImageDimensions(image.getWidth(), image.getHeight());
    }

    public static void validateCapacity(BufferedImage image, int dataBits) {
        int totalPixels = image.getWidth() * image.getHeight();
        int availableBits = totalPixels;
        int requiredBits = HEADER_BITS + dataBits;

        if (requiredBits > availableBits) {
            double requiredKB = requiredBits / 8.0 / 1024.0;
            double availableKB = availableBits / 8.0 / 1024.0;

            throw new IllegalArgumentException(
                String.format(
                    "Image capacity exceeded! Required: %.2f KB, Available: %.2f KB. Use a larger image or compress your message.",
                    requiredKB, availableKB
                )
            );
        }
    }

    public static int getMaxMessageBytes(BufferedImage image) {
        int totalPixels = image.getWidth() * image.getHeight();
        int availableBits = totalPixels - HEADER_BITS;
        return availableBits / 8;
    }

    public static boolean isSupportedImageFormat(String filename) {
        if (filename == null) return false;
        String lower = filename.toLowerCase();
        return lower.endsWith(".png") ||
               lower.endsWith(".jpg") ||
               lower.endsWith(".jpeg") ||
               lower.endsWith(".bmp");
    }
}
