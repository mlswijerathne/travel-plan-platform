package com.travelplan.event.service.impl;

import com.travelplan.event.dto.EventSummaryResponse;
import com.travelplan.event.dto.RegionalPulseResponse;
import com.travelplan.event.dto.RouteEventResult;
import com.travelplan.event.dto.RouteSearchRequest;
import com.travelplan.event.mapper.EventMapper;
import com.travelplan.event.model.entity.Event;
import com.travelplan.event.repository.EventRepository;
import com.travelplan.event.service.GeospatialService;
import com.travelplan.event.util.GeoUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.AbstractMap;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeospatialServiceImpl implements GeospatialService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    // -------------------------------------------------------------------------
    // TASK-7.1 — Route-Based Event Finder
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<RouteEventResult> findEventsAlongRoute(RouteSearchRequest req) {

        double sLat = req.getStartLat();
        double sLng = req.getStartLng();
        double eLat = req.getEndLat();
        double eLng = req.getEndLng();
        double radiusKm = req.getRadiusKm() != null ? req.getRadiusKm() : 50.0;

        log.info("Route search: ({},{}) → ({},{}) corridor={}km",
                sLat, sLng, eLat, eLng, radiusKm);

        // --- Step 1: cheap bounding-box pre-filter in SQL ------------------
        double padDeg = GeoUtils.kmToDegrees(radiusKm);
        double minLat = Math.min(sLat, eLat) - padDeg;
        double maxLat = Math.max(sLat, eLat) + padDeg;
        double minLng = Math.min(sLng, eLng) - padDeg;
        double maxLng = Math.max(sLng, eLng) + padDeg;

        OffsetDateTime dateFrom = req.getDateFrom() != null
                ? req.getDateFrom().atStartOfDay().atOffset(ZoneOffset.UTC) : null;
        OffsetDateTime dateTo = req.getDateTo() != null
                ? req.getDateTo().atTime(23, 59, 59).atOffset(ZoneOffset.UTC) : null;

        List<Event> candidates = eventRepository.findPublishedInBoundingBox(
                minLat, maxLat, minLng, maxLng);

        log.info("Route bounding-box candidates: {}", candidates.size());

        // --- Step 2: accurate point-to-segment Haversine filter + date range in Java ---
        return candidates.stream()
                .filter(e -> e.getLatitude() != null && e.getLongitude() != null)
                .filter(e -> dateFrom == null || !e.getStartDateTime().isBefore(dateFrom))
                .filter(e -> dateTo   == null || !e.getStartDateTime().isAfter(dateTo))
                .map(e -> {
                    double pLat = e.getLatitude().doubleValue();
                    double pLng = e.getLongitude().doubleValue();
                    double dist = GeoUtils.pointToSegmentKm(pLat, pLng, sLat, sLng, eLat, eLng);
                    return new AbstractMap.SimpleEntry<>(e, dist);
                })
                .filter(entry -> entry.getValue() <= radiusKm)
                .sorted(Comparator.comparingDouble(AbstractMap.SimpleEntry::getValue))
                .map(entry -> toRouteResult(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    private RouteEventResult toRouteResult(Event e, double distanceKm) {
        return RouteEventResult.builder()
                .id(e.getId())
                .title(e.getTitle())
                .category(e.getCategory())
                .location(e.getLocation())
                .latitude(e.getLatitude())
                .longitude(e.getLongitude())
                .startDateTime(e.getStartDateTime())
                .endDateTime(e.getEndDateTime())
                .availableSeats(e.getAvailableSeats())
                .ticketPrice(e.getTicketPrice())
                .currency(e.getCurrency())
                .status(e.getStatus())
                .imageUrl(e.getImageUrl())
                .vibes(e.getVibes())
                .authenticCultural(e.isAuthenticCultural())
                .distanceFromRouteKm(round2(distanceKm))
                .build();
    }

    // -------------------------------------------------------------------------
    // TASK-7.2 — Regional Pulse Aggregation
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public RegionalPulseResponse getRegionalPulse(String district, double lat, double lng, double radiusKm) {

        log.info("Regional pulse: district={} centre=({},{}) radius={}km", district, lat, lng, radiusKm);

        List<Event> events = eventRepository.findPublishedWithinRadiusKm(lat, lng, radiusKm);

        log.info("Regional pulse: {} events found in {}km of {}", events.size(), radiusKm, district);

        // --- Aggregations ---------------------------------------------------

        int totalAvailableSeats = events.stream()
                .mapToInt(Event::getAvailableSeats)
                .sum();

        int eventsWithSeats = (int) events.stream()
                .filter(e -> e.getAvailableSeats() > 0)
                .count();

        BigDecimal avgPrice = events.isEmpty() ? null :
                events.stream()
                        .map(Event::getTicketPrice)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(events.size()), 2, RoundingMode.HALF_UP);

        BigDecimal minPrice = events.stream()
                .map(Event::getTicketPrice)
                .min(Comparator.naturalOrder())
                .orElse(null);

        BigDecimal maxPrice = events.stream()
                .map(Event::getTicketPrice)
                .max(Comparator.naturalOrder())
                .orElse(null);

        Map<String, Long> categoryBreakdown = events.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getCategory().name(),
                        Collectors.counting()
                ));

        long culturalCount = events.stream()
                .filter(Event::isAuthenticCultural)
                .count();

        // --- Upcoming preview (up to 5 soonest) ----------------------------
        List<EventSummaryResponse> upcoming = events.stream()
                .sorted(Comparator.comparing(Event::getStartDateTime))
                .limit(5)
                .map(eventMapper::toSummaryResponse)
                .collect(Collectors.toList());

        return RegionalPulseResponse.builder()
                .district(district)
                .centerLat(lat)
                .centerLng(lng)
                .radiusKm(radiusKm)
                .totalEvents(events.size())
                .totalAvailableSeats(totalAvailableSeats)
                .averageTicketPrice(avgPrice)
                .minTicketPrice(minPrice)
                .maxTicketPrice(maxPrice)
                .eventsWithSeatsAvailable(eventsWithSeats)
                .categoryBreakdown(categoryBreakdown)
                .authenticCulturalCount(culturalCount)
                .upcomingEvents(upcoming)
                .build();
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
