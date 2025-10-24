## Smart Task Management API Reference

This document describes all public REST APIs, request/response schemas, security, error handling, and core components of the Smart Task Management service.

- Base URL: `http://localhost:8080`
- OpenAPI UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

### Conventions
- All JSON responses use a common envelope.
  - Success:
    ```json
    {
      "success": true,
      "message": "...",
      "data": { },
      "meta": { },
      "timestamp": "YYYY-MM-DD HH:mm:ss"
    }
    ```
  - Error:
    ```json
    {
      "code": 429,
      "message": "Too many requests",
      "details": "...",
      "timestamp": "2025-01-01T12:00:00",
      "errors": {
        "field": "validation message"
      }
    }
    ```
- Content-Type: `application/json` for requests with bodies.
- Authentication: Bearer JWT in `Authorization: Bearer <accessToken>` header for protected routes.
- Pagination: `page` (0-based) and `size` query params; sorting via `sort=field,asc|desc`.

## Authentication APIs
All endpoints under `/api/auth/**` are publicly accessible (no JWT required) unless otherwise noted. Some of them operate on the provided token explicitly.

### Register
- Method/Path: `POST /api/auth/register`
- Auth: Public
- Body (AuthenticationRequest):
  ```json
  {
    "userName": "johndoe",
    "password": "P@ssw0rd^",
    "email": "john@example.com",
    "mobileNumber": "01112345678",
    "role": "USER"
  }
  ```
  Notes:
  - Password must include at least one digit, lowercase, uppercase, and special character `!@#&$^()–`.
  - Email must be unique; verification email is sent.
- Responses:
  - 201 Created (BaseResponse<String>)
  - 422 Validation error (field-level messages)
  - 400/500 for malformed input or unexpected errors
- Example:
  ```bash
  curl -X POST http://localhost:8080/api/auth/register \
    -H 'Content-Type: application/json' \
    -d '{
      "userName":"johndoe",
      "password":"P@ssw0rd^",
      "email":"john@example.com"
    }'
  ```

### Authenticate (Login)
- Method/Path: `POST /api/auth/authenticate`
- Auth: Public
- Body (AuthenticationRequest; login uses only `email` and `password`):
  ```json
  {
    "email": "john@example.com",
    "password": "P@ssw0rd^"
  }
  ```
- Success (200 OK):
  ```json
  {
    "success": true,
    "message": "Authentication successful",
    "data": {
      "accessToken": "<jwt>",
      "refreshToken": "<jwt>"
    },
    "timestamp": "..."
  }
  ```
- Errors: 401/422/429/500 as applicable
- Example:
  ```bash
  curl -X POST http://localhost:8080/api/auth/authenticate \
    -H 'Content-Type: application/json' \
    -d '{"email":"john@example.com","password":"P@ssw0rd^"}'
  ```

### Refresh Token
- Method/Path: `POST /api/auth/refreshToken`
- Auth: Public
- Body:
  ```json
  { "refreshToken": "<jwt>" }
  ```
- Success (200 OK): returns new `accessToken` and `refreshToken`.
- Errors: 400/401/404 (invalid or missing refresh token), 500
- Example:
  ```bash
  curl -X POST http://localhost:8080/api/auth/refreshToken \
    -H 'Content-Type: application/json' \
    -d '{"refreshToken":"<refresh>"}'
  ```

### Reset Password
- Method/Path: `POST /api/auth/resetPassword`
- Auth: Public
- Body (either `email` or `userName` must be provided):
  ```json
  {
    "email": "john@example.com",
    "userName": "",
    "newPassword": "N3wP@ssw0rd^"
  }
  ```
- Success (200 OK): message indicating reset completed and account unlocked if applicable.
- Errors: 400/401/422/423 (locked)/500 as applicable
- Example:
  ```bash
  curl -X POST http://localhost:8080/api/auth/resetPassword \
    -H 'Content-Type: application/json' \
    -d '{"email":"john@example.com","newPassword":"N3wP@ssw0rd^"}'
  ```

