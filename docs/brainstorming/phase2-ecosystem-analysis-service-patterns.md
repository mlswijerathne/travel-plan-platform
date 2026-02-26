# Phase 2: Ecosystem Analysis & Service Patterns

**Project:** Travel Plan Web Application - Microservices Architecture
**Date:** 2026-01-30
**Technique:** Ecosystem Thinking
**Purpose:** Identify service clusters, symbiotic relationships, and data flow patterns

---

## Executive Summary

This document captures the ecosystem analysis of 12 microservices for the Travel Plan Web Application. Using biomimetic thinking, we identified natural service clusters, the "food chain" of data flow, and critical patterns that define how services interact.

**Key Findings:**
- AI Agent Service acts as the "Apex Predator" - consuming from all producers
- Review Service creates a critical feedback loop that improves the entire ecosystem
- Four distinct service clusters emerge: Provider, Transaction, Experience, Engagement
- Clear dependency direction rule: Data flows DOWN, Events flow UP

---

## 1. Service Ecosystem Roles

### 1.1 PRODUCERS (Create Core Value)

These services CREATE the raw data that others consume:

```
┌─────────────────────────────────────────────────────────────┐
│  🌱 PRODUCER SERVICES - Generate Primary Data               │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌───────────┐  ┌───────────┐  ┌───────────┐               │
│  │  HOTEL    │  │   TOUR    │  │  VEHICLE  │               │
│  │  SERVICE  │  │   GUIDE   │  │  SERVICE  │               │
│  │           │  │  SERVICE  │  │           │               │
│  │ Produces: │  │ Produces: │  │ Produces: │               │
│  │ • Rooms   │  │ • Guides  │  │ • Cars    │               │
│  │ • Rates   │  │ • Skills  │  │ • Rates   │               │
│  │ • Avail.  │  │ • Avail.  │  │ • Avail.  │               │
│  └───────────┘  └───────────┘  └───────────┘               │
│                                                             │
│  ┌───────────┐  ┌───────────┐                              │
│  │  EVENT    │  │ TOURIST   │                              │
│  │  MGMT     │  │   MGMT    │                              │
│  │           │  │           │                              │
│  │ Produces: │  │ Produces: │                              │
│  │ • Events  │  │ • Profiles│                              │
│  │ • Tickets │  │ • Prefs   │                              │
│  └───────────┘  └───────────┘                              │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

**Characteristics:**
- Own their domain data completely
- Expose CRUD operations for their entities
- Receive rating updates from Review Service
- Are queried by AI Agent for recommendations

---

### 1.2 PRIMARY CONSUMER (The Apex Predator)

```
┌─────────────────────────────────────────────────────────────┐
│  🦁 PRIMARY CONSUMER - The Apex Predator                    │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│              ┌─────────────────────┐                        │
│              │     AI AGENT        │                        │
│              │     SERVICE         │                        │
│              │                     │                        │
│              │  Consumes:          │                        │
│              │  • ALL Producers    │                        │
│              │  • External APIs    │                        │
│              │  • Review ratings   │                        │
│              │                     │                        │
│              │  Produces:          │                        │
│              │  • Recommendations  │                        │
│              │  • Trip Plans       │                        │
│              └─────────────────────┘                        │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

**Characteristics:**
- Queries ALL producer services
- Integrates with external APIs (Google Maps, Weather, etc.)
- Normalizes data into unified recommendation format
- Stateless - does not own transaction data
- Multi-agent architecture internally

---

### 1.3 SECONDARY CONSUMERS (Process Transactions)

