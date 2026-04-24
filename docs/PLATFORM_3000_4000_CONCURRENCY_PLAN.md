# GoPryme Execution Plan (Steps 1-4) for 3k Sustained / 4k Peak Users

This repository now includes the concrete baseline changes required to move from audit-only guidance to executable hardening.

## Step 1 — Degimmickification (implemented)

### Applied changes
- Replaced custom bounded platform-thread async pool with Java 21 virtual-thread-per-task executor in `LifecycleConfig`.
- Reduced oversized JVM-side DB pooling assumptions and made pool sizing explicitly env-driven for Neon/PgBouncer.
- Retuned Tomcat defaults to avoid massive pinned platform-thread counts while supporting high connection fan-in.

### Why
- The old `ThreadPoolTaskExecutor` (`core=20`, `max=500`, queue `4000`) introduced queue latency and complex backpressure behavior.
- Neon with PgBouncer should not be fronted by a very large app-side idle pool.

## Step 2 — Systemic Debug + Stress Gate (implemented tooling)

### Delivered assets
- `performance/k6/steady_3k.js` for 3,000-user steady profile.
- `performance/k6/peak_4k.js` for 4,000-user peak profile.
- `scripts/loadtest_gate.sh` to run both gates via Dockerized k6.

### Required load profile
- **Steady state target:** 3,000 concurrent users for 30 minutes.
- **Peak target:** 4,000 concurrent users for 5-10 minute spikes.

### Runbook
1. Start Redis/backend/nginx with production compose and prod profile.
2. Run closed-model scenario:
   - 70% read APIs (`/api/public/*`, `/api/recommendation/*`)
   - 20% CRM writes (`/api/crm/*`)
   - 10% document/session paths
3. Capture and enforce gates:
   - P95 < 400 ms for reads
   - P95 < 800 ms for writes
   - Error rate < 1%
   - Hikari active <= `DB_POOL_MAX`
   - No sustained DB connection timeout bursts

## Step 3 — Modular Monolith Shift (what to change next)

### Priority changes
- Add explicit read/query paths for recommendation/public traffic so CRM/eligibility writes do not contend with read-heavy queries.
- Add fetch-join/entity-graph strategy where repositories fan out and create N+1 behavior during CRM list endpoints.
- Move cross-domain side effects to outbox-only async consumers so write transaction scope remains short.
- Enforce pagination on admin/high-cardinality endpoints (no unpaged `findAll` reads).
- Cap recommendation candidate scans and avoid per-loan-type query fan-out on public product grid APIs.

## Step 4 — HA + Edge Performance (what to change next)

### Priority changes
- Add HPA/replica strategy and session-cookie compatibility validation behind nginx.
- Enable CDN caching headers for immutable static assets on frontend deploy artifact.
- Add OpenTelemetry trace export for request-path + DB wait visibility.

## Configuration envelope for 3k/4k target

Use these baseline values as starting point before load-test tuning:

```env
DB_POOL_MAX=24
DB_POOL_MIN_IDLE=0
TOMCAT_MAX_THREADS=300
TOMCAT_MAX_CONNECTIONS=4096
```

Then tune based on observed `hikaricp.connections.*`, CPU saturation, and p95/p99 latency.
