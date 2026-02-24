# Trip Plan Service Specification

> **Service Name:** trip-plan-service
> **Port:** 8089
> **Package:** `com.travelplan.tripplan`
> **Database Schema:** `tripplan`
> **Version:** 1.0.0-SNAPSHOT

---

## 1. Service Overview

### Responsibility

The Trip Plan Service manages curated travel packages — pre-built trip bundles that combine accommodations, tour guides, and vehicles into discounted, bookable experiences. It enables admin users to create and manage packages, and tourists to browse, filter, and book entire packages with a single action.

### Key Features

- Package creation with multi-day, multi-provider itineraries
- Bundle pricing with discount percentages
- Package item management (day-by-day provider assignments)
- Featured package promotion
- Search and filter by destination, duration, price, and featured status
- One-click package booking integration
- Automatic itinerary generation from package structure

### Dependencies

| Dependency | Purpose |
|---|---|
| `common-lib` | Shared DTOs, JWT filter, Security config, exception handling |
| Supabase Auth | JWT-based authentication |
| Eureka Discovery Server | Service registration and discovery |
| PostgreSQL (Supabase) | Persistent storage |
| Flyway | Database migration management |
| OpenFeign | Service-to-service communication |

---

## 2. API Endpoints

### 2.1 Create Package

| Field | Value |
|---|---|
| **Method** | `POST` |
| **URL** | `/api/packages` |
| **Description** | Creates a new trip package. Admin-only operation. |
| **Auth** | Bearer JWT — Role: `ADMIN` |

**Request Body:**

```json
{
  "name": "Cultural Triangle Explorer",
  "description": "3-day guided tour of Sri Lanka's ancient cities: Sigiriya, Polonnaruwa, and Dambulla.",
  "durationDays": 3,
  "basePrice": 650.00,
  "discountPercentage": 15.00,
  "maxParticipants": 12,
  "destinations": ["Sigiriya", "Polonnaruwa", "Dambulla"],
  "inclusions": ["Accommodation", "Tour guide", "Transport", "Entrance tickets", "Breakfast"],
  "exclusions": ["Lunch", "Dinner", "Personal expenses"],
  "images": ["https://storage.example.com/packages/cultural-triangle.jpg"],
  "isFeatured": true,
  "items": [
    {
      "dayNumber": 1,
      "providerType": "HOTEL",
      "providerId": 8,
      "itemName": "Heritage Hotel Sigiriya - Night 1",
      "description": "Check-in and evening at leisure",
      "sortOrder": 0
    },
    {
      "dayNumber": 1,
      "providerType": "TOUR_GUIDE",
      "providerId": 3,
      "itemName": "Cultural Heritage Guide",
      "description": "Expert guide for all 3 days",
      "sortOrder": 1
    },
    {
      "dayNumber": 1,
      "providerType": "VEHICLE",
      "providerId": 7,
      "itemName": "Air-conditioned SUV",
      "description": "Private transport for the full trip",
      "sortOrder": 2
    }
  ]
}
```

| Field | Type | Required | Validation |
|---|---|---|---|
| `name` | String | Yes | `@NotBlank`, max 255 chars |
| `description` | String | No | Free text |
| `durationDays` | Integer | Yes | Min 1 |
| `basePrice` | BigDecimal | Yes | Min 0.01 |
| `discountPercentage` | BigDecimal | No | 0–100, default: 0 |
| `maxParticipants` | Integer | No | Min 1 |
| `destinations` | String[] | No | Array of destination names |
| `inclusions` | String[] | No | What's included in the package |
| `exclusions` | String[] | No | What's not included |
| `images` | String[] | No | Package image URLs |
| `isFeatured` | Boolean | No | Default: false |
| `items` | Array | No | Package day-by-day items |

**Response:** `201 Created`

```json
{
  "data": {
    "id": 1,
    "name": "Cultural Triangle Explorer",
    "description": "3-day guided tour...",
    "durationDays": 3,
    "basePrice": 650.00,
    "discountPercentage": 15.00,
    "finalPrice": 552.50,
    "maxParticipants": 12,
    "destinations": ["Sigiriya", "Polonnaruwa", "Dambulla"],
    "inclusions": ["Accommodation", "Tour guide", "Transport", "Entrance tickets", "Breakfast"],
    "exclusions": ["Lunch", "Dinner", "Personal expenses"],
    "images": ["https://storage.example.com/packages/cultural-triangle.jpg"],
    "isFeatured": true,
    "isActive": true,
    "createdBy": "admin-uuid",
    "items": [ ... ],
    "createdAt": "2026-02-24T10:00:00Z",
    "updatedAt": "2026-02-24T10:00:00Z"
  }
}
```

---

### 2.2 Get Package by ID

| Field | Value |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/packages/{id}` |
| **Description** | Retrieves full package details including items. |
| **Auth** | Bearer JWT |

---

### 2.3 Search Packages

| Field | Value |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/packages` |
| **Description** | Browse and filter available packages. |
| **Auth** | Bearer JWT |

