# API Gateway Specification

> **Service Name:** api-gateway
> **Port:** 8060
> **Package:** `com.travelplan.gateway`
> **Database Schema:** N/A (no database)
> **Version:** 1.0.0-SNAPSHOT

---

## 1. Service Overview

### Responsibility

The API Gateway is the single entry point for all client requests to the Travel Plan Platform. Built on Spring Cloud Gateway (reactive WebFlux), it handles request routing, JWT authentication, CORS management, and load-balanced forwarding to downstream microservices via Eureka service discovery.

### Key Features

- Centralized request routing to all microservices
- JWT authentication and role extraction at the edge
- CORS configuration for frontend communication
- Load-balanced routing via Eureka service discovery
- Health check and monitoring endpoints
- Rate limiting readiness (extensible filter chain)

### Dependencies

| Dependency | Purpose |
|---|---|
| Spring Cloud Gateway | Reactive HTTP gateway with route predicates and filters |
| Spring Cloud Netflix Eureka Client | Service discovery for load-balanced routing |
| Spring WebFlux + Security | Reactive security filter chain |
| JJWT 0.12.6 | JWT parsing and validation |

---

## 2. Route Configuration

All routes use Eureka-based load balancing (`lb://service-name`).

### Route Table

| Route ID | Path Predicate | Target Service | Port |
|---|---|---|---|
| `tourist-service` | `/api/tourists/**` | `lb://tourist-service` | 8082 |
| `hotel-service` | `/api/hotels/**` | `lb://hotel-service` | 8083 |
| `tour-guide-service` | `/api/tour-guides/**` | `lb://tour-guide-service` | 8084 |
| `vehicle-service` | `/api/vehicles/**` | `lb://vehicle-service` | 8085 |
| `booking-service` | `/api/bookings/**` | `lb://booking-service` | 8086 |
| `itinerary-service` | `/api/itineraries/**` | `lb://itinerary-service` | 8087 |
| `review-service` | `/api/reviews/**` | `lb://review-service` | 8088 |
| `trip-plan-service` | `/api/trip-plans/**`, `/api/packages/**` | `lb://trip-plan-service` | 8089 |
| `event-service` | `/api/events/**` | `lb://event-service` | 8090 |
| `ecommerce-service` | `/api/products/**`, `/api/orders/**` | `lb://ecommerce-service` | 8091 |
| `ai-agent-service` | `/api/chat/**` | `lb://ai-agent-service` | 8093 |

---

## 3. Authentication & Security

### JWT Authentication Filter

The gateway implements a reactive `WebFilter` (`JwtAuthenticationFilter`) that:

1. Extracts the `Authorization: Bearer <token>` header
2. Parses the JWT using JJWT with the Supabase shared secret (HS256)
3. Extracts the `sub` claim as user ID
4. Extracts the role from `app_metadata.role` or `user_metadata.role` (defaults to `TOURIST`)
5. Sets a `UsernamePasswordAuthenticationToken` in the `ReactiveSecurityContextHolder`
6. Forwards the original `Authorization` header to downstream services

### Security Configuration

```
Permitted (no JWT required):
  /actuator/**
  /health
  /eureka/**

All other paths: authenticated
```

### CORS Configuration

| Setting | Value |
|---|---|
| Allowed Origins | `${CORS_ALLOWED_ORIGINS}` (default: `http://localhost:3000`) |
| Allowed Methods | `GET, POST, PUT, PATCH, DELETE, OPTIONS` |
| Allowed Headers | `*` |
| Allow Credentials | `true` |
| Max Age | `3600` seconds |

---

## 4. Data Model Schema

The API Gateway has **no database**. It is a stateless routing layer.

---

## 5. Inter-Service Communication

### Inbound

| Consumer | Protocol | Description |
|---|---|---|
| Frontend (Next.js) | HTTPS | All API requests from browser |
| Mobile clients | HTTPS | Future mobile app integration |

### Outbound

| Target | Protocol | Description |
|---|---|---|
| All microservices | HTTP (load-balanced) | Forwarded requests via Eureka |
| Eureka Discovery Server | HTTP | Service registry and heartbeat |

### Communication Method

- **Routing:** Spring Cloud Gateway with `lb://` load-balanced URIs
- **Discovery:** Eureka client with `fetch-registry: true`
- **JWT Propagation:** Original `Authorization` header forwarded as-is to downstream services

---

## 6. Security Considerations

### Edge Security

- JWT validation happens at the gateway level, providing a security perimeter
- Invalid or expired tokens are rejected before reaching downstream services
- Role extraction enables future route-level authorization

### Configuration

| Environment Variable | Purpose |
|---|---|
| `SUPABASE_JWT_SECRET` | Shared symmetric key for JWT validation |
| `CORS_ALLOWED_ORIGINS` | Frontend origins for CORS headers |
| `EUREKA_URL` | Eureka server URL for service discovery |

---

## 7. Error Handling Standard

### Gateway-Level Errors

| HTTP Status | Condition |
|---|---|
| `401` | Missing, expired, or malformed JWT token |
| `404` | No route matches the request path |
| `502` | Downstream service returned an error |
| `503` | Downstream service is unavailable (not registered in Eureka) |
| `504` | Downstream service timed out |

---

## 8. Example Request & Response

### Request Through Gateway

All frontend requests go through port `8060`:

**Request:**

```bash
curl -X GET http://localhost:8060/api/tourists/me \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..."
```

This request is:
1. Received by the API Gateway on port 8060
2. JWT validated and user ID / role extracted
3. Route matched: `/api/tourists/**` → `lb://tourist-service`
4. Forwarded to tourist-service (port 8082) with original headers
5. Response returned to the client

**Response:** `200 OK` — Proxied from tourist-service.

### Gateway Health Check

**Request:**

```bash
curl http://localhost:8060/actuator/health
```

**Response:** `200 OK`

```json
{
  "status": "UP"
}
```
