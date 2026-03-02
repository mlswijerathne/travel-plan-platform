package com.travelplan.tripplan.repository;

import com.travelplan.tripplan.entity.TripPackage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PackageRepository extends JpaRepository<TripPackage, Long>, JpaSpecificationExecutor<TripPackage> {

    Optional<TripPackage> findByIdAndIsActiveTrue(Long id);

    List<TripPackage> findByIsFeaturedTrueAndIsActiveTrue();

    Page<TripPackage> findByIsActiveTrue(Pageable pageable);
}
