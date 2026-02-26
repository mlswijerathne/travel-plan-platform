# Hotel Service Specification

> **Service Name:** hotel-service
> **Port:** 8083
> **Package:** `com.travelplan.hotel`
> **Database Schema:** `hotel`
> **Version:** 1.0.0-SNAPSHOT

---

## 1. Service Overview

### Responsibility

The Hotel Service manages hotel listings, room inventory, availability calendars, and rating aggregations for hotel owners registered on the platform. It serves as the primary data source for accommodation search, detail retrieval, and availability checks used by the AI Agent, Booking, and Review services.

### Key Features

- Hotel owner registration and property management (CRUD)
- Room type management with pricing and amenities
- Availability calendar with date-range queries
- Search and filter by city, location, star rating, price range, and amenities
- Aggregate rating updates from the Review Service (via SQS events)
- Provider dashboard with booking statistics

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

### 2.1 Register Hotel

| Field | Value |
|---|---|
| **Method** | `POST` |
| **URL** | `/api/hotels` |
| **Description** | Creates a new hotel listing for the authenticated hotel owner. |
| **Auth** | Bearer JWT — Role: `HOTEL_OWNER` |

**Request Body:**

```json
{
  "name": "Cinnamon Grand Colombo",
  "description": "A luxury 5-star hotel in the heart of Colombo with ocean views.",
  "address": "77 Galle Road, Colombo 03",
  "city": "Colombo",
  "latitude": 6.9271,
  "longitude": 79.8612,
  "starRating": 5,
  "amenities": ["wifi", "pool", "spa", "restaurant", "parking", "gym"],
  "checkInTime": "14:00",
  "checkOutTime": "11:00",
  "rooms": [
    {
      "roomType": "DELUXE",
      "name": "Deluxe Ocean View",
      "description": "Spacious room with panoramic ocean views",
      "pricePerNight": 185.00,
      "maxOccupancy": 2,
      "amenities": ["air_conditioning", "minibar", "balcony"],
      "totalRooms": 20
    },
    {
      "roomType": "SUITE",
      "name": "Presidential Suite",
      "description": "Premium suite with living room and private terrace",
      "pricePerNight": 450.00,
      "maxOccupancy": 4,
      "amenities": ["air_conditioning", "minibar", "balcony", "jacuzzi", "butler_service"],
      "totalRooms": 5
    }
  ]
}
```

| Field | Type | Required | Validation |
|---|---|---|---|
| `name` | String | Yes | `@NotBlank`, max 255 chars |
| `description` | String | No | — |
| `address` | String | Yes | `@NotBlank` |
| `city` | String | Yes | `@NotBlank`, max 100 chars |
| `latitude` | BigDecimal | No | Range: -90 to 90 |
| `longitude` | BigDecimal | No | Range: -180 to 180 |
| `starRating` | Integer | No | 1–5 |
| `amenities` | String[] | No | — |
| `checkInTime` | String (HH:mm) | No | Default: `14:00` |
| `checkOutTime` | String (HH:mm) | No | Default: `11:00` |
| `rooms` | Array | No | Nested room objects |

**Response:** `201 Created`

```json
{
  "data": {
    "id": 1,
    "ownerId": "supabase-owner-uuid",
    "name": "Cinnamon Grand Colombo",
    "description": "A luxury 5-star hotel in the heart of Colombo with ocean views.",
    "address": "77 Galle Road, Colombo 03",
    "city": "Colombo",
    "latitude": 6.92710000,
    "longitude": 79.86120000,
    "starRating": 5,
    "averageRating": 0.00,
    "reviewCount": 0,
    "amenities": ["wifi", "pool", "spa", "restaurant", "parking", "gym"],
    "checkInTime": "14:00",
    "checkOutTime": "11:00",
    "isActive": true,
    "rooms": [
      {
        "id": 1,
        "roomType": "DELUXE",
        "name": "Deluxe Ocean View",
        "description": "Spacious room with panoramic ocean views",
        "pricePerNight": 185.00,
        "maxOccupancy": 2,
        "amenities": ["air_conditioning", "minibar", "balcony"],
        "totalRooms": 20,
        "isActive": true
      }
    ],
    "createdAt": "2026-02-24T10:00:00Z",
    "updatedAt": "2026-02-24T10:00:00Z"
  }
}
```

---

### 2.2 Get Hotel by ID

