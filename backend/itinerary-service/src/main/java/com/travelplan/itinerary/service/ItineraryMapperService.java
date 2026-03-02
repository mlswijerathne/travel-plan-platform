package com.travelplan.itinerary.service;

import com.travelplan.itinerary.dto.ItineraryActivityDTO;
import com.travelplan.itinerary.dto.ItineraryDayDTO;
import com.travelplan.itinerary.dto.ItineraryDTO;
import com.travelplan.itinerary.model.Itinerary;
import com.travelplan.itinerary.model.ItineraryDay;
import com.travelplan.itinerary.model.ItineraryActivity;
import com.travelplan.itinerary.model.Expense;
import com.travelplan.itinerary.dto.ExpenseDTO;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class ItineraryMapperService {

    public ItineraryDTO toDTO(Itinerary itinerary) {
        return ItineraryDTO.builder()
                .id(itinerary.getId())
                .touristId(itinerary.getTouristId())
                .title(itinerary.getTitle())
                .description(itinerary.getDescription())
                .startDate(itinerary.getStartDate())
                .endDate(itinerary.getEndDate())
                .status(itinerary.getStatus())
                .totalBudget(itinerary.getTotalBudget())
                .actualSpent(itinerary.getActualSpent())
                .days(itinerary.getDays().stream()
                        .map(this::toDayDTO)
                        .collect(Collectors.toList()))
                .expenses(itinerary.getExpenses().stream()
                        .map(this::toExpenseDTO)
                        .collect(Collectors.toList()))
                .build();
    }

    public ItineraryDayDTO toDayDTO(ItineraryDay day) {
        return ItineraryDayDTO.builder()
                .id(day.getId())
                .dayNumber(day.getDayNumber())
                .date(day.getDate())
                .notes(day.getNotes())
                .activities(day.getActivities().stream()
                        .map(this::toActivityDTO)
                        .collect(Collectors.toList()))
                .build();
    }

    public ItineraryActivityDTO toActivityDTO(ItineraryActivity activity) {
        return ItineraryActivityDTO.builder()
                .id(activity.getId())
                .activityType(activity.getActivityType())
                .providerType(activity.getProviderType())
                .providerId(activity.getProviderId())
                .title(activity.getTitle())
                .description(activity.getDescription())
                .startTime(activity.getStartTime())
                .endTime(activity.getEndTime())
                .location(activity.getLocation())
                .estimatedCost(activity.getEstimatedCost())
                .bookingId(activity.getBookingId())
                .sortOrder(activity.getSortOrder())
                .build();
    }

    public ExpenseDTO toExpenseDTO(Expense expense) {
        return ExpenseDTO.builder()
                .id(expense.getId())
                .category(expense.getCategory())
                .description(expense.getDescription())
                .amount(expense.getAmount())
                .expenseDate(expense.getExpenseDate())
                .build();
    }
}
