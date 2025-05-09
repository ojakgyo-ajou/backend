package com.aolda.ojakgyo.controller;

import com.aolda.ojakgyo.dto.ImageResponseDto;
import com.aolda.ojakgyo.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @PostMapping
    public ResponseEntity<ImageResponseDto> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("imageName") String imageName) throws IOException {
        String savedImageName = imageService.saveImage(file, imageName);
        
        String imageUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/images/{imageName}")
                .buildAndExpand(savedImageName)
                .toUriString();

        ImageResponseDto response = ImageResponseDto.builder()
                .imageName(savedImageName)
                .imageUrl(imageUrl)
                .originalFileName(file.getOriginalFilename())
                .fileSize(file.getSize())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{imageName}")
    public ResponseEntity<byte[]> getImage(@PathVariable String imageName) throws IOException {
        log.info("이미지 조회 요청: {}", imageName);
        try {
            byte[] imageBytes = imageService.getImage(imageName);
            String contentType = imageService.getImageContentType(imageName);

            log.info("이미지 조회 성공: {}, ContentType: {}, Size: {}", imageName, contentType, imageBytes.length);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentLength(imageBytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(imageBytes);
        } catch (IllegalArgumentException e) {
            log.error("이미지 조회 실패: {}, 에러: {}", imageName, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
} 