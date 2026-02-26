---
stepsCompleted: [1, 2, 3]
inputDocuments:
  - "docs/project_idea.md"
  - "_bmad-output/planning-artifacts/architecture.md"
  - "_bmad-output/planning-artifacts/ux-design-specification.md"
  - "_bmad-output/brainstorming/FINAL-travel-app-microservices-architecture-report.md"
project_name: "Travel Plan Web Application"
date: "2026-01-31"
---

# Travel Plan Web Application - Epic Breakdown

## Overview

This document provides the complete epic and story breakdown for the Travel Plan Web Application, decomposing the requirements from the project specification, Architecture decisions, and UX Design into implementable stories.

## Requirements Inventory

### Functional Requirements

**Tourist Management (FR1-FR5)**
- FR1: Tourists can register an account with email, nationality, preferences, and budget level
- FR2: Tourists can log in and manage their profile information
- FR3: Tourists can set travel preferences (interests, budget, language)
- FR4: Tourists can view their trip history
- FR5: Tourists can manage wallet/credits for refunds

**AI Trip Planning (FR6-FR10)**
- FR6: Tourists can chat with AI assistant using natural language to plan trips
- FR7: AI agent generates personalized itineraries based on preferences, dates, and budget
- FR8: AI agent prioritizes registered providers (internal-first pattern)
- FR9: AI agent supplements with external data when internal results < 3
- FR10: AI provides day-by-day itinerary with cost breakdown

**Hotel Service (FR11-FR16)**
- FR11: Hotel owners can register and create hotel listings
- FR12: Hotel owners can manage room types and pricing
- FR13: Hotel owners can set availability calendars
- FR14: Hotels display ratings from reviews
- FR15: Hotels can accept/decline booking requests
- FR16: System searches hotels by location and dates

**Tour Guide Service (FR17-FR22)**
- FR17: Tour guides can register and create profiles
- FR18: Tour guides can list skills and languages spoken
- FR19: Tour guides can set availability schedules
- FR20: Tour guides display ratings from reviews
- FR21: Tour guides can accept/decline booking requests
- FR22: System searches guides by location and specialization

**Vehicle Service (FR23-FR28)**
- FR23: Vehicle owners can register and list vehicles
- FR24: Vehicle owners can specify vehicle type and capacity
- FR25: Vehicle owners can set availability and rates
- FR26: Vehicles display ratings from reviews
- FR27: Vehicle owners can accept/decline booking requests
- FR28: System searches vehicles by location and dates

**Booking Service (FR29-FR34)**
- FR29: Tourists can book hotels, guides, and vehicles
- FR30: System supports multi-provider booking in single transaction
- FR31: Tourists can view booking details and status
- FR32: Tourists can cancel bookings with refund policy
- FR33: Providers receive booking notifications
- FR34: System pre-checks availability before booking

**Itinerary Service (FR35-FR40)**
- FR35: System creates itinerary from confirmed bookings
- FR36: Tourists can view day-by-day trip schedule
- FR37: Tourists can track expenses during trip
- FR38: Tourists can add custom items to itinerary
- FR39: Tourists can download offline itinerary (PDF)
- FR40: System triggers review requests after trip completion

**Review Service (FR41-FR45)**
- FR41: Tourists can submit reviews for hotels, guides, and vehicles
- FR42: Tourists can rate providers (1-5 stars) with text and photos
- FR43: Reviews update provider aggregate ratings
- FR44: Tourists can view pending reviews
- FR45: Providers can respond to reviews

**Trip Plan/Packages Service (FR46-FR50)**
- FR46: Admins can create pre-built trip packages
- FR47: Packages bundle hotels, guides, vehicles, and activities
- FR48: Tourists can browse packages by theme/duration/budget
- FR49: Tourists can book entire package in one action
- FR50: Package booking creates complete itinerary

**Event Management Service (FR51-FR54) - Simplified**
- FR51: Admins can create and manage events
- FR52: Events have location, date, and description
- FR53: Tourists can browse events
- FR54: Events display in relevant location searches

**E-Commerce Service (FR55-FR58) - Simplified**
- FR55: Admins can manage product catalog
- FR56: Products have name, price, and description
- FR57: Tourists can browse products
- FR58: Tourists can create orders

### Non-Functional Requirements

- NFR1: AI chat responses should stream progressively for real-time feel
- NFR2: All API responses should return within 2 seconds under normal load
- NFR3: System must support 5 user roles with role-based access control
- NFR4: All services must validate JWT tokens from Supabase
- NFR5: Database must use schema-per-service isolation
- NFR6: System must handle concurrent booking requests without double-booking
- NFR7: Frontend must be responsive (mobile, tablet, desktop)
- NFR8: System must support offline itinerary access via PDF download
- NFR9: All API endpoints must follow consistent error response format
- NFR10: System must log all requests with correlation IDs

### Additional Requirements

**From Architecture Document:**
- Starter Template: Spring Boot 3.5.x (backend) + Next.js 16 (frontend)
- Database: Supabase PostgreSQL with schema-per-service
- Authentication: Supabase Auth with JWT validation
- AI Integration: Google Gemini API
- Sync Communication: Spring Cloud OpenFeign
- Async Communication: Amazon SQS
- Deployment: AWS ECS Fargate
- Service Discovery: AWS Cloud Map
- Email: Amazon SES

**From UX Design Document:**
- Chat-first tourist experience with streaming responses
- Mobile-first provider dashboard
- Quick reply chips for AI chat
- Photo carousels for provider listings
- Map-based itinerary visualization
- One-tap accept/decline for provider bookings
- Celebration animations for booking completion

### FR Coverage Map

| FR Range | Epic | Coverage |
|----------|------|----------|
| FR1-FR5 | Epic 1: Foundation & User Authentication | ✅ Complete |
| FR6-FR10 | Epic 3: AI Trip Planning & Recommendations | ✅ Complete |
| FR11-FR16 | Epic 2: Provider Marketplace - Hotels | ✅ Complete |
| FR17-FR22 | Epic 2: Provider Marketplace - Tour Guides | ✅ Complete |
| FR23-FR28 | Epic 2: Provider Marketplace - Vehicles | ✅ Complete |
| FR29-FR34 | Epic 4: Booking & Reservations | ✅ Complete |
| FR35-FR40 | Epic 5: Itinerary & Trip Lifecycle | ✅ Complete |
| FR41-FR45 | Epic 6: Reviews & Ratings | ✅ Complete |
| FR46-FR50 | Epic 7: Trip Packages | ✅ Complete |
| FR51-FR54 | Epic 8: Events & E-Commerce MVP | ✅ Complete |
| FR55-FR58 | Epic 8: Events & E-Commerce MVP | ✅ Complete |

**Coverage Summary:** 58/58 FRs mapped (100%)

## Epic List

### Epic 1: Foundation & User Authentication
**Priority:** Critical (Must be first)
**FRs Covered:** FR1-FR5
**Services:** Tourist Service, Frontend Foundation
**Dependencies:** None (enables all other epics)

**Description:**
Establishes the core infrastructure for the application including Supabase authentication integration, Tourist Service with user registration/profile management, and foundational frontend setup with Next.js 16, Tailwind CSS, and shadcn/ui components.

**Key Deliverables:**
- Supabase Auth integration with JWT validation
- Tourist registration with email, nationality, preferences, budget level
- Profile management and preferences updates
- Wallet/credits system for refunds
- Trip history viewing
- Frontend authentication flow and protected routes
- Base UI component library setup

---

### Epic 2: Provider Marketplace - Listings
**Priority:** High
**FRs Covered:** FR11-FR28
**Services:** Hotel Service, Tour Guide Service, Vehicle Service
**Dependencies:** Epic 1 (authentication)

**Description:**
Implements the three provider services that form the core marketplace: hotels, tour guides, and vehicles. Each provider type can register, create listings, manage availability, set pricing, and accept/decline booking requests.

**Key Deliverables:**
- Hotel owner registration and listing management (FR11-FR16)
- Room type and pricing configuration
- Tour guide registration and profile creation (FR17-FR22)
- Skills, languages, and availability management
- Vehicle owner registration and listing (FR23-FR28)
- Vehicle type, capacity, and rate configuration
- Location-based search for all provider types
- Mobile-first provider dashboard
- Photo upload and carousel display
- One-tap accept/decline for booking requests

---

### Epic 3: AI Trip Planning & Recommendations
**Priority:** High
**FRs Covered:** FR6-FR10
**Services:** AI Agent Service
**Dependencies:** Epic 1, Epic 2 (providers to recommend)

**Description:**
Implements the AI-powered trip planning experience using Google Gemini API. Tourists can chat naturally with the AI assistant to plan trips, receiving personalized itineraries based on preferences, dates, and budget.

**Key Deliverables:**
- Chat interface with streaming responses (NFR1)
- Natural language trip planning conversations
- Internal-first provider prioritization pattern
- External data fallback when internal results < 3
- Personalized itinerary generation
- Day-by-day schedule with cost breakdown
- Quick reply chips for common actions
- Function calling for provider searches

---

