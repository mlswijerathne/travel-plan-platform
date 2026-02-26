# Phase 4: Decision Tree Mapping - Implementation Specifications

**Project:** Travel Plan Web Application - Microservices Architecture
**Date:** 2026-01-30
**Technique:** Decision Tree Mapping
**Purpose:** Create concrete implementation specifications, API contracts, and service communication patterns

---

## Executive Summary

This document provides implementation-ready specifications derived from the brainstorming session. It includes decision flow diagrams, API contracts, DTO specifications, and the complete service dependency matrix.

**Deliverables:**
- 6 Core Decision Trees (major system flows)
- Complete API Endpoint Map (30+ endpoints)
- DTO/Contract Specifications
- Service Dependency Matrix
- Inter-Service Communication Patterns

---

## 1. Decision Trees

### 1.1 Tourist Trip Planning Flow

**Entry Point:** Tourist opens application

```
                    TOURIST OPENS APP
                           │
                           ▼
                ┌──────────────────────┐
                │ Has account?          │
                └──────────┬───────────┘
                           │
              ┌────────────┴────────────┐
              │                         │
              ▼                         ▼
         ┌────────┐               ┌────────────┐
         │  YES   │               │    NO      │
         └───┬────┘               └─────┬──────┘
             │                          │
             ▼                          ▼
      ┌─────────────┐           ┌─────────────────┐
      │    LOGIN    │           │   REGISTER      │
      │ Auth Service│           │ Tourist Mgmt    │
      └──────┬──────┘           │ + Auth Service  │
             │                  └────────┬────────┘
             │                           │
             └───────────┬───────────────┘
                         │
                         ▼
              ┌──────────────────────┐
              │ What does user want? │
              └──────────┬───────────┘
                         │
        ┌────────────────┼────────────────┐
        │                │                │
        ▼                ▼                ▼
   ┌─────────┐    ┌───────────┐    ┌───────────┐
   │ BROWSE  │    │  CUSTOM   │    │  CHAT     │
   │PACKAGES │    │   PLAN    │    │  WITH AI  │
   └────┬────┘    └─────┬─────┘    └─────┬─────┘
        │               │                │
        ▼               ▼                ▼
   Trip Plan       AI Agent          AI Agent
   Service         Service           Service
```

**Services Involved:**
- Auth Service (authentication)
- Tourist Management Service (registration, profile)
- Trip Plan Service (package browsing)
- AI Agent Service (custom planning, chat)

**Key Decision Points:**
1. Account existence check
2. User intent determination (browse vs custom vs chat)

---

### 1.2 AI Agent Recommendation Flow

**Entry Point:** Tourist requests recommendations (e.g., "Find hotels in Mirissa")

```
            TOURIST: "Find hotels in Mirissa"
                           │
                           ▼
              ┌────────────────────────┐
              │      AI AGENT          │
              │  Parse user intent     │
              └────────────┬───────────┘
                           │
                           ▼
              ┌────────────────────────┐
              │ Query Hotel Service    │
              │ GET /hotels?location=  │
              │     mirissa            │
              └────────────┬───────────┘
                           │
                           ▼
              ┌────────────────────────┐
              │ Results count?         │
              └────────────┬───────────┘
                           │
         ┌─────────────────┼─────────────────┐
         │                 │                 │
         ▼                 ▼                 ▼
    ┌─────────┐      ┌──────────┐     ┌──────────┐
    │  >= 3   │      │  1 or 2  │     │    0     │
    │ results │      │ results  │     │ results  │
    └────┬────┘      └─────┬────┘     └─────┬────┘
         │                 │                │
         ▼                 ▼                ▼
    Return           Hybrid Fill:      Fallback:
    Internal         Internal +        Query Cache
    Only             External          (Google Data)
                           │                │
                           ▼                ▼
              ┌────────────────────────────────────┐
              │      NORMALIZE TO DTO              │
              │                                    │
              │  {                                 │
              │    id, name, location,             │
              │    isBookable: true/false,         │
              │    bookingSource: INTERNAL/EXT,    │
              │    rating: { source, score },      │
              │    pricing: { amount, currency }   │
              │  }                                 │
              └────────────────────────────────────┘
                           │
                           ▼
              ┌────────────────────────┐
              │ Enrich with Reviews    │
              │ GET /reviews/ratings?  │
              │   entityIds=h1,h2,h3   │
              └────────────┬───────────┘
                           │
                           ▼
                    RETURN TO TOURIST
```

