package com.stegacrypt.service;

import com.stegacrypt.util.ValidationUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * ImageProcessingService.java
 * Handles image loading, saving, and format conversions.
 */
@Service
public class ImageProcessingService {

    /**
     * Loads image from MultipartFile.
     * Converts to TYPE_INT_ARGB for consistent pixel manipulation.
     * 
     * @param file Uploaded image file
     * @return BufferedImage in ARGB format
     * @throws IOException if loading fails
     */
    public BufferedImage loadImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        
        String filename = file.getOriginalFilename();
        if (!ValidationUtil.isSupportedImageFormat(filename)) {
            throw new IllegalArgumentException(
                "Unsupported format. Supported: PNG, JPG, JPEG, BMP"
            );
        }
        
        BufferedImage image = ImageIO.read(file.getInputStream());
        
        if (image == null) {
            throw new IOException("Failed to read image file");
        }
        
        ValidationUtil.validateImage(image);
        
        // Convert to TYPE_INT_ARGB for consistent RGBA access
        if (image.getType() != BufferedImage.TYPE_INT_ARGB) {
            BufferedImage converted = new BufferedImage(
                image.getWidth(),
                image.getHeight(),
                BufferedImage.TYPE_INT_ARGB
            );
            converted.getGraphics().drawImage(image, 0, 0, null);
            return converted;
        }
        
        return image;
    }

    /**
     * Loads image from byte array.
     */
    public BufferedImage loadImageFromBytes(byte[] imageData) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
        BufferedImage image = ImageIO.read(bais);
        
        if (image == null) {
            throw new IOException("Failed to decode image data");
        }
        
        ValidationUtil.validateImage(image);
        
        // Convert to ARGB
        if (image.getType() != BufferedImage.TYPE_INT_ARGB) {
            BufferedImage converted = new BufferedImage(
                image.getWidth(),
                image.getHeight(),
                BufferedImage.TYPE_INT_ARGB
            );
            converted.getGraphics().drawImage(image, 0, 0, null);
            return converted;
        }
        
        return image;
    }

    /**
     * Saves BufferedImage to byte array as PNG.
     * PNG is lossless, preserving hidden LSB data.
     * 
     * @param image Image to save
     * @return PNG byte array
     * @throws IOException if saving fails
     */
    public byte[] saveImageAsPNG(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        if (!ImageIO.write(image, "PNG", baos)) {
            throw new IOException("Failed to write PNG image");
        }
        
        return baos.toByteArray();
    }

    /**
     * Saves BufferedImage to byte array with specified format.
     * 
     * WARNING: JPG is lossy and will corrupt hidden data!
     * Only use PNG for stego images.
     * 
     * @param image Image to save
     * @param format "PNG", "JPG", or "BMP"
     * @return Image byte array
     * @throws IOException if saving fails
     */
    public byte[] saveImage(BufferedImage image, String format) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        if (!ImageIO.write(image, format.toUpperCase(), baos)) {
            throw new IOException("Failed to write " + format + " image");
        }
        
        return baos.toByteArray();
    }

    /**
     * Gets image information as a formatted string.
     */
    public String getImageInfo(BufferedImage image) {
        return String.format(
            "Dimensions: %dx%d | Total Pixels: %d | Type: %s | Capacity: ~%d KB",
            image.getWidth(),
            image.getHeight(),
            image.getWidth() * image.getHeight(),
            getImageTypeName(image.getType()),
            ValidationUtil.getMaxMessageBytes(image) / 1024
        );
    }

    /**
     * Converts BufferedImage type constant to human-readable name.
     */
    private String getImageTypeName(int type) {
        switch (type) {
            case BufferedImage.TYPE_INT_ARGB: return "INT_ARGB";
            case BufferedImage.TYPE_INT_RGB: return "INT_RGB";
            case BufferedImage.TYPE_3BYTE_BGR: return "3BYTE_BGR";
            case BufferedImage.TYPE_4BYTE_ABGR: return "4BYTE_ABGR";
            case BufferedImage.TYPE_BYTE_GRAY: return "BYTE_GRAY";
            default: return "CUSTOM";
        }
    }

    /**
     * Validates if file is an image.
     */
    public boolean isImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }

    /**
     * Gets file size in KB.
     */
    public double getFileSizeKB(MultipartFile file) {
        return file.getSize() / 1024.0;
    }
}
