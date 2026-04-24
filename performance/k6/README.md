# k6 Load Tests for 3k/4k Concurrency Gates

These scripts implement the Step 2 stress gates defined in `docs/PLATFORM_3000_4000_CONCURRENCY_PLAN.md`.

## Profiles

- `steady_3k.js`: ramps to 3,000 VUs, holds for 30 minutes.
- `peak_4k.js`: ramps to 4,000 VUs, holds for 10 minutes.

Traffic mix is aligned with the platform plan:
- ~70% read APIs (`/api/v1/public/*`, `/actuator/health`)
- ~20% write APIs (`/api/v1/public/leads`)
- ~10% mixed read+write

## Run locally (requires k6)

```bash
BASE_URL=http://localhost:8082 k6 run performance/k6/steady_3k.js
BASE_URL=http://localhost:8082 k6 run performance/k6/peak_4k.js
```

## Run with Dockerized k6 (no host install)

```bash
docker run --rm --network host -e BASE_URL=http://localhost:8082 \
  -v "$PWD:/work" -w /work grafana/k6:0.51.0 run performance/k6/steady_3k.js

docker run --rm --network host -e BASE_URL=http://localhost:8082 \
  -v "$PWD:/work" -w /work grafana/k6:0.51.0 run performance/k6/peak_4k.js
```

## Pass/fail gates

Thresholds are encoded in scripts; k6 exits non-zero when gates fail:
- `http_req_failed < 1%`
- steady p95 all requests `< 400ms`; steady write p95 `< 800ms`
- peak p95 all requests `< 500ms`; peak write p95 `< 900ms`
