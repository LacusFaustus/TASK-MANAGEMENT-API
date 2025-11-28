package com.taskmanagement.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileStorageService {
    String storeFile(MultipartFile file, Long userId) throws IOException;
    void deleteFile(String fileUrl);
    byte[] getFile(String fileUrl) throws IOException;
    String getFileContentType(String fileUrl);
    Long getFileSize(String fileUrl);
}