```
┌─────────────────────────────────────────────────────────────┐
│  🦊 SECONDARY CONSUMERS - Process Transactions              │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌───────────────┐      ┌───────────────┐                  │
│  │   BOOKING     │      │  TRIP PLAN    │                  │
│  │   SERVICE     │      │  (PACKAGES)   │                  │
│  │               │      │               │                  │
│  │ Consumes:     │      │ Consumes:     │                  │
│  │ • Provider    │      │ • Provider    │                  │
│  │   availability│      │   data        │                  │
│  │ • Tourist     │      │ • Pre-built   │                  │
│  │   selections  │      │   templates   │                  │
│  │               │      │               │                  │
│  │ Produces:     │      │ Produces:     │                  │
│  │ • Bookings    │      │ • Packages    │                  │
│  │ • Confirm.    │      │ • Bundle deals│                  │
│  └───────────────┘      └───────────────┘                  │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

**Characteristics:**
- Handle financial commitments
- Implement cancellation policies
- Coordinate with multiple providers
- Generate confirmations

---

### 1.4 DECOMPOSERS (Process Outcomes)

```
┌─────────────────────────────────────────────────────────────┐
│  🍄 DECOMPOSERS - Process Trip Outcomes                     │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌───────────────┐      ┌───────────────┐                  │
│  │  ITINERARY    │      │   REVIEW      │                  │
│  │   SERVICE     │      │   SERVICE     │                  │
│  │               │      │               │                  │
│  │ Consumes:     │      │ Consumes:     │                  │
│  │ • Bookings    │      │ • Completed   │                  │
│  │ • Events      │      │   trips       │                  │
│  │ • Schedule    │      │ • Tourist     │                  │
│  │   data        │      │   feedback    │                  │
│  │               │      │               │                  │
│  │ Produces:     │      │ Produces:     │                  │
│  │ • Trip        │      │ • Ratings     │ ──► FEEDS BACK  │
│  │   timeline    │      │ • Reviews     │     TO AI AGENT │
│  │ • Triggers    │      │ • Reliability │     & PRODUCERS │
│  └───────────────┘      └───────────────┘                  │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

**Characteristics:**
- Process completed transactions
- Generate insights and feedback
- Trigger downstream actions
- Close the feedback loop

---

### 1.5 SYMBIONTS (Support Services)

```
┌─────────────────────────────────────────────────────────────┐
│  🐝 SYMBIONTS - Enable Other Services                       │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌───────────┐  ┌───────────┐  ┌───────────┐               │
│  │   AUTH    │  │   EMAIL   │  │E-COMMERCE │               │
│  │  SERVICE  │  │  SERVICE  │  │  SERVICE  │               │
│  │           │  │           │  │           │               │
│  │ Enables:  │  │ Enables:  │  │ Enables:  │               │
│  │ • ALL     │  │ • Notifi- │  │ • Trip    │               │
│  │   service │  │   cations │  │   enhance-│               │
│  │   access  │  │ • Review  │  │   ment    │               │
│  │ • Role-   │  │   requests│  │ • Event   │               │
│  │   based   │  │ • Confirm-│  │   merch   │               │
│  │   actions │  │   ations  │  │           │               │
│  └───────────┘  └───────────┘  └───────────┘               │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

**Characteristics:**
- Don't own core business data
- Enable and enhance other services
- Cross-cutting concerns (security, communication, commerce)

---

## 2. The Data Food Chain

```
                         TOURIST (The Sun - Energy Source)
                                    │
                                    ▼
    ┌──────────────────────────────────────────────────────┐
    │                    PRODUCERS                          │
    │   Hotel │ Tour Guide │ Vehicle │ Event │ Tourist Mgmt │
    └──────────────────────┬───────────────────────────────┘
                           │
                           ▼
    ┌──────────────────────────────────────────────────────┐
    │              PRIMARY CONSUMER                         │
    │                  AI AGENT                             │
    │         (Aggregates, Transforms, Recommends)          │
    └──────────────────────┬───────────────────────────────┘
                           │
              ┌────────────┴────────────┐
              ▼                         ▼
    ┌─────────────────┐      ┌─────────────────┐
    │    BOOKING      │      │   TRIP PLAN     │
    │    SERVICE      │      │   (PACKAGES)    │
    └────────┬────────┘      └────────┬────────┘
             │                        │
             └───────────┬────────────┘
                         ▼
    ┌──────────────────────────────────────────────────────┐
    │                   ITINERARY                           │
    │            (Trip Lifecycle Owner)                     │
    └──────────────────────┬───────────────────────────────┘
                           │
                           ▼
    ┌──────────────────────────────────────────────────────┐
    │                    REVIEW                             │
    │            (Feedback Generator)                       │
    └──────────────────────┬───────────────────────────────┘
                           │
                           ▼
              ┌────────────┴────────────┐
              │    FEEDBACK LOOP        │
              │  (Back to Producers     │
              │   & AI Agent)           │
              └─────────────────────────┘
