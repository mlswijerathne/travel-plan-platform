---
stepsCompleted: [1, 2, 3, 4, 5, 6, 7, 8]
inputDocuments:
  - "docs/project_idea.md"
  - "_bmad-output/brainstorming/FINAL-travel-app-microservices-architecture-report.md"
projectName: "Travel Plan Web Application"
author: "LakshithaWijerathneB"
date: "2026-01-30"
---

# UX Design Specification: Travel Plan Web Application

**Author:** LakshithaWijerathneB
**Date:** 2026-01-30

---

## Executive Summary

### Project Vision

Travel Plan Web Application is an AI-powered travel planning platform for Sri Lanka tourism that transforms trip planning from overwhelming research into a simple conversation. The platform connects international tourists with registered local providers (hotels, tour guides, vehicle owners) through an intelligent multi-agent system that prioritizes platform partners while supplementing with external data when needed.

The core experience centers on an AI companion that understands traveler preferences, weather patterns, optimal routes, and budget constraints to generate personalized itineraries that can be booked in a single flow. The platform serves dual audiences: tourists seeking stress-free travel experiences and local providers seeking visibility and bookings.

### Target Users

**Primary Users:**

| Persona | Description | Primary Goal | Device Preference |
|---------|-------------|--------------|-------------------|
| **Tourist** | International travelers (moderate-high tech comfort) planning Sri Lanka trips | Discover, plan, and book a complete trip with minimal friction | Desktop for planning, Mobile during travel |
| **Hotel Owner** | Small to medium property owners seeking online bookings | List property, manage availability, accept bookings | Mobile-primary |
| **Tour Guide** | Independent guides building reputation and client base | Showcase skills, receive bookings, build reviews | Mobile-primary |
| **Vehicle Owner** | Transport providers (cars, vans, tuk-tuks) seeking hires | List vehicles, manage schedule, accept trip requests | Mobile-primary |
| **Admin** | Platform administrators curating quality experiences | Manage events, create packages, monitor platform health | Desktop |

**User Characteristics:**
- Tourists have moderate-high booking anxiety (unfamiliar destination, language barriers)
- Providers have basic-to-moderate tech comfort; interfaces must be extremely simple
- All users expect mobile-responsive experiences

### Key Design Challenges

1. **Two-App Problem**: Serving tourists (discovery/inspiration) and providers (control/revenue) with completely different mental models while maintaining cohesive brand identity

2. **AI Trust & Transparency**: Building user confidence that AI recommendations serve their interests, not just paid placements; providing clear reasoning and easy override controls

3. **Complex Multi-Booking Flow**: Making 5-7 separate provider confirmations feel like one seamless purchase, managing partial confirmations gracefully

4. **Provider Onboarding Friction**: Enabling rural Sri Lankan business owners to create listings in under 15 minutes on mobile devices with minimal technical knowledge

5. **Review Integrity**: Encouraging authentic reviews without manipulation while maintaining the internal-first provider priority system's credibility

### Design Opportunities

1. **AI Companion Personality**: Create warm, local-feeling chat personality that differentiates from transactional competitors - like texting a knowledgeable Sri Lankan friend

2. **Progressive Provider Onboarding**: 5-minute basic listing → go live immediately → enhance over time; deliver value before asking for investment

3. **Visual Trip Timeline**: Map-based journey visualization showing daily locations, weather, booked activities - tourists SEE their adventure before living it

4. **Micro-Moments of Delight**: Contextual animations, countdowns, celebratory moments that create emotional connection (whale emoji for Mirissa booking, trip countdown, memory gallery)

5. **Review Timing Intelligence**: Context-aware review prompts at optimal moments (hotel checkout, activity completion) for fresher, more authentic feedback

## Core User Experience

### Defining Experience

The Travel Plan Web Application delivers two distinct but interconnected core experiences:

**Tourist Experience - "Dream to Reality"**
The core loop transforms vague travel desires into booked adventures through conversational AI. The defining interaction is the AI chat where tourists express natural language requests ("I want beaches and wildlife in February for $1500") and receive complete, visual trip plans. The experience prioritizes feeling like you're planning with a knowledgeable local friend, not querying a database.

**Provider Experience - "Effortless Revenue"**
The core loop is notification-driven booking management optimized for mobile. Providers receive booking requests, review essential details, and accept/decline with a single tap. The experience prioritizes minimal time investment for maximum business value—providers should spend seconds, not minutes, on platform interactions.

