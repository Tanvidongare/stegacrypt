package com.stegacrypt.model;

import jakarta.validation.constraints.NotBlank;

/**
 * Request model for extracting a message with a private key.
 */
public class ExtractRequest {

    @NotBlank(message = "Private key cannot be empty")
    private String privateKey;

    public ExtractRequest() {}

    public ExtractRequest(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getPrivateKey() { return privateKey; }
    public void setPrivateKey(String privateKey) { this.privateKey = privateKey; }
}