```

---

## 3. The Symbiotic Rating Network

### 3.1 Review Flow Architecture

```
                    ┌─────────────────────┐
                    │   REVIEW SERVICE    │
                    │                     │
                    │  New review saved   │
                    └──────────┬──────────┘
                               │
         ┌─────────────────────┼─────────────────────┐
         │                     │                     │
         ▼                     ▼                     ▼
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│  HOTEL SERVICE  │  │  TOUR GUIDE     │  │  TRIP PLAN      │
│                 │  │  SERVICE        │  │  SERVICE        │
│ Update:         │  │                 │  │                 │
│ • avgRating     │  │ Update:         │  │ Update:         │
│ • reviewCount   │  │ • avgRating     │  │ • packageRating │
│ • lastReviewAt  │  │ • reliability   │  │ • popularity    │
│                 │  │ • flagIfLow     │  │ • ranking       │
└────────┬────────┘  └────────┬────────┘  └────────┬────────┘
         │                    │                     │
         └────────────────────┼─────────────────────┘
                              │
                              ▼
                    ┌─────────────────────┐
                    │     AI AGENT        │
                    │                     │
                    │  Queries enriched   │
                    │  provider data      │
                    │  (ratings included) │
                    └─────────────────────┘
```

### 3.2 Feedback Loop Benefits

```
┌─────────────────────────────────────────────────────────────┐
│            THE SYMBIOTIC RATING NETWORK                     │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│   TOURIST completes trip                                    │
│        │                                                    │
│        ▼                                                    │
│   REVIEW SERVICE collects feedback                          │
│        │                                                    │
│        ├──► PROVIDERS get better/worse visibility          │
│        │    (incentive to improve service)                  │
│        │                                                    │
│        ├──► AI AGENT makes smarter recommendations          │
│        │    (tourists get better trips)                     │
│        │                                                    │
│        ├──► PACKAGES get ranked by satisfaction             │
│        │    (best experiences rise to top)                  │
│        │                                                    │
│        └──► TOURIST sees they made an impact                │
│             (encourages more reviews)                       │
│                                                             │
│   RESULT: Everyone benefits from the feedback loop!         │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 4. Service Clusters

### 4.1 Provider Cluster (The Supply Side)

```
┌─────────────────────────────────────────────────────────────┐
│  📦 PROVIDER CLUSTER                                        │
│     Services that manage SUPPLY                             │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────┐   ┌─────────┐   ┌─────────┐                   │
│  │ HOTEL   │   │  TOUR   │   │ VEHICLE │                   │
│  │ SERVICE │   │  GUIDE  │   │ SERVICE │                   │
│  └────┬────┘   └────┬────┘   └────┬────┘                   │
│       │             │             │                         │
│       └─────────────┼─────────────┘                         │
│                     │                                       │
│              SHARED PATTERNS:                               │
│              • Owner registration                           │
│              • Availability management                      │
│              • Pricing/rates                                │
│              • Rating integration                           │
│              • Booking acceptance                           │
│                                                             │
│  RECOMMENDED: Implement common IProviderService interface   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

**Common Interface Methods:**
- `checkAvailability(location, dates, quantity)`
- `getByLocation(location, radius)`
- `updateRating(entityId, newRating, reviewCount)`
- `acceptBooking(bookingRequest)`
- `cancelBooking(bookingId)`

---

### 4.2 Transaction Cluster (The Commerce Side)

```
┌─────────────────────────────────────────────────────────────┐
│  💰 TRANSACTION CLUSTER                                     │
│     Services that handle MONEY & COMMITMENTS                │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌───────────┐   ┌───────────┐   ┌───────────┐             │
│  │ BOOKING   │   │ TRIP PLAN │   │E-COMMERCE │             │
│  │ SERVICE   │   │ (PACKAGES)│   │  SERVICE  │             │
│  └─────┬─────┘   └─────┬─────┘   └─────┬─────┘             │
│        │               │               │                    │
│        └───────────────┼───────────────┘                    │
│                        │                                    │
│              SHARED PATTERNS:                               │
│              • Cart/checkout flow                           │
│              • Pricing calculation                          │
│              • Cancellation policies                        │
│              • Credit/refund handling                       │
│              • Confirmation generation                      │
│                                                             │
│  RECOMMENDED: Unified cart experience across services       │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

