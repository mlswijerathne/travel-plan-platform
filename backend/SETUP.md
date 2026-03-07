# Travel Plan Platform - Setup Guide for New Computer

## Prerequisites

| Tool | Version | Check |
|------|---------|-------|
| Java | 21+ | `java -version` |
| Maven | 3.9+ | `mvn -version` |
| Docker Desktop | Any | Must be running |
| Git | Any | `git --version` |
| curl | Any | `curl --version` |

---

## Step 1: Clone the Repository

```bash
git clone <repo-url>
cd travel-plan-platform/backend
git checkout dev
```

---

## Step 2: Create the `.env` File

The `.env` file is **not in git** (it contains secrets). You must get it from a teammate.

Copy the shared `.env` file into the `backend/` directory:

```
backend/
  .env          <-- place it here
  run-all.sh
  pom.xml
  ...
```

Verify it is configured correctly:

```bash
./setup-env.sh --check
```

All required variables should show `[OK]`.

---

## Step 3: Check Your Network

The databases are hosted on **Neon (PostgreSQL on AWS)**. They require outbound connections on **port 5432**.

**University / corporate / hotel WiFi often blocks port 5432.**

Test your connection:

```bash
# Load env vars first
source .env

# Test tourist DB connection
timeout 5 bash -c "echo > /dev/tcp/ep-still-tree-aixr9ica.c-4.us-east-1.aws.neon.tech/5432" && echo "PORT OPEN" || echo "PORT BLOCKED - use mobile hotspot"
```

If you see `PORT BLOCKED`, switch to **mobile hotspot** before continuing.

---

## Step 4: Build All Services First

Run a full build before starting services. This installs `common-lib` and compiles everything, showing any real errors:

```bash
cd backend
mvn clean install -DskipTests
```

If this fails, read the error output — it will show the exact file and line with the compilation error.

---

## Step 5: Start Everything

```bash
./run-all.sh
```

Expected result — all services UP:

```
Eureka Dashboard       [UP]
API Gateway            [UP]
Kafka UI               [UP]
tourist-service        [UP]
vehicle-service        [UP]
trip-plan-service      [UP]
event-service          [UP]
ecommerce-service      [UP]
ai-agent-service       [UP]
hotel-service          [UP]
tour-guide-service     [UP]
booking-service        [UP]
itinerary-service      [UP]
review-service         [UP]
```

---

## Troubleshooting

### Error: `Failed to configure a DataSource: 'url' attribute is not specified`

**Cause:** `.env` file is missing or not loaded.

**Fix:**
1. Make sure `backend/.env` exists
2. Make sure it contains all `*_DB_URL`, `*_DB_USERNAME`, `*_DB_PASSWORD` values
3. Run `./setup-env.sh --check` to validate

---

### Error: `The connection attempt failed` / `SocketTimeoutException: Read timed out`

**Cause:** Your network is blocking outbound port 5432 (PostgreSQL).

**Fix:** Switch to **mobile hotspot** and re-run `./run-all.sh`.

The Neon databases are confirmed running — this is always a network issue.

---

### Error: `MojoExecutionException` (Maven build failure)

**Cause:** Compilation error in one of the services.

**Fix:** Run the full build to see the real error:

```bash
mvn clean install -DskipTests
```

Or compile a single failing service:

```bash
# Replace vehicle-service with whichever service is failing
mvn -f vehicle-service/pom.xml compile
```

The output will show the exact file and line causing the failure.

Common sub-causes:
- `common-lib` not installed in local Maven repo — fixed by `mvn clean install -DskipTests`
- Code change not yet pulled — run `git pull origin dev`
- Wrong Java version — must be Java 21 (`java -version`)

---

### Services start but crash immediately

Check the log for the specific service:

```bash
# Replace tourist-service with the failing service name
tail -50 logs/tourist-service.log
```

---

## Port Reference

| Service | Port |
|---------|------|
| Eureka | 8761 |
| API Gateway | 8060 |
| Kafka UI | 8080 |
| tourist-service | 8082 |
| hotel-service | 8083 |
| tour-guide-service | 8084 |
| vehicle-service | 8085 |
| booking-service | 8086 |
| itinerary-service | 8087 |
| review-service | 8088 |
| trip-plan-service | 8089 |
| event-service | 8090 |
| ecommerce-service | 8091 |
| ai-agent-service | 8093 |

---

## Stopping All Services

```bash
./stop-all.sh
# or press Ctrl+C in the terminal running run-all.sh
```
