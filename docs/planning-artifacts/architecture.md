---
stepsCompleted: [1, 2, 3, 4, 5, 6, 7, 8]
status: 'complete'
completedAt: '2026-01-31'
inputDocuments:
  - "docs/project_idea.md"
  - "_bmad-output/brainstorming/FINAL-travel-app-microservices-architecture-report.md"
  - "_bmad-output/brainstorming/phase4-decision-tree-implementation-specs.md"
  - "_bmad-output/planning-artifacts/ux-design-specification.md"
  - "_bmad-output/planning-artifacts/prd.md"
workflowType: 'architecture'
project_name: 'Travel Plan Web Application'
user_name: 'LakshithaWijerathneB'
date: '2026-01-31'
---

# Architecture Decision Document

_This document builds collaboratively through step-by-step discovery. Sections are appended as we work through each architectural decision together._

## Project Context Analysis

### Requirements Overview

**Functional Requirements:**

The Travel Plan Web Application is an AI-powered travel planning platform for Sri Lanka tourism with the following core capabilities:

1. **AI Trip Planning** - Natural language conversation with Gemini-powered agent that generates personalized itineraries
2. **Provider Marketplace** - Hotels, Tour Guides, and Vehicle Owners register and receive bookings through the platform
3. **Internal-First Pattern** - Registered providers always prioritized; external data (Google Maps) supplements only when internal results < 3
4. **Booking Management** - Multi-provider booking in single transaction, cancellation with refund policies
5. **Itinerary Lifecycle** - Trip planning → During trip expense tracking → Post-trip reviews
6. **Review Ecosystem** - Reviews flow back to update provider ratings, creating feedback loop
7. **Package Offerings** - Admin-curated pre-built trip packages for quick booking
8. **Event Discovery** - Admin-posted events with basic CRUD (simplified scope)
9. **E-Commerce** - Basic product catalog for trip-related items (simplified scope)

**Non-Functional Requirements:**

| NFR | Requirement | Architectural Impact |
|-----|-------------|---------------------|
| **Performance** | AI chat streaming responses | SSE/WebSocket for real-time feel |
| **Scalability** | Handle concurrent trip planning sessions | Stateless services, managed infrastructure |
| **Availability** | Core booking flow must be reliable | AWS managed services, circuit breakers |
| **Security** | Multi-tenant with role-based access | Supabase JWT, service-level authorization |
| **Maintainability** | University project with potential future extension | Clean service boundaries, shared DTO library |

**Scale & Complexity:**

- Primary domain: Full-stack distributed system
- Complexity level: Medium-High
- Backend services: 8 Spring Boot microservices
- Frontend: 1 Next.js application (3 route groups: tourist, provider, admin)
- External integrations: Supabase, Gemini API, AWS (ECS, SQS, Cloud Map, SES)

### Technical Constraints & Dependencies

| Constraint | Description |
|------------|-------------|
| **University Requirement** | Must implement 9 services with CRUD and inter-service communication |
| **Timeline** | ~5 weeks (completion by March first week) |
| **No Payment Gateway** | Booking flow without actual payment processing |
| **Supabase Dependency** | Auth and PostgreSQL hosted externally |
| **AWS Deployment** | ECS Fargate containers, managed infrastructure |
| **Gemini API** | External AI service with rate limits and costs |

### Cross-Cutting Concerns Identified

1. **Authentication & Authorization**
   - Supabase handles user identity (JWT tokens)
   - Each service validates JWT and enforces role-based access
   - Roles: TOURIST, HOTEL_OWNER, TOUR_GUIDE, VEHICLE_OWNER, ADMIN

2. **Inter-Service Communication**
   - Synchronous: OpenFeign clients for data queries
   - Asynchronous: Amazon SQS for event notifications (rating updates, booking confirmations)
   - Service Discovery: AWS Cloud Map

3. **Rating Propagation Pattern**
   - Review Service calculates aggregates
   - Pushes RatingUpdateEvent to relevant provider service
   - Provider services store denormalized ratings for fast queries

4. **Transaction Integrity**
   - Saga pattern for multi-provider bookings
   - Pre-check availability before initiating booking
   - Compensating transactions on partial failures

5. **Configuration Management**
   - AWS Parameter Store for environment-specific config
   - Eliminates need for Spring Cloud Config Server

