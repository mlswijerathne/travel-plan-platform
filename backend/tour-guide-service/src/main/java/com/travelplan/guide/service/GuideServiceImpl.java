package com.travelplan.guide.service;

import com.travelplan.guide.domain.Availability;
import com.travelplan.guide.domain.Guide;
import com.travelplan.guide.dto.AvailabilityResponse;
import com.travelplan.guide.dto.GuideRequest;
import com.travelplan.guide.dto.GuideResponse;
import com.travelplan.guide.repository.AvailabilityRepository;
import com.travelplan.guide.repository.GuideRepository;
import com.travelplan.guide.Exception.ResourceNotFoundException;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GuideServiceImpl implements GuideService {

    private final GuideRepository guideRepository;
    private final AvailabilityRepository availabilityRepository;

    public GuideServiceImpl(GuideRepository guideRepository, AvailabilityRepository availabilityRepository) {
        this.guideRepository = guideRepository;
        this.availabilityRepository = availabilityRepository;
    }

    @Override
    @Transactional
    public GuideResponse registerGuide(String userId, GuideRequest request) {
        guideRepository.findByEmail(request.getEmail()).ifPresent(g -> {
            throw new IllegalStateException("Guide already registered with email: " + request.getEmail());
        });

        Guide guide = new Guide();
        mapRequestToGuide(request, guide);
        guide.setUserId(userId);

        Guide savedGuide = guideRepository.save(guide);
        return mapToResponse(savedGuide);
    }

    @Override
    public GuideResponse getGuide(Long id) {
        Guide guide = guideRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tour guide not found with id: " + id));
        return mapToResponse(guide);
    }

    @Override
    public GuideResponse getGuideByUserId(String userId) {
        Guide guide = guideRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Tour guide not found with user id: " + userId));
        return mapToResponse(guide);
    }

    @Override
    @Transactional
    public GuideResponse updateGuide(Long id, GuideRequest request) {
        Guide guide = guideRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tour guide not found with id: " + id));

        mapRequestToGuide(request, guide);
        Guide savedGuide = guideRepository.save(guide);
        return mapToResponse(savedGuide);
    }

    @Override
    @Transactional
    public GuideResponse updateGuideByUserId(String userId, GuideRequest request) {
        Guide guide = guideRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Tour guide not found with user id: " + userId));

        mapRequestToGuide(request, guide);
        Guide savedGuide = guideRepository.save(guide);
        return mapToResponse(savedGuide);
    }

    @Override
    @Transactional
    public void deleteGuide(Long id) {
        Guide guide = guideRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tour guide not found with id: " + id));
        guide.setActive(false);
        guideRepository.save(guide);
    }

    @Override
    public Page<GuideResponse> searchGuides(
            String language,
            String specialization,
            BigDecimal minRating,
            BigDecimal maxHourlyRate,
            BigDecimal maxDailyRate,
            Boolean isVerified,
            String query,
            Integer page,
            Integer size) {

        Specification<Guide> spec = (root, querySpec, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (language != null && !language.isEmpty()) {
                predicates.add(cb.isTrue(
                        cb.function("array_contains", Boolean.class, root.get("languages"), cb.literal(language))));
            }

            if (specialization != null && !specialization.isEmpty()) {
                predicates.add(cb.isTrue(cb.function("array_contains", Boolean.class, root.get("specializations"),
                        cb.literal(specialization))));
            }

            if (minRating != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("averageRating"), minRating));
            }

            if (maxHourlyRate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("hourlyRate"), maxHourlyRate));
            }

            if (maxDailyRate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("dailyRate"), maxDailyRate));
            }

            if (isVerified != null) {
                predicates.add(cb.equal(root.get("isVerified"), isVerified));
            }

            if (query != null && !query.isEmpty()) {
                String searchPattern = "%" + query.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("firstName")), searchPattern),
                        cb.like(cb.lower(root.get("lastName")), searchPattern),
                        cb.like(cb.lower(root.get("bio")), searchPattern)));
            }

            predicates.add(cb.isTrue(root.get("isActive")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        PageRequest pageRequest = PageRequest.of(page, size);
        return guideRepository.findAll(spec, pageRequest).map(this::mapToResponse);
    }

    @Override
    public AvailabilityResponse getAvailability(Long id, LocalDate startDate, LocalDate endDate) {
        Guide guide = guideRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tour guide not found with id: " + id));

        List<Availability> overrides = availabilityRepository.findByGuideAndAvailableDateBetween(guide, startDate,
                endDate);

        List<LocalDate> bookedDates = overrides.stream()
                .filter(a -> !a.isAvailable())
                .map(Availability::getAvailableDate)
                .distinct()
                .collect(Collectors.toList());

        boolean isAvailable = bookedDates.isEmpty();

        return new AvailabilityResponse(guide.getId(), isAvailable, guide.getDailyRate(), bookedDates);
    }

    private void mapRequestToGuide(GuideRequest request, Guide guide) {
        guide.setFirstName(request.getFirstName());
        guide.setLastName(request.getLastName());
        guide.setEmail(request.getEmail());
        guide.setPhoneNumber(request.getPhoneNumber());
        guide.setBio(request.getBio());
        guide.setLanguages(request.getLanguages());
        guide.setSpecializations(request.getSpecializations());
        guide.setExperienceYears(request.getExperienceYears());
        guide.setHourlyRate(request.getHourlyRate());
        guide.setDailyRate(request.getDailyRate());
        guide.setProfileImageUrl(request.getProfileImageUrl());
    }

    private GuideResponse mapToResponse(Guide guide) {
        GuideResponse response = new GuideResponse();
        response.setId(guide.getId());
        response.setUserId(guide.getUserId());
        response.setFirstName(guide.getFirstName());
        response.setLastName(guide.getLastName());
        response.setEmail(guide.getEmail());
        response.setPhoneNumber(guide.getPhoneNumber());
        response.setBio(guide.getBio());
        response.setLanguages(guide.getLanguages());
        response.setSpecializations(guide.getSpecializations());
        response.setExperienceYears(guide.getExperienceYears());
        response.setHourlyRate(guide.getHourlyRate());
        response.setDailyRate(guide.getDailyRate());
        response.setAverageRating(guide.getAverageRating());
        response.setReviewCount(guide.getReviewCount());
        response.setProfileImageUrl(guide.getProfileImageUrl());
        response.setVerified(guide.getVerified());
        response.setActive(guide.getActive());
        response.setCreatedAt(guide.getCreatedAt());
        response.setUpdatedAt(guide.getUpdatedAt());
        return response;
    }
}
