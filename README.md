# StegaCrypt

StegaCrypt is a full-stack steganography web application that encrypts text and hides it inside images. It combines public-key cryptography, randomized LSB embedding, and a built-in secure chat workflow in one project.

## Highlights

- RSA public/private key workflow for safer message sharing
- AES-256-GCM for message encryption with integrity protection
- RSA-OAEP wrapping for one-time AES session keys
- Randomized LSB steganography for image embedding
- Capacity checking before embedding
- Secure chat with login, registration, demo members, and recipient-based decryption
- React frontend and Spring Boot backend

## How It Works

### Embed

1. Upload a carrier image.
2. Generate or paste the recipient public key.
3. Encrypt the message with a fresh AES session key.
4. Wrap that key with RSA.
5. Embed the payload into the image and export a PNG stego file.

### Extract

1. Upload the stego image.
2. Provide the matching RSA private key or use the secure chat flow.
3. Rebuild the deterministic embedding path.
4. Recover and decrypt the hidden message.

## Tech Stack

- Frontend: React, Vite, CSS
- Backend: Spring Boot, Java 17, Maven
- Crypto: RSA-2048, RSA-OAEP, AES-256-GCM
- Image processing: randomized LSB steganography

## Local Run

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

Open `http://localhost:3000` after both services are running.

## Documentation

- [Quick Start](docs/QUICKSTART.md)
- [API Docs](docs/API_DOCS.md)
- [Deployment Guide](docs/DEPLOYMENT.md)
- [Run Guide and User Manual](docs/RUN_ME.md)
- [Project Report](docs/PROJECT_REPORT.md)
- [Hosting and Project Working](docs/HOSTING_AND_PROJECT_WORKING.md)

## Important Notes

- Keep the private key or generated key file safe.
- Use the exported PNG output for reliable extraction.
- Avoid editing or recompressing the stego image after embedding.
- Secure chat data is runtime-backed and may reset when the backend restarts.

## Academic Context

This project was developed as an MIT-WPU mini project for the 2025-2026 academic year.
