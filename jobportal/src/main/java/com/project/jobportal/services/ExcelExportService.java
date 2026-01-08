package com.project.jobportal.services;

import com.project.jobportal.dto.RecruiterJobsDto;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ExcelExportService {

    public ByteArrayInputStream exportRecruiterJobs(List<RecruiterJobsDto> jobs) {
        // creating an excel workbook
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("My vacancies");

            // creating the header header
            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Job title", "Company", "Location", "Number of candidates"};

            // header style
            CellStyle headerCellStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerCellStyle.setFont(headerFont);
            headerCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerCellStyle);
            }

            // filling data from the RecruiterJobsDto
            int rowIdx = 1;
            for (RecruiterJobsDto job : jobs) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(job.getJobPostId());
                row.createCell(1).setCellValue(job.getJobTitle());
                row.createCell(2).setCellValue(job.getJobCompanyId().getName());
                row.createCell(3).setCellValue(job.getJobLocationId().getCity() + ", " + job.getJobLocationId().getCountry());
                row.createCell(4).setCellValue(job.getTotalCandidates());
            }

            // auto column width change
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("Ошибка при генерации Excel отчета: " + e.getMessage());
        }
    }
}