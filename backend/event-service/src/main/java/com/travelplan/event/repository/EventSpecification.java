package com.travelplan.event.repository;

import com.travelplan.event.model.entity.Event;
import com.travelplan.event.model.enums.EventCategory;
import com.travelplan.event.model.enums.EventStatus;
import jakarta.persistence.criteria.Expression;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaPath;
import org.hibernate.query.criteria.JpaRoot;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;

/**
 * TASK-6.2: Composable JPA Specifications for Event filtering.
 * Includes semantic keyword search across the JSONB "vibes" column (STORY-6.2)
 * and cultural authenticity filter (STORY-6.1).
 */
public final class EventSpecification {

    private EventSpecification() {}

    public static Specification<Event> hasStatus(EventStatus status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Event> hasCategory(String category) {
        if (category == null) return null;
        try {
            EventCategory cat = EventCategory.valueOf(category.toUpperCase());
            return (root, query, cb) -> cb.equal(root.get("category"), cat);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static Specification<Event> locationContains(String location) {
        return (root, query, cb) ->
                location == null ? null
                        : cb.like(cb.lower(root.get("location")), "%" + location.toLowerCase() + "%");
    }

    public static Specification<Event> startDateFrom(LocalDate dateFrom) {
        return (root, query, cb) ->
                dateFrom == null ? null
                        : cb.greaterThanOrEqualTo(root.get("startDateTime"),
                        dateFrom.atStartOfDay().atOffset(ZoneOffset.UTC));
    }

    public static Specification<Event> startDateTo(LocalDate dateTo) {
        return (root, query, cb) ->
                dateTo == null ? null
                        : cb.lessThanOrEqualTo(root.get("startDateTime"),
                        dateTo.atTime(23, 59, 59).atOffset(ZoneOffset.UTC));
    }

    public static Specification<Event> minPrice(Double minPrice) {
        return (root, query, cb) ->
                minPrice == null ? null
                        : cb.greaterThanOrEqualTo(root.get("ticketPrice"), BigDecimal.valueOf(minPrice));
    }

    public static Specification<Event> maxPrice(Double maxPrice) {
        return (root, query, cb) ->
                maxPrice == null ? null
                        : cb.lessThanOrEqualTo(root.get("ticketPrice"), BigDecimal.valueOf(maxPrice));
    }

    /**
     * STORY-6.1: Filter events flagged as verified cultural heritage.
     * Maps to: WHERE is_authentic_cultural = TRUE
     */
    public static Specification<Event> isAuthenticCultural(Boolean cultural) {
        return (root, query, cb) ->
                (cultural == null || !cultural) ? null
                        : cb.isTrue(root.get("authenticCultural"));
    }

    /**
     * TASK-6.2 / STORY-6.2: Semantic keyword search across the JSONB "vibes" array.
     * Uses Hibernate 6's HibernateCriteriaBuilder.cast() (via JpaRoot) to generate
     * a proper SQL CAST of the JSONB column to varchar, then applies case-insensitive LIKE.
     * Supports mood-based tags such as "Underground", "Intimate", "Beach-Vibe", etc.
     *
     * Generated SQL: LOWER(CAST(vibes AS varchar)) LIKE '%beach-vibe%'
     */
    public static Specification<Event> hasVibe(String vibe) {
        if (vibe == null || vibe.isBlank()) return null;
        return (root, query, cb) -> {
            // JpaRoot.get() returns JpaPath<T> which extends JpaExpression<T>,
            // satisfying HibernateCriteriaBuilder.cast(JpaExpression<T>, Class<X>) signature
            HibernateCriteriaBuilder hcb = (HibernateCriteriaBuilder) cb;
            JpaPath<Object> vibesPath = ((JpaRoot<Event>) root).get("vibes");
            Expression<String> vibesText = hcb.cast(vibesPath, String.class);
            return cb.like(cb.lower(vibesText), "%" + vibe.trim().toLowerCase() + "%");
        };
    }

    /**
     * Convenience builder: combine all browse-published filters (always PUBLISHED).
     */
    public static Specification<Event> browsePublished(
            String category, String location, LocalDate dateFrom, LocalDate dateTo,
            Double minPrice, Double maxPrice, String vibe, Boolean authenticCultural) {
        return Specification
                .where(hasStatus(EventStatus.PUBLISHED))
                .and(hasCategory(category))
                .and(locationContains(location))
                .and(startDateFrom(dateFrom))
                .and(startDateTo(dateTo))
                .and(minPrice(minPrice))
                .and(maxPrice(maxPrice))
                .and(hasVibe(vibe))
                .and(isAuthenticCultural(authenticCultural));
    }

    /**
     * Convenience builder: combine all admin/organizer search filters.
     */
    public static Specification<Event> search(
            String category, String location, LocalDate dateFrom, LocalDate dateTo,
            Double minPrice, Double maxPrice, EventStatus status, String vibe, Boolean authenticCultural) {
        return Specification
                .where(hasStatus(status))
                .and(hasCategory(category))
                .and(locationContains(location))
                .and(startDateFrom(dateFrom))
                .and(startDateTo(dateTo))
                .and(minPrice(minPrice))
                .and(maxPrice(maxPrice))
                .and(hasVibe(vibe))
                .and(isAuthenticCultural(authenticCultural));
    }
}
