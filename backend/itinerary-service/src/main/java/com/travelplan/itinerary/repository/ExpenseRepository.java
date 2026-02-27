package com.travelplan.itinerary.repository;

import com.travelplan.itinerary.model.Expense;
import com.travelplan.itinerary.model.ExpenseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByItineraryId(Long itineraryId);

    List<Expense> findByItineraryIdOrderByExpenseDateDesc(Long itineraryId);

    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.itinerary.id = :itineraryId")
    BigDecimal sumByItineraryId(@Param("itineraryId") Long itineraryId);

    @Query("SELECT e FROM Expense e WHERE e.itinerary.id = :itineraryId AND e.expenseDate = :date ORDER BY e.createdAt")
    List<Expense> findByItineraryIdAndDate(@Param("itineraryId") Long itineraryId, @Param("date") LocalDate date);

    @Query("SELECT new map(e.category, SUM(e.amount)) FROM Expense e WHERE e.itinerary.id = :itineraryId GROUP BY e.category")
    List<java.util.Map<String, Object>> categoryBreakdown(@Param("itineraryId") Long itineraryId);
}