**Internal-First Fallback Pattern:**
1. Query internal database first (registered providers)
2. If results < 3, supplement with cached external data
3. If results = 0, use external cache only
4. Always normalize to unified DTO format
5. Enrich with Review Service ratings

**Services Involved:**
- AI Agent Service (orchestration)
- Hotel/Guide/Vehicle Services (provider data)
- Review Service (ratings enrichment)
- Cache Database (external API data)

---

### 1.3 Booking Flow

**Entry Point:** Tourist clicks "Book Now"

```
         TOURIST CLICKS "BOOK NOW"
                    │
                    ▼
         ┌──────────────────────┐
         │ What type of booking?│
         └──────────┬───────────┘
                    │
      ┌─────────────┼─────────────┐
      │             │             │
      ▼             ▼             ▼
 ┌─────────┐  ┌──────────┐  ┌──────────┐
 │ SINGLE  │  │ PACKAGE  │  │  EVENT   │
 │  ITEM   │  │          │  │  TICKET  │
 └────┬────┘  └─────┬────┘  └─────┬────┘
      │             │             │
      ▼             ▼             ▼
   Booking      Trip Plan      Event
   Service       Service       Service
      │             │             │
      │             ▼             │
      │    ┌─────────────────┐    │
      │    │ Pre-Check ALL   │    │
      │    │ Availability    │    │
      │    └────────┬────────┘    │
      │             │             │
      │        ┌────┴────┐        │
      │        ▼         ▼        │
      │    ALL OK?    ANY FAIL?   │
      │        │         │        │
      │        ▼         ▼        │
      │    Continue   Return      │
      │    Booking    Error +     │
      │               Alternatives│
      │             │             │
      └─────────────┼─────────────┘
                    │
                    ▼
         ┌──────────────────────┐
         │   BOOKING SERVICE    │
         │                      │
         │ 1. Create booking    │
         │ 2. Reserve inventory │
         │ 3. Calculate price   │
         │ 4. Generate confirm# │
         └──────────┬───────────┘
                    │
                    ▼
         ┌──────────────────────┐
         │  ITINERARY SERVICE   │
         │                      │
         │ 1. Add to trip       │
         │ 2. Update schedule   │
         │ 3. Check conflicts   │
         └──────────┬───────────┘
                    │
                    ▼
         ┌──────────────────────┐
         │    EMAIL SERVICE     │
         │                      │
         │ Send confirmation    │
         └──────────────────────┘
```

**Package Booking Special Flow:**
1. Pre-check ALL components availability before booking
2. If any component fails, return error with alternatives
3. Only proceed if 100% availability confirmed
4. Create all bookings in single transaction (Saga pattern)

**Services Involved:**
- Booking Service (transaction management)
- Trip Plan Service (package coordination)
- Event Service (ticket booking)
- Provider Services (availability check)
- Itinerary Service (schedule update)
- Email Service (confirmation)

---

### 1.4 Review Collection Flow

**Entry Point:** Trip end date passes (scheduled job)

```
         TRIP END DATE PASSES
                    │
                    ▼
         ┌──────────────────────┐
         │   ITINERARY SERVICE  │
         │   Scheduled Job      │
         │   (Daily 9 AM)       │
         └──────────┬───────────┘
                    │
                    ▼
         ┌──────────────────────┐
         │ Find trips where:    │
         │ endDate < today AND  │
         │ reviewRequestSent    │
         │ = false              │
         └──────────┬───────────┘
                    │
                    ▼
         ┌──────────────────────┐
         │ For each trip:       │
         │ Get all bookings     │
         └──────────┬───────────┘
                    │
                    ▼
         ┌──────────────────────┐
         │   REVIEW SERVICE     │
         │                      │
         │ Create PENDING       │
         │ review shells for:   │
         │ - Each hotel         │
         │ - Each guide         │
         │ - Each vehicle       │
         │ - Each event         │
         │ - The trip plan      │
         └──────────┬───────────┘
                    │
                    ▼
         ┌──────────────────────┐
         │    EMAIL SERVICE     │
         │                      │
         │ POST /emails/        │
         │   review-request     │
         │                      │
         │ Payload:             │
         │ - touristId          │
         │ - touristEmail       │
         │ - tripId             │
         │ - reviewableItems[]  │
         └──────────┬───────────┘
                    │
                    ▼
         ┌──────────────────────┐
         │ Update trip:         │
         │ reviewRequestSent    │
         │ = true               │
         └──────────────────────┘
```

