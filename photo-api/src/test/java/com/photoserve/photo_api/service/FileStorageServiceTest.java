package com.photoserve.photo_api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("null")
class FileStorageServiceTest {

    @TempDir
    Path tempDir;

    private FileStorageService fileStorageService;

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageService(tempDir.toString());
    }

    @Test
    void constructor_ShouldCreateUploadDirectory() {
        // Assert
        assertTrue(Files.exists(tempDir));
        assertTrue(Files.isDirectory(tempDir));
    }

    @Test
    void constructor_WithNonExistentDirectory_ShouldCreateDirectory() throws IOException {
        // Arrange
        Path newDir = tempDir.resolve("new-upload-dir");
        assertFalse(Files.exists(newDir));

        // Act
        new FileStorageService(newDir.toString());

        // Assert
        assertTrue(Files.exists(newDir));
        assertTrue(Files.isDirectory(newDir));
    }

    @Test
    void storeFile_WithValidFile_ShouldStoreWithUniqueFilename() throws IOException {
        // Arrange
        MultipartFile file = new MockMultipartFile(
            "file",
            "test.jpg",
            "image/jpeg",
            "test content".getBytes()
        );

        // Act
        String storedFilename = fileStorageService.storeFile(file);

        // Assert
        assertNotNull(storedFilename);
        assertTrue(storedFilename.endsWith(".jpg"));
        assertNotEquals("test.jpg", storedFilename); // Should be unique

        Path storedPath = tempDir.resolve(storedFilename);
        assertTrue(Files.exists(storedPath));
        assertEquals("test content", Files.readString(storedPath));
    }

    @Test
    void storeFile_WithPathTraversalAttempt_ShouldThrowException() {
        // Arrange
        MultipartFile file = new MockMultipartFile(
            "file",
            "../evil.jpg",
            "image/jpeg",
            "malicious content".getBytes()
        );

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            fileStorageService.storeFile(file);
        });
        assertTrue(exception.getMessage().contains("Invalid path sequence"));
    }

    @Test
    void storeFile_MultipleTimes_ShouldGenerateUniqueFilenames() throws IOException {
        // Arrange
        MultipartFile file1 = new MockMultipartFile("file", "test.jpg", "image/jpeg", "content1".getBytes());
        MultipartFile file2 = new MockMultipartFile("file", "test.jpg", "image/jpeg", "content2".getBytes());

        // Act
        String filename1 = fileStorageService.storeFile(file1);
        String filename2 = fileStorageService.storeFile(file2);

        // Assert
        assertNotEquals(filename1, filename2);
        assertTrue(Files.exists(tempDir.resolve(filename1)));
        assertTrue(Files.exists(tempDir.resolve(filename2)));
    }

    @Test
    void storeFile_WithFileWithoutExtension_ShouldStoreSuccessfully() throws IOException {
        // Arrange
        MultipartFile file = new MockMultipartFile("file", "testfile", "image/jpeg", "content".getBytes());

        // Act
        String storedFilename = fileStorageService.storeFile(file);

        // Assert
        assertNotNull(storedFilename);
        assertTrue(Files.exists(tempDir.resolve(storedFilename)));
    }

    @Test
    void storeFileWithOriginalName_WithValidFile_ShouldStoreWithOriginalFilename() throws IOException {
        // Arrange
        MultipartFile file = new MockMultipartFile(
            "file",
            "original.jpg",
            "image/jpeg",
            "test content".getBytes()
        );

        // Act
        String storedFilename = fileStorageService.storeFileWithOriginalName(file);

        // Assert
        assertEquals("original.jpg", storedFilename);

        Path storedPath = tempDir.resolve(storedFilename);
        assertTrue(Files.exists(storedPath));
        assertEquals("test content", Files.readString(storedPath));
    }

    @Test
    void storeFileWithOriginalName_WithPathTraversalAttempt_ShouldThrowException() {
        // Arrange
        MultipartFile file = new MockMultipartFile(
            "file",
            "../evil.jpg",
            "image/jpeg",
            "malicious content".getBytes()
        );

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            fileStorageService.storeFileWithOriginalName(file);
        });
        assertTrue(exception.getMessage().contains("Invalid path sequence"));
    }

    @Test
    void storeFileWithOriginalName_WithExistingFile_ShouldReplaceFile() throws IOException {
        // Arrange
        MultipartFile file1 = new MockMultipartFile("file", "test.jpg", "image/jpeg", "original content".getBytes());
        MultipartFile file2 = new MockMultipartFile("file", "test.jpg", "image/jpeg", "new content".getBytes());

        // Act
        String filename1 = fileStorageService.storeFileWithOriginalName(file1);
        String filename2 = fileStorageService.storeFileWithOriginalName(file2);

        // Assert
        assertEquals(filename1, filename2);
        Path storedPath = tempDir.resolve(filename1);
        assertEquals("new content", Files.readString(storedPath));
    }

    @Test
    void getFileStorageLocation_ShouldReturnCorrectPath() {
        // Act
        Path location = fileStorageService.getFileStorageLocation();

        // Assert
        assertEquals(tempDir.toAbsolutePath().normalize(), location);
    }

    @Test
    void deleteFile_WithExistingFile_ShouldDeleteFile() throws IOException {
        // Arrange
        Path testFile = tempDir.resolve("test.jpg");
        Files.writeString(testFile, "test content");
        assertTrue(Files.exists(testFile));

        // Act
        fileStorageService.deleteFile("test.jpg");

        // Assert
        assertFalse(Files.exists(testFile));
    }

    @Test
    void deleteFile_WithNonExistentFile_ShouldNotThrowException() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            fileStorageService.deleteFile("nonexistent.jpg");
        });
    }

    @Test
    void loadFile_WithExistingFile_ShouldReturnFilePath() throws IOException {
        // Arrange
        Path testFile = tempDir.resolve("test.jpg");
        Files.writeString(testFile, "test content");

        // Act
        Path loadedPath = fileStorageService.loadFile("test.jpg");

        // Assert
        assertNotNull(loadedPath);
        assertTrue(Files.exists(loadedPath));
        assertEquals("test content", Files.readString(loadedPath));
    }

    @Test
    void loadFile_WithNonExistentFile_ShouldThrowException() {
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            fileStorageService.loadFile("nonexistent.jpg");
        });
        assertTrue(exception.getMessage().contains("File not found"));
    }

    @Test
    void loadFile_ShouldReturnNormalizedPath() throws IOException {
        // Arrange
        Path testFile = tempDir.resolve("test.jpg");
        Files.writeString(testFile, "test content");

        // Act
        Path loadedPath = fileStorageService.loadFile("test.jpg");

        // Assert
        assertEquals(tempDir.resolve("test.jpg").normalize(), loadedPath);
    }
}
