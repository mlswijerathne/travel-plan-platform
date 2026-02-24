# Booking Service Specification

> **Service Name:** booking-service
> **Port:** 8086
> **Package:** `com.travelplan.booking`
> **Database Schema:** `booking`
> **Version:** 1.0.0-SNAPSHOT

---

## 1. Service Overview

### Responsibility

The Booking Service is the transactional core of the Travel Plan Platform. It orchestrates multi-provider bookings across hotels, tour guides, and vehicles using the Saga pattern. It manages the full booking lifecycle from creation through confirmation, cancellation, and refund processing, publishing events to downstream services via Amazon SQS.

### Key Features

- Single and multi-provider booking creation
- Saga pattern orchestration with automatic compensation on failure
- Pre-booking availability validation across all provider services
- Unique booking reference generation (`TRP-yyyyMMdd-XXXXXX`)
- Time-based refund policy (full, partial, or no refund)
- Booking item status management per provider
- Event-driven notifications via Amazon SQS
- Provider-facing booking views

### Dependencies

| Dependency | Purpose |
|---|---|
| `common-lib` | Shared DTOs, JWT filter, Security config, exception handling |
| `hotel-service` | Availability checks and hotel validation (Feign) |
| `tour-guide-service` | Availability checks and guide validation (Feign) |
| `vehicle-service` | Availability checks and vehicle validation (Feign) |
| Supabase Auth | JWT-based authentication |
| Eureka Discovery Server | Service registration and Feign client resolution |
| PostgreSQL (Supabase) | Persistent storage |
| Amazon SQS | Asynchronous event publishing |
| Flyway | Database migration management |

---

## 2. API Endpoints

### 2.1 Create Booking

| Field | Value |
|---|---|
| **Method** | `POST` |
| **URL** | `/api/bookings` |
| **Description** | Creates a new booking with one or more provider items. Triggers saga orchestration for multi-provider bookings. |
| **Auth** | Bearer JWT — Role: `TOURIST` |

**Request Body:**

```json
{
  "startDate": "2026-03-15",
  "endDate": "2026-03-18",
  "itineraryId": 42,
  "notes": "Honeymoon trip, please arrange flowers in the room",
  "items": [
    {
      "providerType": "HOTEL",
      "providerId": 12,
      "itemName": "Deluxe Ocean View - 3 nights",
      "quantity": 1,
      "unitPrice": 185.00,
      "startDate": "2026-03-15",
      "endDate": "2026-03-18"
    },
    {
      "providerType": "TOUR_GUIDE",
      "providerId": 1,
      "itemName": "Wildlife Safari Guide - 2 days",
      "quantity": 2,
      "unitPrice": 150.00,
      "startDate": "2026-03-16",
      "endDate": "2026-03-17"
    },
    {
      "providerType": "VEHICLE",
      "providerId": 5,
      "itemName": "Toyota HiAce Van - 3 days",
      "quantity": 1,
      "unitPrice": 95.00,
      "startDate": "2026-03-15",
      "endDate": "2026-03-18"
    }
  ]
}
```

| Field | Type | Required | Validation |
|---|---|---|---|
| `startDate` | LocalDate | Yes | `@FutureOrPresent` |
| `endDate` | LocalDate | Yes | `@Future`, must be after startDate |
| `itineraryId` | Long | No | Links to an existing itinerary |
| `notes` | String | No | Free text |
| `items` | Array | Yes | `@NotEmpty`, min 1 item |
| `items[].providerType` | String | Yes | `HOTEL`, `TOUR_GUIDE`, or `VEHICLE` |
| `items[].providerId` | Long | Yes | Must reference a valid provider |
| `items[].itemName` | String | Yes | `@NotBlank` |
| `items[].quantity` | Integer | No | Min 1 (default: 1) |
| `items[].unitPrice` | BigDecimal | Yes | Min 0.01 |
| `items[].startDate` | LocalDate | No | Item-specific start |
| `items[].endDate` | LocalDate | No | Item-specific end |

**Response:** `201 Created`

