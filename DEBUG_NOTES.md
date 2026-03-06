# Debug Notes - Auth and OpenRouter Issues

Date: 2026-03-06
Project: `openrouter` (Spring Boot)

## 1) Symptom Timeline

1. `POST /auth/login` returned `401 Unauthorized`.
2. After fixing login request issues, `POST /api/chat` returned `500 Internal Server Error`.
3. Stack trace showed: `RuntimeException: AI service error: 401 UNAUTHORIZED` from `OpenRouterClient`.
4. App failed to start with:
   - `IllegalStateException: OPENROUTER_API_KEY is missing`.
5. After key setup, app started but `/api/chat` failed with OpenRouter `402 Payment Required`.
6. OpenRouter response said request asked for up to `16384` tokens while account could afford only `3956`.

## 2) Root Causes

### A) `/auth/login` 401

- Login endpoint expects:
  - `username`
  - `password`
- Request body sent was chat-style payload:
  - `{ "message": "Explain polymorphism in Java" }`
- In `AuthService`, missing/invalid credentials led to auth failure path.

### B) `/api/chat` 500 with upstream 401

- App authentication (JWT/API filtering) was not the blocker at this stage.
- Failure occurred when backend called OpenRouter:
  - OpenRouter responded `401 Unauthorized`.
- Main reason: invalid/missing/revoked OpenRouter API key used by backend.

### C) Startup failure: `OPENROUTER_API_KEY is missing`

- `WebClientConfig` now fails fast when API key is empty.
- This is intentional to avoid silent misconfiguration.

### D) OpenRouter `402 Payment Required`

- Upstream provider accepted auth but rejected request due credits/token budget.
- Response clearly indicated token budget mismatch (`16384` requested vs lower affordable amount).
- Main reason: request did not cap `max_tokens`, so provider default was too high for current credit limit.

## 3) Code Changes Applied

## Login and Auth Error Clarity

1. `src/main/java/com/open/openrouter/dto/LoginRequest.java`
   - Added `@NotBlank` validation on:
     - `username`
     - `password`

2. `src/main/java/com/open/openrouter/controller/AuthController.java`
   - Added `@Valid` to login request body.

3. `src/main/java/com/open/openrouter/service/AuthService.java`
   - Replaced generic `RuntimeException` with:
     - `ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password")`

4. `src/main/java/com/open/openrouter/config/SecurityConfig.java`
   - Ensured auth routes are public:
     - `requestMatchers("/auth/**").permitAll()`
   - Allowed `/error` route:
     - `requestMatchers("/error").permitAll()`
   - This prevents some framework error responses from being masked by security.

## OpenRouter Integration Hardening

5. `src/main/java/com/open/openrouter/config/WebClientConfig.java`
   - Removed API key debug print (security risk).
   - Added fail-fast when key is empty:
     - throws `IllegalStateException("OPENROUTER_API_KEY is missing")`

6. `src/main/resources/application.properties`
   - Moved key to env-based config:
     - `openrouter.api-key=${OPENROUTER_API_KEY:}`
   - Avoids hardcoding secrets in source.

7. `src/main/java/com/open/openrouter/client/OpenRouterClient.java`
   - Improved upstream error mapping:
     - OpenRouter `401` -> clear message about API key.
     - OpenRouter `402` -> clear message about credits/billing.
     - Other upstream errors -> structured status message.

8. `src/main/java/com/open/openrouter/dto/OpenRouterRequest.java`
   - Added `max_tokens` field to outbound payload.

9. `src/main/java/com/open/openrouter/service/ChatService.java`
   - Added `openrouter.max-tokens` config usage.
   - Sets `orRequest.setMax_tokens(maxTokens)` for every call.

10. `src/main/resources/application.properties`
   - Added:
     - `openrouter.max-tokens=512`
   - Keeps per-call token requests within a safer budget range.

## 4) Correct Request Flow (Postman)

1. Login first:
   - `POST http://localhost:8090/auth/login`
   - Body:
     ```json
     {
       "username": "admin",
       "password": "password"
     }
     ```
   - Copy returned `token`.

2. Chat request:
   - `POST http://localhost:8090/api/chat`
   - Header:
     - `Authorization: Bearer <token>`
   - Body:
     ```json
     {
       "prompt": "Explain polymorphism in Java"
     }
     ```

## 5) OpenRouter Key Setup (PowerShell)

```powershell
$env:OPENROUTER_API_KEY="sk-or-v1-<your-valid-key>"
mvn spring-boot:run
```

Notes:
- If key was ever exposed publicly, rotate/revoke it and use a new key.
- Verify account billing/credits if you see `402`.
- In IntelliJ, set `OPENROUTER_API_KEY` in Run Configuration environment variables if app fails on startup.

## 6) Diagnostic Checklist for Future

1. Is `/auth/login` body using `username/password` (not `message`)?
2. Did login return a JWT token successfully?
3. Is `Authorization: Bearer <jwt>` included for `/api/chat`?
4. Is `OPENROUTER_API_KEY` set in the same terminal/session running the app?
5. Is the OpenRouter key valid and not revoked?
6. Is `openrouter.max-tokens` set to a safe value (e.g., `256` to `1024`)?
7. Does account have available credits (if `402`)?
8. Check backend logs:
   - Auth failure vs upstream OpenRouter failure are different layers.

## 7) Error Layer Map (Important for Study)

- `401` at `/auth/login`: usually your app input/auth logic.
- `401` inside `OpenRouterClient`: upstream provider auth failure (external API key issue).
- `500` at `/api/chat` with upstream 401 in trace: app wrapped provider error; root cause is still upstream auth.
- Startup `IllegalStateException OPENROUTER_API_KEY is missing`: local config/env issue before app boot completes.
- `402` from OpenRouter: provider billing/token-limit issue, usually fixed by credits or reducing `max_tokens`.
