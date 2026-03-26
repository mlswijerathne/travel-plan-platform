 Travel Plan Platform - Viva Preparation Guide

## Table of Contents
1. [Architecture Overview](#1-architecture-overview)
2. [Common Library (common-lib)](#2-common-library-common-lib)
3. [Discovery Server](#3-discovery-server)
4. [Tourist Service](#4-tourist-service)
5. [Booking Service](#5-booking-service)
6. [Key Design Patterns & Concepts](#6-key-design-patterns--concepts)
7. [Potential Viva Questions & Answers](#7-potential-viva-questions--answers)

---

## 1. Architecture Overview

This is a **microservices-based travel planning platform** built with:

| Technology | Purpose |
|---|---|
| **Spring Boot 3.5.0** | Application framework (Java 21) |
| **Spring Cloud** | Microservice infrastructure (Eureka, OpenFeign) |
| **PostgreSQL** | Primary database (per-service databases) |
| **Apache Kafka** | Asynchronous event-driven communication |
| **Supabase** | Authentication provider (JWT) |
| **Azure Blob Storage** | Image/file storage |
| **Flyway** | Database version control (migrations) |
| **Docker** | Containerization |

### Service Port Mapping

| Service | Port | Responsibility |
|---|---|---|
| Discovery Server | 8761 | Service registry (Eureka) |
| Tourist Service | 8082 | Tourist profiles, wallet, preferences |
| Hotel Service | 8083 | Hotel listings & availability |
| Tour Guide Service | 8084 | Tour guide management |
| Vehicle Service | 8085 | Vehicle rentals |
| Booking Service | 8086 | Booking orchestration (Saga pattern) |
| Itinerary Service | 8087 | Trip itineraries |
| Review Service | 8088 | Reviews & ratings |
| Trip Plan Service | 8089 | Travel packages |
| AI Agent Service | 8093 | AI-powered chat assistant |

### How Services Communicate

```
                    +-----------------------+
                    |   Discovery Server    |
                    |   (Eureka - 8761)     |
                    +-----------+-----------+
                          |  register/discover
          +---------------+----------------+
          |               |                |
   +------+------+  +----+-----+  +-------+------+
   | Tourist     |  | Booking  |  | Hotel        |
   | Service     |  | Service  |  | Service      |
   | (8082)      |  | (8086)   |  | (8083)       |
   +------+------+  +----+-----+  +--------------+
          |               |
          |    Kafka       |   OpenFeign (sync)
          +<----- booking-events ----->+
```

- **Synchronous**: OpenFeign (HTTP client) for real-time calls (e.g., booking checks availability on hotel)
- **Asynchronous**: Apache Kafka for events (e.g., booking publishes refund event, tourist service credits wallet)
- **Service Discovery**: Eureka — services register themselves and discover others by name

---

## 2. Common Library (common-lib)

### Purpose
A **shared Maven module** that every microservice depends on. It provides reusable security, DTOs, exceptions, and configurations to avoid code duplication.

### Folder Structure
```
common-lib/src/main/java/com/travelplan/common/
├── config/
│   ├── CommonSecurityConfig.java    # Default Spring Security setup
│   ├── CorsConfig.java             # CORS configuration
│   ├── JwtValidationFilter.java    # Supabase JWT validation
│   └── OpenApiConfig.java          # Swagger/OpenAPI setup
├── dto/
│   ├── ApiResponse.java            # Generic API response wrapper
│   ├── ErrorResponse.java          # Standard error format
│   ├── PaginatedResponse.java      # Paginated list wrapper
│   └── RatingUpdateEvent.java      # Rating event structure
├── exception/
│   ├── GlobalExceptionHandler.java # Central error handler
│   ├── ResourceNotFoundException.java  # 404
│   ├── ValidationException.java        # 400
│   ├── UnauthorizedException.java      # 401
│   ├── ForbiddenException.java         # 403
│   ├── ConflictException.java          # 409
│   └── ServiceUnavailableException.java # 503
└── storage/
    ├── AzureBlobStorageService.java    # Azure Blob image upload/delete
    └── ImageUploadController.java      # REST API for image management
```

### Key Components Explained

#### 2.1 JwtValidationFilter (Security Core)

This is the **most critical shared component**. It validates every incoming request's JWT token.

**How it works:**
1. Extracts `Bearer <token>` from the `Authorization` header
2. Tries **ES256** verification first (fetches public key from Supabase JWKS endpoint)
3. Falls back to **HS256** (HMAC with shared secret) for legacy tokens
4. Extracts `subject` (user ID) and `app_metadata.role` from the JWT claims
5. Creates a Spring Security `Authentication` object with the user ID and role

**Security decision — why only `app_metadata`?**
- `user_metadata` is writable by any authenticated user via Supabase client SDK — an attacker could set their own role to "ADMIN"
- `app_metadata` is only writable by the service role (admin API) — trustworthy source
- A database trigger copies role into `app_metadata` at signup time

```java
// Simplified flow:
String token = authHeader.substring(7);          // Remove "Bearer "
Claims claims = parseToken(token);                // Verify signature
String userId = claims.getSubject();              // Supabase UUID
String role = extractAppRole(claims);             // From app_metadata ONLY
// Create Spring Security authentication
Authentication auth = new UsernamePasswordAuthenticationToken(userId, token, authorities);
SecurityContextHolder.getContext().setAuthentication(auth);
```

**Skipped paths** (no JWT needed): `/actuator/**`, `/health`, `/error`, `/swagger-ui/**`, `/api/public/**`

#### 2.2 CommonSecurityConfig

Provides a **default** security filter chain that all services inherit:
- **Stateless** sessions (no server-side session — JWT is the session)
- CSRF disabled (not needed for stateless APIs)
- JWT filter runs before Spring's default authentication filter
- Uses `@ConditionalOnMissingBean` — services can **override** this by defining their own `SecurityFilterChain` bean

#### 2.3 ApiResponse<T> — Standard Response Wrapper

Every API response follows this structure:
```json
{
  "data": { ... },          // The actual payload (generic type T)
  "meta": {
    "timestamp": "2026-03-07T10:30:00Z",
    "requestId": "abc-123"
  }
}
```

#### 2.4 ErrorResponse — Standard Error Format

All errors follow this structure:
```json
{
  "timestamp": "2026-03-07T10:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Hotel not found with id: '123'",
  "path": "/api/hotels/123",
  "traceId": "uuid-for-debugging"
}
```

#### 2.5 GlobalExceptionHandler

A `@RestControllerAdvice` that catches all exceptions and converts them to proper HTTP responses:

| Exception | HTTP Status | Example |
|---|---|---|
| `ResourceNotFoundException` | 404 | "Hotel not found with id: 5" |
| `ValidationException` | 400 | "End date must be after start date" |
| `MethodArgumentNotValidException` | 400 | "email: must be valid" (Bean validation) |
| `UnauthorizedException` | 401 | "Unauthorized access" |
| `ForbiddenException` | 403 | "You can only view your own bookings" |
| `ServiceUnavailableException` | 503 | "Service 'hotel-service' is unavailable" |
| `Exception` (catch-all) | 500 | "An unexpected error occurred" |

Each error gets a unique **traceId** (UUID) for debugging across services.

#### 2.6 Azure Blob Storage

- **Conditional**: Only loads if `azure.storage.connection-string` is configured
- Uploads images with random UUID filenames (prevents name collisions)
- Validates: image type only, max 10MB
- Whitelisted containers: hotels, vehicles, tour-guides, tourists, packages, events, reviews, products

---

## 3. Discovery Server

### Purpose
A **Netflix Eureka Server** that acts as the **service registry**. All microservices register here and discover each other by name instead of hardcoded URLs.

### Folder Structure
```
discovery-server/
├── Dockerfile
├── pom.xml
└── src/main/
    ├── java/com/travelplan/discovery/
    │   ├── DiscoveryServerApplication.java
    │   └── config/SecurityConfig.java
    └── resources/
        ├── application.yml
        └── application-prod.yml
```

### How It Works

```
1. Discovery Server starts on port 8761
2. Each microservice (tourist, hotel, booking, etc.) registers itself:
     eureka.client.register-with-eureka: true
3. When booking-service needs hotel-service:
     - It asks Eureka: "Where is hotel-service?"
     - Eureka returns the IP/port
     - OpenFeign makes the HTTP call
4. Services send heartbeats every 30 seconds
5. If a service stops sending heartbeats, Eureka removes it
```

### Key Configuration Explained

```yaml
eureka:
  client:
    register-with-eureka: false   # Server doesn't register itself
    fetch-registry: false         # Server doesn't need to fetch from others
  server:
    enable-self-preservation: false  # DEV: quickly remove dead services
    eviction-interval-timer-in-ms: 5000  # Check every 5 seconds
```

**Production differences:**
- `enable-self-preservation: true` — prevents removing services during network partitions (safety)
- `eviction-interval-timer-in-ms: 15000` — less aggressive (every 15 seconds)
- Kubernetes liveness/readiness probes enabled

### SecurityConfig
- **All endpoints public** (`permitAll()`) — this is an internal-only service
- CSRF disabled — needed for service registration heartbeats

### Why Eureka Over Hardcoded URLs?
- Services can scale (multiple instances) — Eureka does load balancing
- Services can move (different IPs in containers) — Eureka tracks them
- Services can fail and recover — Eureka detects via heartbeats

---

## 4. Tourist Service

### Purpose
Manages **tourist/user profiles**, **travel preferences**, and a **wallet/credits system** for refunds.

### Folder Structure
```
tourist-service/src/main/java/com/travelplan/tourist/
├── TouristServiceApplication.java
├── config/
│   ├── KafkaConfig.java              # Kafka error handling + DLT
│   └── TouristSecurityConfig.java    # JWT security rules
├── controller/
│   └── TouristController.java        # REST endpoints
├── service/
│   ├── TouristService.java           # Interface
│   └── impl/TouristServiceImpl.java  # Business logic
├── entity/
│   ├── Tourist.java                  # Core profile entity
│   ├── TouristPreference.java        # Travel preferences
│   └── WalletTransaction.java        # Wallet credits/debits
├── dto/
│   ├── TouristRegistrationRequest.java
│   ├── TouristResponse.java
│   ├── TouristUpdateRequest.java
│   ├── PreferenceRequest.java / PreferenceResponse.java
│   └── WalletResponse.java / WalletTransactionResponse.java
├── repository/
│   ├── TouristRepository.java
│   ├── TouristPreferenceRepository.java
│   └── WalletTransactionRepository.java
├── mapper/
│   └── TouristMapper.java            # Entity <-> DTO conversion
└── listener/
    └── BookingRefundListener.java     # Kafka consumer for refunds
```

### Database Schema (3 Tables)

```
tourists                      tourist_preferences           wallet_transactions
+------------------+         +----------------------+      +--------------------+
| id (PK)          |    1:1  | id (PK)              |      | id (PK)            |
| user_id (unique) |<------->| tourist_id (FK,unique)|      | tourist_id (FK)    |
| email (unique)   |         | preferred_budget     |      | amount             |
| first_name       |         | travel_style         |      | type (REFUND/USED) |
| last_name        |         | dietary_restrictions[]|      | description        |
| phone_number     |         | interests[]          |      | reference_id       |
| nationality      |         | preferred_languages[]|      | created_at         |
| profile_image_url|         | accessibility_needs  |      +--------------------+
| is_active        |         +----------------------+
| created_at       |
| updated_at       |
+------------------+
```

**Note:** `dietary_restrictions`, `interests`, `preferred_languages` use PostgreSQL `TEXT[]` array type with Hibernate 6's `@JdbcTypeCode(SqlTypes.ARRAY)`.

### REST API Endpoints

| Method | Path | Auth | Description |
|---|---|---|---|
| `POST` | `/api/tourists/register` | Public | Register new tourist |
| `GET` | `/api/tourists/me` | JWT | Get own profile |
| `PUT` | `/api/tourists/me` | JWT | Update own profile (partial) |
| `GET` | `/api/tourists/me/preferences` | JWT | Get travel preferences |
| `PUT` | `/api/tourists/me/preferences` | JWT | Update travel preferences |
| `GET` | `/api/tourists/me/wallet` | JWT | Get wallet balance & history |
| `GET` | `/api/tourists/{id}` | JWT | Get any tourist by ID |

### Key Business Logic

#### Registration Flow
```
POST /api/tourists/register
    → Check email doesn't exist (ConflictException if duplicate)
    → Check userId doesn't exist (ConflictException if duplicate)
    → Create Tourist entity
    → Create default TouristPreference (if budget provided)
    → Return TouristResponse (HTTP 201)
```

#### Wallet System
The wallet tracks refund credits using a **transaction ledger** pattern:

```
Balance = SUM(REFUND amounts) + SUM(ADJUSTMENT amounts) - SUM(USED amounts)
```

This is calculated via a `@Query` in the repository:
```java
@Query("SELECT COALESCE(SUM(CASE WHEN w.type IN ('REFUND','ADJUSTMENT') THEN w.amount ELSE 0 END), 0) "
     + "- COALESCE(SUM(CASE WHEN w.type = 'USED' THEN w.amount ELSE 0 END), 0) "
     + "FROM WalletTransaction w WHERE w.tourist.id = :touristId")
BigDecimal calculateBalance(@Param("touristId") Long touristId);
```

#### Kafka Refund Listener
When the booking service cancels a booking and processes a refund, it publishes a `booking.refund.processed` event to the `booking-events` Kafka topic. The tourist service listens:

```
booking-service                          tourist-service
     |                                        |
     |  Kafka: booking-events topic           |
     |  event: booking.refund.processed       |
     +--------------------------------------->|
     |                                        |
     |                    BookingRefundListener picks up event
     |                    → Extracts touristId, amount, bookingRef
     |                    → Calls touristService.creditWallet()
     |                    → Creates WalletTransaction (type=REFUND)
```

**Error handling**: Kafka consumer has retry policy (3 retries, 1-second backoff). Failed messages go to a Dead Letter Topic (`-dlt`).

### Authentication Pattern
```java
@GetMapping("/me")
public ApiResponse<TouristResponse> getCurrentTourist(Authentication authentication) {
    String userId = authentication.getName();  // Supabase UUID from JWT
    return ApiResponse.success(touristService.getByUserId(userId));
}
```
- `Authentication` is injected by Spring Security after `JwtValidationFilter` validates the token
- `authentication.getName()` returns the Supabase user UUID (the JWT `sub` claim)

---

## 5. Booking Service

### Purpose
The **orchestrator** for multi-provider bookings. A single booking can include a hotel, tour guide, AND vehicle. Uses the **Saga pattern** for distributed transaction management.

### Folder Structure
```
booking-service/src/main/java/com/travelplan/booking/
├── BookingServiceApplication.java
├── config/
│   ├── BookingSecurityConfig.java     # Security rules (public + auth endpoints)
│   ├── FeignJwtInterceptor.java       # Propagates JWT to inter-service calls
│   └── KafkaConfig.java              # Kafka topics definition
├── controller/
│   └── BookingController.java         # REST endpoints
├── service/
│   ├── BookingService.java            # Interface
│   ├── impl/BookingServiceImpl.java   # Core booking logic
│   ├── SagaOrchestrator.java          # Distributed transaction manager
│   ├── EventPublisher.java            # Kafka event publishing
│   └── RefundPolicyService.java       # Time-based refund calculator
├── entity/
│   ├── Booking.java                   # Main booking entity
│   ├── BookingItem.java               # Individual provider items
│   └── SagaOrchestration.java         # Saga state tracking
├── dto/ (11 DTOs)
│   ├── CreateBookingRequest.java
│   ├── BookingResponse.java / BookingItemRequest.java / BookingItemResponse.java
│   ├── AvailabilityCheckRequest.java / AvailabilityCheckResponse.java
│   ├── CancelBookingRequest.java / UpdateBookingItemStatusRequest.java
│   └── BookingEvent.java              # Kafka event structure
├── mapper/
│   └── BookingMapper.java
├── client/
│   ├── HotelServiceClient.java        # Feign client -> hotel-service
│   ├── TourGuideServiceClient.java    # Feign client -> tour-guide-service
│   └── VehicleServiceClient.java      # Feign client -> vehicle-service
├── enums/
│   ├── BookingStatus.java             # PENDING, CONFIRMED, CANCELLED, COMPLETED
│   ├── ProviderType.java              # HOTEL, TOUR_GUIDE, VEHICLE
│   └── SagaState.java                 # INITIATED, IN_PROGRESS, COMPLETED, etc.
└── repository/
    ├── BookingRepository.java
    ├── BookingItemRepository.java
    └── SagaOrchestrationRepository.java
```

### Database Schema (3 Tables)

```
bookings                          booking_items                 saga_orchestration
+-----------------------+         +--------------------+        +--------------------+
| id (PK)               |   1:N  | id (PK)            |        | id (PK)            |
| tourist_id             |<------>| booking_id (FK)    |        | booking_id (FK)    |
| itinerary_id           |        | provider_type      |        | saga_state         |
| booking_reference      |        | provider_id        |        | current_step       |
| status                 |        | item_name          |        | total_steps        |
| total_amount           |        | quantity           |        | completed_steps    |
| booking_date           |        | unit_price         |        | failure_reason     |
| start_date / end_date  |        | subtotal           |        | timeout_at         |
| notes                  |        | start_date/end_date|        | created_at         |
| cancellation_reason    |        | status             |        | completed_at       |
| refund_amount          |        | created_at         |        +--------------------+
| refund_policy          |        +--------------------+
| created_at / updated_at|
+-----------------------+
```

### REST API Endpoints

| Method | Path | Auth | Description |
|---|---|---|---|
| `POST` | `/api/bookings` | JWT | Create a new booking (triggers saga) |
| `GET` | `/api/bookings/{id}` | JWT | Get booking by ID (owner only) |
| `GET` | `/api/bookings` | JWT | List my bookings (paginated, filterable) |
| `POST` | `/api/bookings/{id}/cancel` | JWT | Cancel booking (calculates refund) |
| `POST` | `/api/bookings/availability-check` | Public | Check provider availability |
| `PUT` | `/api/bookings/{id}/items/{itemId}/status` | Internal | Update item status (service-to-service) |
| `GET` | `/api/bookings/provider/{type}/{id}` | Internal | Get bookings for a provider |
| `GET` | `/api/bookings/reference/{ref}` | Internal | Lookup by booking reference |
| `PUT` | `/api/bookings/{id}/itinerary/{itinId}` | Internal | Link booking to itinerary |

### Core Booking Flow

```
Tourist sends: POST /api/bookings
{
  "startDate": "2026-04-01",
  "endDate": "2026-04-05",
  "items": [
    { "providerType": "HOTEL", "providerId": 10, "itemName": "Beach Resort", "unitPrice": 150.00, "quantity": 4 },
    { "providerType": "VEHICLE", "providerId": 5, "itemName": "SUV Rental", "unitPrice": 80.00, "quantity": 4 }
  ]
}
```

**Step-by-step execution:**

```
1. VALIDATE REQUEST
   ├── endDate > startDate?
   ├── Provider types valid? (HOTEL, TOUR_GUIDE, VEHICLE only)
   └── Items non-empty?

2. CREATE BOOKING (status = PENDING)
   ├── Generate reference: "TRP-20260401-A1B2C3"
   ├── Calculate total: (150×4) + (80×4) = $920
   └── Save Booking + BookingItems to database

3. EXECUTE SAGA (distributed transaction)
   ├── PRE-CHECK: Call each provider's /availability endpoint
   │   ├── hotel-service: GET /api/hotels/10/availability?startDate=2026-04-01&endDate=2026-04-05
   │   └── vehicle-service: GET /api/vehicles/5/availability?startDate=2026-04-01&endDate=2026-04-05
   │   └── If ANY unavailable → CANCEL booking, throw ValidationException
   │
   ├── CREATE SAGA RECORD (state = IN_PROGRESS)
   │
   ├── CONFIRM STEP-BY-STEP:
   │   ├── Step 1: Call hotel-service GET /api/hotels/10 → Mark step completed
   │   └── Step 2: Call vehicle-service GET /api/vehicles/5 → Mark step completed
   │   └── If ANY step fails:
   │       ├── COMPENSATE: Cancel all previously confirmed items
   │       ├── Mark saga as ROLLED_BACK
   │       └── Return failure
   │
   └── ALL STEPS PASSED: Mark saga COMPLETED

4. UPDATE BOOKING (status = CONFIRMED)
   └── All items status = CONFIRMED

5. PUBLISH KAFKA EVENTS
   ├── "booking.reservation.created" → booking-events topic
   └── "booking.reservation.confirmed" → booking-events + booking-notifications topics

6. RETURN RESPONSE (HTTP 201)
```

### Saga Pattern Explained

The **Saga pattern** solves the problem of distributed transactions across microservices. Unlike a single database transaction, we can't use ACID across multiple services.

**Problem**: If hotel booking succeeds but vehicle booking fails, we need to undo the hotel booking.

**Solution**: Execute steps one-by-one. If any step fails, run **compensating transactions** (undo) for all previously completed steps.

```
                    SAGA ORCHESTRATOR
                          |
         Success Path     |      Failure Path
         ─────────────    |      ──────────────
                          |
    ┌─ Step 1: Hotel ─────┤
    │  (confirm)          │
    │                     │
    ├─ Step 2: Vehicle ───┤──── Step 2 FAILS!
    │  (confirm)          │         │
    │                     │    ┌────┘
    ├─ Step 3: Guide ─────┤    │ COMPENSATE:
    │  (confirm)          │    ├── Cancel Hotel  (undo step 1)
    │                     │    └── Mark saga ROLLED_BACK
    └─ ALL DONE ──────────┘
       Mark COMPLETED
```

**SagaOrchestration entity** tracks:
- `sagaState`: INITIATED → IN_PROGRESS → COMPLETED (or ROLLING_BACK → ROLLED_BACK)
- `currentStep` / `totalSteps`: Progress tracking
- `completedSteps`: CSV audit trail (e.g., "1:HOTEL:10,2:VEHICLE:5")
- `timeoutAt`: 5-minute timeout for stuck sagas

### Cancellation & Refund Flow

```
Tourist sends: POST /api/bookings/123/cancel
{ "reason": "Change of plans" }

1. VALIDATE
   ├── Booking exists?
   ├── Tourist owns this booking?
   └── Status not CANCELLED or COMPLETED?

2. CALCULATE REFUND (RefundPolicyService)
   ├── > 48 hours before start → 100% FULL_REFUND
   ├── 24-48 hours before start → 50% PARTIAL_REFUND
   └── < 24 hours before start → 0% NO_REFUND

3. UPDATE BOOKING
   ├── status = CANCELLED
   ├── cancellationReason = "Change of plans"
   ├── refundAmount = calculated amount
   ├── refundPolicy = "FULL_REFUND"
   └── All items status = CANCELLED

4. PUBLISH KAFKA EVENTS
   ├── "booking.reservation.cancelled" → booking-events + booking-notifications
   └── "booking.refund.processed" → booking-events (if refundAmount > 0)
         │
         └──→ tourist-service picks this up via BookingRefundListener
              → Credits tourist's wallet
```

### Feign Clients (Inter-Service Communication)

```java
@FeignClient(name = "hotel-service")    // Discovered via Eureka by name!
public interface HotelServiceClient {
    @GetMapping("/api/hotels/{id}")
    ApiResponse<Object> getHotelById(@PathVariable("id") Long id);

    @GetMapping("/api/hotels/{id}/availability")
    ApiResponse<Object> checkAvailability(@PathVariable("id") Long id,
                                          @RequestParam("startDate") String startDate,
                                          @RequestParam("endDate") String endDate);
}
```

**FeignJwtInterceptor**: Automatically copies the JWT from the incoming request to outgoing Feign calls, so the downstream service can authenticate the user.

### Kafka Event Publishing

Two Kafka topics (defined in KafkaConfig):
- `booking-events` (3 partitions) — all booking lifecycle events
- `booking-notifications` (3 partitions) — only events worth notifying about

Events published:

| Event Type | Topics | When |
|---|---|---|
| `booking.reservation.created` | booking-events | After booking saved |
| `booking.reservation.confirmed` | both | After saga completes |
| `booking.reservation.cancelled` | both | After cancellation |
| `booking.refund.processed` | booking-events | After refund calculated |

### Status Transitions

```
BookingItem:  PENDING → CONFIRMED → COMPLETED
                  └──→ CANCELLED    └──→ CANCELLED

Booking:      PENDING → CONFIRMED → COMPLETED
                  └──→ CANCELLED    └──→ CANCELLED

Saga:         INITIATED → IN_PROGRESS → COMPLETED
                              └──→ ROLLING_BACK → ROLLED_BACK
                              └──→ FAILED
```

**Booking-level status** is derived from items:
- All items CONFIRMED → Booking CONFIRMED
- All items CANCELLED → Booking CANCELLED
- All items COMPLETED → Booking COMPLETED

---

## 6. Key Design Patterns & Concepts

### 6.1 Microservices Architecture
- Each service has its **own database** (Database per Service pattern)
- Services communicate via REST (OpenFeign) and events (Kafka)
- Service discovery via Eureka (no hardcoded URLs)

### 6.2 Saga Pattern (Booking Service)
- Orchestration-based saga for multi-provider bookings
- Compensating transactions on failure (rollback)
- Persistent saga state for auditability

### 6.3 Event-Driven Architecture (Kafka)
- Loose coupling between services
- Booking service publishes events, tourist service consumes them
- Dead Letter Topics for failed message handling

### 6.4 Shared Library Pattern (common-lib)
- Avoid code duplication across microservices
- Consistent error handling, security, and API response format
- `@ConditionalOnMissingBean` allows services to override defaults

### 6.5 JWT-Based Stateless Authentication
- Supabase issues JWTs, services validate them
- No server-side sessions (scalable, stateless)
- Role extracted from trusted `app_metadata` only

### 6.6 Database Migration (Flyway)
- Version-controlled schema changes (V1, V2, V3...)
- `ddl-auto: validate` — Hibernate validates but doesn't modify schema
- Flyway manages all DDL changes

### 6.7 DTO Pattern
- Separate objects for requests, responses, and entities
- Validation annotations on request DTOs
- Mapper classes convert between layers

### 6.8 Repository Pattern (Spring Data JPA)
- Interfaces extending `JpaRepository`
- Custom query methods via method naming convention
- `@Query` for complex queries (wallet balance calculation)

---

## 7. Potential Viva Questions & Answers

### Q1: Why microservices instead of monolithic?
**A:** Each service can be developed, deployed, and scaled independently. For example, booking-service may need more instances during peak season while tourist-service stays the same. Teams can work on different services simultaneously. Technology choices can vary per service if needed.

### Q2: What is Eureka and why do you need it?
**A:** Eureka is a service registry. In a microservices environment, services run on dynamic IPs/ports (especially in containers). Instead of hardcoding URLs, services register with Eureka and discover each other by name. When booking-service needs hotel-service, it asks Eureka for the current address.

### Q3: How does authentication work across services?
**A:** Supabase issues JWT tokens when users log in. Every service has a `JwtValidationFilter` (from common-lib) that validates the token, extracts the user ID and role, and sets up Spring Security context. For service-to-service calls, `FeignJwtInterceptor` propagates the JWT from the incoming request to the outgoing Feign call.

### Q4: What is the Saga pattern and why is it needed?
**A:** When a booking includes a hotel AND a vehicle, we need both to succeed or both to fail. But they're in different databases across different services — we can't use a single database transaction. The Saga pattern executes each step sequentially. If step 2 (vehicle) fails, it compensates step 1 (cancels the hotel). This maintains data consistency across services.

### Q5: Why Kafka for events? Why not just REST calls?
**A:** Kafka provides asynchronous, decoupled communication. When a booking is cancelled, the booking service doesn't need to know or wait for the tourist service to credit the wallet. It publishes an event and moves on. This means:
- Services don't depend on each other being online at the same moment
- If tourist-service is temporarily down, the event waits in Kafka and processes later
- Multiple services can react to the same event independently

### Q6: How does the refund system work?
**A:** Time-based policy: >48h = full refund, 24-48h = 50%, <24h = none. The booking service calculates the refund and publishes a Kafka event. The tourist service's `BookingRefundListener` picks it up and credits the wallet. The wallet balance is calculated from a transaction ledger (sum of refunds minus sum of usage).

### Q7: What is `@ConditionalOnMissingBean` and why use it?
**A:** It means "only create this bean if no other bean of this type exists." In common-lib, `CommonSecurityConfig` defines a default `SecurityFilterChain`. But booking-service needs custom rules (some endpoints are public for service-to-service calls). So booking-service defines its own `SecurityFilterChain`, and the common-lib one is skipped. This allows defaults with overrides.

### Q8: Why Flyway instead of `ddl-auto: update`?
**A:** `ddl-auto: update` auto-generates DDL which can be unpredictable and risky in production (it might drop columns). Flyway uses versioned SQL scripts (V1, V2, V3) that are explicit, reviewable, and repeatable. You know exactly what changes are applied. It's the industry standard for production database management.

### Q9: Why is the wallet balance calculated via query instead of stored as a field?
**A:** The transaction ledger pattern is more reliable. A stored balance field could get out of sync if a transaction fails partway through. By calculating `SUM(credits) - SUM(debits)` from the transaction table, the balance is always correct and we have a full audit trail of every change.

### Q10: How do you handle partial profile updates?
**A:** The `TouristUpdateRequest` DTO has all fields optional (no validation constraints). The mapper's `updateEntity()` method checks each field: `if (request.getFirstName() != null) tourist.setFirstName(request.getFirstName())`. Only non-null fields are updated, preserving existing data. This avoids requiring the frontend to send the entire object.

### Q11: What happens if a downstream service is unavailable during booking?
**A:** The saga's pre-check step calls each provider's availability endpoint. If the call fails (timeout or error), it catches the exception and marks that item as unavailable. The entire booking fails immediately with a ValidationException — no partial bookings are created. If failure happens during the confirmation step, the saga rolls back all previously confirmed items.

### Q12: Why extract role from `app_metadata` instead of `user_metadata`?
**A:** Security. `user_metadata` is writable by any authenticated user via the Supabase client SDK. An attacker could call `supabase.auth.updateUser({ data: { role: 'ADMIN' }})` and give themselves admin access. `app_metadata` is only writable via the Supabase admin/service-role API, making it trustworthy. A database trigger copies the role from user_metadata to app_metadata at signup time.

### Q13: What is the booking reference format?
**A:** `TRP-yyyyMMdd-XXXXXX` (e.g., `TRP-20260401-A1B2C3`). It's human-readable and includes the booking date. The random suffix prevents collisions. It's stored as a unique column in the database.

### Q14: How are the Docker images optimized?
**A:** Multi-stage builds. The first stage uses a full Maven image to compile. The second stage uses a slim JRE-only Alpine image (much smaller). A non-root `spring` user (UID 1001) runs the application for security. Maven dependencies are cached between builds.