6. **Observability**
   - Structured logging with correlation IDs
   - AWS CloudWatch for metrics and logs

## Starter Template Evaluation

### Primary Technology Domains

This project requires two distinct technology starters:
1. **Backend**: Spring Boot microservices (Java)
2. **Frontend**: Next.js with React (TypeScript)

### Backend Starter: Spring Initializr

**Tool:** Spring Initializr (start.spring.io)

**Base Configuration:**
| Setting | Value |
|---------|-------|
| Project | Maven |
| Language | Java 21 |
| Spring Boot | 3.5.x (stable, well-documented) |
| Packaging | Jar |

**Rationale:** Spring Boot 3.5.x chosen over 4.0.x for stability and documentation availability given the university project timeline. Spring Initializr provides a consistent starting point for all microservices.

**Common Dependencies (All Services):**
- Spring Web
- Spring Boot DevTools
- Lombok
- Spring Boot Actuator
- Validation
- Spring Data JPA
- PostgreSQL Driver

**Additional Dependencies by Service:**
| Service | Additional Dependencies |
|---------|------------------------|
| Booking Service | Spring Cloud OpenFeign |
| Itinerary Service | Spring Cloud OpenFeign |
| AI Agent Service | Spring Cloud OpenFeign, Spring WebFlux |
| Trip Plan Service | Spring Cloud OpenFeign |

### Frontend Starter: Next.js + shadcn/ui

**Initialization Commands:**
```bash
npx create-next-app@latest travel-plan-frontend \
  --typescript --tailwind --eslint --app --src-dir --import-alias "@/*"

cd travel-plan-frontend
npx shadcn@latest init
```

**Rationale:** Next.js 16 with App Router provides stable Turbopack, React 19, and Server Components. shadcn/ui provides accessible, customizable components that match the UX specification requirements.

### Architectural Decisions Provided by Starters

| Decision | Backend (Spring Boot) | Frontend (Next.js) |
|----------|----------------------|-------------------|
| **Language** | Java 21 | TypeScript 5.x |
| **Build Tool** | Maven | Turbopack |
| **Testing** | JUnit 5, Mockito | Jest, React Testing Library |
| **Linting** | Checkstyle (optional) | ESLint |
| **Styling** | N/A | Tailwind CSS |
| **Component Library** | N/A | shadcn/ui (Radix primitives) |

**Note:** Project initialization using these starters should be the first implementation task.

## Core Architectural Decisions

### Decision Priority Analysis

**Critical Decisions (Block Implementation):**
- Database: Supabase PostgreSQL with schema-per-service isolation
- Authentication: Supabase Auth with JWT validation in each service
- Inter-service Communication: OpenFeign (sync) + Amazon SQS (async)
- AI Integration: Google Gemini API via AI Agent Service

**Important Decisions (Shape Architecture):**
- Migration Tool: Flyway for database versioning
- Caching: Spring Cache with Caffeine (in-memory)
- State Management: TanStack Query + React Context
- API Documentation: SpringDoc OpenAPI

**Deferred Decisions (Post-MVP):**
- Redis distributed caching (if scaling requires)
- Rate limiting (if traffic demands)
- Advanced monitoring (Prometheus/Grafana)

### Data Architecture

| Decision | Choice | Version | Rationale |
|----------|--------|---------|-----------|
| **Database** | Supabase PostgreSQL | 15.x | Managed hosting, built-in auth integration, Row Level Security |
| **ORM** | Spring Data JPA + Hibernate | 6.x | Spring Boot default, mature ecosystem |
| **Migration Tool** | Flyway | 10.x | Simple SQL-based migrations, easy versioning |
| **Caching** | Spring Cache + Caffeine | 3.x | In-memory, no external dependency, sufficient for MVP |
| **Connection Pooling** | HikariCP | Default | Spring Boot default, high performance |

**Schema Strategy:**
- One PostgreSQL instance with separate schemas per service
- Each service connects only to its own schema
- Cross-service data access via API calls, not direct DB queries

### Authentication & Security

