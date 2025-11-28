package com.taskmanagement.controller;

import com.taskmanagement.entity.enums.ExportFormat;
import com.taskmanagement.service.ExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/api/v1/export")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Export", description = "API для экспорта данных")
public class ExportController {

    private final ExportService exportService;

    @GetMapping("/workspaces/{workspaceId}/tasks")
    @Operation(summary = "Экспорт задач рабочего пространства")
    public void exportWorkspaceTasks(
            @PathVariable Long workspaceId,
            @RequestParam ExportFormat format,
            HttpServletResponse response,
            @AuthenticationPrincipal Long userId) throws IOException {

        String fileName = String.format("workspace_%d_tasks.%s", workspaceId, format.name().toLowerCase());
        String contentType = getContentType(format);

        response.setContentType(contentType);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");

        exportService.exportWorkspaceTasks(workspaceId, format, response.getOutputStream(), userId);
    }

    @GetMapping("/user/tasks")
    @Operation(summary = "Экспорт задач пользователя")
    public void exportUserTasks(
            @RequestParam ExportFormat format,
            HttpServletResponse response,
            @AuthenticationPrincipal Long userId) throws IOException {

        String fileName = String.format("my_tasks.%s", format.name().toLowerCase());
        String contentType = getContentType(format);

        response.setContentType(contentType);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");

        exportService.exportUserTasks(userId, format, response.getOutputStream());
    }

    private String getContentType(ExportFormat format) {
        switch (format) {
            case JSON:
                return MediaType.APPLICATION_JSON_VALUE;
            case CSV:
                return "text/csv";
            case EXCEL:
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            default:
                return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
    }
}
