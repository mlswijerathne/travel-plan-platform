package com.travelplan.tourist.repository;

import com.travelplan.tourist.entity.Tourist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TouristRepository extends JpaRepository<Tourist, Long> {

    Optional<Tourist> findByUserId(String userId);

    Optional<Tourist> findByEmail(String email);

    boolean existsByUserId(String userId);

    boolean existsByEmail(String email);
}
