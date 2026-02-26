# Tourist Service Specification

> **Service Name:** tourist-service
> **Port:** 8082
> **Package:** `com.travelplan.tourist`
> **Database Schema:** `tourist`
> **Version:** 1.0.0-SNAPSHOT

---

## 1. Service Overview

### Responsibility

The Tourist Service is the central user profile and identity management service for the Travel Plan Platform. It manages tourist registration, profile information, travel preferences, and wallet/credit transactions.

### Key Features

- Tourist registration with Supabase Auth integration
- Profile retrieval and update (name, phone, nationality, image)
- Travel preference management (budget, style, dietary, interests, languages, accessibility)
- Wallet balance tracking with transaction history (refunds, usage, adjustments)
- Internal lookup by ID for inter-service communication

### Dependencies

| Dependency | Purpose |
|---|---|
| `common-lib` | Shared `ApiResponse`, `ErrorResponse`, JWT filter, Security config, exception handling |
| Supabase Auth | External identity provider; JWT tokens validated on every request |
| Eureka Discovery Server | Service registration and discovery |
| PostgreSQL (Supabase) | Persistent storage via schema `tourist` |
| Flyway | Database migration management |

---

## 2. API Endpoints

### 2.1 Register Tourist

| Field | Value |
|---|---|
| **Method** | `POST` |
| **URL** | `/api/tourists/register` |
| **Description** | Registers a new tourist after Supabase Auth sign-up. Public endpoint (no JWT required). |
| **Auth** | None (permit all) |

**Request Body:**

```json
{
  "userId": "supabase-uuid-string",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+94771234567",
  "nationality": "British",
  "profileImageUrl": "https://storage.example.com/avatar.jpg",
  "preferredBudget": "MODERATE"
}
```

| Field | Type | Required | Validation |
|---|---|---|---|
| `userId` | String | Yes | `@NotBlank` |
| `email` | String | Yes | `@NotBlank`, `@Email` |
| `firstName` | String | Yes | `@NotBlank`, max 100 chars |
| `lastName` | String | Yes | `@NotBlank`, max 100 chars |
| `phoneNumber` | String | No | Max 20 chars |
| `nationality` | String | No | Max 100 chars |
| `profileImageUrl` | String | No | — |
| `preferredBudget` | String | No | Creates default preference if provided |

**Response:** `201 Created`

```json
{
  "data": {
    "id": 1,
    "userId": "supabase-uuid-string",
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+94771234567",
    "nationality": "British",
    "profileImageUrl": "https://storage.example.com/avatar.jpg",
    "isActive": true,
    "preferences": {
      "preferredBudget": "MODERATE",
      "travelStyle": null,
      "dietaryRestrictions": null,
      "interests": null,
      "preferredLanguages": null,
      "accessibilityNeeds": null
    },
    "createdAt": "2026-02-24T10:00:00Z",
    "updatedAt": "2026-02-24T10:00:00Z"
  },
  "meta": {
    "timestamp": "2026-02-24T10:00:00Z",
    "requestId": null
  }
}
```

**Error Responses:**

| Status | Condition |
|---|---|
| `400 Bad Request` | Validation failure (missing fields, invalid email) |
| `409 Conflict` | Email or userId already registered |

---

### 2.2 Get Current Tourist Profile

