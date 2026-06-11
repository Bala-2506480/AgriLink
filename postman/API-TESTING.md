# AgriLink — Identity & Access API Testing Guide

End-to-end Postman guide for the IAM module: **Session**, **User**, and **Role** management.

- **Base URL:** `http://localhost:8081`
- **Auth:** JWT Bearer token (obtained from login)
- **Content-Type:** `application/json` for all requests with a body
- **Collection:** import [`AgriLink-IdentityAccess.postman_collection.json`](AgriLink-IdentityAccess.postman_collection.json)

---

## 1. Setup

1. Start MySQL and the app (`mvnw spring-boot:run`). On first boot, `DataSeeder` seeds the 6 roles and one admin user.
2. In Postman, **Import** the collection file above.
3. Open the collection's **Variables** tab and confirm:

   | Variable | Default | Notes |
   |---|---|---|
   | `baseUrl` | `http://localhost:8081` | change if your port differs |
   | `token` | *(empty)* | auto-filled by the Login request |
   | `roleId` | `1` | target role for role GET/PUT |
   | `userId` | `1` | target user for user GET/PUT |

4. Run **Session → Login (admin)** first. Its test script stores the JWT in `{{token}}`, so every other request is pre-authorized.

### Seeded roles

| roleId | roleName | Description |
|---|---|---|
| 1 | `AgriLinkAdmin` | Full administrative access |
| 2 | `ExtensionOfficer` | Field officer — registers/verifies farmers |
| 3 | `ProcurementOfficer` | Manages crop procurement |
| 4 | `SubsidyAdmin` | Reviews/approves subsidy applications |
| 5 | `ComplianceAnalyst` | Audits actions, ensures compliance |
| 6 | `Farmer` | Manages own crop plans/subsidy requests |

### Seeded admin

| Email | Password |
|---|---|
| `admin@agrilink.com` | `Admin@1234` |

---

## 2. Authentication flow

```
POST /session/login        → returns accessToken (15 min) + refreshToken (7 days)
   use accessToken as:  Authorization: Bearer <accessToken>
POST /session/refresh      → exchange refreshToken for a new accessToken (rotates refreshToken)
POST /session/logout       → revokes all active sessions for the caller
```

Add this header to every protected request:

```
Authorization: Bearer {{token}}
```

---

## 3. Endpoint reference

15 endpoints total — **6 POST, 4 GET, 3 PUT, 2 DELETE**.

### Access matrix

| Endpoint | Method | Allowed roles |
|---|---|---|
| `/session/login` | POST | public |
| `/session/refresh` | POST | public |
| `/session/logout` | POST | any authenticated |
| `/user/createUser` | POST | `AgriLinkAdmin` (any role), `ExtensionOfficer` (Farmer only) |
| `/user` | GET | `AgriLinkAdmin` |
| `/user/{id}` | GET | `AgriLinkAdmin` |
| `/user/{id}` | PUT | `AgriLinkAdmin` |
| `/user/{id}` | DELETE | `AgriLinkAdmin` — **soft delete** (deactivate) |
| `/role/createRole` | POST | `AgriLinkAdmin` |
| `/role` | GET | `AgriLinkAdmin` |
| `/role/{id}` | GET | `AgriLinkAdmin` |
| `/role/{id}` | PUT | `AgriLinkAdmin` |
| `/role/{id}` | DELETE | `AgriLinkAdmin` — **hard delete** (blocked if users assigned) |

---

## 4. Session

### 4.1 Login — `POST /agriLink/session/login`  *(public)*

**Request**
```json
{
  "email": "admin@agrilink.com",
  "password": "Admin@1234"
}
```