### Platform Strategy

| Aspect | Decision | Rationale |
|--------|----------|-----------|
| **Platform Type** | Responsive Web Application | Practical for university scope; accessible on all devices |
| **Primary Tourist Device** | Desktop for planning, Mobile for trip execution | Research happens at home; itinerary access needed on-the-go |
| **Primary Provider Device** | Mobile-first | Small business owners manage on-the-go |
| **Offline Capability** | Downloadable PDF itinerary | Simple solution for rural connectivity gaps |
| **Progressive Enhancement** | Core features work everywhere; rich features on capable devices | Ensures accessibility across device spectrum |

### Effortless Interactions

**Zero-Friction Tourist Actions:**
- Expressing travel desires through natural conversation
- Understanding trip plans through visual timeline (glanceable)
- Modifying recommendations with simple requests ("swap for a spa day")
- Booking entire trip with single action
- Accessing today's itinerary instantly on app open

**Zero-Friction Provider Actions:**
- Receiving booking notifications (push)
- Accepting/declining with single tap
- Viewing upcoming bookings at a glance
- Basic listing creation in under 5 minutes

### Critical Success Moments

| Moment | Description | Success Indicator |
|--------|-------------|-------------------|
| **AI First Response** | Tourist's initial interaction with AI companion | User continues conversation (not abandons) |
| **Plan Revelation** | AI presents complete visual trip plan | User explores details (not confused scroll) |
| **Booking Completion** | Multi-provider checkout in single flow | Confirmation without anxiety |
| **Provider First Booking** | First booking received through platform | Provider accepts and returns for more |
| **Post-Trip Review** | Review submission after trip completion | Completion rate and review quality |

### Experience Principles

1. **Conversation Over Configuration**: Never show tourists a form when a chat message would work. "Tell me what you want" beats "Fill out these 12 fields."

2. **Show, Don't List**: Visual timeline over bullet points. Map-based journey over text itinerary. Photos and weather icons over descriptions.

3. **One Action, Complete Result**: One "Book Trip" button, not 7 individual bookings. One chat message triggers a complete plan. One notification tap completes a provider action.

4. **Progressive Complexity**: Tourists see simple view first, details on demand. Providers start with basic listing, enhance over time. Advanced features discoverable, not mandatory.

5. **Trust Through Transparency**: Show WHY the AI recommended something. Display provider ratings prominently. Mark internal vs. external results clearly.

## Desired Emotional Response

### Primary Emotional Goals

**Tourist Experience: "Trusted Adventure Partner"**
Users should feel they have a knowledgeable, caring companion who understands them even when they're vague, protects them from bad decisions, surprises them with discoveries they didn't know to ask for, and supports them when things don't go as planned.

Target advocacy statement: "It felt like traveling with a local friend who knew everything."

**Provider Experience: "Professional Partnership"**
Providers should feel like valued business partners who look professional to customers, stay in control of their business decisions, get rewarded for quality service, and grow their business without technical headaches.

Target advocacy statement: "It's like having a marketing team that actually brings customers."

### Emotional Journey Mapping

**Tourist Journey:**
| Phase | Target Emotion | Key Feeling |
|-------|---------------|-------------|
| Discovery | Curious → Intrigued | "What's this?" |
| Planning | Excited → Delighted | "This is perfect for me" |
| Booking | Relieved → Confident | "It's all sorted" |
| Pre-Trip | Eager → Prepared | "I can't wait, and I'm ready" |
| During Trip | Guided → Adventurous | "I know exactly where to go" |
| Post-Trip | Grateful → Nostalgic | "That was amazing" |

**Provider Journey:**
| Phase | Target Emotion | Key Feeling |
|-------|---------------|-------------|
| Onboarding | Hopeful → Proud | "My business looks great" |
| Waiting | Patient → Optimistic | "This will work" |
| First Booking | Validated → Excited | "Real customers!" |
| Ongoing | Confident → Grateful | "This helps my business" |

### Micro-Emotions

**Critical Positive States to Create:**
- **Understood**: AI immediately grasps intent from natural language
- **Delighted Surprise**: Discoveries user didn't know to ask for
- **Confident**: Clear information, no hidden costs or confusion
- **Secure**: Booking process feels safe and reversible
- **Supported**: Help available when things go wrong
- **Guided**: Always know what's next during trip
- **Valued**: Providers feel like partners, not just inventory

