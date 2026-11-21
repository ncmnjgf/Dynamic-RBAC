# Dynamic RBAC Authorization Engine

A production-grade, database-driven **Role-Based Access Control (RBAC)** authorization engine built with **Spring Boot 3.3.5**, **Spring Security 6**, and **H2 Database**.

---

## 📌 Project Overview

Traditional Spring Security applications enforce security using static, compile-time annotations such as:
```java
// ❌ HARDCODED ROLE CHECKS (NOT ALLOWED IN THIS ARCHITECTURE)
@PreAuthorize("hasRole('ADMIN')")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
```
This rigid pattern requires code modification, testing, and deployment whenever access policies or roles evolve.

This project delivers a **dynamic, database-driven RBAC engine** where:
- Roles and permissions are stored in relational database tables.
- Decisions are made at runtime via Spring Security's method-level security with a custom `PermissionEvaluator`.
- **Zero hardcoded role names** exist in the authorization layer.
- Changing permissions or assigning roles takes immediate effect without application restarts.

---

## 🏛️ Architecture & Database Design

### Relational Schema (H2 In-Memory)

```
┌──────────────┐          ┌───────────────────────┐          ┌───────────────────┐
│     USER     │ 1      * │       USER_ROLE       │ *      1 │       ROLE        │
├──────────────┤──────────├───────────────────────┤──────────├───────────────────┤
│ id           │          │ id                    │          │ id                │
│ username     │          │ user_id (FK -> User)  │          │ name              │
│ password     │          │ role_id (FK -> Role)  │          │ (e.g., 'ADMIN')   │
└──────────────┘          └───────────────────────┘          └─────────┬─────────┘
                                                                       │ 1
                                                                       │
                                                                       │ *
                                                             ┌─────────┴─────────┐
                                                             │  ROLE_PERMISSION  │
                                                             ├───────────────────┤
                                                             │ id                │
                                                             │ role_id (FK)      │
                                                             │ permission_id (FK)│
                                                             └─────────┬─────────┘
                                                                       │ *
                                                                       │
                                                                       │ 1
                                                             ┌─────────┴─────────┐
                                                             │    PERMISSION     │
                                                             ├───────────────────┤
                                                             │ id                │
                                                             │ name              │
                                                             │(e.g., ROLE_CREATE)│
                                                             └───────────────────┘
```

### Core Entities:
1. **`User`**: System actors with credentials (`username`, BCrypt-hashed `password`).
2. **`Role`**: Logical groupings of privileges (`ADMIN`, `USER`, `MANAGER`, etc.).
3. **`Permission`**: Atomic privileges (`ROLE_CREATE`, `PERMISSION_CREATE`, `USER_ROLE_ASSIGN`, `ROLE_PERMISSION_ASSIGN`, `SECURE_DATA_READ`).
4. **`UserRole`**: Junction table mapping users to roles (many-to-many).
5. **`RolePermission`**: Junction table mapping roles to permissions (many-to-many).

---

## 🔄 Authorization Flow & Runtime Evaluation

```
[ Client Request ]
       │  (e.g., POST /roles with Basic Auth header)
       ▼
[ SecurityFilterChain ]
       │  Stateless HTTP Basic Authentication
       ▼
[ CustomUserDetailsService ]
       │  Loads UserDetails & dynamically resolves granted permissions
       ▼
[ Controller Method Interception ]
       │  @PreAuthorize("hasPermission(#request, 'ROLE_CREATE')")
       ▼
[ MethodSecurityExpressionHandler ]
       │  Delegates to CustomPermissionEvaluator
       ▼
[ CustomPermissionEvaluator.hasPermission(...) ]
       │  1. Resolves authenticated username from Authentication token.
       │  2. Executes JPQL query to retrieve dynamic permissions:
       │     SELECT DISTINCT p.name FROM Permission p
       │     JOIN RolePermission rp ON rp.permission.id = p.id
       │     JOIN UserRole ur ON ur.role.id = rp.role.id
       │     WHERE ur.user.username = :username
       │  3. Verifies if requested permission ('ROLE_CREATE') exists in set.
       ├───► Granted  -> Method execution proceeds (200 OK / 201 Created)
       └───► Denied   -> Throws AccessDeniedException -> GlobalExceptionHandler -> 403 Forbidden
```

---

## 🛡️ Why Hardcoded Roles are Avoided

1. **Flexibility & Agility**: Privilege changes and new roles can be created and reconfigured dynamically via REST APIs without rebuilding or redeploying code.
2. **Principle of Least Privilege**: Access is granted based on atomic permissions (e.g., `SECURE_DATA_READ`), not broad, coarse-grained roles.
3. **Multi-Tenant & Enterprise Scale**: Different tenants or departments can create custom role matrices mapped to standardized system permissions.

