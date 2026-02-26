# Hotel Service API Documentation

## Overview
Hotel Service manages hotel and room CRUD operations, availability checking, and rating updates for the Travel Plan Platform.

## Base URL
`http://localhost:8083`

## Authentication
All endpoints (except GET operations for public data) require JWT authentication via Supabase.
Include the JWT token in the Authorization header: `Authorization: Bearer <token>`

---

## Hotel Endpoints

### Create Hotel
**POST** `/api/hotels`

Creates a new hotel for the authenticated owner.

**Request Body:**
```json
{
  "name": "Grand Hotel Colombo",
  "description": "Luxury hotel in the heart of Colombo",
  "address": "123 Galle Road, Colombo 03",
  "city": "Colombo",
  "latitude": 6.9271,
  "longitude": 79.8612,
  "starRating": 5,
  "amenities": ["WiFi", "Pool", "Spa", "Restaurant"],
  "checkInTime": "14:00:00",
  "checkOutTime": "11:00:00"
}
```

**Response:** `201 Created`
```json
{
  "data": {
    "id": 1,
    "ownerId": "uuid-123",
    "name": "Grand Hotel Colombo",
    "description": "Luxury hotel in the heart of Colombo",
    "address": "123 Galle Road, Colombo 03",
    "city": "Colombo",
    "latitude": 6.9271,
    "longitude": 79.8612,
    "starRating": 5,
    "averageRating": 0.00,
    "reviewCount": 0,
    "amenities": ["WiFi", "Pool", "Spa", "Restaurant"],
    "checkInTime": "14:00:00",
    "checkOutTime": "11:00:00",
    "isActive": true,
    "createdAt": "2026-02-26T10:00:00Z",
    "updatedAt": "2026-02-26T10:00:00Z"
  },
  "meta": {
    "timestamp": "2026-02-26T10:00:00Z",
    "requestId": null
  }
}
```

---

### Get Hotel by ID
**GET** `/api/hotels/{id}`

Retrieves a hotel by its ID.

**Response:** `200 OK`
```json
{
  "data": {
    "id": 1,
    "name": "Grand Hotel Colombo",
    ...
  }
}
```

---

### Get Hotel with Rooms
**GET** `/api/hotels/{id}/details`

Retrieves a hotel with all its rooms.

**Response:** `200 OK`
```json
{
  "data": {
    "id": 1,
    "name": "Grand Hotel Colombo",
    "rooms": [
      {
        "id": 1,
        "roomType": "DELUXE",
        "name": "Deluxe Ocean View",
        "pricePerNight": 150.00,
        ...
      }
    ],
    ...
  }
}
```

---

### Get All Hotels
**GET** `/api/hotels?page=0&size=20`

Retrieves all active hotels with pagination.

**Query Parameters:**
- `page` (optional, default: 0)
- `size` (optional, default: 20)

**Response:** `200 OK`
```json
{
  "data": [...],
  "pagination": {
    "page": 0,
    "pageSize": 20,
    "totalItems": 50,
    "totalPages": 3
  }
}
```

---

### Get Hotels by Owner
**GET** `/api/hotels/owner?page=0&size=20`

Retrieves all hotels owned by the authenticated user.

**Authentication Required:** Yes

---

### Search Hotels
**GET** `/api/hotels/search?city=Colombo&starRating=4&page=0&size=20`

Searches hotels by city and/or star rating.

**Query Parameters:**
- `city` (optional)
- `starRating` (optional, minimum rating)
- `page` (optional, default: 0)
- `size` (optional, default: 20)

---

### Search Hotels by Query
**GET** `/api/hotels/query?q=beach&page=0&size=20`

Searches hotels by name, city, or description.

**Query Parameters:**
- `q` (required, search query)
- `page` (optional, default: 0)
- `size` (optional, default: 20)

---

### Update Hotel
**PUT** `/api/hotels/{id}`

Updates a hotel (only owner can update).

**Authentication Required:** Yes

**Request Body:** (all fields optional)
```json
{
  "name": "Grand Hotel Colombo - Updated",
  "description": "Updated description",
  "starRating": 5,
  "isActive": true,
  ...
}
```

---

### Delete Hotel
**DELETE** `/api/hotels/{id}`

Deletes a hotel (only owner can delete).

**Authentication Required:** Yes

**Response:** `204 No Content`

---

### Check Hotel Availability
**GET** `/api/hotels/{id}/availability?startDate=2026-03-01&endDate=2026-03-05`

Checks room availability for a hotel.

**Query Parameters:**
- `startDate` (required, format: YYYY-MM-DD)
- `endDate` (required, format: YYYY-MM-DD)

**Response:** `200 OK`
```json
{
  "data": {
    "hotelId": 1,
    "hotelName": "Grand Hotel Colombo",
    "available": true,
    "availableRooms": 25,
    "message": "Rooms available for booking"
  }
}
```

---

## Room Endpoints

### Create Room
**POST** `/api/rooms`

Creates a new room for a hotel.

**Authentication Required:** Yes (must be hotel owner)

**Request Body:**
```json
{
  "hotelId": 1,
  "roomType": "DELUXE",
  "name": "Deluxe Ocean View",
  "description": "Spacious room with ocean view",
  "pricePerNight": 150.00,
  "maxOccupancy": 2,
  "amenities": ["AC", "TV", "Mini Bar"],
  "totalRooms": 10
}
```

**Response:** `201 Created`

---

### Get Room by ID
**GET** `/api/rooms/{id}`

Retrieves a room by its ID.

---

### Get Rooms by Hotel
**GET** `/api/rooms/hotel/{hotelId}`

Retrieves all rooms for a specific hotel.

**Response:** `200 OK`
```json
{
  "data": [
    {
      "id": 1,
      "hotelId": 1,
      "roomType": "DELUXE",
      "name": "Deluxe Ocean View",
      "pricePerNight": 150.00,
      ...
    }
  ]
}
```

---

### Get Rooms by Hotel (Paginated)
**GET** `/api/rooms/hotel/{hotelId}/paginated?page=0&size=20`

Retrieves rooms for a hotel with pagination.

---

### Update Room
**PUT** `/api/rooms/{id}`

Updates a room (only hotel owner can update).

**Authentication Required:** Yes

---

### Delete Room
**DELETE** `/api/rooms/{id}`

Deletes a room (only hotel owner can delete).

**Authentication Required:** Yes

**Response:** `204 No Content`

---

## Error Responses

All errors follow this format:

```json
{
  "error": "Error message",
  "status": 404,
  "timestamp": "2026-02-26T10:00:00Z",
  "path": "/api/hotels/999"
}
```

**Common Error Codes:**
- `400 Bad Request` - Invalid request data
- `401 Unauthorized` - Missing or invalid authentication
- `403 Forbidden` - User doesn't have permission
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server error

---

## Event Handling

### Rating Update Events

The service listens for rating update events from the review-service via message queue.

**Event Format:**
```json
{
  "eventType": "review.rating.updated",
  "eventId": "uuid",
  "timestamp": "2026-02-26T10:00:00Z",
  "version": "1.0",
  "source": "review-service",
  "payload": {
    "entityType": "HOTEL",
    "entityId": 1,
    "newRating": 4.5,
    "reviewCount": 100
  }
}
```

The service automatically updates hotel ratings when these events are received.

---

## Health Check

**GET** `/actuator/health`

Returns the health status of the service.

**Response:** `200 OK`
```json
{
  "status": "UP"
}
```

---

## API Documentation

Interactive API documentation is available at:
- Swagger UI: `http://localhost:8083/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8083/v3/api-docs`
