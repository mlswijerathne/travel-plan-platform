package com.travelplan.itinerary.controller;

import com.travelplan.itinerary.dto.ExpenseDTO;
import com.travelplan.itinerary.dto.ExpenseSummaryDTO;
import com.travelplan.itinerary.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/itineraries/{itineraryId}/expenses")
@RequiredArgsConstructor
@Slf4j
public class ExpenseController {
    private final ExpenseService expenseService;

    @GetMapping
    public ResponseEntity<List<ExpenseDTO>> getExpenses(
            @PathVariable Long itineraryId,
            @RequestHeader("X-Tourist-Id") String touristId) {
        log.info("GET expenses for itinerary {}", itineraryId);
        List<ExpenseDTO> expenses = expenseService.getExpenses(itineraryId, touristId);
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/summary")
    public ResponseEntity<ExpenseSummaryDTO> getExpenseSummary(
            @PathVariable Long itineraryId,
            @RequestHeader("X-Tourist-Id") String touristId) {
        log.info("GET expense summary for itinerary {}", itineraryId);
        ExpenseSummaryDTO summary = expenseService.getExpenseSummary(itineraryId, touristId);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/by-date")
    public ResponseEntity<List<ExpenseDTO>> getExpensesByDay(
            @PathVariable Long itineraryId,
            @RequestParam LocalDate date,
            @RequestHeader("X-Tourist-Id") String touristId) {
        log.info("GET expenses for itinerary {} on date {}", itineraryId, date);
        List<ExpenseDTO> expenses = expenseService.getExpensesByDay(itineraryId, touristId, date);
        return ResponseEntity.ok(expenses);
    }

    @PostMapping
    public ResponseEntity<ExpenseDTO> addExpense(
            @PathVariable Long itineraryId,
            @RequestHeader("X-Tourist-Id") String touristId,
            @RequestBody ExpenseDTO expenseDTO) {
        log.info("POST expense for itinerary {}", itineraryId);
        ExpenseDTO expense = expenseService.addExpense(itineraryId, touristId, expenseDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(expense);
    }

    @PutMapping("/{expenseId}")
    public ResponseEntity<ExpenseDTO> updateExpense(
            @PathVariable Long itineraryId,
            @PathVariable Long expenseId,
            @RequestHeader("X-Tourist-Id") String touristId,
            @RequestBody ExpenseDTO expenseDTO) {
        log.info("PUT expense {} for itinerary {}", expenseId, itineraryId);
        ExpenseDTO expense = expenseService.updateExpense(expenseId, itineraryId, touristId, expenseDTO);
        return ResponseEntity.ok(expense);
    }

    @DeleteMapping("/{expenseId}")
    public ResponseEntity<Void> deleteExpense(
            @PathVariable Long itineraryId,
            @PathVariable Long expenseId,
            @RequestHeader("X-Tourist-Id") String touristId) {
        log.info("DELETE expense {} for itinerary {}", expenseId, itineraryId);
        expenseService.deleteExpense(expenseId, itineraryId, touristId);
        return ResponseEntity.noContent().build();
    }
}
