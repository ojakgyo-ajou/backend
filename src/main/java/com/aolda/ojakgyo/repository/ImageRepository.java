package com.aolda.ojakgyo.repository;

import com.aolda.ojakgyo.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Long> {
    Optional<Image> findByImageName(String imageName);
    boolean existsByImageName(String imageName);
} 