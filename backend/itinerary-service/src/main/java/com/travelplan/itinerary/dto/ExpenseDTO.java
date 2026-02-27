package com.travelplan.itinerary.dto;

import com.travelplan.itinerary.model.ExpenseCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseDTO {
    private Long id;
    private ExpenseCategory category;
    private String description;
    private BigDecimal amount;
    private LocalDate expenseDate;
}