| Decision | Choice | Rationale |
|----------|--------|-----------|
| **Identity Provider** | Supabase Auth | Managed auth, JWT tokens, multiple auth methods |
| **Token Format** | JWT (HS256) | Supabase default, validated in each service |
| **Authorization** | Role-based (RBAC) | 5 roles: TOURIST, HOTEL_OWNER, TOUR_GUIDE, VEHICLE_OWNER, ADMIN |
| **API Security** | Bearer token in Authorization header | Standard REST pattern |
| **CORS** | Configured per environment | Allow frontend origin only |

**Security Implementation:**
- Each Spring Boot service validates JWT using Supabase secret
- Role extracted from JWT claims for authorization
- Public endpoints: health checks, login redirect
- Protected endpoints: require valid JWT + appropriate role

### API & Communication Patterns

| Decision | Choice | Version | Rationale |
|----------|--------|---------|-----------|
| **API Style** | REST | - | Simple, well-understood, sufficient for project scope |
| **Documentation** | SpringDoc OpenAPI | 2.x | Auto-generated Swagger UI from annotations |
| **Serialization** | Jackson JSON | Default | Spring Boot default |
| **Validation** | Jakarta Validation | 3.x | Annotation-based, integrated with Spring |
| **Sync Communication** | Spring Cloud OpenFeign | 4.x | Declarative REST clients between services |
| **Async Communication** | Amazon SQS | - | Managed queue, no broker to maintain |

**Error Response Standard:**

All services return errors in this format:
```json
{
  "timestamp": "2026-01-31T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Human-readable error description",
  "path": "/api/resource",
  "traceId": "correlation-id"
}
```

**Inter-Service Communication Matrix:**

| Pattern | Use Case | Technology |
|---------|----------|------------|
| Synchronous Query | AI Agent → Provider Services | OpenFeign |
| Synchronous Query | Booking → Check Availability | OpenFeign |
| Async Event | Review → Rating Update | SQS |
| Async Event | Booking → Email Notification | SQS |
| Async Event | Itinerary → Review Request | SQS |

### Frontend Architecture

| Decision | Choice | Version | Rationale |
|----------|--------|---------|-----------|
| **Framework** | Next.js | 16.x | App Router, Server Components, Turbopack |
| **Language** | TypeScript | 5.x | Type safety, better DX |
| **Styling** | Tailwind CSS | 4.x | Utility-first, matches UX spec |
| **Components** | shadcn/ui | Latest | Accessible, customizable, Radix primitives |
| **Server State** | TanStack Query | 5.x | Automatic caching, background sync, optimistic updates |
| **Client State** | React Context | Built-in | Simple UI state (modals, sidebar) |
| **Forms** | React Hook Form + Zod | 7.x / 3.x | Performance, type-safe validation |
| **Icons** | Lucide React | Latest | Consistent with shadcn/ui |
| **Maps** | Leaflet | 1.9.x | Open-source, no API key required |

**Route Structure:**
```
src/app/
├── (public)/           # Landing, login
├── (tourist)/          # Tourist authenticated routes
├── (provider)/         # Provider authenticated routes
└── (admin)/            # Admin authenticated routes
```

### Infrastructure & Deployment

| Decision | Choice | Rationale |
|----------|--------|-----------|
| **Container Runtime** | Docker | Industry standard |
| **Local Development** | Docker Compose | One command to run all services |
| **Container Registry** | AWS ECR | Native AWS integration |
| **Compute** | AWS ECS Fargate | Serverless containers, no EC2 management |
| **Service Discovery** | AWS Cloud Map | Native ECS integration |
| **Configuration** | AWS Parameter Store | Centralized secrets and config |
| **Queue** | Amazon SQS | Managed, no broker maintenance |
| **Email** | Amazon SES | Simple email sending |
| **CI/CD** | GitHub Actions | Free, good AWS integration |
| **Frontend Hosting** | Vercel or AWS Amplify | Optimized for Next.js |

**Testing Strategy:**

| Layer | Tool | Scope |
|-------|------|-------|
| Unit (Backend) | JUnit 5 + Mockito | Service layer logic |
| Integration (Backend) | Spring Boot Test + Testcontainers | Repository + Controller |
| Unit (Frontend) | Jest + React Testing Library | Component behavior |
| E2E (Optional) | Playwright | Critical user flows |

### Decision Impact Analysis

