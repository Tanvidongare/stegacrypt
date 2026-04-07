# StegaCrypt Web Application

StegaCrypt is a web-based steganography project that hides encrypted text inside images. The current version uses a hybrid public-key design:

- RSA public key encrypts a one-time AES session key
- AES-256-GCM encrypts the message payload
- Randomized LSB steganography embeds the encrypted payload into the image
- The steganography pixel path is deterministically tied to the RSA key pair

## What Changed

The original build used a shared password for both encryption and pixel randomization. This version replaces that with public and private keys.

Embed flow:
1. Generate or paste an RSA public key
2. Encrypt the message with a random AES session key
3. Wrap the AES key with RSA-OAEP
4. Hide the final payload in the image

Extract flow:
1. Upload the stego image
2. Paste the matching RSA private key
3. Regenerate the randomized pixel sequence from the key pair
4. Recover and decrypt the hidden payload

## Project Structure

```text
backend/
  src/main/java/com/stegacrypt/
    controller/SteganographyController.java
    service/CompressionService.java
    service/ImageProcessingService.java
    service/SteganographyService.java
    util/AESUtil.java
    util/RSAUtil.java
    util/BitUtil.java
    util/PRNGUtil.java
    util/ValidationUtil.java
frontend/
  src/
    App.jsx
    App.css
    components/
    services/api.js
```

## Backend API

Base URL: `http://localhost:8080/api`

- `POST /generate-keys`
  Returns a fresh RSA key pair in PEM format.
- `POST /embed`
  Multipart fields: `image`, `message`, `publicKey`, `useCompression`
  Returns the stego image as PNG.
- `POST /extract`
  Multipart fields: `image`, `privateKey`
  Returns the extracted plaintext message and metadata.
- `POST /capacity`
  Multipart field: `image`
  Returns image capacity information.
- `GET /health`
  Returns service health and active encryption mode.

## Running the Application

### Backend

```bash
cd backend
mvn spring-boot:run
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

## Usage Notes

- Share only the public key for embedding.
- Keep the private key safe. Extraction will fail without the matching private key.
- Always download the stego output as PNG.
- JPG recompression can destroy hidden data.

## Security Summary

- RSA-2048 key pairs
- RSA-OAEP with SHA-256 for session-key wrapping
- AES-256-GCM for authenticated payload encryption
- Randomized LSB embedding with deterministic key-derived pixel selection
- Optional GZIP compression before encryption

## Academic Context

This project was developed as an MIT-WPU mini project for the 2025-2026 academic year.