| Field | Value |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/hotels/{id}` |
| **Description** | Retrieves full hotel details including rooms. Public for tourists; used by AI Agent and Booking services. |
| **Auth** | Bearer JWT |

**Path Parameters:**

| Parameter | Type | Description |
|---|---|---|
| `id` | Long | Hotel database ID |

**Response:** `200 OK` — Full `HotelResponse`.

---

### 2.3 Search Hotels

| Field | Value |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/hotels` |
| **Description** | Search and filter hotels with pagination. |
| **Auth** | Bearer JWT |

**Query Parameters:**

| Parameter | Type | Required | Description |
|---|---|---|---|
| `city` | String | No | Filter by city name |
| `minStarRating` | Integer | No | Minimum star rating (1–5) |
| `maxPrice` | BigDecimal | No | Maximum price per night |
| `minPrice` | BigDecimal | No | Minimum price per night |
| `amenities` | String | No | Comma-separated amenity list |
| `checkIn` | LocalDate | No | Desired check-in date |
| `checkOut` | LocalDate | No | Desired check-out date |
| `guests` | Integer | No | Number of guests |
| `page` | Integer | No | Page number (default: 0) |
| `size` | Integer | No | Page size (default: 10) |
| `sort` | String | No | Sort field (e.g., `pricePerNight,asc`) |

**Response:** `200 OK`

```json
{
  "data": [ ... ],
  "pagination": {
    "page": 0,
    "pageSize": 10,
    "totalItems": 45,
    "totalPages": 5
  }
}
```

---

### 2.4 Search Hotels by Query (AI Agent)

| Field | Value |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/hotels/search` |
| **Description** | Natural-language search endpoint used by the AI Agent. Accepts flexible query parameters. |
| **Auth** | Bearer JWT (service-to-service via Feign) |

**Query Parameters:**

| Parameter | Type | Description |
|---|---|---|
| `query` | String | Free-text search query |
| `city` | String | City filter |
| `maxPrice` | BigDecimal | Maximum nightly price |

---

### 2.5 Update Hotel

| Field | Value |
|---|---|
| **Method** | `PUT` |
| **URL** | `/api/hotels/{id}` |
| **Description** | Updates hotel details. Only the owning hotel owner may update. |
| **Auth** | Bearer JWT — Role: `HOTEL_OWNER` |

---

### 2.6 Delete Hotel (Soft Delete)

| Field | Value |
|---|---|
| **Method** | `DELETE` |
| **URL** | `/api/hotels/{id}` |
| **Description** | Soft-deletes a hotel by setting `is_active = false`. |
| **Auth** | Bearer JWT — Role: `HOTEL_OWNER` |

**Response:** `204 No Content`

---

### 2.7 Check Availability

| Field | Value |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/hotels/{id}/availability` |
| **Description** | Checks room availability for a given date range. Used by Booking Service during saga orchestration. |
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
    "hotelId": 1,
    "available": true,
    "rooms": [
      {
        "roomId": 1,
        "roomType": "DELUXE",
        "availableCount": 15,
        "pricePerNight": 185.00
      }
    ]
  }
}
```

---

### 2.8 Get Owner's Hotels

| Field | Value |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/hotels/owner` |
| **Description** | Lists all hotels belonging to the authenticated owner. Provider dashboard endpoint. |
| **Auth** | Bearer JWT — Role: `HOTEL_OWNER` |

---

### 2.9 Manage Rooms

| Field | Value |
|---|---|
| **Method** | `POST` / `PUT` / `DELETE` |
| **URL** | `/api/hotels/{hotelId}/rooms` and `/api/hotels/{hotelId}/rooms/{roomId}` |
| **Description** | CRUD operations for room types within a hotel. |
| **Auth** | Bearer JWT — Role: `HOTEL_OWNER` |

---

## 3. Data Model Schema

### 3.1 `hotels` Table