| Field | Value |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/tourists/me` |
| **Description** | Returns the authenticated tourist's full profile including preferences. |
| **Auth** | Bearer JWT (any authenticated user) |

**Request Headers:**

| Header | Value |
|---|---|
| `Authorization` | `Bearer <jwt_token>` |

**Response:** `200 OK` — Same schema as registration response.

**Error Responses:**

| Status | Condition |
|---|---|
| `401 Unauthorized` | Missing or invalid JWT |
| `404 Not Found` | No tourist profile for this userId |

---

### 2.3 Update Tourist Profile

| Field | Value |
|---|---|
| **Method** | `PUT` |
| **URL** | `/api/tourists/me` |
| **Description** | Partially updates the authenticated tourist's profile. Only non-null fields are applied. |
| **Auth** | Bearer JWT |

**Request Body:**

```json
{
  "firstName": "Jonathan",
  "lastName": "Doe",
  "phoneNumber": "+94777654321",
  "nationality": "Canadian",
  "profileImageUrl": "https://storage.example.com/new-avatar.jpg"
}
```

| Field | Type | Required | Validation |
|---|---|---|---|
| `firstName` | String | No | Max 100 chars |
| `lastName` | String | No | Max 100 chars |
| `phoneNumber` | String | No | Max 20 chars |
| `nationality` | String | No | Max 100 chars |
| `profileImageUrl` | String | No | — |

**Response:** `200 OK` — Updated `TouristResponse`.

---

### 2.4 Get Tourist Preferences

| Field | Value |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/tourists/me/preferences` |
| **Description** | Returns the authenticated tourist's travel preferences. |
| **Auth** | Bearer JWT |

**Response:** `200 OK`

```json
{
  "data": {
    "preferredBudget": "MODERATE",
    "travelStyle": "ADVENTURE",
    "dietaryRestrictions": ["vegetarian", "gluten-free"],
    "interests": ["wildlife", "temples", "hiking"],
    "preferredLanguages": ["English", "Sinhala"],
    "accessibilityNeeds": "wheelchair accessible"
  }
}
```

---

### 2.5 Update Tourist Preferences

| Field | Value |
|---|---|
| **Method** | `PUT` |
| **URL** | `/api/tourists/me/preferences` |
| **Description** | Creates or updates the authenticated tourist's travel preferences. Partial update (null fields ignored). |
| **Auth** | Bearer JWT |

**Request Body:**

```json
{
  "preferredBudget": "LUXURY",
  "travelStyle": "RELAXATION",
  "dietaryRestrictions": ["vegan"],
  "interests": ["beach", "surfing", "food tours"],
  "preferredLanguages": ["English"],
  "accessibilityNeeds": null
}
```

| Field | Type | Required |
|---|---|---|
| `preferredBudget` | String | No |
| `travelStyle` | String | No |
| `dietaryRestrictions` | String[] | No |
| `interests` | String[] | No |
| `preferredLanguages` | String[] | No |
| `accessibilityNeeds` | String | No |

**Response:** `200 OK` — Updated `PreferenceResponse`.

---

### 2.6 Get Wallet

| Field | Value |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/tourists/me/wallet` |
| **Description** | Returns the authenticated tourist's wallet balance and transaction history. |
| **Auth** | Bearer JWT |

**Response:** `200 OK`

```json
{
  "data": {
    "balance": 125.50,
    "transactions": [
      {
        "id": 1,
        "amount": 200.00,
        "type": "REFUND",
        "description": "Booking #TRP-20260224-ABC123 cancellation refund",
        "referenceId": "TRP-20260224-ABC123",
        "createdAt": "2026-02-20T14:30:00Z"
      },
      {
        "id": 2,
        "amount": 74.50,
        "type": "USED",
        "description": "Applied wallet credit to Booking #TRP-20260222-DEF456",
        "referenceId": "TRP-20260222-DEF456",
        "createdAt": "2026-02-22T09:15:00Z"
      }
    ]
  }
}
```

---

### 2.7 Get Tourist by ID (Internal)

| Field | Value |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/tourists/{id}` |
| **Description** | Returns a tourist by database ID. Used by other services for internal lookups. |
| **Auth** | Bearer JWT |

**Path Parameters:**

| Parameter | Type | Description |
|---|---|---|
| `id` | Long | Tourist database ID |

**Response:** `200 OK` — `TouristResponse`.

**Error Responses:**

| Status | Condition |
|---|---|
| `404 Not Found` | Tourist with given ID does not exist |

---

## 3. Data Model Schema

### 3.1 `tourists` Table

