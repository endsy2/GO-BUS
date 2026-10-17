# Go Bus Express

A scalable, three-tier **microservices platform for online bus booking and payments**, built for
the Cambodian market. Customers browse routes, reserve seats in real time, and pay via in-app
digital wallet or **Bakong KHQR** — Cambodia's national QR payment standard — while operators manage
buses, schedules, bookings, and finances through a web admin dashboard.

Developed as a final-year thesis project to demonstrate microservices architecture, real-time
concurrency control, and resilient payment integration under load.

---

## Highlights

- **API Gateway pattern** — all clients talk only to a single gateway (`:8080`) that handles
  JWT (RS256) authentication, routing, rate limiting, and circuit breakers. No client calls a
  microservice directly.
- **Real-time seat booking** — concurrent seat reservation via database pessimistic locking and an
  `AVAILABLE → PENDING → BOOKED` state machine (PENDING auto-expires after 5 minutes), broadcast to
  all connected clients over STOMP-over-SockJS WebSockets.
- **Bakong KHQR payments** — integrated with Cambodia's national QR payment system, with a dedicated
  circuit breaker and extended timeout to tolerate up to 5-minute payment polling.
- **Event-driven notifications** — asynchronous in-app notifications via RabbitMQ
  (`notification.exchange` → User Service).
- **Resilient & observable** — Resilience4j circuit breakers, Eureka service discovery, Redis
  caching/token blacklisting, MinIO object storage, and Spring Boot Actuator health checks.

---

## Architecture

Three clients → one API Gateway → independently deployable microservices, each owning its own
PostgreSQL schema.

```
Mobile App (Flutter)          Admin Dashboard (React)
        │                               │
        └───────────┬───────────────────┘
                    ▼
             API Gateway :8080
        (JWT auth · routing · circuit breaker)
                    │
      ┌─────────────┼──────────────┐
      ▼             ▼              ▼
 User Service   Bus Service   Booking Service
   :8081          :8082           :8083
   user_db        bus_db        booking_db
      └─────────────┼──────────────┘
                    │
    PostgreSQL · Redis · RabbitMQ · MinIO
             (Eureka discovery :8761)
```

**Authentication.** RS256 JWT. The User Service issues tokens; the Gateway validates them with the
public key only and injects `X-User-Id`, `X-User-Roles`, and `X-User-Permissions` headers into
downstream requests. Downstream services trust those headers and never re-verify the JWT.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 17, Spring Boot 3.2, Spring Cloud Gateway, OpenFeign, Resilience4j, Eureka |
| Admin Web | React 19, Tailwind CSS, Radix UI |
| Mobile | Flutter 3.8 / Dart 3.8 (GetX, Dio, Hive) |
| Payments | Bakong KHQR SDK, in-app digital wallet |
| Data & Messaging | PostgreSQL, Redis, RabbitMQ, MinIO |
| Infrastructure | Docker / Docker Compose — deployable to Railway or a self-hosted VPS |

---

## Repository Structure

| Path | Description |
|---|---|
| `backend/` | Five Spring Boot services — Registry (Eureka), Gateway, User, Bus, Booking |
| `admin frontend/go-bus-admin/` | React admin dashboard |
| `payment-with-bakong/` | Standalone FastAPI Bakong KHQR payment service |
| `go-bus-mobile-app/` | Flutter customer app *(separate repository — see its own README)* |

### Backend services

| Service | Directory | Port | Responsibility |
|---|---|---|---|
| Registry (Eureka) | `go-bus-registry-service/` | 8761 | Service discovery |
| Gateway | `go-bus-gateway-service/` | 8080 | JWT auth, routing, rate limiting, circuit breakers |
| User | `go-bus-user-service/` | 8081 | Auth, users, wallets, top-ups, notifications, profile images |
| Bus | `go-bus-bus-service/` | 8082 | Buses, routes, layouts, schedules, seats |
| Booking | `go-bus-booking-service/` | 8083 | Bookings, payments, tickets, refunds, promos, reports |

---

## Environments

There are two environments. The code is the same in both — only the configuration changes.

| | Development | Production |
|---|---|---|
| Spring profile | `dev` (the default — nothing to set) | `prod` (`SPRING_PROFILES_ACTIVE=prod`) |
| Compose file | `docker-compose.dev.yml` | `docker-compose.prod.yml` |
| Env file | `.env.dev` (copy of `.env.dev.example`) | `.env.prod` (copy of `.env.prod.example`) |
| Secrets | Safe dev defaults built in | Required — the stack refuses to start without them |
| Exposed ports | Everything on `127.0.0.1`; gateway on all interfaces | Only nginx `:80` is public |
| Logs | Readable text, DEBUG, SQL shown | JSON, INFO, no SQL |
| Admin dashboard | `npm start` (reads `.env.development`) | Built into an image, served behind nginx |

### How the configuration is organised

Each backend service has three files in `src/main/resources/`:

