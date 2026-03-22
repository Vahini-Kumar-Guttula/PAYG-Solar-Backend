# PAYG Solar System Backend

A comprehensive backend system for managing Pay-As-You-Go (PAYG) solar devices, customers, and payments. Built with Spring Boot 3.2.0 and Java 17.

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Architecture](#architecture)
- [Database Schema](#database-schema)
- [Setup Instructions](#setup-instructions)
- [API Documentation](#api-documentation)
- [Business Logic](#business-logic)
- [Scalability Considerations](#scalability-considerations)
- [Trade-offs and Design Decisions](#trade-offs-and-design-decisions)

## 🎯 Overview

Sun King provides solar systems to customers on a pay-as-you-go (PAYG) basis. This backend system manages the complete lifecycle of customer accounts, device assignments, payment processing, and automated device locking/unlocking based on payment status.

## ✨ Features

### Core Functionality
- **Customer Management**: Create, update, retrieve, and search customers
- **Device Management**: Register devices, assign to customers, track device status
- **Payment Processing**: Record payments, integrate with external payment gateway
- **Automated Device Locking**: Scheduled job to lock devices with overdue payments
- **Automatic Device Unlocking**: Unlock devices upon successful payment
- **Payment History**: Track complete payment history per customer
- **Retry Mechanism**: Retry failed payments

### Device States
- `ACTIVE`: Device is operational
- `INACTIVE`: Device is registered but not assigned
- `LOCKED`: Device is locked due to missed payments

### Payment States
- `PENDING`: Payment initiated but not completed
- `COMPLETED`: Payment successful
- `FAILED`: Payment failed

## 🛠 Technology Stack

- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Build Tool**: Gradle 8.5
- **Database**: H2 (Development/Testing), MySQL (Production)
- **ORM**: Spring Data JPA / Hibernate
- **API Documentation**: Swagger/OpenAPI 3.0
- **External Integration**: WebClient (for payment gateway)
- **Validation**: Jakarta Bean Validation
- **Utilities**: Lombok

## 🏗 Architecture

### Layered Architecture

```
┌─────────────────────────────────────┐
│     Controllers (REST API)          │
├─────────────────────────────────────┤
│     DTOs (Request/Response)         │
├─────────────────────────────────────┤
│     Service Layer (Business Logic)  │
├─────────────────────────────────────┤
│     Repository Layer (Data Access)  │
├─────────────────────────────────────┤
│     Database (PostgreSQL)           │
└─────────────────────────────────────┘
```

### Key Components

1. **Controllers**: Handle HTTP requests and responses
   - `CustomerController`: Customer management endpoints
   - `DeviceController`: Device management endpoints
   - `PaymentController`: Payment processing endpoints

2. **Services**: Business logic implementation
   - `CustomerService`: Customer operations
   - `DeviceService`: Device operations and locking logic
   - `PaymentService`: Payment processing and device unlocking
   - `PaymentGatewayService`: External payment gateway integration

3. **Repositories**: Data access layer
   - `CustomerRepository`: Customer data operations
   - `DeviceRepository`: Device data operations
   - `DeviceAssignmentRepository`: Assignment data operations
   - `PaymentRepository`: Payment data operations

4. **Exception Handling**: Centralized error handling
   - `GlobalExceptionHandler`: Handles all exceptions
   - Custom exceptions: `ResourceNotFoundException`, `ResourceAlreadyExistsException`, `BusinessException`

## 💾 Database Schema

### Tables

#### 1. customers
```sql
- id (BIGSERIAL PRIMARY KEY)
- name (VARCHAR(255) NOT NULL)
- email (VARCHAR(255) UNIQUE NOT NULL)
- phone (VARCHAR(20) UNIQUE NOT NULL)
- address (TEXT)
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)

Indexes: email, phone
```

#### 2. devices
```sql
- id (BIGSERIAL PRIMARY KEY)
- serial_number (VARCHAR(100) UNIQUE NOT NULL)
- model (VARCHAR(100) NOT NULL)
- status (VARCHAR(20) NOT NULL) -- ACTIVE, INACTIVE, LOCKED
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)

Indexes: serial_number, status
```

#### 3. device_assignments
```sql
- id (BIGSERIAL PRIMARY KEY)
- customer_id (BIGINT FK -> customers.id)
- device_id (BIGINT FK -> devices.id)
- total_cost (DECIMAL(10,2) NOT NULL)
- amount_paid (DECIMAL(10,2) DEFAULT 0)
- payment_frequency (VARCHAR(20)) -- DAILY, WEEKLY, MONTHLY
- payment_amount (DECIMAL(10,2) NOT NULL)
- last_payment_date (DATE)
- next_payment_due_date (DATE)
- missed_payments_count (INTEGER DEFAULT 0)
- assigned_at (TIMESTAMP)
- completed_at (TIMESTAMP)

Indexes: customer_id, device_id, next_payment_due_date
```

#### 4. payments
```sql
- id (BIGSERIAL PRIMARY KEY)
- assignment_id (BIGINT FK -> device_assignments.id)
- amount (DECIMAL(10,2) NOT NULL)
- payment_date (TIMESTAMP)
- status (VARCHAR(20)) -- PENDING, COMPLETED, FAILED
- transaction_id (VARCHAR(255))
- gateway_response (TEXT)
- retry_count (INTEGER DEFAULT 0)

Indexes: assignment_id, payment_date, status
```

### Relationships
- One Customer can have multiple Device Assignments
- One Device can have one active Assignment at a time
- One Assignment can have multiple Payments

## 🚀 Setup Instructions

### Prerequisites
- Java 17 or higher
- MySQL 8.0 or higher (for production)
- Gradle 8.5 (or use included wrapper)

### 1. Clone the Repository
```bash
git clone <repository-url>
cd payg-solar-backend
```

### 2. Database Setup

#### Option A: H2 In-Memory Database (Default - No Setup Required)
The application uses H2 in-memory database by default. Just run the application and it will work out of the box.

#### Option B: MySQL Database (Production)
```bash
# Create database
mysql -u root -p
CREATE DATABASE payg_solar;
exit;

# Run schema (optional - Hibernate will create tables automatically)
mysql -u root -p payg_solar < src/main/resources/schema-mysql.sql
```

### 3. Configure Application

#### For H2 (Default - No Configuration Needed)
The application is pre-configured to use H2. Just run it.

#### For MySQL (Production)
Run with MySQL profile:
```bash
./gradlew bootRun --args='--spring.profiles.active=mysql'
```

Or edit `src/main/resources/application.yml` to set MySQL as default, or create `application-local.yml`:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/payg_solar
    username: your_username
    password: your_password
```

### 4. Build the Application
```bash
# Using Gradle wrapper (recommended)
./gradlew clean build

# Or using installed Gradle
gradle clean build
```

### 5. Run the Application
```bash
# Using Gradle wrapper
./gradlew bootRun

# Or using installed Gradle
gradle bootRun

# Or run the JAR
java -jar build/libs/payg-solar-backend-1.0.0.jar
```

### 6. Access the Application
- **API Base URL**: http://localhost:8080/api/v1
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/api-docs

## 📚 API Documentation

### Customer Endpoints

#### Create Customer
```http
POST /api/v1/customers
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john.doe@example.com",
  "phone": "+254712345678",
  "address": "123 Main St, Nairobi"
}
```

#### Get Customer
```http
GET /api/v1/customers/{id}
```

#### List Customers (Paginated)
```http
GET /api/v1/customers?page=0&size=20&sort=name,asc
```

#### Search Customers
```http
GET /api/v1/customers/search?query=john
```

### Device Endpoints

#### Register Device
```http
POST /api/v1/devices
Content-Type: application/json

{
  "serialNumber": "SN-2024-001",
  "model": "SolarMax 100W"
}
```

#### Assign Device to Customer
```http
POST /api/v1/devices/{deviceId}/assign
Content-Type: application/json

{
  "customerId": 1,
  "totalCost": 50000.00,
  "paymentFrequency": "WEEKLY",
  "paymentAmount": 500.00
}
```

#### Get Device Status
```http
GET /api/v1/devices/{id}/status
```

#### List Devices (Paginated)
```http
GET /api/v1/devices?page=0&size=20&status=ACTIVE
```

### Payment Endpoints

#### Process Payment
```http
POST /api/v1/payments
Content-Type: application/json

{
  "assignmentId": 1,
  "amount": 500.00
}
```

#### Get Payment History
```http
GET /api/v1/payments/customer/{customerId}?page=0&size=20
```

#### Get Payment Details
```http
GET /api/v1/payments/{id}
```

#### Retry Failed Payment
```http
POST /api/v1/payments/{id}/retry
```

### Response Format

#### Success Response
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john.doe@example.com",
  "phone": "+254712345678",
  "address": "123 Main St, Nairobi",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

#### Error Response
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Customer not found with id: 999",
  "path": "/api/v1/customers/999"
}
```

#### Validation Error Response
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/customers",
  "errors": {
    "email": "Email must be valid",
    "name": "Name is required"
  }
}
```

## 🔐 Business Logic

### Device Locking Logic (CRITICAL)

#### Scheduled Job
- **Frequency**: Runs daily at 2:00 AM
- **Configuration**: `application.payg.overdue-days=7` (configurable)
- **Process**:
  1. Identifies all assignments with `next_payment_due_date` older than 7 days
  2. Locks the associated device (status → LOCKED)
  3. Increments `missed_payments_count`
  4. Logs the locking action

#### Implementation
```java
@Scheduled(cron = "0 0 2 * * *")
@Transactional
public void checkAndLockOverdueDevices() {
    LocalDate cutoffDate = LocalDate.now().minusDays(overdueDays);
    List<DeviceAssignment> overdueAssignments = 
        assignmentRepository.findOverdueAssignments(cutoffDate);
    
    for (DeviceAssignment assignment : overdueAssignments) {
        lockDevice(assignment);
    }
}
```

### Device Unlocking Logic

#### Automatic Unlocking
- **Trigger**: Successful payment processing
- **Process**:
  1. Payment is validated and processed through gateway
  2. If payment succeeds:
     - Update assignment balance
     - Calculate next payment due date
     - Reset `missed_payments_count` to 0
     - Unlock device (status → ACTIVE)
  3. Log the unlocking action

#### Implementation
```java
@Transactional
public PaymentDTO.Response processPayment(PaymentDTO.CreateRequest request) {
    // ... validation and gateway call ...
    
    if (gatewayResponse.isSuccess()) {
        payment.setStatus(Payment.PaymentStatus.COMPLETED);
        updateAssignmentAfterPayment(assignment, request.getAmount());
        deviceService.unlockDevice(assignment); // UNLOCKS DEVICE
    }
    
    return paymentMapper.toResponse(paymentRepository.save(payment));
}
```

### Payment Gateway Integration

#### Mock Implementation
- **Success Rate**: 95% (configurable)
- **Timeout**: 5 seconds
- **Retry**: Supports manual retry for failed payments

#### Real Integration
Replace `PaymentGatewayService` implementation with actual API:
```java
@Service
public class PaymentGatewayService {
    private final WebClient webClient;
    
    public GatewayResponse processPayment(PaymentRequest request) {
        return webClient.post()
            .uri("/payments")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(GatewayResponse.class)
            .block();
    }
}
```

## 📈 Scalability Considerations

### Database Optimization

1. **Indexing Strategy**
   - Primary keys on all tables
   - Indexes on frequently queried columns (email, phone, serial_number, status)
   - Composite indexes on date ranges for payment queries
   - Index on `next_payment_due_date` for scheduled job performance

2. **Query Optimization**
   - Custom JPQL queries to avoid N+1 problems
   - Fetch joins for related entities
   - Pagination on all list endpoints
   - Batch processing for scheduled jobs

3. **Connection Pooling**
   ```yaml
   spring:
     datasource:
       hikari:
         maximum-pool-size: 20
         minimum-idle: 5
         connection-timeout: 30000
   ```

### Application Scalability

1. **Stateless Design**
   - No session state stored in application
   - Enables horizontal scaling

2. **Pagination**
   - Default page size: 20
   - Maximum page size: 100
   - Prevents memory issues with large datasets

3. **Async Processing** (Future Enhancement)
   - Payment processing can be moved to message queue
   - Device locking can be distributed across multiple workers

4. **Caching** (Future Enhancement)
   - Redis for frequently accessed data
   - Cache customer and device lookups

### Handling 1M+ Customers

1. **Database Partitioning**
   - Partition `payments` table by date
   - Partition `device_assignments` by customer_id range

2. **Read Replicas**
   - Separate read and write operations
   - Route queries to read replicas

3. **Batch Processing**
   - Process device locking in batches of 1000
   - Use cursor-based pagination for large datasets

## ⚖️ Trade-offs and Design Decisions

### 1. Synchronous vs Asynchronous Payment Processing

**Decision**: Synchronous processing
**Rationale**: 
- Simpler implementation for MVP
- Immediate feedback to user
- Easier error handling and retry logic

**Trade-off**: 
- May impact response time under high load
- Future: Move to async with Kafka/RabbitMQ for better scalability

### 2. Scheduled Job vs Event-Driven Locking

**Decision**: Scheduled job (daily at 2 AM)
**Rationale**:
- Predictable execution time
- Batch processing is more efficient
- Reduces database load

**Trade-off**:
- Devices may remain active for up to 24 hours after becoming overdue
- Future: Add real-time checks on critical operations

### 3. Mock vs Real Payment Gateway

**Decision**: Mock implementation with 95% success rate
**Rationale**:
- Enables testing without external dependencies
- Demonstrates integration pattern
- Easy to swap with real implementation

**Trade-off**:
- Requires replacement for production
- Mock doesn't test real-world edge cases

### 4. PostgreSQL vs NoSQL

**Decision**: PostgreSQL (relational database)
**Rationale**:
- Strong ACID guarantees for financial transactions
- Complex queries and joins needed
- Well-established indexing and optimization

**Trade-off**:
- May require sharding for extreme scale
- NoSQL could offer better horizontal scaling

### 5. Monolithic vs Microservices

**Decision**: Monolithic architecture
**Rationale**:
- Simpler deployment and development
- Sufficient for current scale
- Easier to maintain with small team

**Trade-off**:
- Harder to scale individual components
- Future: Can be split into microservices (Customer, Device, Payment services)

### 6. Soft Delete vs Hard Delete

**Decision**: Hard delete (with caution)
**Rationale**:
- Simpler data model
- GDPR compliance easier
- Cleaner database

**Trade-off**:
- Cannot recover deleted data
- Future: Implement soft delete for audit trail

### 7. Optimistic vs Pessimistic Locking

**Decision**: Optimistic locking (JPA @Version)
**Rationale**:
- Better performance under normal load
- Conflicts are rare in this domain
- Simpler implementation

**Trade-off**:
- May have conflicts under high concurrency
- Future: Add pessimistic locking for critical operations

## 🧪 Testing

### Run Tests
```bash
./gradlew test
```

### Test Coverage
- Unit tests for service layer
- Integration tests for repositories
- API tests for controllers

## 📦 Deployment

### Build for Production
```bash
./gradlew clean build -Pprod
```

### Docker Deployment (See docker-compose.yml)
```bash
docker-compose up -d
```

### AWS Deployment
1. Build JAR: `./gradlew bootJar`
2. Upload to EC2 instance
3. Configure RDS PostgreSQL
4. Run with: `java -jar payg-solar-backend-1.0.0.jar`

## 🔒 Security Considerations

### Current Implementation
- Input validation on all endpoints
- SQL injection prevention via JPA
- Exception handling to prevent information leakage

### Future Enhancements
- JWT authentication
- Role-based access control (Admin vs Agent)
- API rate limiting
- Encryption for sensitive data

## 📝 License

This project is proprietary software for Sun King.

## 👥 Contributors

- Development Team

## 📞 Support

For issues and questions, please contact the development team.

---

**Version**: 1.0.0  
**Last Updated**: 2026-03-22