**Scheduled Job Specification:**
```java
@Scheduled(cron = "0 0 9 * * *")  // Every day 9 AM
public void checkCompletedTrips() {
    List<Trip> completed = tripRepo
        .findByEndDateBeforeAndReviewRequestSentFalse(LocalDate.now());

    for (Trip trip : completed) {
        List<Booking> bookings = bookingService.getByTripId(trip.getId());
        reviewService.createPendingReviews(trip.getId(), bookings);
        emailService.sendReviewRequest(trip);
        trip.setReviewRequestSent(true);
        tripRepo.save(trip);
    }
}
```

---

### 1.5 Review → Provider Rating Sync Flow

**Entry Point:** New review submitted

```
         NEW REVIEW SUBMITTED
                    │
                    ▼
         ┌──────────────────────┐
         │   REVIEW SERVICE     │
         │                      │
         │ 1. Save review       │
         │ 2. Calculate new     │
         │    aggregate rating  │
         └──────────┬───────────┘
                    │
                    ▼
         ┌──────────────────────┐
         │ Determine entity     │
         │ type                 │
         └──────────┬───────────┘
                    │
     ┌──────────────┼──────────────┬──────────────┐
     │              │              │              │
     ▼              ▼              ▼              ▼
  ┌──────┐    ┌──────────┐   ┌─────────┐   ┌──────────┐
  │HOTEL │    │TOUR_GUIDE│   │ VEHICLE │   │TRIP_PLAN │
  └──┬───┘    └────┬─────┘   └────┬────┘   └────┬─────┘
     │             │              │              │
     ▼             ▼              ▼              ▼
  Hotel        Tour Guide     Vehicle       Trip Plan
  Service      Service        Service       Service
     │             │              │              │
     └─────────────┴──────────────┴──────────────┘
                         │
                         ▼
              ┌──────────────────────┐
              │  PUT /providers/     │
              │    {id}/rating       │
              │                      │
              │  Payload:            │
              │  - newRating: 4.5    │
              │  - reviewCount: 48   │
              │  - lastReviewAt:     │
              │    2026-01-30        │
              └──────────────────────┘
```

**Rating Update Logic:**
```java
public void updateProviderRating(String entityType, String entityId) {
    // Calculate new aggregate
    RatingAggregate agg = reviewRepo.calculateAggregate(entityType, entityId);

    // Determine target service
    String serviceUrl = switch (entityType) {
        case "HOTEL" -> hotelServiceUrl;
        case "TOUR_GUIDE" -> guideServiceUrl;
        case "VEHICLE" -> vehicleServiceUrl;
        case "TRIP_PLAN" -> tripPlanServiceUrl;
        default -> throw new IllegalArgumentException();
    };

    // Push update
    restTemplate.put(serviceUrl + "/" + entityId + "/rating",
        new RatingUpdate(agg.getAverage(), agg.getCount(), LocalDateTime.now()));
}
```

---

### 1.6 Cancellation Flow with Refund

**Entry Point:** Tourist requests cancellation

```
         TOURIST REQUESTS CANCELLATION
                    │
                    ▼
         ┌──────────────────────┐
         │   BOOKING SERVICE    │
         │                      │
         │ 1. Validate booking  │
         │ 2. Check trip status │
         └──────────┬───────────┘
                    │
                    ▼
         ┌──────────────────────┐
         │ Trip already started?│
         └──────────┬───────────┘
                    │
         ┌──────────┴──────────┐
         │                     │
         ▼                     ▼
     ┌───────┐            ┌─────────┐
     │  YES  │            │   NO    │
     └───┬───┘            └────┬────┘
         │                     │
         ▼                     ▼
    REJECT:               Calculate days
    "Cannot cancel        until trip
    active trip"               │
                               ▼
                    ┌──────────────────────┐
                    │ For each booking:    │
                    │ Get cancellation     │
                    │ policy from provider │
                    └──────────┬───────────┘
                               │
                               ▼
                    ┌──────────────────────┐
                    │ Calculate refund:    │
                    │                      │
                    │ if days >= 7:        │
                    │   100% refund        │
                    │ if days >= 3:        │
                    │   50% refund         │
                    │ if days < 3:         │
                    │   0% refund          │
                    └──────────┬───────────┘
                               │
                               ▼
                    ┌──────────────────────┐
                    │ Cancel all bookings  │
                    │ in provider services │
                    └──────────┬───────────┘
                               │
                               ▼
                    ┌──────────────────────┐
                    │ TOURIST MANAGEMENT   │
                    │                      │
                    │ Add credit:          │
                    │ POST /tourists/      │
                    │   {id}/wallet/credit │
                    └──────────┬───────────┘
                               │
                               ▼
                    ┌──────────────────────┐
                    │ ITINERARY SERVICE    │
                    │                      │
                    │ Mark trip CANCELLED  │
                    └──────────┬───────────┘
                               │
                               ▼
                    ┌──────────────────────┐
                    │    EMAIL SERVICE     │
                    │                      │
                    │ Send cancellation    │
                    │ confirmation with    │
                    │ refund details       │
                    └──────────────────────┘
```

