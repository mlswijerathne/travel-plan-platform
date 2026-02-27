package com.travelplan.tripplan.repository;

import com.travelplan.tripplan.entity.PackageItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PackageItemRepository extends JpaRepository<PackageItem, Long> {
}
