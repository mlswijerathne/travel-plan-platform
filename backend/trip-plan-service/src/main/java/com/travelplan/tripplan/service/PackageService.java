package com.travelplan.tripplan.service;

import com.travelplan.tripplan.client.HotelServiceClient;
import com.travelplan.tripplan.client.TourGuideServiceClient;
import com.travelplan.tripplan.client.VehicleServiceClient;
import com.travelplan.tripplan.dto.PackageItemRequest;
import com.travelplan.tripplan.dto.PackageItemResponse;
import com.travelplan.tripplan.dto.PackageRequest;
import com.travelplan.tripplan.dto.PackageResponse;
import com.travelplan.tripplan.entity.TripPackage;
import com.travelplan.tripplan.entity.PackageItem;
import com.travelplan.tripplan.entity.ProviderType;
import com.travelplan.tripplan.exception.ResourceNotFoundException;
import com.travelplan.tripplan.repository.PackageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PackageService {

    private static final Logger log = LoggerFactory.getLogger(PackageService.class);

    private final PackageRepository packageRepository;
    private final HotelServiceClient hotelServiceClient;
    private final TourGuideServiceClient tourGuideServiceClient;
    private final VehicleServiceClient vehicleServiceClient;

    public PackageService(PackageRepository packageRepository,
            HotelServiceClient hotelServiceClient,
            TourGuideServiceClient tourGuideServiceClient,
            VehicleServiceClient vehicleServiceClient) {
        this.packageRepository = packageRepository;
        this.hotelServiceClient = hotelServiceClient;
        this.tourGuideServiceClient = tourGuideServiceClient;
        this.vehicleServiceClient = vehicleServiceClient;
    }

    @Transactional
    public PackageResponse createPackage(PackageRequest request, String createdBy) {
        // Validate providers
        if (request.getItems() != null) {
            for (PackageItemRequest item : request.getItems()) {
                validateProvider(item.getProviderType(), item.getProviderId());
            }
        }

        TripPackage tripPackage = new TripPackage();
        mapRequestToEntity(request, tripPackage);
        tripPackage.setCreatedBy(createdBy);

        if (request.getItems() != null) {
            request.getItems().forEach(itemDto -> {
                PackageItem item = mapItemRequestToEntity(itemDto);
                tripPackage.addItem(item);
            });
        }

        TripPackage savedPackage = packageRepository.save(tripPackage);
        return mapEntityToResponse(savedPackage);
    }

    @Transactional(readOnly = true)
    public PackageResponse getPackageById(Long id) {
        TripPackage tripPackage = packageRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Package not found with id: " + id));
        return mapEntityToResponse(tripPackage);
    }

    @Transactional(readOnly = true)
    public Page<PackageResponse> searchPackages(Pageable pageable) {
        // Simple search for now, can be extended with Specifications for filtering
        return packageRepository.findByIsActiveTrue(pageable)
                .map(this::mapEntityToResponse);
    }

    @Transactional(readOnly = true)
    public List<PackageResponse> getFeaturedPackages() {
        return packageRepository.findByIsFeaturedTrueAndIsActiveTrue().stream()
                .map(this::mapEntityToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PackageResponse updatePackage(Long id, PackageRequest request) {
        TripPackage existingPackage = packageRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Package not found with id: " + id));

        // Overwrite fields
        mapRequestToEntity(request, existingPackage);

        // Update items (simplified: clear and re-add)
        existingPackage.getItems().clear();
        if (request.getItems() != null) {
            request.getItems().forEach(itemDto -> {
                validateProvider(itemDto.getProviderType(), itemDto.getProviderId());
                PackageItem item = mapItemRequestToEntity(itemDto);
                existingPackage.addItem(item);
            });
        }

        TripPackage savedPackage = packageRepository.save(existingPackage);
        return mapEntityToResponse(savedPackage);
    }

    @Transactional
    public void deletePackage(Long id) {
        TripPackage existingPackage = packageRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Package not found with id: " + id));
        existingPackage.setIsActive(false);
        packageRepository.save(existingPackage);
    }

    private void validateProvider(ProviderType type, Long providerId) {
        try {
            switch (type) {
                case HOTEL -> hotelServiceClient.getHotelById(providerId);
                case TOUR_GUIDE -> tourGuideServiceClient.getTourGuideById(providerId);
                case VEHICLE -> vehicleServiceClient.getVehicleById(providerId);
            }
        } catch (Exception e) {
            log.error("Failed to validate provider {} with id {}", type, providerId, e);
            throw new IllegalArgumentException("Invalid provider: " + type + " ID: " + providerId);
        }
    }

    private void mapRequestToEntity(PackageRequest request, TripPackage entity) {
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setDurationDays(request.getDurationDays());
        entity.setBasePrice(request.getBasePrice());
        entity.setDiscountPercentage(
                request.getDiscountPercentage() != null ? request.getDiscountPercentage() : BigDecimal.ZERO);
        entity.setMaxParticipants(request.getMaxParticipants());
        entity.setDestinations(request.getDestinations());
        entity.setInclusions(request.getInclusions());
        entity.setExclusions(request.getExclusions());
        entity.setImages(request.getImages());
        entity.setIsFeatured(request.getIsFeatured() != null ? request.getIsFeatured() : false);
    }

    private PackageItem mapItemRequestToEntity(PackageItemRequest request) {
        PackageItem item = new PackageItem();
        item.setDayNumber(request.getDayNumber());
        item.setProviderType(request.getProviderType());
        item.setProviderId(request.getProviderId());
        item.setItemName(request.getItemName());
        item.setDescription(request.getDescription());
        item.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        return item;
    }

    private PackageResponse mapEntityToResponse(TripPackage entity) {
        BigDecimal discountMultiplier = BigDecimal.ONE
                .subtract(entity.getDiscountPercentage().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
        BigDecimal finalPrice = entity.getBasePrice().multiply(discountMultiplier).setScale(2, RoundingMode.HALF_UP);

        PackageResponse response = new PackageResponse();
        response.setId(entity.getId());
        response.setName(entity.getName());
        response.setDescription(entity.getDescription());
        response.setDurationDays(entity.getDurationDays());
        response.setBasePrice(entity.getBasePrice());
        response.setDiscountPercentage(entity.getDiscountPercentage());
        response.setFinalPrice(finalPrice);
        response.setMaxParticipants(entity.getMaxParticipants());
        response.setDestinations(entity.getDestinations());
        response.setInclusions(entity.getInclusions());
        response.setExclusions(entity.getExclusions());
        response.setImages(entity.getImages());
        response.setIsFeatured(entity.getIsFeatured());
        response.setIsActive(entity.getIsActive());
        response.setCreatedBy(entity.getCreatedBy());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        if (entity.getItems() != null) {
            response.setItems(
                    entity.getItems().stream().map(this::mapItemEntityToResponse).collect(Collectors.toList()));
        }
        return response;
    }

    private PackageItemResponse mapItemEntityToResponse(PackageItem item) {
        PackageItemResponse response = new PackageItemResponse();
        response.setId(item.getId());
        response.setDayNumber(item.getDayNumber());
        response.setProviderType(item.getProviderType() != null ? item.getProviderType().name() : null);
        response.setProviderId(item.getProviderId());
        response.setItemName(item.getItemName());
        response.setDescription(item.getDescription());
        response.setSortOrder(item.getSortOrder());
        response.setCreatedAt(item.getCreatedAt());
        return response;
    }
}