| Column | Type | Nullable | Default | Description |
|---|---|---|---|---|
| `id` | `BIGSERIAL` | No | Auto-increment | Primary key |
| `user_id` | `VARCHAR(255)` | No | — | Supabase Auth UID (unique) |
| `email` | `VARCHAR(255)` | No | — | Email address (unique) |
| `first_name` | `VARCHAR(100)` | No | — | First name |
| `last_name` | `VARCHAR(100)` | No | — | Last name |
| `phone_number` | `VARCHAR(20)` | Yes | — | Phone number |
| `nationality` | `VARCHAR(100)` | Yes | — | Country of origin |
| `profile_image_url` | `TEXT` | Yes | — | Avatar URL |
| `is_active` | `BOOLEAN` | Yes | `true` | Soft-delete flag |
| `created_at` | `TIMESTAMPTZ` | Yes | `CURRENT_TIMESTAMP` | Record creation time |
| `updated_at` | `TIMESTAMPTZ` | Yes | `CURRENT_TIMESTAMP` | Last modification time |

**Indexes:**

| Index | Column(s) | Purpose |
|---|---|---|
| `idx_tourists_user_id` | `user_id` | Fast lookup by Supabase UID |
| `idx_tourists_email` | `email` | Fast lookup by email |

### 3.2 `tourist_preferences` Table

| Column | Type | Nullable | Default | Description |
|---|---|---|---|---|
| `id` | `BIGSERIAL` | No | Auto-increment | Primary key |
| `tourist_id` | `BIGINT` | No | — | FK → `tourists.id` (unique, cascade delete) |
| `preferred_budget` | `VARCHAR(50)` | Yes | — | Budget level (BUDGET, MODERATE, LUXURY) |
| `travel_style` | `VARCHAR(50)` | Yes | — | Travel style (ADVENTURE, RELAXATION, CULTURAL) |
| `dietary_restrictions` | `TEXT[]` | Yes | — | PostgreSQL array of dietary requirements |
| `interests` | `TEXT[]` | Yes | — | PostgreSQL array of interest tags |
| `preferred_languages` | `TEXT[]` | Yes | — | PostgreSQL array of languages |
| `accessibility_needs` | `TEXT` | Yes | — | Free-text accessibility description |
| `created_at` | `TIMESTAMPTZ` | Yes | `CURRENT_TIMESTAMP` | Record creation time |
| `updated_at` | `TIMESTAMPTZ` | Yes | `CURRENT_TIMESTAMP` | Last modification time |

**Indexes:**

| Index | Column(s) | Purpose |
|---|---|---|
| `idx_tourist_preferences_tourist_id` | `tourist_id` | Fast join to tourists |

**Constraints:**

- `tourist_preferences_uk_tourist UNIQUE (tourist_id)` — One preference record per tourist.

### 3.3 `wallet_transactions` Table

| Column | Type | Nullable | Default | Description |
|---|---|---|---|---|
| `id` | `BIGSERIAL` | No | Auto-increment | Primary key |
| `tourist_id` | `BIGINT` | No | — | FK → `tourists.id` (cascade delete) |
| `amount` | `NUMERIC(12,2)` | No | — | Transaction amount |
| `type` | `VARCHAR(20)` | No | — | `REFUND`, `USED`, or `ADJUSTMENT` |
| `description` | `VARCHAR(500)` | No | — | Human-readable description |
| `reference_id` | `VARCHAR(255)` | Yes | — | Associated booking reference |
| `created_at` | `TIMESTAMPTZ` | Yes | `CURRENT_TIMESTAMP` | Transaction timestamp |

**Indexes:**

| Index | Column(s) | Purpose |
|---|---|---|
| `idx_wallet_transactions_tourist_id` | `tourist_id` | Fast lookup by tourist |
| `idx_wallet_transactions_created_at` | `created_at DESC` | Chronological ordering |

**Balance Calculation:**