### Epic 4: Booking & Reservations
**Priority:** High
**FRs Covered:** FR29-FR34
**Services:** Booking Service
**Dependencies:** Epic 1, Epic 2

**Description:**
Implements the booking workflow enabling tourists to reserve hotels, tour guides, and vehicles. Supports multi-provider bookings in a single transaction using the saga pattern for consistency.

**Key Deliverables:**
- Single and multi-provider booking flow
- Pre-availability validation before booking
- Booking status tracking (pending, confirmed, cancelled)
- Cancellation with refund policy enforcement
- Provider notification via SQS events
- Booking details and history viewing
- Celebration animations on successful booking
- Double-booking prevention (NFR6)

---

### Epic 5: Itinerary & Trip Lifecycle
**Priority:** Medium
**FRs Covered:** FR35-FR40
**Services:** Itinerary Service
**Dependencies:** Epic 4 (confirmed bookings)

**Description:**
Manages the complete trip lifecycle from booking confirmation through trip completion. Creates itineraries from bookings, enables expense tracking, and triggers post-trip review requests.

**Key Deliverables:**
- Automatic itinerary creation from confirmed bookings
- Day-by-day trip schedule view
- Map-based itinerary visualization
- Expense tracking during trips
- Custom item additions to itinerary
- PDF download for offline access (NFR8)
- Trip completion detection
- Review request triggering via SQS

---

### Epic 6: Reviews & Ratings
**Priority:** Medium
**FRs Covered:** FR41-FR45
**Services:** Review Service
**Dependencies:** Epic 5 (trip completion)

**Description:**
Implements the review system allowing tourists to rate and review providers after trip completion. Reviews update aggregate provider ratings and providers can respond to feedback.

**Key Deliverables:**
- 1-5 star rating with text reviews
- Photo uploads with reviews
- Pending review queue for tourists
- Aggregate rating calculation and propagation
- Provider response capability
- Review display on provider listings
- Rating update events via SQS

---

### Epic 7: Trip Packages
**Priority:** Medium
**FRs Covered:** FR46-FR50
**Services:** Trip Plan Service
**Dependencies:** Epic 2, Epic 4

**Description:**
Enables admins to create pre-built trip packages that bundle hotels, guides, vehicles, and activities. Tourists can browse and book entire packages in one action.

**Key Deliverables:**
- Admin package creation interface
- Provider bundling (hotels, guides, vehicles)
- Package browsing by theme/duration/budget
- One-click package booking
- Automatic itinerary generation from package
- Package pricing and availability management

---

### Epic 8: Events & E-Commerce MVP
**Priority:** Low (Simplified scope)
**FRs Covered:** FR51-FR58
**Services:** Event Service, E-Commerce Service
**Dependencies:** Epic 1

**Description:**
Simplified implementations of event management and e-commerce features. Events are admin-managed and displayed in location searches. E-commerce provides basic product catalog and ordering.

**Key Deliverables:**
- Admin event creation and management (FR51-FR54)
- Event location, date, description fields
- Event browsing and location-based display
- Admin product catalog management (FR55-FR58)
- Product name, price, description
- Product browsing interface
- Basic order creation

---

## Epic 1: Foundation & User Authentication

**Epic Goal:** Establish the core infrastructure for the application including Supabase authentication integration, Tourist Service with user registration/profile management, and foundational frontend setup.

### Story 1.1: Tourist Service Foundation & Registration API

As a **new visitor**,
I want to **register an account with my email, nationality, preferences, and budget level**,
So that **I can access personalized trip planning features**.

**Acceptance Criteria:**

**Given** a visitor is not registered
**When** they submit registration with email, password, full name, nationality, and budget level
**Then** a new tourist account is created in Supabase Auth
**And** a tourist profile record is created in the tourist schema
**And** a JWT token is returned for authentication
**And** the response includes the tourist profile data

**Given** a visitor submits registration with an existing email
**When** the system processes the request
**Then** a 409 Conflict error is returned with message "Email already registered"

**Given** a visitor submits incomplete registration data
**When** required fields (email, password, fullName) are missing
**Then** a 400 Bad Request error is returned with validation details

**Technical Notes:**
- Creates `tourist` schema with `tourists` table
- Integrates with Supabase Auth for user creation
- JWT validation middleware setup
- Budget levels: BUDGET, MODERATE, LUXURY

---

### Story 1.2: Frontend Foundation & Authentication UI

As a **tourist**,
I want to **sign up and sign in through a clean interface**,
So that **I can securely access my account**.

**Acceptance Criteria:**

**Given** a visitor on the landing page
**When** they click "Sign Up"
**Then** a registration form displays with fields for email, password, full name, nationality, and budget level
**And** the form validates input in real-time using Zod

**Given** a visitor completes the registration form
**When** they submit valid data
**Then** they are registered via the Tourist Service API
**And** redirected to their dashboard
**And** a success toast notification appears

**Given** a registered tourist on the landing page
**When** they enter valid credentials and click "Sign In"
**Then** they are authenticated via Supabase Auth
**And** redirected to their dashboard

**Given** a tourist is authenticated
**When** they click "Sign Out"
**Then** the session is cleared
**And** they are redirected to the landing page

**Technical Notes:**
- Next.js 16 App Router setup
- Tailwind CSS + shadcn/ui components
- Supabase Auth client integration
- Protected route middleware
- Responsive design (mobile-first)

---

### Story 1.3: Tourist Profile Management

As a **registered tourist**,
I want to **view and update my profile information**,
So that **my account details stay current**.

**Acceptance Criteria:**

**Given** an authenticated tourist
**When** they navigate to their profile page
**Then** their current profile information is displayed (name, email, nationality, budget level)

**Given** an authenticated tourist viewing their profile
**When** they edit their name, nationality, or budget level and save
**Then** the profile is updated in the database
**And** a success message confirms the update

**Given** an authenticated tourist
**When** they attempt to change their email
**Then** they must verify the new email address via Supabase Auth flow

**Given** a request to update profile without valid JWT
**When** the API receives the request
**Then** a 401 Unauthorized error is returned

**Technical Notes:**
- Profile API endpoints (GET /api/tourists/me, PUT /api/tourists/me)
- Frontend profile page with edit mode
- Form validation with React Hook Form + Zod

---

### Story 1.4: Travel Preferences Configuration

As a **tourist**,
I want to **set my travel preferences including interests, preferred language, and detailed budget settings**,
So that **AI recommendations match my personal tastes**.

**Acceptance Criteria:**

**Given** an authenticated tourist on preferences page
**When** the page loads
**Then** their current preferences are displayed (or defaults if not set)

**Given** an authenticated tourist
**When** they select interests from categories (Adventure, Culture, Nature, Food, Relaxation, Nightlife)
**Then** multiple interests can be selected
**And** selections are saved to their profile

**Given** an authenticated tourist
**When** they set their preferred language (English, Spanish, French, German, Chinese, Japanese)
**Then** the language preference is saved
**And** affects AI chat responses

**Given** an authenticated tourist
**When** they set daily budget range (min/max in USD)
**Then** the budget range is saved
**And** used for trip planning recommendations

**Technical Notes:**
- Adds `preferences` JSONB column to tourists table
- Preferences API endpoint (PUT /api/tourists/me/preferences)
- Multi-select UI components for interests
- Currency selector for budget

---

### Story 1.5: Wallet/Credits System

As a **tourist**,
I want to **view my wallet balance and credit history**,
So that **I can track refunds and use credits for future bookings**.

**Acceptance Criteria:**

**Given** an authenticated tourist
**When** they navigate to wallet page
**Then** their current credit balance is displayed in USD

**Given** a tourist with credit transactions
**When** they view wallet history
**Then** all transactions are listed with date, amount, type (REFUND, USED), and description
**And** transactions are sorted by date (newest first)

**Given** a booking is cancelled with refund
**When** the refund is processed (via event from Booking Service - future epic)
**Then** credits are added to the tourist's wallet
**And** a transaction record is created

**Given** a tourist has zero credits
**When** they view their wallet
**Then** the balance shows $0.00
**And** a message indicates "No credits yet"

**Technical Notes:**
- Creates `wallet_transactions` table in tourist schema
- Wallet API endpoints (GET /api/tourists/me/wallet)
- SQS consumer for refund events (listener ready, actual events come in Epic 4)
- Transaction types: REFUND, USED, ADJUSTMENT

---

### Story 1.6: Trip History Viewing

As a **tourist**,
I want to **view my past trips and their details**,
So that **I can remember my travel experiences**.

**Acceptance Criteria:**

**Given** an authenticated tourist with past trips
**When** they navigate to trip history
**Then** a list of completed trips is displayed
**And** each trip shows destination, dates, and status

**Given** a tourist viewing trip history
**When** they click on a specific trip
**Then** trip details expand showing itinerary summary
**And** links to associated bookings (if available)

**Given** a tourist with no past trips
**When** they view trip history
**Then** an empty state message appears: "No trips yet. Start planning your adventure!"
**And** a CTA button links to the AI chat