**Cancellation Policy Matrix:**

| Days Before Trip | Refund Percentage | Policy Name |
|------------------|-------------------|-------------|
| >= 7 days | 100% | Full Refund |
| 3-6 days | 50% | Partial Refund |
| < 3 days | 0% | No Refund |

---

## 2. DTO Specifications

### 2.1 RecommendationDTO

```java
public class RecommendationDTO {
    // Identity
    private String id;
    private String type;  // HOTEL, GUIDE, VEHICLE, EVENT
    private String name;
    private String description;

    // Location
    private Location location;

    // Booking info
    private boolean isBookable;
    private String bookingSource;  // INTERNAL, EXTERNAL
    private String internalId;     // null if external
    private String externalUrl;    // null if internal

    // Pricing
    private PricingInfo pricing;

    // Rating
    private RatingInfo rating;

    // Media
    private List<String> images;

    // Metadata
    private Map<String, Object> metadata;
}

public class Location {
    private String city;
    private String region;
    private String country;
    private Double latitude;
    private Double longitude;
}

public class PricingInfo {
    private BigDecimal amount;
    private String currency;
    private String period;  // per_night, per_day, per_person
    private Integer priceLevel;  // 1-4 for external (Google)
}

public class RatingInfo {
    private String source;  // INTERNAL, GOOGLE
    private Double score;
    private Integer reviewCount;
}
```

### 2.2 BookingRequest / BookingResponse

```java
// REQUEST
public class BookingRequest {
    private String touristId;
    private String entityType;  // HOTEL, GUIDE, VEHICLE, EVENT
    private String entityId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer quantity;
    private String tripId;  // optional, links to itinerary
    private Map<String, Object> options;  // room type, etc.
}

// RESPONSE
public class BookingResponse {
    private String bookingId;
    private String status;  // CONFIRMED, PENDING, FAILED
    private String confirmationNumber;
    private BigDecimal totalPrice;
    private String currency;
    private LocalDateTime expiresAt;  // for pending bookings
    private List<String> failureReasons;  // if failed
    private BookingDetails details;
}

public class BookingDetails {
    private String entityType;
    private String entityName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String location;
    private Map<String, Object> specifics;
}
```

### 2.3 Review Schema

```java
public class Review {
    // Identity
    private String reviewId;
    private String tripId;
    private String touristId;

    // Target
    private String entityType;  // HOTEL, GUIDE, VEHICLE, EVENT, TRIP_PLAN, PRODUCT
    private String entityId;

    // Content
    private Integer rating;  // 1-5, null if not yet rated
    private String title;
    private String text;
    private List<String> photoUrls;

    // Metadata
    private String status;  // PENDING, PARTIAL, COMPLETE
    private String capturedAt;  // LOCATION_EXIT, POST_TRIP, IN_APP
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    // Engagement
    private Integer helpfulVotes;
    private String ownerResponse;
    private LocalDateTime ownerResponseAt;
}
```

### 2.4 CancellationResponse

```java
public class CancellationResponse {
    private String cancellationId;
    private String tripId;
    private String status;  // CANCELLED, PARTIAL, REJECTED

    // Financial
    private BigDecimal totalPaid;
    private BigDecimal totalRefund;
    private BigDecimal cancellationFee;
    private String refundMethod;  // ACCOUNT_CREDIT

    // Details
    private List<CancellationItem> items;

    // Next steps
    private String message;
    private BigDecimal newWalletBalance;
}

public class CancellationItem {
    private String entityType;
    private String entityName;
    private BigDecimal amountPaid;
    private BigDecimal refundAmount;
    private String policyApplied;  // "50% refund (3-7 days)"
}
```

### 2.5 RatingUpdateEvent

