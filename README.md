# House of Chaos – Antiques E-commerce API

REST API backend for an online antiques marketplace. Spring Boot 3.4, JWT auth with refresh token rotation, paginated catalog, and Feign-based review microservice integration.

## Overview

Main backend for **House of Chaos**, an e-commerce platform for antique furniture (chairs, tables, lamps, couches). Stateless REST API consumed by an Angular SPA; product reviews are handled by a separate service (Feign client).

### Key Features

- 🔐 **OAuth2 Resource Server** – JWT access tokens, HttpOnly refresh cookies with rotation
- 🛒 **E-commerce** – Browse (paginated), cart, orders (create, confirm, cancel)
- 👤 **Users** – Registration, profile and address management
- 👑 **Admin** – Products, categories, user roles (promote/demote)
- ⭐ **Reviews** – Feign client to dedicated review service
- ⏰ **Scheduled jobs** – New-arrival expiry, cancelled-order cleanup
- 💾 **Caching** – Spring Cache (categories, product lists)
- ✅ **Tests** – Unit, integration, API; 81% line coverage. CI via GitHub Actions (tests on push/PR)

---

## Architecture

The system consists of three main components:

1. **Main API** (this repository) – User auth, products, orders, carts
2. **Review Microservice** – Separate service for product reviews
3. **Angular Frontend** – SPA client for the end users

### Technology Stack

**Core:** Java 17, Spring Boot 3.4.x, Gradle

**Backend:** Spring Web, Security (OAuth2 Resource Server, JWT), Data JPA, MySQL, OpenFeign (review service), Nimbus JOSE JWT, Spring Cache, Scheduling. Product images via Cloudinary.

**Tests:** JUnit 5, Mockito, Spring Security Test, H2. 81% line coverage.

---

## Features in Detail

### Authentication & Security

- **Stateless Authentication**: JWT-based with short-lived access tokens (5 minutes)
- **Refresh Token Flow**: Secure, HttpOnly cookies with automatic rotation
- **Password Security**: BCrypt hashing with configurable strength
- **Role-Based Access Control**: USER and ADMIN roles with endpoint restrictions
- **CORS Configuration**: Configured for Angular frontend on port 4200

**Auth Endpoints:**
- `POST /api/v1/users/register` – User registration
- `POST /api/v1/auth/login` – Login (returns JWT + sets refresh cookie)
- `POST /api/v1/auth/refresh` – Refresh access token
- `POST /api/v1/auth/logout` – Logout (invalidates refresh token)

### Products & Categories

- Full CRUD for products (admin only); soft delete
- Category management (delete blocked if products exist)
- Paginated endpoints: product list (optional category filter), new-arrivals, top-deals (cheapest; optional name search). Default page size 8, max 50.
- Product images via Cloudinary (thumb + large URLs built from `imagePublicId`)

### Shopping Cart