### 4.3 Experience Cluster (The Journey Side)

```
┌─────────────────────────────────────────────────────────────┐
│  🗺️ EXPERIENCE CLUSTER                                      │
│     Services that manage the TOURIST JOURNEY                │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌───────────┐   ┌───────────┐   ┌───────────┐             │
│  │ AI AGENT  │   │ ITINERARY │   │  REVIEW   │             │
│  │ SERVICE   │   │  SERVICE  │   │  SERVICE  │             │
│  └─────┬─────┘   └─────┬─────┘   └─────┬─────┘             │
│        │               │               │                    │
│   BEFORE TRIP     DURING TRIP     AFTER TRIP               │
│        │               │               │                    │
│        └───────────────┼───────────────┘                    │
│                        │                                    │
│              SHARED CONTEXT:                                │
│              • Tourist preferences                          │
│              • Trip timeline                                │
│              • Location awareness                           │
│              • Experience quality                           │
│                                                             │
│  RECOMMENDED: Share TripContext object across services      │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

**TripContext Object:**
```json
{
  "tripId": "trip_789",
  "touristId": "tourist_maria",
  "preferences": ["wildlife", "beaches", "nature"],
  "dates": { "start": "2026-02-10", "end": "2026-02-17" },
  "locations": ["Colombo", "Yala", "Mirissa", "Galle"],
  "bookings": ["booking_001", "booking_002", "..."],
  "currentPhase": "DURING_TRIP"
}
```

---

### 4.4 Engagement Cluster (The Marketing Side)

```
┌─────────────────────────────────────────────────────────────┐
│  📣 ENGAGEMENT CLUSTER                                      │
│     Services that drive DISCOVERY & RETENTION               │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌───────────┐   ┌───────────┐   ┌───────────┐             │
│  │   EVENT   │   │ E-COMMERCE│   │   EMAIL   │             │
│  │   MGMT    │   │  SERVICE  │   │  SERVICE  │             │
│  └─────┬─────┘   └─────┬─────┘   └─────┬─────┘             │
│        │               │               │                    │
│        └───────────────┼───────────────┘                    │
│                        │                                    │
│              SHARED GOAL:                                   │
│              • Increase engagement                          │
│              • Drive additional revenue                     │
│              • Keep tourists connected                      │
│              • Promote discoveries                          │
│                                                             │
│  RECOMMENDED: Itinerary triggers engagement opportunities   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

**Engagement Triggers:**
| Trigger Event | Service Response |
|---------------|------------------|
| Tourist arrives in city | Event Service checks local events |
| Tourist has beach day | E-Commerce suggests beach products |
| Trip ending soon | Email sends review request |
| Trip completed | Email sends "book again" promo |

---

## 5. Dependency Direction Rule

