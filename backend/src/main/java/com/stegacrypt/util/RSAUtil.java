package com.stegacrypt.util;

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * RSAUtil.java
 * RSA key pair generation, PEM conversion, and session-key wrapping utilities.
 */
public class RSAUtil {

    private static final String RSA_ALGORITHM = "RSA";
    private static final String RSA_TRANSFORMATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    private static final int KEY_SIZE = 2048;

    public static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance(RSA_ALGORITHM);
        generator.initialize(KEY_SIZE);
        return generator.generateKeyPair();
    }

    public static String publicKeyToPem(PublicKey publicKey) {
        return toPem("PUBLIC KEY", publicKey.getEncoded());
    }

    public static String privateKeyToPem(PrivateKey privateKey) {
        return toPem("PRIVATE KEY", privateKey.getEncoded());
    }

    public static PublicKey parsePublicKey(String pem) throws Exception {
        ValidationUtil.validateKeyMaterial(pem, "Public key");
        byte[] keyBytes = decodePem(pem, "PUBLIC KEY");
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance(RSA_ALGORITHM).generatePublic(spec);
    }

    public static PrivateKey parsePrivateKey(String pem) throws Exception {
        ValidationUtil.validateKeyMaterial(pem, "Private key");
        byte[] keyBytes = decodePem(pem, "PRIVATE KEY");
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance(RSA_ALGORITHM).generatePrivate(spec);
    }

    public static PublicKey derivePublicKey(PrivateKey privateKey) throws Exception {
        if (!(privateKey instanceof RSAPrivateCrtKey rsaPrivateKey)) {
            throw new IllegalArgumentException("Unsupported private key format for public key derivation");
        }

        RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(
            rsaPrivateKey.getModulus(),
            rsaPrivateKey.getPublicExponent()
        );

        return KeyFactory.getInstance(RSA_ALGORITHM).generatePublic(publicKeySpec);
    }

    public static byte[] encryptSessionKey(byte[] sessionKey, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance(RSA_TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(sessionKey);
    }

    public static byte[] decryptSessionKey(byte[] wrappedSessionKey, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance(RSA_TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(wrappedSessionKey);
    }

    public static String getKeyFingerprint(PublicKey publicKey) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(publicKey.getEncoded());

        StringBuilder builder = new StringBuilder();
        for (byte b : hash) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

    public static int getKeySize() {
        return KEY_SIZE;
    }

    private static String toPem(String type, byte[] keyBytes) {
        String encoded = Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(keyBytes);
        return "-----BEGIN " + type + "-----\n" + encoded + "\n-----END " + type + "-----";
    }

    private static byte[] decodePem(String pem, String type) {
        String normalized = pem
            .replace("-----BEGIN " + type + "-----", "")
            .replace("-----END " + type + "-----", "")
            .replaceAll("\\s", "");

        return Base64.getDecoder().decode(normalized);
    }
}
