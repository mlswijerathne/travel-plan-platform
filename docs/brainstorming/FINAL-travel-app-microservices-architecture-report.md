# FINAL REPORT: Travel Plan Web Application
## Microservices Architecture - Brainstorming Session Results

**Project:** Travel Plan Web Application with AI-Powered Trip Planning
**Date:** 2026-01-30
**Facilitator:** LakshithaWijerathneB
**Methodology:** Progressive Technique Flow (4 Phases)

---

## Executive Summary

This comprehensive brainstorming session explored the complete microservices architecture for a Travel Plan Web Application - a university project requiring 9 core services with CRUD operations and inter-service communication, plus 3 additional services (AI Agent, Authentication, Email).

### Session Metrics

| Metric | Value |
|--------|-------|
| **Total Ideas Generated** | 79 |
| **Techniques Applied** | 4 (Mind Mapping, Ecosystem Thinking, SCAMPER, Decision Tree) |
| **Services Designed** | 12 |
| **API Endpoints Specified** | 50+ |
| **Decision Trees Mapped** | 6 |
| **Phase Documents Created** | 4 |

### Key Outcomes

1. **Complete Service Architecture** - All 12 services defined with clear boundaries
2. **Inter-Service Communication Patterns** - Full dependency matrix and API contracts
3. **AI Agent Integration** - Multi-agent system with internal-first fallback pattern
4. **Feedback Loop Design** - Review Service updates all provider services
5. **Implementation Specifications** - DTOs, endpoints, and decision flows ready for development

---

## Project Overview

### The Vision

A comprehensive travel planning platform for Sri Lanka tourism that combines:
- **AI-Powered Planning**: Intelligent trip recommendations via multi-agent system
- **Provider Marketplace**: Registered hotels, tour guides, and vehicles get priority
- **Package Offerings**: Pre-built curated trips for decision-fatigued tourists
- **Complete Lifecycle**: From planning to booking to review

### Target Users

| Actor | Role |
|-------|------|
| **Tourist** | Plans trips, books services, leaves reviews |
| **Hotel Owner** | Registers property, manages availability |
| **Tour Guide** | Offers services, builds reputation |
| **Vehicle Owner** | Provides transport, accepts bookings |
| **Admin** | Manages events, creates packages, monitors platform |

---

## Service Architecture

### Complete Service Map

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        TRAVEL PLAN APPLICATION                          │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                    ADDITIONAL SERVICES                           │   │
│  │  ┌───────────┐  ┌───────────────┐  ┌─────────────┐              │   │
│  │  │   AUTH    │  │   AI AGENT    │  │    EMAIL    │              │   │
│  │  │  SERVICE  │  │   SERVICE     │  │   SERVICE   │              │   │
│  │  └───────────┘  └───────────────┘  └─────────────┘              │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                                                         │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                    REQUIRED 9 SERVICES                           │   │
│  │                                                                  │   │
│  │  PROVIDER CLUSTER          TRANSACTION CLUSTER                   │   │
│  │  ┌─────────┐               ┌───────────┐                        │   │
│  │  │ HOTEL   │               │  BOOKING  │                        │   │
│  │  │ SERVICE │               │  SERVICE  │                        │   │
│  │  └─────────┘               └───────────┘                        │   │
│  │  ┌─────────┐               ┌───────────┐                        │   │
│  │  │  TOUR   │               │ TRIP PLAN │                        │   │
│  │  │  GUIDE  │               │ (PACKAGES)│                        │   │
│  │  └─────────┘               └───────────┘                        │   │
│  │  ┌─────────┐               ┌───────────┐                        │   │
│  │  │ VEHICLE │               │E-COMMERCE │                        │   │
│  │  │ SERVICE │               │  SERVICE  │                        │   │
│  │  └─────────┘               └───────────┘                        │   │
│  │                                                                  │   │
│  │  EXPERIENCE CLUSTER        ENGAGEMENT CLUSTER                    │   │
│  │  ┌───────────┐             ┌───────────┐                        │   │
│  │  │ ITINERARY │             │   EVENT   │                        │   │
│  │  │  SERVICE  │             │   MGMT    │                        │   │
│  │  └───────────┘             └───────────┘                        │   │
│  │  ┌───────────┐             ┌───────────┐                        │   │
│  │  │  REVIEW   │             │  TOURIST  │                        │   │
│  │  │  SERVICE  │             │   MGMT    │                        │   │
│  │  └───────────┘             └───────────┘                        │   │
│  │                                                                  │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### Service Descriptions

