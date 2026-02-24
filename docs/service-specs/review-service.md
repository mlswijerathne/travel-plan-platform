# Review Service Specification

> **Service Name:** review-service
> **Port:** 8088
> **Package:** `com.travelplan.review`
> **Database Schema:** `review`
> **Version:** 1.0.0-SNAPSHOT

---

## 1. Service Overview

### Responsibility

The Review Service manages tourist reviews and ratings for all provider types (hotels, tour guides, vehicles). It handles review submission, aggregation, provider responses, and publishes rating update events to keep provider services in sync with their aggregate scores.

### Key Features

- Review submission with ratings (1–5 stars) and optional photo uploads
- One review per tourist per provider per booking (uniqueness constraint)
- Aggregate rating calculation per provider entity
- Provider response to reviews
- Pending reviews management (completed bookings without reviews)
- Review visibility and moderation controls
- Verified review badge (linked to confirmed bookings)
- Rating update event publishing via Amazon SQS

### Dependencies

| Dependency | Purpose |
|---|---|
| `common-lib` | Shared DTOs, JWT filter, Security config, exception handling, `RatingUpdateEvent` |
| Supabase Auth | JWT-based authentication |
| Eureka Discovery Server | Service registration and discovery |
| PostgreSQL (Supabase) | Persistent storage |
| Amazon SQS | Asynchronous rating update event publishing |
| Flyway | Database migration management |

---

## 2. API Endpoints

### 2.1 Submit Review

| Field | Value |
|---|---|
| **Method** | `POST` |
| **URL** | `/api/reviews` |
| **Description** | Submits a review for a provider entity after a completed booking. |
| **Auth** | Bearer JWT — Role: `TOURIST` |

**Request Body:**

```json
{
  "entityType": "HOTEL",
  "entityId": 12,
  "bookingId": 25,
  "rating": 5,
  "title": "Absolutely stunning stay!",
  "content": "The ocean view was breathtaking. Staff were incredibly attentive and friendly. The breakfast buffet had an amazing selection of Sri Lankan and international dishes.",
  "images": [
    "https://storage.example.com/reviews/ocean-view.jpg",
    "https://storage.example.com/reviews/breakfast.jpg"
  ]
}
```

| Field | Type | Required | Validation |
|---|---|---|---|
| `entityType` | String | Yes | `HOTEL`, `TOUR_GUIDE`, or `VEHICLE` |
| `entityId` | Long | Yes | Must reference a valid provider |
| `bookingId` | Long | No | Links review to a specific booking |
| `rating` | Integer | Yes | 1–5 (CHECK constraint) |
| `title` | String | No | Max 255 chars |
| `content` | String | No | Free text |
| `images` | String[] | No | Array of image URLs |

**Response:** `201 Created`

```json
{
  "data": {
    "id": 1,
    "touristId": "tourist-uuid",
    "entityType": "HOTEL",
    "entityId": 12,
    "bookingId": 25,
    "rating": 5,
    "title": "Absolutely stunning stay!",
    "content": "The ocean view was breathtaking...",
    "images": ["https://storage.example.com/reviews/ocean-view.jpg"],
    "isVerified": true,
    "isVisible": true,
    "createdAt": "2026-02-24T10:00:00Z",
    "updatedAt": "2026-02-24T10:00:00Z"
  }
}
```

**Side Effects:**
- Calculates new aggregate rating for the provider entity
- Publishes `review.rating.updated` event to SQS

---

### 2.2 Get Reviews for Entity

| Field | Value |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/reviews` |
| **Description** | Lists reviews for a specific provider entity with pagination. |
| **Auth** | Bearer JWT |

**Query Parameters:**

| Parameter | Type | Required | Description |
|---|---|---|---|
| `entityType` | String | Yes | `HOTEL`, `TOUR_GUIDE`, `VEHICLE` |
| `entityId` | Long | Yes | Provider ID |
| `page` | Integer | No | Page number (default: 0) |
| `size` | Integer | No | Page size (default: 10) |
| `sort` | String | No | Sort field (e.g., `createdAt,desc`) |

**Response:** `200 OK`

```json
{
  "data": [
    {
      "id": 1,
      "touristId": "tourist-uuid",
      "entityType": "HOTEL",
      "entityId": 12,
      "rating": 5,
      "title": "Absolutely stunning stay!",
      "content": "...",
      "images": ["..."],
      "isVerified": true,
      "isVisible": true,
      "response": {
        "id": 1,
        "providerId": "owner-uuid",
        "content": "Thank you for your kind words! We're delighted you enjoyed your stay.",
        "createdAt": "2026-02-25T08:00:00Z"
      },
      "createdAt": "2026-02-24T10:00:00Z"
    }
  ],
  "pagination": {
    "page": 0,
    "pageSize": 10,
    "totalItems": 128,
    "totalPages": 13
  }
}
```

---

### 2.3 Get Review Summary

| Field | Value |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/reviews/summary` |
| **Description** | Returns aggregate rating statistics for a provider entity. Used by AI Agent for recommendations. |
| **Auth** | Bearer JWT |