```json
{
  "data": {
    "id": 1,
    "touristId": "tourist-uuid",
    "bookingReference": "TRP-20260315-A7X3K9",
    "itineraryId": 42,
    "status": "CONFIRMED",
    "totalAmount": 770.00,
    "bookingDate": "2026-02-24T10:00:00Z",
    "startDate": "2026-03-15",
    "endDate": "2026-03-18",
    "notes": "Honeymoon trip, please arrange flowers in the room",
    "cancellationReason": null,
    "refundAmount": null,
    "refundPolicy": null,
    "items": [
      {
        "id": 1,
        "providerType": "HOTEL",
        "providerId": 12,
        "itemName": "Deluxe Ocean View - 3 nights",
        "quantity": 1,
        "unitPrice": 185.00,
        "subtotal": 185.00,
        "startDate": "2026-03-15",
        "endDate": "2026-03-18",
        "status": "CONFIRMED",
        "createdAt": "2026-02-24T10:00:00Z"
      },
      {
        "id": 2,
        "providerType": "TOUR_GUIDE",
        "providerId": 1,
        "itemName": "Wildlife Safari Guide - 2 days",
        "quantity": 2,
        "unitPrice": 150.00,
        "subtotal": 300.00,
        "startDate": "2026-03-16",
        "endDate": "2026-03-17",
        "status": "CONFIRMED",
        "createdAt": "2026-02-24T10:00:00Z"
      },
      {
        "id": 3,
        "providerType": "VEHICLE",
        "providerId": 5,
        "itemName": "Toyota HiAce Van - 3 days",
        "quantity": 1,
        "unitPrice": 95.00,
        "subtotal": 95.00,
        "startDate": "2026-03-15",
        "endDate": "2026-03-18",
        "status": "CONFIRMED",
        "createdAt": "2026-02-24T10:00:00Z"
      }
    ],
    "createdAt": "2026-02-24T10:00:00Z",
    "updatedAt": "2026-02-24T10:00:01Z"
  }
}
```

**Saga Flow:**

1. Pre-check availability across all providers (HOTEL → TOUR_GUIDE → VEHICLE)
2. Create saga record in `saga_orchestration` table
3. Confirm each provider step-by-step
4. If any step fails → compensate all previously confirmed items (rollback)
5. On full success → status becomes `CONFIRMED`
6. On failure → status becomes `PENDING` with saga state `ROLLED_BACK`

---

### 2.2 Get Booking by ID

| Field | Value |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/bookings/{id}` |
| **Description** | Retrieves booking details. Tourist can only access their own bookings. |
| **Auth** | Bearer JWT |

**Path Parameters:**

| Parameter | Type | Description |
|---|---|---|
| `id` | Long | Booking database ID |

**Response:** `200 OK` — Full `BookingResponse`.

**Error Responses:**

| Status | Condition |
|---|---|
| `403` | Tourist attempting to access another tourist's booking |
| `404` | Booking not found |

---

### 2.3 Get Tourist Bookings (Paginated)

| Field | Value |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/bookings` |
| **Description** | Lists the authenticated tourist's bookings with optional status filter. |
| **Auth** | Bearer JWT — Role: `TOURIST` |

**Query Parameters:**

| Parameter | Type | Required | Description |
|---|---|---|---|
| `status` | String | No | Filter: `PENDING`, `CONFIRMED`, `CANCELLED`, `COMPLETED` |
| `page` | Integer | No | Page number (default: 0) |
| `size` | Integer | No | Page size (default: 10) |

**Response:** `200 OK` — `PaginatedResponse<BookingResponse>`.

---

### 2.4 Cancel Booking

| Field | Value |
|---|---|
| **Method** | `POST` |
| **URL** | `/api/bookings/{id}/cancel` |
| **Description** | Cancels a booking and calculates refund based on time-based policy. |
| **Auth** | Bearer JWT — Role: `TOURIST` |

**Request Body:**

```json
{
  "reason": "Plans changed, need to reschedule the trip"
}
```

**Refund Policy:**

| Timeframe | Refund |
|---|---|
| > 48 hours before start date | 100% (`FULL_REFUND`) |
| 24–48 hours before start date | 50% (`PARTIAL_REFUND`) |
| < 24 hours before start date | 0% (`NO_REFUND`) |

