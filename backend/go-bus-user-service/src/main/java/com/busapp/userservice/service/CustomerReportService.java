package com.busapp.userservice.service;

import com.busapp.userservice.dto.*;

import java.time.LocalDate;
import java.util.List;

public interface CustomerReportService {
    
    List<ActiveCustomerReportResponse> getActiveCustomerReport(LocalDate startDate, LocalDate endDate);
    
    List<FrequentTravelerReportResponse> getFrequentTravelerReport(LocalDate startDate, LocalDate endDate, Integer limit);
    
    List<CustomerDemographicsReportResponse> getCustomerDemographicsReport(LocalDate startDate, LocalDate endDate);
    
    List<BookingPatternReportResponse> getBookingPatternsByDay(LocalDate startDate, LocalDate endDate);
    
    List<BookingPatternReportResponse> getBookingPatternsByHour(LocalDate startDate, LocalDate endDate);
}
