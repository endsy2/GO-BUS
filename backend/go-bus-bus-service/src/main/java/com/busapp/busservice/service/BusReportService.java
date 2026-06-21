package com.busapp.busservice.service;

import com.busapp.busservice.dto.*;

import java.time.LocalDate;
import java.util.List;

public interface BusReportService {
    
    List<BusUtilizationReportResponse> getBusUtilizationReport(LocalDate startDate, LocalDate endDate);
    
    List<RoutePerformanceReportResponse> getRoutePerformanceReport(LocalDate startDate, LocalDate endDate);
    
    List<InactiveBusReportResponse> getInactiveBusReport(Integer daysThreshold);
    
    List<InactiveRouteReportResponse> getInactiveRouteReport(Integer daysThreshold);
    
    List<BusCapacityAnalysisResponse> getBusCapacityAnalysis(LocalDate startDate, LocalDate endDate);
}