**Implementation Sequence:**
1. Set up monorepo structure (backend + frontend)
2. Configure Supabase project (auth + database schemas)
3. Create common-lib with shared DTOs and error handling
4. Implement Tourist Service (first service, establishes patterns)
5. Implement Provider Services (Hotel, Guide, Vehicle)
6. Implement Booking + Itinerary Services
7. Implement Review Service with rating propagation
8. Implement AI Agent Service with Gemini integration
9. Implement simplified Event + E-Commerce Services
10. Frontend development in parallel after API contracts defined

**Cross-Component Dependencies:**
- All services depend on common-lib for DTOs and exceptions
- All services depend on Supabase for auth validation
- Booking depends on all Provider services for availability
- Review pushes updates to all Provider services
- AI Agent queries all Provider services + Review for recommendations

## Implementation Patterns & Consistency Rules

### Pattern Categories Defined

**Critical Conflict Points Addressed:** 25+ areas where AI agents could make different choices, now standardized.

### Naming Patterns

#### Database Naming Conventions

| Element | Convention | Example |
|---------|------------|---------|
| Tables | snake_case, plural | `hotels`, `tour_guides`, `booking_items` |
| Columns | snake_case | `created_at`, `user_id`, `is_active` |
| Primary Keys | `id` | `id` (BIGSERIAL) |
| Foreign Keys | `{table}_id` | `hotel_id`, `tourist_id` |
| Indexes | `idx_{table}_{columns}` | `idx_hotels_location` |
| Constraints | `{table}_{type}_{columns}` | `hotels_uk_email`, `bookings_fk_hotel` |

#### API Naming Conventions

| Element | Convention | Example |
|---------|------------|---------|
| Endpoints | plural, kebab-case | `/api/hotels`, `/api/tour-guides` |
| Path params | `{id}` format | `/api/hotels/{id}` |
| Query params | camelCase | `?locationId=1&isAvailable=true` |
| Actions | POST to resource | `POST /api/bookings/{id}/cancel` |

#### JSON Field Conventions

| Element | Convention | Example |
|---------|------------|---------|
| Fields | camelCase | `createdAt`, `userId`, `isActive` |
| Dates | ISO 8601 | `"2026-02-15T10:30:00Z"` |
| Booleans | true/false | `"isAvailable": true` |
| Nulls | Explicit null | `"middleName": null` |

#### Code Naming Conventions

**Java (Backend):**

| Element | Convention | Example |
|---------|------------|---------|
| Classes | PascalCase | `HotelService`, `BookingController` |
| Methods | camelCase | `findByLocation()`, `createBooking()` |
| Variables | camelCase | `hotelId`, `isAvailable` |
| Constants | UPPER_SNAKE | `MAX_BOOKING_DAYS` |
| Packages | lowercase.dots | `com.travelplan.hotel.service` |

**TypeScript (Frontend):**

| Element | Convention | Example |
|---------|------------|---------|
| Components | PascalCase file + export | `HotelCard.tsx` |
| Hooks | use prefix | `useBookings.ts` |
| Utils | kebab-case | `date-utils.ts` |
| Types | PascalCase | `HotelResponse`, `BookingStatus` |

### Structure Patterns

#### Backend Service Structure

Each microservice follows this structure:
```
{service-name}/
├── src/main/java/com/travelplan/{domain}/
│   ├── {Domain}ServiceApplication.java
│   ├── controller/           # REST controllers
│   ├── service/              # Business logic interfaces
│   │   └── impl/             # Service implementations
│   ├── repository/           # JPA repositories
│   ├── entity/               # JPA entities
│   ├── dto/                  # Request/Response DTOs
│   ├── mapper/               # Entity ↔ DTO mappers
│   ├── exception/            # Custom exceptions
│   ├── config/               # Configuration classes
│   └── client/               # Feign clients (if needed)
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/         # Flyway migrations
└── src/test/java/            # Mirror of main structure
```

#### Frontend Structure

```
src/
├── app/                      # Next.js App Router
│   ├── (public)/             # Unauthenticated routes
│   ├── (tourist)/            # Tourist authenticated routes
│   ├── (provider)/           # Provider authenticated routes
│   └── (admin)/              # Admin authenticated routes
├── components/
│   ├── ui/                   # shadcn/ui base components
│   ├── chat/                 # AI chat components
│   ├── booking/              # Booking flow components
│   ├── provider/             # Provider dashboard components
│   └── shared/               # Cross-cutting components
├── lib/
│   ├── api/                  # API client functions
│   ├── supabase/             # Supabase client config
│   ├── utils/                # Utility functions
│   └── validators/           # Zod schemas
├── hooks/                    # Custom React hooks
└── types/                    # TypeScript type definitions
```

