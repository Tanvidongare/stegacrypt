# StegaCrypt Hosting Platforms and Project Working

## Project Overview

StegaCrypt is a web-based steganography application that hides encrypted text inside images. The project is divided into two main parts:

- Frontend: A React + Vite web interface used by the user to upload images, enter messages, generate keys, embed messages, and extract hidden messages.
- Backend: A Spring Boot API that performs key generation, encryption, compression, image processing, and steganography operations.

This separation makes the project easier to deploy, maintain, and scale because the frontend and backend have different runtime requirements.

## Hosting Platforms Used

The project uses two hosting platforms:

- Netlify for the frontend.
- Render for the backend.

These platforms were chosen because they match the technical needs of each part of the application.

## Why Netlify Was Used for the Frontend

Netlify was used to host the React + Vite frontend because the frontend is a static web application after it is built.

During development, Vite runs the React app locally. During deployment, `npm run build` converts the frontend into optimized static files inside the `dist` folder. These files contain HTML, CSS, JavaScript, and assets that can be served directly by a static hosting platform.

Netlify is suitable for this because:

- It is designed for static frontend applications.
- It can automatically run the build command and publish the `dist` output.
- It provides fast global delivery for frontend files.
- It supports environment variables such as `VITE_API_BASE_URL`, which lets the frontend connect to the deployed backend API.
- It supports single-page application routing through redirects.
- It is simple to connect with a Git repository and redeploy whenever the frontend changes.

In this project, the `netlify.toml` file configures the frontend deployment:

```toml
[build]
  base = "frontend"
  command = "npm run build"
  publish = "dist"

[[redirects]]
  from = "/*"
  to = "/index.html"
  status = 200
```

The redirect rule is important because React apps are single-page applications. If the user refreshes a page or opens a route directly, Netlify sends the request back to `index.html`, allowing React to handle the route.

## Why Render Was Used for the Backend

Render was used to host the Spring Boot backend because the backend is not a static site. It is a Java server application that must keep running and expose REST API endpoints.

The backend performs tasks that require server-side execution, such as:

- Generating RSA key pairs.
- Encrypting message data using AES-256-GCM.
- Wrapping the AES session key using RSA-OAEP.
- Compressing and decompressing message data.
- Reading and writing image pixels.
- Embedding encrypted data into images.
- Extracting hidden data from stego images.

Render is suitable for this backend because:

- It supports web services that run continuously.
- It supports Docker deployments, which makes the Java 17 + Maven environment reproducible.
- It provides a public backend URL that the Netlify frontend can call.
- It supports health checks, which help confirm that the backend API is running.
- It supports environment variables such as `FRONTEND_ORIGINS`, which are used for CORS configuration.
- It has a free plan suitable for a student mini project or demonstration deployment.

In this project, the `render.yaml` file defines the backend service:

```yaml
services:
  - type: web
    name: stegacrypt-backend
    env: docker
    rootDir: backend
    plan: free
    healthCheckPath: /api/health
    envVars:
      - key: FRONTEND_ORIGINS
        value: http://localhost:3000,http://localhost:5173,https://*.netlify.app
```

The backend also includes a `Dockerfile`, which builds the Spring Boot application using Maven and then runs the final JAR using Java 17:

```dockerfile
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/stegacrypt-backend-1.0.0.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
```

## Why Two Platforms Were Used Instead of One

The project uses separate platforms because the frontend and backend have different hosting needs.

The frontend only needs static file hosting after the Vite build is complete, so Netlify is a lightweight and efficient choice. The backend needs a running Java server with API routes, image processing, and cryptography logic, so Render is more appropriate.

This split also improves maintainability:

- Frontend updates can be deployed independently on Netlify.
- Backend API updates can be deployed independently on Render.
- The frontend can point to the backend using `VITE_API_BASE_URL`.
- CORS can be controlled on the backend using allowed frontend origins.
- Each platform is used for what it does best.

## Working of the Project

The project works through a frontend-to-backend flow.

The user interacts with the React frontend. The frontend sends requests to the Spring Boot backend using Axios. The backend processes the request and sends the result back to the frontend.

The frontend API base URL is configured in `frontend/src/services/api.js`:

```javascript
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';
```

This means:

- In local development, the frontend uses `http://localhost:8080/api`.
- In deployment, Netlify uses the `VITE_API_BASE_URL` environment variable to call the Render backend.

## Main Backend API Endpoints

The backend exposes these main API endpoints:

- `POST /api/generate-keys`: Generates a new RSA public and private key pair.
- `POST /api/embed`: Embeds an encrypted message into an uploaded image.
- `POST /api/extract`: Extracts and decrypts a hidden message from a stego image.
- `POST /api/capacity`: Checks how much data an image can store.
- `GET /api/health`: Checks whether the backend service is running.

## Key Generation Flow

When the user clicks the key generation option:

1. The frontend sends a request to `/api/generate-keys`.
2. The backend generates an RSA-2048 key pair.
3. The backend returns the public key and private key in PEM format.
4. The user can share the public key for embedding.
5. The user must keep the private key safe because it is required for extraction.

## Embed Message Flow

The embed flow hides a message inside an image.

1. The user uploads a carrier image in the frontend.
2. The user enters the secret message.
3. The user provides an RSA public key.
4. The frontend sends the image, message, public key, and compression option to `/api/embed`.
5. The backend validates the message and image.
6. The backend parses the RSA public key.
7. The backend creates a key fingerprint from the public key. This fingerprint is later used as seed material for randomized pixel selection.
8. If compression is enabled and useful, the backend compresses the message.
9. The backend generates a random AES-256 session key.
10. The backend encrypts the message using AES-256-GCM.
11. The backend wraps the AES session key using the RSA public key with RSA-OAEP.
12. The backend creates a final encrypted payload containing metadata, the wrapped AES key, the AES IV, and the ciphertext.
13. The steganography service writes the payload length into the image header area.
14. The remaining encrypted payload bits are embedded into the least significant bits of selected image pixels.
15. The pixel positions are randomized using the key-derived seed.
16. The backend returns the final stego image as a PNG file.
17. The frontend lets the user download the stego image.

PNG output is important because PNG is lossless. Formats like JPG can recompress image data and destroy the hidden bits.

## Extract Message Flow

The extract flow recovers a hidden message from a stego image.

1. The user uploads the stego image.
2. The user provides the matching RSA private key.
3. The frontend sends the image and private key to `/api/extract`.
4. The backend parses the private key.
5. The backend derives the matching public key from the private key.
6. The backend recreates the same key fingerprint used during embedding.
7. The steganography service reads the payload length from the image header area.
8. The service regenerates the same randomized pixel sequence using the key fingerprint.
9. The encrypted payload bits are extracted from the image pixels.
10. The backend unwraps the AES session key using the RSA private key.
11. The backend decrypts the ciphertext using AES-256-GCM.
12. If the original message was compressed, the backend decompresses it.
13. The backend returns the plaintext message and metadata to the frontend.

If the private key does not match the public key used during embedding, the backend will not regenerate the correct key-derived sequence or decrypt the AES session key correctly. In that case, extraction fails.

## Security Approach

The project uses hybrid encryption:

- RSA-2048 is used for public/private key based access control.
- RSA-OAEP with SHA-256 is used to protect the AES session key.
- AES-256-GCM is used to encrypt the actual message payload.
- AES-GCM also provides authentication, so corrupted or tampered encrypted data can be detected during decryption.
- Randomized LSB steganography hides payload bits across image pixels instead of placing all data in a simple sequential pattern.
- The randomized pixel sequence is tied to the RSA key pair, so extraction depends on the matching private key.

## Overall Deployment Flow

The complete deployed setup works like this:

1. The user opens the Netlify-hosted frontend.
2. Netlify serves the built React/Vite static files.
3. The frontend reads `VITE_API_BASE_URL`.
4. API requests are sent from the Netlify frontend to the Render backend.
5. Render runs the Spring Boot backend inside a Docker container.
6. The backend processes steganography and encryption operations.
7. The backend returns images, messages, or metadata to the frontend.

## Conclusion

Netlify was used because the frontend is a static React/Vite application that can be built and served efficiently. Render was used because the backend is a continuously running Spring Boot Java API that needs Docker support, environment variables, health checks, and server-side processing.

Together, Netlify and Render provide a clean full-stack deployment model for StegaCrypt: Netlify handles the user interface, while Render handles the secure backend logic for encryption, image processing, and steganography.