**Query Parameters:**

| Parameter | Type | Required | Description |
|---|---|---|---|
| `destination` | String | No | Filter by destination |
| `minDuration` | Integer | No | Minimum duration in days |
| `maxDuration` | Integer | No | Maximum duration in days |
| `maxPrice` | BigDecimal | No | Maximum final price |
| `isFeatured` | Boolean | No | Featured packages only |
| `query` | String | No | Free-text search (name, description, destinations) |
| `page` | Integer | No | Page number (default: 0) |
| `size` | Integer | No | Page size (default: 10) |

**Response:** `200 OK` — Paginated `PackageResponse` list.

---

### 2.4 Update Package

| Field | Value |
|---|---|
| **Method** | `PUT` |
| **URL** | `/api/packages/{id}` |
| **Description** | Updates package details. |
| **Auth** | Bearer JWT — Role: `ADMIN` |

---

### 2.5 Delete Package (Soft Delete)

| Field | Value |
|---|---|
| **Method** | `DELETE` |
| **URL** | `/api/packages/{id}` |
| **Description** | Soft-deletes a package. |
| **Auth** | Bearer JWT — Role: `ADMIN` |

**Response:** `204 No Content`

---

### 2.6 Get Featured Packages

| Field | Value |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/packages/featured` |
| **Description** | Returns currently featured packages for homepage display. |
| **Auth** | Bearer JWT |

---

## 3. Data Model Schema

### 3.1 `packages` Table

| Column | Type | Nullable | Default | Description |
|---|---|---|---|---|
| `id` | `BIGSERIAL` | No | Auto-increment | Primary key |
| `name` | `VARCHAR(255)` | No | — | Package name |
| `description` | `TEXT` | Yes | — | Package description |
| `duration_days` | `INT` | No | — | Trip duration |
| `base_price` | `DECIMAL(10,2)` | No | — | Original price |
| `discount_percentage` | `DECIMAL(5,2)` | Yes | `0` | Discount (0–100) |
| `max_participants` | `INT` | Yes | — | Capacity limit |
| `destinations` | `TEXT[]` | Yes | — | Destination list |
| `inclusions` | `TEXT[]` | Yes | — | What's included |
| `exclusions` | `TEXT[]` | Yes | — | What's excluded |
| `images` | `TEXT[]` | Yes | — | Image URLs |
| `is_featured` | `BOOLEAN` | Yes | `false` | Homepage promotion flag |
| `is_active` | `BOOLEAN` | Yes | `true` | Soft-delete flag |
| `created_by` | `VARCHAR(255)` | No | — | Admin who created it |
| `created_at` | `TIMESTAMPTZ` | Yes | `CURRENT_TIMESTAMP` | Record creation |
| `updated_at` | `TIMESTAMPTZ` | Yes | `CURRENT_TIMESTAMP` | Last modification |

**Indexes:**

| Index | Column(s) | Purpose |
|---|---|---|
| `idx_packages_is_active` | `is_active` | Active package queries |
| `idx_packages_is_featured` | `is_featured` | Featured package quick lookup |

### 3.2 `package_items` Table

| Column | Type | Nullable | Default | Description |
|---|---|---|---|---|
| `id` | `BIGSERIAL` | No | Auto-increment | Primary key |
| `package_id` | `BIGINT` | No | — | FK → `packages.id` (cascade delete) |
| `day_number` | `INT` | No | — | Day in the itinerary |
| `provider_type` | `VARCHAR(50)` | No | — | `HOTEL`, `TOUR_GUIDE`, `VEHICLE` |
| `provider_id` | `BIGINT` | No | — | Provider entity ID |
| `item_name` | `VARCHAR(255)` | No | — | Display name |
| `description` | `TEXT` | Yes | — | Item description |
| `sort_order` | `INT` | Yes | `0` | Display ordering |
| `created_at` | `TIMESTAMPTZ` | Yes | `CURRENT_TIMESTAMP` | Record creation |

**Indexes:**

| Index | Column(s) | Purpose |
|---|---|---|
| `idx_package_items_package_id` | `package_id` | Items per package |

### Relationships

```
packages (1) ──── (0..*) package_items
```

---

## 4. User Input Requirements

### Package Creation

| Field | Validation Rules |
|---|---|
| `name` | Required. 1–255 characters. Unique recommended. |
| `durationDays` | Required. Integer >= 1. |
| `basePrice` | Required. Decimal > 0. |
| `discountPercentage` | Optional. 0–100 range. |
| `maxParticipants` | Optional. Integer >= 1. |
| `items[].dayNumber` | Required. Must be between 1 and `durationDays`. |
| `items[].providerType` | Required. Must be `HOTEL`, `TOUR_GUIDE`, or `VEHICLE`. |
| `items[].providerId` | Required. Must reference an active provider. |

### Business Constraints

- Only `ADMIN` role can create, update, or delete packages.
- `finalPrice` = `basePrice × (1 - discountPercentage/100)`, calculated server-side.
- Package items reference providers by ID; provider validity is checked at creation.
- Featured packages appear on the homepage and in AI Agent recommendations.
- Deleting a package is a soft delete; existing bookings using it remain valid.

---

## 5. Inter-Service Communication

### Inbound (Other Services → Trip Plan Service)

| Consumer | Endpoint | Method | Purpose |
|---|---|---|---|
| AI Agent Service | `/api/packages` (search) | GET (Feign) | Package recommendations |
| AI Agent Service | `/api/packages/{id}` | GET (Feign) | Package details for trip planning |
| Frontend | `/api/packages/**` | REST (via gateway) | Tourist browsing |

### Outbound (Trip Plan Service → Other Services)

| Target | Endpoint | Method | Purpose |
|---|---|---|---|
| Hotel Service | `/api/hotels/{id}` | GET (Feign) | Validate hotel provider in package |
| Tour Guide Service | `/api/tour-guides/{id}` | GET (Feign) | Validate guide provider |
| Vehicle Service | `/api/vehicles/{id}` | GET (Feign) | Validate vehicle provider |

### Communication Method

- **Synchronous:** REST via OpenFeign
- **Gateway Routes:** `/api/trip-plans/**` and `/api/packages/**` → `lb://trip-plan-service`

---

## 6. Security Considerations

### Authentication

- **Method:** Supabase JWT (HS256)
- **Public Endpoints:** `/actuator/**`, `/swagger-ui/**`, `/v3/api-docs/**`

### Role-Based Access Control

| Role | Permissions |
|---|---|
| `TOURIST` | Read-only: browse packages, view details |
| `ADMIN` | Full CRUD on packages |

### Data Validation

- Bean Validation on all DTOs
- Admin-only enforcement on all write operations
- Provider ID validation via Feign calls during package creation

---

## 7. Error Handling Standard

### Error Response Format

```json
{
  "timestamp": "2026-02-24T10:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Package not found with id: '99'",
  "path": "/api/packages/99",
  "traceId": "uuid"
}
```

### Common Error Codes

| HTTP Status | Condition |
|---|---|
| `400` | Invalid input (missing name, negative price, invalid duration) |
| `401` | Missing or invalid JWT |
| `403` | Non-admin attempting to create/update/delete package |
| `404` | Package not found |
| `500` | Internal server error |

---

## 8. Example Request & Response

### Browse Featured Packages

**Request:**

```bash
curl -X GET "http://localhost:8089/api/packages/featured" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..."
```

**Response:** `200 OK`

```json
{
  "data": [
    {
      "id": 1,
      "name": "Cultural Triangle Explorer",
      "description": "3-day guided tour of Sri Lanka's ancient cities.",
      "durationDays": 3,
      "basePrice": 650.00,
      "discountPercentage": 15.00,
      "finalPrice": 552.50,
      "maxParticipants": 12,
      "destinations": ["Sigiriya", "Polonnaruwa", "Dambulla"],
      "inclusions": ["Accommodation", "Tour guide", "Transport", "Entrance tickets"],
      "images": ["https://storage.example.com/packages/cultural-triangle.jpg"],
      "isFeatured": true,
      "isActive": true,
      "items": [
        {
          "id": 1,
          "dayNumber": 1,
          "providerType": "HOTEL",
          "providerId": 8,
          "itemName": "Heritage Hotel Sigiriya",
          "sortOrder": 0
        }
      ]
    },
    {
      "id": 3,
      "name": "Southern Beach Paradise",
      "description": "5-day coastal relaxation from Galle to Mirissa.",
      "durationDays": 5,
      "basePrice": 980.00,
      "discountPercentage": 10.00,
      "finalPrice": 882.00,
      "destinations": ["Galle", "Unawatuna", "Mirissa"],
      "isFeatured": true,
      "isActive": true
    }
  ]
}
```

### Search Packages by Destination

**Request:**

```bash
curl -X GET "http://localhost:8089/api/packages?destination=Ella&maxPrice=500&page=0&size=5" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..."
```

**Response:** `200 OK`

```json
{
  "data": [
    {
      "id": 7,
      "name": "Ella Hill Country Adventure",
      "description": "2-day hiking and train ride experience in Ella.",
      "durationDays": 2,
      "basePrice": 380.00,
      "discountPercentage": 5.00,
      "finalPrice": 361.00,
      "destinations": ["Ella", "Nine Arches Bridge", "Little Adam's Peak"],
      "isFeatured": false,
      "isActive": true
    }
  ],
  "pagination": {
    "page": 0,
    "pageSize": 5,
    "totalItems": 1,
    "totalPages": 1
  }
}
```