| File | Contains |
|---|---|
| `application.yml` | Settings shared by every environment (pools, routes, timeouts). No secrets. |
| `application-dev.yml` | Local defaults: `localhost`, dev passwords, a **dev-only** JWT key pair. |
| `application-prod.yml` | Only `${VARIABLE}` references. A variable without a default is **required**. |

Spring loads `application.yml` first, then the active profile's file on top of it. Any value can
also be overridden by an environment variable of the same name.

### Development

```bash
cp .env.dev.example .env.dev
```

**Option A — infrastructure in Docker, services in your IDE** (fastest edit → run loop):

```bash
docker compose -f docker-compose.dev.yml --env-file .env.dev up -d postgres redis rabbitmq minio
cd backend/go-bus-registry-service && ./gradlew bootRun   # then user, bus, booking — gateway last
```

No profile flag or env vars are needed: `dev` is the default and points at `localhost`.

**Option B — the whole backend in Docker:**

```bash
docker compose -f docker-compose.dev.yml --env-file .env.dev up -d --build
```

**Admin dashboard** (either option):

```bash
cd "admin frontend/go-bus-admin"
npm install
npm start
```

| Dev URL | |
|---|---|
| Admin dashboard | http://localhost:3000 — `admin@busapp.com` / `admin123` (dev only) |
| API gateway | http://localhost:8080 |
| Eureka | http://localhost:8761 |
| RabbitMQ UI | http://localhost:15672 — `guest` / `guest` |
| MinIO console | http://localhost:9001 — `minioadmin` / `minioadmin` |
| RedisInsight | http://localhost:5540 |

### Production

```bash
cp .env.prod.example .env.prod
./backend/generate-keys.sh           # paste JWT_PRIVATE_KEY and JWT_PUBLIC_KEY into .env.prod
# fill in every REQUIRED value; set REACT_APP_* to your server's IP or domain
docker compose -f docker-compose.prod.yml --env-file .env.prod up -d --build
docker compose -f docker-compose.prod.yml ps   # wait until every service is "healthy"
```

Then open `http://<your-server-ip>/`. If a required value is missing, compose stops before
starting anything, e.g. `required variable JWT_PRIVATE_KEY is missing a value`.

**Never reuse the development JWT keys or passwords in production** — they are public in git, so
anyone could forge an admin token.

**Railway / other hosts.** Every Docker image already defaults to the `prod` profile. Set the
variables that the service's `application-prod.yml` references. Variables renamed by the
environment split:

| Old name | New name |
|---|---|
| `Public_Key` (gateway) | `JWT_PUBLIC_KEY` |
| `REGISTRY_SERVICE_URL`, `EUREKA_BASE_URL`, `EUREKA_HOST` + `EUREKA_PORT` | `EUREKA_URL` (full URL ending in `/eureka/`) |
| `BUS-SERVICE-URL`, `BUS_SERVICE_BASE_URL` (booking) | `BUS_SERVICE_URL` |
| `USER-SERVICE-URL`, `USER_SERVICE_BASE_URL` (booking) | `USER_SERVICE_URL` |
| `BOOKING_SERVICE_HOST` + `BOOKING_SERVICE_PORT` | `BOOKING_SERVICE_URL` |
| `RABBITMQ_URL` (booking) | `SPRING_RABBITMQ_ADDRESSES` |
| `POLL-INTERVAL`, `TIME-OUT` | `BAKONG_POLLING_INTERVAL_MS`, `BAKONG_CONNECTION_TIMEOUT_MS` |

New: `ADMIN_DEFAULT_PASSWORD` (user-service). `JWT_PRIVATE_KEY` / `JWT_PUBLIC_KEY` are now actually
read by user-service (they used to be hard-coded).

### Bakong Payment Service (optional, standalone)

```bash
cd payment-with-bakong
python -m venv venv && source venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --reload
```

---

## Testing

```bash
# Backend
cd backend/go-bus-user-service && ./gradlew test

# Admin dashboard
cd "admin frontend/go-bus-admin" && npm test
```

API collections for Postman are in `backend/` (`BusApp Microservices v4.postman_collection.json`).

**Load testing.** JMeter test plans (`GoBus-StressTest.jmx`, `GoBus-Bakong-StressTest.jmx`,
`login-stress-test.jmx`) and guides live at the repository root; `run-stress.ps1` drives a run.

---

## Deployment

- **Railway** (primary) — each service deployed independently with the `prod` profile. See
  [Environments › Production](#production) for the variables.
- **Self-hosted VPS** — single-host Docker Compose via `docker-compose.prod.yml` (backend services +
  admin frontend behind an Nginx reverse proxy). See [Environments › Production](#production).

Deployment order: PostgreSQL / Redis / RabbitMQ / MinIO → Eureka → User / Bus / Booking → Gateway → Frontend.

---

## Documentation

- `PROJECT_OVERVIEW_FOR_THESIS.md` — long-form architecture and design write-up.
- Each sub-project has its own `CLAUDE.md` with build commands and architecture notes.

---

## License

Developed for academic purposes as a final-year thesis project.
