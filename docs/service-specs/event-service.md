# Event Service Specification

> **Service Name:** event-service
> **Port:** 8090
> **Package:** `com.travelplan.event`
> **Database Schema:** `event`
> **Version:** 1.0.0-SNAPSHOT

---

## 1. Service Overview

### Responsibility

The Event Service manages local events and cultural activities happening across Sri Lanka. It provides a simplified event management system (MVP scope) that allows administrators to create and manage events, and tourists to browse and discover events by location, date, and type.

### Key Features

- Event CRUD operations (admin-managed)
- Event browsing by location, date range, and type
- Geo-location support with latitude/longitude
- Featured event promotion
- Event type categorization (festival, cultural, sports, nature, food, music)
- Image gallery support

### Dependencies

| Dependency | Purpose |
|---|---|
| `common-lib` | Shared DTOs, JWT filter, Security config, exception handling |
| Supabase Auth | JWT-based authentication |
| Eureka Discovery Server | Service registration and discovery |
| PostgreSQL (Supabase) | Persistent storage |
| Flyway | Database migration management |

---

## 2. API Endpoints

### 2.1 Create Event

| Field | Value |
|---|---|
| **Method** | `POST` |
| **URL** | `/api/events` |
| **Description** | Creates a new event. Admin-only operation. |
| **Auth** | Bearer JWT — Role: `ADMIN` |

**Request Body:**

```json
{
  "title": "Kandy Esala Perahera",
  "description": "The grand annual Buddhist procession featuring elaborately decorated elephants, traditional dancers, and fire performers.",
  "eventType": "FESTIVAL",
  "location": "Temple of the Sacred Tooth Relic, Kandy",
  "latitude": 7.2936,
  "longitude": 80.6350,
  "startDate": "2026-08-10T18:00:00Z",
  "endDate": "2026-08-20T23:00:00Z",
  "images": [
    "https://storage.example.com/events/perahera-1.jpg",
    "https://storage.example.com/events/perahera-2.jpg"
  ],
  "isFeatured": true
}
```

| Field | Type | Required | Validation |
|---|---|---|---|
| `title` | String | Yes | `@NotBlank`, max 255 chars |
| `description` | String | No | Free text |
| `eventType` | String | No | `FESTIVAL`, `CULTURAL`, `SPORTS`, `NATURE`, `FOOD`, `MUSIC` |
| `location` | String | Yes | `@NotBlank` |
| `latitude` | BigDecimal | No | -90 to 90 |
| `longitude` | BigDecimal | No | -180 to 180 |
| `startDate` | Instant | Yes | Must be in the future |
| `endDate` | Instant | Yes | Must be after startDate |
| `images` | String[] | No | Image URL array |
| `isFeatured` | Boolean | No | Default: false |

**Response:** `201 Created`

```json
{
  "data": {
    "id": 1,
    "title": "Kandy Esala Perahera",
    "description": "The grand annual Buddhist procession...",
    "eventType": "FESTIVAL",
    "location": "Temple of the Sacred Tooth Relic, Kandy",
    "latitude": 7.29360000,
    "longitude": 80.63500000,
    "startDate": "2026-08-10T18:00:00Z",
    "endDate": "2026-08-20T23:00:00Z",
    "images": ["https://storage.example.com/events/perahera-1.jpg"],
    "isFeatured": true,
    "isActive": true,
    "createdBy": "admin-uuid",
    "createdAt": "2026-02-24T10:00:00Z",
    "updatedAt": "2026-02-24T10:00:00Z"
  }
}
```

---

### 2.2 Get Event by ID

| Field | Value |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/events/{id}` |
| **Description** | Retrieves full event details. |
| **Auth** | Bearer JWT |

---

### 2.3 Browse Events

| Field | Value |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/events` |
| **Description** | Search and filter events with pagination. |
| **Auth** | Bearer JWT |

**Query Parameters:**

| Parameter | Type | Required | Description |
|---|---|---|---|
| `eventType` | String | No | Filter by event type |
| `location` | String | No | Location text search |
| `startDate` | Instant | No | Events starting on or after |
| `endDate` | Instant | No | Events ending on or before |
| `isFeatured` | Boolean | No | Featured events only |
| `latitude` | BigDecimal | No | Center latitude for geo search |
| `longitude` | BigDecimal | No | Center longitude for geo search |
| `radiusKm` | BigDecimal | No | Search radius in kilometers |
| `page` | Integer | No | Page number (default: 0) |
| `size` | Integer | No | Page size (default: 10) |

**Response:** `200 OK` — Paginated `EventResponse` list.

---

### 2.4 Update Event

| Field | Value |
|---|---|
| **Method** | `PUT` |
| **URL** | `/api/events/{id}` |
| **Description** | Updates event details. |
| **Auth** | Bearer JWT — Role: `ADMIN` |

