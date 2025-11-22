# HouseOfChaos – Main Backend API

### In development

A REST-based backend for an e-commerce platform focused on antiques.

- Exposes a **stateless REST API** consumed by an Angular SPA.
- Implements **OAuth2 Resource Server** with short-lived **JWT access tokens** and a **refresh token** flow.
- Integrates with a separate **Review Microservice** for product reviews (via Feign client).

---

## Architecture Overview

The system consists of three main parts:

- **Main Backend API** (this repo)
    - Authentication & authorization
    - Products, categories, carts, orders, users
    - Scheduling & caching
    - Feign client for the review microservice

- **Review Microservice** (`house-chaos-review-service`)
    - Standalone Spring Boot app
    - Own database
    - Manages product reviews

- **Angular Client** (`house-chaos-angular-client`)
    - SPA frontend for browsing products, managing cart, placing orders, managing profile & reviews

---

## Current & Planned Features (Main API)

### Implemented / In Progress

- ✅ **Authentication & Security**
    - Registration & login
    - Stateless auth using **JWT access tokens**
    - OAuth2 **Resource Server** with `spring-boot-starter-oauth2-resource-server`
    - Custom `JwtService` for token generation
    - Refresh token flow

- ✅ **Users & Profiles**
    - User registration with email & password
    - Profile endpoint (`/api/users/profile`)
    - Profile update with **address** (one address per user, used for shipping)
    - Role-based access (`USER`, `ADMIN`)

- ✅ **Products & Categories**
    - Product entity with name, description, price, quantity, image URL
    - Category entity and relationship with products
    - Admin endpoints for managing products & categories

- ✅ **Validation & Error Handling**
    - DTO validation with `jakarta.validation`
    - Global `@RestControllerAdvice` with `ProblemDetail` responses
    - Field-level error details for invalid input (400 BAD REQUEST)
    - Custom exceptions (e.g. `ResourceNotFoundException`)

- ✅ **Database**
    - MySQL with JPA/Hibernate
    - UUID primary keys for domain entities

### Planned (Main API, near term)

- 🛒 Cart & Orders
    - Add to cart / remove from cart
    - Checkout flow → create order from cart
    - Order cancellation
    - Stock quantity handling on order confirmation

- 🧾 Review Integration
    - Feign client for **Review Microservice**
    - Endpoints to create / edit / delete reviews for products
    - Enrich product details with review list & ownership info

- ⏰ Scheduling & Caching
    - Caching for categories with Spring Cache
    - Scheduled tasks (cron + fixedDelay) for:
        - Marking products as not-new after a given period
        - Cleaning up abandoned carts

- 👑 Admin Features
    - Admin panel for product & category management
    - Admin ability to manage user roles

---

## Tech Stack

- **Language:** Java 17
- **Framework:** Spring Boot **3.4.0**
- **Build Tool:** Gradle

### Backend

- **Web:** `spring-boot-starter-web` (REST API)
- **Security:**
    - `spring-boot-starter-security`
    - `spring-boot-starter-oauth2-resource-server`
    - `com.nimbusds:nimbus-jose-jwt` (JWT signing & verification)
- **Persistence:**
    - `spring-boot-starter-data-jpa`
    - MySQL (`mysql-connector-j`)
- **Validation:**
    - `spring-boot-starter-validation` (`jakarta.validation`)
- **Observability:**
    - `spring-boot-starter-actuator`
- **Utilities:**
    - Lombok
    - Spring Boot DevTools

### Testing

- `spring-boot-starter-test`
- JUnit 5 (JUnit Platform)

---

## Running the Application (Dev)

1. Ensure MySQL is running.
2. Configure DB credentials (environment variables or `application.properties`):
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/house_chaos_main?createDatabaseIfNotExist=true
   spring.datasource.username=${DB_USERNAME:username}
   spring.datasource.password=${DB_PASSWORD:password}
   spring.jpa.hibernate.ddl-auto=update

## Related Repositories

- **Main API (this repo)** – [`house-of-chaos-main`](https://github.com/AntoanYosifov/house-of-chaos-main)
- **Angular client** – [`house-of-chaos-web`](https://github.com/AntoanYosifov/house-of-chaos-web)
- **Review microservice** – [`review-microservice`](https://github.com/AntoanYosifov/review-microservice)