**Response:** `200 OK`

```json
{
  "data": {
    "id": 1,
    "status": "CANCELLED",
    "cancellationReason": "Plans changed, need to reschedule the trip",
    "refundAmount": 770.00,
    "refundPolicy": "FULL_REFUND",
    ...
  }
}
```

**Error Responses:**

| Status | Condition |
|---|---|
| `400` | Booking already cancelled or completed |
| `403` | Tourist attempting to cancel another tourist's booking |

---

### 2.5 Check Availability

| Field | Value |
|---|---|
| **Method** | `POST` |
| **URL** | `/api/bookings/availability-check` |
| **Description** | Pre-booking availability check across multiple providers without creating a booking. |
| **Auth** | Bearer JWT |

**Request Body:**

```json
{
  "items": [
    {
      "providerType": "HOTEL",
      "providerId": 12,
      "startDate": "2026-03-15",
      "endDate": "2026-03-18",
      "quantity": 1
    },
    {
      "providerType": "VEHICLE",
      "providerId": 5,
      "startDate": "2026-03-15",
      "endDate": "2026-03-18",
      "quantity": 1
    }
  ]
}
```

**Response:** `200 OK`

```json
{
  "data": {
    "available": true,
    "items": [
      {
        "providerType": "HOTEL",
        "providerId": 12,
        "available": true,
        "message": "Available"
      },
      {
        "providerType": "VEHICLE",
        "providerId": 5,
        "available": true,
        "message": "Available"
      }
    ]
  }
}
```

---

### 2.6 Update Booking Item Status

| Field | Value |
|---|---|
| **Method** | `PUT` |
| **URL** | `/api/bookings/{bookingId}/items/{itemId}/status` |
| **Description** | Updates the status of a specific booking item (e.g., provider confirms or completes). |
| **Auth** | Bearer JWT |

**Request Body:**

```json
{
  "status": "CONFIRMED",
  "reason": "Room prepared and reserved"
}
```

| Field | Type | Required |
|---|---|---|
| `status` | String | Yes (`PENDING`, `CONFIRMED`, `COMPLETED`, `CANCELLED`) |
| `reason` | String | No |

**Valid Status Transitions:**

- `PENDING` → `CONFIRMED` or `CANCELLED`
- `CONFIRMED` → `COMPLETED` or `CANCELLED`

---

### 2.7 Get Provider Bookings

| Field | Value |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/bookings/provider/{providerType}/{providerId}` |
| **Description** | Lists bookings for a specific provider. Used by provider dashboards. |
| **Auth** | Bearer JWT |

**Path Parameters:**

| Parameter | Type | Description |
|---|---|---|
| `providerType` | String | `HOTEL`, `TOUR_GUIDE`, or `VEHICLE` |
| `providerId` | Long | Provider database ID |

**Query Parameters:**

| Parameter | Type | Required |
|---|---|---|
| `status` | String | No |
| `page` | Integer | No |
| `size` | Integer | No |

---

### 2.8 Get Booking by Reference

| Field | Value |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/bookings/reference/{bookingReference}` |
| **Description** | Looks up a booking by its human-readable reference code. |
| **Auth** | Bearer JWT |

**Path Parameters:**

| Parameter | Type | Description |
|---|---|---|
| `bookingReference` | String | e.g., `TRP-20260315-A7X3K9` |

---

## 3. Data Model Schema

### 3.1 `bookings` Table

