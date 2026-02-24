# House of Chaos – Antiques E-commerce API

A comprehensive REST API backend for an online antiques marketplace, built with Spring Boot 3.4 and modern security practices.

## Overview

This is the main backend service for **House of Chaos**, an e-commerce platform specializing in antique furniture (chairs, tables, lamps, couches). It provides a stateless REST API consumed by an Angular SPA and integrates with a separate microservice for product reviews.

### Key Features

- 🔐 **OAuth2 Resource Server** with JWT access tokens and secure refresh token rotation
- 🛒 **Full E-commerce Flow**: Browse products, manage cart, place orders, confirm/cancel orders
- 👤 **User Management**: Registration, profile updates, address management
- 👑 **Admin Panel**: Product/category management, user role administration
- ⭐ **Review System**: Microservice integration via Feign client
- ⏰ **Automated Tasks**: Scheduled cleanup for old orders and product status updates
- 💾 **Caching**: Spring Cache for frequently accessed categories
- ✅ **Comprehensive Testing**: 80% line coverage with unit, integration, and API tests

---

## Architecture

The system consists of three main components:

1. **Main API** (this repository) – User auth, products, orders, carts
2. **Review Microservice** – Separate service for product reviews
3. **Angular Frontend** – SPA client for the end users

### Technology Stack

**Core:**
- Java 17
- Spring Boot 3.4.0
- Gradle

**Backend:**
- Spring Web (REST API)
- Spring Security + OAuth2 Resource Server
- Spring Data JPA (Hibernate)
- MySQL Database
- Spring Cloud OpenFeign (microservice communication)
- Nimbus JOSE JWT (token signing)
- Spring Cache (category caching)
- Spring Scheduling (automated tasks)

**Development & Testing:**
- Lombok (reduce boilerplate)
- H2 (in-memory database for tests)
- JUnit 5 + Mockito
- Spring Security Test
- 80% line coverage

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

- Full CRUD for products (admin only)
- Soft delete for products (maintains referential integrity)
- Category management with validation (no delete if products exist)
- Special endpoints:
  - `GET /api/v1/products/new-arrivals` – Top 10 newest products
  - `GET /api/v1/products/top-deals` – Top 10 cheapest products
- Product images served from `/images` directory

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

- **Java 17** or higher
- **MySQL 8.0+** running locally
- **Gradle** (wrapper included)

### Database Setup

1. Start MySQL server

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
# Run all tests with coverage
./gradlew test

# View coverage report
open build/reports/jacoco/test/html/index.html
```

**Test Coverage: 81% line coverage**
- Unit tests for services (UserService, CartService, ProductService, etc.)
- Integration tests for service interactions (AdminService, OrderService, AuthService)
- API tests for controllers (AdminController, OrderController)

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

# Security
spring.security.oauth2.resourceserver.jwt.secret-key=<your-256-bit-secret>
security.jwt.ttl-seconds=300  # 5 minutes
security.refresh.token.ttl-days=14
security.refresh.cookie.secure=false  # Set to true in production

# Note: The JWT secret key is included in the repository for testing and 
# examination purposes. In a production environment, this should be 
# externalized via environment variables or a secrets management system.

# Public URL (for image links)
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

The project includes comprehensive test coverage (81% line coverage):

### Test Types

**Unit Tests** – Service layer with mocked dependencies
- `UserServiceUTest`, `CartServiceUTest`, `ProductServiceUTest`, `OrderServiceUTest`
- `CategoryServiceUTest`, `RoleServiceUTest`, `AddressServiceUTest`

**Integration Tests** – Multi-service interactions with real database (H2)
- `AdminServiceITest` – Product/category/user management
- `OrderServiceITest` – Order creation, confirmation, cancellation
- `AuthServiceITest` – Login flow with token generation

**API Tests** – Controller layer with mocked services
- `AdminControllerApiTest` – Admin endpoints
- `OrderControllerApiTest` – Order endpoints

### Running Tests

```bash
# Run all tests
./gradlew test

# Run with coverage report
./gradlew test jacocoTestReport
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
# Get new arrivals
curl http://localhost:8080/api/v1/products/new-arrivals

# Get products by category
curl http://localhost:8080/api/v1/products/category/{categoryId}

# Get cheapest products
curl http://localhost:8080/api/v1/products/top-deals
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

### Project Highlights

✨ **Feature-Based Architecture** – Code organized by business domain (auth, cart, order, product, etc.) with layered structure (web, service, repository) within each feature  
✨ **Security Best Practices** – Stateless JWT, refresh token rotation, password hashing  
✨ **Validation** – Request-level validation with detailed error messages  
✨ **Exception Handling** – Global error handler with RFC 7807 Problem Detail  
✨ **Testing** – Comprehensive test suite with 81% coverage  
✨ **Microservices** – Feign client integration with graceful degradation  
✨ **Scheduled Tasks** – Automated cleanup and maintenance  
✨ **Database Design** – UUID primary keys, proper relationships, soft deletes

### Code Quality

- Consistent package structure
- DTOs for all API contracts
- Custom exceptions for business rules
- Proper transaction management
- Logging for important operations

---

## Related Repositories

- **Main API** – [house-of-chaos-main](https://github.com/AntoanYosifov/house-of-chaos-main) (this repo)
- **Angular Frontend** – [house-of-chaos-web](https://github.com/AntoanYosifov/house-of-chaos-web)
- **Review Microservice** – [review-microservice](https://github.com/AntoanYosifov/review-microservice)

---

## License

This project is part of an educational portfolio.

---

## Author

Developed by Antoan Yosifov
