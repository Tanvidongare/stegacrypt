package com.stegacrypt.util;

/**
 * BitUtil.java
 * Utility class for converting between bytes, bits, and integers.
 * Essential for LSB steganography operations.
 */
public class BitUtil {

    /**
     * Converts a byte array to a bit array (0s and 1s).
     * Each byte produces 8 bits, MSB first.
     * 
     * Example: byte[]{170} → int[]{1,0,1,0,1,0,1,0}
     * 
     * @param bytes Input byte array
     * @return Array of bits (0s and 1s)
     */
    public static int[] bytesToBits(byte[] bytes) {
        int[] bits = new int[bytes.length * 8];
        for (int i = 0; i < bytes.length; i++) {
            for (int bit = 7; bit >= 0; bit--) {
                bits[i * 8 + (7 - bit)] = (bytes[i] >> bit) & 1;
            }
        }
        return bits;
    }

    /**
     * Converts a bit array back to bytes.
     * Groups of 8 bits are combined into single bytes.
     * 
     * @param bits Array of bits (must be multiple of 8)
     * @return Reconstructed byte array
     */
    public static byte[] bitsToBytes(int[] bits) {
        if (bits.length % 8 != 0) {
            throw new IllegalArgumentException("Bit array length must be multiple of 8");
        }
        
        byte[] bytes = new byte[bits.length / 8];
        for (int i = 0; i < bytes.length; i++) {
            int value = 0;
            for (int bit = 0; bit < 8; bit++) {
                value = (value << 1) | bits[i * 8 + bit];
            }
            bytes[i] = (byte) value;
        }
        return bytes;
    }

    /**
     * Converts an integer to 32 bits.
     * Used for storing message length header in the image.
     * 
     * @param value Integer to convert
     * @return 32-bit array representation
     */
    public static int[] intToBits(int value) {
        int[] bits = new int[32];
        for (int i = 31; i >= 0; i--) {
            bits[31 - i] = (value >> i) & 1;
        }
        return bits;
    }

    /**
     * Converts 32 bits back to an integer.
     * Used for reading message length header from the image.
     * 
     * @param bits 32-bit array
     * @return Reconstructed integer
     */
    public static int bitsToInt(int[] bits) {
        if (bits.length != 32) {
            throw new IllegalArgumentException("Bit array must be exactly 32 bits for integer conversion");
        }
        
        int value = 0;
        for (int i = 0; i < 32; i++) {
            value = (value << 1) | bits[i];
        }
        return value;
    }

    /**
     * Converts a long to 64 bits.
     * Used for extended metadata storage.
     * 
     * @param value Long to convert
     * @return 64-bit array representation
     */
    public static int[] longToBits(long value) {
        int[] bits = new int[64];
        for (int i = 63; i >= 0; i--) {
            bits[63 - i] = (int)((value >> i) & 1);
        }
        return bits;
    }

    /**
     * Converts 64 bits back to a long.
     * 
     * @param bits 64-bit array
     * @return Reconstructed long
     */
    public static long bitsToLong(int[] bits) {
        if (bits.length != 64) {
            throw new IllegalArgumentException("Bit array must be exactly 64 bits for long conversion");
        }
        
        long value = 0;
        for (int i = 0; i < 64; i++) {
            value = (value << 1) | bits[i];
        }
        return value;
    }
}
