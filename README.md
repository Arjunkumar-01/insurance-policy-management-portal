# Insurance Policy Management Portal (IPMP)

An enterprise-grade, role-based digital insurance platform designed to streamline policy issuance, claims lifecycle processing, premium billing, agent customer portfolios, and administrative analytics.

---

## 📌 Project Overview

**Insurance Policy Management Portal (IPMP)** is a full-stack insurance management platform engineered to deliver transparency, efficiency, and automated workflows across the entire insurance operations lifecycle. Built with a robust **Spring Boot (Java 21)** REST backend, a responsive **React (TypeScript + Vite)** frontend, and a relational **PostgreSQL** persistence engine, IPMP replaces fragmented communication and manual processing with unified, role-based digital workspaces.

---

## Live Application

The application has been successfully deployed and validated on AWS. Reviewers can use the live URL below to access the system.

Frontend URL:
http://insurance-policy-management-portal.s3-website-us-east-1.amazonaws.com

---

## Reviewer Login Credentials

These are validation/demo accounts created exclusively for project review, testing, and evaluation purposes.

### Customer

Username:
mike_anderson

Password:
Password@1

### Agent

Username:
aaron_agent

Password:
Password@1

### Claims Officer

Username:
clara_officer

Password:
Password@1

### Admin

Username:
alice_admin

Password:
Password@1

---

## 🎯 Business Objective

Traditional insurance workflows suffer from disconnected channels, delayed claim approvals, opaque policy statuses, and lack of role segregation. IPMP addresses these challenges by providing:

1. **Customer Self-Service:** Instant product discovery, online policy purchase, claim submission with document attachments, real-time claim status tracking, and premium payment processing with downloadable receipts.
2. **Agent Assistance:** Dedicated customer portfolio workspace enabling agents to monitor assigned clients, track 90-day renewal windows, view client claims, and assist with policy inquiries.
3. **Claims Officer Adjudication:** Multi-stage claims queue triage (`SUBMITTED` &rarr; `UNDER_REVIEW` &rarr; `APPROVED` / `REJECTED` &rarr; `SETTLED`) with mandatory decision reason logging and audit timestamps.
4. **Administrative Governance:** System-wide metrics, policy cancellation approvals, product catalog lifecycle management (activation/deactivation), revenue distribution analysis, and user management.

---

## 👥 User Roles & Access Matrix

| Role | Workspace Route | Key Capabilities & Permissions |
|---|---|---|
| **CUSTOMER** | `/customer/dashboard` | Browse insurance products, purchase policies, view coverage terms, submit claims with external document URLs, pay premiums, view payment history and receipts, edit profile. |
| **AGENT** | `/agent/dashboard` | Search customer accounts, view client policy portfolios, monitor expiring policies (90-day renewal radar), review client claims and payment history. |
| **CLAIMS_OFFICER** | `/claims/dashboard` | Access incoming claims queue, transition claims to review, approve/reject claims with mandatory justification, issue final claim settlements. |
| **ADMIN** | `/admin/dashboard` | Overview analytics, user account management, product catalog creation/updates/activation/deactivation, policy cancellation approvals, revenue and loss ratio reports. |

---

## 🏗 System Architecture

```
                                  +---------------------------------------------+
                                  |               Client Browser                |
                                  +---------------------------------------------+
                                         /                               \
                     (Static Web Hosting / HTTP)                  (REST API / JWT Auth)
                                       /                                   \
                                      v                                     v
                        +---------------------------+         +---------------------------+
                        |      AWS S3 Bucket        |         |    Spring Boot Backend    |
                        |   (React 18 + TypeScript) |         |     (AWS EC2 / Port 8080) |
                        +---------------------------+         +---------------------------+
                                                                            |
                                                                     (JPA / Hibernate)
                                                                            |
                                                                            v
                                                              +---------------------------+
                                                              |    PostgreSQL Database    |
                                                              |  (Flyway Migrations V1-V8)|
                                                              +---------------------------+
```

---

## 💻 Technology Stack

### Backend
- **Framework:** Spring Boot 4.1.0
- **Runtime:** Java 21 (LTS)
- **Security:** Spring Security 7 with Stateless JWT (`io.jsonwebtoken:jjwt 0.12.6`) & BCrypt Password Hashing
- **ORM & Data:** Spring Data JPA, Hibernate ORM, HikariCP Connection Pooling
- **Database:** PostgreSQL 16
- **Database Migrations:** Flyway (Versioned migrations `V1` through `V8`)
- **Validation:** Jakarta Bean Validation (Hibernate Validator)
- **Monitoring:** Spring Boot Actuator (`/actuator/health`)
- **Build Tool:** Apache Maven

### Frontend
- **Framework:** React 18.3.1
- **Language:** TypeScript 6 (Strict Mode)
- **Build Tool & Dev Server:** Vite 8.2.2
- **Routing:** React Router DOM 6.30.6 (Protected and Role-Guarded Route Boundaries)
- **Server State Management:** TanStack React Query v5
- **HTTP Client:** Axios with JWT Request Interceptor & 401 Session Interceptor
- **Form Management:** React Hook Form with Zod Schema Validation
- **Visualizations & Icons:** Recharts, Lucide React
- **Styling:** Custom Modular CSS Architecture

---

## 📊 Database Schema & Entities

