package com.travelplan.tourist.repository;

import com.travelplan.tourist.entity.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {

    List<WalletTransaction> findByTouristIdOrderByCreatedAtDesc(Long touristId);

    @Query("SELECT COALESCE(SUM(CASE WHEN w.type = 'REFUND' OR w.type = 'ADJUSTMENT' THEN w.amount " +
           "WHEN w.type = 'USED' THEN -w.amount ELSE 0 END), 0) " +
           "FROM WalletTransaction w WHERE w.tourist.id = :touristId")
    BigDecimal calculateBalance(@Param("touristId") Long touristId);
}