### Format Patterns

#### API Response Formats

**Success Response:**
```json
{
  "data": { ... },
  "meta": {
    "timestamp": "2026-01-31T10:00:00Z",
    "requestId": "abc-123"
  }
}
```

**Paginated Response:**
```json
{
  "data": [ ... ],
  "pagination": {
    "page": 1,
    "pageSize": 20,
    "totalItems": 156,
    "totalPages": 8
  }
}
```

**Error Response:**
```json
{
  "timestamp": "2026-01-31T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Human-readable error description",
  "path": "/api/resource",
  "traceId": "correlation-id"
}
```

### Communication Patterns

#### SQS Event Naming

**Format:** `{domain}.{entity}.{action}`

| Event | Description |
|-------|-------------|
| `review.rating.updated` | Rating recalculated after new review |
| `booking.reservation.created` | New booking confirmed |
| `booking.reservation.cancelled` | Booking cancelled |
| `itinerary.trip.completed` | Trip end date passed |
| `email.notification.requested` | Email needs to be sent |

#### Event Payload Structure

```json
{
  "eventType": "review.rating.updated",
  "eventId": "uuid-v4",
  "timestamp": "2026-01-31T10:00:00Z",
  "version": "1.0",
  "source": "review-service",
  "payload": {
    "entityType": "HOTEL",
    "entityId": "123",
    "newRating": 4.5,
    "reviewCount": 48
  }
}
```

### Process Patterns

#### Error Handling

**Backend - Global Exception Handler:**
Every service includes `GlobalExceptionHandler` class that maps:
- `ResourceNotFoundException` → 404
- `ValidationException` → 400
- `UnauthorizedException` → 401
- `ForbiddenException` → 403
- `ServiceUnavailableException` → 503
- Generic `Exception` → 500

**Frontend - Error Boundaries:**
- Route-level error boundaries for each route group
- Component-level try-catch for critical operations
- TanStack Query `onError` callbacks for API errors

#### Loading State Patterns

**Frontend Loading Pattern:**
```typescript
const { data, isLoading, isError, error } = useQuery({...});

if (isLoading) return <Skeleton />;
if (isError) return <ErrorDisplay error={error} />;
return <DataDisplay data={data} />;
```

**Loading State Naming:**
- `isLoading` - Initial data fetch
- `isFetching` - Background refetch
- `isSubmitting` - Form submission
- `isPending` - Mutation in progress

### Enforcement Guidelines

**All AI Agents MUST:**

1. **Follow naming conventions exactly** - No mixing camelCase/snake_case in same layer
2. **Use standard project structure** - No custom folder arrangements
3. **Return API responses in defined format** - Always wrap in data/meta or error format
4. **Handle errors via global handler** - No ad-hoc error responses
5. **Use Flyway for all DB changes** - No manual SQL or JPA auto-DDL
6. **Place tests in standard locations** - Co-located with source or in test folder
7. **Use defined DTO patterns** - Separate Request/Response DTOs, never expose entities

**Pattern Verification:**
- Code reviews check pattern compliance
- Linting rules enforce naming conventions
- Integration tests verify API response formats
- CI pipeline validates migration files exist

### Anti-Patterns to Avoid

| Anti-Pattern | Correct Pattern |
|--------------|-----------------|
| Exposing JPA entities in API | Use DTOs with mappers |
| Mixing snake_case in JSON | Always camelCase in JSON |
| Custom error response formats | Use GlobalExceptionHandler |
| Manual SQL without migrations | Flyway V{n}__description.sql |
| Business logic in controllers | Controllers call services only |
| Hardcoded config values | Use application.yml + Parameter Store |
| Direct cross-schema DB queries | Use Feign clients for cross-service data |

## Project Structure & Boundaries

### Complete Project Directory Structure

**Monorepo Layout:**

