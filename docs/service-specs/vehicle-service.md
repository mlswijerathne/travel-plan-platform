# Vehicle Service Specification

> **Service Name:** vehicle-service
> **Port:** 8085
> **Package:** `com.travelplan.vehicle`
> **Database Schema:** `vehicle`
> **Version:** 1.0.0-SNAPSHOT

---

## 1. Service Overview

### Responsibility

The Vehicle Service manages vehicle rental listings, fleet inventory, availability tracking, and rating aggregations for vehicle owners on the platform. It provides vehicle search, availability checking, and booking support for transportation needs within Sri Lanka.

### Key Features

- Vehicle registration and fleet management (CRUD)
- Vehicle type categorization (car, van, bus, tuk-tuk, motorbike)
- Daily rate pricing configuration
- Availability tracking with date-range queries
- Search and filter by type, capacity, features, price, and rating
- Aggregate rating updates from Review Service (via SQS events)
- Owner dashboard with fleet overview

### Dependencies

| Dependency | Purpose |
|---|---|
| `common-lib` | Shared `ApiResponse`, JWT filter, Security config, exception handling |
| Supabase Auth | JWT-based authentication |
| Eureka Discovery Server | Service registration and discovery |
| PostgreSQL (Supabase) | Persistent storage |
| Flyway | Database migration management |

---

## 2. API Endpoints

### 2.1 Register Vehicle

| Field | Value |
|---|---|
| **Method** | `POST` |
| **URL** | `/api/vehicles` |
| **Description** | Registers a new vehicle for the authenticated vehicle owner. |
| **Auth** | Bearer JWT — Role: `VEHICLE_OWNER` |

**Request Body:**

```json
{
  "vehicleType": "SUV",
  "make": "Toyota",
  "model": "Land Cruiser",
  "year": 2024,
  "licensePlate": "WP-CAB-1234",
  "seatingCapacity": 7,
  "dailyRate": 85.00,
  "features": ["air_conditioning", "gps", "bluetooth", "roof_rack", "4wd"],
  "images": [
    "https://storage.example.com/vehicles/lc-front.jpg",
    "https://storage.example.com/vehicles/lc-interior.jpg"
  ]
}
```

| Field | Type | Required | Validation |
|---|---|---|---|
| `vehicleType` | String | Yes | `@NotBlank`, max 50 chars. Values: `CAR`, `SUV`, `VAN`, `BUS`, `TUK_TUK`, `MOTORBIKE` |
| `make` | String | Yes | `@NotBlank`, max 100 chars |
| `model` | String | Yes | `@NotBlank`, max 100 chars |
| `year` | Integer | No | Reasonable year range |
| `licensePlate` | String | Yes | `@NotBlank`, max 20 chars, unique |
| `seatingCapacity` | Integer | Yes | Min 1 |
| `dailyRate` | BigDecimal | Yes | Min 0.01 |
| `features` | String[] | No | Array of feature tags |
| `images` | String[] | No | Array of image URLs |

**Response:** `201 Created`

```json
{
  "data": {
    "id": 1,
    "ownerId": "supabase-owner-uuid",
    "vehicleType": "SUV",
    "make": "Toyota",
    "model": "Land Cruiser",
    "year": 2024,
    "licensePlate": "WP-CAB-1234",
    "seatingCapacity": 7,
    "dailyRate": 85.00,
    "features": ["air_conditioning", "gps", "bluetooth", "roof_rack", "4wd"],
    "images": ["https://storage.example.com/vehicles/lc-front.jpg"],
    "averageRating": 0.00,
    "reviewCount": 0,
    "isAvailable": true,
    "isActive": true,
    "createdAt": "2026-02-24T10:00:00Z",
    "updatedAt": "2026-02-24T10:00:00Z"
  }
}
```

---

### 2.2 Get Vehicle by ID

| Field | Value |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/vehicles/{id}` |
| **Description** | Retrieves full vehicle details. Used by AI Agent, Booking, and frontend. |
| **Auth** | Bearer JWT |

**Path Parameters:**

| Parameter | Type | Description |
|---|---|---|
| `id` | Long | Vehicle database ID |

**Response:** `200 OK` — Full `VehicleResponse`.

---

### 2.3 Search Vehicles

| Field | Value |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/vehicles` |
| **Description** | Search and filter vehicles with pagination. |
| **Auth** | Bearer JWT |

**Query Parameters:**

| Parameter | Type | Required | Description |
|---|---|---|---|
| `vehicleType` | String | No | Filter by vehicle type |
| `minCapacity` | Integer | No | Minimum seating capacity |
| `maxDailyRate` | BigDecimal | No | Maximum daily rate |
| `minDailyRate` | BigDecimal | No | Minimum daily rate |
| `features` | String | No | Comma-separated feature list |
| `startDate` | LocalDate | No | Available from date |
| `endDate` | LocalDate | No | Available until date |
| `query` | String | No | Free-text search |
| `page` | Integer | No | Page number (default: 0) |
| `size` | Integer | No | Page size (default: 10) |