**Given** the trip history endpoint is called
**When** the tourist has trips in various statuses
**Then** only COMPLETED trips appear in history
**And** ACTIVE or CANCELLED trips are excluded

**Technical Notes:**
- Trip history API (GET /api/tourists/me/trips)
- Reads from Itinerary Service via OpenFeign (service ready, data comes in Epic 5)
- Frontend trip cards with expandable details
- Empty state with call-to-action

---

## Epic 2: Provider Marketplace - Listings

**Epic Goal:** Implement the three provider services (Hotels, Tour Guides, Vehicles) that form the core marketplace, enabling providers to register, create listings, manage availability, and be discovered by tourists.

### Story 2.1: Hotel Service Foundation & Registration

As a **hotel owner**,
I want to **register my hotel with basic information and photos**,
So that **tourists can discover my property**.

**Acceptance Criteria:**

**Given** an authenticated user with HOTEL_OWNER role
**When** they submit hotel registration with name, description, location (address, city, country, coordinates), star rating, and amenities
**Then** a new hotel record is created in the hotel schema
**And** the hotel is linked to the owner's user ID
**And** the hotel ID is returned in the response

**Given** a hotel owner registering a hotel
**When** they upload photos (up to 10 images)
**Then** photos are stored and associated with the hotel
**And** the first photo is set as the primary/cover image

**Given** a hotel owner
**When** they have already registered a hotel
**Then** they can register additional hotels (multi-property support)

**Given** incomplete hotel registration data
**When** required fields (name, city, country) are missing
**Then** a 400 Bad Request error is returned with validation details

**Technical Notes:**
- Creates `hotel` schema with `hotels`, `hotel_photos` tables
- Hotel Service Spring Boot application setup
- JWT validation for HOTEL_OWNER role
- Photo upload to S3 or Supabase Storage
- Location stored with lat/lng for geo-queries

---

### Story 2.2: Hotel Room Management & Pricing

As a **hotel owner**,
I want to **define room types with descriptions and pricing**,
So that **tourists know what accommodations I offer**.

**Acceptance Criteria:**

**Given** a hotel owner with a registered hotel
**When** they add a room type with name, description, capacity, bed configuration, and base price per night
**Then** the room type is created and linked to the hotel
**And** the room type appears in hotel details

**Given** a hotel owner viewing their rooms
**When** they edit an existing room type's price or description
**Then** the changes are saved
**And** reflected in search results

**Given** a hotel owner
**When** they add multiple room types (Standard, Deluxe, Suite, etc.)
**Then** each room type is stored separately
**And** can have different pricing

**Given** a hotel owner
**When** they delete a room type with no active bookings
**Then** the room type is removed
**And** no longer appears in searches

**Technical Notes:**
- Creates `room_types` table in hotel schema
- Room type API endpoints (CRUD)
- Price stored as cents (integer) to avoid floating point issues
- Bed configurations: SINGLE, DOUBLE, TWIN, KING, QUEEN

---

### Story 2.3: Hotel Availability Calendar

As a **hotel owner**,
I want to **set room availability and seasonal pricing**,
So that **tourists can only book when rooms are available**.

**Acceptance Criteria:**

**Given** a hotel owner with room types defined
**When** they set availability for a date range
**Then** the number of available rooms per type is stored for each date

**Given** a hotel owner viewing availability calendar
**When** they see a monthly view
**Then** each day shows room counts and any price overrides
**And** booked rooms are subtracted from available count

**Given** a hotel owner
**When** they set a price override for specific dates (holidays, peak season)
**Then** the override price applies instead of base price for those dates

**Given** a hotel owner
**When** they block dates (maintenance, private use)
**Then** those dates show zero availability
**And** cannot be booked

**Technical Notes:**
- Creates `room_availability` table (room_type_id, date, available_count, price_override)
- Calendar API endpoints (GET/PUT for date ranges)
- Efficient date range queries with indexes
- Default availability falls back to room type total count

---

### Story 2.4: Hotel Search API

As a **tourist**,
I want to **search for hotels by location and dates**,
So that **I can find available accommodations**.

**Acceptance Criteria:**

**Given** a tourist searching for hotels
**When** they provide city/location and check-in/check-out dates
**Then** hotels with available rooms for all dates are returned
**And** results include hotel name, rating, price range, primary photo

**Given** a search request
**When** filters are applied (star rating, amenities, price range)
**Then** only matching hotels are returned

**Given** search results
**When** displayed
**Then** hotels are sorted by relevance (rating * availability)
**And** pagination is supported (20 per page default)

**Given** a location search
**When** coordinates are provided
**Then** hotels within 50km radius are included
**And** distance is calculated and returned

**Given** no hotels match the criteria
**When** results are returned
**Then** an empty array is returned with count: 0
**And** no error is thrown

**Technical Notes:**
- Search endpoint: GET /api/hotels/search
- PostGIS or Haversine formula for distance calculation
- Aggregates availability across date range
- Returns summary pricing (min room price for dates)
- Caches popular searches with Caffeine (5 min TTL)

---

### Story 2.5: Tour Guide Service Foundation & Registration

As a **tour guide**,
I want to **create my professional profile**,
So that **tourists can hire me for guided experiences**.

**Acceptance Criteria:**

**Given** an authenticated user with TOUR_GUIDE role
**When** they submit profile with name, bio, location (city, country, service areas), hourly rate, and profile photo
**Then** a guide profile is created in the tour_guide schema
**And** the profile is linked to the user ID

**Given** a guide creating their profile
**When** they upload a profile photo and gallery images (up to 8)
**Then** images are stored and associated with the profile

**Given** a guide
**When** they set their service areas (multiple cities/regions)
**Then** they appear in searches for any of those areas

**Given** incomplete guide registration
**When** required fields (name, city, hourlyRate) are missing
**Then** a 400 Bad Request error is returned

**Technical Notes:**
- Creates `tour_guide` schema with `guides`, `guide_photos`, `service_areas` tables
- Tour Guide Service Spring Boot application setup
- Hourly rate stored as cents (integer)
- Service areas enable multi-location guides

---

### Story 2.6: Tour Guide Skills & Availability

As a **tour guide**,
I want to **list my skills, languages, and available times**,
So that **tourists can find guides matching their needs**.

**Acceptance Criteria:**

**Given** a registered guide
**When** they add languages spoken with proficiency level (NATIVE, FLUENT, CONVERSATIONAL)
**Then** languages are saved to their profile
**And** searchable by tourists

**Given** a registered guide
**When** they add specializations (History, Adventure, Food, Art, Nature, Photography)
**Then** specializations are saved
**And** multiple can be selected

**Given** a guide setting availability
**When** they define weekly recurring schedule (Mon 9-17, Tue 10-18, etc.)
**Then** the schedule is saved as their default availability

**Given** a guide
**When** they block specific dates or add extra available slots
**Then** overrides are applied to the recurring schedule

**Given** a guide viewing their calendar
**When** they have bookings
**Then** booked slots appear as unavailable

**Technical Notes:**
- Creates `guide_languages`, `guide_specializations`, `guide_availability` tables
- Weekly schedule stored as JSONB for flexibility
- Date-specific overrides in separate table
- Languages: ISO 639-1 codes

---

### Story 2.7: Tour Guide Search API

As a **tourist**,
I want to **search for tour guides by location, date, and specialization**,
So that **I can find the right guide for my trip**.

**Acceptance Criteria:**

**Given** a tourist searching for guides
**When** they provide location and optional date/time
**Then** available guides in that area are returned
**And** results include name, photo, rating, hourly rate, languages

**Given** a search with filters
**When** specialization or language filters are applied
**Then** only matching guides are returned

**Given** a date/time in the search
**When** checking availability
**Then** only guides available at that time are included

**Given** search results
**When** displayed
**Then** guides are sorted by rating (highest first)
**And** pagination supported (20 per page)

**Technical Notes:**
- Search endpoint: GET /api/guides/search
- Joins availability and filters by requested time
- Service area matching for location
- Returns aggregate rating from reviews (default 0 if no reviews)

---

### Story 2.8: Vehicle Service Foundation & Registration

As a **vehicle owner**,
I want to **register my vehicle for rental**,
So that **tourists can book transportation**.

**Acceptance Criteria:**

**Given** an authenticated user with VEHICLE_OWNER role
**When** they submit vehicle registration with type (CAR, VAN, SUV, BUS, MOTORCYCLE), make, model, year, capacity, and location
**Then** a vehicle record is created in the vehicle schema
**And** linked to the owner's user ID

**Given** a vehicle owner registering
**When** they upload photos (up to 6 images)
**Then** photos are stored with the vehicle
**And** first image set as primary

**Given** a vehicle owner
**When** they add features (AC, GPS, WiFi, Child Seat, Luggage Rack)
**Then** features are saved as searchable attributes

**Given** a vehicle owner
**When** they register multiple vehicles
**Then** each is stored separately under their account

**Technical Notes:**
- Creates `vehicle` schema with `vehicles`, `vehicle_photos`, `vehicle_features` tables
- Vehicle Service Spring Boot application setup
- Vehicle types enum: CAR, VAN, SUV, BUS, MOTORCYCLE, TUK_TUK
- Capacity as integer (passenger count)

