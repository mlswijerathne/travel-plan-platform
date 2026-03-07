# How to Run the Application

## Prerequisites

| Tool           | Version | Verify Command     |
|----------------|---------|-------------------|
| Java (JDK)     | 21+     | `java -version`   |
| Maven          | 3.9+    | `mvn -version`    |
| Node.js        | 18+     | `node -v`         |
| pnpm           | 8+      | `pnpm -v`         |
| Docker Desktop | Any     | `docker --version` (must be running) |
| Git            | Any     | `git --version`   |

---

## 1. Clone the Repository

```bash
git clone <repository-url>
cd travel-plan-platform
```

---

## 2. Backend Setup

### 2.1 Configure Environment Variables

```bash
cd backend
cp .env.example .env
```

Edit `backend/.env` and fill in the actual values for:

- **Supabase**: `SUPABASE_URL`, `SUPABASE_JWT_SECRET`
- **Database credentials** (per service): `*_DB_URL`, `*_DB_USERNAME`, `*_DB_PASSWORD` (PostgreSQL on Neon)
- **Azure Blob Storage**: `AZURE_STORAGE_CONNECTION_STRING`
- **AI keys**: `OPENAI_API_KEY`, `GEMINI_API_KEY`

### 2.2 Build All Services

```bash
cd backend
mvn clean install -DskipTests
```

This builds the shared `common-lib` and compiles all microservices.

### 2.3 Start All Backend Services

```bash
./run-all.sh
```

This single command will:
1. Check all prerequisites
2. Build `common-lib`
3. Start Kafka via Docker Compose
4. Start Eureka Discovery Server and API Gateway
5. Start all microservices
6. Run health checks and display status

**Alternative options:**
```bash
./run-all.sh --no-kafka     # Skip Kafka (core services only)
./run-all.sh --build-only   # Build without starting
```

### 2.4 Verify Backend is Running

Once started, you should see all services showing `[UP]`:

| Service            | Port | URL                    |
|--------------------|------|------------------------|
| Eureka Dashboard   | 8761 | http://localhost:8761  |
| API Gateway        | 8060 | http://localhost:8060  |
| Kafka UI           | 8080 | http://localhost:8080  |
| Tourist Service    | 8082 | http://localhost:8082  |
| Hotel Service      | 8083 | http://localhost:8083  |
| Tour Guide Service | 8084 | http://localhost:8084  |
| Vehicle Service    | 8085 | http://localhost:8085  |
| Booking Service    | 8086 | http://localhost:8086  |
| Itinerary Service  | 8087 | http://localhost:8087  |
| Review Service     | 8088 | http://localhost:8088  |
| Trip Plan Service  | 8089 | http://localhost:8089  |
| Event Service      | 8090 | http://localhost:8090  |
| Ecommerce Service  | 8091 | http://localhost:8091  |
| AI Agent Service   | 8093 | http://localhost:8093  |

### 2.5 Stop All Backend Services

```bash
./stop-all.sh
# or press Ctrl+C in the terminal running run-all.sh
```

---

## 3. Frontend Setup

### 3.1 Configure Environment Variables

```bash
cd frontend
cp .env.example .env.local
```

Edit `frontend/.env.local` and set:

```
NEXT_PUBLIC_API_BASE_URL=http://localhost:8060
NEXT_PUBLIC_SUPABASE_URL=<your-supabase-url>
NEXT_PUBLIC_SUPABASE_ANON_KEY=<your-supabase-anon-key>
```

### 3.2 Install Dependencies

```bash
pnpm install
```

### 3.3 Start the Frontend

```bash
pnpm dev
```

The frontend will be available at **http://localhost:3000**.

---

## 4. Quick Start Summary

Open **two terminals** and run:

**Terminal 1 - Backend:**
```bash
cd backend
./run-all.sh
```

**Terminal 2 - Frontend:**
```bash
cd frontend
pnpm install
pnpm dev
```

Then open **http://localhost:3000** in your browser.

---

## Troubleshooting

| Problem | Solution |
|---------|----------|
| `DataSource 'url' attribute not specified` | `.env` file is missing or incomplete in `backend/` |
| `Connection attempt failed` / `SocketTimeoutException` | Network blocking port 5432 - use mobile hotspot |
| Maven build failure | Run `mvn clean install -DskipTests` to see the exact error |
| Service crashes on startup | Check logs: `tail -50 backend/logs/<service-name>.log` |