| Column | Type | Nullable | Default | Description |
|---|---|---|---|---|
| `id` | `BIGSERIAL` | No | Auto-increment | Primary key |
| `tourist_id` | `VARCHAR(255)` | No | — | Supabase UID of booking tourist |
| `itinerary_id` | `BIGINT` | Yes | — | Associated itinerary |
| `booking_reference` | `VARCHAR(50)` | Yes | — | Human-readable reference (unique) |
| `status` | `VARCHAR(50)` | No | `PENDING` | `PENDING`, `CONFIRMED`, `CANCELLED`, `COMPLETED` |
| `total_amount` | `DECIMAL(12,2)` | No | — | Sum of all item subtotals |
| `booking_date` | `TIMESTAMPTZ` | Yes | `CURRENT_TIMESTAMP` | When booking was placed |
| `start_date` | `DATE` | No | — | Trip start date |
| `end_date` | `DATE` | No | — | Trip end date |
| `notes` | `TEXT` | Yes | — | Special requests |
| `cancellation_reason` | `TEXT` | Yes | — | Reason for cancellation |
| `refund_amount` | `DECIMAL(12,2)` | Yes | — | Calculated refund amount |
| `refund_policy` | `VARCHAR(50)` | Yes | — | `FULL_REFUND`, `PARTIAL_REFUND`, `NO_REFUND` |
| `created_at` | `TIMESTAMPTZ` | Yes | `CURRENT_TIMESTAMP` | Record creation |
| `updated_at` | `TIMESTAMPTZ` | Yes | `CURRENT_TIMESTAMP` | Last modification |

**Indexes:**

| Index | Column(s) | Purpose |
|---|---|---|
| `idx_bookings_tourist_id` | `tourist_id` | Tourist booking lookup |
| `idx_bookings_status` | `status` | Status-based filtering |
| `booking_reference` | UNIQUE constraint | Reference code lookup |

### 3.2 `booking_items` Table

| Column | Type | Nullable | Default | Description |
|---|---|---|---|---|
| `id` | `BIGSERIAL` | No | Auto-increment | Primary key |
| `booking_id` | `BIGINT` | No | — | FK → `bookings.id` (cascade delete) |
| `provider_type` | `VARCHAR(50)` | No | — | `HOTEL`, `TOUR_GUIDE`, `VEHICLE` |
| `provider_id` | `BIGINT` | No | — | Provider entity ID |
| `item_name` | `VARCHAR(255)` | No | — | Display name |
| `quantity` | `INT` | Yes | `1` | Quantity booked |
| `unit_price` | `DECIMAL(10,2)` | No | — | Price per unit |
| `subtotal` | `DECIMAL(10,2)` | No | — | `unitPrice × quantity` |
| `start_date` | `DATE` | Yes | — | Item-specific start |
| `end_date` | `DATE` | Yes | — | Item-specific end |
| `status` | `VARCHAR(50)` | No | `PENDING` | Item-level status |
| `created_at` | `TIMESTAMPTZ` | Yes | `CURRENT_TIMESTAMP` | Record creation |

**Indexes:**

| Index | Column(s) | Purpose |
|---|---|---|
| `idx_booking_items_booking_id` | `booking_id` | Items per booking |
| `idx_booking_items_provider` | `provider_type, provider_id` | Provider booking lookup |
| `idx_booking_items_status` | `status` | Status-based filtering |
| `idx_booking_items_dates` | `start_date, end_date` | Date-range queries |

### 3.3 `saga_orchestration` Table

| Column | Type | Nullable | Default | Description |
|---|---|---|---|---|
| `id` | `BIGSERIAL` | No | Auto-increment | Primary key |
| `booking_id` | `BIGINT` | No | — | FK → `bookings.id` (cascade delete) |
| `saga_state` | `VARCHAR(50)` | No | `INITIATED` | Saga lifecycle state |
| `current_step` | `INT` | No | `0` | Current step index |
| `total_steps` | `INT` | No | `0` | Total confirmation steps |
| `completed_steps` | `TEXT` | Yes | — | Serialized completed step list |
| `failure_reason` | `TEXT` | Yes | — | Failure description |
| `created_at` | `TIMESTAMPTZ` | Yes | `CURRENT_TIMESTAMP` | Saga start |
| `completed_at` | `TIMESTAMPTZ` | Yes | — | Saga completion |
| `timeout_at` | `TIMESTAMPTZ` | Yes | — | 5-minute timeout deadline |

**Saga States:** `INITIATED` → `IN_PROGRESS` → `COMPLETED` | `ROLLING_BACK` → `ROLLED_BACK` | `FAILED`

### Relationships

```
bookings (1) ──── (1..*) booking_items
bookings (1) ──── (0..1) saga_orchestration
```

---

## 4. User Input Requirements

### Booking Creation

