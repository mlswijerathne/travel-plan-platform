package com.travelplan.guide.repository;

import com.travelplan.guide.domain.Guide;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GuideRepository extends JpaRepository<Guide, Long>, JpaSpecificationExecutor<Guide> {

    Optional<Guide> findByUserId(String userId);

    Optional<Guide> findByEmail(String email);
}