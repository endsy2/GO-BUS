package com.busapp.userservice.util;

import com.busapp.userservice.dto.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class ExcelGeneratorUtil {

    public static byte[] generateActiveCustomerExcel(List<ActiveCustomerReportResponse> reports) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Active Customers");
            
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            
            int rowNum = 0;
            
            rowNum = createTitle(sheet, rowNum, "Active Customer Report", workbook);
            rowNum++;
            
            String[] headers = {"User ID", "Username", "Full Name", "Email", "Phone", 
                               "Total Bookings", "Total Spent", "Last Booking Date"};
            rowNum = createHeaderRow(sheet, rowNum, headers, headerStyle);
            
            for (ActiveCustomerReportResponse report : reports) {
                Row dataRow = sheet.createRow(rowNum++);
                int col = 0;
                createCell(dataRow, col++, report.getUserId() != null ? report.getUserId().toString() : "", dataStyle);
                createCell(dataRow, col++, report.getUserName() != null ? report.getUserName() : "", dataStyle);
                createCell(dataRow, col++, report.getFullName() != null ? report.getFullName() : "", dataStyle);
                createCell(dataRow, col++, report.getEmail() != null ? report.getEmail() : "", dataStyle);
                createCell(dataRow, col++, report.getPhone() != null ? report.getPhone() : "", dataStyle);
                createCell(dataRow, col++, report.getTotalBookings() != null ? report.getTotalBookings().toString() : "0", dataStyle);
                createNumericCell(dataRow, col++, report.getTotalSpent() != null ? report.getTotalSpent() : 0.0, currencyStyle);
                createCell(dataRow, col++, report.getLastBookingDate() != null ? report.getLastBookingDate() : "", dataStyle);
            }
            
            autoSizeColumns(sheet, headers.length);
            workbook.write(out);
            return out.toByteArray();
        }
    }

    public static byte[] generateFrequentTravelerExcel(List<FrequentTravelerReportResponse> reports) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Frequent Travelers");
            
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            
            int rowNum = 0;
            
            rowNum = createTitle(sheet, rowNum, "Frequent Traveler Report", workbook);
            rowNum++;
            
            String[] headers = {"Rank", "User ID", "Username", "Full Name", "Email", "Phone", 
                               "Total Bookings", "Confirmed Bookings", "Total Spent", "First Booking", "Last Booking"};
            rowNum = createHeaderRow(sheet, rowNum, headers, headerStyle);
            
            for (FrequentTravelerReportResponse report : reports) {
                Row dataRow = sheet.createRow(rowNum++);
                int col = 0;
                createCell(dataRow, col++, report.getRank() != null ? report.getRank().toString() : "", dataStyle);
                createCell(dataRow, col++, report.getUserId() != null ? report.getUserId().toString() : "", dataStyle);
                createCell(dataRow, col++, report.getUserName() != null ? report.getUserName() : "", dataStyle);
                createCell(dataRow, col++, report.getFullName() != null ? report.getFullName() : "", dataStyle);
                createCell(dataRow, col++, report.getEmail() != null ? report.getEmail() : "", dataStyle);
                createCell(dataRow, col++, report.getPhone() != null ? report.getPhone() : "", dataStyle);
                createCell(dataRow, col++, report.getTotalBookings() != null ? report.getTotalBookings().toString() : "0", dataStyle);
                createCell(dataRow, col++, report.getConfirmedBookings() != null ? report.getConfirmedBookings().toString() : "0", dataStyle);
                createNumericCell(dataRow, col++, report.getTotalSpent() != null ? report.getTotalSpent() : 0.0, currencyStyle);
                createCell(dataRow, col++, report.getFirstBookingDate() != null ? report.getFirstBookingDate() : "", dataStyle);
                createCell(dataRow, col++, report.getLastBookingDate() != null ? report.getLastBookingDate() : "", dataStyle);
            }
            
            autoSizeColumns(sheet, headers.length);
            workbook.write(out);
            return out.toByteArray();
        }
    }

    public static byte[] generateCustomerDemographicsExcel(List<CustomerDemographicsReportResponse> reports) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Customer Demographics");
            
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            
            int rowNum = 0;
            
            rowNum = createTitle(sheet, rowNum, "Customer Demographics Report", workbook);
            rowNum++;
            
            String[] headers = {"Gender", "Total Users", "Active Users", "Total Bookings", 
                               "Total Revenue", "Avg Bookings/User"};
            rowNum = createHeaderRow(sheet, rowNum, headers, headerStyle);
            
            for (CustomerDemographicsReportResponse report : reports) {
                Row dataRow = sheet.createRow(rowNum++);
                int col = 0;
                createCell(dataRow, col++, report.getGender() != null ? report.getGender() : "", dataStyle);
                createCell(dataRow, col++, report.getTotalUsers() != null ? report.getTotalUsers().toString() : "0", dataStyle);
                createCell(dataRow, col++, report.getActiveUsers() != null ? report.getActiveUsers().toString() : "0", dataStyle);
                createCell(dataRow, col++, report.getTotalBookings() != null ? report.getTotalBookings().toString() : "0", dataStyle);
                createNumericCell(dataRow, col++, report.getTotalRevenue() != null ? report.getTotalRevenue() : 0.0, currencyStyle);
                createNumericCell(dataRow, col++, report.getAverageBookingsPerUser() != null ? report.getAverageBookingsPerUser() : 0.0, dataStyle);
            }
            
            autoSizeColumns(sheet, headers.length);
            workbook.write(out);
            return out.toByteArray();
        }
    }

    public static byte[] generateBookingPatternsExcel(List<BookingPatternReportResponse> reports, String title) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Booking Patterns");
            
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            
            int rowNum = 0;
            
            rowNum = createTitle(sheet, rowNum, title, workbook);
            rowNum++;
            
            String[] headers = {"Period", "Total Bookings", "Unique Customers", "Total Revenue", "Avg Booking Value"};
            rowNum = createHeaderRow(sheet, rowNum, headers, headerStyle);
            
            for (BookingPatternReportResponse report : reports) {
                Row dataRow = sheet.createRow(rowNum++);
                int col = 0;
                createCell(dataRow, col++, report.getPeriod() != null ? report.getPeriod() : "", dataStyle);
                createCell(dataRow, col++, report.getTotalBookings() != null ? report.getTotalBookings().toString() : "0", dataStyle);
                createCell(dataRow, col++, report.getUniqueCustomers() != null ? report.getUniqueCustomers().toString() : "0", dataStyle);
                createNumericCell(dataRow, col++, report.getTotalRevenue() != null ? report.getTotalRevenue() : 0.0, currencyStyle);
                createNumericCell(dataRow, col++, report.getAverageBookingValue() != null ? report.getAverageBookingValue() : 0.0, currencyStyle);
            }
            
            autoSizeColumns(sheet, headers.length);
            workbook.write(out);
            return out.toByteArray();
        }
    }

    // Helper methods

    private static int createTitle(Sheet sheet, int rowNum, String title, Workbook workbook) {
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(title);
        titleCell.setCellStyle(createTitleStyle(workbook));
        return rowNum;
    }

    private static int createHeaderRow(Sheet sheet, int rowNum, String[] headers, CellStyle headerStyle) {
        Row headerRow = sheet.createRow(rowNum++);
        for (int i = 0; i < headers.length; i++) {
            createCell(headerRow, i, headers[i], headerStyle);
        }
        return rowNum;
    }

    private static void createCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private static void createNumericCell(Row row, int column, Double value, CellStyle style) {
        Cell cell = row.createCell(column);
        if (value != null) {
            cell.setCellValue(value);
        }
        cell.setCellStyle(style);
    }

    private static void autoSizeColumns(Sheet sheet, int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    // Style creation methods

    private static CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        style.setFont(font);
        return style;
    }

    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        setBorders(style);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private static CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        setBorders(style);
        return style;
    }

    private static CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("$#,##0.00"));
        return style;
    }

    private static void setBorders(CellStyle style) {
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }
}
