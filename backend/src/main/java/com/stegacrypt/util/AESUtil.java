package com.stegacrypt.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * AESUtil.java
 * Hybrid cryptography utility that combines RSA key wrapping with AES-GCM payload encryption.
 */
public class AESUtil {

    private static final byte[] MAGIC = "SGC2".getBytes(StandardCharsets.US_ASCII);
    private static final byte VERSION = 1;
    private static final byte FLAG_COMPRESSED = 1;

    private static final String AES_ALGORITHM = "AES";
    private static final String AES_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int AES_KEY_LENGTH = 256;
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Encrypts message bytes using a random AES session key and wraps that key with the RSA public key.
     *
     * Payload format:
     * [4-byte magic][1-byte version][1-byte flags][2-byte wrappedKeyLength]
     * [1-byte ivLength][4-byte cipherLength][wrappedKey][iv][cipherText]
     */
    public static byte[] encrypt(byte[] plainData, PublicKey publicKey, boolean compressed) throws Exception {
        SecretKey sessionKey = generateSessionKey();
        byte[] iv = new byte[GCM_IV_LENGTH];
        SECURE_RANDOM.nextBytes(iv);

        Cipher aesCipher = Cipher.getInstance(AES_TRANSFORMATION);
        aesCipher.init(Cipher.ENCRYPT_MODE, sessionKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
        byte[] cipherText = aesCipher.doFinal(plainData);

        byte[] wrappedKey = RSAUtil.encryptSessionKey(sessionKey.getEncoded(), publicKey);
        byte flags = (byte) (compressed ? FLAG_COMPRESSED : 0);

        ByteBuffer buffer = ByteBuffer.allocate(
            MAGIC.length + 1 + 1 + 2 + 1 + 4 + wrappedKey.length + iv.length + cipherText.length
        );
        buffer.put(MAGIC);
        buffer.put(VERSION);
        buffer.put(flags);
        buffer.putShort((short) wrappedKey.length);
        buffer.put((byte) iv.length);
        buffer.putInt(cipherText.length);
        buffer.put(wrappedKey);
        buffer.put(iv);
        buffer.put(cipherText);

        return buffer.array();
    }

    /**
     * Decrypts a hybrid RSA + AES-GCM payload using the RSA private key.
     */
    public static DecryptionResult decrypt(byte[] encryptedData, PrivateKey privateKey) throws Exception {
        if (encryptedData == null || encryptedData.length < 13) {
            throw new IllegalArgumentException("Encrypted payload is too short or corrupted");
        }

        ByteBuffer buffer = ByteBuffer.wrap(encryptedData);
        byte[] magic = new byte[MAGIC.length];
        buffer.get(magic);
        if (!Arrays.equals(magic, MAGIC)) {
            throw new IllegalArgumentException("Unsupported stego payload format");
        }

        byte version = buffer.get();
        if (version != VERSION) {
            throw new IllegalArgumentException("Unsupported payload version: " + version);
        }

        byte flags = buffer.get();
        boolean compressed = (flags & FLAG_COMPRESSED) != 0;

        int wrappedKeyLength = Short.toUnsignedInt(buffer.getShort());
        int ivLength = Byte.toUnsignedInt(buffer.get());
        int cipherLength = buffer.getInt();

        if (wrappedKeyLength <= 0 || ivLength <= 0 || cipherLength <= 0) {
            throw new IllegalArgumentException("Invalid payload metadata detected");
        }

        int expectedRemaining = wrappedKeyLength + ivLength + cipherLength;
        if (buffer.remaining() != expectedRemaining) {
            throw new IllegalArgumentException("Payload length mismatch detected");
        }

        byte[] wrappedKey = new byte[wrappedKeyLength];
        byte[] iv = new byte[ivLength];
        byte[] cipherText = new byte[cipherLength];
        buffer.get(wrappedKey);
        buffer.get(iv);
        buffer.get(cipherText);

        byte[] sessionKeyBytes = RSAUtil.decryptSessionKey(wrappedKey, privateKey);
        SecretKey sessionKey = new SecretKeySpec(sessionKeyBytes, AES_ALGORITHM);

        Cipher aesCipher = Cipher.getInstance(AES_TRANSFORMATION);
        aesCipher.init(Cipher.DECRYPT_MODE, sessionKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
        byte[] plainData = aesCipher.doFinal(cipherText);

        return new DecryptionResult(plainData, compressed, wrappedKeyLength, cipherText.length);
    }

    private static SecretKey generateSessionKey() throws Exception {
        KeyGenerator generator = KeyGenerator.getInstance(AES_ALGORITHM);
        generator.init(AES_KEY_LENGTH, SECURE_RANDOM);
        return generator.generateKey();
    }

    public static final class DecryptionResult {
        private final byte[] plainData;
        private final boolean compressed;
        private final int wrappedKeyLength;
        private final int encryptedPayloadLength;

        public DecryptionResult(byte[] plainData, boolean compressed, int wrappedKeyLength, int encryptedPayloadLength) {
            this.plainData = plainData;
            this.compressed = compressed;
            this.wrappedKeyLength = wrappedKeyLength;
            this.encryptedPayloadLength = encryptedPayloadLength;
        }

        public byte[] getPlainData() {
            return plainData;
        }

        public boolean isCompressed() {
            return compressed;
        }

        public int getWrappedKeyLength() {
            return wrappedKeyLength;
        }

        public int getEncryptedPayloadLength() {
            return encryptedPayloadLength;
        }
    }
}
