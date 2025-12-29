package com.photoserve.photo_api.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("null")
class PhotoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validate_WithValidFilename_ShouldHaveNoViolations() {
        // Arrange
        Photo photo = new Photo("test.jpg");

        // Act
        Set<ConstraintViolation<Photo>> violations = validator.validate(photo);

        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    void validate_WithValidJpegFilename_ShouldHaveNoViolations() {
        // Arrange
        Photo photo = new Photo("image.jpeg");

        // Act
        Set<ConstraintViolation<Photo>> violations = validator.validate(photo);

        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    void validate_WithValidPngFilename_ShouldHaveNoViolations() {
        // Arrange
        Photo photo = new Photo("picture.png");

        // Act
        Set<ConstraintViolation<Photo>> violations = validator.validate(photo);

        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    void validate_WithValidGifFilename_ShouldHaveNoViolations() {
        // Arrange
        Photo photo = new Photo("animation.gif");

        // Act
        Set<ConstraintViolation<Photo>> violations = validator.validate(photo);

        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    void validate_WithValidWebpFilename_ShouldHaveNoViolations() {
        // Arrange
        Photo photo = new Photo("modern.webp");

        // Act
        Set<ConstraintViolation<Photo>> violations = validator.validate(photo);

        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    void validate_WithValidHeicFilename_ShouldHaveNoViolations() {
        // Arrange
        Photo photo = new Photo("iphone.heic");

        // Act
        Set<ConstraintViolation<Photo>> violations = validator.validate(photo);

        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    void validate_WithUppercaseExtension_ShouldHaveNoViolations() {
        // Arrange
        Photo photo = new Photo("photo.JPG");

        // Act
        Set<ConstraintViolation<Photo>> violations = validator.validate(photo);

        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    void validate_WithMixedCaseExtension_ShouldHaveNoViolations() {
        // Arrange
        Photo photo = new Photo("photo.JpG");

        // Act
        Set<ConstraintViolation<Photo>> violations = validator.validate(photo);

        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    void validate_WithUnderscoresAndHyphens_ShouldHaveNoViolations() {
        // Arrange
        Photo photo = new Photo("my-photo_2024.jpg");

        // Act
        Set<ConstraintViolation<Photo>> violations = validator.validate(photo);

        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    void validate_WithUUID_ShouldHaveNoViolations() {
        // Arrange
        Photo photo = new Photo("550e8400-e29b-41d4-a716-446655440000.jpg");

        // Act
        Set<ConstraintViolation<Photo>> violations = validator.validate(photo);

        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    void validate_WithBlankFilename_ShouldHaveViolation() {
        // Arrange
        Photo photo = new Photo("");

        // Act
        Set<ConstraintViolation<Photo>> violations = validator.validate(photo);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("Filename cannot be empty")));
    }

    @Test
    void validate_WithNullFilename_ShouldHaveViolation() {
        // Arrange
        Photo photo = new Photo(null);

        // Act
        Set<ConstraintViolation<Photo>> violations = validator.validate(photo);

        // Assert
        assertFalse(violations.isEmpty());
    }

    @Test
    void validate_WithInvalidExtension_ShouldHaveViolation() {
        // Arrange
        Photo photo = new Photo("document.pdf");

        // Act
        Set<ConstraintViolation<Photo>> violations = validator.validate(photo);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("Invalid filename format")));
    }

    @Test
    void validate_WithTextFile_ShouldHaveViolation() {
        // Arrange
        Photo photo = new Photo("file.txt");

        // Act
        Set<ConstraintViolation<Photo>> violations = validator.validate(photo);

        // Assert
        assertFalse(violations.isEmpty());
    }

    @Test
    void validate_WithNoExtension_ShouldHaveViolation() {
        // Arrange
        Photo photo = new Photo("filename");

        // Act
        Set<ConstraintViolation<Photo>> violations = validator.validate(photo);

        // Assert
        assertFalse(violations.isEmpty());
    }

    @Test
    void validate_WithSpaces_ShouldHaveViolation() {
        // Arrange
        Photo photo = new Photo("my photo.jpg");

        // Act
        Set<ConstraintViolation<Photo>> violations = validator.validate(photo);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("Invalid filename format")));
    }

    @Test
    void validate_WithSpecialCharacters_ShouldHaveViolation() {
        // Arrange
        Photo photo = new Photo("photo@#$.jpg");

        // Act
        Set<ConstraintViolation<Photo>> violations = validator.validate(photo);

        // Assert
        assertFalse(violations.isEmpty());
    }

    @Test
    void validate_WithPathTraversal_ShouldHaveViolation() {
        // Arrange
        Photo photo = new Photo("../evil.jpg");

        // Act
        Set<ConstraintViolation<Photo>> violations = validator.validate(photo);

        // Assert
        assertFalse(violations.isEmpty());
    }

    @Test
    void validate_WithPathSeparator_ShouldHaveViolation() {
        // Arrange
        Photo photo = new Photo("folder/photo.jpg");

        // Act
        Set<ConstraintViolation<Photo>> violations = validator.validate(photo);

        // Assert
        assertFalse(violations.isEmpty());
    }

    @Test
    void validate_WithTooLongFilename_ShouldHaveViolation() {
        // Arrange - 252 chars for name + 4 for .jpg = 256 total (exceeds max of 255)
        String longFilename = "a".repeat(252) + ".jpg";
        Photo photo = new Photo(longFilename);

        // Act
        Set<ConstraintViolation<Photo>> violations = validator.validate(photo);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("Filename must be between 1 and 255 characters")));
    }

    @Test
    void validate_WithMaxLengthFilename_ShouldHaveNoViolations() {
        // Arrange - 251 chars for name + 4 for .jpg = 255 total
        String maxLengthName = "a".repeat(251) + ".jpg";
        Photo photo = new Photo(maxLengthName);

        // Act
        Set<ConstraintViolation<Photo>> violations = validator.validate(photo);

        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    void validate_WithNumericFilename_ShouldHaveNoViolations() {
        // Arrange
        Photo photo = new Photo("12345.jpg");

        // Act
        Set<ConstraintViolation<Photo>> violations = validator.validate(photo);

        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    void validate_WithMultipleDots_ShouldHaveNoViolations() {
        // Arrange
        Photo photo = new Photo("file.backup.jpg");

        // Act
        Set<ConstraintViolation<Photo>> violations = validator.validate(photo);

        // Assert
        assertTrue(violations.isEmpty());
    }
}
