package com.photoserve.photo_api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "photos")
public class Photo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "filename", unique = true, nullable = false)
    @NotBlank(message = "Filename cannot be empty")
    @Size(min = 1, max = 255, message = "Filename must be between 1 and 255 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_\\-\\.]+\\.(jpg|jpeg|png|gif|webp|heic|HEIC|JPG|JPEG|PNG|GIF|WEBP)$",
             message = "Invalid filename format. Must be a valid image file")
    private String filename;

    public Photo() {
    // Keep this empty
    }

    public Photo(String filename) {
        this.filename = filename;
    }

    // Constructor
    public Photo(Long id, String filename) {
        this.id = id;
        this.filename = filename;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