```
┌─────────────────────────────────────────────────────────────┐
│  📐 DEPENDENCY RULE: Data flows DOWN, Events flow UP        │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  DATA QUERIES (Synchronous REST):                           │
│  ─────────────────────────────────────────────────────────  │
│  AI Agent ──queries──► Provider Services                    │
│  Booking ──queries──► Provider Services                     │
│  E-Commerce ──queries──► Itinerary Service                  │
│  Trip Plan ──queries──► Provider Services                   │
│                                                             │
│  Direction: Higher-level services query lower-level         │
│                                                             │
│  ═══════════════════════════════════════════════════════════│
│                                                             │
│  EVENT NOTIFICATIONS (Can be async):                        │
│  ─────────────────────────────────────────────────────────  │
│  Review Service ──notifies──► Provider Services             │
│  Review Service ──notifies──► Trip Plan Service             │
│  Itinerary Service ──notifies──► Email Service              │
│  Booking Service ──notifies──► Itinerary Service            │
│  Itinerary Service ──notifies──► Review Service             │
│                                                             │
│  Direction: Services notify others of state changes         │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

**Benefits of this pattern:**
- No circular dependencies
- Services can be tested in isolation
- Clear ownership boundaries
- Predictable data flow

---

## 6. Ideas Generated in Phase 2

| ID | Idea | Description |
|----|------|-------------|
| 47 | Review → Hotel Service Rating Sync | Push aggregated ratings for fast queries |
| 48 | Review → Tour Guide Reliability Update | Auto-flag underperforming guides |
| 49 | Review → Trip Plan Package Ranking | Self-optimizing package marketplace |
| 50 | Vehicle Service Review Updates | Consistent pattern across all providers |
| 51 | Abstract Provider Service Interface | Common contract for all provider types |
| 52 | Unified Cart Across Transaction Cluster | Single checkout for all purchasables |
| 53 | Trip Context Object | Shared context across Experience Cluster |
| 54 | Engagement Triggers from Itinerary | Location/timing-aware marketing |
| 55 | Ecosystem Dashboard for Admin | Health metrics across all clusters |
| 56 | Clear API Direction Convention | Query DOWN, Notify UP |

---

## 7. Pattern Summary Table

| Pattern | Description | Services Involved |
|---------|-------------|-------------------|
| **Apex Predator** | AI Agent consumes from all producers | AI → All Providers |
| **Symbiotic Rating Network** | Reviews improve all services | Review → Hotel, Guide, Vehicle, TripPlan |
| **Provider Cluster** | Common interface for supply | Hotel, TourGuide, Vehicle |
| **Transaction Cluster** | Unified commerce patterns | Booking, TripPlan, E-Commerce |
| **Experience Cluster** | Journey lifecycle management | AI, Itinerary, Review |
| **Engagement Cluster** | Marketing & retention | Event, E-Commerce, Email |
| **Feedback Loop** | Reviews flow back to improve recommendations | Review → AI → Providers |
| **Lifecycle Owner** | Itinerary owns trip state | Itinerary triggers all post-booking flows |
| **Dependency Direction** | Query DOWN, Notify UP | All services |

---

## 8. Architecture Recommendations

### 8.1 Communication Patterns

| Pattern | Use Case | Implementation |
|---------|----------|----------------|
| Synchronous REST | Data queries | AI Agent → Hotel Service |
| Async Notification | State changes | Review Service → All Providers |
| Scheduled Jobs | Time-based triggers | Itinerary → Email (review requests) |

### 8.2 Data Ownership

| Service | Owns | Caches/Denormalizes |
|---------|------|---------------------|
| Hotel Service | Hotel data | Rating from Review Service |
| Tour Guide Service | Guide data | Rating from Review Service |
| Vehicle Service | Vehicle data | Rating from Review Service |
| Booking Service | Booking transactions | Provider availability snapshots |
| Itinerary Service | Trip timeline | Booking details |
| Review Service | All reviews & ratings | Nothing - source of truth |
| Trip Plan Service | Package definitions | Provider details, ratings |

### 8.3 Scalability Considerations

- **Provider Cluster:** Can scale independently based on registration volume
- **AI Agent:** Stateless, horizontally scalable
- **Booking Service:** May need distributed transactions (Saga pattern)
- **Review Service:** Write-heavy, consider event sourcing

---

## Document Info

**Generated:** 2026-01-30
**Session:** Brainstorming - Microservices Integration Architecture
**Phase:** 2 of 4 (Pattern Recognition)
**Next Phase:** SCAMPER (Idea Development)
