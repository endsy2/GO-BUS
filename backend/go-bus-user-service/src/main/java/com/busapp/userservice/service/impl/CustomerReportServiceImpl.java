package com.busapp.userservice.service.impl;

import com.busapp.userservice.client.BookingClient;
import com.busapp.userservice.dto.*;
import com.busapp.userservice.model.User;
import com.busapp.userservice.model.enums.Gender;
import com.busapp.userservice.repository.UserRepository;
import com.busapp.userservice.service.CustomerReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerReportServiceImpl implements CustomerReportService {

    private final UserRepository userRepository;
    private final BookingClient bookingClient;

    @Override
    public List<ActiveCustomerReportResponse> getActiveCustomerReport(LocalDate startDate, LocalDate endDate) {
        try {
            List<Map<String, Object>> activeUsers = bookingClient.getActiveUsers(startDate, endDate);
            
            return activeUsers.stream().map(userData -> {
                Long userId = ((Number) userData.get("userId")).longValue();
                User user = userRepository.findById(userId).orElse(null);
                
                if (user == null) {
                    return null;
                }
                
                return ActiveCustomerReportResponse.builder()
                        .userId(userId)
                        .userName(user.getUserName())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .phone(user.getPhone())
                        .totalBookings(((Number) userData.get("totalBookings")).longValue())
                        .totalSpent(((Number) userData.get("totalSpent")).doubleValue())
                        .lastBookingDate(userData.get("lastBookingDate").toString())
                        .build();
            }).filter(Objects::nonNull).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to fetch active customer report: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<FrequentTravelerReportResponse> getFrequentTravelerReport(LocalDate startDate, LocalDate endDate, Integer limit) {
        try {
            List<Map<String, Object>> travelers = bookingClient.getFrequentTravelers(startDate, endDate, limit);
            
            int rank = 1;
            List<FrequentTravelerReportResponse> result = new ArrayList<>();
            
            for (Map<String, Object> travelerData : travelers) {
                Long userId = ((Number) travelerData.get("userId")).longValue();
                User user = userRepository.findById(userId).orElse(null);
                
                if (user == null) {
                    continue;
                }
                
                result.add(FrequentTravelerReportResponse.builder()
                        .userId(userId)
                        .userName(user.getUserName())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .phone(user.getPhone())
                        .totalBookings(((Number) travelerData.get("totalBookings")).longValue())
                        .confirmedBookings(((Number) travelerData.get("confirmedBookings")).longValue())
                        .totalSpent(((Number) travelerData.get("totalSpent")).doubleValue())
                        .firstBookingDate(travelerData.get("firstBookingDate").toString())
                        .lastBookingDate(travelerData.get("lastBookingDate").toString())
                        .rank(rank++)
                        .build());
            }
            
            return result;
        } catch (Exception e) {
            log.error("Failed to fetch frequent traveler report: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<CustomerDemographicsReportResponse> getCustomerDemographicsReport(LocalDate startDate, LocalDate endDate) {
        List<User> allUsers = userRepository.findAll();
        
        Map<Gender, List<User>> usersByGender = allUsers.stream()
                .filter(user -> user.getGender() != null)
                .collect(Collectors.groupingBy(User::getGender));

        List<CustomerDemographicsReportResponse> demographics = new ArrayList<>();

        for (Map.Entry<Gender, List<User>> entry : usersByGender.entrySet()) {
            Gender gender = entry.getKey();
            List<User> users = entry.getValue();
            
            long totalBookings = 0;
            double totalRevenue = 0.0;
            long activeUsers = 0;

            for (User user : users) {
                try {
                    Map<String, Object> stats = bookingClient.getUserBookingStats(user.getId(), startDate, endDate);
                    Long bookings = ((Number) stats.get("totalBookings")).longValue();
                    Double revenue = ((Number) stats.get("totalRevenue")).doubleValue();
                    
                    totalBookings += bookings;
                    totalRevenue += revenue;
                    
                    if (bookings > 0) {
                        activeUsers++;
                    }
                } catch (Exception e) {
                    log.warn("Failed to fetch stats for user {}: {}", user.getId(), e.getMessage());
                }
            }

            double avgBookingsPerUser = users.size() > 0 ? (double) totalBookings / users.size() : 0.0;

            demographics.add(CustomerDemographicsReportResponse.builder()
                    .gender(gender.name())
                    .totalUsers((long) users.size())
                    .activeUsers(activeUsers)
                    .totalBookings(totalBookings)
                    .totalRevenue(Math.round(totalRevenue * 100.0) / 100.0)
                    .averageBookingsPerUser(Math.round(avgBookingsPerUser * 100.0) / 100.0)
                    .build());
        }

        return demographics;
    }

    @Override
    public List<BookingPatternReportResponse> getBookingPatternsByDay(LocalDate startDate, LocalDate endDate) {
        try {
            List<Map<String, Object>> patterns = bookingClient.getBookingPatternsByDay(startDate, endDate);
            
            return patterns.stream().map(data -> 
                BookingPatternReportResponse.builder()
                        .period(data.get("dayOfWeek").toString())
                        .totalBookings(((Number) data.get("totalBookings")).longValue())
                        .uniqueCustomers(((Number) data.get("uniqueCustomers")).longValue())
                        .totalRevenue(((Number) data.get("totalRevenue")).doubleValue())
                        .averageBookingValue(((Number) data.get("averageBookingValue")).doubleValue())
                        .build()
            ).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to fetch booking patterns by day: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<BookingPatternReportResponse> getBookingPatternsByHour(LocalDate startDate, LocalDate endDate) {
        try {
            List<Map<String, Object>> patterns = bookingClient.getBookingPatternsByHour(startDate, endDate);
            
            return patterns.stream().map(data -> 
                BookingPatternReportResponse.builder()
                        .period(data.get("hour").toString())
                        .totalBookings(((Number) data.get("totalBookings")).longValue())
                        .uniqueCustomers(((Number) data.get("uniqueCustomers")).longValue())
                        .totalRevenue(((Number) data.get("totalRevenue")).doubleValue())
                        .averageBookingValue(((Number) data.get("averageBookingValue")).doubleValue())
                        .build()
            ).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to fetch booking patterns by hour: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