```java
public class RatingUpdateEvent {
    private String entityType;
    private String entityId;
    private Double newAverageRating;
    private Integer totalReviewCount;
    private LocalDateTime lastReviewAt;

    // Trend data
    private Double ratingChange;  // +0.1 or -0.2
    private String trend;  // IMPROVING, DECLINING, STABLE
}
```

---

## 3. API Endpoint Specification

### 3.1 Authentication Service

| Endpoint | Method | Description | Request | Response |
|----------|--------|-------------|---------|----------|
| `/auth/login` | POST | Authenticate user | `{email, password}` | `{token, user}` |
| `/auth/register` | POST | Register new user | `{email, password, role}` | `{token, user}` |
| `/auth/refresh` | POST | Refresh token | `{refreshToken}` | `{token}` |
| `/auth/logout` | POST | Invalidate token | `{token}` | `{success}` |

### 3.2 Tourist Management Service

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/tourists/{id}` | GET | Get tourist profile |
| `/tourists/{id}` | PUT | Update tourist profile |
| `/tourists/{id}/preferences` | GET | Get preferences |
| `/tourists/{id}/preferences` | PUT | Update preferences |
| `/tourists/{id}/wallet` | GET | Get wallet balance |
| `/tourists/{id}/wallet/credit` | POST | Add credit to wallet |
| `/tourists/{id}/trips` | GET | Get trip history |

### 3.3 Provider Services (Hotel, Tour Guide, Vehicle)

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/hotels` | GET | Search hotels (query params: location, dates, guests) |
| `/hotels/{id}` | GET | Get hotel details |
| `/hotels/{id}/availability` | GET | Check availability |
| `/hotels/{id}/rating` | PUT | Update rating (from Review Service) |
| `/hotels` | POST | Register new hotel (owner) |
| `/hotels/{id}` | PUT | Update hotel (owner) |

*Same pattern for `/guides` and `/vehicles`*

### 3.4 Event Management Service

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/events` | GET | Search events (query params: location, dates, tags) |
| `/events/{id}` | GET | Get event details |
| `/events` | POST | Create event (admin only) |
| `/events/{id}` | PUT | Update event (admin only) |
| `/events/{id}` | DELETE | Delete event (admin only) |

### 3.5 Booking Service

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/bookings` | POST | Create new booking |
| `/bookings/{id}` | GET | Get booking details |
| `/bookings/{id}/cancel` | POST | Cancel booking |
| `/bookings/trip/{tripId}` | GET | Get all bookings for trip |
| `/bookings/availability` | POST | Batch availability check |

### 3.6 Trip Plan (Packages) Service

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/packages` | GET | Search packages (query params: tags, duration, budget) |
| `/packages/{id}` | GET | Get package details |
| `/packages/{id}/availability` | GET | Check package availability |
| `/packages/{id}/book` | POST | Book entire package |
| `/packages` | POST | Create package (admin only) |
| `/packages/{id}/rating` | PUT | Update package rating |

### 3.7 Itinerary Service

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/itineraries/{tripId}` | GET | Get full itinerary |
| `/itineraries/{tripId}/items` | POST | Add item to itinerary |
| `/itineraries/{tripId}/items/{itemId}` | PUT | Update item |
| `/itineraries/{tripId}/items/{itemId}` | DELETE | Remove item |
| `/itineraries/{tripId}/status` | PUT | Update trip status |
| `/itineraries/{tripId}/download` | GET | Download offline package |

### 3.8 Review Service

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/reviews` | POST | Submit new review |
| `/reviews/{id}` | GET | Get review details |
| `/reviews/{id}` | PUT | Update review |
| `/reviews/ratings` | GET | Batch get ratings (query: entityIds) |
| `/reviews/entity/{type}/{id}` | GET | Get reviews for entity |
| `/reviews/pending/{touristId}` | GET | Get pending reviews |
| `/reviews/tourist/{touristId}` | GET | Get tourist's reviews |

### 3.9 E-Commerce Service

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/products` | GET | Search products (query: tripId, location, eventId) |
| `/products/{id}` | GET | Get product details |
| `/orders` | POST | Create order |
| `/orders/{id}` | GET | Get order details |
| `/orders/{id}/status` | PUT | Update order status |
| `/orders/tourist/{touristId}` | GET | Get tourist's orders |

