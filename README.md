# 🏥 Patient Management System

A production-style **microservices** backend for managing patients, built with Spring Boot. Features an API Gateway, gRPC inter-service communication, Kafka event streaming, and PostgreSQL persistence.

---

## 🏗️ Architecture

```
Client
  │
  ▼
API Gateway (port 4004)
  │
  ├──► Patient Service (port 4000)  ──gRPC──► Billing Service (port 4001 / gRPC 9001)
  │              │
  │           Kafka
  │              │
  │              ▼
  │       Analytics Service
  │
  └──► PostgreSQL Database
```

---

## 🧩 Services

| Service | Port | Responsibility |
|---|---|---|
| `api-gateway` | 4004 | Routes all incoming HTTP requests |
| `patient-service` | 4000 | Core CRUD for patients |
| `billing-service` | 4001 (HTTP) / 9001 (gRPC) | Creates billing accounts via gRPC |
| `analytics-service` | — | Consumes Kafka events for analytics |

---

## 🛠️ Tech Stack

- **Spring Boot 4.x** — core framework
- **Spring Cloud Gateway (WebFlux)** — API gateway & routing
- **gRPC + Protobuf** — Patient Service → Billing Service communication
- **Apache Kafka** — event streaming for patient events
- **PostgreSQL** — primary database
- **Spring Data JPA** — ORM
- **SpringDoc OpenAPI** — auto-generated API docs
- **Spring Boot Actuator** — health checks & gateway inspection
- **Docker** — containerisation

---

## 📡 API Endpoints

All requests go through the API Gateway at `http://localhost:4004`

### Patients

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/patients` | Get all patients |
| `POST` | `/api/patients` | Create a new patient |
| `PUT` | `/api/patients/{id}` | Update a patient |
| `DELETE` | `/api/patients/{id}` | Delete a patient |

### Example — Create Patient

```http
POST http://localhost:4004/api/patients
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john.doe@example.com",
  "address": "123 Main St",
  "dateOfBirth": "1990-01-01",
  "registeredDate": "2025-01-01"
}
```

---

## ⚙️ How It Works

### Request Flow
```
GET /api/patients
  → API Gateway (4004)
  → strips /api prefix
  → forwards to Patient Service at http://patient-service:4000/patients
```

### gRPC Flow
When a patient is created, Patient Service calls Billing Service over gRPC to create a billing account:
```protobuf
service BillingService {
  rpc CreateBillingAccount (BillingRequest) returns (BillingResponse);
}
```

### Kafka Flow
Patient Service publishes a PatientEvent to Kafka on every patient creation. Analytics Service consumes these events:
```protobuf
message PatientEvent {
  string patientId = 1;
  string name = 2;
  string email = 3;
  string event_type = 4;
}
```

---

## 🚀 Running the Project

### Prerequisites
- Java 21
- Docker & Docker Compose
- Maven

### Start all services

```bash
docker-compose up --build
```

### Verify gateway routes are loaded

```http
GET http://localhost:4004/actuator/gateway/routes
```

Should return both registered routes. If it returns `[]`, the gateway config hasn't loaded correctly.

### API Docs

```
http://localhost:4004/api-docs/patients
```

---

## 🔍 Debugging Tips

- **Empty `[]` from `/actuator/gateway/routes`** → routes aren't registering, check `application.yml` indentation and `pom.xml` dependencies
- **404 through gateway but 200 direct** → check the route predicate and `StripPrefix` filter
- **gRPC errors** → make sure billing-service is running on port 9001
- **No Kafka events** → check Kafka broker is up and serializers match between producer and consumer

---

## 📁 Project Structure

```
patient-management/
├── api-gateway/          # Spring Cloud Gateway
├── patient-service/      # Core patient CRUD + gRPC client + Kafka producer
├── billing-service/      # gRPC server for billing accounts
├── analytics-service/    # Kafka consumer
├── api-requests/         # .http test files for IntelliJ
├── grpc-requests/        # gRPC test requests
└── api-docs/             # OpenAPI spec
```

---

## 📌 Notes

- The gateway uses Docker hostnames (`patient-service`, `billing-service`) — these resolve inside Docker network only
- Running services locally requires updating URIs in `application.yml` to `localhost`
