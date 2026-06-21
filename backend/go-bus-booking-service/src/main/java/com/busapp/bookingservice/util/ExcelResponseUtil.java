package com.busapp.bookingservice.util;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class ExcelResponseUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE;
    private static final String EXCEL_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    /**
     * Creates a ResponseEntity with proper headers for Excel file download
     *
     * @param excelData The Excel file as byte array
     * @param filename  The filename for the download
     * @return ResponseEntity configured for Excel file download
     */
    public static ResponseEntity<byte[]> createExcelResponse(byte[] excelData, String filename) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(EXCEL_CONTENT_TYPE))
                .contentLength(excelData.length)
                .body(excelData);
    }

    /**
     * Generates filename for booking report
     */
    public static String generateBookingReportFilename(LocalDate startDate, LocalDate endDate) {
        return String.format("%s_%s.xlsx",
                UUID.randomUUID(),
                LocalDate.now().format(DATE_FORMATTER));
    }

    /**
     * Generates filename for route revenue report
     */
    public static String generateRouteRevenueFilename(LocalDate startDate, LocalDate endDate) {
        return String.format("%s_%s.xlsx",
                UUID.randomUUID(),
                LocalDate.now().format(DATE_FORMATTER));
    }

    /**
     * Generates filename for popular routes report
     */
    public static String generatePopularRoutesFilename(LocalDate startDate, LocalDate endDate) {
        return String.format("%s_%s.xlsx",
                UUID.randomUUID(),
                LocalDate.now().format(DATE_FORMATTER));
    }

    /**
     * Generates filename for seat occupancy report
     */
    public static String generateSeatOccupancyFilename(Long scheduleId) {
        return String.format("%s_%s.xlsx",
                UUID.randomUUID(),
                LocalDate.now().format(DATE_FORMATTER));
    }

    /**
     * Generates filename for ticket sales report
     */
    public static String generateTicketSalesFilename(LocalDate startDate, LocalDate endDate) {
        return String.format("%s_%s.xlsx",
                UUID.randomUUID(),
                LocalDate.now().format(DATE_FORMATTER));
    }

    /**
     * Generic method to generate filename with date range
     */
    public static String generateFilename(String reportType, LocalDate startDate, LocalDate endDate) {
        return String.format("%s_%s.xlsx",
                UUID.randomUUID(),
                LocalDate.now().format(DATE_FORMATTER));
    }

    /**
     * Generic method to generate filename without date range
     */
    public static String generateFilename(String reportType) {
        return String.format("%s_%s.xlsx",
                UUID.randomUUID(),
                LocalDate.now().format(DATE_FORMATTER));
    }


    /**
     * Generates filename for revenue report
     */
    public static String generateRevenueReportFilename(LocalDate startDate, LocalDate endDate, String period) {
        return String.format("%s_%s.xlsx",
                UUID.randomUUID(),
                LocalDate.now().format(DATE_FORMATTER));
    }

    /**
     * Generates filename for payment method report
     */
    public static String generatePaymentMethodReportFilename(LocalDate startDate, LocalDate endDate) {
        return String.format("%s_%s.xlsx",
                UUID.randomUUID(),
                LocalDate.now().format(DATE_FORMATTER));
    }

    /**
     * Generates filename for refund cancellation report
     */
    public static String generateRefundCancellationReportFilename(LocalDate startDate, LocalDate endDate, String period) {
        return String.format("%s_%s.xlsx",
                UUID.randomUUID(),
                LocalDate.now().format(DATE_FORMATTER));
    }

    /**
     * Generates filename for promo code usage report
     */
    public static String generatePromoCodeUsageReportFilename(LocalDate startDate, LocalDate endDate) {
        return String.format("%s_%s.xlsx",
                UUID.randomUUID(),
                LocalDate.now().format(DATE_FORMATTER));
    }


}