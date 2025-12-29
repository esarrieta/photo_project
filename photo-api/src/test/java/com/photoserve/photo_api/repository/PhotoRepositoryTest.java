package com.photoserve.photo_api.repository;

import com.photoserve.photo_api.model.Photo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@SuppressWarnings("null")
class PhotoRepositoryTest {

    @Autowired
    private PhotoRepository photoRepository;

    @Test
    void save_WithValidPhoto_ShouldPersistPhoto() {
        // Arrange
        Photo photo = new Photo("test.jpg");

        // Act
        Photo savedPhoto = photoRepository.save(photo);

        // Assert
        assertNotNull(savedPhoto);
        assertNotNull(savedPhoto.getId());
        assertEquals("test.jpg", savedPhoto.getFilename());
    }

    @Test
    void findById_WhenPhotoExists_ShouldReturnPhoto() {
        // Arrange
        Photo photo = new Photo("test.jpg");
        Photo savedPhoto = photoRepository.save(photo);

        // Act
        Optional<Photo> foundPhoto = photoRepository.findById(savedPhoto.getId());

        // Assert
        assertTrue(foundPhoto.isPresent());
        assertEquals("test.jpg", foundPhoto.get().getFilename());
    }

    @Test
    void findById_WhenPhotoDoesNotExist_ShouldReturnEmpty() {
        // Act
        Optional<Photo> foundPhoto = photoRepository.findById(999L);

        // Assert
        assertFalse(foundPhoto.isPresent());
    }

    @Test
    void findByFilename_WhenPhotoExists_ShouldReturnPhoto() {
        // Arrange
        Photo photo = new Photo("test.jpg");
        photoRepository.save(photo);

        // Act
        Optional<Photo> foundPhoto = photoRepository.findByFilename("test.jpg");

        // Assert
        assertTrue(foundPhoto.isPresent());
        assertEquals("test.jpg", foundPhoto.get().getFilename());
    }

    @Test
    void findByFilename_WhenPhotoDoesNotExist_ShouldReturnEmpty() {
        // Act
        Optional<Photo> foundPhoto = photoRepository.findByFilename("nonexistent.jpg");

        // Assert
        assertFalse(foundPhoto.isPresent());
    }

    @Test
    void findAll_ShouldReturnAllPhotos() {
        // Arrange
        photoRepository.save(new Photo("photo1.jpg"));
        photoRepository.save(new Photo("photo2.png"));
        photoRepository.save(new Photo("photo3.gif"));

        // Act
        List<Photo> photos = photoRepository.findAll();

        // Assert
        assertEquals(3, photos.size());
    }

    @Test
    void findAll_WhenNoPhotos_ShouldReturnEmptyList() {
        // Act
        List<Photo> photos = photoRepository.findAll();

        // Assert
        assertTrue(photos.isEmpty());
    }

    @Test
    void delete_ShouldRemovePhoto() {
        // Arrange
        Photo photo = new Photo("test.jpg");
        Photo savedPhoto = photoRepository.save(photo);
        Long photoId = savedPhoto.getId();

        // Act
        photoRepository.delete(savedPhoto);

        // Assert
        Optional<Photo> deletedPhoto = photoRepository.findById(photoId);
        assertFalse(deletedPhoto.isPresent());
    }

    @Test
    void save_WithDuplicateFilename_ShouldThrowException() {
        // Arrange
        Photo photo1 = new Photo("duplicate.jpg");
        Photo photo2 = new Photo("duplicate.jpg");

        // Act
        photoRepository.save(photo1);

        // Assert
        assertThrows(DataIntegrityViolationException.class, () -> {
            photoRepository.save(photo2);
            photoRepository.flush(); // Force the constraint check
        });
    }

    @Test
    void save_WithMultiplePhotos_ShouldPersistAll() {
        // Arrange
        Photo photo1 = new Photo("photo1.jpg");
        Photo photo2 = new Photo("photo2.png");
        Photo photo3 = new Photo("photo3.gif");

        // Act
        photoRepository.save(photo1);
        photoRepository.save(photo2);
        photoRepository.save(photo3);

        // Assert
        List<Photo> photos = photoRepository.findAll();
        assertEquals(3, photos.size());
    }

    @Test
    void update_ShouldModifyExistingPhoto() {
        // Arrange
        Photo photo = new Photo("original.jpg");
        Photo savedPhoto = photoRepository.save(photo);

        // Act
        savedPhoto.setFilename("updated.jpg");
        Photo updatedPhoto = photoRepository.save(savedPhoto);

        // Assert
        assertEquals(savedPhoto.getId(), updatedPhoto.getId());
        assertEquals("updated.jpg", updatedPhoto.getFilename());

        Optional<Photo> foundPhoto = photoRepository.findById(savedPhoto.getId());
        assertTrue(foundPhoto.isPresent());
        assertEquals("updated.jpg", foundPhoto.get().getFilename());
    }

    @Test
    void deleteById_ShouldRemovePhoto() {
        // Arrange
        Photo photo = new Photo("test.jpg");
        Photo savedPhoto = photoRepository.save(photo);
        Long photoId = savedPhoto.getId();

        // Act
        photoRepository.deleteById(photoId);

        // Assert
        Optional<Photo> deletedPhoto = photoRepository.findById(photoId);
        assertFalse(deletedPhoto.isPresent());
    }

    @Test
    void count_ShouldReturnCorrectCount() {
        // Arrange
        photoRepository.save(new Photo("photo1.jpg"));
        photoRepository.save(new Photo("photo2.png"));

        // Act
        long count = photoRepository.count();

        // Assert
        assertEquals(2, count);
    }

    @Test
    void existsById_WhenPhotoExists_ShouldReturnTrue() {
        // Arrange
        Photo photo = new Photo("test.jpg");
        Photo savedPhoto = photoRepository.save(photo);

        // Act
        boolean exists = photoRepository.existsById(savedPhoto.getId());

        // Assert
        assertTrue(exists);
    }

    @Test
    void existsById_WhenPhotoDoesNotExist_ShouldReturnFalse() {
        // Act
        boolean exists = photoRepository.existsById(999L);

        // Assert
        assertFalse(exists);
    }
}
