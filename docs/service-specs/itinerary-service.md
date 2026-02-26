# Itinerary Service Specification

> **Service Name:** itinerary-service
> **Port:** 8087
> **Package:** `com.travelplan.itinerary`
> **Database Schema:** `itinerary`
> **Version:** 1.0.0-SNAPSHOT

---

## 1. Service Overview

### Responsibility

The Itinerary Service manages trip itineraries, day-by-day schedules, activities, and expense tracking. It provides the structured trip view that transforms AI-generated plans and confirmed bookings into actionable, chronological travel schedules that tourists can view, modify, and export.

### Key Features

- Itinerary creation (manual or auto-generated from bookings)
- Day-by-day schedule management with ordered activities
- Activity CRUD with provider linking (hotel, guide, vehicle)
- Expense tracking and budget monitoring
- Itinerary status lifecycle (DRAFT → ACTIVE → COMPLETED)
- PDF export capability
- Trip completion detection

### Dependencies

| Dependency | Purpose |
|---|---|
| `common-lib` | Shared DTOs, JWT filter, Security config, exception handling |
| `booking-service` | Booking confirmation events trigger itinerary creation (SQS) |
| Supabase Auth | JWT-based authentication |
| Eureka Discovery Server | Service registration and discovery |
| PostgreSQL (Supabase) | Persistent storage |
| Flyway | Database migration management |
| OpenFeign | Service-to-service communication |

---

## 2. API Endpoints

### 2.1 Create Itinerary

| Field | Value |
|---|---|
| **Method** | `POST` |
| **URL** | `/api/itineraries` |
| **Description** | Creates a new itinerary for the authenticated tourist. |
| **Auth** | Bearer JWT — Role: `TOURIST` |

**Request Body:**

```json
{
  "title": "Sri Lanka Southern Coast Adventure",
  "description": "7-day trip exploring Galle, Mirissa, and Ella",
  "startDate": "2026-04-01",
  "endDate": "2026-04-07",
  "totalBudget": 2500.00
}
```

| Field | Type | Required | Validation |
|---|---|---|---|
| `title` | String | Yes | `@NotBlank`, max 255 chars |
| `description` | String | No | Free text |
| `startDate` | LocalDate | Yes | Must be today or future |
| `endDate` | LocalDate | Yes | Must be after startDate |
| `totalBudget` | BigDecimal | No | Min 0 |

**Response:** `201 Created`

```json
{
  "data": {
    "id": 1,
    "touristId": "tourist-uuid",
    "title": "Sri Lanka Southern Coast Adventure",
    "description": "7-day trip exploring Galle, Mirissa, and Ella",
    "startDate": "2026-04-01",
    "endDate": "2026-04-07",
    "status": "DRAFT",
    "totalBudget": 2500.00,
    "actualSpent": 0.00,
    "days": [],
    "createdAt": "2026-02-24T10:00:00Z",
    "updatedAt": "2026-02-24T10:00:00Z"
  }
}
```

---

### 2.2 Get Itinerary by ID

| Field | Value |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/itineraries/{id}` |
| **Description** | Retrieves a full itinerary with days, activities, and expenses. |
| **Auth** | Bearer JWT |

**Response:** `200 OK`

```json
{
  "data": {
    "id": 1,
    "touristId": "tourist-uuid",
    "title": "Sri Lanka Southern Coast Adventure",
    "status": "ACTIVE",
    "totalBudget": 2500.00,
    "actualSpent": 870.00,
    "days": [
      {
        "id": 1,
        "dayNumber": 1,
        "date": "2026-04-01",
        "notes": "Arrival day - settle in at Galle",
        "activities": [
          {
            "id": 1,
            "activityType": "ACCOMMODATION",
            "providerType": "HOTEL",
            "providerId": 12,
            "title": "Check-in at Galle Fort Hotel",
            "description": "Heritage boutique hotel inside the fort",
            "startTime": "14:00",
            "endTime": null,
            "location": "Galle Fort",
            "estimatedCost": 120.00,
            "bookingId": 25,
            "sortOrder": 0
          },
          {
            "id": 2,
            "activityType": "SIGHTSEEING",
            "providerType": null,
            "providerId": null,
            "title": "Explore Galle Fort at sunset",
            "description": "Walk along the ramparts and visit the lighthouse",
            "startTime": "17:00",
            "endTime": "19:00",
            "location": "Galle Fort Ramparts",
            "estimatedCost": 0.00,
            "bookingId": null,
            "sortOrder": 1
          }
        ]
      }
    ],
    "createdAt": "2026-02-24T10:00:00Z",
    "updatedAt": "2026-02-24T12:30:00Z"
  }
}
```

---

### 2.3 Get Tourist Itineraries

| Field | Value |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/itineraries` |
| **Description** | Lists all itineraries for the authenticated tourist. |
| **Auth** | Bearer JWT — Role: `TOURIST` |