```
travel-plan-platform/
├── README.md
├── .gitignore
├── .github/
│   └── workflows/
│       ├── backend-ci.yml
│       ├── frontend-ci.yml
│       └── deploy.yml
│
├── backend/                              # Spring Boot Microservices
│   ├── pom.xml                           # Parent POM
│   ├── docker-compose.yml                # Local development
│   ├── docker-compose.prod.yml           # Production template
│   │
│   ├── common-lib/                       # Shared library
│   │   ├── pom.xml
│   │   └── src/main/java/com/travelplan/common/
│   │       ├── dto/
│   │       │   ├── ApiResponse.java
│   │       │   ├── PaginatedResponse.java
│   │       │   ├── ErrorResponse.java
│   │       │   ├── RecommendationDTO.java
│   │       │   ├── BookingRequest.java
│   │       │   ├── BookingResponse.java
│   │       │   └── RatingUpdateEvent.java
│   │       ├── exception/
│   │       │   ├── ResourceNotFoundException.java
│   │       │   ├── ValidationException.java
│   │       │   ├── UnauthorizedException.java
│   │       │   ├── ForbiddenException.java
│   │       │   └── ServiceUnavailableException.java
│   │       ├── config/
│   │       │   ├── SecurityConfig.java
│   │       │   ├── JwtValidationFilter.java
│   │       │   └── CorsConfig.java
│   │       └── util/
│   │           ├── DateUtils.java
│   │           └── ValidationUtils.java
│   │
│   ├── tourist-service/
│   │   ├── pom.xml
│   │   ├── Dockerfile
│   │   └── src/
│   │       ├── main/
│   │       │   ├── java/com/travelplan/tourist/
│   │       │   │   ├── TouristServiceApplication.java
│   │       │   │   ├── controller/TouristController.java
│   │       │   │   ├── service/
│   │       │   │   │   ├── TouristService.java
│   │       │   │   │   └── impl/TouristServiceImpl.java
│   │       │   │   ├── repository/TouristRepository.java
│   │       │   │   ├── entity/
│   │       │   │   │   ├── Tourist.java
│   │       │   │   │   └── TouristPreference.java
│   │       │   │   ├── dto/
│   │       │   │   ├── mapper/TouristMapper.java
│   │       │   │   └── config/TouristConfig.java
│   │       │   └── resources/
│   │       │       ├── application.yml
│   │       │       ├── application-dev.yml
│   │       │       ├── application-prod.yml
│   │       │       └── db/migration/
│   │       │           ├── V1__create_tourists_table.sql
│   │       │           └── V2__create_preferences_table.sql
│   │       └── test/java/com/travelplan/tourist/
│   │
│   ├── hotel-service/                    # Provider service template
│   │   ├── pom.xml
│   │   ├── Dockerfile
│   │   └── src/main/java/com/travelplan/hotel/
│   │       ├── HotelServiceApplication.java
│   │       ├── controller/HotelController.java
│   │       ├── service/
│   │       ├── repository/
│   │       ├── entity/
│   │       ├── dto/
│   │       ├── mapper/
│   │       └── config/
│   │
│   ├── tour-guide-service/               # Same structure as hotel
│   ├── vehicle-service/                  # Same structure as hotel
│   │
│   ├── booking-service/
│   │   └── src/main/java/com/travelplan/booking/
│   │       ├── client/                   # Feign clients
│   │       │   ├── HotelClient.java
│   │       │   ├── TourGuideClient.java
│   │       │   └── VehicleClient.java
│   │       └── service/SagaOrchestrator.java
│   │
│   ├── itinerary-service/
│   ├── review-service/
│   │   └── src/main/java/com/travelplan/review/
│   │       └── messaging/RatingUpdatePublisher.java
│   │
│   ├── trip-plan-service/
│   ├── event-service/                    # Simplified CRUD
│   ├── ecommerce-service/                # Simplified CRUD
│   │
│   └── ai-agent-service/
│       └── src/main/java/com/travelplan/aiagent/
│           ├── service/
│           │   ├── GeminiService.java
│           │   └── TripPlannerService.java
│           └── client/                   # All provider clients
│
└── frontend/                             # Next.js Application
    ├── package.json
    ├── next.config.ts
    ├── tailwind.config.ts
    ├── tsconfig.json
    ├── components.json
    ├── .env.example
    ├── Dockerfile
    │
    ├── public/
    │   ├── images/
    │   └── fonts/
    │
    └── src/
        ├── app/
        │   ├── globals.css
        │   ├── layout.tsx
        │   ├── page.tsx
        │   ├── (public)/
        │   │   ├── login/page.tsx
        │   │   ├── register/page.tsx
        │   │   └── packages/
        │   ├── (tourist)/
        │   │   ├── layout.tsx
        │   │   ├── chat/page.tsx
        │   │   ├── trips/
        │   │   ├── bookings/page.tsx
        │   │   ├── reviews/
        │   │   └── profile/page.tsx
        │   ├── (provider)/
        │   │   ├── layout.tsx
        │   │   ├── dashboard/page.tsx
        │   │   ├── bookings/
        │   │   ├── listings/
        │   │   └── settings/page.tsx
        │   └── (admin)/
        │       ├── layout.tsx
        │       ├── dashboard/page.tsx
        │       ├── events/
        │       ├── packages/
        │       └── users/page.tsx
        │
        ├── components/
        │   ├── ui/                       # shadcn/ui
        │   ├── chat/
        │   ├── booking/
        │   ├── itinerary/
        │   ├── provider/
        │   ├── review/
        │   └── shared/
        │
        ├── lib/
        │   ├── supabase/
        │   ├── api/
        │   ├── utils/
        │   └── validators/
        │
        ├── hooks/
        ├── types/
        ├── providers/
        └── middleware.ts
```

