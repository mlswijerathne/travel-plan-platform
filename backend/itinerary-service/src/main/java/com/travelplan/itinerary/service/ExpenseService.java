package com.travelplan.itinerary.service;

import com.travelplan.itinerary.dto.ExpenseDTO;
import com.travelplan.itinerary.dto.ExpenseSummaryDTO;
import com.travelplan.itinerary.model.Expense;
import com.travelplan.itinerary.model.ExpenseCategory;
import com.travelplan.itinerary.model.Itinerary;
import com.travelplan.itinerary.repository.ExpenseRepository;
import com.travelplan.itinerary.repository.ItineraryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final ItineraryRepository itineraryRepository;

    public ExpenseDTO addExpense(Long itineraryId, String touristId, ExpenseDTO expenseDTO) {
        log.info("Adding expense to itinerary {} for tourist {}", itineraryId, touristId);

        Itinerary itinerary = itineraryRepository.findByIdAndTouristId(itineraryId, touristId)
                .orElseThrow(() -> new IllegalArgumentException("Itinerary not found or access denied"));

        Expense expense = Expense.builder()
                .itinerary(itinerary)
                .category(expenseDTO.getCategory())
                .description(expenseDTO.getDescription())
                .amount(expenseDTO.getAmount())
                .expenseDate(expenseDTO.getExpenseDate())
                .build();

        Expense saved = expenseRepository.save(expense);
        updateItineraryActualSpent(itinerary);
        log.info("Expense created with ID: {}", saved.getId());

        return toDTO(saved);
    }

    public List<ExpenseDTO> getExpenses(Long itineraryId, String touristId) {
        log.info("Fetching expenses for itinerary {}", itineraryId);

        itineraryRepository.findByIdAndTouristId(itineraryId, touristId)
                .orElseThrow(() -> new IllegalArgumentException("Itinerary not found or access denied"));

        return expenseRepository.findByItineraryIdOrderByExpenseDateDesc(itineraryId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ExpenseDTO updateExpense(Long expenseId, Long itineraryId, String touristId, ExpenseDTO expenseDTO) {
        log.info("Updating expense {} for itinerary {}", expenseId, itineraryId);

        Itinerary itinerary = itineraryRepository.findByIdAndTouristId(itineraryId, touristId)
                .orElseThrow(() -> new IllegalArgumentException("Itinerary not found or access denied"));

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new IllegalArgumentException("Expense not found"));

        if (expenseDTO.getCategory() != null) {
            expense.setCategory(expenseDTO.getCategory());
        }
        if (expenseDTO.getDescription() != null) {
            expense.setDescription(expenseDTO.getDescription());
        }
        if (expenseDTO.getAmount() != null) {
            expense.setAmount(expenseDTO.getAmount());
        }
        if (expenseDTO.getExpenseDate() != null) {
            expense.setExpenseDate(expenseDTO.getExpenseDate());
        }

        Expense updated = expenseRepository.save(expense);
        updateItineraryActualSpent(itinerary);
        return toDTO(updated);
    }

    public void deleteExpense(Long expenseId, Long itineraryId, String touristId) {
        log.info("Deleting expense {}", expenseId);

        Itinerary itinerary = itineraryRepository.findByIdAndTouristId(itineraryId, touristId)
                .orElseThrow(() -> new IllegalArgumentException("Itinerary not found or access denied"));

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new IllegalArgumentException("Expense not found"));

        expenseRepository.delete(expense);
        updateItineraryActualSpent(itinerary);
    }

    public ExpenseSummaryDTO getExpenseSummary(Long itineraryId, String touristId) {
        log.info("Generating expense summary for itinerary {}", itineraryId);

        Itinerary itinerary = itineraryRepository.findByIdAndTouristId(itineraryId, touristId)
                .orElseThrow(() -> new IllegalArgumentException("Itinerary not found or access denied"));

        BigDecimal totalSpent = expenseRepository.sumByItineraryId(itineraryId);
        if (totalSpent == null) {
            totalSpent = BigDecimal.ZERO;
        }

        BigDecimal totalBudget = itinerary.getTotalBudget() != null ? itinerary.getTotalBudget() : BigDecimal.ZERO;
        BigDecimal remainingBudget = totalBudget.subtract(totalSpent);

        Map<String, BigDecimal> categoryBreakdown = new HashMap<>();
        List<Expense> allExpenses = expenseRepository.findByItineraryId(itineraryId);
        
        for (Expense expense : allExpenses) {
            String categoryName = expense.getCategory().name();
            categoryBreakdown.merge(categoryName, expense.getAmount(), BigDecimal::add);
        }

        return ExpenseSummaryDTO.builder()
                .totalSpent(totalSpent)
                .remainingBudget(remainingBudget)
                .totalBudget(totalBudget)
                .categoryBreakdown(categoryBreakdown)
                .build();
    }

    public List<ExpenseDTO> getExpensesByDay(Long itineraryId, String touristId, LocalDate date) {
        log.info("Fetching expenses for itinerary {} on date {}", itineraryId, date);

        itineraryRepository.findByIdAndTouristId(itineraryId, touristId)
                .orElseThrow(() -> new IllegalArgumentException("Itinerary not found or access denied"));

        return expenseRepository.findByItineraryIdAndDate(itineraryId, date).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private void updateItineraryActualSpent(Itinerary itinerary) {
        BigDecimal totalSpent = expenseRepository.sumByItineraryId(itinerary.getId());
        if (totalSpent == null) {
            totalSpent = BigDecimal.ZERO;
        }
        itinerary.setActualSpent(totalSpent);
        itineraryRepository.save(itinerary);
    }

    private ExpenseDTO toDTO(Expense expense) {
        return ExpenseDTO.builder()
                .id(expense.getId())
                .category(expense.getCategory())
                .description(expense.getDescription())
                .amount(expense.getAmount())
                .expenseDate(expense.getExpenseDate())
                .build();
    }
}