**Query Parameters:**

| Parameter | Type | Required | Description |
|---|---|---|---|
| `status` | String | No | Filter: `DRAFT`, `ACTIVE`, `COMPLETED` |
| `page` | Integer | No | Page number (default: 0) |
| `size` | Integer | No | Page size (default: 10) |

---

### 2.4 Update Itinerary

| Field | Value |
|---|---|
| **Method** | `PUT` |
| **URL** | `/api/itineraries/{id}` |
| **Description** | Updates itinerary metadata (title, description, budget). |
| **Auth** | Bearer JWT — Role: `TOURIST` |

---

### 2.5 Add Day to Itinerary

| Field | Value |
|---|---|
| **Method** | `POST` |
| **URL** | `/api/itineraries/{id}/days` |
| **Description** | Adds a new day to the itinerary schedule. |
| **Auth** | Bearer JWT — Role: `TOURIST` |

**Request Body:**

```json
{
  "dayNumber": 2,
  "date": "2026-04-02",
  "notes": "Whale watching in Mirissa"
}
```

---

### 2.6 Add Activity to Day

| Field | Value |
|---|---|
| **Method** | `POST` |
| **URL** | `/api/itineraries/{itineraryId}/days/{dayId}/activities` |
| **Description** | Adds an activity to a specific day. Can be linked to a booking or custom. |
| **Auth** | Bearer JWT — Role: `TOURIST` |

**Request Body:**

```json
{
  "activityType": "TOUR",
  "providerType": "TOUR_GUIDE",
  "providerId": 1,
  "title": "Whale Watching Excursion",
  "description": "Morning blue whale watching with experienced guide",
  "startTime": "06:00",
  "endTime": "11:00",
  "location": "Mirissa Harbour",
  "estimatedCost": 75.00,
  "bookingId": 25,
  "sortOrder": 0
}
```

| Field | Type | Required |
|---|---|---|
| `activityType` | String | Yes (`ACCOMMODATION`, `TRANSPORT`, `TOUR`, `SIGHTSEEING`, `DINING`, `CUSTOM`) |
| `providerType` | String | No (required if linked to booking) |
| `providerId` | Long | No |
| `title` | String | Yes |
| `startTime` | String (HH:mm) | No |
| `endTime` | String (HH:mm) | No |
| `location` | String | No |
| `estimatedCost` | BigDecimal | No |
| `bookingId` | Long | No |
| `sortOrder` | Integer | No (default: 0) |

---

### 2.7 Add Expense

| Field | Value |
|---|---|
| **Method** | `POST` |
| **URL** | `/api/itineraries/{id}/expenses` |
| **Description** | Records an expense against the itinerary for budget tracking. |
| **Auth** | Bearer JWT — Role: `TOURIST` |

**Request Body:**

```json
{
  "category": "FOOD",
  "description": "Dinner at Ministry of Crab, Colombo",
  "amount": 65.00,
  "expenseDate": "2026-04-01"
}
```

| Field | Type | Required |
|---|---|---|
| `category` | String | Yes (`ACCOMMODATION`, `TRANSPORT`, `FOOD`, `ACTIVITY`, `SHOPPING`, `MISC`) |
| `description` | String | No |
| `amount` | BigDecimal | Yes |
| `expenseDate` | LocalDate | Yes |

---

### 2.8 Delete Itinerary

| Field | Value |
|---|---|
| **Method** | `DELETE` |
| **URL** | `/api/itineraries/{id}` |
| **Description** | Deletes an itinerary and all associated days, activities, and expenses. |
| **Auth** | Bearer JWT — Role: `TOURIST` |

**Response:** `204 No Content`

---

## 3. Data Model Schema

### 3.1 `itineraries` Table