| Field | Validation Rules |
|---|---|
| `startDate` | Required. Must be today or in the future. |
| `endDate` | Required. Must be after `startDate`. |
| `items` | Required. At least 1 item. Max recommended: 10. |
| `items[].providerType` | Required. Must be `HOTEL`, `TOUR_GUIDE`, or `VEHICLE`. |
| `items[].providerId` | Required. Must reference a valid, active provider. |
| `items[].unitPrice` | Required. Must be >= 0.01. |

### Business Constraints

- A tourist can only cancel their own bookings.
- `COMPLETED` bookings cannot be cancelled.
- Already `CANCELLED` bookings cannot be cancelled again.
- `totalAmount` is calculated server-side as the sum of all `subtotal` values.
- `bookingReference` is auto-generated and immutable.
- Saga timeout: 5 minutes from creation.

---

## 5. Inter-Service Communication

### Outbound (Booking Service → Other Services)

| Target | Endpoint | Method | Purpose |
|---|---|---|---|
| Hotel Service | `/api/hotels/{id}` | GET (Feign) | Validate hotel exists |
| Hotel Service | `/api/hotels/{id}/availability` | GET (Feign) | Check room availability |
| Tour Guide Service | `/api/tour-guides/{id}` | GET (Feign) | Validate guide exists |
| Tour Guide Service | `/api/tour-guides/{id}/availability` | GET (Feign) | Check guide availability |
| Vehicle Service | `/api/vehicles/{id}` | GET (Feign) | Validate vehicle exists |
| Vehicle Service | `/api/vehicles/{id}/availability` | GET (Feign) | Check vehicle availability |

### Outbound Events (SQS)

| Event Type | Queue | Payload |
|---|---|---|
| `BOOKING_CREATED` | `booking-queue` | Full booking with items |
| `BOOKING_CONFIRMED` | `booking-queue`, `notification-queue` | Confirmed booking details |
| `BOOKING_CANCELLED` | `booking-queue`, `notification-queue` | Cancelled booking with refund info |
| `BOOKING_REFUND_PROCESSED` | `booking-queue` | Refund amount and policy |

### Inbound (Other Services → Booking Service)

| Consumer | Endpoint | Purpose |
|---|---|---|
| Itinerary Service | `/api/bookings/{id}` | Link booking to itinerary |
| Review Service | `/api/bookings/{id}` | Validate booking for review eligibility |

### SQS Event Payload

```json
{
  "eventType": "BOOKING_CONFIRMED",
  "eventId": "uuid",
  "timestamp": "2026-02-24T10:00:00Z",
  "version": "1.0",
  "source": "booking-service",
  "payload": {
    "bookingId": 1,
    "bookingReference": "TRP-20260315-A7X3K9",
    "touristId": "tourist-uuid",
    "totalAmount": 770.00,
    "refundAmount": null,
    "refundPolicy": null,
    "cancellationReason": null,
    "items": [
      {
        "providerType": "HOTEL",
        "providerId": 12,
        "itemName": "Deluxe Ocean View - 3 nights",
        "subtotal": 185.00,
        "startDate": "2026-03-15",
        "endDate": "2026-03-18",
        "status": "CONFIRMED"
      }
    ]
  }
}
```

### Communication Details

- **Feign Timeout:** Connect 5s, Read 5s
- **JWT Propagation:** `FeignJwtInterceptor` forwards Authorization header
- **SQS Graceful Fallback:** If SQS is not configured, events are logged locally (dev mode)
- **Gateway Route:** `/api/bookings/**` → `lb://booking-service`

---

## 6. Security Considerations

### Authentication

- **Method:** Supabase JWT (HS256)
- **Public Endpoints:** `/actuator/**`, `/swagger-ui/**`, `/v3/api-docs/**`

### Role-Based Access Control

| Role | Permissions |
|---|---|
| `TOURIST` | Create, view own, cancel own bookings |
| `HOTEL_OWNER` | View bookings for own hotels, update item status |
| `TOUR_GUIDE` | View bookings for own services, update item status |
| `VEHICLE_OWNER` | View bookings for own vehicles, update item status |
| `ADMIN` | Full platform access |

### Data Validation

