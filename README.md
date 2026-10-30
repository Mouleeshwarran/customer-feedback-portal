# Customer Feedback Portal (FeedbackHub)
> A modern, secure, full-stack feedback collection, moderation, and analytics platform.

![Technology](https://img.shields.io/badge/Java-21-blue.svg)
![Framework](https://img.shields.io/badge/Spring_Boot-3.4.1-green.svg)
![Frontend](https://img.shields.io/badge/React-18-61dafb.svg)
![Build](https://img.shields.io/badge/Vite-6-purple.svg)
![Tests](https://img.shields.io/badge/JUnit-39_Passed-brightgreen.svg)
![Security](https://img.shields.io/badge/Spring_Security-RBAC-red.svg)

---

## 1. Project Overview & Features
**FeedbackHub** is designed to provide transparent, verified feedback management with distinct separation of roles, strict data isolation, evidence attachments, and comprehensive auditability.

### Key Capabilities
* **Role-Based Access Control (RBAC)**: Strict role boundaries for `CUSTOMER`, `MODERATOR`, and `ADMINISTRATOR`.
* **Evidence Attachments (FR-01)**: Secure multipart evidence upload with MIME-type enforcement, 10 MB size limits, UUID server-side storage keys, and path traversal prevention.
* **Public & Private Submissions**: Approved public feedback browse with real-time text search, rating and category filters; private submissions remain strictly between the customer and staff.
* **Moderation Workflow**: Mandatory justification reasons for every decision (`APPROVED`, `REJECTED`, `HIDDEN`) with instant queue refresh and live audit logs.
* **Abuse Reporting**: Community abuse reporting on public feedback with duplicate submission prevention (`409 Conflict`) and moderator resolution workflow.
* **Administrator Portal**: Dedicated views for user account lifecycle (activation/deactivation), category taxonomy management, and immutable system audit trail.
* **Security Hardening**:
  * Adaptive BCrypt password hashing (12 rounds) with 12+ character policy.
  * In-memory sliding window rate limiters for submissions (max 3/min) and login attempts (max 5/15min).
  * Centralized exception handling with unique `correlationId` tracking.
  * Security response headers: HSTS, `X-Frame-Options: SAMEORIGIN`, `X-Content-Type-Options: nosniff`.
* **Modern Pastel UI**: Clean, accessible SaaS interface designed with soft lavender, lilac, powder blue, and mint palette.

---

## 2. Technology Stack
* **Backend**:
  * Java 21 LTS
  * Spring Boot 3.4.1 (Web, Security, Data JPA, Validation)
  * Springdoc OpenAPI 2.7.0 (Swagger UI)
  * H2 Database (File-backed `./data/feedbackdb` in dev; in-memory in tests; MySQL-compatible schema)
  * Apache Maven (via Maven Wrapper `mvnw`)
* **Frontend**:
  * React 18
  * Vite
  * Lucide React Icons
  * Pure CSS with Pastel Theme variables

---

## 3. Demo Credentials
Fictional development accounts are pre-seeded and initialized on boot:

| Role | Email Address | Password | Permissions |
|---|---|---|---|
| **Customer** | `customer@example.com` | `CustomerDemo123!` | Submit feedback, view own submissions, report abuse |
| **Moderator** | `moderator@example.com` | `ModeratorDemo123!` | Review pending feedback, resolve abuse reports, view stats |
| **Administrator** | `admin@example.com` | `AdminDemo123!` | Manage users, manage categories, inspect audit logs, full moderation |

*Tip: The login page includes one-click **Quick Demo Logins** chips for instant testing.*

---

## 4. Setup & Running the Application

### Prerequisites
* **Java**: JDK 21+ installed and configured on your `PATH`.
* **Node.js**: v18+ and `npm` installed.

### A. Run Backend
1. Open a terminal in the `backend` directory:
   ```powershell
   cd D:\ioc\customer-feedback-portal\backend
   ./mvnw spring-boot:run
   ```
2. The backend server starts at **`http://localhost:8080`**.
   * **Swagger UI Documentation**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
   * **OpenAPI Raw JSON**: [http://localhost:8080/api-docs](http://localhost:8080/api-docs)
   * **H2 Database Console**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console) (JDBC URL: `jdbc:h2:file:./data/feedbackdb`, User: `sa`, Password: *(empty)*)

### B. Run Frontend
1. Open a second terminal in the `frontend` directory:
   ```powershell
   cd D:\ioc\customer-feedback-portal\frontend
   npm install
   npm run dev
   ```
2. The frontend web portal will launch at **`http://localhost:5173`**.

---

## 5. Automated Testing

### Run Backend Test Suite
```powershell
cd D:\ioc\customer-feedback-portal\backend
./mvnw test
```
*Executes all 39 unit, integration, and security tests against an isolated in-memory test database.*

### Build Frontend Production Bundle
```powershell
cd D:\ioc\customer-feedback-portal\frontend
npm run build
```

---

## 6. Project Documentation & Deliverables

All required submission deliverables are organized in the [`docs/`](./docs) directory:
* [**Architecture Document**](./docs/ARCHITECTURE.md): System context, data flow, trust boundaries, and architectural trade-offs.
* [**ER Diagram**](./docs/ER_DIAGRAM.md): Mermaid entity relationship diagram, relationships, and constraints.
* [**Security Specification**](./docs/SECURITY.md): Threat model, authorization matrix, file upload defenses, and headers.
* [**OpenAPI Specification**](./docs/openapi.yaml): Full OpenAPI 3.0 YAML specification.
* [**Postman Collection**](./docs/feedbackhub.postman_collection.json): Comprehensive API test collection.
* [**Test Report**](./docs/TEST_REPORT.md): 39 passed test cases covering functional, role escalation, and negative security tests.
* [**Demonstration Guide**](./docs/DEMO_GUIDE.md): 5–10 minute script walking through customer, moderator, and admin workflows.
* [**Database Migrations**](./backend/src/main/resources/db/migration/): Clean V1 schema DDL and V2 seed data scripts.
