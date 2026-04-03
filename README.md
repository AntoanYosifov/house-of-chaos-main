# House of Chaos — Antiques E-commerce API

Production-deployed REST API backend for an online antiques marketplace. Spring Boot 3.4, stateless JWT auth with refresh token rotation, full e-commerce domain (cart, orders, inventory), and a Feign-based review microservice.

**Live:** [houseofchaoss.com](https://houseofchaoss.com)

---

## Architecture

Three-component system, fully deployed on AWS:

```
Browser
  │
  ├─► houseofchaoss.com
  │     Cloudflare DNS → CloudFront → S3 (Angular SPA)
  │
  └─► api.houseofchaoss.com
        Cloudflare DNS + DDoS proxy → EC2 → nginx (SSL termination) → Spring Boot :8080 → RDS MySQL
```

| Component | Technology |
|---|---|
| Backend API | Spring Boot 3.4, Java 17, Gradle |
| Database | MySQL 8 on AWS RDS |
| Frontend host | AWS S3 + CloudFront |
| API host | AWS EC2 + nginx |
| Certificates | ACM (CloudFront), Let's Encrypt via Certbot (API) |
| DNS / DDoS | Cloudflare |
| Secrets | AWS SSM Parameter Store |
| Container registry | GitHub Container Registry (GHCR) |
| CI/CD | GitHub Actions — tests on push/PR, frontend deploy on merge to main |

---

## Infrastructure

### Backend (EC2)
- Spring Boot runs in Docker on EC2 (`t3.micro`, Amazon Linux)
- nginx sits in front as a reverse proxy — handles SSL termination, forwards to `:8080`
- Let's Encrypt certificate issued via Certbot DNS challenge (Cloudflare API), auto-renewing
- Elastic IP ensures the server address is stable across restarts
- Secrets (DB credentials, JWT secret, Cloudinary URL, admin credentials) fetched from SSM Parameter Store at container startup — nothing hardcoded, nothing in environment files

### Frontend (S3 + CloudFront)
- Angular build artifacts in S3, served via CloudFront CDN
- Custom domain `houseofchaoss.com` with ACM-issued HTTPS certificate
- CD pipeline automatically syncs S3 and invalidates CloudFront cache on every push to `main`

### CI/CD (GitHub Actions)
- **Tests:** run on every push and pull request
- **Deploy:** keyless AWS authentication via IAM OIDC — GitHub exchanges a short-lived OIDC token for temporary AWS credentials scoped to this repo and branch; no long-lived access keys stored anywhere
- Docker image built with multi-stage build, cross-compiled for `linux/amd64` from ARM (Apple Silicon), pushed to GHCR

### Security posture
- EC2 security group: SSH locked to a specific IP, HTTP/HTTPS open (nginx handles redirect)
- Cloudflare orange cloud on API subdomain: hides EC2 IP, absorbs DDoS at the edge
- IAM roles follow least-privilege — deploy role can only write to the frontend S3 bucket and invalidate the CloudFront distribution
- SSM parameters encrypted at rest (SecureString for all secrets)

---

## Application Features

### Authentication & Security
- **Stateless JWT** — HMAC-SHA256, 5-minute access tokens
- **Refresh token rotation** — tokens stored hashed in DB, issued as `HttpOnly Secure SameSite=Lax` cookies scoped to `/api/v1/auth`; rotated on every use
- **RBAC** — `ROLE_USER` and `ROLE_ADMIN`; admin routes at `/api/v1/admin/**`
- **BCrypt** password hashing
- **CORS** configured for Angular frontend

### E-commerce Domain
- **Products** — paginated catalog with category filter, new-arrivals, top-deals (cheapest); soft delete; Cloudinary image hosting
- **Cart** — one cart per user (auto-created on registration); add/remove/quantity; stock validation
- **Orders** — create from cart, confirm with shipping address (reduces inventory), cancel, history by status
- **Categories** — admin-managed; delete blocked if products exist
- **Addresses** — per-user shipping address management

### Reviews (Microservice)
- Feign client to a dedicated review microservice on `:8081`
- Create, read, delete reviews per product
- Business rule: review author name must match the user's first name
- Fails gracefully when the microservice is unavailable

### Admin
- Full product and category CRUD
- User management: list all users, promote/demote to admin

### Scheduled Jobs
- Daily: marks products as "not new" after 10 days
- Daily at 3 AM: deletes cancelled orders older than 30 days

### Observability
- Spring Cache on categories and product lists
- RFC 7807 Problem Detail error responses via `GlobalExceptionHandler`
- Jakarta Validation on all request DTOs with field-level error messages

---

## Tech Stack

**Backend:** Java 17, Spring Boot 3.4, Spring Security (OAuth2 Resource Server), Spring Data JPA, MySQL, OpenFeign, Nimbus JOSE JWT, Spring Cache, Spring Scheduling, Cloudinary

**Testing:** JUnit 5, Mockito, Spring Security Test, H2 — unit, integration, and API layers. 81% line coverage.

**DevOps:** Docker, GitHub Actions, AWS (EC2, RDS, S3, CloudFront, ACM, IAM, SSM), nginx, Cloudflare, Certbot, GHCR

---

## Running Locally

### Prerequisites
Java 17+, Docker (for the full stack), or a local MySQL 8 instance.

### Environment variables required

| Variable | Description |
|---|---|
| `JWT_SECRET` | Base64-encoded 32-byte key — `openssl rand -base64 32` |
| `CLOUDINARY_URL` | From your Cloudinary dashboard |
| `DB_USERNAME` | MySQL username |
| `DB_PASSWORD` | MySQL password |

### Option A — Gradle (local MySQL)

```bash
export DB_USERNAME=root
export DB_PASSWORD=yourpassword
export JWT_SECRET=$(openssl rand -base64 32)
export CLOUDINARY_URL=cloudinary://...

./gradlew bootRun
```

### Option B — Docker Compose (API + MySQL)

```bash
cp docker/.env.example docker/.env
# fill in docker/.env

cd docker
docker compose --env-file .env up --build -d

# verify
docker compose ps
curl -s http://localhost:8080/actuator/health
```

On first run the app seeds roles, a default admin (`admin@email.com` / `adminpassword`), 4 categories, and 10 products per category.

### Tests

```bash
./gradlew test

# coverage report
open build/reports/jacoco/test/html/index.html
```

---

## API Reference

### Public

```
POST /api/v1/users/register
POST /api/v1/auth/login
POST /api/v1/auth/refresh
POST /api/v1/auth/logout
GET  /api/v1/products
GET  /api/v1/products/new-arrivals
GET  /api/v1/products/top-deals
GET  /api/v1/categories
```

### Authenticated (Bearer token required)

```
GET    /api/v1/cart
PUT    /api/v1/cart/items/{productId}
POST   /api/v1/cart/items/{cartItemId}/decrease
DELETE /api/v1/cart/items/{cartItemId}

POST   /api/v1/orders
GET    /api/v1/orders/{id}
GET    /api/v1/orders/new
GET    /api/v1/orders/confirmed
PATCH  /api/v1/orders/confirm/{id}
POST   /api/v1/orders/cancel/{id}
DELETE /api/v1/orders/{id}

GET    /api/v1/users/profile
PUT    /api/v1/users/profile

GET    /api/v1/reviews/product/{productId}
POST   /api/v1/reviews
DELETE /api/v1/reviews/{id}
```

### Admin (`ROLE_ADMIN` required)

```
POST   /api/v1/admin/products
PATCH  /api/v1/admin/products/{id}
DELETE /api/v1/admin/products/{id}

POST   /api/v1/admin/categories
DELETE /api/v1/admin/categories/{id}

GET    /api/v1/admin/users
PATCH  /api/v1/admin/users/promote/{id}
PATCH  /api/v1/admin/users/demote/{id}
```

---

## Refresh Token Flow

```
1. POST /auth/login        → access token in body + HttpOnly cookie set
2. Access token expires    → POST /auth/refresh with cookie → new access token
3. Refresh token rotated   → old token invalidated, new cookie set
4. POST /auth/logout       → token deleted from DB, cookie cleared
```

---

## Project Structure

Feature-based package layout under `com.antdevrealm.housechaosmain`:

```
auth/        login, refresh, logout, JWT service, refresh token management
user/        registration, profile
product/     catalog, search, pagination
category/    category management
cart/        cart and cart items
order/       order lifecycle
review/      Feign client to review microservice
admin/       admin controllers
address/     shipping addresses
cloudinary/  image upload
security/    SecurityConfig, CORS, JWT decoder/encoder
exception/   GlobalExceptionHandler, Problem Detail
job/         scheduled cleanup tasks
util/        TokenHasher, shared utilities
```

---

## Related Repositories

- [house-of-chaos-web](https://github.com/AntoanYosifov/house-of-chaos-web) — Angular frontend
- [review-microservice](https://github.com/AntoanYosifov/review-microservice) — Review service