**Response `200 OK`**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "Yh9aF3k...",
  "expiresIn": 900,
  "userId": 1,
  "roleName": "AgriLinkAdmin",
  "regionId": 1
}
```

**Postman test script** (Tests tab) — auto-saves the token:
```javascript
var json = pm.response.json();
if (json.accessToken) pm.collectionVariables.set("token", json.accessToken);
pm.test("login ok", () => pm.response.to.have.status(200));
```

**Errors**
| Cause | Status | Body |
|---|---|---|
| Wrong email/password | `401` | `{ "message": "Invalid email or password" }` |
| Suspended account | `401` | `{ "message": "Account is suspended. Contact your administrator." }` |

---

### 4.2 Refresh — `POST /agriLink/session/refresh`  *(public)*

**Request**
```json
{ "refreshToken": "Yh9aF3k..." }
```

**Response `200 OK`** — same shape as login, with a **new** `accessToken` and a **rotated** `refreshToken`.

**Errors**
| Cause | Status | Body |
|---|---|---|
| Unknown / already-rotated token | `401` | `{ "message": "Invalid refresh token" }` |
| Expired token | `401` | `{ "message": "Refresh token expired. Please log in again." }` |

---

### 4.3 Logout — `POST /agriLink/session/logout`  *(authenticated)*

Header: `Authorization: Bearer {{token}}` — no body.

**Response `200 OK`**
```json
{ "message": "Logged out successfully" }
```

---

## 5. User

### 5.1 Create user — `POST /agriLink/user/createUser`

Allowed for `AgriLinkAdmin` (any role) and `ExtensionOfficer` (only `roleId` = Farmer).

**Request**
```json
{
  "roleId": 2,
  "name": "Ravi Kumar",
  "email": "ravi@agrilink.com",
  "password": "Ravi@1234",
  "phone": "9876543210",
  "regionId": 7
}
```

**Response `201 Created`**
```json
{ "message": "User created successfully" }
```

**Errors**
| Cause | Status | Body |
|---|---|---|
| Missing/invalid field | `400` | `{ "message": "Validation failed", "details": { "email": "Enter a valid email address" } }` |
| Duplicate email | `409` | `{ "message": "Email already registered" }` |
| `roleId` does not exist | `404` | `{ "message": "Role not found with id: 99" }` |
| Assigning an inactive role | `409` | `{ "message": "Cannot assign an inactive role" }` |
| ExtensionOfficer creating a non-Farmer | `403` | `{ "message": "ExtensionOfficer can only create Farmer accounts" }` |
| Caller lacks Admin/Officer role | `403` | *(Spring Security)* |

> **Validation rules:** `roleId` required; `name` required; `email` required + valid; `password` required, min 8 chars; `phone` required, **exactly 10 digits** (`\d{10}`). `regionId` optional.

> **Status values (everywhere):** `A` = Active, `I` = Inactive, `S` = Suspended. Roles use only `A`/`I`; users use `A`/`I`/`S`.

---

### 5.2 List users — `GET /agriLink/user`  *(Admin)*

**Response `200 OK`**
```json
[
  { "userId": 1, "name": "System Administrator", "email": "admin@agrilink.com",
    "phone": "0000000000", "roleName": "AgriLinkAdmin", "regionId": 1,
    "status": "A", "createdAt": "2026-06-09T09:00:00" },
  { "userId": 2, "name": "Ravi Kumar", "email": "ravi@agrilink.com",
    "phone": "9876543210", "roleName": "ExtensionOfficer", "regionId": 7,
    "status": "A", "createdAt": "2026-06-09T10:15:30" }
]
```

---

### 5.3 Get user by id — `GET /agriLink/user/{id}`  *(Admin)*

`GET /agriLink/user/2` → `200 OK` with a single user object (same shape as above).

**Error:** `404` → `{ "message": "User not found with id: 99" }`

---

### 5.4 Update user — `PUT /agriLink/user/{id}`  *(Admin)*

Partial update — only the fields you send are changed.

**Request** (e.g. reassign role + suspend)
```json
{
  "roleId": 6,
  "regionId": 12,
  "status": "S"
}
```

**Response `200 OK`**
```json
{ "message": "User updated successfully" }
```

**Updatable fields:** `name`, `phone` (10 digits), `regionId`, `roleId`, `status` (`A` | `I` | `S`).

**Errors**
| Cause | Status |
|---|---|
| `status` not in {`A`,`I`,`S`}, or `phone` not 10 digits | `400` |
| `roleId` not found | `404` |
| User id not found | `404` |
| Assigning an inactive role | `409` |

---

### 5.5 Delete user — `DELETE /agriLink/user/{id}`  *(Admin)*

**Soft delete** — sets the user's `status` to `I` (Inactive) and revokes their active sessions. The record is kept. No request body.

`DELETE /agriLink/user/2`

**Response `200 OK`**
```json
{ "message": "User deactivated successfully" }
```

**Error:** user id not found → `404` → `{ "message": "User not found with id: 99" }`

> The deactivated user can no longer authenticate (login is rejected for `status = I`) and existing tokens stop working once their sessions are revoked.

---

## 6. Role

### 6.1 Create role — `POST /agriLink/role/createRole`  *(Admin)*

> The 6 standard roles are seeded automatically. Use this only to add a new custom role.

**Request**
```json
{
  "roleName": "RegionalCoordinator",
  "description": "Coordinates officers within a region"
}
```

**Response `201 Created`**
```json
{ "message": "Role created successfully" }
```

**Error:** duplicate name → `409` → `{ "message": "Role already exists: RegionalCoordinator" }`

---

### 6.2 List roles — `GET /agriLink/role`  *(Admin)*

**Response `200 OK`**
```json
[
  { "roleId": 1, "roleName": "AgriLinkAdmin", "description": "Full administrative access", "status": "A" },
  { "roleId": 2, "roleName": "ExtensionOfficer", "description": "Field officer — registers/verifies farmers", "status": "A" }
]
```

---

### 6.3 Get role by id — `GET /agriLink/role/{id}`  *(Admin)*

`GET /agriLink/role/2` → `200 OK` with a single role object.

**Error:** `404` → `{ "message": "Role not found with id: 99" }`

---

### 6.4 Update role — `PUT /agriLink/role/{id}`  *(Admin)*

Partial update.

**Request**
```json
{
  "description": "Updated role description",
  "status": "A"
}
```

**Response `200 OK`**
```json
{ "message": "Role updated successfully" }
```

**Updatable fields:** `roleName`, `description`, `status` (`A` | `I`).

---

### 6.5 Delete role — `DELETE /agriLink/role/{id}`  *(Admin)*

**Hard delete** — permanently removes the role, but only if **no users** are assigned to it. No request body.

`DELETE /agriLink/role/7`

**Response `200 OK`**
```json
{ "message": "Role deleted successfully" }
```

**Errors**
| Cause | Status | Body |
|---|---|---|
| Role id not found | `404` | `{ "message": "Role not found with id: 99" }` |
| Users still assigned to the role | `409` | `{ "message": "Role is assigned to one or more users and cannot be deleted" }` |

---

## 7. End-to-end test scenarios

### Scenario A — Admin onboards an officer, officer onboards a farmer
1. **Login (admin)** → `{{token}}` set.
2. **Create user** with `roleId: 2` (ExtensionOfficer) → `201`.
3. **Login** as the new officer (set `email`/`password` in the login body) → re-stores `{{token}}`.
4. **Create user** with `roleId: 6` (Farmer) → `201`. ✅ allowed.
5. **Create user** with `roleId: 4` (SubsidyAdmin) → `403` *"ExtensionOfficer can only create Farmer accounts"*. ✅ blocked.

### Scenario B — Token lifecycle
1. **Login** → save `refreshToken`.
2. **Refresh** with that token → new `accessToken`, rotated `refreshToken`.
3. **Refresh** again with the *old* token → `401 Invalid refresh token`. ✅ rotation works.
4. **Logout** → `200`. Subsequent **Refresh** → `401`. ✅ sessions revoked.

### Scenario C — Authorization boundaries
1. As a **Farmer** token, call **List users** (`GET /user`) → `403`. ✅ admin-only.
2. With **no** `Authorization` header, call any protected endpoint → `401/403`. ✅.

---

## 8. HTTP status cheat-sheet

| Status | Meaning in this API |
|---|---|
| `200 OK` | Successful GET / PUT / DELETE / login / logout |
| `201 Created` | Successful create (user/role) |
| `400 Bad Request` | Validation failure (`@Valid`) |
| `401 Unauthorized` | Bad credentials / invalid-expired token |
| `403 Forbidden` | Authenticated but not allowed (wrong role / officer rule) |
| `404 Not Found` | Referenced id does not exist |
| `409 Conflict` | Duplicate email/role name, or inactive-role assignment |
| `500` | Unexpected server error |
