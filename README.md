###  In development ###

A REST-based backend for an e-commerce platform focused on antiques.  
Implements stateless authentication with short-lived **JWT access tokens** and a **refresh-token** flow.

<img width="1920" height="1080" alt="image" src="https://github.com/user-attachments/assets/cc2ba1f1-bb5f-484f-a38e-8cc44b6f9a96" />


---

## Planned Features

- ✅ RESTful endpoints for products, categories, carts, orders, and users  
- 🔐 Spring Security with JWT (short-lived access token) + refresh token rotation  
- 🧰 Validation with `jakarta.validation` (Spring Boot starter)  
- 📊 Actuator health/info endpoints  
- 🗄️ JPA/Hibernate with MySQL  
- ♻️ Devtools for hot reload  
- 🧪 JUnit 5 + Spring Boot Test
- 💱 Currency microservice to fetch FX rates and display real-time prices in **EUR** and **USD**  
- 📦 Inventory & stock reservations  
- 📨 Email/notification service (order confirmations, password reset)  
- 🗺 API documentation (Springdoc OpenAPI)

---

## Tech Stack

- **Language:** Java 17  
- **Framework:** Spring Boot **3.4.10**  
- **Build:** Gradle  
- **Persistence:** Spring Data JPA (Hibernate), **MySQL**  
- **Security:** Spring Security, **JJWT** (`io.jsonwebtoken`)  
- **Validation:** `spring-boot-starter-validation`  
- **Observability:** Spring Boot Actuator  
- **Utilities:** Lombok, Devtools  
- **Testing:** Spring Boot Test, JUnit Platform