**Response:** `200 OK` — Paginated `VehicleResponse` list.

---

### 2.4 Update Vehicle

| Field | Value |
|---|---|
| **Method** | `PUT` |
| **URL** | `/api/vehicles/{id}` |
| **Description** | Updates vehicle details. Only the owning vehicle owner may update. |
| **Auth** | Bearer JWT — Role: `VEHICLE_OWNER` |

---

### 2.5 Delete Vehicle (Soft Delete)

| Field | Value |
|---|---|
| **Method** | `DELETE` |
| **URL** | `/api/vehicles/{id}` |
| **Description** | Soft-deletes a vehicle by setting `is_active = false`. |
| **Auth** | Bearer JWT — Role: `VEHICLE_OWNER` |

**Response:** `204 No Content`

---

### 2.6 Check Availability

| Field | Value |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/vehicles/{id}/availability` |
| **Description** | Checks vehicle availability for a date range. Used by Booking Service during saga. |
| **Auth** | Bearer JWT |

**Query Parameters:**

| Parameter | Type | Required |
|---|---|---|
| `startDate` | LocalDate | Yes |
| `endDate` | LocalDate | Yes |

**Response:** `200 OK`

```json
{
  "data": {
    "vehicleId": 1,
    "available": true,
    "dailyRate": 85.00,
    "bookedDates": ["2026-03-20", "2026-03-21"]
  }
}
```

---

### 2.7 Get Owner's Vehicles

| Field | Value |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/vehicles/owner` |
| **Description** | Lists all vehicles for the authenticated owner. Provider dashboard. |
| **Auth** | Bearer JWT — Role: `VEHICLE_OWNER` |

---

## 3. Data Model Schema

### 3.1 `vehicles` Table

| Column | Type | Nullable | Default | Description |
|---|---|---|---|---|
| `id` | `BIGSERIAL` | No | Auto-increment | Primary key |
| `owner_id` | `VARCHAR(255)` | No | — | Supabase UID of vehicle owner |
| `vehicle_type` | `VARCHAR(50)` | No | — | Vehicle category |
| `make` | `VARCHAR(100)` | No | — | Manufacturer |
| `model` | `VARCHAR(100)` | No | — | Model name |
| `year` | `INT` | Yes | — | Manufacture year |
| `license_plate` | `VARCHAR(20)` | No | — | License plate (unique) |
| `seating_capacity` | `INT` | No | — | Passenger capacity |
| `daily_rate` | `DECIMAL(10,2)` | No | — | Daily rental rate (USD) |
| `features` | `TEXT[]` | Yes | — | Feature tags array |
| `images` | `TEXT[]` | Yes | — | Image URL array |
| `average_rating` | `DECIMAL(3,2)` | Yes | `0` | Aggregate review score |
| `review_count` | `INT` | Yes | `0` | Total reviews received |
| `is_available` | `BOOLEAN` | Yes | `true` | Current availability flag |
| `is_active` | `BOOLEAN` | Yes | `true` | Soft-delete flag |
| `created_at` | `TIMESTAMPTZ` | Yes | `CURRENT_TIMESTAMP` | Record creation |
| `updated_at` | `TIMESTAMPTZ` | Yes | `CURRENT_TIMESTAMP` | Last modification |

**Indexes:**

| Index | Column(s) | Purpose |
|---|---|---|
| `idx_vehicles_owner_id` | `owner_id` | Owner fleet lookup |
| `idx_vehicles_type` | `vehicle_type` | Type-based search |

**Constraints:**

- `license_plate UNIQUE` — No duplicate plates.

### Relationships

```
vehicles ── standalone entity (no FK dependencies within this service)
```

Cross-service references:
- `reviews.entity_type = 'VEHICLE'` and `reviews.entity_id → vehicles.id`
- `booking_items.provider_type = 'VEHICLE'` and `booking_items.provider_id → vehicles.id`

---

## 4. User Input Requirements

### Vehicle Registration

| Field | Validation Rules |
|---|---|
| `vehicleType` | Required. One of: `CAR`, `SUV`, `VAN`, `BUS`, `TUK_TUK`, `MOTORBIKE`. |
| `make` | Required. 1–100 characters. |
| `model` | Required. 1–100 characters. |
| `licensePlate` | Required. 1–20 characters. Unique across all vehicles. |
| `seatingCapacity` | Required. Integer >= 1. |
| `dailyRate` | Required. Decimal > 0. |
| `images` | Recommended. At least 1 image for listing quality. |

### Business Constraints

