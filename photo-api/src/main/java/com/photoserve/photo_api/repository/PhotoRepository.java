package com.photoserve.photo_api.repository;

import com.photoserve.photo_api.model.Photo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {
    Optional<Photo> findByFilename(String filename);
}