| Column | Type | Nullable | Default | Description |
|---|---|---|---|---|
| `id` | `BIGSERIAL` | No | Auto-increment | Primary key |
| `tourist_id` | `VARCHAR(255)` | No | — | Supabase UID |
| `title` | `VARCHAR(255)` | No | — | Trip title |
| `description` | `TEXT` | Yes | — | Trip description |
| `start_date` | `DATE` | No | — | Trip start |
| `end_date` | `DATE` | No | — | Trip end |
| `status` | `VARCHAR(50)` | No | `DRAFT` | `DRAFT`, `ACTIVE`, `COMPLETED` |
| `total_budget` | `DECIMAL(12,2)` | Yes | — | Planned budget |
| `actual_spent` | `DECIMAL(12,2)` | Yes | `0` | Running expense total |
| `created_at` | `TIMESTAMPTZ` | Yes | `CURRENT_TIMESTAMP` | Record creation |
| `updated_at` | `TIMESTAMPTZ` | Yes | `CURRENT_TIMESTAMP` | Last modification |

**Indexes:**

| Index | Column(s) | Purpose |
|---|---|---|
| `idx_itineraries_tourist_id` | `tourist_id` | Tourist itinerary lookup |

### 3.2 `itinerary_days` Table

| Column | Type | Nullable | Default | Description |
|---|---|---|---|---|
| `id` | `BIGSERIAL` | No | Auto-increment | Primary key |
| `itinerary_id` | `BIGINT` | No | — | FK → `itineraries.id` (cascade delete) |
| `day_number` | `INT` | No | — | Day sequence (1-indexed) |
| `date` | `DATE` | No | — | Calendar date |
| `notes` | `TEXT` | Yes | — | Day-level notes |
| `created_at` | `TIMESTAMPTZ` | Yes | `CURRENT_TIMESTAMP` | Record creation |

**Indexes:**

| Index | Column(s) | Purpose |
|---|---|---|
| `idx_itinerary_days_itinerary_id` | `itinerary_id` | Days per itinerary |

### 3.3 `itinerary_activities` Table

| Column | Type | Nullable | Default | Description |
|---|---|---|---|---|
| `id` | `BIGSERIAL` | No | Auto-increment | Primary key |
| `day_id` | `BIGINT` | No | — | FK → `itinerary_days.id` (cascade delete) |
| `activity_type` | `VARCHAR(50)` | No | — | Activity category |
| `provider_type` | `VARCHAR(50)` | Yes | — | `HOTEL`, `TOUR_GUIDE`, `VEHICLE` |
| `provider_id` | `BIGINT` | Yes | — | Provider entity ID |
| `title` | `VARCHAR(255)` | No | — | Activity title |
| `description` | `TEXT` | Yes | — | Activity description |
| `start_time` | `TIME` | Yes | — | Planned start time |
| `end_time` | `TIME` | Yes | — | Planned end time |
| `location` | `TEXT` | Yes | — | Location name/address |
| `estimated_cost` | `DECIMAL(10,2)` | Yes | — | Expected cost |
| `booking_id` | `BIGINT` | Yes | — | Linked booking ID |
| `sort_order` | `INT` | Yes | `0` | Display ordering |
| `created_at` | `TIMESTAMPTZ` | Yes | `CURRENT_TIMESTAMP` | Record creation |

### 3.4 `expenses` Table

| Column | Type | Nullable | Default | Description |
|---|---|---|---|---|
| `id` | `BIGSERIAL` | No | Auto-increment | Primary key |
| `itinerary_id` | `BIGINT` | No | — | FK → `itineraries.id` (cascade delete) |
| `category` | `VARCHAR(50)` | No | — | Expense category |
| `description` | `TEXT` | Yes | — | Expense description |
| `amount` | `DECIMAL(10,2)` | No | — | Amount spent |
| `expense_date` | `DATE` | No | — | Date of expense |
| `created_at` | `TIMESTAMPTZ` | Yes | `CURRENT_TIMESTAMP` | Record creation |

**Indexes:**

| Index | Column(s) | Purpose |
|---|---|---|
| `idx_expenses_itinerary_id` | `itinerary_id` | Expenses per itinerary |

### Relationships

```
itineraries (1) ──── (0..*) itinerary_days
itinerary_days (1) ──── (0..*) itinerary_activities
itineraries (1) ──── (0..*) expenses
```

---

## 4. User Input Requirements

### Itinerary Creation

| Field | Validation Rules |
|---|---|
| `title` | Required. 1–255 characters. |
| `startDate` | Required. Must be a valid future or present date. |
| `endDate` | Required. Must be after `startDate`. |
| `totalBudget` | Optional. Positive decimal. |