```sql
SELECT COALESCE(
  SUM(CASE
    WHEN type IN ('REFUND', 'ADJUSTMENT') THEN amount
    WHEN type = 'USED' THEN -amount
    ELSE 0
  END), 0
) FROM wallet_transactions WHERE tourist_id = :touristId
```

### Relationships

```
tourists (1) ──── (0..1) tourist_preferences
tourists (1) ──── (0..*) wallet_transactions
```

---

## 4. User Input Requirements

### Registration

| Field | Validation Rules |
|---|---|
| `userId` | Required. Must be a valid Supabase UUID. Must be unique across all tourists. |
| `email` | Required. Must be a valid RFC 5322 email format. Must be unique. |
| `firstName` | Required. 1–100 characters. |
| `lastName` | Required. 1–100 characters. |
| `phoneNumber` | Optional. Max 20 characters. International format recommended. |
| `nationality` | Optional. Max 100 characters. |
| `profileImageUrl` | Optional. Valid URL string. |
| `preferredBudget` | Optional. Accepted values: `BUDGET`, `MODERATE`, `LUXURY`. |

### Profile Update

- All fields optional (partial update semantics).
- Only non-null fields overwrite existing values.

### Preference Update

- All fields optional.
- Array fields (`dietaryRestrictions`, `interests`, `preferredLanguages`) replace the entire array when provided.

### Business Constraints

- A tourist cannot register twice with the same `email` or `userId`.
- Wallet balance is read-only — transactions are created by other services (booking-service creates `REFUND` and `USED` entries).
- Profile image upload is handled externally (Supabase Storage); only the URL is stored.

---

## 5. Inter-Service Communication

### Inbound (Other Services → Tourist Service)

| Consumer | Endpoint | Method | Purpose |
|---|---|---|---|
| Booking Service | `/api/tourists/{id}` | GET (Feign) | Validate tourist exists for booking |
| AI Agent Service | `/api/tourists/me` | GET (Feign) | Retrieve preferences for AI recommendations |
| Itinerary Service | `/api/tourists/{id}` | GET (Feign) | Tourist info for itinerary display |

### Outbound (Tourist Service → Other Services)

The Tourist Service does **not** make outbound calls to other services. It is a leaf service in the dependency graph.

### Communication Method

- **Synchronous:** REST via Spring Cloud OpenFeign (service-to-service through Eureka discovery)
- **Authentication:** JWT propagated in `Authorization` header via `FeignJwtInterceptor`

### Payload Structure

All responses wrapped in `ApiResponse<T>`:

```json
{
  "data": { ... },
  "meta": {
    "timestamp": "2026-02-24T10:00:00Z",
    "requestId": "uuid"
  }
}
```

---

## 6. Security Considerations

### Authentication

- **Method:** Supabase JWT (HS256 symmetric key)
- **Filter:** `JwtValidationFilter` from `common-lib` extracts `sub` claim as userId
- **Public Endpoints:** `/api/tourists/register`, `/actuator/**`, `/swagger-ui/**`, `/v3/api-docs/**`
- **Protected Endpoints:** All other endpoints require valid Bearer token

### Role-Based Access Control

| Role | Permissions |
|---|---|
| `TOURIST` | Full access to own profile (`/me` endpoints) |
| `HOTEL_OWNER` | Read-only access to tourist profiles via `/{id}` |
| `TOUR_GUIDE` | Read-only access to tourist profiles via `/{id}` |
| `VEHICLE_OWNER` | Read-only access to tourist profiles via `/{id}` |
| `ADMIN` | Full platform access |

Roles are extracted from Supabase JWT `app_metadata.role` or `user_metadata.role`, defaulting to `TOURIST`.

### Data Validation

- Server-side Bean Validation (`jakarta.validation`) on all request DTOs
- SQL injection prevention via JPA parameterized queries
- XSS prevention via JSON serialization (no raw HTML rendering)

---

## 7. Error Handling Standard

### Error Response Format