---

### Story 2.9: Vehicle Configuration & Rates

As a **vehicle owner**,
I want to **set rental rates and availability**,
So that **tourists know pricing and can book available dates**.

**Acceptance Criteria:**

**Given** a registered vehicle
**When** owner sets daily rate and optional hourly rate
**Then** rates are saved to the vehicle record

**Given** a vehicle owner
**When** they set availability calendar (available/unavailable dates)
**Then** only available dates can be booked

**Given** a vehicle owner
**When** they set seasonal/peak pricing for date ranges
**Then** override prices apply for those dates

**Given** a vehicle with bookings
**When** viewing availability
**Then** booked dates show as unavailable

**Given** a vehicle owner
**When** they set minimum rental duration (e.g., 4 hours, 1 day)
**Then** bookings below minimum are rejected

**Technical Notes:**
- Creates `vehicle_availability`, `vehicle_rates` tables
- Daily and hourly rates stored as cents
- Minimum duration stored in hours
- Calendar similar to hotel availability pattern

---

### Story 2.10: Vehicle Search API

As a **tourist**,
I want to **search for vehicles by location, dates, and type**,
So that **I can find transportation for my trip**.

**Acceptance Criteria:**

**Given** a tourist searching for vehicles
**When** they provide location and pickup/return dates
**Then** available vehicles are returned
**And** results include type, make/model, capacity, daily rate, photo

**Given** search with filters
**When** vehicle type or capacity filters applied
**Then** only matching vehicles returned

**Given** a date range search
**When** checking availability
**Then** only vehicles available for entire range are included

**Given** search results
**When** displayed
**Then** sorted by price (lowest first)
**And** pagination supported

**Technical Notes:**
- Search endpoint: GET /api/vehicles/search
- Date range availability check
- Location-based filtering (city or coordinates)
- Returns calculated total price for date range

---

### Story 2.11: Provider Dashboard - Listings Management

As a **provider (hotel owner, guide, or vehicle owner)**,
I want to **manage my listings through a unified dashboard**,
So that **I can efficiently run my business**.

**Acceptance Criteria:**

**Given** an authenticated provider
**When** they access the dashboard
**Then** they see their listings with key metrics (views, bookings, rating)

**Given** a provider on the dashboard
**When** they click on a listing
**Then** they can edit all details (description, photos, pricing)

**Given** a provider
**When** they view the calendar tab
**Then** they see availability across all their listings
**And** can quickly update availability

**Given** a provider on mobile
**When** they access the dashboard
**Then** the interface is optimized for touch (large tap targets, swipe gestures)

**Given** a provider with multiple listings
**When** they view the dashboard
**Then** listings are displayed as cards with status indicators

**Technical Notes:**
- Unified provider dashboard route (/dashboard)
- Role-based content (detects HOTEL_OWNER, TOUR_GUIDE, VEHICLE_OWNER)
- Mobile-first responsive design
- Photo carousel component with upload/reorder
- TanStack Query for data fetching with optimistic updates

---

### Story 2.12: Provider Dashboard - Booking Requests

As a **provider**,
I want to **view and respond to booking requests quickly**,
So that **I don't miss business opportunities**.

**Acceptance Criteria:**

**Given** a provider with pending booking requests
**When** they view the requests tab
**Then** pending requests are listed with tourist name, dates, and amount

**Given** a provider viewing a booking request
**When** they tap "Accept"
**Then** the booking status changes to CONFIRMED
**And** a confirmation notification is sent to the tourist

**Given** a provider viewing a booking request
**When** they tap "Decline" and provide a reason
**Then** the booking status changes to DECLINED
**And** a notification with reason is sent to the tourist

**Given** a new booking request arrives
**When** the provider is on the dashboard
**Then** a badge/notification indicates new requests
**And** request count updates in real-time

**Given** a provider on mobile
**When** they view booking requests
**Then** one-tap accept/decline buttons are prominently displayed
**And** swipe gestures are supported (swipe right = accept, left = decline)

**Technical Notes:**
- Booking request endpoints consumed via OpenFeign from Booking Service (Epic 4)
- Accept/Decline API calls to Booking Service
- SQS event listener for new booking notifications
- Push notification setup (for future PWA support)
- Optimistic UI updates

---

## Epic 3: AI Trip Planning & Recommendations

**Epic Goal:** Implement the AI-powered trip planning experience using Google Gemini API, enabling tourists to chat naturally with an AI assistant to plan trips and receive personalized itineraries.

### Story 3.1: AI Agent Service Foundation

As a **system**,
I want a **dedicated AI Agent Service with Gemini API integration**,
So that **AI-powered features can be delivered reliably**.

**Acceptance Criteria:**

**Given** the AI Agent Service is deployed
**When** it starts up
**Then** it connects to Google Gemini API successfully
**And** health check endpoint returns healthy status

**Given** a request to the AI service
**When** the JWT token is valid
**Then** the request is processed
**And** user context (preferences, history) is available

**Given** a request without valid JWT
**When** processed by the service
**Then** a 401 Unauthorized error is returned

**Given** the Gemini API is unavailable
**When** a request is made
**Then** a graceful error response is returned
**And** the error is logged with correlation ID

**Technical Notes:**
- Creates AI Agent Service Spring Boot application
- Gemini API client configuration
- JWT validation middleware
- OpenFeign clients for Tourist, Hotel, Guide, Vehicle services
- Circuit breaker for external API calls

---

### Story 3.2: Chat Interface & Message Streaming

As a **tourist**,
I want to **chat with an AI assistant that responds in real-time**,
So that **planning feels natural and responsive**.

**Acceptance Criteria:**

**Given** an authenticated tourist
**When** they open the trip planning chat
**Then** a chat interface loads with welcome message
**And** their preferences are displayed as context

**Given** a tourist typing a message
**When** they send it
**Then** a typing indicator shows immediately
**And** the AI response streams word-by-word

**Given** a streaming response
**When** tokens arrive
**Then** they render progressively in the chat bubble
**And** the user can read while more content loads

**Given** a chat session
**When** the tourist navigates away and returns
**Then** the conversation history is preserved
**And** context is maintained

**Given** network interruption during streaming
**When** connection is lost
**Then** partial response is preserved
**And** user can retry the message

**Technical Notes:**
- Server-Sent Events (SSE) for streaming
- Chat API endpoint: POST /api/ai/chat (returns SSE stream)
- Frontend chat component with streaming render
- Message persistence in tourist schema
- Conversation context management

---

### Story 3.3: Natural Language Trip Planning

As a **tourist**,
I want to **describe my trip in natural language**,
So that **I don't need to fill out complex forms**.

**Acceptance Criteria:**

**Given** a tourist in the chat
**When** they say "I want to visit Colombo for 3 days next month with my family"
**Then** the AI extracts: destination (Colombo), duration (3 days), timing (next month), group (family)
**And** asks clarifying questions if needed

**Given** extracted trip parameters
**When** the AI processes them
**Then** it considers the tourist's saved preferences (interests, budget level)
**And** incorporates them into recommendations

**Given** ambiguous input like "somewhere nice for a week"
**When** processed
**Then** the AI asks follow-up questions about preferences
**And** suggests destinations based on tourist profile

**Given** a budget-specific request
**When** the tourist mentions budget constraints
**Then** the AI filters recommendations accordingly
**And** provides cost-conscious alternatives

**Technical Notes:**
- Gemini function calling for entity extraction
- Prompt engineering for travel context
- User preference injection into system prompt
- Multi-turn conversation state management

---

### Story 3.4: Internal-First Provider Search

As a **tourist**,
I want **AI to recommend registered providers first**,
So that **I support the platform's partners**.

**Acceptance Criteria:**

**Given** a trip planning request
**When** the AI searches for hotels
**Then** it queries Hotel Service first via OpenFeign
**And** registered hotels are prioritized in recommendations

**Given** internal search returns 3+ results
**When** presenting options
**Then** only internal providers are shown
**And** no external data is fetched

**Given** internal search returns < 3 results
**When** the threshold is not met
**Then** external data sources are queried to supplement
**And** results are clearly labeled (Partner vs External)

**Given** a search for tour guides
**When** processed
**Then** Tour Guide Service is queried first
**And** same internal-first logic applies

**Given** a search for vehicles
**When** processed
**Then** Vehicle Service is queried first
**And** same internal-first logic applies

**Technical Notes:**
- OpenFeign clients to Hotel, Guide, Vehicle services
- Search aggregation logic
- Result count threshold check (configurable, default 3)
- External API integration (Google Places, TripAdvisor) as fallback
- Result source labeling

---

### Story 3.5: Personalized Itinerary Generation

As a **tourist**,
I want the **AI to generate a complete day-by-day itinerary**,
So that **I have a ready-to-book travel plan**.

**Acceptance Criteria:**

**Given** a tourist has confirmed trip parameters
**When** they request an itinerary
**Then** the AI generates a day-by-day schedule
**And** each day includes activities, meals, and accommodation