| # | Service | Cluster | Primary Responsibility |
|---|---------|---------|------------------------|
| 1 | **Hotel Service** | Provider | Manage registered hotels, availability, pricing |
| 2 | **Tour Guide Service** | Provider | Manage registered guides, skills, schedules |
| 3 | **Vehicle Service** | Provider | Manage registered vehicles, availability, rates |
| 4 | **Booking Service** | Transaction | Process all bookings, cancellations, refunds |
| 5 | **Trip Plan Service** | Transaction | Manage pre-built packages, bundle pricing |
| 6 | **E-Commerce Service** | Transaction | Souvenirs, event merchandise, airport pickup |
| 7 | **Itinerary Service** | Experience | Trip lifecycle, scheduling, review triggers |
| 8 | **Review Service** | Experience | All feedback, ratings, provider updates |
| 9 | **Event Management** | Engagement | Admin-created events, location/date matching |
| + | **AI Agent Service** | Orchestration | Multi-agent recommendations, trip planning |
| + | **Auth Service** | Security | Authentication, authorization, roles |
| + | **Email Service** | Communication | Notifications, confirmations, review requests |
| + | **Tourist Management** | Core | Profiles, preferences, wallet/credits |

---

## Key Architectural Patterns

### Pattern 1: Internal-First Fallback

```
Tourist Request: "Find hotels in Mirissa"
                    │
                    ▼
            ┌───────────────┐
            │  AI AGENT     │
            │               │
            │ 1. Query      │──► Hotel Service (Internal DB)
            │    Internal   │
            │               │    Results: 1 hotel
            │ 2. Results    │
            │    < 3?       │──► YES: Supplement from cache
            │               │
            │ 3. Normalize  │──► Unified RecommendationDTO
            │    & Merge    │
            │               │
            │ 4. Enrich     │──► Review Service (ratings)
            │               │
            └───────────────┘
                    │
                    ▼
            Return: 1 registered (bookable) + 2 external (view-only)
```

**Key Points:**
- Registered providers ALWAYS appear first
- External (Google Maps) data only supplements when internal insufficient
- External results are view-only (not bookable through platform)
- Creates strong incentive for providers to register

### Pattern 2: Symbiotic Rating Network

```
                    ┌─────────────────────┐
                    │   REVIEW SERVICE    │
                    │                     │
                    │  Tourist submits    │
                    │  review             │
                    └──────────┬──────────┘
                               │
         ┌─────────────────────┼─────────────────────┐
         │                     │                     │
         ▼                     ▼                     ▼
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│  HOTEL SERVICE  │  │  TOUR GUIDE     │  │  TRIP PLAN      │
│                 │  │  SERVICE        │  │  SERVICE        │
│ • Update rating │  │ • Update rating │  │ • Update rating │
│ • Flag if low   │  │ • Flag if low   │  │ • Adjust rank   │
└─────────────────┘  └─────────────────┘  └─────────────────┘
         │                     │                     │
         └─────────────────────┼─────────────────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │     AI AGENT        │
                    │                     │
                    │  Better data for    │
                    │  recommendations    │
                    └─────────────────────┘
```

**Key Points:**
- Reviews flow BACK to all provider services
- Providers store denormalized ratings for fast queries
- Low ratings trigger admin alerts
- AI Agent uses enriched data for smarter recommendations

### Pattern 3: Trip Lifecycle Management

```
BEFORE TRIP              DURING TRIP              AFTER TRIP
     │                        │                        │
     ▼                        ▼                        ▼
┌─────────────┐        ┌─────────────┐        ┌─────────────┐
│  AI AGENT   │        │  ITINERARY  │        │   REVIEW    │
│             │        │   SERVICE   │        │   SERVICE   │
│ • Plan trip │        │ • Track     │        │ • Collect   │
│ • Recommend │        │   schedule  │        │   feedback  │
│ • Book      │        │ • Expenses  │        │ • Update    │
│             │        │ • Issues    │        │   providers │
└─────────────┘        └─────────────┘        └─────────────┘
```

**Key Points:**
- Itinerary Service is the "Lifecycle Owner"
- Triggers review requests when trip ends
- Tracks expenses during trip
- Handles real-time issue reporting

### Pattern 4: Dependency Direction Rule

