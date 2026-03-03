# Project Report: Travel Plan Platform

**Course:** Final Project Submission
**Date:** March 2, 2026
**Team Repository:** https://github.com/mlswijerathne/travel-plan-platform

---

## Table of Contents

1. [Introduction](#1-introduction)
2. [GIT Repository Link](#2-git-repository-link)
3. [Application Architecture](#3-application-architecture)
4. [Backend / Frontend / Technology Stack Details](#4-backend--frontend--technology-stack-details)
5. [Workload Matrix – Individual User Contribution](#5-workload-matrix--individual-user-contribution)
6. [How to Run the Application](#6-how-to-run-the-application)

---

## 1. Introduction

### Project Overview

**Travel Plan Platform** is an AI-powered travel planning web application designed specifically for Sri Lanka tourism. It connects tourists with local service providers — hotels, tour guides, and vehicle owners — through a unified marketplace, and uses a conversational AI assistant to generate personalised trip itineraries.

### Problem Statement

Planning travel in Sri Lanka currently requires tourists to search across multiple disconnected websites for accommodation, guides, and transport. There is no single platform that provides an intelligent, end-to-end planning experience with real-time availability, bookings, and AI-generated itineraries tailored to the visitor's preferences.

### Solution

The platform provides:

- **AI Chat Assistant** – A Gemini-powered conversational agent that answers travel questions, recommends hotels/guides/vehicles, and generates full multi-day trip plans.
- **Provider Marketplace** – Service providers (hotels, tour guides, vehicle owners) register and manage their listings directly on the platform.
- **Booking System** – Tourists can browse and book multiple service types in a single transaction using a Saga orchestration pattern.
- **Itinerary Builder** – Automatically generates and exports a PDF itinerary after booking confirmation.
- **Review & Rating System** – Post-trip reviews drive provider ratings via Kafka-powered event pipelines.
- **E-Commerce & Events** – Travel merchandise shop and local event discovery.

### Key Features

| Feature | Description |
|---|---|
| AI Trip Planning | Chat interface with Google Gemini LLM generates personalised itineraries |
| Multi-Provider Booking | Book hotel + guide + vehicle in one transaction with saga rollback |
| Real-Time Availability | Provider availability checked synchronously via Feign clients |
| PDF Itinerary Export | Confirmed bookings generate downloadable PDF trip plans |
| Role-Based Access | 5 roles: Tourist, Hotel Owner, Tour Guide, Vehicle Owner, Admin |
| Event-Driven Architecture | Apache Kafka drives async workflows between services |
| Geospatial Search | Google Maps integration for location-based provider search |

### Scope & Constraints

- University project — no live payment gateway (wallet simulation only)
- Minimum 9 microservices as per project requirements
- Deployed locally via Docker Compose; CI pipeline targets AWS ECR

---

## 2. GIT Repository Link

**GitHub Repository:** https://github.com/mlswijerathne/travel-plan-platform

**Branch Strategy:**

| Branch | Purpose |
|---|---|
| `main` | Production-ready, protected branch |
| `dev` | Integration branch for team work |
| `feature/*` | Individual feature branches (e.g., `feature/hotel-service`, `feature/event-management-service`) |

**Pull Requests Merged:** 11 PRs (feature branches → dev → main)

**CI/CD:** GitHub Actions pipeline (`.github/workflows/backend-ci.yml`, `.github/workflows/frontend-ci.yml`) runs Maven build + tests on every push to `main`/`develop`, and builds Docker images pushed to AWS ECR on merge to `main`.

---

## 3. Application Architecture

### Architecture Pattern

The platform is built as a **Microservices Architecture** with event-driven communication. The system is composed of 13 independent Spring Boot services, a reactive API Gateway, Netflix Eureka service discovery, and a Next.js frontend.

### Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                         CLIENT LAYER                                 │
│    Next.js 16 Frontend  ·  React 19  ·  TypeScript  ·  Port 3000    │
└───────────────────────────────┬─────────────────────────────────────┘
                                │ HTTPS + JWT Bearer Token
                                ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      INFRASTRUCTURE LAYER                            │
│  ┌─────────────────────┐   ┌──────────────────────────────────────┐ │
│  │   Eureka Server     │   │           API Gateway                │ │
│  │  Service Registry   │◄──│  Spring Cloud Gateway (Reactive)     │ │
│  │      :8761          │   │  JWT Validation · CORS · lb://       │ │
│  └─────────────────────┘   │             :8060                    │ │
│                             └──────────────┬─────────────────────┘ │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │  Apache Kafka (KRaft, 7.7.0) · 4 Topics + 4 DLT · :9092   │   │
│  └─────────────────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────────────┘
                                │
           ┌────────────────────┼────────────────────┐
           ▼                    ▼                    ▼
┌─────────────────┐  ┌──────────────────┐  ┌─────────────────────────┐
│  CORE DOMAIN    │  │  ORCHESTRATION   │  │   SECONDARY SERVICES    │
│                 │  │                  │  │                         │
│ Tourist :8082   │  │ Booking  :8086   │  │ Trip Plan    :8089      │
│ Hotel   :8083   │  │ Itinerary:8087   │  │ Event        :8090      │
│ Guide   :8084   │  │ Review   :8088   │  │ E-Commerce   :8091      │
│ Vehicle :8085   │  │                  │  │                         │
└────────┬────────┘  └────────┬─────────┘  └────────────────────────┘
         │                    │
         └─────────┬──────────┘
                   ▼
┌─────────────────────────────────────────────────────────────────────┐
│                          AI LAYER                                    │
│         AI Agent Service  ·  Google Gemini  ·  :8093                │
│         LangChain4j  ·  Google ADK 0.5.0  ·  Google Maps API        │
└─────────────────────────────────────────────────────────────────────┘
                   │
┌─────────────────────────────────────────────────────────────────────┐
│                       DATABASE LAYER                                 │
│  Neon PostgreSQL              │  Supabase PostgreSQL                 │
│  tourist_db, hotel_db         │  vehicle, itinerary                  │
│  guide_db, booking_db         │  tripplan, event, ecommerce          │
│  review_db                    │                                      │
└─────────────────────────────────────────────────────────────────────┘

EXTERNAL SERVICES:
  Supabase Auth  (JWT + OAuth · 5 Roles)
  Google Gemini  (LLM API)
  Google Maps    (Places, Directions)
```

### Microservice Registry

| # | Service | Port | Database | Responsibility |
|---|---|---|---|---|
| 1 | **Discovery Server** | 8761 | — | Netflix Eureka service registry |
| 2 | **API Gateway** | 8060 | — | JWT validation, CORS, load-balanced routing |
| 3 | **Tourist Service** | 8082 | Neon (tourist_db) | Tourist profiles, preferences, wallet |
| 4 | **Hotel Service** | 8083 | Neon (hotel_db) | Hotel CRUD, rooms, availability, search |
| 5 | **Tour Guide Service** | 8084 | Neon (guide_db) | Guide profiles, skills, schedules, geospatial |
| 6 | **Vehicle Service** | 8085 | Supabase (vehicle) | Vehicle management & availability |
| 7 | **Booking Service** | 8086 | Neon (booking_db) | Multi-provider booking, Saga orchestration, refund engine |
| 8 | **Itinerary Service** | 8087 | Supabase (itinerary) | Trip planning, day/activity CRUD, PDF export |
| 9 | **Review Service** | 8088 | Neon (review_db) | Reviews, star ratings, provider responses |
| 10 | **Trip Plan Service** | 8089 | Supabase (tripplan) | Trip templates & curated packages |
| 11 | **Event Service** | 8090 | Supabase (event) | Travel events, ticket tiers, registrations, geospatial |
| 12 | **E-Commerce Service** | 8091 | Supabase (ecommerce) | Travel merchandise products & orders |
| 13 | **AI Agent Service** | 8093 | — | AI chat (SSE), recommendations, trip plan generation |

### Inter-Service Communication

#### Synchronous — OpenFeign Clients

| From | To | Purpose |
|---|---|---|
| Booking Service | Hotel, Guide, Vehicle | Availability check + saga confirm/cancel |
| AI Agent Service | Hotel, Guide, Vehicle, Review, Trip Plan | Search & recommendation queries |

#### Asynchronous — Apache Kafka Event Flows

```
Booking Service   ──[booking-events]──────────► Itinerary Service
                  ──[booking-notifications]──►  (Future: Notification Service)

Itinerary Service ──[trip-completion-events]──► Review Service

Review Service    ──[rating-update-events]────► Hotel Service
                                           ───► Tour Guide Service
```

Each topic has **3 partitions** and a corresponding `.DLT` (Dead Letter Topic) for failed message handling.

### Booking Saga Pattern

The Booking Service implements the **Saga Orchestrator pattern** to ensure consistency across hotel, guide, and vehicle reservations:

```
Start ──► Reserve Hotel ──► Reserve Guide ──► Reserve Vehicle
              │                   │                 │
          [Failure]           [Failure]         [Failure]
              ▼                   ▼                 ▼
        Cancel All          Cancel Hotel       Cancel Hotel
                            Cancel Guide       Cancel Guide
                                               (Rollback)
```

### Authentication & Authorization

Authentication is handled externally by **Supabase Auth** (JWT + OAuth). The API Gateway validates JWT tokens on every request using a custom `JwtValidationFilter`.

| Role | Capabilities |
|---|---|
| `TOURIST` | Browse, book, write reviews, AI chat, manage profile & wallet |
| `HOTEL_OWNER` | Manage hotels/rooms, view bookings, respond to reviews |
| `TOUR_GUIDE` | Manage guide profile, availability, respond to reviews |
| `VEHICLE_OWNER` | Manage vehicles, view bookings |
| `ADMIN` | Full platform access |

---

## 4. Backend / Frontend / Technology Stack Details

### Technology Stack Summary

| Layer | Technology | Version |
|---|---|---|
| **Frontend Framework** | Next.js | 16.1.6 |
| **UI Library** | React | 19.2.4 |
| **Language (Frontend)** | TypeScript | 5.9.3 |
| **Styling** | Tailwind CSS | 4.x |
| **State/Data Fetching** | TanStack React Query | 5.60.0 |
| **UI Components** | Radix UI + shadcn/ui | — |
| **Forms** | React Hook Form + Zod | 7.54 / 3.24 |
| **Maps (Frontend)** | Leaflet + React Leaflet | 1.9 / 4.2 |
| **Backend Language** | Java | 21 (LTS) |
| **Backend Framework** | Spring Boot | 3.5.0 |
| **Cloud Framework** | Spring Cloud | 2025.0.0 |
| **Service Discovery** | Netflix Eureka | (via Spring Cloud) |
| **API Gateway** | Spring Cloud Gateway (Reactive) | — |
| **Messaging** | Apache Kafka (KRaft mode) | 7.7.0 |
| **HTTP Clients** | OpenFeign | — |
| **ORM** | Spring Data JPA / Hibernate | — |
| **Mapping** | MapStruct | 1.6.3 |
| **Auth Provider** | Supabase Auth (JWT) | — |
| **Databases** | PostgreSQL (Neon + Supabase) | — |
| **AI / LLM** | Google Gemini API | — |
| **AI Orchestration** | Google ADK | 0.5.0 |
| **AI Tooling** | LangChain4j | — |
| **Containerisation** | Docker + Docker Compose | — |
| **CI/CD** | GitHub Actions → AWS ECR | — |
| **Build Tool (Backend)** | Apache Maven | 3.x |
| **Package Manager (FE)** | pnpm | — |
| **API Docs** | SpringDoc OpenAPI | 2.8.0 |

---

### Backend Details

#### Project Structure

```
backend/
├── pom.xml                    ← Parent POM (Spring Boot 3.5.0, Java 21)
├── common-lib/                ← Shared: ApiResponse, JwtFilter, SecurityConfig
├── discovery-server/          ← Netflix Eureka registry (:8761)
├── api-gateway/               ← Spring Cloud Gateway (:8060)
├── tourist-service/           ← Tourist domain (:8082)
├── hotel-service/             ← Hotel domain (:8083)
├── tour-guide-service/        ← Guide domain (:8084)
├── vehicle-service/           ← Vehicle domain (:8085)
├── booking-service/           ← Booking + Saga (:8086)
├── itinerary-service/         ← Itinerary + PDF (:8087)
├── review-service/            ← Reviews + Ratings (:8088)
├── trip-plan-service/         ← Trip packages (:8089)
├── event-service/             ← Events + Tickets (:8090)
├── ecommerce-service/         ← Shop (:8091)
├── ai-agent-service/          ← AI Chat + Recommendations (:8093)
├── docker-compose.yml         ← Full stack compose file
└── docker-compose-infra.yml   ← Infrastructure only (Kafka, Eureka)
```

#### common-lib

Shared library included in all services. Provides:
- `ApiResponse<T>` — standard response wrapper
- `JwtValidationFilter` — JWT parsing and role extraction
- `SecurityConfig` — Spring Security base configuration with public/protected route rules
- `OpenApiConfig` — Swagger/OpenAPI configuration

#### Key Service Implementations

**Booking Service (Saga Orchestrator)**
- Implements the Choreography+Orchestration hybrid Saga pattern
- Calls Hotel, Guide, and Vehicle services via Feign for availability and reservation
- On any failure, triggers compensating transactions (cancellations) across all reserved services
- Publishes `booking-events` to Kafka upon successful booking creation

**AI Agent Service**
- Built on **Google ADK 0.5.0** with multi-agent routing
- Agents: `HotelSearchAgent`, `GuideSearchAgent`, `VehicleSearchAgent`, `TripPlanAgent`, `ReviewAgent`
- Uses `AgentFactory` to construct agent pipelines
- Streams responses via **Server-Sent Events (SSE)** to the frontend chat interface
- Integrates Google Maps API for geospatial queries (nearby attractions, directions)
- Calls backend services via Feign for real-time platform data

**Itinerary Service**
- Consumes `booking-events` from Kafka to auto-create itinerary skeletons
- Supports Day and Activity CRUD for manual trip customisation
- Publishes `trip-completion-events` via a scheduled job that detects completed trips
- Generates **PDF itinerary exports** using a PDF generation library

**Event Service**
- Full geospatial support using PostGIS extensions for location-based event queries
- Supports ticket tiers with dynamic pricing
- Event registration with quota management
- Exposes public and authenticated endpoints

**Hotel Service**
- Full hotel CRUD with room type management
- Availability checking with date-range queries
- Consumes `rating-update-events` from Kafka to update aggregated star ratings

---

### Frontend Details

#### Project Structure

```
frontend/
├── package.json               ← Next.js 16, React 19, TypeScript
├── src/
│   ├── app/                   ← Next.js App Router (route groups)
│   │   ├── (public)/          ← Unauthenticated routes
│   │   │   ├── login/         ← Login page
│   │   │   └── register/      ← Registration page
│   │   ├── (tourist)/         ← Protected: Tourist routes
│   │   │   ├── bookings/      ← Booking management
│   │   │   ├── chat/          ← AI Assistant chat interface
│   │   │   ├── guides/        ← Browse tour guides
│   │   │   ├── hotels/        ← Browse hotels
│   │   │   ├── profile/       ← Tourist profile
│   │   │   ├── reviews/       ← View/submit reviews
│   │   │   └── wallet/        ← Wallet management
│   │   ├── (provider)/        ← Protected: Provider dashboard
│   │   ├── admin/             ← Admin panel
│   │   ├── packages/          ← Trip packages browsing
│   │   ├── shop/              ← E-commerce shop
│   │   └── vehicles/          ← Vehicle listings
│   ├── components/            ← UI component library
│   │   ├── bookings/          ← BookingCard, CancelDialog
│   │   ├── chat/              ← ChatMessage, AgentStatusBar, TripPlanDialog
│   │   ├── guides/            ← GuideCard, GuideFilters
│   │   ├── hotels/            ← HotelCard, HotelFilters, RoomCard
│   │   ├── provider/          ← Provider dashboard components
│   │   ├── reviews/           ← ReviewCard, ReviewFormDialog
│   │   ├── shared/            ← AuthNav, Pagination, StarRating, EmptyState
│   │   └── ui/                ← Radix/shadcn primitives
│   ├── lib/api/               ← API client modules (per service)
│   ├── lib/supabase/          ← Supabase client/server helpers
│   ├── hooks/                 ← TanStack Query custom hooks
│   ├── providers/             ← React context providers
│   └── types/                 ← TypeScript type definitions
```

#### Key Frontend Features

**AI Chat Interface** (`/chat`)
- Real-time SSE streaming connection to AI Agent Service
- Renders Markdown responses using `react-markdown`
- Shows `AgentStatusBar` with active agent name during multi-agent handoffs
- `TripPlanDialog` allows saving AI-generated trip plans directly to the platform
- `QuickReplies` provide suggested follow-up questions

**Booking Flow**
- Browse → Select → Book modal with date picker and availability check
- Multi-item booking (hotel + guide + vehicle in one request)
- `CancelBookingDialog` with refund confirmation

**Provider Dashboard** (`/(provider)`)
- Hotel form with room management (`RoomManagement.tsx`)
- Response to review capability (`ResponseDialog.tsx`)
- Provider statistics card (`StatCard.tsx`)

**Authentication**
- Supabase SSR integration (`@supabase/ssr`)
- Auth callback route handler (`/auth/callback`)
- Role-based route protection via Next.js middleware

#### Frontend Routes

| Route | Access | Purpose |
|---|---|---|
| `/` | Public | Landing page |
| `/login`, `/register` | Public | Supabase auth flows |
| `/hotels`, `/guides` | Public | Browse providers |
| `/packages` | Public | View trip packages |
| `/shop` | Public | E-commerce shop |
| `/chat` | Tourist | AI travel assistant |
| `/bookings` | Tourist | Booking management |
| `/profile` | Tourist | Profile & preferences |
| `/wallet` | Tourist | Wallet top-up & history |
| `/reviews` | Tourist | Submit and view reviews |
| `/(provider)` | Provider | Provider dashboard |
| `/admin` | Admin | Admin panel |

---

## 5. Workload Matrix – Individual User Contribution

> Commit data sourced from `git log --all` as of March 2, 2026.
> Total contributors: 9 | Total commits: 86

| # | Contributor | GitHub Handle | Commits | Primary Contribution |
|---|---|---|---|---|
| 1 | **Lakshitha Wijerathne** | mlswijerathne | 28 | Project lead & architect. Set up the entire platform skeleton: Tourist Service, API Gateway, Discovery Server (Eureka), Booking Service (Saga orchestration), AI Agent Service, Kafka configuration, multi-role authentication, Supabase auth integration, security hardening, Docker/CI setup, frontend core |
| 2 | **Janith Rathnayake** | jmbrathnayake | 31 | Event Service — full backend implementation including event CRUD, event registration, ticket tier management, geospatial endpoint, route pricing, DB migrations, and DTOs |
| 3 | **Pemidu Herath** | PemiduHerath | 18 | Itinerary Service — day/activity management, booking event listener (Kafka consumer), PDF export, map integration, service connections |
| 4 | **Nipuna Nirodha** | Nipuna7 | 3 | Tour Guide Service — controllers, service layer, repository, and domain models |
| 5 | **Yeshani Eranaga** | (yeshani Eranaga) | 2 | Backend service implementation support |
| 6 | **Buddhi Vichakkshana** | (buddhi vichakkshana) | 1 | Vehicle Service — full implementation including frontend vehicle components |
| 7 | **Janitha Chamoth** | janitha | 1 | E-Commerce Service — full-stack implementation (product listing, orders) |
| 8 | **Sulakshana Wijekoon** | sulakshana | 1 | Trip Plan Service — full-stack implementation (trip templates & packages) |
| 9 | **Jayani Adikari** | jayaniadikari19 | 1 | Review Service — folder structure and initial setup |

### Service Ownership Summary

| Service | Primary Owner |
|---|---|
| API Gateway | Lakshitha Wijerathne |
| Discovery Server (Eureka) | Lakshitha Wijerathne |
| Tourist Service | Lakshitha Wijerathne |
| AI Agent Service | Lakshitha Wijerathne |
| Booking Service | Lakshitha Wijerathne |
| Frontend (core + auth + chat) | Lakshitha Wijerathne |
| Hotel Service | Lakshitha Wijerathne |
| Tour Guide Service | Nipuna Nirodha |
| Vehicle Service | Buddhi Vichakkshana |
| Itinerary Service | Pemidu Herath |
| Review Service | Jayani Adikari / Yeshani Eranaga |
| Trip Plan Service | Sulakshana Wijekoon |
| Event Service | Janith Rathnayake |
| E-Commerce Service | Janitha Chamoth |

---

## 6. How to Run the Application

### Prerequisites

Ensure the following are installed on your machine:

| Tool | Version | Purpose |
|---|---|---|
| Java JDK | 21 (LTS) | Backend services |
| Apache Maven | 3.9+ | Backend build tool |
| Docker Desktop | Latest | Kafka, infrastructure containers |
| Node.js | 20+ (LTS) | Frontend runtime |
| pnpm | Latest | Frontend package manager |
| Git | — | Clone repository |

### Environment Setup

#### 1. Clone the Repository

```bash
git clone https://github.com/mlswijerathne/travel-plan-platform.git
cd travel-plan-platform
```

#### 2. Configure Backend Environment Variables

Copy the example environment file and fill in your credentials:

```bash
cd backend
cp .env.example .env
```

Edit `backend/.env` with:

```env
# Supabase (Auth + Database)
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_JWT_SECRET=your-jwt-secret

# Neon PostgreSQL Databases
TOURIST_DB_URL=jdbc:postgresql://...
TOURIST_DB_USERNAME=...
TOURIST_DB_PASSWORD=...

HOTEL_DB_URL=jdbc:postgresql://...
HOTEL_DB_USERNAME=...
HOTEL_DB_PASSWORD=...

TOUR_GUIDE_DB_URL=jdbc:postgresql://...
TOUR_GUIDE_DB_USERNAME=...
TOUR_GUIDE_DB_PASSWORD=...

BOOKING_DB_URL=jdbc:postgresql://...
BOOKING_DB_USERNAME=...
BOOKING_DB_PASSWORD=...

REVIEW_DB_URL=jdbc:postgresql://...
REVIEW_DB_USERNAME=...
REVIEW_DB_PASSWORD=...

# Supabase PostgreSQL Databases
VEHICLE_DB_URL=jdbc:postgresql://...
VEHICLE_DB_USERNAME=...
VEHICLE_DB_PASSWORD=...

ITINERARY_DB_URL=jdbc:postgresql://...
ITINERARY_DB_USERNAME=...
ITINERARY_DB_PASSWORD=...

TRIP_PLAN_DB_URL=jdbc:postgresql://...
TRIP_PLAN_DB_USERNAME=...
TRIP_PLAN_DB_PASSWORD=...

EVENT_DB_URL=jdbc:postgresql://...
EVENT_DB_USERNAME=...
EVENT_DB_PASSWORD=...

ECOMMERCE_DB_URL=jdbc:postgresql://...
ECOMMERCE_DB_USERNAME=...
ECOMMERCE_DB_PASSWORD=...

# AI API Keys
GOOGLE_AI_API_KEY=your-gemini-api-key
GOOGLE_MAPS_API_KEY=your-maps-api-key

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:3000
```

#### 3. Configure Frontend Environment Variables

```bash
cd ../frontend
cp .env.example .env.local
```

Edit `frontend/.env.local`:

```env
NEXT_PUBLIC_SUPABASE_URL=https://your-project.supabase.co
NEXT_PUBLIC_SUPABASE_ANON_KEY=your-anon-key
NEXT_PUBLIC_API_URL=http://localhost:8060
```

---

### Option A: Run with Docker Compose (Recommended)

This runs all services — infrastructure, backend microservices, and prerequisites — in containers.

```bash
cd backend

# Step 1: Start infrastructure (Kafka, Eureka, API Gateway)
docker compose up kafka kafka-init kafka-ui discovery-server api-gateway -d

# Step 2: Wait for Kafka and Eureka to be healthy (30-60 seconds)

# Step 3: Start core domain services
docker compose up tourist-service hotel-service tour-guide-service vehicle-service -d

# Step 4: Start orchestration services
docker compose up booking-service itinerary-service review-service -d

# Step 5: Start secondary and AI services
docker compose up trip-plan-service event-service ecommerce-service ai-agent-service -d
```

Then start the frontend:

```bash
cd ../frontend
pnpm install
pnpm dev
```

**Service URLs:**

| Service | URL |
|---|---|
| Frontend | http://localhost:3000 |
| API Gateway | http://localhost:8060 |
| Eureka Dashboard | http://localhost:8761 |
| Kafka UI | http://localhost:8080 |
| Tourist Service | http://localhost:8082 |
| Hotel Service | http://localhost:8083 |
| Tour Guide Service | http://localhost:8084 |
| Vehicle Service | http://localhost:8085 |
| Booking Service | http://localhost:8086 |
| Itinerary Service | http://localhost:8087 |
| Review Service | http://localhost:8088 |
| Trip Plan Service | http://localhost:8089 |
| Event Service | http://localhost:8090 |
| E-Commerce Service | http://localhost:8091 |
| AI Agent Service | http://localhost:8093 |

---

### Option B: Run Backend Services Locally (Maven)

Use the provided startup scripts for local development:

```bash
cd backend

# Start infrastructure via Docker
docker compose up kafka kafka-init kafka-ui -d

# Build all services
mvn clean package -DskipTests

# Start all services using the startup script
bash start-local.sh

# To stop all services
bash stop-all.sh

# To view logs
bash view-logs.sh
```

Individual service startup:

```bash
# Start a single service
cd tourist-service
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

---

### Option C: Run Frontend Only (Against Deployed Backend)

If the backend is already deployed or running in Docker:

```bash
cd frontend
pnpm install
pnpm dev
```

Access the app at: **http://localhost:3000**

---

### Health Checks

Verify services are running:

```bash
# Check Eureka (all registered services)
curl http://localhost:8761/actuator/health

# Check API Gateway
curl http://localhost:8060/actuator/health

# Check a specific service
curl http://localhost:8082/actuator/health   # Tourist Service
curl http://localhost:8083/actuator/health   # Hotel Service
curl http://localhost:8093/actuator/health   # AI Agent Service

# Or run the included health check script
cd backend
bash health-check.sh
```

---

### API Documentation (Swagger)

Each service exposes Swagger UI at `/swagger-ui.html` when running in dev profile:

| Service | Swagger URL |
|---|---|
| Hotel Service | http://localhost:8083/swagger-ui.html |
| Tour Guide Service | http://localhost:8084/swagger-ui.html |
| Booking Service | http://localhost:8086/swagger-ui.html |
| Review Service | http://localhost:8088/swagger-ui.html |
| Event Service | http://localhost:8090/swagger-ui.html |

---

### Common Issues & Troubleshooting

| Issue | Solution |
|---|---|
| Services not registering in Eureka | Wait 30–60 seconds for Eureka to fully start; check `EUREKA_URL` env variable |
| Kafka connection refused | Run `docker compose up kafka kafka-init -d` and wait for health check to pass |
| JWT validation failing | Verify `SUPABASE_JWT_SECRET` matches the Supabase project settings |
| Database connection error | Verify DB credentials in `.env`; ensure Neon/Supabase DB is accessible |
| Frontend API 404 | Confirm `NEXT_PUBLIC_API_URL=http://localhost:8060` and gateway is running |
| AI agent not responding | Check `GOOGLE_AI_API_KEY` and `GOOGLE_MAPS_API_KEY` are valid in ai-agent-service `.env` |

---

*End of Project Report*
