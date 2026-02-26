# Tour Guide Service Specification

> **Service Name:** tour-guide-service
> **Port:** 8084
> **Package:** `com.travelplan.guide`
> **Database Schema:** `guide`
> **Version:** 1.0.0-SNAPSHOT

---

## 1. Service Overview

### Responsibility

The Tour Guide Service manages tour guide profiles, their skills, languages, specializations, rate cards, and availability schedules. It enables tour guides to register on the platform, manage their offerings, and be discoverable by tourists through the AI Agent and search interfaces.

### Key Features

- Tour guide registration and profile management (CRUD)
- Skills and specialization tagging (wildlife, cultural, adventure, etc.)
- Multi-language support for guide listings
- Hourly and daily rate configuration
- Availability schedule management
- Search and filter by language, specialization, rating, and price
- Aggregate rating updates from Review Service (via SQS events)
- Verification status management

### Dependencies

| Dependency | Purpose |
|---|---|
| `common-lib` | Shared `ApiResponse`, JWT filter, Security config, exception handling |
| Supabase Auth | JWT-based authentication and role extraction |
| Eureka Discovery Server | Service registration and discovery |
| PostgreSQL (Supabase) | Persistent storage |
| Flyway | Database migration management |

---

## 2. API Endpoints

### 2.1 Register Tour Guide

| Field | Value |
|---|---|
| **Method** | `POST` |
| **URL** | `/api/tour-guides` |
| **Description** | Registers a new tour guide profile for the authenticated user. |
| **Auth** | Bearer JWT — Role: `TOUR_GUIDE` |

**Request Body:**

```json
{
  "firstName": "Kamal",
  "lastName": "Perera",
  "email": "kamal.perera@example.com",
  "phoneNumber": "+94771234567",
  "bio": "Experienced wildlife guide with 10 years in Yala and Wilpattu national parks.",
  "languages": ["English", "Sinhala", "German"],
  "specializations": ["wildlife", "national_parks", "bird_watching"],
  "experienceYears": 10,
  "hourlyRate": 25.00,
  "dailyRate": 150.00,
  "profileImageUrl": "https://storage.example.com/kamal.jpg"
}
```

| Field | Type | Required | Validation |
|---|---|---|---|
| `firstName` | String | Yes | `@NotBlank`, max 100 chars |
| `lastName` | String | Yes | `@NotBlank`, max 100 chars |
| `email` | String | Yes | `@NotBlank`, `@Email`, unique |
| `phoneNumber` | String | No | Max 20 chars |
| `bio` | String | No | Free text |
| `languages` | String[] | No | Array of language names |
| `specializations` | String[] | No | Array of specialization tags |
| `experienceYears` | Integer | No | Min 0 |
| `hourlyRate` | BigDecimal | No | Min 0.01 |
| `dailyRate` | BigDecimal | No | Min 0.01 |
| `profileImageUrl` | String | No | Valid URL |

**Response:** `201 Created`

```json
{
  "data": {
    "id": 1,
    "userId": "supabase-guide-uuid",
    "firstName": "Kamal",
    "lastName": "Perera",
    "email": "kamal.perera@example.com",
    "phoneNumber": "+94771234567",
    "bio": "Experienced wildlife guide with 10 years in Yala and Wilpattu national parks.",
    "languages": ["English", "Sinhala", "German"],
    "specializations": ["wildlife", "national_parks", "bird_watching"],
    "experienceYears": 10,
    "hourlyRate": 25.00,
    "dailyRate": 150.00,
    "averageRating": 0.00,
    "reviewCount": 0,
    "profileImageUrl": "https://storage.example.com/kamal.jpg",
    "isVerified": false,
    "isActive": true,
    "createdAt": "2026-02-24T10:00:00Z",
    "updatedAt": "2026-02-24T10:00:00Z"
  }
}
```

---

### 2.2 Get Tour Guide by ID

| Field | Value |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/tour-guides/{id}` |
| **Description** | Retrieves a tour guide's full profile. Used by AI Agent, Booking, and frontend. |
| **Auth** | Bearer JWT |

**Path Parameters:**

| Parameter | Type | Description |
|---|---|---|
| `id` | Long | Tour guide database ID |

**Response:** `200 OK` — Full `TourGuideResponse`.

---

### 2.3 Search Tour Guides

| Field | Value |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/tour-guides` |
| **Description** | Search and filter tour guides with pagination. |
| **Auth** | Bearer JWT |

**Query Parameters:**

