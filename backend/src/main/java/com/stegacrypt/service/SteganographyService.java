package com.stegacrypt.service;

import com.stegacrypt.util.BitUtil;
import com.stegacrypt.util.PRNGUtil;
import com.stegacrypt.util.ValidationUtil;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * SteganographyService.java
 * Enhanced LSB steganography with randomized pixel selection.
 */
@Service
public class SteganographyService {

    private static final int HEADER_PIXELS = 64;

    public void embedData(BufferedImage image, byte[] encryptedData, String seedMaterial) throws Exception {
        ValidationUtil.validateImage(image);
        ValidationUtil.validateKeyMaterial(seedMaterial, "Key seed");

        int totalPixels = image.getWidth() * image.getHeight();
        int dataBits = encryptedData.length * 8;
        int requiredBits = HEADER_PIXELS + dataBits;

        System.out.printf("[Stego] Image: %dx%d = %d pixels%n",
            image.getWidth(), image.getHeight(), totalPixels);
        System.out.printf("[Stego] Data: %d bytes = %d bits%n",
            encryptedData.length, dataBits);
        System.out.printf("[Stego] Required: %d pixels (including header)%n", requiredBits);

        ValidationUtil.validateCapacity(image, dataBits);

        int[] lengthBits = BitUtil.intToBits(encryptedData.length);
        int[] dataBitsArray = BitUtil.bytesToBits(encryptedData);

        System.out.println("[Stego] Embedding header (64 pixels)...");
        embedBitsSequential(image, lengthBits, 0);

        System.out.println("[Stego] Generating randomized pixel sequence...");
        List<Integer> randomPixels = PRNGUtil.generateRandomPixelSequence(
            seedMaterial,
            totalPixels - HEADER_PIXELS,
            dataBitsArray.length
        );

        for (int i = 0; i < randomPixels.size(); i++) {
            randomPixels.set(i, randomPixels.get(i) + HEADER_PIXELS);
        }

        System.out.println("[Stego] Embedding data in randomized pixels...");
        embedBitsRandomized(image, dataBitsArray, randomPixels);

        System.out.println("[Stego] Embedding complete");
    }

    public byte[] extractData(BufferedImage image, String seedMaterial) throws Exception {
        ValidationUtil.validateImage(image);
        ValidationUtil.validateKeyMaterial(seedMaterial, "Key seed");

        int width = image.getWidth();
        int totalPixels = width * image.getHeight();

        System.out.println("[Stego] Starting extraction...");

        int[] lengthBits = new int[32];
        for (int i = 0; i < 32; i++) {
            int x = i % width;
            int y = i / width;
            int pixel = image.getRGB(x, y);
            int blue = pixel & 0xFF;
            lengthBits[i] = blue & 1;
        }

        int dataLength = BitUtil.bitsToInt(lengthBits);
        System.out.printf("[Stego] Header decoded: %d bytes of hidden data%n", dataLength);

        if (dataLength <= 0 || dataLength > 10_000_000) {
            throw new IllegalArgumentException(
                "Invalid data length detected: " + dataLength + ". Wrong private key or corrupted image."
            );
        }

        int dataBits = dataLength * 8;
        if (HEADER_PIXELS + dataBits > totalPixels) {
            throw new IllegalArgumentException("Data length exceeds image capacity");
        }

        System.out.println("[Stego] Regenerating pixel sequence from key fingerprint...");
        List<Integer> randomPixels = PRNGUtil.generateRandomPixelSequence(
            seedMaterial,
            totalPixels - HEADER_PIXELS,
            dataBits
        );

        for (int i = 0; i < randomPixels.size(); i++) {
            randomPixels.set(i, randomPixels.get(i) + HEADER_PIXELS);
        }

        System.out.println("[Stego] Extracting data bits...");
        int[] extractedBits = new int[dataBits];

        for (int i = 0; i < dataBits; i++) {
            int pixelIndex = randomPixels.get(i);
            int x = pixelIndex % width;
            int y = pixelIndex / width;
            int pixel = image.getRGB(x, y);
            int blue = pixel & 0xFF;
            extractedBits[i] = blue & 1;
        }

        byte[] encryptedData = BitUtil.bitsToBytes(extractedBits);
        System.out.printf("[Stego] Extracted %d bytes%n", encryptedData.length);

        return encryptedData;
    }

    private void embedBitsSequential(BufferedImage image, int[] bits, int startPixel) {
        int width = image.getWidth();

        for (int i = 0; i < bits.length; i++) {
            int pixelIndex = startPixel + i;
            int x = pixelIndex % width;
            int y = pixelIndex / width;
            embedBitInPixel(image, x, y, bits[i]);
        }
    }

    private void embedBitsRandomized(BufferedImage image, int[] bits, List<Integer> pixelSequence) {
        int width = image.getWidth();

        for (int i = 0; i < bits.length; i++) {
            int pixelIndex = pixelSequence.get(i);
            int x = pixelIndex % width;
            int y = pixelIndex / width;
            embedBitInPixel(image, x, y, bits[i]);
        }
    }

    private void embedBitInPixel(BufferedImage image, int x, int y, int bit) {
        int pixel = image.getRGB(x, y);
        int alpha = (pixel >> 24) & 0xFF;
        int red = (pixel >> 16) & 0xFF;
        int green = (pixel >> 8) & 0xFF;
        int blue = pixel & 0xFF;

        blue = (blue & 0xFE) | bit;

        int newPixel = (alpha << 24) | (red << 16) | (green << 8) | blue;
        image.setRGB(x, y, newPixel);
    }

    public int getCapacityBytes(BufferedImage image) {
        int totalPixels = image.getWidth() * image.getHeight();
        int availableBits = totalPixels - HEADER_PIXELS;
        return availableBits / 8;
    }
}
