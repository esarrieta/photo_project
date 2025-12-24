package com.photoserve.photo_api.controller;

import com.photoserve.photo_api.model.Photo;
import com.photoserve.photo_api.repository.PhotoRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/photos")
public class PhotoController {

    @Autowired
    private PhotoRepository photoRepository;

    @GetMapping
    public List<Photo> getAllPhotos() {
        return photoRepository.findAll();
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<?> getPhotoById(@PathVariable Long id) { 
        return photoRepository.findById(id)
            .<ResponseEntity<?>>map(photo -> ResponseEntity.ok(photo))
            .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Photo with ID " + id + " not found."));
    }

    @GetMapping("/file/{filename}")
    public ResponseEntity<?> getPhotoByFilename(@PathVariable String filename) {
        return photoRepository.findByFilename(filename)
            .<ResponseEntity<?>>map(photo -> ResponseEntity.ok(photo))
            .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Photo with filename '" + filename + "' not found in database."));
    }

    @PostMapping
    public ResponseEntity<?> createPhoto(@Valid @RequestBody Photo photo, BindingResult bindingResult) {
        // Check for validation errors
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
        }

        // Check if photo with same filename already exists
        if (photoRepository.findByFilename(photo.getFilename()).isPresent()) {
            Map<String, String> error = new HashMap<>();
            error.put("filename", "A photo with this filename already exists");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }

        try {
            Photo savedPhoto = photoRepository.save(photo);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedPhoto);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to save photo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}