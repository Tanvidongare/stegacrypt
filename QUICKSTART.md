# Quick Start Guide - StegaCrypt

## 1. Start the Backend

```bash
cd backend
mvn spring-boot:run
```

Backend URL: `http://localhost:8080`

## 2. Start the Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend URL: `http://localhost:3000`

## 3. Generate a Key Pair

In the Embed tab:

1. Click `Generate Key Pair`
2. Copy and store the private key safely
3. Use the public key for embedding

## 4. Hide a Message

1. Upload a PNG, JPG, or BMP carrier image
2. Enter the secret message
3. Paste or generate the recipient public key
4. Click `Embed Message`
5. Download the generated PNG stego image

## 5. Extract a Message

1. Upload the stego image
2. Paste the matching RSA private key
3. Click `Extract Message`

## Important

- The private key must match the public key used for embedding.
- Save the stego image as PNG only.
- If the image is modified after embedding, extraction may fail.