### Logout
- Method/Path: `POST /api/auth/logout`
- Auth: Public; requires `Authorization: Bearer <accessToken>` header (token to invalidate)
- Success (200 OK): token invalidated
- Error: 498 (custom) on failure, with BaseResponse error body
- Example:
  ```bash
  curl -X POST http://localhost:8080/api/auth/logout \
    -H 'Authorization: Bearer <access>'
  ```

### Kill Session (by ID)
- Method/Path: `POST /api/auth/kill?id=<tokenId>`
- Auth: Public
- Query params: `id` (long) — token row ID
- Success (200 OK): session invalidated
- Error: 498 (custom) on failure
- Example:
  ```bash
  curl -X POST 'http://localhost:8080/api/auth/kill?id=123'
  ```

### Verify Email
- Method/Path: `GET /api/auth/verify-email?token=<uuid>`
- Auth: Public
- Success (200 OK): user enabled and status set active
- Errors:
  - 400 Invalid/expired/used token (BaseResponse error)
  - 500 on unexpected errors
- Example:
  ```bash
  curl 'http://localhost:8080/api/auth/verify-email?token=<uuid>'
  ```

## User APIs

### List Users
- Method/Path: `GET /api/users`
- Auth: Requires role `ADMIN`
- Query params:
  - `page` (default 0)
  - `size` (default 10)
  - `sort` (default `id,asc`) — format `field,asc|desc`
- Success (200 OK):
  ```json
  {
    "success": true,
    "message": "Users retrieved successfully",
    "data": [
      {
        "id": 1,
        "username": "admin",
        "email": "admin@example.com",
        "role": "ADMIN",
        "mobileNumber": "+1234567890",
        "enabled": true,
        "statusId": 1,
        "passwordExpiryDate": "2025-12-31T00:00:00.000+00:00"
      }
    ],
    "meta": {
      "page": 0,
      "size": 10,
      "totalItems": 1,
      "totalPages": 1
    },
    "timestamp": "..."
  }
  ```
- Errors: 401/403/422/429/500 as applicable
- Example:
  ```bash
  curl 'http://localhost:8080/api/users?page=0&size=20&sort=username,desc' \
    -H 'Authorization: Bearer <admin-access>'
  ```

## DTO Schemas

### AuthenticationRequest
Fields and validation:
- `userName` (2-50, `[a-zA-Z0-9_.-]+`)
- `password` (8-100, must contain digit, lowercase, uppercase, special `!@#&$^()–`)
- `email` (valid, <=100)
- `mobileNumber` (matches `^(010|011|012|015)\d{8}$`)
- `role` (`ADMIN|USER`) — for registration; new users are intended to be `USER`.

### AuthResponse
- `accessToken` (string)
- `refreshToken` (string)

### JwtRefreshRequest
- `refreshToken` (string, required)

### ResetPasswordForm
- `email` (string) or `userName` (string) — at least one required
- `newPassword` (same rules as above)

### UserDto
- `id` (long)
- `username` (string)
- `email` (string)
- `role` (`ADMIN|USER`)
- `mobileNumber` (E.164-like; `+` optional, 10-15 digits)
- `enabled` (boolean)
- `statusId` (number)
- `passwordExpiryDate` (date-time)

### Response Envelopes
- Base success response (generic `BaseResponse<T>`)
- Error response (`ErrorResponse`) includes: `code`, `message`, `details`, optional `errors` map, `timestamp`

## Authentication & Authorization
- JWT is used for stateless auth.
- Obtain tokens via `POST /api/auth/authenticate`.
- Send `Authorization: Bearer <accessToken>` to access protected endpoints (e.g., `/api/users`).
- Token validity is configured via properties:
  - `jwt.access.token.validity` (ms)
  - `jwt.refresh.token.validity` (ms)
- Security rules:
  - Permit all: `/api/auth/**`, `/v3/api-docs/**`, `/swagger-ui/**`, `/swagger-ui.html`
  - `ROLE_ADMIN` required: `/api/admin/**`
  - `ROLE_USER` or `ROLE_ADMIN`: `/tasks/**`
  - All others: authenticated

Example (JavaScript fetch):
```javascript
const res = await fetch('http://localhost:8080/api/users?page=0&size=10', {
  headers: { Authorization: `Bearer ${accessToken}` }
});
const body = await res.json();
```

