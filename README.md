## SummerShop - E-Commerce Microservices Platform

### Architecture Overview
A distributed microservices architecture implementing an e-commerce system with modern cloud-native patterns.
<img width="751" height="501" alt="summershop drawio" src="https://github.com/user-attachments/assets/494b4887-e99c-4ae7-bd7e-2e753afed977" />

### Core Services

**1. Infrastructure Services**
- **Eureka Server** (Port 8761): Service discovery and registration
- **API Gateway** (Port 8080): Central entry point with JWT authentication, routing, and CORS handling

**2. Business Services**
- **Account Service** (Port 8081): Customer account management using PostgreSQL
- **Auth Service** (Port 8083): JWT-based authentication and user registration using PostgreSQL
- **Item Service** (Port 8082): Product catalog and inventory management using MongoDB
- **Order Service** (Port 8084): Order lifecycle management using Cassandra
- **Payment Service** (Port 8085): Payment processing with idempotency support using PostgreSQL

### Technical Stack
- **Framework**: Spring Boot 3.1.5, Spring Cloud
- **Languages**: Java 17
- **Databases**: PostgreSQL, MongoDB, Cassandra (polyglot persistence)
- **Messaging**: Apache Kafka for event-driven architecture
- **Service Discovery**: Netflix Eureka
- **API Gateway**: Spring Cloud Gateway
- **Security**: JWT tokens with Spring Security
- **Build Tool**: Maven (multi-module project)

### Key Features
- **Event-Driven Architecture**: Kafka-based async communication between Order and Payment services
- **Distributed Data Management**: Each service owns its database (Database per Service pattern)
- **Security**: JWT-based authentication with token refresh mechanism
- **Idempotency**: Payment service implements idempotency keys to prevent duplicate transactions
- **Polyglot Persistence**: Different databases optimized for each service's needs

### Communication Patterns
- **Synchronous**: REST APIs through API Gateway
- **Asynchronous**: Kafka events for Order-Payment orchestration
- **Service Discovery**: Dynamic service location via Eureka

### Docker Support
Complete docker-compose setup with all required infrastructure including databases, Kafka, Zookeeper, and optional Kafka UI for monitoring.

