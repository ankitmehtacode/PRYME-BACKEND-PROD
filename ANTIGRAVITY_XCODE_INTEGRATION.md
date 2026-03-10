# Antigravity Frontend + Xcode/Swift Client Integration Guide

This backend already exposes REST APIs for public and admin flows.
Use this guide to connect either:
- a Google Antigravity web frontend, or
- an iOS/Xcode Swift client.

## 1) Backend base URL
- Local: `http://localhost:8080`
- Public APIs: `/api/v1/public/**`
- Auth APIs: `/api/v1/auth/**`
  - Public: `POST /api/v1/auth/login`
  - Protected: `GET /api/v1/auth/me`, `POST /api/v1/auth/logout`, `GET /api/v1/auth/sessions/{userId}`
- Admin APIs: `/api/v1/admin/**`

## 2) CORS configuration (web frontend)
CORS is controlled by:
- `app.security.allowed-origins` in `application.yml`

Example:
```yaml
app:
  security:
    allowed-origins: http://localhost:3000,http://localhost:5173,https://your-antigravity-app.web.app
```

## 3) Core endpoint map for frontend wiring
- Partner bank marquee: `GET /api/v1/public/banks/partners`
- Product selector: `GET /api/v1/public/products`
- Hero offers: `GET /api/v1/public/offers/hero`
- Public reviews: `GET /api/v1/public/reviews`
- Lead submit: `POST /api/v1/public/leads`
- Eligibility best match: `POST /api/v1/public/eligibility/lnt-lap/best-match`
- Prepayment ROI: `POST /api/v1/calculators/prepayment/roi`
- My applications: `GET /api/v1/applications/me`
- Upload KYC document: `POST /api/v1/documents/upload` (multipart: `applicationId`, `docType`, `file`)
- Verify identity number: `POST /api/v1/documents/verify-id`

Admin:
- Applications list: `GET /api/v1/admin/applications`
- Assign lead: `PATCH /api/v1/admin/applications/{applicationId}/assign`
- Update status: `PATCH /api/v1/admin/applications/{applicationId}/status`

## 4) Frontend route/auth loop (React)
- Use `integration_templates/frontend/src/contexts/AuthContext.tsx` as the auth bootstrap pattern.
- Use `integration_templates/frontend/src/components/auth/ProtectedRoute.tsx` for role-guarded routes.
- Keep token in `localStorage` key `pryme_token`, then call `/api/v1/auth/me` on app boot to restore session.
- Listen to browser events `pryme:session-expired` and `pryme:login-success` for auth pop-up/toast UX.

## 5) Swift/Xcode client pattern
Use `URLSession` with JSON request/response models.
- Set `Authorization: Bearer <token>` for protected routes.
- Decode response DTOs to typed `Codable` structs.
- For retry-safe lead submit, send `Idempotency-Key` header.
- Document upload must use `multipart/form-data` with server-validated MIME types (`application/pdf`, `image/jpeg`, `image/png`).

## 6) Production checklist
- Enable lead WAL backup path (`app.leads.backup-dir`) so inbound lead payloads are durably journaled before DB commit.
- Put backend behind HTTPS only.
- Restrict `allowed-origins` to exact production hosts.
- Move secrets (JWT keys, DB credentials) to environment variables.
- Add API monitoring and request logging.


## 7) Production concurrency profile
- Optimized defaults are included for a 2 vCPU / 8 GB instance profile:
  - Tomcat tuned (`max-threads: 200`, `accept-count: 500`, `max-connections: 8192`)
  - Hikari pool tuned (`maximum-pool-size: 24`, `minimum-idle: 8`)
  - Session lifecycle cleanup scheduler enabled for in-memory token hygiene
- Expose health and metrics via actuator endpoints:
  - `/actuator/health`
  - `/actuator/metrics`
  - `/actuator/prometheus`