| Column | Type | Nullable | Default | Description |
|---|---|---|---|---|
| `id` | `BIGSERIAL` | No | Auto-increment | Primary key |
| `owner_id` | `VARCHAR(255)` | No | — | Supabase UID of hotel owner |
| `name` | `VARCHAR(255)` | No | — | Hotel name |
| `description` | `TEXT` | Yes | — | Full description |
| `address` | `TEXT` | No | — | Street address |
| `city` | `VARCHAR(100)` | No | — | City name |
| `latitude` | `DECIMAL(10,8)` | Yes | — | GPS latitude |
| `longitude` | `DECIMAL(11,8)` | Yes | — | GPS longitude |
| `star_rating` | `INT` | Yes | — | 1–5 star classification |
| `average_rating` | `DECIMAL(3,2)` | Yes | `0` | Aggregated review score |
| `review_count` | `INT` | Yes | `0` | Total number of reviews |
| `amenities` | `TEXT[]` | Yes | — | PostgreSQL array of amenity tags |
| `check_in_time` | `TIME` | Yes | `14:00` | Standard check-in time |
| `check_out_time` | `TIME` | Yes | `11:00` | Standard check-out time |
| `is_active` | `BOOLEAN` | Yes | `true` | Soft-delete flag |
| `created_at` | `TIMESTAMPTZ` | Yes | `CURRENT_TIMESTAMP` | Record creation |
| `updated_at` | `TIMESTAMPTZ` | Yes | `CURRENT_TIMESTAMP` | Last modification |

**Indexes:**

| Index | Column(s) | Purpose |
|---|---|---|
| `idx_hotels_owner_id` | `owner_id` | Owner's hotel listing lookup |
| `idx_hotels_city` | `city` | City-based search |
| `idx_hotels_location` | `latitude, longitude` | Geo-proximity queries |

### 3.2 `rooms` Table

| Column | Type | Nullable | Default | Description |
|---|---|---|---|---|
| `id` | `BIGSERIAL` | No | Auto-increment | Primary key |
| `hotel_id` | `BIGINT` | No | — | FK → `hotels.id` (cascade delete) |
| `room_type` | `VARCHAR(50)` | No | — | STANDARD, DELUXE, SUITE, etc. |
| `name` | `VARCHAR(100)` | No | — | Room display name |
| `description` | `TEXT` | Yes | — | Room description |
| `price_per_night` | `DECIMAL(10,2)` | No | — | Nightly rate (USD) |
| `max_occupancy` | `INT` | No | `2` | Maximum guest count |
| `amenities` | `TEXT[]` | Yes | — | Room-specific amenities |
| `total_rooms` | `INT` | No | `1` | Total inventory count |
| `is_active` | `BOOLEAN` | Yes | `true` | Active flag |
| `created_at` | `TIMESTAMPTZ` | Yes | `CURRENT_TIMESTAMP` | Record creation |
| `updated_at` | `TIMESTAMPTZ` | Yes | `CURRENT_TIMESTAMP` | Last modification |

**Indexes:**

| Index | Column(s) | Purpose |
|---|---|---|
| `idx_rooms_hotel_id` | `hotel_id` | Room lookup by hotel |

### Relationships

```
hotels (1) ──── (0..*) rooms
```

---

## 4. User Input Requirements

### Hotel Creation

| Field | Validation Rules |
|---|---|
| `name` | Required. 1–255 characters. |
| `address` | Required. Free text. |
| `city` | Required. 1–100 characters. Must be a valid Sri Lankan city. |
| `starRating` | Optional. Integer between 1 and 5 (enforced by CHECK constraint). |
| `latitude` | Optional. Must be a valid coordinate (-90 to 90). |
| `longitude` | Optional. Must be a valid coordinate (-180 to 180). |
| `checkInTime` | Optional. HH:mm format. Defaults to `14:00`. |
| `checkOutTime` | Optional. HH:mm format. Defaults to `11:00`. |

### Room Creation

| Field | Validation Rules |
|---|---|
| `roomType` | Required. One of: STANDARD, DELUXE, SUITE, FAMILY, DORMITORY. |
| `name` | Required. 1–100 characters. |
| `pricePerNight` | Required. Must be > 0. |
| `maxOccupancy` | Required. Must be >= 1. |
| `totalRooms` | Required. Must be >= 1. |

### Business Constraints

- Only `HOTEL_OWNER` role can create, update, or delete hotels.
- A hotel owner can only modify their own properties (ownership validated via JWT `sub` claim).
- Deleting a hotel is a soft delete (`is_active = false`); rooms remain but become unsearchable.
- `average_rating` and `review_count` are updated asynchronously via SQS events from the Review Service.

---

## 5. Inter-Service Communication

### Inbound (Other Services → Hotel Service)

| Consumer | Endpoint | Method | Purpose |
|---|---|---|---|
| AI Agent Service | `/api/hotels/search` | GET (Feign) | AI-powered hotel search |
| AI Agent Service | `/api/hotels/{id}` | GET (Feign) | Hotel detail retrieval |
| Booking Service | `/api/hotels/{id}` | GET (Feign) | Hotel validation during booking |
| Booking Service | `/api/hotels/{id}/availability` | GET (Feign) | Availability check during saga |
| Review Service | Rating update event | SQS Consumer | Update `average_rating` and `review_count` |

