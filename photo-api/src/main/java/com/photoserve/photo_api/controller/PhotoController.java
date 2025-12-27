package com.photoserve.photo_api.controller;

import com.photoserve.photo_api.model.Photo;
import com.photoserve.photo_api.repository.PhotoRepository;
import com.photoserve.photo_api.service.FileStorageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/photos")
public class PhotoController {

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private FileStorageService fileStorageService;

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
    @DeleteMapping("/id/{id}")
    public ResponseEntity<?> deletePhotoById(@PathVariable Long id) {
        return photoRepository.findById(id).map(photo -> {
            photoRepository.delete(photo);
            return ResponseEntity.ok("Photo with ID " + id + " deleted successfully.");
        }).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body("Photo with ID " + id + " not found."));
    }
    @DeleteMapping("/file/{filename}")
    public ResponseEntity<?> deletePhotoByFilename(@PathVariable String filename) {
        return photoRepository.findByFilename(filename).map(photo -> {
            photoRepository.delete(photo);
            return ResponseEntity.ok("Photo with filename '" + filename + "' deleted successfully.");
        }).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body("Photo with filename '" + filename + "' not found."));
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadPhoto(@RequestParam("file") MultipartFile file,
                                        @RequestParam(value = "useOriginalName", defaultValue = "false") boolean useOriginalName) {
        try {
            // Validate file
            if (file.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Please select a file to upload");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            // Validate file type
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.matches(".*\\.(jpg|jpeg|png|gif|webp|heic|HEIC|JPG|JPEG|PNG|GIF|WEBP)$")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid file type. Only image files are allowed");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            // Store the file
            String storedFilename;
            if (useOriginalName) {
                storedFilename = fileStorageService.storeFileWithOriginalName(file);
            } else {
                storedFilename = fileStorageService.storeFile(file);
            }

            // Check if photo already exists in database
            if (photoRepository.findByFilename(storedFilename).isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("filename", "A photo with this filename already exists in database");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
            }

            // Save photo metadata to database
            Photo photo = new Photo(storedFilename);
            Photo savedPhoto = photoRepository.save(photo);

            // Create response with file info
            Map<String, Object> response = new HashMap<>();
            response.put("photo", savedPhoto);
            response.put("originalFilename", originalFilename);
            response.put("storedFilename", storedFilename);
            response.put("fileSize", file.getSize());
            response.put("uploadPath", fileStorageService.getFileStorageLocation().toString());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to upload file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> downloadPhoto(@PathVariable String filename) {
        try {
            // Load file from storage
            Path filePath = fileStorageService.loadFile(filename);
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            // Determine content type based on file extension
            String contentType = "application/octet-stream";
            String lowerFilename = filename.toLowerCase();
            if (lowerFilename.endsWith(".jpg") || lowerFilename.endsWith(".jpeg")) {
                contentType = "image/jpeg";
            } else if (lowerFilename.endsWith(".png")) {
                contentType = "image/png";
            } else if (lowerFilename.endsWith(".gif")) {
                contentType = "image/gif";
            } else if (lowerFilename.endsWith(".webp")) {
                contentType = "image/webp";
            } else if (lowerFilename.endsWith(".heic")) {
                contentType = "image/heic";
            }

            // Return file with appropriate headers
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);

        } catch (MalformedURLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}