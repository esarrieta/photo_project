package com.photoserve.photo_api.controller;

import com.photoserve.photo_api.model.Photo;
import com.photoserve.photo_api.repository.PhotoRepository;
import com.photoserve.photo_api.service.FileStorageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PhotoController.class)
@SuppressWarnings("null")
class PhotoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PhotoRepository photoRepository;

    @MockitoBean
    private FileStorageService fileStorageService;

    @Test
    void getAllPhotos_ShouldReturnAllPhotos() throws Exception {
        // Arrange
        List<Photo> photos = Arrays.asList(
            new Photo(1L, "photo1.jpg"),
            new Photo(2L, "photo2.png")
        );
        when(photoRepository.findAll()).thenReturn(photos);

        // Act & Assert
        mockMvc.perform(get("/photos"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].filename").value("photo1.jpg"))
            .andExpect(jsonPath("$[1].id").value(2))
            .andExpect(jsonPath("$[1].filename").value("photo2.png"));

        verify(photoRepository, times(1)).findAll();
    }

    @Test
    void getPhotoById_WhenPhotoExists_ShouldReturnPhoto() throws Exception {
        // Arrange
        Photo photo = new Photo(1L, "test.jpg");
        when(photoRepository.findById(1L)).thenReturn(Optional.of(photo));

        // Act & Assert
        mockMvc.perform(get("/photos/id/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.filename").value("test.jpg"));

        verify(photoRepository, times(1)).findById(1L);
    }

    @Test
    void getPhotoById_WhenPhotoDoesNotExist_ShouldReturn404() throws Exception {
        // Arrange
        when(photoRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/photos/id/1"))
            .andExpect(status().isNotFound())
            .andExpect(content().string("Photo with ID 1 not found."));

        verify(photoRepository, times(1)).findById(1L);
    }

    @Test
    void getPhotoByFilename_WhenPhotoExists_ShouldReturnPhoto() throws Exception {
        // Arrange
        Photo photo = new Photo(1L, "test.jpg");
        when(photoRepository.findByFilename("test.jpg")).thenReturn(Optional.of(photo));

        // Act & Assert
        mockMvc.perform(get("/photos/file/test.jpg"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.filename").value("test.jpg"));

        verify(photoRepository, times(1)).findByFilename("test.jpg");
    }

    @Test
    void getPhotoByFilename_WhenPhotoDoesNotExist_ShouldReturn404() throws Exception {
        // Arrange
        when(photoRepository.findByFilename("test.jpg")).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/photos/file/test.jpg"))
            .andExpect(status().isNotFound())
            .andExpect(content().string("Photo with filename 'test.jpg' not found in database."));

        verify(photoRepository, times(1)).findByFilename("test.jpg");
    }

    @Test
    void createPhoto_WithValidData_ShouldCreatePhoto() throws Exception {
        // Arrange
        Photo photo = new Photo("test.jpg");
        Photo savedPhoto = new Photo(1L, "test.jpg");

        when(photoRepository.findByFilename("test.jpg")).thenReturn(Optional.empty());
        when(photoRepository.save(any(Photo.class))).thenReturn(savedPhoto);

        // Act & Assert
        mockMvc.perform(post("/photos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(photo)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.filename").value("test.jpg"));

        verify(photoRepository, times(1)).findByFilename("test.jpg");
        verify(photoRepository, times(1)).save(any(Photo.class));
    }

    @Test
    void createPhoto_WithInvalidFilename_ShouldReturnBadRequest() throws Exception {
        // Arrange
        Photo photo = new Photo("invalid_file.txt");

        // Act & Assert
        mockMvc.perform(post("/photos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(photo)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.filename").exists());

        verify(photoRepository, never()).save(any(Photo.class));
    }

    @Test
    void createPhoto_WithBlankFilename_ShouldReturnBadRequest() throws Exception {
        // Arrange
        Photo photo = new Photo("");

        // Act & Assert
        mockMvc.perform(post("/photos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(photo)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.filename").exists());

        verify(photoRepository, never()).save(any(Photo.class));
    }

    @Test
    void createPhoto_WithDuplicateFilename_ShouldReturnConflict() throws Exception {
        // Arrange
        Photo photo = new Photo("test.jpg");
        Photo existingPhoto = new Photo(1L, "test.jpg");

        when(photoRepository.findByFilename("test.jpg")).thenReturn(Optional.of(existingPhoto));

        // Act & Assert
        mockMvc.perform(post("/photos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(photo)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.filename").value("A photo with this filename already exists"));

        verify(photoRepository, times(1)).findByFilename("test.jpg");
        verify(photoRepository, never()).save(any(Photo.class));
    }

    @Test
    void deletePhotoById_WhenPhotoExists_ShouldDeletePhoto() throws Exception {
        // Arrange
        Photo photo = new Photo(1L, "test.jpg");
        when(photoRepository.findById(1L)).thenReturn(Optional.of(photo));
        doNothing().when(photoRepository).delete(photo);

        // Act & Assert
        mockMvc.perform(delete("/photos/id/1"))
            .andExpect(status().isOk())
            .andExpect(content().string("Photo with ID 1 deleted successfully."));

        verify(photoRepository, times(1)).findById(1L);
        verify(photoRepository, times(1)).delete(photo);
    }

    @Test
    void deletePhotoById_WhenPhotoDoesNotExist_ShouldReturn404() throws Exception {
        // Arrange
        when(photoRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(delete("/photos/id/1"))
            .andExpect(status().isNotFound())
            .andExpect(content().string("Photo with ID 1 not found."));

        verify(photoRepository, times(1)).findById(1L);
        verify(photoRepository, never()).delete(any(Photo.class));
    }

    @Test
    void deletePhotoByFilename_WhenPhotoExists_ShouldDeletePhoto() throws Exception {
        // Arrange
        Photo photo = new Photo(1L, "test.jpg");
        when(photoRepository.findByFilename("test.jpg")).thenReturn(Optional.of(photo));
        doNothing().when(photoRepository).delete(photo);

        // Act & Assert
        mockMvc.perform(delete("/photos/file/test.jpg"))
            .andExpect(status().isOk())
            .andExpect(content().string("Photo with filename 'test.jpg' deleted successfully."));

        verify(photoRepository, times(1)).findByFilename("test.jpg");
        verify(photoRepository, times(1)).delete(photo);
    }

    @Test
    void deletePhotoByFilename_WhenPhotoDoesNotExist_ShouldReturn404() throws Exception {
        // Arrange
        when(photoRepository.findByFilename("test.jpg")).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(delete("/photos/file/test.jpg"))
            .andExpect(status().isNotFound())
            .andExpect(content().string("Photo with filename 'test.jpg' not found."));

        verify(photoRepository, times(1)).findByFilename("test.jpg");
        verify(photoRepository, never()).delete(any(Photo.class));
    }

    @Test
    void uploadPhoto_WithValidFile_ShouldUploadSuccessfully() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "test image content".getBytes()
        );

        String storedFilename = "uuid-test.jpg";
        Photo savedPhoto = new Photo(1L, storedFilename);

        when(fileStorageService.storeFile(any())).thenReturn(storedFilename);
        when(fileStorageService.getFileStorageLocation()).thenReturn(Paths.get("/uploads"));
        when(photoRepository.findByFilename(storedFilename)).thenReturn(Optional.empty());
        when(photoRepository.save(any(Photo.class))).thenReturn(savedPhoto);

        // Act & Assert
        mockMvc.perform(multipart("/photos/upload")
                .file(file))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.photo.id").value(1))
            .andExpect(jsonPath("$.photo.filename").value(storedFilename))
            .andExpect(jsonPath("$.originalFilename").value("test.jpg"))
            .andExpect(jsonPath("$.storedFilename").value(storedFilename))
            .andExpect(jsonPath("$.fileSize").value(file.getSize()));

        verify(fileStorageService, times(1)).storeFile(any());
        verify(photoRepository, times(1)).save(any(Photo.class));
    }

    @Test
    void uploadPhoto_WithOriginalName_ShouldUseOriginalFilename() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "test image content".getBytes()
        );

        Photo savedPhoto = new Photo(1L, "test.jpg");

        when(fileStorageService.storeFileWithOriginalName(any())).thenReturn("test.jpg");
        when(fileStorageService.getFileStorageLocation()).thenReturn(Paths.get("/uploads"));
        when(photoRepository.findByFilename("test.jpg")).thenReturn(Optional.empty());
        when(photoRepository.save(any(Photo.class))).thenReturn(savedPhoto);

        // Act & Assert
        mockMvc.perform(multipart("/photos/upload")
                .file(file)
                .param("useOriginalName", "true"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.photo.filename").value("test.jpg"))
            .andExpect(jsonPath("$.storedFilename").value("test.jpg"));

        verify(fileStorageService, times(1)).storeFileWithOriginalName(any());
        verify(photoRepository, times(1)).save(any(Photo.class));
    }

    @Test
    void uploadPhoto_WithEmptyFile_ShouldReturnBadRequest() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            new byte[0]
        );

        // Act & Assert
        mockMvc.perform(multipart("/photos/upload")
                .file(file))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Please select a file to upload"));

        verify(fileStorageService, never()).storeFile(any());
        verify(photoRepository, never()).save(any(Photo.class));
    }

    @Test
    void uploadPhoto_WithInvalidFileType_ShouldReturnBadRequest() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.txt",
            MediaType.TEXT_PLAIN_VALUE,
            "test content".getBytes()
        );

        // Act & Assert
        mockMvc.perform(multipart("/photos/upload")
                .file(file))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Invalid file type. Only image files are allowed"));

        verify(fileStorageService, never()).storeFile(any());
        verify(photoRepository, never()).save(any(Photo.class));
    }

    @Test
    void uploadPhoto_WhenFilenameAlreadyExistsInDatabase_ShouldReturnConflict() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "test image content".getBytes()
        );

        String storedFilename = "uuid-test.jpg";
        Photo existingPhoto = new Photo(1L, storedFilename);

        when(fileStorageService.storeFile(any())).thenReturn(storedFilename);
        when(photoRepository.findByFilename(storedFilename)).thenReturn(Optional.of(existingPhoto));

        // Act & Assert
        mockMvc.perform(multipart("/photos/upload")
                .file(file))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.filename").value("A photo with this filename already exists in database"));

        verify(fileStorageService, times(1)).storeFile(any());
        verify(photoRepository, never()).save(any(Photo.class));
    }

    @Test
    void downloadPhoto_WhenFileExists_ShouldReturnFile() throws Exception {
        // Arrange - Create a temporary file
        Path tempFile = java.nio.file.Files.createTempFile("test", ".jpg");
        java.nio.file.Files.write(tempFile, "fake image content".getBytes());

        try {
            when(fileStorageService.loadFile("test.jpg")).thenReturn(tempFile);

            // Act & Assert
            mockMvc.perform(get("/photos/download/test.jpg"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "inline; filename=\"test.jpg\""));

            verify(fileStorageService, times(1)).loadFile("test.jpg");
        } finally {
            // Cleanup
            java.nio.file.Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void downloadPhoto_WithDifferentImageTypes_ShouldSetCorrectContentType() throws Exception {
        // Test JPEG
        Path jpegPath = java.nio.file.Files.createTempFile("test", ".jpg");
        java.nio.file.Files.write(jpegPath, "fake jpeg".getBytes());

        try {
            when(fileStorageService.loadFile("test.jpg")).thenReturn(jpegPath);
            mockMvc.perform(get("/photos/download/test.jpg"))
                .andExpect(status().isOk());
        } finally {
            java.nio.file.Files.deleteIfExists(jpegPath);
        }

        // Test PNG
        Path pngPath = java.nio.file.Files.createTempFile("test", ".png");
        java.nio.file.Files.write(pngPath, "fake png".getBytes());

        try {
            when(fileStorageService.loadFile("test.png")).thenReturn(pngPath);
            mockMvc.perform(get("/photos/download/test.png"))
                .andExpect(status().isOk());
        } finally {
            java.nio.file.Files.deleteIfExists(pngPath);
        }

        // Test GIF
        Path gifPath = java.nio.file.Files.createTempFile("test", ".gif");
        java.nio.file.Files.write(gifPath, "fake gif".getBytes());

        try {
            when(fileStorageService.loadFile("test.gif")).thenReturn(gifPath);
            mockMvc.perform(get("/photos/download/test.gif"))
                .andExpect(status().isOk());
        } finally {
            java.nio.file.Files.deleteIfExists(gifPath);
        }
    }

    @Test
    void downloadPhoto_WhenFileDoesNotExist_ShouldReturn404() throws Exception {
        // Arrange
        when(fileStorageService.loadFile("test.jpg")).thenThrow(new RuntimeException("File not found"));

        // Act & Assert
        mockMvc.perform(get("/photos/download/test.jpg"))
            .andExpect(status().isNotFound());

        verify(fileStorageService, times(1)).loadFile("test.jpg");
    }
}