## Rate Limiting
- Applied to all `/api/**` endpoints via an interceptor.
- Default limit: **10 requests per 60 seconds** per client IP and endpoint.
- Exceeding the limit throws a `429 Too Many Requests` with an `ErrorResponse` body.
- Customize per endpoint/class using `@RateLimited`:

```java
import com.task.annotation.RateLimited;

@RateLimited(value = "/api/auth/authenticate", requests = 5, duration = 60)
@PostMapping("/authenticate")
public ResponseEntity<BaseResponse<AuthResponse>> authenticate(@RequestBody @Valid AuthenticationRequest request) {
  // ...
}
```

## Error Handling
Centralized via `GlobalExceptionHandler` with consistent `ErrorResponse` format. Common cases:
- 401 Unauthorized (authentication failures, invalid tokens)
- 403 Forbidden (access denied)
- 404 Not Found
- 405 Method Not Allowed
- 422 Unprocessable Entity (validation)
- 429 Too Many Requests (rate limit)
- 400 Bad Request (malformed JSON, type mismatches, missing params)
- 500 Internal Server Error (uncaught)

Example validation error (422):
```json
{
  "code": 422,
  "message": "Unprocessable entity",
  "details": "Validation failed for 2 field(s)",
  "errors": {
    "email": "Email should be valid",
    "password": "Password must contain at least one digit, one lowercase, one uppercase letter and one special character"
  },
  "timestamp": "2025-01-01T12:00:00"
}
```

## Developer Components (Public Services & Config)

### Services
- AuthService
  - `void register(AuthenticationRequest request)` — creates user, sends verification email.
  - `AuthResponse login(AuthenticationRequest request)` — validates credentials, returns tokens.
  - `void unlockAccounts()` — scheduled job to unlock after 1 hour.
- TokenService
  - `void saveToken(String access, String refresh, Long userId)` — persists tokens.
  - `void logout(String accessToken)` — invalidates the access token.
  - `LogoutResponse kill(long tokenId)` — invalidates by token row ID.
  - `AuthResponse getUserNameFromTokenUsingRefreshToken(String refreshToken)` — rotates tokens.
- JwtService
  - `String generateToken(User user)`; `String generateRefreshToken(User user)`
  - `Boolean isTokenValid(String token, User user)`; `String extractUserEmail(String token)`
- JwtUserDetailsService
  - `User loadUserByEmail(String email)`; `UserDetails loadUserByUsername(String username)`
  - `void resetUserPassword(ResetPasswordForm form)` — sets new password and extends expiry.
  - `void updatePassword(User user, String oldPassword, String newPassword)`
  - `void updateProfile(User user, String email, String mobile)`
- UserService
  - `Page<UserDto> getAllUsers(Pageable pageable)`
- VerificationTokenService
  - `VerificationToken createVerificationToken(User user)`
  - `void verifyEmailToken(String token)` — enables user
- RateLimitService
  - `void checkRateLimit(String ip, String key)` — default limit
  - `void checkRateLimit(String ip, String key, int maxAttempts, int windowSeconds)`

### Security & Filters
- SecurityConfig: configures CSRF off, stateless sessions, route permissions, and registers `JwtAuthFilter`.
- JwtAuthFilter: extracts `Authorization` header, validates JWT, and populates security context.

### Rate Limiting Infrastructure
- WebMvcConfig: applies `RateLimitInterceptor` to `/api/**`.
- RateLimitInterceptor: enforces default or `@RateLimited`-customized limits per client IP + endpoint key.
- RateLimitConfig: Caffeine cache backing store; defaults: `MAX_ATTEMPTS=10`, `ATTEMPT_WINDOW_SECONDS=60`.

## Example Client Flow
1. Register user via `/api/auth/register`.
2. Verify email via link `/api/auth/verify-email?token=...`.
3. Authenticate to get tokens: `/api/auth/authenticate`.
4. Call protected APIs with `Authorization: Bearer <accessToken>`.
5. Refresh tokens when needed: `/api/auth/refreshToken`.
6. Logout to invalidate the current access token: `/api/auth/logout`.

## Notes
- Swagger UI is enabled; prefer it for interactive exploration during development.
- Token lifetimes and base URL are configured in `application.properties`.
