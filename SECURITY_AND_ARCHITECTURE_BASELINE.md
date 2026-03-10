# Security & Modular Monolith Baseline

## Modular Monolith Boundaries
The backend is organized by domain modules inside one deployable unit:
- `iam`: identity, auth, session issuance
- `bankconfig`: admin-managed bank catalog
- `loanproduct`: product matrix and eligibility predicates
- `recommendation`: public recommendation read-model
- `crm`: lead/application operations
- `calculators`, `eligibility`: financial/public compute modules
- `common`, `config`: cross-cutting policies only

Rule: modules communicate through services/contracts, not by bypassing to unrelated controllers.

## Finance-Grade Security Controls
- BCrypt password hashing (`PasswordEncoder`) for user credentials.
- Stateless security policy + route-level authorization in `SecurityConfig`.
- Strict CORS allowlist (`localhost:3000`, `pryme.in`) only.
- Secure headers:
  - Content-Security-Policy
  - HSTS
  - Frame options
- Global API error contract to avoid stacktrace leakage.

## Multi-Session Handler (High-Level)
- SessionManager issues cryptographically strong bearer tokens.
- Maximum 3 active sessions per user (oldest session evicted).
- Session expiry and explicit logout invalidation supported.
- Session cache used to reduce lookup latency.

## Smooth Frontend Operation Wiring
The backend now exposes clean admin endpoints suitable for frontend button actions:
- Create bank: `POST /api/v1/admin/banks`
- Update bank: `PUT /api/v1/admin/banks/{id}`
- Toggle visibility: `PATCH /api/v1/admin/banks/{id}/visibility`
- Delete bank: `DELETE /api/v1/admin/banks/{id}`
- List banks: `GET /api/v1/admin/banks`

And public recommendation read endpoint:
- `GET /api/v1/public/banks/recommendation?salary=<x>&cibil=<y>`

## Caching Strategy
- `banks:all` cache for admin list reads.
- `banks:recommendation` cache for public recommendation combinations.
- write operations evict dependent caches to avoid stale data.

Additional frontend wiring endpoints:
- Hero cards feed: `GET /api/v1/public/offers/hero`
- Product selector feed: `GET /api/v1/public/products`
- Partner bank marquee feed: `GET /api/v1/public/banks/partners`
- Apply button submit: `POST /api/v1/public/leads` (supports `Idempotency-Key` header, requires `loanType`)
- Admin lead inbox: `GET /api/v1/admin/leads?page=0&size=20`

- CRM pipeline API: `GET /api/v1/admin/applications`, `PATCH /api/v1/admin/applications/{applicationId}/status`, `PATCH /api/v1/admin/applications/{applicationId}/assign`
  - Supports optional optimistic-lock `version` in payload to prevent overwrite races.

- Customer reviews feed: `GET /api/v1/public/reviews`
- Review CMS admin: `GET/POST/PUT/DELETE /api/v1/admin/reviews`

- High-precision eligibility engine: `POST /api/v1/public/eligibility/lnt-lap/best-match` (BigDecimal-based; returns best-match + evaluated program set).

- Eligibility factors include conditions, EMI-obligation months, min/max loan caps, and special notes in program response metadata.

- Prepayment ROI engine: `POST /api/v1/calculators/prepayment/roi` (strategy-wise interest saved + time trimmed with BigDecimal amortization).
