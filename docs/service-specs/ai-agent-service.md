# AI Agent Service Specification

> **Service Name:** ai-agent-service
> **Port:** 8093
> **Package:** `com.travelplan.aiagent`
> **Database Schema:** N/A (stateless, in-memory sessions)
> **Version:** 1.0.0-SNAPSHOT

---

## 1. Service Overview

### Responsibility

The AI Agent Service is the conversational intelligence layer of the Travel Plan Platform. It provides a natural-language chat interface powered by Google Gemini (via Google ADK 0.5.0) that enables tourists to plan trips, discover providers, generate itineraries, and analyze budgets — all through a multi-agent orchestration system with platform-first provider prioritization.

### Key Features

- Natural-language conversational trip planning
- Multi-agent orchestration (6 specialized AI agents)
- Real-time streaming responses via Server-Sent Events (SSE)
- Platform-first provider discovery (platform providers prioritized over Google Maps)
- Google Maps integration for directions, nearby places, geocoding, and distance matrix
- Session management with conversation history
- Quick reply chip generation for guided UX
- Trip plan generation with day-by-day itineraries and cost breakdowns
- Provider recommendation aggregation across hotels, guides, and vehicles

### AI Agent Architecture

| Agent | Responsibility |
|---|---|
| **TripPlannerAgent** (Root) | Coordinator that delegates to specialist agents based on user intent |
| **HotelSearchAgent** | Hotel search with platform-first pattern |
| **TourGuideSearchAgent** | Tour guide discovery and recommendation |
| **VehicleSearchAgent** | Vehicle rental search with Google Maps directions |
| **ItineraryGeneratorAgent** | Day-by-day itinerary generation |
| **BudgetAnalyzerAgent** | Cost breakdown analysis and savings tips |

### Dependencies

| Dependency | Purpose |
|---|---|
| `common-lib` | Shared security config (not used in dev mode) |
| Google ADK 0.5.0 | Multi-agent orchestration framework |
| Google Gemini API | LLM backend for all agents |
| Google Maps Platform | Directions, Places, Geocoding, Distance Matrix APIs |
| `hotel-service` | Hotel search and details (Feign) |
| `tour-guide-service` | Tour guide search and details (Feign) |
| `vehicle-service` | Vehicle search and details (Feign) |
| `review-service` | Provider reviews and ratings (Feign) |
| `trip-plan-service` | Package search and details (Feign) |
| Eureka Discovery Server | Service registration and Feign resolution |

---

## 2. API Endpoints

### 2.1 Send Chat Message

| Field | Value |
|---|---|
| **Method** | `POST` |
| **URL** | `/api/chat` |
| **Description** | Sends a user message to the AI agent and returns a complete response. |
| **Auth** | Bearer JWT (bypassed in dev profile) |

**Request Body:**

```json
{
  "message": "I want to plan a 5-day trip to the southern coast of Sri Lanka with my family. Budget around $2000.",
  "sessionId": "existing-session-uuid"
}
```

| Field | Type | Required | Description |
|---|---|---|---|
| `message` | String | Yes | User's natural language message |
| `sessionId` | String | No | Existing session ID (creates new if omitted) |

**Response:** `200 OK`

```json
{
  "data": {
    "message": "I'd love to help you plan a southern coast family trip! Based on your budget of $2000 for 5 days, here's what I recommend:\n\n**Accommodation:** I found the Galle Fort Hotel — a charming 4-star heritage property right inside the fort. Rooms start at $120/night.\n\n**Tour Guide:** Kamal Perera is highly rated (4.75 stars) and specializes in southern coastal tours. His daily rate is $150.\n\nWould you like me to create a detailed day-by-day itinerary?",
    "sessionId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "quickReplies": [
      { "label": "Yes, create itinerary", "value": "create_itinerary" },
      { "label": "Show more hotels", "value": "more_hotels" },
      { "label": "Lower budget options", "value": "budget_options" }
    ],
    "providers": [
      {
        "id": 12,
        "name": "Galle Fort Hotel",
        "type": "HOTEL",
        "price": 120.00,
        "rating": 4.35,
        "source": "PLATFORM"
      },
      {
        "id": 1,
        "name": "Kamal Perera",
        "type": "TOUR_GUIDE",
        "price": 150.00,
        "rating": 4.75,
        "source": "PLATFORM"
      }
    ]
  }
}
```

---

### 2.2 Stream Chat Response (SSE)

| Field | Value |
|---|---|
| **Method** | `POST` |
| **URL** | `/api/chat/stream` |
| **Description** | Sends a user message and streams the response as Server-Sent Events for real-time display. |
| **Auth** | Bearer JWT (bypassed in dev profile) |
| **Content-Type** | `text/event-stream` |