**Critical Negative States to Prevent:**
- **Overwhelm**: Never show more than 3-5 options without request
- **Distrust**: Always show reasoning behind AI recommendations
- **Anxiety**: Provide real-time status and clear confirmations
- **Abandonment**: Always offer next step and human escalation
- **Frustration**: Graceful error handling with helpful messages

### Design Implications

| Emotional Goal | UX Design Approach |
|----------------|-------------------|
| Trust | Transparent AI reasoning ("I recommend this because..."), clear pricing, verified reviews |
| Excitement | Visual discovery (photos, maps), surprise recommendations, countdown to trip |
| Confidence | Progress indicators, confirmation summaries, downloadable proof |
| Control | Easy modifications, clear cancellation policies, provider accept/decline power |
| Support | Persistent help access, error recovery flows, human escalation option |
| Pride (Providers) | Professional listing templates, review showcasing, booking statistics |

### Emotional Design Principles

1. **Never Leave Users Hanging**: Every action gets immediate feedback. Every wait has a status. Every error has a next step.

2. **Surprise with Relevance, Not Volume**: One perfect recommendation beats ten good ones. Quality of understanding over quantity of options.

3. **Build Trust Through Transparency**: Show the "why" behind every AI recommendation. Never hide costs. Mark sponsored content clearly.

4. **Celebrate Milestones**: Trip booked = celebration moment. First booking received = provider celebration. Review submitted = gratitude shown.

5. **Protect Without Patronizing**: Guide users away from bad decisions (weather warnings, budget alerts) without making them feel stupid or restricted.

## UX Pattern Analysis & Inspiration

### Inspiring Products Analysis

**Primary Inspiration Sources:**

| App | Key Lessons | Apply To |
|-----|-------------|----------|
| **Airbnb** | Photo-first discovery, trust badges, transparent pricing, wishlists | Tourist discovery, provider listings, booking flow |
| **ChatGPT/Claude** | Natural language input, streaming responses, iterative refinement | AI companion chat interface |
| **Google Maps** | Map-centric thinking, location cards, journey visualization | Itinerary view, destination discovery |
| **Booking.com** | Multi-item checkout flow, availability calendars | Booking flow, provider availability |
| **Uber Driver** | Notification-driven, one-tap actions, earnings visibility | Provider booking management |
| **WhatsApp Business** | Familiar chat patterns, simple business tools | Provider communication, basic features |

### Transferable UX Patterns

**Navigation Patterns:**
- Map + List Toggle for destination discovery
- Tab bar navigation: Chat, Trips, Explore, Profile (tourist)
- Notification hub as primary screen (provider)
- Chat/AI conversation as home screen

**Interaction Patterns:**
- Streaming responses for AI chat (progressive reveal)
- Swipe or tap to accept/decline bookings
- Quick reply chips for suggested AI responses
- Photo carousel for provider galleries
- Expandable cards for summary → detail flow
- Bottom sheets for contextual details

**Visual Patterns:**
- Hero images with overlay text for destination/package cards
- Trust badges for verified providers
- Progress stepper for multi-item booking
- Timeline visualization for day-by-day itinerary
- Floating action button for primary action

### Anti-Patterns to Avoid

| Avoid | Because | Alternative |
|-------|---------|-------------|
| Hidden fees at checkout | Destroys trust at conversion moment | Show total price from first view |
| Fake urgency messaging | Creates anxiety, users see through it | Show real availability honestly |
| Information overload | Causes decision paralysis | Curated top 3-5 options with "show more" |
| Complex onboarding forms | Providers abandon before seeing value | 5-minute basic listing, enhance later |
| Buried reviews/ratings | Trust not visible when needed | Show ratings on every provider card |
| Form-heavy tourist input | Friction reduces conversions | Chat-first, forms only as fallback |
| No feedback during waits | Users think app is broken | Always show progress/status indicators |
| Desktop-only provider tools | Rural providers are mobile-first | Design mobile-first, enhance for desktop |

### Design Inspiration Strategy

**Adopt Directly:**
- Chat-first planning interface (AI companion core experience)
- Photo-first provider listings (trust and emotional connection)
- One-tap accept/decline for providers (minimum friction)
- Transparent total pricing (trust principle)
- Map-based itinerary visualization (visual journey understanding)
- Streaming AI responses (alive feeling, reduced perceived wait)

