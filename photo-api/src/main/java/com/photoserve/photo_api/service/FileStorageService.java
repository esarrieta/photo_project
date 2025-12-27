package com.photoserve.photo_api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageService(@Value("${file.upload-dir}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("Could not create upload directory!", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        // Normalize file name
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // Check if the file's name contains invalid characters
            if (originalFilename.contains("..")) {
                throw new RuntimeException("Invalid path sequence in filename: " + originalFilename);
            }

            // Generate unique filename to avoid conflicts
            String extension = "";
            int lastDot = originalFilename.lastIndexOf('.');
            if (lastDot > 0) {
                extension = originalFilename.substring(lastDot);
            }
            String uniqueFilename = UUID.randomUUID().toString() + extension;

            // Copy file to the target location
            Path targetLocation = this.fileStorageLocation.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return uniqueFilename;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + originalFilename + ". Please try again!", ex);
        }
    }

    public String storeFileWithOriginalName(MultipartFile file) {
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            if (originalFilename.contains("..")) {
                throw new RuntimeException("Invalid path sequence in filename: " + originalFilename);
            }

            Path targetLocation = this.fileStorageLocation.resolve(originalFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return originalFilename;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + originalFilename + ". Please try again!", ex);
        }
    }

    public Path getFileStorageLocation() {
        return fileStorageLocation;
    }

    public void deleteFile(String filename) {
        try {
            Path filePath = this.fileStorageLocation.resolve(filename).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new RuntimeException("Could not delete file " + filename + ". Please try again!", ex);
        }
    }

    public Path loadFile(String filename) {
        Path filePath = this.fileStorageLocation.resolve(filename).normalize();
        if (!Files.exists(filePath)) {
            throw new RuntimeException("File not found: " + filename);
        }
        return filePath;
    }
}
