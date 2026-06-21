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

## Getting Started

### Backend (Docker — recommended)

```bash
cd backend
cp .env.example .env
./generate-keys.sh        # generates RS256 key pair — paste output into .env
docker compose up --build
```

This starts all five services plus PostgreSQL, Redis, RabbitMQ, and MinIO.

**Run a single service locally** (with infra already up):

```bash
cd backend/go-bus-user-service
./gradlew bootRun
```

Start order when running without Docker: **Registry → User / Bus / Booking → Gateway**.

### Admin Dashboard

```bash
cd "admin frontend/go-bus-admin"
npm install
npm start          # dev server
npm run build      # production build
```

Required `.env`:

```
REACT_APP_BASE_URL=https://<your-gateway-host>
REACT_APP_WS_URL=https://<your-gateway-host>/bus-service/ws/bus
```

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

- **Railway** (primary) — each service deployed independently. See `RAILWAY_DEPLOYMENT_GUIDE.md`.
- **Self-hosted VPS** — single-host Docker Compose via `docker-compose.vps.yml` (backend services +
  admin frontend behind an Nginx reverse proxy). See `DEPLOY_VPS.md`.

Deployment order: PostgreSQL / Redis / RabbitMQ / MinIO → Eureka → User / Bus / Booking → Gateway → Frontend.

---

## Documentation

- `PROJECT_OVERVIEW_FOR_THESIS.md` — long-form architecture and design write-up.
- `CLAUDE.md` — developer/contributor guide to the codebase and conventions.
- Each sub-project has its own `CLAUDE.md` with build commands and architecture notes.

---

## License

Developed for academic purposes as a final-year thesis project.