| Parameter | Type | Required | Description |
|---|---|---|---|
| `language` | String | No | Filter by spoken language |
| `specialization` | String | No | Filter by specialization tag |
| `minRating` | BigDecimal | No | Minimum average rating |
| `maxHourlyRate` | BigDecimal | No | Maximum hourly rate |
| `maxDailyRate` | BigDecimal | No | Maximum daily rate |
| `isVerified` | Boolean | No | Filter by verification status |
| `query` | String | No | Free-text search (name, bio, specializations) |
| `page` | Integer | No | Page number (default: 0) |
| `size` | Integer | No | Page size (default: 10) |

**Response:** `200 OK`

```json
{
  "data": [ ... ],
  "pagination": {
    "page": 0,
    "pageSize": 10,
    "totalItems": 23,
    "totalPages": 3
  }
}
```

---

### 2.4 Update Tour Guide Profile

| Field | Value |
|---|---|
| **Method** | `PUT` |
| **URL** | `/api/tour-guides/me` |
| **Description** | Updates the authenticated tour guide's profile. |
| **Auth** | Bearer JWT — Role: `TOUR_GUIDE` |

---

### 2.5 Check Availability

| Field | Value |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/tour-guides/{id}/availability` |
| **Description** | Checks if a tour guide is available for a given date range. Used by Booking Service during saga. |
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
    "guideId": 1,
    "available": true,
    "dailyRate": 150.00,
    "bookedDates": ["2026-03-10", "2026-03-11"]
  }
}
```

---

### 2.6 Get My Profile (Provider Dashboard)

| Field | Value |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/tour-guides/me` |
| **Description** | Returns the authenticated tour guide's own profile. |
| **Auth** | Bearer JWT — Role: `TOUR_GUIDE` |

---

### 2.7 Delete Tour Guide (Soft Delete)

| Field | Value |
|---|---|
| **Method** | `DELETE` |
| **URL** | `/api/tour-guides/{id}` |
| **Description** | Soft-deletes a tour guide profile. |
| **Auth** | Bearer JWT — Role: `TOUR_GUIDE` or `ADMIN` |

**Response:** `204 No Content`

---

## 3. Data Model Schema

### 3.1 `tour_guides` Table

| Column | Type | Nullable | Default | Description |
|---|---|---|---|---|
| `id` | `BIGSERIAL` | No | Auto-increment | Primary key |
| `user_id` | `VARCHAR(255)` | No | — | Supabase Auth UID (unique) |
| `first_name` | `VARCHAR(100)` | No | — | First name |
| `last_name` | `VARCHAR(100)` | No | — | Last name |
| `email` | `VARCHAR(255)` | No | — | Email (unique) |
| `phone_number` | `VARCHAR(20)` | Yes | — | Phone number |
| `bio` | `TEXT` | Yes | — | Biography / description |
| `languages` | `TEXT[]` | Yes | — | Spoken languages (PostgreSQL array) |
| `specializations` | `TEXT[]` | Yes | — | Expertise areas (PostgreSQL array) |
| `experience_years` | `INT` | Yes | `0` | Years of experience |
| `hourly_rate` | `DECIMAL(10,2)` | Yes | — | Hourly rate (USD) |
| `daily_rate` | `DECIMAL(10,2)` | Yes | — | Daily rate (USD) |
| `average_rating` | `DECIMAL(3,2)` | Yes | `0` | Aggregate review score |
| `review_count` | `INT` | Yes | `0` | Total reviews received |
| `profile_image_url` | `TEXT` | Yes | — | Profile image URL |
| `is_verified` | `BOOLEAN` | Yes | `false` | Admin verification flag |
| `is_active` | `BOOLEAN` | Yes | `true` | Soft-delete flag |
| `created_at` | `TIMESTAMPTZ` | Yes | `CURRENT_TIMESTAMP` | Record creation |
| `updated_at` | `TIMESTAMPTZ` | Yes | `CURRENT_TIMESTAMP` | Last modification |

**Indexes:**

| Index | Column(s) | Type | Purpose |
|---|---|---|---|
| `idx_tour_guides_user_id` | `user_id` | B-Tree | Fast lookup by Supabase UID |
| `idx_tour_guides_languages` | `languages` | GIN | Array-contains queries for language search |

### Relationships

```
tour_guides ── standalone entity (no FK dependencies within this service)
```

Cross-service references:
- `reviews.entity_type = 'TOUR_GUIDE'` and `reviews.entity_id → tour_guides.id`
- `booking_items.provider_type = 'TOUR_GUIDE'` and `booking_items.provider_id → tour_guides.id`

---

## 4. User Input Requirements

### Registration

| Field | Validation Rules |
|---|---|
| `firstName` | Required. 1–100 characters. |
| `lastName` | Required. 1–100 characters. |
| `email` | Required. Valid email format. Unique across all guides. |
| `languages` | Recommended. At least one language for discoverability. |
| `specializations` | Recommended. Tags from: `wildlife`, `cultural`, `adventure`, `historical`, `food_tours`, `bird_watching`, `national_parks`, `diving`, `surfing`, `hiking`. |
| `hourlyRate` / `dailyRate` | At least one rate should be set for booking calculations. |

### Business Constraints

- Only `TOUR_GUIDE` role can create and manage their own profile.
- Guides must be verified (`is_verified = true`) to appear in priority search results.
- `average_rating` and `review_count` are read-only, updated via SQS from Review Service.
- A guide cannot delete their profile while they have active bookings.

---

## 5. Inter-Service Communication

### Inbound (Other Services → Tour Guide Service)

| Consumer | Endpoint | Method | Purpose |
|---|---|---|---|
| AI Agent Service | `/api/tour-guides` (search) | GET (Feign) | Discover guides for trip planning |
| AI Agent Service | `/api/tour-guides/{id}` | GET (Feign) | Guide detail for itinerary generation |
| Booking Service | `/api/tour-guides/{id}` | GET (Feign) | Validate guide exists during booking |
| Booking Service | `/api/tour-guides/{id}/availability` | GET (Feign) | Availability check during saga |
| Review Service | Rating update event | SQS Consumer | Update `average_rating` and `review_count` |

### Outbound

None. The Tour Guide Service is a leaf service.

### Communication Method

- **Synchronous:** REST via OpenFeign (`lb://tour-guide-service`)
- **Asynchronous:** Amazon SQS for rating update events
- **Gateway Route:** `/api/tour-guides/**` → `lb://tour-guide-service`

