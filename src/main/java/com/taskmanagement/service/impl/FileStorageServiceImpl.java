package com.taskmanagement.service.impl;

import com.taskmanagement.exception.BusinessException;
import com.taskmanagement.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    @Override
    public String storeFile(MultipartFile file, Long userId) throws IOException {
        validateFile(file);

        String fileName = generateFileName(file.getOriginalFilename(), userId);
        Path targetLocation = Paths.get(uploadDir).resolve(fileName);

        Files.createDirectories(targetLocation.getParent());
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        log.info("File stored: {} for user: {}", fileName, userId);
        return fileName;
    }

    @Override
    public void deleteFile(String fileUrl) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(fileUrl).normalize();
            Files.deleteIfExists(filePath);
            log.info("File deleted: {}", fileUrl);
        } catch (IOException e) {
            log.error("Failed to delete file: {}", fileUrl, e);
            throw new BusinessException("Failed to delete file: " + fileUrl);
        }
    }

    @Override
    public byte[] getFile(String fileUrl) throws IOException {
        Path filePath = Paths.get(uploadDir).resolve(fileUrl).normalize();
        return Files.readAllBytes(filePath);
    }

    @Override
    public String getFileContentType(String fileUrl) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(fileUrl).normalize();
            return Files.probeContentType(filePath);
        } catch (IOException e) {
            log.warn("Could not determine content type for file: {}", fileUrl);
            return "application/octet-stream";
        }
    }

    @Override
    public Long getFileSize(String fileUrl) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(fileUrl).normalize();
            return Files.size(filePath);
        } catch (IOException e) {
            log.warn("Could not get file size for: {}", fileUrl);
            return 0L;
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("File size exceeds maximum limit of 10MB");
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType != null && !isAllowedContentType(contentType)) {
            throw new BusinessException("File type not allowed: " + contentType);
        }
    }

    private boolean isAllowedContentType(String contentType) {
        return contentType.startsWith("image/") ||
                contentType.equals("application/pdf") ||
                contentType.equals("application/msword") ||
                contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
                contentType.startsWith("text/");
    }

    private String generateFileName(String originalFileName, Long userId) {
        String fileExtension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        String uuid = UUID.randomUUID().toString();
        return String.format("user_%d/%s%s", userId, uuid, fileExtension);
    }
}
