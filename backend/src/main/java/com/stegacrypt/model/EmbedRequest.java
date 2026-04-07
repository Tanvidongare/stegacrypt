package com.stegacrypt.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request model for embedding a message with a public key.
 */
public class EmbedRequest {

    @NotBlank(message = "Message cannot be empty")
    @Size(min = 1, max = 1000000, message = "Message length must be between 1 and 1,000,000 characters")
    private String message;

    @NotBlank(message = "Public key cannot be empty")
    private String publicKey;

    private boolean useCompression = true;

    public EmbedRequest() {}

    public EmbedRequest(String message, String publicKey, boolean useCompression) {
        this.message = message;
        this.publicKey = publicKey;
        this.useCompression = useCompression;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getPublicKey() { return publicKey; }
    public void setPublicKey(String publicKey) { this.publicKey = publicKey; }

    public boolean isUseCompression() { return useCompression; }
    public void setUseCompression(boolean useCompression) { this.useCompression = useCompression; }
}