- Only `VEHICLE_OWNER` role can create, update, or delete vehicles.
- Owners can only modify their own vehicles.
- `license_plate` must be unique across the platform.
- `average_rating` and `review_count` are system-managed (via SQS from Review Service).
- A vehicle cannot be deleted while it has active bookings.

---

## 5. Inter-Service Communication

### Inbound (Other Services → Vehicle Service)

| Consumer | Endpoint | Method | Purpose |
|---|---|---|---|
| AI Agent Service | `/api/vehicles` (search) | GET (Feign) | Discover vehicles for trip planning |
| AI Agent Service | `/api/vehicles/{id}` | GET (Feign) | Vehicle detail for itinerary generation |
| Booking Service | `/api/vehicles/{id}` | GET (Feign) | Validate vehicle exists during booking |
| Booking Service | `/api/vehicles/{id}/availability` | GET (Feign) | Availability check during saga |
| Review Service | Rating update event | SQS Consumer | Update `average_rating` and `review_count` |

### Outbound

None. The Vehicle Service is a leaf service.

### Communication Method

- **Synchronous:** REST via OpenFeign (`lb://vehicle-service`)
- **Asynchronous:** Amazon SQS for rating update events
- **Gateway Route:** `/api/vehicles/**` → `lb://vehicle-service`

---

## 6. Security Considerations

### Authentication

- **Method:** Supabase JWT (HS256)
- **Public Endpoints:** `/actuator/**`, `/swagger-ui/**`, `/v3/api-docs/**`

### Role-Based Access Control

| Role | Permissions |
|---|---|
| `TOURIST` | Read-only: search vehicles, view details, check availability |
| `VEHICLE_OWNER` | Full CRUD on own vehicles |
| `ADMIN` | Full platform access |

### Data Validation

- Bean Validation on all DTOs
- Unique constraint on `license_plate` at database level
- Owner authorization on all write operations

---

## 7. Error Handling Standard

### Error Response Format

```json
{
  "timestamp": "2026-02-24T10:00:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "Vehicle already exists with licensePlate: 'WP-CAB-1234'",
  "path": "/api/vehicles",
  "traceId": "uuid"
}
```

### Common Error Codes

| HTTP Status | Condition |
|---|---|
| `400` | Invalid input (missing make, negative rate) |
| `401` | Missing or invalid JWT |
| `403` | Non-owner attempting to modify vehicle |
| `404` | Vehicle not found |
| `409` | Duplicate license plate |
| `500` | Internal server error |

---

## 8. Example Request & Response

### Search Vehicles for a Group Trip

**Request:**

```bash
curl -X GET "http://localhost:8085/api/vehicles?vehicleType=VAN&minCapacity=8&maxDailyRate=120&page=0&size=5" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..."
```

**Response:** `200 OK`

```json
{
  "data": [
    {
      "id": 5,
      "ownerId": "owner-uuid-789",
      "vehicleType": "VAN",
      "make": "Toyota",
      "model": "HiAce",
      "year": 2023,
      "licensePlate": "WP-VAN-5678",
      "seatingCapacity": 12,
      "dailyRate": 95.00,
      "features": ["air_conditioning", "gps", "bluetooth", "luggage_rack"],
      "images": ["https://storage.example.com/vehicles/hiace.jpg"],
      "averageRating": 4.60,
      "reviewCount": 34,
      "isAvailable": true,
      "isActive": true,
      "createdAt": "2025-11-01T08:00:00Z",
      "updatedAt": "2026-02-18T09:30:00Z"
    }
  ],
  "pagination": {
    "page": 0,
    "pageSize": 5,
    "totalItems": 3,
    "totalPages": 1
  }
}
```

### Register a New Vehicle

**Request:**

```bash
curl -X POST http://localhost:8085/api/vehicles \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..." \
  -d '{
    "vehicleType": "TUK_TUK",
    "make": "Bajaj",
    "model": "RE",
    "year": 2025,
    "licensePlate": "SP-TK-9012",
    "seatingCapacity": 3,
    "dailyRate": 25.00,
    "features": ["rain_cover", "bluetooth_speaker"],
    "images": ["https://storage.example.com/vehicles/tuktuk.jpg"]
  }'
```

**Response:** `201 Created`

```json
{
  "data": {
    "id": 15,
    "ownerId": "owner-uuid-tuktuk",
    "vehicleType": "TUK_TUK",
    "make": "Bajaj",
    "model": "RE",
    "year": 2025,
    "licensePlate": "SP-TK-9012",
    "seatingCapacity": 3,
    "dailyRate": 25.00,
    "features": ["rain_cover", "bluetooth_speaker"],
    "images": ["https://storage.example.com/vehicles/tuktuk.jpg"],
    "averageRating": 0.00,
    "reviewCount": 0,
    "isAvailable": true,
    "isActive": true,
    "createdAt": "2026-02-24T10:30:00Z",
    "updatedAt": "2026-02-24T10:30:00Z"
  }
}
```
