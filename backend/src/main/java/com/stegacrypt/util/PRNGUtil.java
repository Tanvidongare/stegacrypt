package com.stegacrypt.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * PRNGUtil.java
 * Pseudo-random utility for deterministic pixel selection.
 *
 * The caller provides any stable seed material. In the current design this is the
 * SHA-256 fingerprint of the RSA public key derived from the key pair.
 */
public class PRNGUtil {

    public static List<Integer> generateRandomPixelSequence(String seedMaterial, int totalPixels, int count) {
        if (count > totalPixels) {
            throw new IllegalArgumentException(
                "Cannot generate " + count + " pixels from image with only " + totalPixels + " pixels"
            );
        }

        long seed = generateSeed(seedMaterial);
        Random random = new Random(seed);

        List<Integer> allPixels = new ArrayList<>(totalPixels);
        for (int i = 0; i < totalPixels; i++) {
            allPixels.add(i);
        }

        Collections.shuffle(allPixels, random);
        return allPixels.subList(0, count);
    }

    private static long generateSeed(String seedMaterial) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(seedMaterial.getBytes(StandardCharsets.UTF_8));

            long seed = 0;
            for (int i = 0; i < 8; i++) {
                seed = (seed << 8) | (hash[i] & 0xFF);
            }
            return seed;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    public static List<Integer> generateSequenceWithHeader(
            String seedMaterial,
            int totalPixels,
            int headerSize,
            int dataPixelsNeeded) {

        List<Integer> sequence = new ArrayList<>();
        for (int i = 0; i < headerSize; i++) {
            sequence.add(i);
        }

        long seed = generateSeed(seedMaterial);
        Random random = new Random(seed);

        List<Integer> availablePixels = new ArrayList<>();
        for (int i = headerSize; i < totalPixels; i++) {
            availablePixels.add(i);
        }

        Collections.shuffle(availablePixels, random);
        sequence.addAll(availablePixels.subList(0, dataPixelsNeeded));

        return sequence;
    }

    public static boolean testDeterminism(String seedMaterial, int totalPixels, int count) {
        List<Integer> seq1 = generateRandomPixelSequence(seedMaterial, totalPixels, count);
        List<Integer> seq2 = generateRandomPixelSequence(seedMaterial, totalPixels, count);
        return seq1.equals(seq2);
    }
}
