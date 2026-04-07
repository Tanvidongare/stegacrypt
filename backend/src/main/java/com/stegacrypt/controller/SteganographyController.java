package com.stegacrypt.controller;

import com.stegacrypt.service.CompressionService;
import com.stegacrypt.service.ImageProcessingService;
import com.stegacrypt.service.SteganographyService;
import com.stegacrypt.util.AESUtil;
import com.stegacrypt.util.RSAUtil;
import com.stegacrypt.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

/**
 * SteganographyController.java
 * REST API endpoints for steganography operations.
 */
@RestController
@RequestMapping("/api")
public class SteganographyController {

    @Autowired
    private ImageProcessingService imageService;

    @Autowired
    private CompressionService compressionService;

    @Autowired
    private SteganographyService steganographyService;

    @PostMapping("/generate-keys")
    public ResponseEntity<?> generateKeyPair() {
        try {
            KeyPair keyPair = RSAUtil.generateKeyPair();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("algorithm", "RSA");
            response.put("keySize", RSAUtil.getKeySize());
            response.put("publicKey", RSAUtil.publicKeyToPem(keyPair.getPublic()));
            response.put("privateKey", RSAUtil.privateKeyToPem(keyPair.getPrivate()));
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return buildErrorResponse("Key generation failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/embed")
    public ResponseEntity<?> embedMessage(
            @RequestParam("image") MultipartFile image,
            @RequestParam("message") String message,
            @RequestParam("publicKey") String publicKey,
            @RequestParam(value = "useCompression", defaultValue = "true") boolean useCompression) {

        try {
            System.out.println("\n========== EMBED REQUEST ==========");
            System.out.println("Message length: " + message.length() + " characters");
            System.out.println("Compression requested: " + useCompression);
            System.out.println("Image: " + image.getOriginalFilename() +
                             " (" + String.format("%.2f", imageService.getFileSizeKB(image)) + " KB)");

            ValidationUtil.validateMessage(message);
            PublicKey recipientPublicKey = RSAUtil.parsePublicKey(publicKey);
            String seedMaterial = RSAUtil.getKeyFingerprint(recipientPublicKey);

            BufferedImage carrierImage = imageService.loadImage(image);
            System.out.println(imageService.getImageInfo(carrierImage));

            boolean compressed = useCompression && compressionService.shouldCompress(message);
            byte[] dataToEncrypt = compressed
                ? compressionService.compress(message)
                : message.getBytes(StandardCharsets.UTF_8);
            if (!compressed) {
                System.out.println("[Compression] Skipped (message too short or disabled)");
            }

            System.out.println("[Encryption] Encrypting with hybrid RSA + AES-GCM...");
            byte[] encryptedPayload = AESUtil.encrypt(dataToEncrypt, recipientPublicKey, compressed);
            System.out.println("[Encryption] Payload size: " + encryptedPayload.length + " bytes");

            steganographyService.embedData(carrierImage, encryptedPayload, seedMaterial);

            byte[] stegoImageBytes = imageService.saveImageAsPNG(carrierImage);
            System.out.println("[Output] Stego image size: " +
                             String.format("%.2f KB", stegoImageBytes.length / 1024.0));
            System.out.println("========== EMBED SUCCESS ==========\n");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentDispositionFormData("attachment", "stego_" +
                System.currentTimeMillis() + ".png");

            return new ResponseEntity<>(stegoImageBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("[ERROR] Embed failed: " + e.getMessage());
            e.printStackTrace();
            return buildErrorResponse("Embed failed: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/extract")
    public ResponseEntity<?> extractMessage(
            @RequestParam("image") MultipartFile image,
            @RequestParam("privateKey") String privateKey) {

        try {
            System.out.println("\n========== EXTRACT REQUEST ==========");
            System.out.println("Image: " + image.getOriginalFilename());

            PrivateKey recipientPrivateKey = RSAUtil.parsePrivateKey(privateKey);
            PublicKey recipientPublicKey = RSAUtil.derivePublicKey(recipientPrivateKey);
            String seedMaterial = RSAUtil.getKeyFingerprint(recipientPublicKey);

            BufferedImage stegoImage = imageService.loadImage(image);
            System.out.println(imageService.getImageInfo(stegoImage));

            byte[] encryptedData = steganographyService.extractData(stegoImage, seedMaterial);
            System.out.println("[Extract] Extracted " + encryptedData.length + " bytes");

            System.out.println("[Decryption] Decrypting with RSA private key...");
            AESUtil.DecryptionResult decrypted = AESUtil.decrypt(encryptedData, recipientPrivateKey);

            String message = decrypted.isCompressed()
                ? compressionService.decompress(decrypted.getPlainData())
                : new String(decrypted.getPlainData(), StandardCharsets.UTF_8);

            System.out.println("[Success] Extracted message length: " + message.length() + " characters");
            System.out.println("========== EXTRACT SUCCESS ==========\n");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", message);
            response.put("encryptedSize", encryptedData.length);
            response.put("messageLength", message.length());
            response.put("wrappedKeyLength", decrypted.getWrappedKeyLength());
            response.put("usedCompression", decrypted.isCompressed());
            response.put("encryptionMode", "RSA-OAEP + AES-256-GCM");
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("[ERROR] Extract failed: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Extract failed: " + e.getMessage());
            errorResponse.put("hint", "Check if the private key matches the public key used during embedding and ensure the image was not modified.");
            errorResponse.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PostMapping("/capacity")
    public ResponseEntity<?> checkCapacity(@RequestParam("image") MultipartFile image) {
        try {
            BufferedImage img = imageService.loadImage(image);
            int capacityBytes = steganographyService.getCapacityBytes(img);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("width", img.getWidth());
            response.put("height", img.getHeight());
            response.put("totalPixels", img.getWidth() * img.getHeight());
            response.put("capacityBytes", capacityBytes);
            response.put("capacityKB", capacityBytes / 1024.0);
            response.put("imageInfo", imageService.getImageInfo(img));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return buildErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "StegaCrypt API");
        response.put("version", "2.0.0");
        response.put("encryptionMode", "RSA-OAEP + AES-256-GCM");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(String message, HttpStatus status) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", message);
        errorResponse.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.status(status).body(errorResponse);
    }
}