### Architectural Boundaries

#### Service Boundaries

| Service | Schema | Port | Responsibility |
|---------|--------|------|----------------|
| tourist-service | `tourist_db` | 8082 | User profiles, preferences, wallet |
| hotel-service | `hotel_db` | 8083 | Hotel CRUD, availability, ratings |
| tour-guide-service | `guide_db` | 8084 | Guide CRUD, skills, schedules |
| vehicle-service | `vehicle_db` | 8085 | Vehicle CRUD, availability |
| booking-service | `booking_db` | 8086 | Booking orchestration, saga |
| itinerary-service | `itinerary_db` | 8087 | Trip lifecycle, expenses |
| review-service | `review_db` | 8088 | Reviews, rating propagation |
| trip-plan-service | `tripplan_db` | 8089 | Packages, bundle pricing |
| event-service | `event_db` | 8090 | Events CRUD |
| ecommerce-service | `ecommerce_db` | 8091 | Products, orders |
| ai-agent-service | N/A | 8093 | Gemini integration, recommendations |

#### API Gateway Routing

```
/api/tourists/**     → tourist-service
/api/hotels/**       → hotel-service
/api/tour-guides/**  → tour-guide-service
/api/vehicles/**     → vehicle-service
/api/bookings/**     → booking-service
/api/itineraries/**  → itinerary-service
/api/reviews/**      → review-service
/api/packages/**     → trip-plan-service
/api/events/**       → event-service
/api/products/**     → ecommerce-service
/api/chat/**         → ai-agent-service
```

### Requirements to Structure Mapping

| Feature | Backend Service | Frontend Route | Components |
|---------|-----------------|----------------|------------|
| AI Chat | ai-agent-service | `(tourist)/chat` | `chat/*` |
| Hotel Booking | hotel-service, booking-service | `(tourist)/trips` | `booking/*` |
| Guide Booking | tour-guide-service, booking-service | `(tourist)/trips` | `booking/*` |
| Vehicle Booking | vehicle-service, booking-service | `(tourist)/trips` | `booking/*` |
| Itinerary View | itinerary-service | `(tourist)/trips/[id]` | `itinerary/*` |
| Write Reviews | review-service | `(tourist)/reviews` | `review/*` |
| Provider Dashboard | respective provider service | `(provider)/dashboard` | `provider/*` |
| Accept Bookings | booking-service | `(provider)/bookings` | `provider/*` |
| Manage Listing | respective provider service | `(provider)/listings` | `provider/*` |
| Admin Events | event-service | `(admin)/events` | Admin components |
| Admin Packages | trip-plan-service | `(admin)/packages` | Admin components |

### Integration Points

#### Internal Communication (Feign Clients)