### Outbound (Hotel Service → Other Services)

| Target | Method | Purpose |
|---|---|---|
| Eureka Discovery Server | REST (heartbeat) | Service registration |

### Communication Details

- **Synchronous:** REST via OpenFeign with Eureka load balancing (`lb://hotel-service`)
- **Asynchronous:** Amazon SQS for `review.rating.updated` events
- **JWT Propagation:** `FeignJwtInterceptor` forwards Authorization header

### SQS Event Payload (Inbound)

```json
{
  "eventType": "review.rating.updated",
  "eventId": "uuid",
  "timestamp": "2026-02-24T10:00:00Z",
  "version": "1.0",
  "source": "review-service",
  "payload": {
    "entityType": "HOTEL",
    "entityId": 1,
    "newRating": 4.35,
    "reviewCount": 128
  }
}
```

---

## 6. Security Considerations

### Authentication

- **Method:** Supabase JWT (HS256)
- **Filter:** `JwtValidationFilter` from `common-lib`
- **Public Endpoints:** `/actuator/**`, `/swagger-ui/**`, `/v3/api-docs/**`

### Role-Based Access Control

| Role | Permissions |
|---|---|
| `TOURIST` | Read hotels, search, check availability |
| `HOTEL_OWNER` | Full CRUD on own hotels and rooms |
| `ADMIN` | Full platform access |

### Data Validation

- Server-side Bean Validation on all request DTOs
- `star_rating` CHECK constraint at database level (1–5)
- Owner authorization check on all mutation operations

---

## 7. Error Handling Standard

### Error Response Format

```json
{
  "timestamp": "2026-02-24T10:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Hotel not found with id: '99'",
  "path": "/api/hotels/99",
  "traceId": "550e8400-e29b-41d4-a716-446655440000"
}
```

### Common Error Codes

| HTTP Status | Condition |
|---|---|
| `400` | Invalid input (missing name, invalid star rating, bad date format) |
| `401` | Missing or invalid JWT |
| `403` | Non-owner attempting to modify a hotel |
| `404` | Hotel or room not found |
| `409` | Duplicate hotel name for same owner |
| `500` | Internal server error |

---

## 8. Example Request & Response

### Search Hotels in Kandy

**Request:**

```bash
curl -X GET "http://localhost:8083/api/hotels?city=Kandy&minStarRating=3&maxPrice=150&page=0&size=5" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..."
```

**Response:** `200 OK`

```json
{
  "data": [
    {
      "id": 12,
      "ownerId": "owner-uuid-456",
      "name": "Earl's Regency Kandy",
      "description": "Nestled in the hills overlooking the Mahaweli River.",
      "address": "Tennekumbura, Kandy",
      "city": "Kandy",
      "latitude": 7.29060000,
      "longitude": 80.63120000,
      "starRating": 4,
      "averageRating": 4.20,
      "reviewCount": 87,
      "amenities": ["wifi", "pool", "restaurant", "spa"],
      "checkInTime": "14:00",
      "checkOutTime": "11:00",
      "isActive": true,
      "rooms": [
        {
          "id": 30,
          "roomType": "DELUXE",
          "name": "Hill View Deluxe",
          "pricePerNight": 120.00,
          "maxOccupancy": 2,
          "totalRooms": 30,
          "isActive": true
        }
      ],
      "createdAt": "2026-01-15T08:00:00Z",
      "updatedAt": "2026-02-20T12:00:00Z"
    }
  ],
  "pagination": {
    "page": 0,
    "pageSize": 5,
    "totalItems": 12,
    "totalPages": 3
  }
}
```

### Check Availability

**Request:**

```bash
curl -X GET "http://localhost:8083/api/hotels/12/availability?startDate=2026-03-15&endDate=2026-03-18" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..."
```

**Response:** `200 OK`

```json
{
  "data": {
    "hotelId": 12,
    "available": true,
    "rooms": [
      {
        "roomId": 30,
        "roomType": "DELUXE",
        "availableCount": 22,
        "pricePerNight": 120.00
      },
      {
        "roomId": 31,
        "roomType": "SUITE",
        "availableCount": 3,
        "pricePerNight": 280.00
      }
    ]
  }
}
```
