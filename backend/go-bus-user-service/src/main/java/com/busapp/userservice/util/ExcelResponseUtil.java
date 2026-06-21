package com.busapp.userservice.util;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class ExcelResponseUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE;
    private static final String EXCEL_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    public static ResponseEntity<byte[]> createExcelResponse(byte[] excelData, String filename) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(EXCEL_CONTENT_TYPE))
                .contentLength(excelData.length)
                .body(excelData);
    }

    public static String generateActiveCustomerFilename(LocalDate startDate, LocalDate endDate) {
        return String.format("%s_%s.xlsx",
                UUID.randomUUID(),
                LocalDate.now().format(DATE_FORMATTER));
    }

    public static String generateFrequentTravelerFilename(LocalDate startDate, LocalDate endDate) {
        return String.format("%s_%s.xlsx",
                UUID.randomUUID(),
                LocalDate.now().format(DATE_FORMATTER));
    }

    public static String generateCustomerDemographicsFilename(LocalDate startDate, LocalDate endDate) {
        return String.format("%s_%s.xlsx",
                UUID.randomUUID(),
                LocalDate.now().format(DATE_FORMATTER));
    }

    public static String generateBookingPatternsByDayFilename(LocalDate startDate, LocalDate endDate) {
        return String.format("%s_%s.xlsx",
                UUID.randomUUID(),
                LocalDate.now().format(DATE_FORMATTER));
    }

    public static String generateBookingPatternsByHourFilename(LocalDate startDate, LocalDate endDate) {
        return String.format("%s_%s.xlsx",
                UUID.randomUUID(),
                LocalDate.now().format(DATE_FORMATTER));
    }
}