- One cart per user (created automatically on registration)
- Add/remove items with quantity management
- Stock validation (can't add more than available)
- Cart cleared automatically when order is created

### Orders

- Create orders from cart items
- Order statuses: NEW, CONFIRMED, CANCELLED
- Confirm order with shipping address (reduces product inventory)
- Cancel orders (only NEW orders can be cancelled)
- Order history by status

### Review Integration

- Feign client integration with review microservice (port 8081)
- Create, read, and delete reviews for products
- Business rule: review author name must match user's first name
- Graceful error handling for microservice failures

### Admin Features

**Product Management:**
- Add new products with validation
- Update product details (price, description)
- Soft delete products

**Category Management:**
- Create categories with unique names
- Delete categories (blocked if products exist)

**User Management:**
- View all users (excluding self)
- Promote users to admin
- Demote users from admin

### Automated Tasks

**Product New Arrival Cleanup:**
- Runs daily (every 24 hours)
- Marks products as "not new" after 10 days

**Order Cleanup:**
- Runs daily at 3 AM
- Deletes cancelled orders older than 30 days

### Validation & Error Handling

- Jakarta Validation on all request DTOs
- Global exception handler with RFC 7807 Problem Detail responses
- Field-level validation errors (e.g., "Email is required")
- Custom exceptions: `ResourceNotFoundException`, `BusinessRuleException`, `EmailAlreadyUsedException`
- Proper HTTP status codes (400, 404, 409, 500)

---

## Getting Started

### Prerequisites

Java 17+, MySQL 8.0+ (or use Docker below), Gradle (wrapper included).

### Database Setup

1. Start MySQL (e.g. `cd infra && docker compose up -d`; or use a local instance).

2. Create database (or let Spring create it):
   ```sql
   CREATE DATABASE house_of_chaos_main;
   ```

3. Update database credentials:
   
   **Option A:** Environment variables (recommended)
   ```bash
   export DB_USERNAME=your_mysql_username
   export DB_PASSWORD=your_mysql_password
   ```
   
   **Option B:** Edit `src/main/resources/application.properties`
   ```properties
   spring.datasource.username=your_mysql_username
   spring.datasource.password=your_mysql_password
   ```

### Running the Application

```bash
# Build the project
./gradlew build

# Run the application
./gradlew bootRun
```

The API will start on **http://localhost:8080**

### Initial Setup

On first run, the application automatically seeds:
- USER and ADMIN roles
- Default admin user: `admin@email.com` / `adminpassword`
- Sample categories: chair, table, couch, lamp
- Sample products (10 per category)

### Running Tests

```bash
./gradlew test
# Coverage report: build/reports/jacoco/test/html/index.html
# Open it (e.g. on macOS: open build/reports/jacoco/test/html/index.html)
```

Unit tests (services), integration tests (AdminService, OrderService, AuthService), API tests (AdminController, OrderController). 81% line coverage.

---

## API Overview

### Public Endpoints

- `POST /api/v1/users/register` – Register new user
- `POST /api/v1/auth/login` – Login
- `GET /api/v1/products` – Browse products
- `GET /api/v1/products/new-arrivals` – New arrivals
- `GET /api/v1/products/top-deals` – Cheapest products
- `GET /api/v1/categories` – All categories

### Authenticated Endpoints (Requires JWT)

**Cart:**
- `GET /api/v1/cart` – View cart
- `PUT /api/v1/cart/items/{productId}` – Add item to cart
- `POST /api/v1/cart/items/{cartItemId}/decrease` – Decrease quantity
- `DELETE /api/v1/cart/items/{cartItemId}` – Remove item

**Orders:**
- `POST /api/v1/orders` – Create order from cart
- `GET /api/v1/orders/{id}` – Get order by ID
- `GET /api/v1/orders/new` – Get NEW orders
- `GET /api/v1/orders/confirmed` – Get CONFIRMED orders
- `PATCH /api/v1/orders/confirm/{id}` – Confirm order with shipping address
- `POST /api/v1/orders/cancel/{id}` – Cancel order
- `DELETE /api/v1/orders/{id}` – Delete order

**Profile:**
- `GET /api/v1/users/profile` – View profile
- `PUT /api/v1/users/profile` – Update profile and address

**Reviews:**
- `GET /api/v1/reviews/product/{productId}` – Get reviews for product
- `POST /api/v1/reviews` – Create review
- `DELETE /api/v1/reviews/{id}` – Delete review

### Admin Endpoints (Requires ADMIN role)

**Products:**
- `POST /api/v1/admin/products` – Add product
- `PATCH /api/v1/admin/products/{id}` – Update product
- `DELETE /api/v1/admin/products/{id}` – Delete product

**Categories:**
- `POST /api/v1/admin/categories` – Add category
- `DELETE /api/v1/admin/categories/{id}` – Delete category

**Users:**
- `GET /api/v1/admin/users` – List all users
- `PATCH /api/v1/admin/users/promote/{id}` – Promote user to admin
- `PATCH /api/v1/admin/users/demote/{id}` – Demote user from admin

---

## Configuration

### Application Properties

Key configuration options in `application.properties`:

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/house_of_chaos_main?createDatabaseIfNotExist=true
spring.datasource.username=${DB_USERNAME:username}
spring.datasource.password=${DB_PASSWORD:password}

# Security (use env vars or a secrets manager in production)
spring.security.oauth2.resourceserver.jwt.secret-key=<base64-encoded-256-bit-key>
security.jwt.ttl-seconds=300
security.refresh.token.ttl-days=14
security.refresh.cookie.secure=false  # true in production

# Optional
app.public-base-url=http://localhost:8080
```

### Review Microservice

The review service must be running on **http://localhost:8081** for review features to work. If the review service is unavailable, review endpoints will return appropriate error responses.

---

## Security Implementation

### JWT Claims Structure

```json
{
  "iss": "self",
  "sub": "user@email.com",
  "uid": "user-uuid",
  "authorities": ["ROLE_USER", "ROLE_ADMIN"],
  "iat": 1234567890,
  "exp": 1234567990
}
```

### Refresh Token Flow

1. User logs in → receives JWT + HttpOnly refresh cookie
2. Access token expires (5 min) → client calls `/auth/refresh` with cookie
3. Refresh token is rotated (new token issued, old invalidated)
4. New access token returned
5. On logout → refresh token deleted from database, cookie cleared

### Authorization Rules

- `/api/v1/admin/**` – Requires ADMIN role
- `/api/v1/users/register`, `/api/v1/auth/**`, `/api/v1/products/**`, `/api/v1/categories/**` – Public
- All other endpoints – Requires authentication

---

## Database Schema

**Main Tables:**
- `users` – User accounts with encrypted passwords
- `roles` – USER, ADMIN roles
- `user_roles` – Many-to-many relationship
- `addresses` – Shipping addresses
- `carts` – One cart per user
- `cart_items` – Items in carts
- `products` – Product catalog
- `categories` – Product categories
- `orders` – Customer orders
- `order_items` – Order line items
- `refresh_tokens` – Refresh token storage (hashed)

**Key Relationships:**
- User → Cart (one-to-one)
- User → Orders (one-to-many)
- Product → Category (many-to-one)
- Order → OrderItems (one-to-many)

---

## Testing

Unit tests (services, mocked deps), integration tests (AdminService, OrderService, AuthService with H2), API tests (AdminController, OrderController). 81% line coverage.

```bash
./gradlew test
# Coverage: build/reports/jacoco/test/html/index.html
```

---

## API Examples

### Register & Login

```bash
# Register
curl -X POST http://localhost:8080/api/v1/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123",
    "confirmPassword": "password123"
  }'

# Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'
```

### Browse Products

```bash
# All products (paginated; optional categoryId)
curl "http://localhost:8080/api/v1/products?page=0&size=8"
curl "http://localhost:8080/api/v1/products?categoryId={categoryUuid}"

# New arrivals / top deals (paginated)
curl "http://localhost:8080/api/v1/products/new-arrivals"
curl "http://localhost:8080/api/v1/products/top-deals"
```

### Cart Operations

```bash
# Get cart (requires JWT)
curl http://localhost:8080/api/v1/cart \
  -H "Authorization: Bearer <access_token>"

# Add product to cart
curl -X PUT http://localhost:8080/api/v1/cart/items/{productId} \
  -H "Authorization: Bearer <access_token>"
```

### Create Order

```bash
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Authorization: Bearer <access_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "items": [
      {"productId": "uuid", "quantity": 2}
    ]
  }'
```

---

## Development

**Structure** – Feature-based packages (auth, user, product, category, cart, order, review, admin), each with web/service/repository layers. DTOs for API contracts; Jakarta Validation; global exception handler (RFC 7807 Problem Detail). UUID primary keys, soft deletes for products.

**Security** – Stateless JWT, refresh token rotation (HttpOnly cookie), BCrypt. Feign client to review service with clear failure handling. Scheduled jobs for new-arrival expiry and order cleanup.

---

## Related

- [house-of-chaos-main](https://github.com/AntoanYosifov/house-of-chaos-main) (this repo)
- [house-of-chaos-web](https://github.com/AntoanYosifov/house-of-chaos-web) – Angular frontend
- [review-microservice](https://github.com/AntoanYosifov/review-microservice) – Review service

---

Antoan Yosifov
