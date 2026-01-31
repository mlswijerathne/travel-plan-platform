# Travel Plan Platform

AI-powered travel planning platform for Sri Lanka tourism with a provider marketplace.

## Project Structure

```
travel-plan-platform/
├── backend/          # Spring Boot Microservices (Java 21)
│   ├── common-lib/   # Shared DTOs, exceptions, configs
│   ├── tourist-service/
│   ├── hotel-service/
│   ├── tour-guide-service/
│   ├── vehicle-service/
│   ├── booking-service/
│   ├── itinerary-service/
│   ├── review-service/
│   ├── trip-plan-service/
│   ├── event-service/
│   ├── ecommerce-service/
│   └── ai-agent-service/
└── frontend/         # Next.js 16 (TypeScript)
```

## Tech Stack

### Backend
- Java 21
- Spring Boot 3.5.x
- Spring Data JPA + PostgreSQL
- Spring Cloud OpenFeign
- Amazon SQS

### Frontend
- Next.js 16 (App Router)
- TypeScript
- Tailwind CSS 4.x
- shadcn/ui
- TanStack Query

### Infrastructure
- Supabase (Auth + PostgreSQL)
- AWS ECS Fargate
- AWS Cloud Map
- Amazon SQS / SES
- Docker

## Getting Started

### Prerequisites
- Java 21
- Node.js 22+
- Docker & Docker Compose
- Maven 3.9+

### Backend Setup
```bash
cd backend
mvn clean install
docker-compose up -d
```

### Frontend Setup
```bash
cd frontend
npm install
npm run dev
```

## Services

| Service | Port | Description |
|---------|------|-------------|
| tourist-service | 8082 | User profiles, preferences |
| hotel-service | 8083 | Hotel CRUD, availability |
| tour-guide-service | 8084 | Guide management |
| vehicle-service | 8085 | Vehicle management |
| booking-service | 8086 | Booking orchestration |
| itinerary-service | 8087 | Trip lifecycle |
| review-service | 8088 | Reviews & ratings |
| trip-plan-service | 8089 | Packages & bundles |
| event-service | 8090 | Events CRUD |
| ecommerce-service | 8091 | Products & orders |
| ai-agent-service | 8093 | AI recommendations |

## License

MIT
