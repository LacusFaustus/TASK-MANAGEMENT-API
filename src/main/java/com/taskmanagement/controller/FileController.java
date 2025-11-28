package com.taskmanagement.controller;

import com.taskmanagement.dto.response.ApiResponse;
import com.taskmanagement.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Files", description = "API для работы с файлами")
public class FileController {

    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    @Operation(summary = "Загрузка файла")
    public ResponseEntity<ApiResponse<String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal Long userId) throws IOException {

        String fileUrl = fileStorageService.storeFile(file, userId);
        return ResponseEntity.ok(ApiResponse.success("File uploaded successfully", fileUrl));
    }

    @DeleteMapping
    @Operation(summary = "Удаление файла")
    public ResponseEntity<ApiResponse<Void>> deleteFile(
            @RequestParam String fileUrl,
            @AuthenticationPrincipal Long userId) {

        fileStorageService.deleteFile(fileUrl);
        return ResponseEntity.ok(ApiResponse.success("File deleted successfully"));
    }
}