**Given** the generated itinerary
**When** displayed
**Then** each day shows: morning/afternoon/evening activities
**And** travel time between locations is estimated

**Given** tourist preferences include specific interests
**When** itinerary is generated
**Then** activities match their interests (Adventure, Culture, Food, etc.)
**And** the balance reflects their priority order

**Given** an itinerary with multiple days
**When** hotels are included
**Then** the same hotel is suggested for consecutive nights in one location
**And** hotel changes are minimized for convenience

**Technical Notes:**
- Itinerary generation prompt template
- Day structure: {morning, afternoon, evening, accommodation}
- Activity sequencing by location proximity
- Duration estimation for activities
- JSON structured output from Gemini

---

### Story 3.6: Cost Breakdown & Budget Tracking

As a **tourist**,
I want to **see estimated costs for my itinerary**,
So that **I can plan within my budget**.

**Acceptance Criteria:**

**Given** a generated itinerary
**When** displayed
**Then** each item shows estimated cost
**And** a running total is calculated

**Given** the cost breakdown
**When** viewing the summary
**Then** costs are categorized: Accommodation, Transportation, Activities, Food
**And** percentage of total is shown per category

**Given** a tourist's budget limit
**When** itinerary exceeds budget
**Then** a warning is displayed
**And** AI suggests cost-saving alternatives

**Given** internal provider pricing
**When** included in itinerary
**Then** actual prices from the platform are used
**And** marked as "Book Now" prices

**Given** external recommendations
**When** cost is estimated
**Then** prices are marked as "Estimated"
**And** may vary at booking

