# 🏥 Patient Management System
![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.x-6DB33F?style=flat-square&logo=springboot)
![Spring Cloud Gateway](https://img.shields.io/badge/API_Gateway-Spring_Cloud-6DB33F?style=flat-square&logo=spring)
![Kafka](https://img.shields.io/badge/Kafka-Event_Streaming-231F20?style=flat-square&logo=apachekafka)
![gRPC](https://img.shields.io/badge/gRPC-Protobuf-4285F4?style=flat-square&logo=google)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-4169E1?style=flat-square&logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-Containerised-2496ED?style=flat-square&logo=docker)

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

---

## 🧠 What I Learned

This project was genuinely fun and I really wanted to understand how production backend systems are structured. Here's what I took away:

### Spring Boot & CRUD
- How to properly structure a Spring Boot app beyond just making endpoints work
- Using validation groups to apply different rules on create vs update
- Global exception handling with `@RestControllerAdvice` instead of scattering try/catch everywhere
- DTOs as a boundary layer — keeping your internal model separate from what you expose

### Docker
- Docker was something I never got a proper chance to learn at uni — this project changed that
- Understanding how containers communicate via an internal Docker network (and why `localhost` doesn't work between containers)
- Writing Dockerfiles for Spring Boot services and wiring them together
- How services discover each other by container name rather than IP address

### Microservices Architecture
- The real difference between monolithic and microservices — not just theoretically but feeling it when you have to think about how services talk to each other
- Each service owning its own data and responsibility, rather than one giant codebase doing everything
- The tradeoffs are real — microservices adds complexity, but the separation of concerns and independent deployability makes it worth it at scale
- How an API Gateway acts as the single entry point and why that matters for routing, security, and observability

### gRPC
- Completely new to me before this — gRPC uses HTTP/2 under the hood which means multiplexed streams, lower latency, and binary serialisation via Protobuf instead of JSON
- Defining contracts via `.proto` files first and generating code from them — a different mindset to REST
- Why gRPC makes sense for internal service-to-service calls where performance matters, while REST still makes sense for public-facing APIs

### Kafka
- Kafka was brand new to me — I initially associated it with something like MQTT or basic pub/sub messaging
- Kafka is fundamentally different: it's a distributed log, messages are persisted and replayable, not just fire-and-forget
- Understanding producers, consumers, topics, and serialisation (Protobuf over the wire)
- How the analytics service could consume patient events independently without the patient service knowing or caring

### Swagger / OpenAPI
- How SpringDoc auto-generates an OpenAPI spec from your annotations
- Routing the `/api-docs` endpoint through the gateway so everything is accessible from one place
- How useful it is to have a live, accurate API spec rather than maintaining docs manually

### General Backend Confidence
- I used to feel like I always needed a frontend to show that something was working — this project broke that habit
- Debugging distributed systems across multiple services taught me more about how things actually fail than any tutorial ever did
- Reading logs, tracing requests through the gateway, using the actuator
---

## 🔜 What I'd Improve / Next Steps

- **Authentication** — adding JWT-based auth at the gateway level so all services are protected without each one implementing it separately
- **Testing** — this was one of the harder parts of the project, especially tracking down `pom.xml` dependency issues. I want to invest more in proper integration tests and get more systematic about debugging
- **AWS deployment** — I recently completed my AWS course and want to deploy this properly, likely with ECS or EKS for the containers and MSK for managed Kafka
- **Experiment more independently** — this was a guided project (big credit to **Chris Blakely** for teaching it end to end), and I learned a huge amount from working through it properly rather than vibe coding the whole thing. But the next step is taking these concepts and building something from scratch on my own terms maybe even building something new like the app

> In the age of AI, a lot of this could be generated in minutes. But understanding the architecture decisions, the tradeoffs, and why things break the way they do I feel this part can't be skipped. That's what made this worth doing.Going forward I'll definitely try doing something bigger :).