### 3.10 Email Service

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/emails/review-request` | POST | Send review request email |
| `/emails/confirmation` | POST | Send booking confirmation |
| `/emails/cancellation` | POST | Send cancellation confirmation |
| `/emails/reminder` | POST | Send trip reminder |

### 3.11 AI Agent Service

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/chat` | POST | Send message to AI |
| `/chat/{sessionId}` | GET | Get chat history |
| `/recommend` | POST | Get recommendations |
| `/recommend/packages` | POST | Get matching packages |
| `/plan/generate` | POST | Generate custom trip plan |

---

## 4. Service Dependency Matrix

```
                           CALLS TO →

          Auth Tour Hotel Guide Veh Event Book Itin Rev E-Com Email Trip AI
        ┌─────┬─────┬─────┬─────┬────┬─────┬────┬────┬────┬─────┬─────┬────┬────┐
Auth    │  -  │     │     │     │    │     │    │    │    │     │     │    │    │
Tourist │  ✓  │  -  │     │     │    │     │    │    │    │     │     │    │    │
Hotel   │  ✓  │     │  -  │     │    │     │    │    │ ←  │     │     │    │    │
Guide   │  ✓  │     │     │  -  │    │     │    │    │ ←  │     │     │    │    │
Vehicle │  ✓  │     │     │     │  - │     │    │    │ ←  │     │     │    │    │
Event   │  ✓  │     │     │     │    │  -  │    │    │    │     │     │    │    │
Booking │  ✓  │     │  ✓  │  ✓  │  ✓ │  ✓  │  - │  ✓ │    │     │     │  ✓ │    │
Itiner. │  ✓  │     │     │     │    │     │  ✓ │  - │  ✓ │     │  ✓  │    │    │
Review  │  ✓  │     │  →  │  →  │  → │     │    │    │  - │     │     │  → │    │
E-Comm  │  ✓  │     │     │     │    │     │    │  ✓ │    │  -  │     │    │    │
Email   │     │     │     │     │    │     │    │    │    │     │  -  │    │    │
TripPlan│  ✓  │     │  ✓  │  ✓  │  ✓ │  ✓  │  ✓ │  ✓ │ ←  │     │     │  - │    │
AI Agent│  ✓  │  ✓  │  ✓  │  ✓  │  ✓ │  ✓  │    │    │  ✓ │     │     │  ✓ │  - │

Legend:
✓ = Queries (synchronous REST call)
→ = Notifies (pushes data to)
← = Receives notifications from
```

### Dependency Summary by Service

| Service | Depends On | Depended By |
|---------|------------|-------------|
| **Auth** | - | All services |
| **Tourist Mgmt** | Auth | AI Agent, Booking |
| **Hotel** | Auth | AI Agent, Booking, Trip Plan |
| **Tour Guide** | Auth | AI Agent, Booking, Trip Plan |
| **Vehicle** | Auth | AI Agent, Booking, Trip Plan |
| **Event** | Auth | AI Agent, Booking |
| **Booking** | Auth, All Providers, Itinerary, Trip Plan | - |
| **Itinerary** | Auth, Booking, Review, Email | E-Commerce |
| **Review** | Auth | Hotel, Guide, Vehicle, Trip Plan |
| **E-Commerce** | Auth, Itinerary | - |
| **Email** | - | Itinerary, Booking |
| **Trip Plan** | Auth, All Providers, Booking, Itinerary | AI Agent |
| **AI Agent** | Auth, Tourist, All Providers, Review, Trip Plan | - |

---

## 5. Communication Patterns

### 5.1 Synchronous (REST)

Used for: Data queries, real-time operations

```
AI Agent ──GET /hotels?location=mirissa──► Hotel Service
                                          │
                                          ▼
AI Agent ◄──200 OK + JSON response────────┘
```

### 5.2 Asynchronous (Notification)

Used for: Rating updates, status changes

```
Review Service ──POST /hotels/{id}/rating──► Hotel Service
                         │
                         │ (fire and forget, or async response)
                         ▼
              Hotel Service updates local cache
```

### 5.3 Scheduled Jobs

Used for: Time-based triggers

```java
// Itinerary Service
@Scheduled(cron = "0 0 9 * * *")
public void sendReviewRequests() { ... }

// External API Cache Sync
@Scheduled(cron = "0 0 23 * * *")
public void syncExternalData() { ... }
```

---

## Document Info

**Generated:** 2026-01-30
**Session:** Brainstorming - Microservices Integration Architecture
**Phase:** 4 of 4 (Action Planning)
**Ideas in Phase:** 8 (Ideas #72-79)
**Total Ideas:** 79