### Activity Creation

| Field | Validation Rules |
|---|---|
| `activityType` | Required. One of: `ACCOMMODATION`, `TRANSPORT`, `TOUR`, `SIGHTSEEING`, `DINING`, `CUSTOM`. |
| `title` | Required. 1–255 characters. |
| `startTime` / `endTime` | Optional. HH:mm format. `endTime` must be after `startTime` if both provided. |
| `estimatedCost` | Optional. Non-negative. |

### Business Constraints

- Tourist can only view and manage their own itineraries.
- Day numbers must be sequential and match the date range.
- `actual_spent` is automatically recalculated when expenses are added or removed.
- Activities within a day are ordered by `sort_order`, then `start_time`.

---

## 5. Inter-Service Communication

### Inbound (Other Services → Itinerary Service)

| Consumer | Method | Purpose |
|---|---|---|
| Booking Service | SQS Event (`BOOKING_CONFIRMED`) | Auto-create itinerary from confirmed booking |
| AI Agent Service | REST (Feign) | Create itinerary from AI-generated trip plan |

### Outbound (Itinerary Service → Other Services)

| Target | Endpoint | Method | Purpose |
|---|---|---|---|
| Booking Service | `/api/bookings/{id}` | GET (Feign) | Retrieve booking details for itinerary linking |

### Communication Method

- **Synchronous:** REST via OpenFeign
- **Asynchronous:** Amazon SQS for booking events
- **Gateway Route:** `/api/itineraries/**` → `lb://itinerary-service`

---

## 6. Security Considerations

### Authentication

- **Method:** Supabase JWT (HS256)
- **Public Endpoints:** `/actuator/**`, `/swagger-ui/**`, `/v3/api-docs/**`

### Role-Based Access Control

| Role | Permissions |
|---|---|
| `TOURIST` | Full CRUD on own itineraries, days, activities, expenses |
| `ADMIN` | Full platform access |

### Data Validation

- Bean Validation on all DTOs
- Tourist-ownership enforcement on all operations
- Cascade delete protects referential integrity

---

## 7. Error Handling Standard

### Error Response Format

```json
{
  "timestamp": "2026-02-24T10:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Itinerary not found with id: '99'",
  "path": "/api/itineraries/99",
  "traceId": "uuid"
}
```

### Common Error Codes

| HTTP Status | Condition |
|---|---|
| `400` | Invalid dates, missing title, invalid activity type |
| `401` | Missing or invalid JWT |
| `403` | Accessing another tourist's itinerary |
| `404` | Itinerary, day, or activity not found |
| `500` | Internal server error |

---

## 8. Example Request & Response

### Create Itinerary with Days

**Request:**

```bash
curl -X POST http://localhost:8087/api/itineraries \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..." \
  -d '{
    "title": "Cultural Triangle Explorer",
    "description": "3-day trip to Sigiriya, Polonnaruwa, and Dambulla",
    "startDate": "2026-04-10",
    "endDate": "2026-04-12",
    "totalBudget": 800.00
  }'
```

**Response:** `201 Created`

```json
{
  "data": {
    "id": 15,
    "touristId": "tourist-uuid",
    "title": "Cultural Triangle Explorer",
    "description": "3-day trip to Sigiriya, Polonnaruwa, and Dambulla",
    "startDate": "2026-04-10",
    "endDate": "2026-04-12",
    "status": "DRAFT",
    "totalBudget": 800.00,
    "actualSpent": 0.00,
    "days": [],
    "createdAt": "2026-02-24T11:00:00Z",
    "updatedAt": "2026-02-24T11:00:00Z"
  }
}
```

### Record an Expense

**Request:**

```bash
curl -X POST http://localhost:8087/api/itineraries/15/expenses \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..." \
  -d '{
    "category": "TRANSPORT",
    "description": "Tuk-tuk from Dambulla to Sigiriya",
    "amount": 12.00,
    "expenseDate": "2026-04-10"
  }'
```

**Response:** `201 Created`

```json
{
  "data": {
    "id": 42,
    "category": "TRANSPORT",
    "description": "Tuk-tuk from Dambulla to Sigiriya",
    "amount": 12.00,
    "expenseDate": "2026-04-10",
    "createdAt": "2026-04-10T09:30:00Z"
  }
}
```