```json
{
  "timestamp": "2026-02-24T10:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Tourist not found with userId: 'abc-123'",
  "path": "/api/tourists/me",
  "traceId": "550e8400-e29b-41d4-a716-446655440000"
}
```

### Common Error Codes

| HTTP Status | Error Type | Condition |
|---|---|---|
| `400` | Bad Request | Validation failure, missing required fields |
| `401` | Unauthorized | Missing, expired, or malformed JWT |
| `403` | Forbidden | Insufficient role permissions |
| `404` | Not Found | Tourist profile or preference not found |
| `409` | Conflict | Duplicate email or userId on registration |
| `500` | Internal Server Error | Unexpected server failure |
| `503` | Service Unavailable | Database connectivity issue |

---

## 8. Example Request & Response

### Register a New Tourist

**Request:**

```bash
curl -X POST http://localhost:8082/api/tourists/register \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "d4f5a6b7-c8d9-4e0f-a1b2-c3d4e5f6a7b8",
    "email": "sarah.jones@gmail.com",
    "firstName": "Sarah",
    "lastName": "Jones",
    "phoneNumber": "+447700900123",
    "nationality": "British",
    "preferredBudget": "MODERATE"
  }'
```

**Response:** `201 Created`

```json
{
  "data": {
    "id": 42,
    "userId": "d4f5a6b7-c8d9-4e0f-a1b2-c3d4e5f6a7b8",
    "email": "sarah.jones@gmail.com",
    "firstName": "Sarah",
    "lastName": "Jones",
    "phoneNumber": "+447700900123",
    "nationality": "British",
    "profileImageUrl": null,
    "isActive": true,
    "preferences": {
      "preferredBudget": "MODERATE",
      "travelStyle": null,
      "dietaryRestrictions": null,
      "interests": null,
      "preferredLanguages": null,
      "accessibilityNeeds": null
    },
    "createdAt": "2026-02-24T10:15:30Z",
    "updatedAt": "2026-02-24T10:15:30Z"
  },
  "meta": {
    "timestamp": "2026-02-24T10:15:30Z",
    "requestId": null
  }
}
```

### Update Travel Preferences

**Request:**

```bash
curl -X PUT http://localhost:8082/api/tourists/me/preferences \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..." \
  -d '{
    "travelStyle": "ADVENTURE",
    "interests": ["wildlife safaris", "temple visits", "whale watching"],
    "dietaryRestrictions": ["vegetarian"],
    "preferredLanguages": ["English"]
  }'
```

**Response:** `200 OK`

```json
{
  "data": {
    "preferredBudget": "MODERATE",
    "travelStyle": "ADVENTURE",
    "dietaryRestrictions": ["vegetarian"],
    "interests": ["wildlife safaris", "temple visits", "whale watching"],
    "preferredLanguages": ["English"],
    "accessibilityNeeds": null
  },
  "meta": {
    "timestamp": "2026-02-24T10:20:00Z",
    "requestId": null
  }
}
```

### Get Wallet Balance

**Request:**

```bash
curl -X GET http://localhost:8082/api/tourists/me/wallet \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..."
```

**Response:** `200 OK`

```json
{
  "data": {
    "balance": 350.00,
    "transactions": [
      {
        "id": 5,
        "amount": 500.00,
        "type": "REFUND",
        "description": "Full refund for cancelled booking TRP-20260220-XYZ789",
        "referenceId": "TRP-20260220-XYZ789",
        "createdAt": "2026-02-20T16:45:00Z"
      },
      {
        "id": 6,
        "amount": 150.00,
        "type": "USED",
        "description": "Applied wallet credit to booking TRP-20260223-ABC456",
        "referenceId": "TRP-20260223-ABC456",
        "createdAt": "2026-02-23T11:30:00Z"
      }
    ]
  },
  "meta": {
    "timestamp": "2026-02-24T10:25:00Z",
    "requestId": null
  }
}
```