**Technical Notes:**
- Cost aggregation from provider APIs
- Budget comparison logic
- Alternative suggestion prompt
- Currency conversion support (display in tourist's preferred currency)
- Price confidence indicator (Exact vs Estimated)

---

### Story 3.7: Quick Reply Chips & Actions

As a **tourist**,
I want **quick reply options during chat**,
So that **I can respond faster without typing**.

**Acceptance Criteria:**

**Given** the AI asks a question
**When** there are common answers
**Then** quick reply chips appear below the message
**And** tapping a chip sends that response

**Given** an itinerary is presented
**When** displayed
**Then** action chips appear: "Book This", "Modify", "Start Over"
**And** tapping triggers the corresponding action

**Given** multiple hotel options
**When** shown
**Then** each has a "Select" chip
**And** selection updates the itinerary

**Given** the tourist types a custom response
**When** quick chips are visible
**Then** chips disappear as typing starts
**And** custom message is sent normally

**Technical Notes:**
- Quick reply schema in message payload
- Chip component with variants (text, action)
- Dynamic chip generation based on context
- Chip action handlers (book, modify, select, etc.)

---

## Epic 4: Booking & Reservations

**Epic Goal:** Implement the booking workflow enabling tourists to reserve hotels, tour guides, and vehicles, with support for multi-provider bookings using the saga pattern.

### Story 4.1: Booking Service Foundation

As a **system**,
I want a **dedicated Booking Service to manage all reservations**,
So that **booking logic is centralized and consistent**.

**Acceptance Criteria:**

**Given** the Booking Service is deployed
**When** it starts up
**Then** it connects to the database successfully
**And** health check returns healthy

**Given** a booking request
**When** JWT is valid
**Then** the request is processed with user context

**Given** the service
**When** it needs provider data
**Then** it communicates via OpenFeign to Hotel, Guide, Vehicle services

**Given** booking events occur
**When** notifications are needed
**Then** events are published to SQS

**Technical Notes:**
- Creates `booking` schema with `bookings`, `booking_items` tables
- Booking Service Spring Boot application
- OpenFeign clients for provider services
- SQS publisher for booking events
- Booking statuses: PENDING, CONFIRMED, CANCELLED, COMPLETED

---

### Story 4.2: Single Provider Booking

As a **tourist**,
I want to **book a single hotel, guide, or vehicle**,
So that **I can reserve services for my trip**.

**Acceptance Criteria:**

**Given** a tourist selects a hotel room
**When** they provide check-in/check-out dates and guest count
**Then** availability is verified in real-time
**And** if available, booking is created with PENDING status

**Given** a tourist books a tour guide
**When** they provide date, start time, and duration
**Then** guide availability is verified
**And** if available, booking is created

**Given** a tourist books a vehicle
**When** they provide pickup/return dates and location
**Then** vehicle availability is verified
**And** if available, booking is created

**Given** a successful booking creation
**When** the booking is saved
**Then** the provider is notified via SQS event
**And** availability is temporarily held (15 min for payment in future)

**Technical Notes:**
- Booking API: POST /api/bookings
- Availability check via OpenFeign before creation
- Optimistic locking for concurrent booking prevention
- Temporary hold mechanism (expires without confirmation)

---

### Story 4.3: Pre-Booking Availability Validation

As a **tourist**,
I want the **system to verify availability before I book**,
So that **I don't waste time on unavailable options**.

**Acceptance Criteria:**

**Given** a tourist initiates a booking
**When** the request is received
**Then** availability is checked with the provider service FIRST
**And** booking proceeds only if available

**Given** the requested dates are unavailable
**When** availability check fails
**Then** a 409 Conflict response is returned
**And** alternative available dates are suggested if possible

**Given** multiple tourists book the same resource simultaneously
**When** both requests arrive
**Then** only one succeeds (first-write-wins)
**And** the other receives unavailable error

**Given** availability check
**When** performed
**Then** it happens in < 500ms
**And** does not create any database records

**Technical Notes:**
- Availability check endpoint on each provider service
- Database-level locking for concurrent requests
- Optimistic concurrency with version column
- Quick availability API (read-only, cached)

---

### Story 4.4: Multi-Provider Booking (Saga Pattern)

As a **tourist**,
I want to **book multiple services in one transaction**,
So that **my entire trip is reserved together**.

**Acceptance Criteria:**

**Given** a tourist books hotel + guide + vehicle together
**When** the multi-booking request is submitted
**Then** a saga orchestration begins
**And** each provider is booked sequentially

**Given** all providers confirm availability
**When** the saga completes successfully
**Then** all bookings are CONFIRMED
**And** a single booking reference covers all items

**Given** one provider fails (unavailable)
**When** the saga detects failure
**Then** compensating transactions cancel previous bookings
**And** the tourist is notified which item failed

**Given** a partial failure
**When** rollback completes
**Then** no orphan bookings remain
**And** the system is in consistent state

**Given** the saga is in progress
**When** the tourist checks status
**Then** they see "Processing" with progress indicator

**Technical Notes:**
- Saga orchestrator in Booking Service
- Compensating transaction for each step
- Saga state persisted for recovery
- Timeout handling (30s per step)
- Idempotency keys for retry safety

---

### Story 4.5: Booking Details & Status Tracking

As a **tourist**,
I want to **view my booking details and current status**,
So that **I know what I've reserved**.

**Acceptance Criteria:**

**Given** an authenticated tourist
**When** they view their bookings
**Then** a list of all bookings is displayed
**And** sorted by date (upcoming first)

**Given** a booking in the list
**When** displayed
**Then** it shows: provider name, dates, status, total cost
**And** status badge with color coding (Pending=yellow, Confirmed=green, Cancelled=red)

**Given** a tourist clicks on a booking
**When** details expand
**Then** full information is shown: address, contact, confirmation number
**And** action buttons based on status

**Given** a multi-provider booking
**When** viewed
**Then** all items are grouped under one booking reference
**And** individual item statuses are shown

**Technical Notes:**
- Booking list API: GET /api/bookings
- Booking detail API: GET /api/bookings/{id}
- Frontend booking cards with expandable details
- Status timeline component

---

### Story 4.6: Booking Cancellation & Refunds

As a **tourist**,
I want to **cancel bookings and receive appropriate refunds**,
So that **I can change my plans if needed**.

**Acceptance Criteria:**

**Given** a tourist with a confirmed booking
**When** they request cancellation
**Then** the refund policy is displayed before confirming
**And** estimated refund amount is shown

**Given** cancellation > 48 hours before service
**When** confirmed
**Then** full refund is processed to wallet credits
**And** booking status changes to CANCELLED

**Given** cancellation within 24-48 hours
**When** confirmed
**Then** 50% refund is processed
**And** policy reason is recorded

**Given** cancellation < 24 hours before service
**When** confirmed
**Then** no refund is given
**And** tourist is warned before confirming

**Given** a cancelled booking
**When** provider is notified
**Then** availability is released
**And** provider receives cancellation event via SQS

**Technical Notes:**
- Cancellation API: POST /api/bookings/{id}/cancel
- Refund policy engine (configurable per provider type)
- Wallet credit via SQS event to Tourist Service
- Availability release via OpenFeign to provider

---

### Story 4.7: Provider Booking Notifications

As a **provider**,
I want to **receive instant notifications for new bookings**,
So that **I can respond quickly**.

**Acceptance Criteria:**

**Given** a new booking is created
**When** status is PENDING
**Then** an SQS message is published to provider queue
**And** contains booking details and tourist info

**Given** a provider has the dashboard open
**When** a new booking arrives
**Then** the notification count updates
**And** a toast notification appears

**Given** a booking is cancelled
**When** cancellation is confirmed
**Then** provider receives cancellation notification
**And** reason is included if provided

**Given** a booking is confirmed by provider
**When** they accept
**Then** tourist receives confirmation notification via SQS
**And** email confirmation is sent via SES

**Technical Notes:**
- SQS queues: booking-notifications, cancellation-notifications
- Consumer in each provider service
- Email notification via Amazon SES
- Real-time updates via polling or WebSocket (future)

---

## Epic 5: Itinerary & Trip Lifecycle

**Epic Goal:** Manage the complete trip lifecycle from booking confirmation through trip completion, including itinerary creation, expense tracking, and post-trip review triggers.

### Story 5.1: Itinerary Service Foundation

As a **system**,
I want a **dedicated Itinerary Service to manage trip schedules**,
So that **trip data is organized and accessible**.

**Acceptance Criteria:**

**Given** the Itinerary Service is deployed
**When** it starts up
**Then** it connects to the database and message queues
**And** health check returns healthy

**Given** a booking is confirmed
**When** the confirmation event arrives via SQS
**Then** the service processes it for itinerary creation

**Given** a trip completes
**When** the end date passes
**Then** the service triggers post-trip events

**Technical Notes:**
- Creates `itinerary` schema with `itineraries`, `itinerary_items`, `expenses` tables
- Itinerary Service Spring Boot application
- SQS consumer for booking events
- SQS publisher for review trigger events
- Trip statuses: DRAFT, ACTIVE, COMPLETED, CANCELLED

---

### Story 5.2: Automatic Itinerary Creation from Bookings

As a **tourist**,
I want **itineraries created automatically when I book**,
So that **I don't have to manually organize my trip**.

**Acceptance Criteria:**

**Given** a booking is confirmed
**When** no itinerary exists for those dates
**Then** a new itinerary is created automatically
**And** the booking is added as an itinerary item

**Given** a booking is confirmed
**When** an itinerary already exists for overlapping dates
**Then** the booking is added to the existing itinerary
**And** items are sorted chronologically

**Given** a multi-provider booking
**When** all items are confirmed
**Then** all items appear in the same itinerary
**And** grouped logically (hotel spans multiple days)

**Given** a hotel booking
**When** added to itinerary
**Then** it spans from check-in to check-out dates
**And** appears in the accommodation section for each day

**Technical Notes:**
- SQS consumer for BOOKING_CONFIRMED events
- Itinerary creation/update logic
- Date range handling for multi-day bookings
- Item types: ACCOMMODATION, TRANSPORT, ACTIVITY, GUIDE

---

### Story 5.3: Day-by-Day Schedule View

As a **tourist**,
I want to **view my trip as a day-by-day schedule**,
So that **I know what's planned each day**.

**Acceptance Criteria:**

**Given** a tourist with an active itinerary
**When** they open the itinerary view
**Then** a timeline shows each day of the trip
**And** days are expandable to show details

**Given** a specific day
**When** expanded
**Then** items are shown in chronological order
**And** include: time, activity name, location, duration

**Given** an itinerary with hotel bookings
**When** viewing any day during the stay
**Then** the hotel appears as "Staying at [Hotel Name]"
**And** check-in/check-out days are marked

**Given** empty time slots
**When** viewing the day
**Then** gaps are visible
**And** "Free time" is displayed

**Technical Notes:**
- Itinerary API: GET /api/itineraries/{id}
- Day aggregation logic
- Timeline component with expandable days
- Time slot visualization

---

### Story 5.4: Map-Based Itinerary Visualization

As a **tourist**,
I want to **see my itinerary on a map**,
So that **I understand the geography of my trip**.

**Acceptance Criteria:**

**Given** an itinerary with location data
**When** the map view is selected
**Then** all locations are plotted on an interactive map
**And** connected by route lines in chronological order

**Given** multiple items on the same day
**When** viewing day-specific map
**Then** only that day's locations are highlighted
**And** numbered in visit order

**Given** a location marker
**When** tapped/clicked
**Then** a popup shows item details
**And** quick actions (directions, details)

**Given** the map
**When** zoomed out
**Then** the entire trip route is visible
**And** day boundaries are color-coded

**Technical Notes:**
- Map integration (Leaflet or Google Maps)
- GeoJSON for route rendering
- Location coordinates from provider data
- Day-based filtering

---

### Story 5.5: Expense Tracking

As a **tourist**,
I want to **track my expenses during the trip**,
So that **I stay within budget**.

**Acceptance Criteria:**

**Given** a tourist on an active trip
**When** they add an expense
**Then** they enter: amount, category, description, date
**And** the expense is saved to the itinerary

**Given** expenses exist
**When** viewing the expense summary
**Then** total spent is displayed
**And** breakdown by category is shown

**Given** a budget was set
**When** expenses exceed budget
**Then** a warning is displayed
**And** remaining budget shows negative

**Given** the expense list
**When** viewing
**Then** expenses are grouped by day
**And** daily totals are calculated

**Given** a tourist
**When** they want to remove an expense
**Then** they can delete it
**And** totals recalculate

**Technical Notes:**
- Expenses API: POST/GET/DELETE /api/itineraries/{id}/expenses
- Expense categories: Food, Transport, Activities, Shopping, Other
- Budget comparison logic
- Frontend expense input form

---

### Story 5.6: Custom Itinerary Items

As a **tourist**,
I want to **add my own items to the itinerary**,
So that **personal plans are included**.

**Acceptance Criteria:**

**Given** a tourist viewing their itinerary
**When** they click "Add Custom Item"
**Then** a form appears for: name, date, time, location, notes

**Given** a custom item is added
**When** saved
**Then** it appears in the day's schedule
**And** is marked as "Personal" to distinguish from bookings

**Given** a custom item
**When** the tourist edits it
**Then** changes are saved
**And** reflected immediately in the schedule

**Given** a custom item
**When** deleted
**Then** it's removed from the itinerary
**And** the schedule updates

**Technical Notes:**
- Custom items stored in `itinerary_items` with type CUSTOM
- CRUD operations for custom items
- No booking reference for custom items
- Optional location with map picker

---

### Story 5.7: PDF Itinerary Download

As a **tourist**,
I want to **download my itinerary as a PDF**,
So that **I can access it offline**.

**Acceptance Criteria:**

**Given** a tourist with an itinerary
**When** they click "Download PDF"
**Then** a PDF is generated with the complete itinerary
**And** downloads to their device

**Given** the PDF content
**When** generated
**Then** it includes: trip title, dates, day-by-day schedule
**And** provider contact information

**Given** the PDF
**When** opened offline
**Then** all text and maps are visible
**And** no internet required to view

**Given** bookings with confirmation numbers
**When** included in PDF
**Then** confirmation numbers are displayed
**And** QR codes for quick check-in (if supported)

**Technical Notes:**
- PDF generation library (OpenPDF or iText)
- API endpoint: GET /api/itineraries/{id}/pdf
- Static map images embedded
- Styling for print readability

---

### Story 5.8: Trip Completion & Review Triggers

As a **system**,
I want to **detect trip completion and trigger review requests**,
So that **tourists are prompted to leave feedback**.

**Acceptance Criteria:**

**Given** an active itinerary
**When** the end date passes (midnight local time)
**Then** the itinerary status changes to COMPLETED
**And** a review trigger event is published

**Given** a completed trip
**When** the review trigger fires
**Then** an SQS message is sent to Review Service
**And** contains: tourist ID, list of booked providers

**Given** the tourist
**When** they next log in after trip completion
**Then** they see a prompt to review their providers
**And** can access pending reviews

**Given** a cancelled itinerary
**When** the end date passes
**Then** no review trigger is sent
**And** status remains CANCELLED

**Technical Notes:**
- Scheduled job for trip completion detection (daily)
- SQS publisher for TRIP_COMPLETED events
- Status transition logic
- Frontend review prompt component

---

## Epic 6: Reviews & Ratings

**Epic Goal:** Implement the review system allowing tourists to rate providers after trips, with aggregate rating calculations and provider response capabilities.

### Story 6.1: Review Service Foundation

As a **system**,
I want a **dedicated Review Service to manage all feedback**,
So that **reviews are handled consistently across providers**.

**Acceptance Criteria:**

**Given** the Review Service is deployed
**When** it starts up
**Then** it connects to database and message queues
**And** health check returns healthy

**Given** a trip completion event
**When** received via SQS
**Then** pending review records are created for each provider

**Given** a review is submitted
**When** saved
**Then** a rating update event is published
**And** provider aggregate is recalculated

**Technical Notes:**
- Creates `review` schema with `reviews`, `review_photos`, `pending_reviews` tables
- Review Service Spring Boot application
- SQS consumer for TRIP_COMPLETED events
- SQS publisher for RATING_UPDATED events
- OpenFeign clients to provider services

---

### Story 6.2: Review Submission with Ratings

As a **tourist**,
I want to **submit reviews with star ratings and text**,
So that **I can share my experience**.

**Acceptance Criteria:**

**Given** a tourist with a completed booking
**When** they open the review form
**Then** provider details are displayed
**And** a 1-5 star rating selector is shown

**Given** a tourist completing a review
**When** they select stars and enter text
**Then** both are required (minimum 10 characters text)
**And** they can submit the review

**Given** a valid review submission
**When** saved
**Then** the review is linked to booking and provider
**And** pending review status is cleared

**Given** a tourist attempts to review same provider twice
**When** they try to submit
**Then** an error indicates already reviewed
**And** existing review is shown for editing

**Technical Notes:**
- Review API: POST /api/reviews
- Validation: rating 1-5, text 10-1000 chars
- One review per booking item per tourist
- Review editing allowed within 7 days

---

### Story 6.3: Photo Uploads with Reviews

As a **tourist**,
I want to **add photos to my reviews**,
So that **others can see visual evidence**.

**Acceptance Criteria:**

**Given** a tourist writing a review
**When** they click "Add Photos"
**Then** they can select up to 5 images
**And** previews are shown before submission

**Given** photos are selected
**When** the review is submitted
**Then** photos are uploaded and linked to the review
**And** optimized for web display

**Given** a review with photos
**When** displayed publicly
**Then** photos appear in a thumbnail gallery
**And** can be viewed full-size on click

**Given** inappropriate photo content
**When** detected (future: AI moderation)
**Then** photo is flagged for review
**And** not displayed until approved

**Technical Notes:**
- Photo upload to S3/Supabase Storage
- Image optimization (resize, compress)
- Review photos table with order field
- Lazy loading for gallery display

---

### Story 6.4: Aggregate Rating Calculation

As a **provider**,
I want **my overall rating calculated from reviews**,
So that **tourists see my average performance**.

**Acceptance Criteria:**

**Given** a new review is submitted
**When** saved
**Then** an event triggers rating recalculation
**And** provider's aggregate rating is updated

**Given** a provider with multiple reviews
**When** aggregate is calculated
**Then** it's the average of all review ratings
**And** rounded to 1 decimal place

**Given** a provider with no reviews
**When** rating is displayed
**Then** "No reviews yet" is shown
**And** numeric rating is null/0

**Given** the aggregate rating
**When** displayed on provider listings
**Then** it shows: star icon + rating + review count
**And** updates reflect within 1 minute

**Technical Notes:**
- SQS consumer for REVIEW_SUBMITTED events
- Rating calculation service
- Update via OpenFeign to provider service
- Cached aggregate in provider record
- Event: RATING_UPDATED

---

### Story 6.5: Pending Reviews Management

As a **tourist**,
I want to **see which providers I need to review**,
So that **I don't forget to give feedback**.

**Acceptance Criteria:**

**Given** a tourist with completed trips
**When** they visit "My Reviews"
**Then** pending reviews are listed prominently
**And** show provider name and trip date

**Given** pending reviews exist
**When** the tourist logs in
**Then** a notification badge shows count
**And** a prompt appears on dashboard

**Given** a pending review
**When** clicked
**Then** the review form opens for that provider
**And** booking details are pre-filled

**Given** a pending review older than 30 days
**When** displayed
**Then** it's marked as "Expiring Soon"
**And** after 60 days, it's automatically dismissed

**Technical Notes:**
- Pending reviews API: GET /api/reviews/pending
- Created from TRIP_COMPLETED event
- Expiration logic (60 days)
- Dashboard notification component

---

### Story 6.6: Provider Review Responses

As a **provider**,
I want to **respond to reviews**,
So that **I can address feedback publicly**.

**Acceptance Criteria:**

**Given** a provider viewing reviews on their dashboard
**When** they see a review without response
**Then** a "Respond" button is available

**Given** a provider writing a response
**When** they submit text (max 500 characters)
**Then** the response is saved and linked to the review
**And** displayed below the original review

**Given** an existing response
**When** the provider wants to edit
**Then** they can update within 7 days
**And** edited responses show "Edited" label

**Given** a tourist viewing a review with response
**When** displayed
**Then** both review and response are visible
**And** provider name/photo accompanies response

**Technical Notes:**
- Response API: POST /api/reviews/{id}/response
- One response per review
- Edit window: 7 days
- Response moderation (future)

---

### Story 6.7: Review Display on Provider Listings

As a **tourist**,
I want to **read reviews on provider pages**,
So that **I can make informed decisions**.

**Acceptance Criteria:**

**Given** a provider listing page
**When** loaded
**Then** reviews section shows recent reviews
**And** sorted by most recent first

**Given** reviews on a listing
**When** displayed
**Then** each shows: rating, text, photos, date, tourist name (first name + initial)
**And** provider response if exists

**Given** many reviews
**When** viewing
**Then** pagination or "Load More" is available
**And** filter by rating is supported

**Given** the reviews section
**When** on mobile
**Then** layout is optimized for touch
**And** photo galleries swipe horizontally

**Technical Notes:**
- Reviews API with provider filter: GET /api/reviews?providerId={id}
- Review card component
- Photo carousel in review card
- Rating filter dropdown

---

## Epic 7: Trip Packages

**Epic Goal:** Enable admins to create pre-built trip packages that bundle providers, allowing tourists to book complete trips in one action.

### Story 7.1: Trip Plan Service Foundation

As a **system**,
I want a **dedicated Trip Plan Service to manage packages**,
So that **package operations are centralized**.

**Acceptance Criteria:**

**Given** the Trip Plan Service is deployed
**When** it starts up
**Then** it connects to database successfully
**And** health check returns healthy

**Given** package operations
**When** provider data is needed
**Then** it communicates via OpenFeign to provider services

**Given** a package booking
**When** initiated
**Then** it coordinates with Booking Service for reservations

**Technical Notes:**
- Creates `trip_plan` schema with `packages`, `package_items`, `package_pricing` tables
- Trip Plan Service Spring Boot application
- OpenFeign clients to all provider services and Booking Service
- Package statuses: DRAFT, ACTIVE, ARCHIVED

---

### Story 7.2: Admin Package Creation

As an **admin**,
I want to **create trip packages with bundled services**,
So that **tourists have curated options**.

**Acceptance Criteria:**

**Given** an authenticated admin
**When** they access package management
**Then** they see list of existing packages
**And** can create new packages

**Given** admin creating a package
**When** they fill in: name, description, duration (days), theme, and base location
**Then** the package shell is created
**And** they can add items

**Given** a package in edit mode
**When** admin adds items
**Then** they can select: hotels, guides, vehicles from registered providers
**And** specify which days each applies

**Given** package items
**When** saved
**Then** each item links to the actual provider
**And** includes day assignments and quantities

**Technical Notes:**
- Admin routes with ADMIN role validation
- Package CRUD API
- Item selection with provider search
- Day assignment matrix

---

### Story 7.3: Package Pricing & Bundling

As an **admin**,
I want to **set package pricing with optional discounts**,
So that **packages offer value**.

**Acceptance Criteria:**

**Given** a package with items
**When** viewing pricing
**Then** sum of individual item prices is calculated
**And** displayed as "Regular Price"

**Given** admin setting package price
**When** they enter a package price
**Then** it can be less than sum (bundle discount)
**And** savings amount is calculated and displayed

**Given** seasonal pricing
**When** admin sets date ranges with price variations
**Then** package price varies by booking date
**And** tourists see applicable price

**Given** the package
**When** provider prices change
**Then** admin is notified
**And** can update package pricing

**Technical Notes:**
- Price aggregation from provider items
- Bundle discount calculation
- Seasonal pricing table
- Price change detection (periodic job)

---

### Story 7.4: Package Browsing & Filtering

As a **tourist**,
I want to **browse packages by theme, duration, and budget**,
So that **I find trips matching my interests**.

**Acceptance Criteria:**

**Given** a tourist on the packages page
**When** loaded
**Then** featured packages are displayed
**And** filter options are visible

**Given** filter by theme
**When** tourist selects (Adventure, Culture, Beach, Wildlife, etc.)
**Then** only matching packages are shown

**Given** filter by duration
**When** tourist selects range (1-3 days, 4-7 days, 7+ days)
**Then** packages within range are shown

**Given** filter by budget
**When** tourist sets price range
**Then** packages within budget are shown
**And** sorted by relevance

**Given** search results
**When** displayed
**Then** each package shows: image, name, duration, price, rating
**And** "View Details" button

**Technical Notes:**
- Package search API: GET /api/packages/search
- Filter parameters: theme, minDays, maxDays, minPrice, maxPrice
- Featured flag for homepage display
- Package card component

---

### Story 7.5: Package Details & Preview

As a **tourist**,
I want to **see full package details before booking**,
So that **I know exactly what's included**.

**Acceptance Criteria:**

**Given** a tourist clicks on a package
**When** details page loads
**Then** full description and photos are shown
**And** day-by-day breakdown is displayed

**Given** the day-by-day view
**When** expanded
**Then** each day shows: accommodation, activities, transport
**And** included meals/services

**Given** package items
**When** displayed
**Then** each links to the actual provider page
**And** tourist can view provider ratings/reviews

**Given** the package
**When** tourist selects travel dates
**Then** availability is checked for all items
**And** total price for those dates is calculated

**Technical Notes:**
- Package detail API: GET /api/packages/{id}
- Day breakdown component
- Provider links with ratings
- Date-based availability check

---

### Story 7.6: One-Click Package Booking

As a **tourist**,
I want to **book an entire package in one action**,
So that **my complete trip is reserved instantly**.

**Acceptance Criteria:**

**Given** a tourist viewing package with valid dates
**When** they click "Book Package"
**Then** booking confirmation shows all items and total price
**And** they can confirm the booking

**Given** booking confirmation
**When** tourist confirms
**Then** a multi-provider saga booking is initiated
**And** all items are booked via Booking Service

**Given** successful package booking
**When** all providers confirm
**Then** a single booking reference is created
**And** celebration animation plays

**Given** partial failure in package booking
**When** one provider is unavailable
**Then** the entire booking is rolled back
**And** tourist is informed which item failed

**Technical Notes:**
- Package booking API: POST /api/packages/{id}/book
- Delegates to Booking Service saga
- All-or-nothing transaction
- Booking reference links to package

---

### Story 7.7: Package Itinerary Generation

As a **tourist**,
I want **my package booking to create a complete itinerary**,
So that **my trip is organized automatically**.

**Acceptance Criteria:**

**Given** a successful package booking
**When** completed
**Then** an itinerary is automatically created
**And** all package items are added to appropriate days

**Given** the generated itinerary
**When** viewed
**Then** it follows the package's day-by-day structure
**And** includes all booked providers

**Given** the itinerary
**When** tourist wants to customize
**Then** they can add custom items
**And** original package items remain linked

**Given** package-generated itinerary
**When** displayed
**Then** it's marked as "Based on [Package Name]"
**And** links back to package details

**Technical Notes:**
- Itinerary creation triggered by package booking event
- Package day structure mapped to itinerary
- Source tracking (package reference)
- Custom items allowed alongside package items

---

## Epic 8: Events & E-Commerce MVP

**Epic Goal:** Implement simplified event management and e-commerce features as MVP scope, allowing admins to manage events and products, and tourists to browse and order.

### Story 8.1: Event Service Foundation

As a **system**,
I want a **dedicated Event Service to manage events**,
So that **event operations are centralized**.

**Acceptance Criteria:**

**Given** the Event Service is deployed
**When** it starts up
**Then** it connects to database successfully
**And** health check returns healthy

**Given** event queries
**When** location filtering is needed
**Then** geo-queries are supported

**Technical Notes:**
- Creates `event` schema with `events` table
- Event Service Spring Boot application
- Location with coordinates for geo-queries
- Event statuses: DRAFT, PUBLISHED, CANCELLED, COMPLETED

---

### Story 8.2: Admin Event Management

As an **admin**,
I want to **create and manage events**,
So that **tourists discover local happenings**.

**Acceptance Criteria:**

**Given** an authenticated admin
**When** they access event management
**Then** they see list of all events
**And** can create, edit, or delete events

**Given** admin creating an event
**When** they fill in: name, description, location (address + coordinates), date/time, duration
**Then** the event is created in DRAFT status

**Given** an event in DRAFT
**When** admin clicks "Publish"
**Then** status changes to PUBLISHED
**And** event appears in tourist searches

**Given** a published event
**When** the event date passes
**Then** status automatically changes to COMPLETED

**Technical Notes:**
- Admin CRUD for events
- Image upload for event banner
- Location picker with map
- Status transitions

---

### Story 8.3: Event Browsing & Discovery

As a **tourist**,
I want to **browse events by location and date**,
So that **I find activities during my trip**.

**Acceptance Criteria:**

**Given** a tourist on the events page
**When** loaded
**Then** upcoming events are displayed
**And** filtered by their current/searched location

**Given** a tourist searching for events
**When** they enter location and date range
**Then** matching events are returned
**And** sorted by date (soonest first)

**Given** event search results
**When** displayed
**Then** each shows: image, name, date, location, brief description

**Given** a tourist clicks on an event
**When** details page loads
**Then** full description, exact location with map, and timing are shown

**Technical Notes:**
- Event search API: GET /api/events/search
- Location radius filter (default 50km)
- Date range filter
- Event card and detail components

---

### Story 8.4: Events in Location Searches

As a **tourist**,
I want to **see relevant events when searching locations**,
So that **I discover activities alongside accommodations**.

**Acceptance Criteria:**

**Given** a tourist searching for hotels in a location
**When** results are displayed
**Then** a "Happening Nearby" section shows events in that area

**Given** AI trip planning
**When** generating itinerary for a destination
**Then** relevant events during trip dates are suggested
**And** can be added to itinerary

**Given** an itinerary view
**When** events exist at the destination during trip
**Then** an "Events" tab shows matching events

**Technical Notes:**
- Event search integration in location queries
- AI prompt injection for event awareness
- Cross-service event lookup via OpenFeign

---

### Story 8.5: E-Commerce Service Foundation

As a **system**,
I want a **dedicated E-Commerce Service for product management**,
So that **merchandise can be sold**.

**Acceptance Criteria:**

**Given** the E-Commerce Service is deployed
**When** it starts up
**Then** it connects to database successfully
**And** health check returns healthy

**Given** orders are created
**When** saved
**Then** order status is tracked
**And** tourist is linked to their orders

**Technical Notes:**
- Creates `ecommerce` schema with `products`, `orders`, `order_items` tables
- E-Commerce Service Spring Boot application
- Product statuses: DRAFT, ACTIVE, OUT_OF_STOCK, DISCONTINUED
- Order statuses: PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED

---

### Story 8.6: Admin Product Catalog Management

As an **admin**,
I want to **manage the product catalog**,
So that **tourists can browse merchandise**.

**Acceptance Criteria:**

**Given** an authenticated admin
**When** they access product management
**Then** they see list of all products
**And** can create, edit, or delete products

**Given** admin creating a product
**When** they fill in: name, description, price, category, stock quantity
**Then** the product is created in DRAFT status

**Given** product with images
**When** admin uploads photos (up to 5)
**Then** images are stored and displayed in gallery

**Given** a product
**When** admin changes status to ACTIVE
**Then** product appears in tourist catalog

**Technical Notes:**
- Product CRUD API
- Image upload for product photos
- Categories: Souvenirs, Apparel, Accessories, Art, Food
- Stock quantity tracking

---

### Story 8.7: Product Browsing

As a **tourist**,
I want to **browse available products**,
So that **I can purchase travel souvenirs**.

**Acceptance Criteria:**

**Given** a tourist on the shop page
**When** loaded
**Then** active products are displayed in a grid
**And** category filters are available

**Given** a tourist filtering by category
**When** they select a category
**Then** only matching products are shown

**Given** product listings
**When** displayed
**Then** each shows: image, name, price
**And** "View" button

**Given** a tourist clicks on a product
**When** details page loads
**Then** full description, all images, and price are shown
**And** "Add to Cart" button appears

**Technical Notes:**
- Product catalog API: GET /api/products
- Category filter
- Product card component
- Product detail page

---

### Story 8.8: Shopping Cart & Order Creation

As a **tourist**,
I want to **add products to cart and place orders**,
So that **I can purchase items**.

**Acceptance Criteria:**

**Given** a tourist viewing a product
**When** they click "Add to Cart"
**Then** product is added to their cart
**And** cart count updates in header

**Given** a tourist with items in cart
**When** they view the cart
**Then** all items are listed with quantities and prices
**And** total is calculated

**Given** cart items
**When** tourist adjusts quantity or removes items
**Then** cart updates accordingly
**And** total recalculates

**Given** a tourist ready to order
**When** they click "Place Order" and provide shipping address
**Then** an order is created with PENDING status
**And** confirmation is displayed

**Given** order confirmation
**When** displayed
**Then** order number, items, total, and shipping address are shown
**And** order appears in "My Orders"

**Technical Notes:**
- Cart stored in localStorage (MVP) or session
- Order creation API: POST /api/orders
- Order confirmation page
- Order history: GET /api/orders
- Note: Payment integration deferred (university project constraint)

---

## Complete Summary

| Epic | Title | Stories | FRs |
|------|-------|---------|-----|
| 1 | Foundation & User Authentication | 6 | FR1-FR5 |
| 2 | Provider Marketplace - Listings | 12 | FR11-FR28 |
| 3 | AI Trip Planning & Recommendations | 7 | FR6-FR10 |
| 4 | Booking & Reservations | 7 | FR29-FR34 |
| 5 | Itinerary & Trip Lifecycle | 8 | FR35-FR40 |
| 6 | Reviews & Ratings | 7 | FR41-FR45 |
| 7 | Trip Packages | 7 | FR46-FR50 |
| 8 | Events & E-Commerce MVP | 8 | FR51-FR58 |

**Total Stories:** 62
**Total FRs Covered:** 58/58 (100%)
**Total NFRs Referenced:** 10/10

