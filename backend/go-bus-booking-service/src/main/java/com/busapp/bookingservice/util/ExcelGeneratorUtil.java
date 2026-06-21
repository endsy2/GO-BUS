package com.busapp.bookingservice.util;

import com.busapp.bookingservice.dto.response.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ExcelGeneratorUtil {

    // Main Excel generation methods

    public static byte[] generateBookingReportExcel(BookingReportResponse report) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Booking Report");
            
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            
            int rowNum = 0;
            
            // Title
            rowNum = createTitle(sheet, rowNum, "Booking Report", workbook);
            rowNum++; // Empty row
            
            // Summary section
            rowNum = createSummaryRow(sheet, rowNum, "Report Period", 
                    report.getStartDate() + " to " + report.getEndDate(), headerStyle, dataStyle);
            rowNum = createSummaryRow(sheet, rowNum, "Total Bookings", 
                    report.getTotalBookings().toString(), headerStyle, dataStyle);
            rowNum = createSummaryRow(sheet, rowNum, "Confirmed Bookings", 
                    report.getConfirmedBookings().toString(), headerStyle, dataStyle);
            rowNum = createSummaryRow(sheet, rowNum, "Pending Bookings", 
                    report.getPendingBookings().toString(), headerStyle, dataStyle);
            rowNum = createSummaryRow(sheet, rowNum, "Cancelled Bookings", 
                    report.getCancelledBookings().toString(), headerStyle, dataStyle);
            
            rowNum++; // Empty row
            
            // Status breakdown table
            String[] statusHeaders = {"Status", "Count"};
            rowNum = createHeaderRow(sheet, rowNum, statusHeaders, headerStyle);
            
            for (Map.Entry<String, Long> entry : report.getBookingsByStatus().entrySet()) {
                Row dataRow = sheet.createRow(rowNum++);
                createCell(dataRow, 0, entry.getKey(), dataStyle);
                createCell(dataRow, 1, entry.getValue().toString(), dataStyle);
            }
            
            autoSizeColumns(sheet, statusHeaders.length);
            workbook.write(out);
            return out.toByteArray();
        }
    }

    public static byte[] generateRouteRevenueExcel(List<RouteRevenueReportResponse> reports) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Route Revenue Report");
            
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            
            int rowNum = 0;
            
            // Title
            rowNum = createTitle(sheet, rowNum, "Route Revenue Report", workbook);
            rowNum++; // Empty row
            
            // Headers
            String[] headers = {"Route ID", "Route Name", "Total Bookings", "Confirmed", 
                               "Cancelled", "Total Revenue", "Tickets Sold"};
            rowNum = createHeaderRow(sheet, rowNum, headers, headerStyle);
            
            // Data rows
            for (RouteRevenueReportResponse report : reports) {
                Row dataRow = sheet.createRow(rowNum++);
                int col = 0;
                createCell(dataRow, col++,report.getRouteId()!=null?Long.toString(report.getRouteId()):"", dataStyle);
                createCell(dataRow, col++, report.getRouteName()!=null?report.getRouteName():"", dataStyle);
                createCell(dataRow, col++, report.getTotalBookings()!=null?report.getTotalBookings().toString():"", dataStyle);
                createCell(dataRow, col++, report.getConfirmedBookings()!=null?report.getConfirmedBookings().toString():"", dataStyle);
                createCell(dataRow, col++, report.getCancelledBookings()!=null?report.getCancelledBookings().toString():"", dataStyle);
                createNumericCell(dataRow, col++, report.getTotalRevenue()!=null?report.getTotalRevenue():null, currencyStyle);
                createCell(dataRow, col++, report.getTotalTicketsSold()!=null?report.getTotalTicketsSold().toString():"", dataStyle);
            }
            
            autoSizeColumns(sheet, headers.length);
            workbook.write(out);
            return out.toByteArray();
        }
    }

    public static byte[] generatePopularRoutesExcel(List<PopularRouteReportResponse> reports) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Popular Routes Report");
            
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle percentStyle = createPercentStyle(workbook);
            
            int rowNum = 0;
            
            // Title
            rowNum = createTitle(sheet, rowNum, "Popular Routes Report", workbook);
            rowNum++; // Empty row
            
            // Headers
            String[] headers = {"Route ID", "Route Name", "Total Bookings", "Confirmed", 
                               "Cancelled", "Tickets Sold", "Occupancy Rate"};
            rowNum = createHeaderRow(sheet, rowNum, headers, headerStyle);
            
            // Data rows
            for (PopularRouteReportResponse report : reports) {
                Row dataRow = sheet.createRow(rowNum++);
                int col = 0;
                createCell(dataRow, col++, report.getRouteId() != null ? report.getRouteId().toString() : "", dataStyle);

                createCell(dataRow, col++, report.getRouteName() != null ? report.getRouteName() : "", dataStyle);

                createCell(dataRow, col++, report.getTotalBookings() != null ? report.getTotalBookings().toString() : "0", dataStyle);

                createCell(dataRow, col++, report.getConfirmedBookings() != null ? report.getConfirmedBookings().toString() : "0", dataStyle);

                createCell(dataRow, col++, report.getCancelledBookings() != null ? report.getCancelledBookings().toString() : "0", dataStyle);

                createCell(dataRow, col++, report.getTotalTicketsSold() != null ? report.getTotalTicketsSold().toString() : "0", dataStyle);

                createNumericCell(dataRow, col++, report.getAverageOccupancyRate() != null ? report.getAverageOccupancyRate() : 0.0, percentStyle);
            }
            
            autoSizeColumns(sheet, headers.length);
            workbook.write(out);
            return out.toByteArray();
        }
    }

    public static byte[] generateSeatOccupancyExcel(List<SeatOccupancyReportResponse> reports) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Seat Occupancy Report");
            
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle percentStyle = createPercentStyle(workbook);
            
            int rowNum = 0;
            
            // Title
            rowNum = createTitle(sheet, rowNum, "Seat Occupancy Report", workbook);
            rowNum++; // Empty row
            
            // Headers
            String[] headers = {"Schedule ID", "Route ID", "Bus ID", "Total Seats", 
                               "Booked Seats", "Occupancy Rate"};
            rowNum = createHeaderRow(sheet, rowNum, headers, headerStyle);
            
            // Data rows
            for (SeatOccupancyReportResponse report : reports) {
                Row dataRow = sheet.createRow(rowNum++);
                int col = 0;
                createCell(dataRow, col++,
                        report.getScheduleId() != null ? report.getScheduleId().toString() : "",
                        dataStyle
                );

                createCell(dataRow, col++,
                        report.getRouteId() != null ? report.getRouteId().toString() : "",
                        dataStyle
                );

                createCell(dataRow, col++,
                        report.getBusId() != null ? report.getBusId().toString() : "",
                        dataStyle
                );

                createCell(dataRow, col++,
                        report.getTotalSeats() != null ? report.getTotalSeats().toString() : "0",
                        dataStyle
                );

                createCell(dataRow, col++,
                        report.getBookedSeats() != null ? report.getBookedSeats().toString() : "0",
                        dataStyle
                );

                createNumericCell(dataRow, col++,
                        report.getOccupancyRate() != null ? report.getOccupancyRate() : 0.0,
                        percentStyle
                );
            }
            
            autoSizeColumns(sheet, headers.length);
            workbook.write(out);
            return out.toByteArray();
        }
    }

    public static byte[] generateTicketSalesExcel(List<TicketSalesReportResponse> reports) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Ticket Sales Report");
            
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            
            int rowNum = 0;
            
            // Title
            rowNum = createTitle(sheet, rowNum, "Ticket Sales Report", workbook);
            rowNum++; // Empty row
            
            // Headers
            String[] headers = {"Schedule ID", "Route ID", "Bus ID", "Tickets Sold", "Total Revenue"};
            rowNum = createHeaderRow(sheet, rowNum, headers, headerStyle);
            
            // Data rows
            for (TicketSalesReportResponse report : reports) {
                Row dataRow = sheet.createRow(rowNum++);
                int col = 0;
                createCell(dataRow, col++,
                        report.getScheduleId() != null ? report.getScheduleId().toString() : "",
                        dataStyle
                );

                createCell(dataRow, col++,
                        report.getRouteId() != null ? report.getRouteId().toString() : "",
                        dataStyle
                );

                createCell(dataRow, col++,
                        report.getBusId() != null ? report.getBusId().toString() : "",
                        dataStyle
                );

                createCell(dataRow, col++,
                        report.getTotalTicketsSold() != null ? report.getTotalTicketsSold().toString() : "0",
                        dataStyle
                );

                createNumericCell(dataRow, col++,
                        report.getTotalRevenue() != null ? report.getTotalRevenue() : 0.0,
                        currencyStyle
                );
            }
            
            autoSizeColumns(sheet, headers.length);
            workbook.write(out);
            return out.toByteArray();
        }
    }

    // Helper methods for creating Excel elements

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

    private static int createSummaryRow(Sheet sheet, int rowNum, String label, String value, 
                                       CellStyle labelStyle, CellStyle valueStyle) {
        Row row = sheet.createRow(rowNum++);
        createCell(row, 0, label, labelStyle);
        createCell(row, 1, value, valueStyle);
        return rowNum;
    }

    private static void createCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private static void createNumericCell(Row row, int column, Double value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
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

    public static byte[] generateRevenueReportExcel(List<RevenueReportResponse> reports) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Revenue Report");
            
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            
            int rowNum = 0;
            
            rowNum = createTitle(sheet, rowNum, "Revenue Report", workbook);
            rowNum++;
            
            String[] headers = {"Period", "Total Bookings", "Confirmed", "Cancelled", 
                               "Total Revenue", "Confirmed Revenue", "Refunded Amount", "Net Revenue"};
            rowNum = createHeaderRow(sheet, rowNum, headers, headerStyle);
            
            for (RevenueReportResponse report : reports) {
                Row dataRow = sheet.createRow(rowNum++);
                int col = 0;
                createCell(dataRow, col++, report.getPeriod() != null ? report.getPeriod() : "", dataStyle);
                createCell(dataRow, col++, report.getTotalBookings() != null ? report.getTotalBookings().toString() : "0", dataStyle);
                createCell(dataRow, col++, report.getConfirmedBookings() != null ? report.getConfirmedBookings().toString() : "0", dataStyle);
                createCell(dataRow, col++, report.getCancelledBookings() != null ? report.getCancelledBookings().toString() : "0", dataStyle);
                createNumericCell(dataRow, col++, report.getTotalRevenue() != null ? report.getTotalRevenue() : 0.0, currencyStyle);
                createNumericCell(dataRow, col++, report.getConfirmedRevenue() != null ? report.getConfirmedRevenue() : 0.0, currencyStyle);
                createNumericCell(dataRow, col++, report.getRefundedAmount() != null ? report.getRefundedAmount() : 0.0, currencyStyle);
                createNumericCell(dataRow, col++, report.getNetRevenue() != null ? report.getNetRevenue() : 0.0, currencyStyle);
            }
            
            autoSizeColumns(sheet, headers.length);
            workbook.write(out);
            return out.toByteArray();
        }
    }

    public static byte[] generatePaymentMethodReportExcel(List<PaymentMethodReportResponse> reports) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Payment Method Report");
            
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            CellStyle percentStyle = createPercentStyle(workbook);
            
            int rowNum = 0;
            
            rowNum = createTitle(sheet, rowNum, "Payment Method Analysis Report", workbook);
            rowNum++;
            
            String[] headers = {"Payment Method", "Total Transactions", "Successful", "Failed", 
                               "Total Amount", "Avg Transaction Value", "% of Total"};
            rowNum = createHeaderRow(sheet, rowNum, headers, headerStyle);
            
            for (PaymentMethodReportResponse report : reports) {
                Row dataRow = sheet.createRow(rowNum++);
                int col = 0;
                createCell(dataRow, col++, report.getPaymentMethod() != null ? report.getPaymentMethod() : "", dataStyle);
                createCell(dataRow, col++, report.getTotalTransactions() != null ? report.getTotalTransactions().toString() : "0", dataStyle);
                createCell(dataRow, col++, report.getSuccessfulTransactions() != null ? report.getSuccessfulTransactions().toString() : "0", dataStyle);
                createCell(dataRow, col++, report.getFailedTransactions() != null ? report.getFailedTransactions().toString() : "0", dataStyle);
                createNumericCell(dataRow, col++, report.getTotalAmount() != null ? report.getTotalAmount() : 0.0, currencyStyle);
                createNumericCell(dataRow, col++, report.getAverageTransactionValue() != null ? report.getAverageTransactionValue() : 0.0, currencyStyle);
                createNumericCell(dataRow, col++, report.getPercentageOfTotal() != null ? report.getPercentageOfTotal() : 0.0, percentStyle);
            }
            
            autoSizeColumns(sheet, headers.length);
            workbook.write(out);
            return out.toByteArray();
        }
    }

    public static byte[] generateRefundCancellationReportExcel(List<RefundCancellationReportResponse> reports) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Refund & Cancellation Report");
            
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            CellStyle percentStyle = createPercentStyle(workbook);
            
            int rowNum = 0;
            
            rowNum = createTitle(sheet, rowNum, "Refund & Cancellation Report", workbook);
            rowNum++;
            
            String[] headers = {"Period", "Total Cancellations", "Total Refund Amount", "Avg Refund Amount", 
                               "User Initiated", "System Initiated", "Cancellation Rate %"};
            rowNum = createHeaderRow(sheet, rowNum, headers, headerStyle);
            
            for (RefundCancellationReportResponse report : reports) {
                Row dataRow = sheet.createRow(rowNum++);
                int col = 0;
                createCell(dataRow, col++, report.getPeriod() != null ? report.getPeriod() : "", dataStyle);
                createCell(dataRow, col++, report.getTotalCancellations() != null ? report.getTotalCancellations().toString() : "0", dataStyle);
                createNumericCell(dataRow, col++, report.getTotalRefundAmount() != null ? report.getTotalRefundAmount() : 0.0, currencyStyle);
                createNumericCell(dataRow, col++, report.getAverageRefundAmount() != null ? report.getAverageRefundAmount() : 0.0, currencyStyle);
                createCell(dataRow, col++, report.getUserInitiatedCancellations() != null ? report.getUserInitiatedCancellations().toString() : "0", dataStyle);
                createCell(dataRow, col++, report.getSystemCancellations() != null ? report.getSystemCancellations().toString() : "0", dataStyle);
                createNumericCell(dataRow, col++, report.getCancellationRate() != null ? report.getCancellationRate() : 0.0, percentStyle);
            }
            
            autoSizeColumns(sheet, headers.length);
            workbook.write(out);
            return out.toByteArray();
        }
    }

    public static byte[] generatePromoCodeUsageReportExcel(List<PromoCodeUsageReportResponse> reports) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Promo Code Usage Report");
            
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            
            int rowNum = 0;
            
            rowNum = createTitle(sheet, rowNum, "Promo Code Usage Report", workbook);
            rowNum++;
            
            String[] headers = {"Promo ID", "Promo Code", "Discount Type", "Discount Value", 
                               "Total Usages", "Max Uses", "Total Discount Given", "Total Revenue", "Status", "Valid From", "Valid To"};
            rowNum = createHeaderRow(sheet, rowNum, headers, headerStyle);
            
            for (PromoCodeUsageReportResponse report : reports) {
                Row dataRow = sheet.createRow(rowNum++);
                int col = 0;
                createCell(dataRow, col++, report.getPromoId() != null ? report.getPromoId().toString() : "", dataStyle);
                createCell(dataRow, col++, report.getPromoCode() != null ? report.getPromoCode() : "", dataStyle);
                createCell(dataRow, col++, report.getDiscountType() != null ? report.getDiscountType() : "", dataStyle);
                createNumericCell(dataRow, col++, report.getDiscountValue() != null ? report.getDiscountValue() : 0.0, dataStyle);
                createCell(dataRow, col++, report.getTotalUsages() != null ? report.getTotalUsages().toString() : "0", dataStyle);
                createCell(dataRow, col++, report.getMaxUses() != null ? report.getMaxUses().toString() : "Unlimited", dataStyle);
                createNumericCell(dataRow, col++, report.getTotalDiscountGiven() != null ? report.getTotalDiscountGiven() : 0.0, currencyStyle);
                createNumericCell(dataRow, col++, report.getTotalRevenueWithPromo() != null ? report.getTotalRevenueWithPromo() : 0.0, currencyStyle);
                createCell(dataRow, col++, report.getStatus() != null ? report.getStatus() : "", dataStyle);
                createCell(dataRow, col++, report.getValidFrom() != null ? report.getValidFrom() : "", dataStyle);
                createCell(dataRow, col++, report.getValidTo() != null ? report.getValidTo() : "", dataStyle);
            }
            
            autoSizeColumns(sheet, headers.length);
            workbook.write(out);
            return out.toByteArray();
        }
    }
}