**Adapt for Our Context:**
- Airbnb wishlists → "Saved Trip Ideas" (save AI-generated plans)
- Uber earnings dashboard → Simplified "Provider Dashboard" (bookings + reviews focus)
- Google Maps offline → Downloadable PDF itinerary (simpler university scope)
- ChatGPT conversations → Quick reply chips (guide users to next steps)

**Explicitly Reject:**
- Hidden fees (violates trust principle)
- Fake urgency tactics (conflicts with "trusted friend" emotional goal)
- Information overload (contradicts "show, don't list" principle)
- Complex provider registration (prevents rural Sri Lankan adoption)
- Desktop-only features (providers are mobile-first)

## Design System Foundation

### Design System Choice

**Stack:** Next.js 14+ with Tailwind CSS and shadcn/ui component library

**Supporting Libraries:**
- **shadcn/ui** - Accessible, customizable components built on Radix UI primitives
- **Framer Motion** - Animations for chat streaming, page transitions, micro-interactions
- **Lucide React** - Consistent iconography
- **Mapbox GL or Leaflet** - Interactive itinerary maps
- **Recharts** - Provider dashboard visualizations

### Rationale for Selection

1. **Full Design Control**: Tailwind + shadcn/ui provides complete control over visual design without fighting component library opinions—essential for creating the unique "AI companion" feel

2. **Two Experiences, One System**: Route groups in Next.js App Router (`(tourist)` and `(provider)`) with shared Tailwind config enables distinct experiences from unified components

3. **Mobile-First Native**: Tailwind's responsive utilities (`sm:`, `md:`, `lg:`) make mobile-first provider experience trivial to implement

4. **Performance Optimized**: Next.js App Router with Server Components + Tailwind's purging = minimal bundle size, fast loads

5. **Developer Experience**: shadcn/ui components are copy-pasted into your codebase—you own them completely, no dependency lock-in

6. **University Timeline Friendly**: Pre-built accessible components accelerate development while maintaining customization freedom

### Implementation Approach

**Phase 1: Foundation Setup**
- Initialize Next.js 14+ with App Router and TypeScript
- Configure Tailwind CSS with design tokens (colors, typography, spacing)
- Install and configure shadcn/ui CLI
- Set up route groups for tourist/provider/admin experiences

**Phase 2: Core Components**
- Build chat interface components (StreamingText, ChatBubble, QuickReplyChips)
- Create shared components (ProviderCard, RatingStars, PhotoCarousel)
- Develop booking flow components (TripPlanCard, CheckoutStepper)

**Phase 3: Experience-Specific**
- Tourist: Chat home, trip visualization, itinerary timeline
- Provider: Notification cards, accept/decline, dashboard
- Admin: Event management, package builder

### Customization Strategy

**Design Tokens (tailwind.config.js):**
- Brand colors with semantic naming (brand-primary, status-success)
- Experience-specific palettes (tourist-bg, provider-card)
- Typography scale for display, body, and chat text
- Consistent spacing and border-radius tokens

**Component Theming:**
- Base shadcn/ui components extended with project-specific variants
- Consistent animation patterns via Framer Motion presets
- Dark mode support via Tailwind's `dark:` variant (future consideration)

**Responsive Strategy:**
- Mobile-first default (base styles = mobile)
- Tablet breakpoint (md:) for enhanced tourist planning
- Desktop breakpoint (lg:) for admin dashboards

### Component Architecture

```
src/
├── components/
│   ├── ui/                    # shadcn/ui base components
│   ├── chat/                  # AI Chat components
│   │   ├── ChatContainer.tsx
│   │   ├── ChatMessage.tsx
│   │   ├── ChatInput.tsx
│   │   ├── QuickReplyChips.tsx
│   │   └── StreamingText.tsx
│   ├── booking/               # Booking flow components
│   │   ├── TripPlanCard.tsx
│   │   ├── BookingSummary.tsx
│   │   ├── ProviderCard.tsx
│   │   └── CheckoutStepper.tsx
│   ├── itinerary/             # Trip visualization
│   │   ├── TimelineView.tsx
│   │   ├── DayCard.tsx
│   │   ├── MapView.tsx
│   │   └── WeatherBadge.tsx
│   ├── provider/              # Provider dashboard
│   │   ├── BookingNotification.tsx
│   │   ├── AcceptDeclineCard.tsx
│   │   ├── EarningsSummary.tsx
│   │   └── CalendarView.tsx
│   └── shared/                # Shared across experiences
│       ├── PhotoCarousel.tsx
│       ├── RatingStars.tsx
│       ├── TrustBadge.tsx
│       └── PriceDisplay.tsx
├── app/
│   ├── (tourist)/             # Tourist routes
│   ├── (provider)/            # Provider routes
│   └── (admin)/               # Admin routes
```

