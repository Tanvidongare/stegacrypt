# StegaCrypt API Documentation

Base URL: `http://localhost:8080/api`

## 1. Generate RSA Key Pair

`POST /generate-keys`

Response:

```json
{
  "success": true,
  "algorithm": "RSA",
  "keySize": 2048,
  "publicKey": "-----BEGIN PUBLIC KEY-----...",
  "privateKey": "-----BEGIN PRIVATE KEY-----...",
  "timestamp": 1710000000000
}
```

## 2. Embed Message

`POST /embed`

Multipart form fields:

- `image`: carrier image file
- `message`: plaintext message
- `publicKey`: RSA public key in PEM format
- `useCompression`: `true` or `false`

Success response:

- `200 OK`
- `Content-Type: image/png`
- Response body is the stego image

## 3. Extract Message

`POST /extract`

Multipart form fields:

- `image`: stego image file
- `privateKey`: RSA private key in PEM format

Success response:

```json
{
  "success": true,
  "message": "hidden text",
  "encryptedSize": 928,
  "messageLength": 11,
  "wrappedKeyLength": 256,
  "usedCompression": false,
  "encryptionMode": "RSA-OAEP + AES-256-GCM",
  "timestamp": 1710000000000
}
```

## 4. Check Capacity

`POST /capacity`

Multipart form fields:

- `image`: carrier image file

Response:

```json
{
  "success": true,
  "width": 1920,
  "height": 1080,
  "totalPixels": 2073600,
  "capacityBytes": 259184,
  "capacityKB": 253.11,
  "imageInfo": "Dimensions: 1920x1080 | Total Pixels: 2073600 | Type: INT_ARGB | Capacity: ~253 KB"
}
```

## 5. Health Check

`GET /health`

Response:

```json
{
  "status": "UP",
  "service": "StegaCrypt API",
  "version": "2.0.0",
  "encryptionMode": "RSA-OAEP + AES-256-GCM",
  "timestamp": 1710000000000
}
```

## 6. Secure Chat Login

`POST /auth/login`

Form fields:

- `username`
- `password`

## 7. Secure Chat Registration

`POST /auth/register`

Form fields:

- `fullName`
- `username`
- `password`

## 8. Secure Chat Bootstrap

`GET /auth/chat`

Header:

- `X-Auth-Token`

Response:

```json
{
  "success": true,
  "currentUser": {
    "fullName": "Abhishek Sushant Chaskar",
    "username": "abhishek.sushant.chaskar",
    "seeded": true
  },
  "members": [
    {
      "fullName": "Tanvi Dongare",
      "username": "tanvi.dongare",
      "seeded": true
    }
  ],
  "seededMembers": [
    {
      "fullName": "Aditya Atul Deshpande",
      "username": "aditya.atul.deshpande",
      "seeded": true,
      "passwordHint": "Stega@123"
    }
  ],
  "messages": [],
  "timestamp": 1710000000000
}
```

## Current Crypto Design

- RSA-2048 key pair generation
- RSA-OAEP with SHA-256 for wrapping the AES session key
- AES-256-GCM for payload encryption and integrity protection
- Randomized LSB steganography for image embedding
- Optional GZIP compression before encryption

## Operational Notes

- The public key is enough to embed a message.
- The matching private key is required to extract it.
- PNG output is mandatory for safe steganographic recovery.