**Query Parameters:**

| Parameter | Type | Required |
|---|---|---|
| `entityType` | String | Yes |
| `entityId` | Long | Yes |

**Response:** `200 OK`

```json
{
  "data": {
    "entityType": "HOTEL",
    "entityId": 12,
    "averageRating": 4.35,
    "reviewCount": 128,
    "ratingDistribution": {
      "5": 62,
      "4": 38,
      "3": 18,
      "2": 7,
      "1": 3
    }
  }
}
```

---

### 2.4 Get Tourist's Reviews

| Field | Value |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/reviews/my-reviews` |
| **Description** | Lists all reviews submitted by the authenticated tourist. |
| **Auth** | Bearer JWT — Role: `TOURIST` |

---

### 2.5 Respond to Review (Provider)

| Field | Value |
|---|---|
| **Method** | `POST` |
| **URL** | `/api/reviews/{reviewId}/responses` |
| **Description** | Allows a provider to respond to a review of their entity. |
| **Auth** | Bearer JWT — Role: `HOTEL_OWNER`, `TOUR_GUIDE`, `VEHICLE_OWNER` |

**Request Body:**

```json
{
  "content": "Thank you for your wonderful review! We hope to welcome you back soon."
}
```

| Field | Type | Required |
|---|---|---|
| `content` | String | Yes (`@NotBlank`) |

**Response:** `201 Created`

---

### 2.6 Update Review

| Field | Value |
|---|---|
| **Method** | `PUT` |
| **URL** | `/api/reviews/{id}` |
| **Description** | Updates an existing review (rating, content, images). Only the author can update. |
| **Auth** | Bearer JWT — Role: `TOURIST` |

---

### 2.7 Delete Review

| Field | Value |
|---|---|
| **Method** | `DELETE` |
| **URL** | `/api/reviews/{id}` |
| **Description** | Deletes a review. Only the author or admin can delete. |
| **Auth** | Bearer JWT — Role: `TOURIST` or `ADMIN` |

**Response:** `204 No Content`

**Side Effects:** Recalculates aggregate rating and publishes updated event.

---

## 3. Data Model Schema

### 3.1 `reviews` Table

| Column | Type | Nullable | Default | Description |
|---|---|---|---|---|
| `id` | `BIGSERIAL` | No | Auto-increment | Primary key |
| `tourist_id` | `VARCHAR(255)` | No | — | Supabase UID of reviewer |
| `entity_type` | `VARCHAR(50)` | No | — | `HOTEL`, `TOUR_GUIDE`, `VEHICLE` |
| `entity_id` | `BIGINT` | No | — | Provider entity ID |
| `booking_id` | `BIGINT` | Yes | — | Associated booking ID |
| `rating` | `INT` | No | — | 1–5 stars (CHECK constraint) |
| `title` | `VARCHAR(255)` | Yes | — | Review title |
| `content` | `TEXT` | Yes | — | Review body |
| `images` | `TEXT[]` | Yes | — | Image URL array |
| `is_verified` | `BOOLEAN` | Yes | `false` | Linked to confirmed booking |
| `is_visible` | `BOOLEAN` | Yes | `true` | Moderation visibility |
| `created_at` | `TIMESTAMPTZ` | Yes | `CURRENT_TIMESTAMP` | Submission time |
| `updated_at` | `TIMESTAMPTZ` | Yes | `CURRENT_TIMESTAMP` | Last edit time |

**Indexes:**

| Index | Column(s) | Purpose |
|---|---|---|
| `idx_reviews_entity` | `entity_type, entity_id` | Aggregate queries per provider |
| `idx_reviews_tourist_id` | `tourist_id` | Tourist's review history |
| `idx_reviews_rating` | `rating` | Rating distribution queries |

**Constraints:**

- `reviews_uk_tourist_entity UNIQUE (tourist_id, entity_type, entity_id, booking_id)` — One review per tourist per provider per booking.

### 3.2 `review_responses` Table

| Column | Type | Nullable | Default | Description |
|---|---|---|---|---|
| `id` | `BIGSERIAL` | No | Auto-increment | Primary key |
| `review_id` | `BIGINT` | No | — | FK → `reviews.id` (cascade delete) |
| `provider_id` | `VARCHAR(255)` | No | — | Supabase UID of responder |
| `content` | `TEXT` | No | — | Response text |
| `created_at` | `TIMESTAMPTZ` | Yes | `CURRENT_TIMESTAMP` | Response time |

### Relationships

```
reviews (1) ──── (0..*) review_responses
```

---

## 4. User Input Requirements

### Review Submission

| Field | Validation Rules |
|---|---|
| `entityType` | Required. Must be `HOTEL`, `TOUR_GUIDE`, or `VEHICLE`. |
| `entityId` | Required. Must reference a valid provider. |
| `rating` | Required. Integer 1–5. |
| `title` | Optional. Max 255 chars. |
| `content` | Optional. Recommended for helpful reviews. |
| `images` | Optional. Max 5 images per review. |

### Business Constraints

- A tourist can only review a provider they have a completed booking with (when `bookingId` is provided).
- Unique constraint prevents duplicate reviews for the same tourist-provider-booking combination.
- Reviews with a linked `bookingId` from a confirmed booking get `is_verified = true`.
- Providers can respond to reviews but cannot modify or delete them.
- Rating updates trigger SQS events to update provider service aggregate scores.

---

## 5. Inter-Service Communication

### Inbound (Other Services → Review Service)

| Consumer | Endpoint | Method | Purpose |
|---|---|---|---|
| AI Agent Service | `/api/reviews` | GET (Feign) | Fetch reviews for recommendations |
| AI Agent Service | `/api/reviews/summary` | GET (Feign) | Aggregate rating for display |

### Outbound (Review Service → Other Services)

| Target | Method | Payload | Purpose |
|---|---|---|---|
| Hotel Service | SQS Event | `RatingUpdateEvent` | Update hotel `average_rating` and `review_count` |
| Tour Guide Service | SQS Event | `RatingUpdateEvent` | Update guide `average_rating` and `review_count` |
| Vehicle Service | SQS Event | `RatingUpdateEvent` | Update vehicle `average_rating` and `review_count` |

### SQS Event Payload (Outbound)

```json
{
  "eventType": "review.rating.updated",
  "eventId": "550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2026-02-24T10:00:00Z",
  "version": "1.0",
  "source": "review-service",
  "payload": {
    "entityType": "HOTEL",
    "entityId": 12,
    "newRating": 4.35,
    "reviewCount": 129
  }
}
```

### Communication Method

- **Synchronous:** REST via OpenFeign (inbound only)
- **Asynchronous:** Amazon SQS for rating update broadcasts
- **Gateway Route:** `/api/reviews/**` → `lb://review-service`

---

## 6. Security Considerations

### Authentication

- **Method:** Supabase JWT (HS256)
- **Public Endpoints:** `/actuator/**`, `/swagger-ui/**`, `/v3/api-docs/**`

### Role-Based Access Control

| Role | Permissions |
|---|---|
| `TOURIST` | Submit, update, delete own reviews; view all visible reviews |
| `HOTEL_OWNER` | Respond to reviews of own hotels; view reviews |
| `TOUR_GUIDE` | Respond to reviews of own services; view reviews |
| `VEHICLE_OWNER` | Respond to reviews of own vehicles; view reviews |
| `ADMIN` | Full access including moderation (toggle `is_visible`) |

### Data Validation

- Rating CHECK constraint at database level (1–5)
- Unique constraint prevents review spam
- Provider response authorization (only the entity owner can respond)

---

## 7. Error Handling Standard

### Error Response Format

```json
{
  "timestamp": "2026-02-24T10:00:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "Review already exists for this tourist, entity, and booking combination",
  "path": "/api/reviews",
  "traceId": "uuid"
}
```

### Common Error Codes

| HTTP Status | Condition |
|---|---|
| `400` | Invalid rating (outside 1–5), missing entity type |
| `401` | Missing or invalid JWT |
| `403` | Non-owner attempting to modify review or non-provider attempting to respond |
| `404` | Review not found |
| `409` | Duplicate review for same tourist-entity-booking |
| `500` | Internal server error |

---

## 8. Example Request & Response

### Submit a Review

**Request:**

```bash
curl -X POST http://localhost:8088/api/reviews \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..." \
  -d '{
    "entityType": "TOUR_GUIDE",
    "entityId": 1,
    "bookingId": 25,
    "rating": 5,
    "title": "Best wildlife guide in Sri Lanka!",
    "content": "Kamal was absolutely fantastic. He spotted leopards, elephants, and even a sloth bear. His knowledge of Yala National Park is encyclopedic.",
    "images": ["https://storage.example.com/reviews/leopard-sighting.jpg"]
  }'
```

**Response:** `201 Created`

```json
{
  "data": {
    "id": 42,
    "touristId": "tourist-uuid",
    "entityType": "TOUR_GUIDE",
    "entityId": 1,
    "bookingId": 25,
    "rating": 5,
    "title": "Best wildlife guide in Sri Lanka!",
    "content": "Kamal was absolutely fantastic...",
    "images": ["https://storage.example.com/reviews/leopard-sighting.jpg"],
    "isVerified": true,
    "isVisible": true,
    "createdAt": "2026-02-24T15:00:00Z",
    "updatedAt": "2026-02-24T15:00:00Z"
  }
}
```

### Provider Responds to Review

**Request:**

```bash
curl -X POST http://localhost:8088/api/reviews/42/responses \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..." \
  -d '{
    "content": "Thank you so much! It was a pleasure guiding you through Yala. The leopard sighting was truly special that day. Hope to see you again!"
  }'
```

**Response:** `201 Created`

```json
{
  "data": {
    "id": 15,
    "reviewId": 42,
    "providerId": "guide-uuid-001",
    "content": "Thank you so much! It was a pleasure guiding you through Yala...",
    "createdAt": "2026-02-25T08:30:00Z"
  }
}
```