## Defining User Experience

### Tourist Defining Experience

**One-Sentence Description:** "Tell it what you want, and it plans your whole trip."

**User Story:** Maria tells her friend: "I just said I wanted beaches and wildlife for $1500, and it gave me a complete 7-day trip with hotels, safari, whale watching—everything. One button to book it all."

**Experience Flow:**

| Stage | What Happens | User Feels |
|-------|--------------|------------|
| **Initiation** | Warm AI greeting with quick-start chips (Beach, Safari, Culture, Nature) + open text input | Welcomed, not overwhelmed |
| **Conversation** | Natural back-and-forth; AI asks clarifying questions with quick reply options | Understood, like talking to a friend |
| **Plan Build** | Progressive streaming as AI assembles trip day-by-day | Anticipation, watching magic happen |
| **Plan Reveal** | Map visualization + cost breakdown + budget comparison | Excitement, "I can SEE my adventure" |
| **Modification** | Chat-based changes ("swap day 3 for a spa day") | In control, easy adjustments |
| **Booking** | Review summary → single "Book Everything" button | Confident, no hidden surprises |
| **Confirmation** | Celebration animation + countdown to trip | Accomplished, can't wait |

**Success Criteria:**
- Time from first message to complete plan: < 5 minutes
- Plan matches stated preferences and budget
- User explores plan details (doesn't just scroll past)
- Booking completion rate > 60% for users who see a plan
- Users share/save plans even before booking

### Provider Defining Experience

**One-Sentence Description:** "Phone buzzes, tap accept, money coming."

**User Story:** Saman tells fellow hotel owners: "I get a notification, see the booking details, tap accept. That's it. Tourists just show up."

**Experience Flow:**

| Stage | What Happens | Provider Feels |
|-------|--------------|----------------|
| **Notification** | Push notification with key details (guest, dates, amount) | Opportunity arrived |
| **Review** | Rich card showing guest info, dates, payout, guest note | Informed, in control |
| **Decision** | Large Accept/Decline buttons, clear payout amount | Empowered, simple choice |
| **Confirmation** | Instant feedback, calendar updated, payout scheduled | Satisfied, business growing |

**Success Criteria:**
- Response time from notification to accept/decline: < 30 seconds possible
- All information needed for decision visible on one screen
- Accept rate > 80% for available dates
- Provider returns to platform within 48 hours of first booking

### Novel UX Patterns

**What's Innovative:**
1. **Chat-to-Itinerary Pipeline**: Natural language → structured trip plan is novel for travel booking (combines ChatGPT conversational model with Airbnb booking model)
2. **Streaming Plan Build**: Watching the trip assemble creates engagement that static results don't
3. **Multi-Provider Single Checkout**: Booking 5-7 providers in one action is unusual—most platforms require separate bookings

**What's Familiar (Adopted):**
1. Chat interface patterns (WhatsApp, ChatGPT)
2. Quick reply chips (Google Messages, chatbots)
3. Card-based booking notifications (Uber Driver, Airbnb Host)
4. Map-based journey visualization (Google Maps)
5. Photo carousels (Airbnb, Instagram)

**Learning Curve Mitigation:**
- Quick-start chips prevent blank page anxiety
- Progressive disclosure (simple first, details on demand)
- Familiar visual patterns (cards, maps, timelines)
- AI guides users through novel flow conversationally

### Experience Mechanics Detail

**Tourist - Initiation:**
- Entry point: Landing page with prominent chat interface
- First message: AI greeting + themed quick-start chips
- No login required to start (capture email at booking)
- Conversation history persists for returning users

**Tourist - Interaction:**
- Natural language input with suggested quick replies
- AI asks focused clarifying questions (max 3 before showing plan)
- Streaming responses create "thinking" feel
- Each AI message ends with clear next step

**Tourist - Feedback:**
- Plan builds progressively (streaming effect)
- Map visualization shows route
- Cost breakdown with budget comparison
- "Why I chose this" explanations on provider cards

**Tourist - Completion:**
- Review summary with all bookings listed
- Single "Book Everything" action
- Celebration moment with confetti/animation
- Immediate access to itinerary + PDF download

**Provider - Notification:**
- Push notification with essential details
- Badge count on app icon
- Sound/vibration for new bookings
- Email backup for offline providers

**Provider - Review:**
- Single-screen booking card
- Guest photo, rating, trip history
- Clear payout amount (after fees)
- Guest's note/special requests visible

**Provider - Action:**
- Large, thumb-friendly Accept/Decline buttons
- Optional decline reason (helps platform)
- Auto-decline timer (24 hours) prevents ghosting
- Swipe gestures as alternative to buttons

**Provider - Confirmation:**
- Instant visual confirmation
- Calendar automatically updated
- Payout timeline shown
- Quick access to upcoming bookings

## Visual Design Foundation

### Color System

**Brand Palette:**

| Color | Hex | Tailwind | Usage |
|-------|-----|----------|-------|
| **Ocean Teal** | #0D9488 | teal-600 | Primary actions, links, user chat bubbles |
| **Sunset Coral** | #F97316 | orange-500 | CTAs, highlights, ratings, excitement |
| **Jungle Green** | #059669 | emerald-600 | Success states, nature, provider earnings |

**Semantic Colors:**
- Success: emerald-500 (#10B981) - Booking confirmed
- Warning: amber-500 (#F59E0B) - Budget alerts, low availability
- Error: red-500 (#EF4444) - Booking failed, payment errors
- Info: blue-500 (#3B82F6) - Tips, information
- Pending: violet-500 (#8B5CF6) - Awaiting confirmation

**Experience-Specific:**
- Tourist background: teal-50 (#F0FDFA) - Dreamy, aspirational feel
- Provider background: neutral-50 (#FAFAFA) - Business-like, clean
- AI chat bubble: slate-100 (#F1F5F9) - Neutral, content-focused
- User chat bubble: teal-600 (#0D9488) - Ownership, "my message"

### Typography System

**Font Stack:**
- Display: Plus Jakarta Sans (headlines, hero text, AI greeting)
- Body: Inter (UI text, body copy, chat messages)
- Fallback: System font stack

**Type Scale:**

| Element | Size | Weight | Font |
|---------|------|--------|------|
| Hero headline | 4xl-5xl (36-48px) | Bold | Display |
| Page title | 3xl (30px) | Semibold | Display |
| Section header | 2xl (24px) | Semibold | Display |
| Card title | xl (20px) | Semibold | Body |
| Body text | base (16px) | Regular | Body |
| Chat messages | base (16px) | Regular/Medium | Body |
| Buttons | sm-base (14-16px) | Semibold | Body |
| Captions | sm (14px) | Medium | Body |
| Metadata | xs (12px) | Regular | Body |

### Spacing & Layout Foundation

**Spacing System:** 8px base unit (Tailwind default scale)
- Tight: 8px (gap-2)
- Standard: 16px (gap-4, p-4)
- Comfortable: 24px (gap-6, p-6)
- Spacious: 32-48px (gap-8, py-12)

**Layout Breakpoints:**
- Mobile: < 768px (single column, 16px padding)
- Tablet: 768-1024px (max-width 768px, 24px padding)
- Desktop: > 1024px (max-width 1280px, multi-column)

**Border Radius Scale:**
- Buttons/inputs: 8px (rounded)
- Cards: 12px (rounded-md)
- Chat bubbles: 24px (rounded-xl)
- Avatars: full (rounded-full)

**Shadows:**
- Cards: shadow-sm (subtle elevation)
- Modals: shadow-lg (prominent elevation)
- Dropdowns: shadow-md (medium elevation)

### Accessibility Considerations

**Color Contrast:**
- All text meets WCAG AA (4.5:1 for normal text, 3:1 for large text)
- Orange-600 used instead of orange-500 for small text on white
- Focus indicators: 2px ring with offset

**Interactive Elements:**
- Minimum touch target: 44x44px
- Visible focus states on all interactive elements
- Color never sole indicator of state (always paired with icon/text)

**Motion & Animation:**
- Respects prefers-reduced-motion
- Subtle transitions (150-300ms)
- No auto-playing animations that can't be paused

**Text & Readability:**
- Base font size: 16px (scalable to 200%)
- Line height: 1.5 for body text
- Maximum line length: ~75 characters for readability