```
┌─────────────────────────────────────────────────────────────┐
│  📐 RULE: Data queries flow DOWN, Events flow UP            │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  DATA QUERIES (Synchronous):                                │
│  AI Agent ──queries──► Provider Services                    │
│  Booking ──queries──► Provider Services                     │
│  E-Commerce ──queries──► Itinerary Service                  │
│                                                             │
│  EVENT NOTIFICATIONS (Async OK):                            │
│  Review Service ──notifies──► Provider Services             │
│  Itinerary Service ──notifies──► Email Service              │
│  Booking Service ──notifies──► Itinerary Service            │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## Service Cluster Details

### Cluster 1: Provider Services

**Services:** Hotel, Tour Guide, Vehicle

**Shared Interface:**
```java
interface IProviderService {
    List<Provider> searchByLocation(String location, SearchCriteria criteria);
    Provider getById(String id);
    boolean checkAvailability(String id, DateRange dates, int quantity);
    void updateRating(String id, RatingUpdate update);
    Booking acceptBooking(BookingRequest request);
    void cancelBooking(String bookingId);
}
```

**Common Features:**
- Owner registration and management
- Availability calendar
- Pricing/rate management
- Rating integration from Review Service
- Booking acceptance/rejection

### Cluster 2: Transaction Services

**Services:** Booking, Trip Plan, E-Commerce

**Shared Patterns:**
- Cart/checkout flow
- Pricing calculation
- Cancellation policies
- Credit/refund handling
- Confirmation generation

**Key Insight:** Use shared booking engine library for DRY code

### Cluster 3: Experience Services

**Services:** AI Agent, Itinerary, Review

**Journey Mapping:**
| Phase | Service | Actions |
|-------|---------|---------|
| Before Trip | AI Agent | Recommendations, planning, booking |
| During Trip | Itinerary | Schedule, expenses, issue reporting |
| After Trip | Review | Feedback collection, provider updates |

### Cluster 4: Engagement Services

**Services:** Event Management, E-Commerce, Email

**Goal:** Drive discovery, retention, and additional revenue

**Triggers from Itinerary:**
- Arriving in city → Show local events
- Beach day planned → Suggest beach products
- Trip ending → Send review request
- Trip completed → Send "book again" promo

---

## Ideas Summary by Phase

### Phase 1: Mind Mapping (Ideas #1-46)

| Category | Ideas | Key Concepts |
|----------|-------|--------------|
| AI Agent & Provider Integration | #1-5 | Internal-first fallback, hybrid fill, data normalization |
| Review Service Integration | #6-13 | Dual ratings, batch endpoints, polymorphic entities |
| Post-Trip Review Flow | #14-18 | Trip completion detection, review shells, email triggers |
| Event Management | #19-24 | Location/date matching, preference scoring, admin-only |
| E-Commerce Integration | #25-30 | Event-linked products, trip-aware store, airport pickup |
| Trip Plan Packages | #31-37 | Pre-built packages, AI suggests packages, feedback loop |
| Error Handling | #38-46 | Saga pattern, cancellation flow, circuit breaker |

### Phase 2: Ecosystem Thinking (Ideas #47-56)

| Pattern | Ideas | Key Concepts |
|---------|-------|--------------|
| Rating Sync | #47-50 | Review → Provider push updates |
| Service Clusters | #51-54 | Provider interface, unified cart, trip context |
| Ecosystem Health | #55-56 | Admin dashboard, API direction convention |

### Phase 3: SCAMPER (Ideas #57-71)

| Lens | Ideas | Key Concepts |
|------|-------|--------------|
| Substitute | #57 | API Gateway for providers |
| Combine | #58-59 | Shared booking engine |
| Adapt | #60-61 | Surge pricing, collaborative filtering |
| Modify | #62-63 | Micro-reviews, AI memory |
| Put to Use | #64-66 | Location tracker, training insights, marketing |
| Eliminate | #67-68 | Shipping complexity, real-time API calls |
| Reverse | #69-71 | Book first plan later, location-exit reviews |

### Phase 4: Decision Tree Mapping (Ideas #72-79)

| Deliverable | Ideas | Key Concepts |
|-------------|-------|--------------|
| Decision Trees | #72-77 | 6 core flows mapped |
| API Specs | #78 | 50+ endpoints specified |
| Dependency Matrix | #79 | Complete service map |

---

## Top Implementation Recommendations

### Must Implement (High Impact, Manageable Complexity)

| Priority | Idea | Description | Impact |
|----------|------|-------------|--------|
| 1 | #1-4 | Internal-First Fallback Pattern | Core differentiator |
| 2 | #47-50 | Review → Provider Rating Sync | Feedback loop |
| 3 | #33 | Package Booking Creates Itinerary | One-click experience |
| 4 | #68 | Cache External APIs | Reliability |
| 5 | #38-39 | Saga Pattern + Pre-Check | Transaction integrity |

### Should Implement (Good Value)

| Priority | Idea | Description | Impact |
|----------|------|-------------|--------|
| 6 | #62 | Micro-Reviews During Trip | Better data quality |
| 7 | #71 | Review at Location Exit | Fresher feedback |
| 8 | #29 | Airport-Only Pickup | Simplified scope |
| 9 | #19-20 | Event Preference Matching | Enhanced experience |
| 10 | #36 | AI Suggests Packages First | Smart routing |

### Consider for Future (Advanced)

| Idea | Description | Why Later |
|------|-------------|-----------|
| #57 | API Gateway | Production optimization |
| #61 | Collaborative Filtering | Needs user volume |
| #64 | Location Tracker | Privacy considerations |
| #70 | Provider Bidding | Complex marketplace |

---

## Technical Specifications Summary

### API Endpoints by Service

| Service | Endpoints | Key Operations |
|---------|-----------|----------------|
| Auth | 4 | login, register, refresh, logout |
| Tourist Mgmt | 7 | profile, preferences, wallet, trips |
| Hotel | 6 | search, details, availability, rating, CRUD |
| Tour Guide | 6 | (same pattern as Hotel) |
| Vehicle | 6 | (same pattern as Hotel) |
| Event | 5 | search, details, CRUD (admin) |
| Booking | 5 | create, get, cancel, by-trip, availability |
| Trip Plan | 6 | search, details, availability, book, CRUD, rating |
| Itinerary | 6 | get, add-item, update-item, delete-item, status, download |
| Review | 7 | submit, get, update, batch-ratings, by-entity, pending, by-tourist |
| E-Commerce | 6 | search, get, create-order, get-order, update-status, by-tourist |
| Email | 4 | review-request, confirmation, cancellation, reminder |
| AI Agent | 5 | chat, history, recommend, recommend-packages, generate-plan |

**Total: 73 endpoints**

### Key DTOs

1. **RecommendationDTO** - Unified format for all provider recommendations
2. **BookingRequest/Response** - Standardized booking contract
3. **Review** - Polymorphic review entity (7 entity types)
4. **CancellationResponse** - Itemized refund breakdown
5. **RatingUpdateEvent** - Provider notification payload

---

## Session Documents

| Document | Purpose |
|----------|---------|
| `brainstorming-session-2026-01-30.md` | Main session file with all ideas |
| `phase2-ecosystem-analysis-service-patterns.md` | Service clusters and patterns |
| `phase3-scamper-service-refinements.md` | Innovation refinements |
| `phase4-decision-tree-implementation-specs.md` | Implementation specifications |
| `FINAL-travel-app-microservices-architecture-report.md` | This summary document |

---

## Next Steps

### Immediate Actions

1. **Review this document** with stakeholders/supervisor
2. **Prioritize features** for MVP vs future phases
3. **Create technical design** documents for each service
4. **Set up project structure** with Spring Boot microservices

### Development Phases Suggested

| Phase | Services | Timeline |
|-------|----------|----------|
| **Phase 1: Core** | Auth, Tourist Mgmt, Hotel, Booking | First |
| **Phase 2: Providers** | Tour Guide, Vehicle, Review | Second |
| **Phase 3: Intelligence** | AI Agent, Itinerary | Third |
| **Phase 4: Engagement** | Event, Trip Plan, E-Commerce, Email | Fourth |

### Success Criteria

- [ ] All 9 required services implemented with CRUD
- [ ] Inter-service communication demonstrated
- [ ] AI Agent integrates with all provider services
- [ ] Complete tourist journey (plan → book → review) works
- [ ] Package booking creates full itinerary

---

## Conclusion

This brainstorming session successfully mapped the complete architecture for a Travel Plan Web Application with:

- **79 actionable ideas** organized by implementation priority
- **12 services** with clear boundaries and responsibilities
- **4 service clusters** following established patterns
- **6 decision trees** covering all major user flows
- **50+ API endpoints** ready for implementation
- **Complete dependency matrix** preventing circular dependencies

The architecture balances university project requirements (9 services with CRUD and interconnection) with real-world patterns (feedback loops, saga transactions, graceful degradation) that demonstrate sophisticated microservices design.

---

**Session Complete**

*Generated through Progressive Technique Flow brainstorming methodology*
*Facilitated by Mary, Business Analyst Agent*
