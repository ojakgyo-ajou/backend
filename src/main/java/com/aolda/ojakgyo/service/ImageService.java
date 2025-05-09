package com.aolda.ojakgyo.service;

import com.aolda.ojakgyo.entity.Image;
import com.aolda.ojakgyo.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ImageService {

    private final ImageRepository imageRepository;

    @Value("${image.upload.path}")
    private String uploadPath;

    @Transactional
    public String saveImage(MultipartFile file, String imageName) throws IOException {
        // 이미지 이름 중복 체크
        if (imageRepository.existsByImageName(imageName)) {
            throw new IllegalArgumentException("이미 존재하는 이미지 이름입니다: " + imageName);
        }

        // 파일 확장자 추출
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null) {
            throw new IllegalArgumentException("파일 이름이 없습니다.");
        }
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        
        // 저장할 파일 경로 생성
        String fileName = imageName + fileExtension;
        Path filePath = Paths.get(uploadPath, fileName);

        // 디렉토리가 없으면 생성
        Files.createDirectories(filePath.getParent());

        // 파일 저장
        Files.copy(file.getInputStream(), filePath);

        // 이미지 정보 저장
        Image image = Image.builder()
                .imageName(imageName)
                .originalFileName(originalFileName)
                .filePath(filePath.toString())
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .build();

        imageRepository.save(image);
        log.info("이미지 저장 완료: {}", imageName);

        return imageName;
    }

    public byte[] getImage(String imageName) throws IOException {
        Image image = imageRepository.findByImageName(imageName)
                .orElseThrow(() -> {
                    log.error("이미지를 찾을 수 없습니다: {}", imageName);
                    return new IllegalArgumentException("이미지를 찾을 수 없습니다: " + imageName);
                });

        Path filePath = Paths.get(image.getFilePath());
        if (!Files.exists(filePath)) {
            log.error("이미지 파일이 존재하지 않습니다: {}", filePath);
            throw new IllegalArgumentException("이미지 파일이 존재하지 않습니다: " + imageName);
        }

        return Files.readAllBytes(filePath);
    }

    public String getImageContentType(String imageName) {
        Image image = imageRepository.findByImageName(imageName)
                .orElseThrow(() -> {
                    log.error("이미지를 찾을 수 없습니다: {}", imageName);
                    return new IllegalArgumentException("이미지를 찾을 수 없습니다: " + imageName);
                });
        return image.getFileType();
    }
} 