---

## 6. Security Considerations

### Authentication

- **Method:** Supabase JWT (HS256)
- **Public Endpoints:** `/actuator/**`, `/swagger-ui/**`, `/v3/api-docs/**`

### Role-Based Access Control

| Role | Permissions |
|---|---|
| `TOURIST` | Read-only: search guides, view profiles, check availability |
| `TOUR_GUIDE` | Full CRUD on own profile |
| `ADMIN` | Full access including verification management |

### Data Validation

- Bean Validation on all DTOs
- GIN index on `languages` array prevents full-table scans for language queries
- Owner-only mutation enforcement

---

## 7. Error Handling Standard

### Error Response Format

```json
{
  "timestamp": "2026-02-24T10:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Tour guide not found with id: '99'",
  "path": "/api/tour-guides/99",
  "traceId": "uuid"
}
```

### Common Error Codes

| HTTP Status | Condition |
|---|---|
| `400` | Invalid input (bad email, negative rate) |
| `401` | Missing or invalid JWT |
| `403` | Non-owner attempting to modify profile |
| `404` | Tour guide not found |
| `409` | Duplicate email on registration |
| `500` | Internal server error |

---

## 8. Example Request & Response

### Search Tour Guides by Language and Specialization

**Request:**

```bash
curl -X GET "http://localhost:8084/api/tour-guides?language=English&specialization=wildlife&maxDailyRate=200&page=0&size=5" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..."
```

**Response:** `200 OK`

```json
{
  "data": [
    {
      "id": 1,
      "userId": "guide-uuid-001",
      "firstName": "Kamal",
      "lastName": "Perera",
      "email": "kamal.perera@example.com",
      "phoneNumber": "+94771234567",
      "bio": "Experienced wildlife guide with 10 years in Yala and Wilpattu national parks.",
      "languages": ["English", "Sinhala", "German"],
      "specializations": ["wildlife", "national_parks", "bird_watching"],
      "experienceYears": 10,
      "hourlyRate": 25.00,
      "dailyRate": 150.00,
      "averageRating": 4.75,
      "reviewCount": 64,
      "profileImageUrl": "https://storage.example.com/kamal.jpg",
      "isVerified": true,
      "isActive": true,
      "createdAt": "2025-06-10T08:00:00Z",
      "updatedAt": "2026-02-20T14:30:00Z"
    }
  ],
  "pagination": {
    "page": 0,
    "pageSize": 5,
    "totalItems": 8,
    "totalPages": 2
  }
}
```

### Check Guide Availability

**Request:**

```bash
curl -X GET "http://localhost:8084/api/tour-guides/1/availability?startDate=2026-03-15&endDate=2026-03-18" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..."
```

**Response:** `200 OK`

```json
{
  "data": {
    "guideId": 1,
    "available": true,
    "dailyRate": 150.00,
    "bookedDates": []
  }
}
```
