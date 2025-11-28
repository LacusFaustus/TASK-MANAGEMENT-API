package com.taskmanagement.service.impl;

import com.taskmanagement.entity.Task;
import com.taskmanagement.entity.enums.ExportFormat;
import com.taskmanagement.exception.UnauthorizedException;
import com.taskmanagement.repository.TaskRepository;
import com.taskmanagement.repository.WorkspaceMemberRepository;
import com.taskmanagement.service.ExportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExportServiceImpl implements ExportService {

    private final TaskRepository taskRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void exportWorkspaceTasks(Long workspaceId, ExportFormat format, OutputStream outputStream, Long userId) {
        validateWorkspaceAccess(workspaceId, userId);

        List<Task> tasks = taskRepository.findByWorkspaceId(workspaceId, Pageable.unpaged()).getContent();

        try {
            switch (format) {
                case JSON:
                    exportToJson(tasks, outputStream);
                    break;
                case CSV:
                    exportToCsv(tasks, outputStream);
                    break;
                case EXCEL:
                    exportToExcel(tasks, outputStream);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported export format: " + format);
            }
        } catch (IOException e) {
            log.error("Export failed for workspace {}: {}", workspaceId, e.getMessage());
            throw new RuntimeException("Export failed", e);
        }
    }

    @Override
    public void exportUserTasks(Long userId, ExportFormat format, OutputStream outputStream) {
        // Реализация экспорта задач пользователя
    }

    @Override
    public void exportWorkspaceAnalytics(Long workspaceId, ExportFormat format, OutputStream outputStream, Long userId) {
        // Реализация экспорта аналитики
    }

    private void exportToJson(List<Task> tasks, OutputStream outputStream) throws IOException {
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.writeValue(outputStream, tasks);
    }

    private void exportToCsv(List<Task> tasks, OutputStream outputStream) throws IOException {
        CsvMapper csvMapper = new CsvMapper();
        CsvSchema schema = csvMapper.schemaFor(Task.class)
                .withHeader()
                .withColumnSeparator(',');

        csvMapper.writer(schema).writeValue(outputStream, tasks);
    }

    private void exportToExcel(List<Task> tasks, OutputStream outputStream) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Tasks");

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Title", "Description", "Status", "Priority", "Assignee", "Due Date", "Created At"};

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Create data rows
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            int rowNum = 1;

            for (Task task : tasks) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(task.getId());
                row.createCell(1).setCellValue(task.getTitle());
                row.createCell(2).setCellValue(task.getDescription());
                row.createCell(3).setCellValue(task.getStatus().name());
                row.createCell(4).setCellValue(task.getPriority().name());
                row.createCell(5).setCellValue(task.getAssignee() != null ?
                        task.getAssignee().getFirstName() + " " + task.getAssignee().getLastName() : "");
                row.createCell(6).setCellValue(task.getDueDate() != null ?
                        task.getDueDate().format(formatter) : "");
                row.createCell(7).setCellValue(task.getCreatedAt().format(formatter));
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
        }
    }

    private void validateWorkspaceAccess(Long workspaceId, Long userId) {
        if (!workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId)) {
            throw new UnauthorizedException("Access denied to workspace");
        }
    }
}
