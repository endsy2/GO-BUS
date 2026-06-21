package com.busapp.busservice.util;

import com.busapp.busservice.dto.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class ExcelGeneratorUtil {

    public static byte[] generateBusUtilizationExcel(List<BusUtilizationReportResponse> reports) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Bus Utilization Report");
            
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle percentStyle = createPercentStyle(workbook);
            
            int rowNum = 0;
            
            // Title
            rowNum = createTitle(sheet, rowNum, "Bus Utilization Report", workbook);
            rowNum++; // Empty row
            
            // Headers
            String[] headers = {"Bus ID", "Bus Number", "Plate", "Model", "Bus Type", 
                               "Route", "Total Schedules", "Total Bookings", "Utilization Rate (%)", "Status"};
            rowNum = createHeaderRow(sheet, rowNum, headers, headerStyle);
            
            // Data rows
            for (BusUtilizationReportResponse report : reports) {
                Row dataRow = sheet.createRow(rowNum++);
                int col = 0;
                createCell(dataRow, col++, report.getBusId() != null ? report.getBusId().toString() : "", dataStyle);
                createCell(dataRow, col++, report.getBusNumber() != null ? report.getBusNumber() : "", dataStyle);
                createCell(dataRow, col++, report.getPlate() != null ? report.getPlate() : "", dataStyle);
                createCell(dataRow, col++, report.getModel() != null ? report.getModel() : "", dataStyle);
                createCell(dataRow, col++, report.getBusType() != null ? report.getBusType() : "", dataStyle);
                createCell(dataRow, col++, report.getRouteName() != null ? report.getRouteName() : "", dataStyle);
                createCell(dataRow, col++, report.getTotalSchedules() != null ? report.getTotalSchedules().toString() : "0", dataStyle);
                createCell(dataRow, col++, report.getTotalBookings() != null ? report.getTotalBookings().toString() : "0", dataStyle);
                createNumericCell(dataRow, col++, report.getUtilizationRate() != null ? report.getUtilizationRate() : 0.0, percentStyle);
                createCell(dataRow, col++, report.getStatus() != null ? report.getStatus() : "", dataStyle);
            }
            
            autoSizeColumns(sheet, headers.length);
            workbook.write(out);
            return out.toByteArray();
        }
    }

    public static byte[] generateRoutePerformanceExcel(List<RoutePerformanceReportResponse> reports) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Route Performance Report");
            
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            
            int rowNum = 0;
            
            // Title
            rowNum = createTitle(sheet, rowNum, "Route Performance Report", workbook);
            rowNum++; // Empty row
            
            // Headers
            String[] headers = {"Route ID", "Origin", "Destination", "Distance (km)", 
                               "Total Buses", "Total Schedules", "Total Bookings", "Total Revenue", "Avg Bookings/Schedule"};
            rowNum = createHeaderRow(sheet, rowNum, headers, headerStyle);
            
            // Data rows
            for (RoutePerformanceReportResponse report : reports) {
                Row dataRow = sheet.createRow(rowNum++);
                int col = 0;
                createCell(dataRow, col++, report.getRouteId() != null ? report.getRouteId().toString() : "", dataStyle);
                createCell(dataRow, col++, report.getOrigin() != null ? report.getOrigin() : "", dataStyle);
                createCell(dataRow, col++, report.getDestination() != null ? report.getDestination() : "", dataStyle);
                createNumericCell(dataRow, col++, report.getDistanceKm() != null ? report.getDistanceKm() : 0.0, dataStyle);
                createCell(dataRow, col++, report.getTotalBuses() != null ? report.getTotalBuses().toString() : "0", dataStyle);
                createCell(dataRow, col++, report.getTotalSchedules() != null ? report.getTotalSchedules().toString() : "0", dataStyle);
                createCell(dataRow, col++, report.getTotalBookings() != null ? report.getTotalBookings().toString() : "0", dataStyle);
                createNumericCell(dataRow, col++, report.getTotalRevenue() != null ? report.getTotalRevenue() : 0.0, currencyStyle);
                createNumericCell(dataRow, col++, report.getAverageBookingsPerSchedule() != null ? report.getAverageBookingsPerSchedule() : 0.0, dataStyle);
            }
            
            autoSizeColumns(sheet, headers.length);
            workbook.write(out);
            return out.toByteArray();
        }
    }

    public static byte[] generateInactiveBusExcel(List<InactiveBusReportResponse> reports) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Inactive Bus Report");
            
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            
            int rowNum = 0;
            
            // Title
            rowNum = createTitle(sheet, rowNum, "Inactive Bus Report", workbook);
            rowNum++; // Empty row
            
            // Headers
            String[] headers = {"Bus ID", "Bus Number", "Plate", "Model", "Bus Type", 
                               "Route", "Total Schedules", "Total Bookings", "Last Schedule Date", "Inactivity Reason"};
            rowNum = createHeaderRow(sheet, rowNum, headers, headerStyle);
            
            // Data rows
            for (InactiveBusReportResponse report : reports) {
                Row dataRow = sheet.createRow(rowNum++);
                int col = 0;
                createCell(dataRow, col++, report.getBusId() != null ? report.getBusId().toString() : "", dataStyle);
                createCell(dataRow, col++, report.getBusNumber() != null ? report.getBusNumber() : "", dataStyle);
                createCell(dataRow, col++, report.getPlate() != null ? report.getPlate() : "", dataStyle);
                createCell(dataRow, col++, report.getModel() != null ? report.getModel() : "", dataStyle);
                createCell(dataRow, col++, report.getBusType() != null ? report.getBusType() : "", dataStyle);
                createCell(dataRow, col++, report.getRouteName() != null ? report.getRouteName() : "", dataStyle);
                createCell(dataRow, col++, report.getTotalSchedules() != null ? report.getTotalSchedules().toString() : "0", dataStyle);
                createCell(dataRow, col++, report.getTotalBookings() != null ? report.getTotalBookings().toString() : "0", dataStyle);
                createCell(dataRow, col++, report.getLastScheduleDate() != null ? report.getLastScheduleDate().toString() : "N/A", dataStyle);
                createCell(dataRow, col++, report.getInactivityReason() != null ? report.getInactivityReason() : "", dataStyle);
            }
            
            autoSizeColumns(sheet, headers.length);
            workbook.write(out);
            return out.toByteArray();
        }
    }

    public static byte[] generateInactiveRouteExcel(List<InactiveRouteReportResponse> reports) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Inactive Route Report");
            
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            
            int rowNum = 0;
            
            // Title
            rowNum = createTitle(sheet, rowNum, "Inactive Route Report", workbook);
            rowNum++; // Empty row
            
            // Headers
            String[] headers = {"Route ID", "Origin", "Destination", "Distance (km)", 
                               "Total Buses", "Total Schedules", "Total Bookings", "Last Schedule Date", "Inactivity Reason"};
            rowNum = createHeaderRow(sheet, rowNum, headers, headerStyle);
            
            // Data rows
            for (InactiveRouteReportResponse report : reports) {
                Row dataRow = sheet.createRow(rowNum++);
                int col = 0;
                createCell(dataRow, col++, report.getRouteId() != null ? report.getRouteId().toString() : "", dataStyle);
                createCell(dataRow, col++, report.getOrigin() != null ? report.getOrigin() : "", dataStyle);
                createCell(dataRow, col++, report.getDestination() != null ? report.getDestination() : "", dataStyle);
                createNumericCell(dataRow, col++, report.getDistanceKm() != null ? report.getDistanceKm() : 0.0, dataStyle);
                createCell(dataRow, col++, report.getTotalBuses() != null ? report.getTotalBuses().toString() : "0", dataStyle);
                createCell(dataRow, col++, report.getTotalSchedules() != null ? report.getTotalSchedules().toString() : "0", dataStyle);
                createCell(dataRow, col++, report.getTotalBookings() != null ? report.getTotalBookings().toString() : "0", dataStyle);
                createCell(dataRow, col++, report.getLastScheduleDate() != null ? report.getLastScheduleDate().toString() : "N/A", dataStyle);
                createCell(dataRow, col++, report.getInactivityReason() != null ? report.getInactivityReason() : "", dataStyle);
            }
            
            autoSizeColumns(sheet, headers.length);
            workbook.write(out);
            return out.toByteArray();
        }
    }

    public static byte[] generateBusCapacityAnalysisExcel(List<BusCapacityAnalysisResponse> reports) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Bus Capacity Analysis");
            
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle percentStyle = createPercentStyle(workbook);
            
            int rowNum = 0;
            
            // Title
            rowNum = createTitle(sheet, rowNum, "Bus Capacity Analysis Report", workbook);
            rowNum++; // Empty row
            
            // Headers
            String[] headers = {"Bus ID", "Bus Number", "Plate", "Route", "Total Seats", 
                               "Total Schedules", "Fully Booked Count", "Fully Booked %", "Avg Occupancy Rate %", "Total Booked Seats"};
            rowNum = createHeaderRow(sheet, rowNum, headers, headerStyle);
            
            // Data rows
            for (BusCapacityAnalysisResponse report : reports) {
                Row dataRow = sheet.createRow(rowNum++);
                int col = 0;
                createCell(dataRow, col++, report.getBusId() != null ? report.getBusId().toString() : "", dataStyle);
                createCell(dataRow, col++, report.getBusNumber() != null ? report.getBusNumber() : "", dataStyle);
                createCell(dataRow, col++, report.getPlate() != null ? report.getPlate() : "", dataStyle);
                createCell(dataRow, col++, report.getRouteName() != null ? report.getRouteName() : "", dataStyle);
                createCell(dataRow, col++, report.getTotalSeats() != null ? report.getTotalSeats().toString() : "0", dataStyle);
                createCell(dataRow, col++, report.getTotalSchedules() != null ? report.getTotalSchedules().toString() : "0", dataStyle);
                createCell(dataRow, col++, report.getFullyBookedCount() != null ? report.getFullyBookedCount().toString() : "0", dataStyle);
                createNumericCell(dataRow, col++, report.getFullyBookedPercentage() != null ? report.getFullyBookedPercentage() : 0.0, percentStyle);
                createNumericCell(dataRow, col++, report.getAverageOccupancyRate() != null ? report.getAverageOccupancyRate() : 0.0, percentStyle);
                createCell(dataRow, col++, report.getTotalBookedSeats() != null ? report.getTotalBookedSeats().toString() : "0", dataStyle);
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

    private static CellStyle createPercentStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("0.00\"%\""));
        return style;
    }

    private static void setBorders(CellStyle style) {
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }
}