- Server-side Bean Validation on all request DTOs
- Tourist-ownership enforcement on booking access and cancellation
- Provider type whitelist validation (only `HOTEL`, `TOUR_GUIDE`, `VEHICLE`)
- Date range validation (endDate > startDate)

---

## 7. Error Handling Standard

### Error Response Format

```json
{
  "timestamp": "2026-02-24T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "End date must be after start date",
  "path": "/api/bookings",
  "traceId": "uuid"
}
```

### Common Error Codes

| HTTP Status | Condition |
|---|---|
| `400` | Invalid dates, invalid provider type, empty items, booking already cancelled/completed |
| `401` | Missing or invalid JWT |
| `403` | Accessing or cancelling another tourist's booking |
| `404` | Booking, booking item, or booking reference not found |
| `500` | Internal server error, saga failure |
| `503` | Provider service unavailable during saga |

---

## 8. Example Request & Response

### Create a Multi-Provider Booking

**Request:**

```bash
curl -X POST http://localhost:8086/api/bookings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..." \
  -d '{
    "startDate": "2026-04-01",
    "endDate": "2026-04-05",
    "notes": "Anniversary trip to Sigiriya and Dambulla",
    "items": [
      {
        "providerType": "HOTEL",
        "providerId": 8,
        "itemName": "Heritage Suite at Sigiriya Lodge - 4 nights",
        "quantity": 1,
        "unitPrice": 220.00
      },
      {
        "providerType": "TOUR_GUIDE",
        "providerId": 3,
        "itemName": "Cultural Heritage Guide - 3 days",
        "quantity": 1,
        "unitPrice": 140.00
      },
      {
        "providerType": "VEHICLE",
        "providerId": 7,
        "itemName": "Air-conditioned SUV - 4 days",
        "quantity": 1,
        "unitPrice": 85.00
      }
    ]
  }'
```

**Response:** `201 Created`

```json
{
  "data": {
    "id": 25,
    "touristId": "tourist-uuid-123",
    "bookingReference": "TRP-20260401-K9M2P5",
    "status": "CONFIRMED",
    "totalAmount": 980.00,
    "bookingDate": "2026-02-24T14:30:00Z",
    "startDate": "2026-04-01",
    "endDate": "2026-04-05",
    "notes": "Anniversary trip to Sigiriya and Dambulla",
    "items": [
      {
        "id": 70,
        "providerType": "HOTEL",
        "providerId": 8,
        "itemName": "Heritage Suite at Sigiriya Lodge - 4 nights",
        "quantity": 1,
        "unitPrice": 220.00,
        "subtotal": 220.00,
        "status": "CONFIRMED"
      },
      {
        "id": 71,
        "providerType": "TOUR_GUIDE",
        "providerId": 3,
        "itemName": "Cultural Heritage Guide - 3 days",
        "quantity": 1,
        "unitPrice": 140.00,
        "subtotal": 140.00,
        "status": "CONFIRMED"
      },
      {
        "id": 72,
        "providerType": "VEHICLE",
        "providerId": 7,
        "itemName": "Air-conditioned SUV - 4 days",
        "quantity": 1,
        "unitPrice": 85.00,
        "subtotal": 85.00,
        "status": "CONFIRMED"
      }
    ],
    "createdAt": "2026-02-24T14:30:00Z",
    "updatedAt": "2026-02-24T14:30:02Z"
  }
}
```

### Cancel Booking with Refund

**Request:**

```bash
curl -X POST http://localhost:8086/api/bookings/25/cancel \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..." \
  -d '{"reason": "Weather forecast shows heavy rain"}'
```

**Response:** `200 OK`

```json
{
  "data": {
    "id": 25,
    "bookingReference": "TRP-20260401-K9M2P5",
    "status": "CANCELLED",
    "totalAmount": 980.00,
    "cancellationReason": "Weather forecast shows heavy rain",
    "refundAmount": 980.00,
    "refundPolicy": "FULL_REFUND",
    "items": [
      { "id": 70, "status": "CANCELLED" },
      { "id": 71, "status": "CANCELLED" },
      { "id": 72, "status": "CANCELLED" }
    ]
  }
}
```