The relational PostgreSQL schema enforces strict referential integrity, automated audit timestamps (`created_at`, `updated_at`, `created_by`, `updated_by`), and state constraints:

- `customers`: User credentials, full personal details, role (`CUSTOMER`, `AGENT`, `CLAIMS_OFFICER`, `ADMIN`), enabled flag.
- `products`: Product catalog with category (`HEALTH`, `MOTOR`, `LIFE`, `TRAVEL`, `HOME`, `OTHER`), tenure, coverage amount, premium, and status (`ACTIVE`, `INACTIVE`).
- `policies`: Active and historical policies linking customer & product, start/end dates, nominee information, renewal status (`NOT_DUE`, `ELIGIBLE`, `RENEWAL_REQUESTED`, `RENEWED`), and cancellation status (`NONE`, `REQUESTED`, `APPROVED`, `REJECTED`).
- `claims`: Claim records with incident date, claim amount, description, supporting document URLs, decision reason, review/settlement timestamps, and status (`SUBMITTED`, `UNDER_REVIEW`, `APPROVED`, `REJECTED`, `SETTLED`).
- `payments`: Premium transactions with payment method (`UPI`, `CREDIT_CARD`, `DEBIT_CARD`, `NET_BANKING`, `INSURANCE_WALLET`), invoice/receipt numbers, transaction references, and status (`PENDING`, `PROCESSING`, `SUCCESSFUL`, `FAILED`, `REFUNDED`).
- `flyway_schema_history`: Complete migration audit history from initial DDL through migration `V8`.

---

## 🚀 Local Development Setup

### Prerequisites
- **Java 21 JDK** installed (`java -version`)
- **Node.js 20+** and **npm** installed (`node -v`, `npm -v`)
- **PostgreSQL 15+** running on `localhost:5432`

### 1. Database Initialization
```sql
CREATE DATABASE insurance_portal;
CREATE USER postgres WITH PASSWORD 'postgres';
GRANT ALL PRIVILEGES ON DATABASE insurance_portal TO postgres;
```

### 2. Backend Startup
```powershell
cd backend
.\mvnw.cmd package
java -jar target\portal-0.0.1-SNAPSHOT.jar
```
*The Spring Boot API starts on `http://localhost:8080`. Flyway automatically creates and migrates the database schema.*

### 3. Frontend Startup
```powershell
cd frontend
npm install
npm run dev
```
*The React application starts on `http://localhost:5173`.*

---

## 📬 API Testing with Postman

A pre-configured Postman collection and environment are provided in [backend/postman](backend/postman) for instant API exploration and automated test execution:

1. **Import Assets:** Import [IPMP-Auth-Customer.postman_collection.json](backend/postman/IPMP-Auth-Customer.postman_collection.json) and [IPMP-local.postman_environment.json](backend/postman/IPMP-local.postman_environment.json) into Postman.
2. **Execute Full Test Workflows:** Follow [COMPLETE_MANUAL_TESTING_GUIDE.md](backend/postman/COMPLETE_MANUAL_TESTING_GUIDE.md) or [DEMO_EXECUTION_SHEET.md](backend/postman/DEMO_EXECUTION_SHEET.md) to run step-by-step end-to-end tests across all user roles (Customer, Agent, Claims Officer, Admin).

---

## ☁️ AWS Deployment Architecture (Demonstration / Free Tier)

| Component | AWS Service | Configuration Details |
|---|---|---|
| **Frontend** | Amazon S3 | Static Website Hosting enabled, public read bucket policy, index/error document pointing to `index.html` (SPA routing). |
| **Backend** | Amazon EC2 | `t3.micro` (Ubuntu 22.04 LTS), Java 21, managed as a `systemd` background service (`ipmp.service`), inbound port 8080. |
| **Database** | PostgreSQL | Hosted locally on the EC2 instance on port 5432 (bound to `127.0.0.1`, restricted from public internet). |
| **CORS** | Spring Security | `APP_CORS_ALLOWED_ORIGINS` configured to permit the S3 static website origin with full preflight caching (`maxAge: 3600s`). |

---

## 🔐 Environment Variables

### Backend (`/etc/ipmp.env` or Process Environment)
```ini
SPRING_PROFILES_ACTIVE=prod
DB_HOST=127.0.0.1
DB_PORT=5432
DB_NAME=insurance_portal
DB_USERNAME=portal_app
DB_PASSWORD=<database_password>
JWT_SECRET=<32_character_or_longer_secret_key>
APP_CORS_ALLOWED_ORIGINS=http://your-s3-bucket.s3-website-us-east-1.amazonaws.com
APP_PRODUCT_SEED_ENABLED=true
APP_VALIDATION_DATA_ENABLED=false
```

### Frontend (`frontend/.env.production`)
```ini
VITE_API_BASE_URL=http://your-ec2-public-dns.amazonaws.com:8080
```

---

## 📸 Screenshots & UI Showcase


- **Customer Dashboard:** Metric cards, upcoming renewals, interactive payment history chart, quick action links.
- **Product Catalog & Comparison:** Multi-category insurance product explorer with side-by-side comparison modal.
- **Claims Lifecycle Queue:** Claims Officer worklist with structured approval, rejection, and settlement dialogs.
- **Agent Customer Portfolio:** Multi-resource customer aggregation with renewal radar and open claim indicators.
- **Admin Analytics:** Revenue distribution charts, policy loss ratios, and cancellation approval queue.

