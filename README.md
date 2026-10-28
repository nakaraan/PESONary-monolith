# Pesonary (spring-monolith-enterprise)

Pesonary is an e-wallet backend built as a Spring Boot monolith for MCO1. It covers REST APIs, data persistence with Spring Data JPA and MySQL, and security with Spring Security and JWT. It will later be split into microservices for MCO2.

Customers hold wallets, and every deposit, withdrawal and transfer is recorded as a transaction with a reference number. Each wallet has a tier that caps its balance, the way e-wallets limit unverified accounts:

| Tier | Maximum balance |
|---|---|
| BASIC (default) | ₱10,000.00 |
| VERIFIED | ₱500,000.00 |

A transaction is refused (HTTP 422) when funds are insufficient, when either wallet is frozen or closed, when it would push a wallet over its tier limit, or when a transfer targets the same wallet. A transfer runs as one database transaction, so a refused transfer leaves both balances unchanged.

## Tech stack

Java 17, Spring Boot 4.1.2, Spring Data JPA, MySQL, Spring Security with JWT (jjwt 0.11.5), JUnit 5 and Mockito, JaCoCo, H2 for tests.

## Prerequisites

- JDK 17 or 21
- MySQL 8 running on `localhost:3306`
- No separate Maven install; the project includes the Maven wrapper

## Database setup

On first run the app connects to MySQL and creates the `pesonary_db` database. Hibernate creates the tables (`ddl-auto=update`).

Set these environment variables before running. The defaults in `application.properties` are for local development only.

| Variable | Purpose | Default |
|---|---|---|
| `DB_USERNAME` | MySQL user | `root` |
| `DB_PASSWORD` | MySQL password | `yourLocalPassword` |
| `JWT_SECRET` | Key that signs tokens, at least 32 characters | placeholder value |

In IntelliJ, set them under Run > Edit Configurations > Environment variables.

## Run

```
.\mvnw.cmd spring-boot:run      # Windows
./mvnw spring-boot:run          # macOS / Linux
```

The API runs at `http://localhost:8080`. `GET /actuator/health` should return `"status": "UP"`.

## Create the first admin

New users register as customers. To make an admin:

1. `POST /api/v1/auth/register` with the username `admin`.
2. In MySQL: `UPDATE pesonary_db.users SET role = 'ROLE_ADMIN' WHERE username = 'admin';`
3. Log in again with `POST /api/v1/auth/login`.

Send the token from the login response's `data` field as `Authorization: Bearer <token>`. Tokens expire after one hour.

## Endpoints

All paths start with `/api/v1` except the health check.

| Method | Path | Who can call it |
|---|---|---|
| POST | `/auth/register` | anyone |
| POST | `/auth/login` | anyone |
| GET | `/customers/me` | any logged-in user |
| POST, GET | `/customers` | admin |
| GET, PUT, DELETE | `/customers/{id}` | admin |
| POST | `/accounts` | admin, or a customer for their own `customerId` |
| GET | `/accounts` (optional `?status=ACTIVE`) | admin |
| GET | `/accounts/page?page=0&size=5` | admin |
| GET | `/accounts/{id}` | admin or the owner |
| PUT, DELETE | `/accounts/{id}` | admin |
| POST | `/transactions` | admin or the owner of the source wallet |
| GET | `/transactions?accountId=1` | admin or the owner |
| GET | `/transactions/{id}` | admin or the owner |
| PUT, DELETE | `/transactions/{id}` | admin (PUT changes only the description; DELETE does not reverse balances) |
| GET | `/actuator/health` | anyone |

Example transfer body. `type` is `DEPOSIT`, `WITHDRAWAL` or `TRANSFER`, and `counterpartyAccountId` is used only for transfers.

```json
{"type": "TRANSFER", "amount": 300.00, "accountId": 1, "counterpartyAccountId": 2, "description": "rent"}
```

## Errors

Every error returns the same JSON shape:

```json
{"timestamp": "2026-10-28T20:15:00", "status": 404, "errorType": "Not Found", "message": "Account not found"}
```

400 invalid input · 401 missing or invalid token, or wrong password · 403 not allowed · 404 not found · 409 conflict with existing data · 422 business rule refused

## Tests

```
.\mvnw.cmd test      # Windows
./mvnw test          # macOS / Linux
```

Tests run on an in-memory H2 database, so MySQL isn't needed. The coverage report is written to `target/site/jacoco/index.html`.

## Project documents

- UML class diagram: 
- Test cases: https://docs.google.com/document/d/1hl3CrwGZ-r3WNBTpfYL5ER1-1VfyIokvpH4_MWbVgtw/edit?usp=sharing
- Postman collection: 
- Demo video: [to be completed: video link]

Author: Wesner Almin III