---

### 2.5 Delete Event (Soft Delete)

| Field | Value |
|---|---|
| **Method** | `DELETE` |
| **URL** | `/api/events/{id}` |
| **Description** | Soft-deletes an event. |
| **Auth** | Bearer JWT — Role: `ADMIN` |

**Response:** `204 No Content`

---

## 3. Data Model Schema

### 3.1 `events` Table

| Column | Type | Nullable | Default | Description |
|---|---|---|---|---|
| `id` | `BIGSERIAL` | No | Auto-increment | Primary key |
| `title` | `VARCHAR(255)` | No | — | Event title |
| `description` | `TEXT` | Yes | — | Event description |
| `event_type` | `VARCHAR(50)` | Yes | — | Event category |
| `location` | `TEXT` | No | — | Venue / address |
| `latitude` | `DECIMAL(10,8)` | Yes | — | GPS latitude |
| `longitude` | `DECIMAL(11,8)` | Yes | — | GPS longitude |
| `start_date` | `TIMESTAMPTZ` | No | — | Event start |
| `end_date` | `TIMESTAMPTZ` | No | — | Event end |
| `images` | `TEXT[]` | Yes | — | Image URL array |
| `is_featured` | `BOOLEAN` | Yes | `false` | Featured flag |
| `is_active` | `BOOLEAN` | Yes | `true` | Soft-delete flag |
| `created_by` | `VARCHAR(255)` | No | — | Admin UID |
| `created_at` | `TIMESTAMPTZ` | Yes | `CURRENT_TIMESTAMP` | Record creation |
| `updated_at` | `TIMESTAMPTZ` | Yes | `CURRENT_TIMESTAMP` | Last modification |

**Indexes:**

| Index | Column(s) | Purpose |
|---|---|---|
| `idx_events_start_date` | `start_date` | Chronological event listing |
| `idx_events_is_active` | `is_active` | Active event queries |
| `idx_events_location` | `latitude, longitude` | Geo-proximity queries |

---

## 4. User Input Requirements

### Event Creation

| Field | Validation Rules |
|---|---|
| `title` | Required. 1–255 characters. |
| `location` | Required. Non-empty text. |
| `startDate` | Required. Must be in the future. |
| `endDate` | Required. Must be after startDate. |
| `eventType` | Optional. One of: `FESTIVAL`, `CULTURAL`, `SPORTS`, `NATURE`, `FOOD`, `MUSIC`. |
| `latitude` / `longitude` | Optional. Valid GPS coordinates. |

### Business Constraints

- Only `ADMIN` role can create, update, or delete events.
- Events are displayed to tourists in chronological order by default.
- Past events remain in the database but are not shown in default queries.
- No booking integration in MVP — events are informational only.

---

## 5. Inter-Service Communication

### Inbound

| Consumer | Endpoint | Purpose |
|---|---|---|
| Frontend | `/api/events/**` (via gateway) | Tourist event browsing |

### Outbound

None. The Event Service is a standalone, leaf service.

### Communication Method

- **Gateway Route:** `/api/events/**` → `lb://event-service`
- No Feign clients or SQS integration in MVP.

---

## 6. Security Considerations

### Authentication

- **Method:** Supabase JWT (HS256)
- **Public Endpoints:** `/actuator/**`, `/swagger-ui/**`, `/v3/api-docs/**`

### Role-Based Access Control

| Role | Permissions |
|---|---|
| `TOURIST` | Read-only: browse and view events |
| `ADMIN` | Full CRUD on events |

---

## 7. Error Handling Standard

### Common Error Codes

| HTTP Status | Condition |
|---|---|
| `400` | Invalid dates, missing title/location |
| `401` | Missing or invalid JWT |
| `403` | Non-admin attempting to create/update/delete |
| `404` | Event not found |
| `500` | Internal server error |

---

## 8. Example Request & Response

### Browse Upcoming Events

**Request:**

```bash
curl -X GET "http://localhost:8090/api/events?eventType=FESTIVAL&isFeatured=true&page=0&size=5" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..."
```

**Response:** `200 OK`

```json
{
  "data": [
    {
      "id": 1,
      "title": "Kandy Esala Perahera",
      "description": "The grand annual Buddhist procession...",
      "eventType": "FESTIVAL",
      "location": "Temple of the Sacred Tooth Relic, Kandy",
      "latitude": 7.29360000,
      "longitude": 80.63500000,
      "startDate": "2026-08-10T18:00:00Z",
      "endDate": "2026-08-20T23:00:00Z",
      "images": ["https://storage.example.com/events/perahera-1.jpg"],
      "isFeatured": true,
      "isActive": true
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
