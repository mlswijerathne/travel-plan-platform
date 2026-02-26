# Discovery Server Specification

> **Service Name:** discovery-server
> **Port:** 8761
> **Package:** `com.travelplan.discovery`
> **Database Schema:** N/A (no database)
> **Version:** 1.0.0-SNAPSHOT

---

## 1. Service Overview

### Responsibility

The Discovery Server is the Netflix Eureka-based service registry for the Travel Plan Platform. It enables all microservices to register themselves and discover other services dynamically, supporting load-balanced communication without hardcoded URLs.

### Key Features

- Service registration and heartbeat monitoring
- Service instance discovery with health status
- Eureka dashboard for visual service monitoring
- Self-preservation mode (disabled for dev/small cluster)
- IP-based instance preference for container environments

### Dependencies

| Dependency | Purpose |
|---|---|
| Spring Cloud Netflix Eureka Server | Service registry |
| Spring Boot Starter Security | Basic security for Eureka endpoints |
| Spring Boot Actuator | Health check and monitoring |

---

## 2. API Endpoints

### 2.1 Eureka Dashboard

| Field | Value |
|---|---|
| **Method** | `GET` |
| **URL** | `/` |
| **Description** | Web-based dashboard showing all registered services, their instances, and health status. |
| **Auth** | Permitted (all requests allowed) |

### 2.2 Service Registry API

Eureka provides a built-in REST API:

| Method | URL | Description |
|---|---|---|
| `GET` | `/eureka/apps` | List all registered applications |
| `GET` | `/eureka/apps/{appName}` | Get instances of a specific application |
| `POST` | `/eureka/apps/{appName}` | Register a new instance |
| `DELETE` | `/eureka/apps/{appName}/{instanceId}` | Deregister an instance |
| `PUT` | `/eureka/apps/{appName}/{instanceId}` | Send heartbeat |
| `GET` | `/eureka/apps/{appName}/{instanceId}/status` | Get instance status |

### 2.3 Health Endpoint

| Field | Value |
|---|---|
| **Method** | `GET` |
| **URL** | `/actuator/health` |
| **Description** | Health check for the discovery server itself. |

---

## 3. Configuration

### application.yml

```yaml
server:
  port: 8761

spring:
  application:
    name: discovery-server

eureka:
  instance:
    hostname: localhost
  client:
    register-with-eureka: false      # Don't register itself
    fetch-registry: false             # Don't fetch from another Eureka
    service-url:
      defaultZone: http://localhost:8761/eureka/
  server:
    enable-self-preservation: false   # Disabled for dev/small clusters
    eviction-interval-timer-in-ms: 5000
```

### Registered Services

When all services are running, the Eureka registry contains:

| Application Name | Port | Status |
|---|---|---|
| `api-gateway` | 8060 | UP |
| `tourist-service` | 8082 | UP |
| `hotel-service` | 8083 | UP |
| `tour-guide-service` | 8084 | UP |
| `vehicle-service` | 8085 | UP |
| `booking-service` | 8086 | UP |
| `itinerary-service` | 8087 | UP |
| `review-service` | 8088 | UP |
| `trip-plan-service` | 8089 | UP |
| `event-service` | 8090 | UP |
| `ecommerce-service` | 8091 | UP |
| `ai-agent-service` | 8093 | UP |

---

## 4. Data Model Schema

The Discovery Server has **no database**. Service registry data is held entirely in memory.

---

## 5. Inter-Service Communication

### Inbound

| Consumer | Protocol | Purpose |
|---|---|---|
| All microservices | HTTP | Service registration and heartbeat |
| API Gateway | HTTP | Service discovery for route resolution |
| Feign Clients | HTTP | Service instance lookup for inter-service calls |

### Outbound

None. The Discovery Server is passive — it only responds to registration and query requests.

### Communication Pattern

1. Each microservice registers with Eureka on startup (`register-with-eureka: true`)
2. Services send heartbeats every 30 seconds (default)
3. The API Gateway and Feign clients query Eureka to resolve `lb://service-name` to actual host:port
4. If a service fails to send heartbeats, it is evicted after the eviction interval (5 seconds in dev)

---

## 6. Security Considerations

### Authentication

- **Current:** All requests permitted (development configuration)
- **Production:** Should be secured with basic auth or network-level restrictions

### Security Config

```java
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
```

### Production Recommendations

- Enable basic authentication for Eureka endpoints
- Restrict network access to Eureka to internal services only
- Enable HTTPS for registration traffic
- Enable self-preservation in production to prevent mass eviction during network partitions

---

## 7. Error Handling Standard

### Common Scenarios

| Scenario | Behavior |
|---|---|
| Service fails to register | Service is not discoverable; requests routed to it will fail with 503 |
| Service heartbeat stops | Evicted after `eviction-interval-timer` (5s in dev, 60s default) |
| Eureka itself is down | Services use cached registry; new registrations fail |
| Self-preservation triggered | Eureka stops evicting instances to prevent cascading failures |

---

## 8. Example Request & Response

### View Registered Services

**Request:**

```bash
curl http://localhost:8761/eureka/apps \
  -H "Accept: application/json"
```

**Response:** `200 OK`

```json
{
  "applications": {
    "application": [
      {
        "name": "TOURIST-SERVICE",
        "instance": [
          {
            "instanceId": "192.168.1.100:tourist-service:8082",
            "hostName": "192.168.1.100",
            "port": { "$": 8082, "@enabled": "true" },
            "status": "UP",
            "app": "TOURIST-SERVICE"
          }
        ]
      },
      {
        "name": "HOTEL-SERVICE",
        "instance": [
          {
            "instanceId": "192.168.1.100:hotel-service:8083",
            "hostName": "192.168.1.100",
            "port": { "$": 8083, "@enabled": "true" },
            "status": "UP"
          }
        ]
      }
    ]
  }
}
```

### Eureka Dashboard

Navigate to `http://localhost:8761` in a browser to see the visual dashboard with:

- System status (uptime, environment)
- Currently registered instances with status
- General info and self-preservation status