| Caller | Target | Purpose |
|--------|--------|---------|
| ai-agent-service | hotel, guide, vehicle, review, trip-plan | Get recommendations |
| booking-service | hotel, guide, vehicle, itinerary | Create bookings |
| trip-plan-service | hotel, guide, vehicle, booking | Package availability |
| itinerary-service | booking, review | Trip lifecycle |

#### External Integrations

| Service | External System | Purpose |
|---------|-----------------|---------|
| Frontend | Supabase Auth | User authentication |
| All services | Supabase JWT | Token validation |
| All services | Supabase PostgreSQL | Data storage |
| ai-agent-service | Google Gemini API | AI recommendations |
| review-service | Amazon SQS | Rating update events |
| booking-service | Amazon SQS | Booking notifications |
| itinerary-service | Amazon SES | Email notifications |

## Architecture Validation Results

### Coherence Validation ✅

**Decision Compatibility:** All technology choices work together. Spring Boot 3.5.x, Next.js 16, Supabase, and AWS services are proven production combinations with strong community support and documentation.

**Pattern Consistency:** Implementation patterns align perfectly with the chosen technology stack. Spring Boot conventions (controller → service → repository), Next.js App Router patterns, and REST API standards are all coherent.

**Structure Alignment:** Project structure supports all architectural decisions. Service boundaries are clear, integration points are well-defined, and the monorepo layout enables coordinated development.

### Requirements Coverage Validation ✅

**Epic/Feature Coverage:**
- All 9 required services implemented with CRUD operations
- AI Agent service provides intelligent trip planning
- Inter-service communication demonstrated via OpenFeign and SQS
- Multi-actor system supported via Supabase RBAC

**Non-Functional Requirements Coverage:**
- Performance: Caffeine caching, stateless services, managed infrastructure
- Security: Supabase JWT validation, role-based authorization
- Scalability: ECS Fargate auto-scaling, schema isolation
- Maintainability: Clean boundaries, shared common-lib

### Implementation Readiness Validation ✅

**Decision Completeness:** All critical decisions documented with specific versions. No ambiguity for AI agents.

**Structure Completeness:** Full project directory structure defined with all files, services, and components.

**Pattern Completeness:** Comprehensive patterns covering naming, structure, API formats, events, and error handling.

### Architecture Completeness Checklist

**✅ Requirements Analysis**
- [x] Project context thoroughly analyzed
- [x] Scale and complexity assessed
- [x] Technical constraints identified (university, timeline, no payment)
- [x] Cross-cutting concerns mapped (auth, communication, ratings)

**✅ Architectural Decisions**
- [x] Critical decisions documented with versions
- [x] Technology stack fully specified
- [x] Integration patterns defined (OpenFeign, SQS)
- [x] Performance considerations addressed (caching, stateless)

**✅ Implementation Patterns**
- [x] Naming conventions established (DB, API, code)
- [x] Structure patterns defined (backend, frontend)
- [x] Communication patterns specified (sync, async, events)
- [x] Process patterns documented (error handling, loading states)

**✅ Project Structure**
- [x] Complete directory structure defined
- [x] Component boundaries established
- [x] Integration points mapped
- [x] Requirements to structure mapping complete

### Architecture Readiness Assessment

**Overall Status:** READY FOR IMPLEMENTATION

**Confidence Level:** HIGH

**Key Strengths:**
1. Clear service boundaries with well-defined responsibilities
2. Proven technology stack with extensive documentation
3. Comprehensive implementation patterns preventing conflicts
4. AWS-managed infrastructure reducing operational complexity
5. Supabase handling auth eliminates custom implementation

**Areas for Future Enhancement:**
1. Monitoring dashboards (CloudWatch)
2. Rate limiting (API Gateway)
3. Advanced caching (Redis if needed)
4. API versioning strategy

### Implementation Handoff

**AI Agent Guidelines:**
- Follow all architectural decisions exactly as documented
- Use implementation patterns consistently across all components
- Respect project structure and boundaries
- Refer to this document for all architectural questions
- Use common-lib for shared DTOs and exceptions

**First Implementation Steps:**
1. Create monorepo structure with parent POM and Next.js app
2. Set up Supabase project with database schemas
3. Implement common-lib with shared classes
4. Build Tourist Service as reference implementation
5. Parallel: Set up frontend with Supabase auth

