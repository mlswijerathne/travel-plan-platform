package com.travelplan.tourist.repository;

import com.travelplan.tourist.entity.TouristPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TouristPreferenceRepository extends JpaRepository<TouristPreference, Long> {

    Optional<TouristPreference> findByTouristId(Long touristId);

    Optional<TouristPreference> findByTouristUserId(String userId);
}