**Request Body:** Same as `/api/chat`.

**SSE Event Types:**

| Event Type | Description | Payload |
|---|---|---|
| `TEXT_DELTA` | Incremental text chunk | `{ "text": "partial text..." }` |
| `TOOL_CALL` | Agent is calling a tool | `{ "toolName": "searchHotels", "args": {...} }` |
| `AGENT_TRANSFER` | Agent delegation | `{ "fromAgent": "TripPlanner", "toAgent": "HotelSearch" }` |
| `FINAL_RESPONSE` | Complete response | Full `ChatResponse` object |
| `ERROR` | Error occurred | `{ "message": "error description" }` |
| `DONE` | Stream complete | `{}` |

**Example SSE Stream:**

```
data: {"type":"TEXT_DELTA","text":"I'd love to help "}

data: {"type":"TEXT_DELTA","text":"you plan a southern coast "}

data: {"type":"TOOL_CALL","toolName":"searchHotels","args":{"city":"Galle","maxPrice":150}}

data: {"type":"TEXT_DELTA","text":"family trip! Based on "}

data: {"type":"FINAL_RESPONSE","message":"...","sessionId":"...","quickReplies":[...]}

data: {"type":"DONE"}
```

---

### 2.3 Get Conversation History

| Field | Value |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/chat/history/{sessionId}` |
| **Description** | Retrieves the conversation history for a session. |
| **Auth** | Bearer JWT |

**Path Parameters:**

| Parameter | Type | Description |
|---|---|---|
| `sessionId` | String | Chat session ID |

**Response:** `200 OK`

```json
{
  "data": {
    "sessionId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "messages": [
      {
        "role": "user",
        "content": "I want to plan a 5-day trip to the southern coast...",
        "timestamp": "2026-02-24T10:00:00Z"
      },
      {
        "role": "assistant",
        "content": "I'd love to help you plan a southern coast family trip!...",
        "timestamp": "2026-02-24T10:00:03Z"
      }
    ]
  }
}
```

---

### 2.4 Get Recommendations

| Field | Value |
|---|---|
| **Method** | `POST` |
| **URL** | `/api/chat/recommend` |
| **Description** | Returns structured provider recommendations based on criteria (non-conversational). |
| **Auth** | Bearer JWT |

**Request Body:**

```json
{
  "destination": "Ella",
  "startDate": "2026-04-10",
  "endDate": "2026-04-12",
  "budget": 500.00,
  "interests": ["hiking", "train rides", "nature"],
  "travelers": 2
}
```

**Response:** `200 OK`

```json
{
  "data": {
    "hotels": [
      {
        "id": 15,
        "name": "98 Acres Resort Ella",
        "type": "HOTEL",
        "price": 95.00,
        "rating": 4.60,
        "source": "PLATFORM"
      }
    ],
    "tourGuides": [
      {
        "id": 5,
        "name": "Nimal Fernando",
        "type": "TOUR_GUIDE",
        "price": 120.00,
        "rating": 4.80,
        "source": "PLATFORM"
      }
    ],
    "vehicles": [
      {
        "id": 10,
        "name": "Suzuki Alto - Economy",
        "type": "VEHICLE",
        "price": 35.00,
        "rating": 4.20,
        "source": "PLATFORM"
      }
    ]
  }
}
```

---

### 2.5 Generate Trip Plan

| Field | Value |
|---|---|
| **Method** | `POST` |
| **URL** | `/api/chat/plan` |
| **Description** | Generates a complete trip plan with itinerary and cost breakdown. |
| **Auth** | Bearer JWT |

**Request Body:**

```json
{
  "destination": "Southern Coast",
  "startDate": "2026-04-01",
  "endDate": "2026-04-05",
  "budget": 2000.00,
  "interests": ["beaches", "whale watching", "historical sites"],
  "travelers": 2
}
```

**Response:** `200 OK`

```json
{
  "data": {
    "itinerary": [
      {
        "dayNumber": 1,
        "date": "2026-04-01",
        "title": "Galle Fort Discovery",
        "activities": [
          "Arrive in Galle via coastal train",
          "Explore Galle Fort UNESCO site",
          "Sunset at the lighthouse"
        ],
        "accommodation": {
          "name": "Galle Fort Hotel",
          "providerId": 12,
          "pricePerNight": 120.00
        },
        "transport": {
          "name": "Private SUV",
          "providerId": 7,
          "dailyRate": 85.00
        }
      }
    ],
    "costBreakdown": {
      "accommodation": 480.00,
      "transport": 340.00,
      "activities": 250.00,
      "food": 300.00,
      "miscellaneous": 130.00,
      "total": 1500.00,
      "savings": 500.00
    }
  }
}
```

---

## 3. Data Model Schema

The AI Agent Service is **stateless** — it does not have its own database. All session data is managed in-memory.

### 3.1 In-Memory Session Store

| Field | Type | Description |
|---|---|---|
| `sessionId` | String (UUID) | Unique session identifier |
| `userId` | String | Supabase UID of the tourist |
| `messages` | List&lt;ChatMessage&gt; | Conversation history |
| `createdAt` | Instant | Session creation time |
| `lastAccessedAt` | Instant | Last interaction time |

**Session Management:**
- Sessions expire after a configurable TTL (default: 30 minutes of inactivity)
- Message history is trimmed to the most recent N messages to stay within LLM context limits
- Session cleanup runs on a scheduled timer (`@EnableScheduling`)

### 3.2 Tool Registry (Static Singleton)

The `ToolRegistry` provides static access to Feign clients and services for ADK tool functions:

| Client | Purpose |
|---|---|
| `HotelServiceClient` | `searchHotels()`, `getHotelById()`, `searchHotelsByQuery()` |
| `TourGuideServiceClient` | `searchTourGuides()`, `getTourGuideById()` |
| `VehicleServiceClient` | `searchVehicles()`, `getVehicleById()` |
| `ReviewServiceClient` | `getReviews()`, `getReviewSummary()` |
| `TripPlanServiceClient` | `searchPackages()`, `getPackageById()` |
| `GoogleMapsService` | `getDirections()`, `searchNearbyPlaces()`, `geocodeLocation()`, `getDistanceMatrix()` |

---

## 4. User Input Requirements

### Chat Messages

| Field | Validation Rules |
|---|---|
| `message` | Required. Non-empty string. Natural language text. |
| `sessionId` | Optional. Valid UUID format if provided. |

### Recommendation Request

| Field | Validation Rules |
|---|---|
| `destination` | Required. Sri Lankan destination name. |
| `startDate` | Required. Future date. |
| `endDate` | Required. After startDate. |
| `budget` | Optional. Positive decimal. |
| `interests` | Optional. Array of interest tags. |
| `travelers` | Optional. Integer >= 1. |

### Business Constraints

- **Platform-First Pattern:** When searching for providers, always query platform services first. Only fall back to Google Maps for supplementary data (directions, nearby places).
- Session state is ephemeral — not persisted to database.
- Conversation context is bounded by LLM context window limits.
- Agent transfers are transparent to the user (seamless delegation).

---

## 5. Inter-Service Communication

### Outbound (AI Agent → Other Services)

| Target | Endpoint | Method | Purpose |
|---|---|---|---|
| Hotel Service | `/api/hotels/search` | GET (Feign) | Search hotels by criteria |
| Hotel Service | `/api/hotels/{id}` | GET (Feign) | Hotel details |
| Tour Guide Service | `/api/tour-guides` | GET (Feign) | Search tour guides |
| Tour Guide Service | `/api/tour-guides/{id}` | GET (Feign) | Guide details |
| Vehicle Service | `/api/vehicles` | GET (Feign) | Search vehicles |
| Vehicle Service | `/api/vehicles/{id}` | GET (Feign) | Vehicle details |
| Review Service | `/api/reviews` | GET (Feign) | Provider reviews |
| Review Service | `/api/reviews/summary` | GET (Feign) | Aggregate ratings |
| Trip Plan Service | `/api/packages` | GET (Feign) | Search packages |
| Trip Plan Service | `/api/packages/{id}` | GET (Feign) | Package details |
| Google Maps API | Directions API | REST (WebClient) | Route planning |
| Google Maps API | Places API | REST (WebClient) | Nearby place discovery |
| Google Maps API | Geocoding API | REST (WebClient) | Location resolution |
| Google Maps API | Distance Matrix API | REST (WebClient) | Travel time estimation |

### Inbound

None directly. The AI Agent Service is consumed by the frontend via the API Gateway.

### Communication Details

- **Feign Timeout:** Default Spring Cloud OpenFeign settings
- **JWT Propagation:** `FeignJwtInterceptor` forwards Authorization header
- **Dev Mode:** `MockFeignClients` provides mock implementations with realistic Sri Lanka data when other services are not running
- **Google Maps:** WebClient-based REST calls with API key authentication
- **Gateway Route:** `/api/chat/**` → `lb://ai-agent-service`

---

## 6. Security Considerations

### Authentication

- **Production:** Supabase JWT (HS256) via `common-lib` SecurityConfig
- **Dev Profile:** `DevSecurityConfig` permits all `/api/chat/**` requests without JWT

### Role-Based Access Control

| Role | Permissions |
|---|---|
| `TOURIST` | Full access to chat, recommendations, trip planning |
| `ADMIN` | Full access |

### Data Validation

- Input message sanitization before passing to LLM
- WebClient codec size limited to 512KB to prevent oversized responses
- Google Maps API key secured via environment variable
- Feign clients use JWT forwarding (no hardcoded credentials)

---

## 7. Error Handling Standard

### Error Response Format

```json
{
  "timestamp": "2026-02-24T10:00:00Z",
  "status": 500,
  "error": "Internal Server Error",
  "message": "AI agent processing failed",
  "path": "/api/chat",
  "traceId": "uuid"
}
```

### Common Error Codes

| HTTP Status | Condition |
|---|---|
| `400` | Empty message, invalid session ID format |
| `401` | Missing or invalid JWT (production only) |
| `404` | Session not found (expired or invalid) |
| `500` | Gemini API failure, agent processing error |
| `503` | Downstream service unavailable (hotel, guide, vehicle service down) |

### SSE Error Handling

Errors during streaming are delivered as `ERROR` events:

```
data: {"type":"ERROR","message":"Failed to search hotels: hotel-service unavailable"}
```

---

## 8. Example Request & Response

### Conversational Trip Planning

**Request:**

```bash
curl -X POST http://localhost:8093/api/chat \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..." \
  -d '{
    "message": "I want to spend 3 days in Ella. I love hiking and nature. Budget is $500 for two people."
  }'
```

**Response:** `200 OK`

```json
{
  "data": {
    "message": "Ella is perfect for hiking and nature lovers! Here's what I found for your 3-day trip:\n\n**Accommodation:**\n- 98 Acres Resort Ella — Stunning mountain views, $95/night (4.6 stars, 47 reviews)\n- Ella Jungle Resort — Eco-friendly treehouse experience, $65/night (4.3 stars)\n\n**Tour Guide:**\n- Nimal Fernando — Specializes in Ella hiking trails, Little Adam's Peak, and Nine Arches Bridge. $120/day (4.8 stars)\n\n**Transport:**\n- Suzuki Alto — Perfect for Ella's winding roads. $35/day\n\n**Estimated Total:** $460 (within your $500 budget!)\n\nWould you like me to create a detailed day-by-day itinerary?",
    "sessionId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
    "quickReplies": [
      { "label": "Create itinerary", "value": "yes_create_itinerary" },
      { "label": "More hotel options", "value": "more_hotels_ella" },
      { "label": "Include train ride", "value": "add_train_experience" },
      { "label": "Check package deals", "value": "ella_packages" }
    ],
    "providers": [
      { "id": 15, "name": "98 Acres Resort Ella", "type": "HOTEL", "price": 95.00, "rating": 4.60, "source": "PLATFORM" },
      { "id": 16, "name": "Ella Jungle Resort", "type": "HOTEL", "price": 65.00, "rating": 4.30, "source": "PLATFORM" },
      { "id": 5, "name": "Nimal Fernando", "type": "TOUR_GUIDE", "price": 120.00, "rating": 4.80, "source": "PLATFORM" },
      { "id": 10, "name": "Suzuki Alto", "type": "VEHICLE", "price": 35.00, "rating": 4.20, "source": "PLATFORM" }
    ]
  }
}
```

### SSE Streaming Example

**Request:**

```bash
curl -X POST http://localhost:8093/api/chat/stream \
  -H "Content-Type: application/json" \
  -H "Accept: text/event-stream" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..." \
  -d '{
    "message": "Show me whale watching options in Mirissa",
    "sessionId": "b2c3d4e5-f6a7-8901-bcde-f12345678901"
  }'
```

**Response:** SSE Stream

```
data: {"type":"AGENT_TRANSFER","fromAgent":"TripPlanner","toAgent":"TourGuideSearch"}

data: {"type":"TOOL_CALL","toolName":"searchTourGuides","args":{"specialization":"whale_watching"}}

data: {"type":"TEXT_DELTA","text":"Great choice! Mirissa is the whale watching "}

data: {"type":"TEXT_DELTA","text":"capital of Sri Lanka. Here are the best options:\n\n"}

data: {"type":"TEXT_DELTA","text":"**Whale Watching Guides:**\n- Chaminda Silva — "}

data: {"type":"FINAL_RESPONSE","message":"Great choice! Mirissa is the whale watching capital...","sessionId":"b2c3d4e5...","quickReplies":[{"label":"Book whale watching","value":"book_whale_watching"},{"label":"Show nearby hotels","value":"mirissa_hotels"}]}

data: {"type":"DONE"}
```