---

## 🔌 API Endpoints & Dynamic Permissions

| HTTP Method | Endpoint | Functional Role | Dynamic Permission Checked | Example Annotation |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/roles` | ADMIN | `ROLE_CREATE` | `@PreAuthorize("hasPermission(#request, 'ROLE_CREATE')")` |
| `POST` | `/permissions` | ADMIN | `PERMISSION_CREATE` | `@PreAuthorize("hasPermission(#request, 'PERMISSION_CREATE')")` |
| `POST` | `/roles/{roleId}/permissions/{permissionId}` | ADMIN | `ROLE_PERMISSION_ASSIGN` | `@PreAuthorize("hasPermission(null, 'ROLE_PERMISSION_ASSIGN')")` |
| `POST` | `/users/{userId}/roles/{roleId}` | ADMIN | `USER_ROLE_ASSIGN` | `@PreAuthorize("hasPermission(null, 'USER_ROLE_ASSIGN')")` |
| `GET` | `/secure-data` | USER | `SECURE_DATA_READ` | `@PreAuthorize("hasPermission(null, 'SECURE_DATA_READ')")` |

---

## 🚀 How to Run the Application

### Prerequisites:
- **Java 17** (or 21)
- Maven (or use included `mvnw.cmd` / `./mvnw`)

### 1. Build & Run Tests
```powershell
.\mvnw.cmd clean test
```

### 2. Start Application
```powershell
.\mvnw.cmd spring-boot:run
```
The server will start on `http://localhost:8080`.

### 3. Interactive Swagger / OpenAPI Documentation
Once the application is running, access Swagger UI in your browser:
- **Swagger UI URL:** `http://localhost:8080/swagger-ui.html` (or `http://localhost:8080/swagger-ui/index.html`)
- **OpenAPI JSON Spec:** `http://localhost:8080/v3/api-docs`

> **Note on Testing in Swagger UI:**  
> Click the **Authorize** button (lock icon at the top right) in Swagger UI to test with different users:
> - **Admin:** Username: `admin` | Password: `admin123`
> - **Regular User:** Username: `user` | Password: `user123`

### 4. Pre-Seeded Default Users & Credentials
Upon startup, `DataInitializer` seeds:
- **Admin**:
  - Username: `admin`
  - Password: `admin123`
  - Permissions: `ROLE_CREATE`, `PERMISSION_CREATE`, `ROLE_PERMISSION_ASSIGN`, `USER_ROLE_ASSIGN`, `SECURE_DATA_READ`
- **User**:
  - Username: `user`
  - Password: `user123`
  - Permissions: `SECURE_DATA_READ`

### 5. H2 Web Console
- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:dynamic_rbac_db`
- Username: `sa`
- Password: *(leave empty)*

---

## 🧪 Example API Calls (cURL / PowerShell)

### 1. Create a Role (`POST /roles`)
```bash
curl -u admin:admin123 -X POST http://localhost:8080/roles \
  -H "Content-Type: application/json" \
  -d '{"name": "AUDITOR"}'
```

### 2. Create a Permission (`POST /permissions`)
```bash
curl -u admin:admin123 -X POST http://localhost:8080/permissions \
  -H "Content-Type: application/json" \
  -d '{"name": "AUDIT_LOG_READ"}'
```

### 3. Assign Permission to Role (`POST /roles/{roleId}/permissions/{permissionId}`)
```bash
curl -u admin:admin123 -X POST http://localhost:8080/roles/3/permissions/6
```

### 4. Assign Role to User (`POST /users/{userId}/roles/{roleId}`)
```bash
curl -u admin:admin123 -X POST http://localhost:8080/users/2/roles/3
```

### 5. Access Secure Data (`GET /secure-data`)
```bash
# Permitted (user has SECURE_DATA_READ permission) -> 200 OK
curl -u user:user123 http://localhost:8080/secure-data

# Denied check (attempting admin endpoint with regular user credentials) -> 403 Forbidden
curl -u user:user123 -X POST http://localhost:8080/roles \
  -H "Content-Type: application/json" \
  -d '{"name": "TEST"}'
```

---

## 📊 Test Coverage & Quality Metrics

- **Unit & Integration Tests**: 28 total tests across all layers (`@SpringBootTest`, MockMvc, Mockito, SecurityContext).
- **Code Coverage**: **91% Instruction Coverage** (exceeds the 80% mandatory requirement) verified with **JaCoCo**.
- JaCoCo Report location: `target/site/jacoco/index.html`.


