#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8082}"
K6_IMAGE="${K6_IMAGE:-grafana/k6:0.51.0}"

if ! command -v docker >/dev/null 2>&1; then
  echo "docker is required for this gate script" >&2
  exit 1
fi

echo "[gate] running steady 3k scenario against ${BASE_URL}"
docker run --rm --network host \
  -e BASE_URL="${BASE_URL}" \
  -v "$PWD:/work" -w /work \
  "${K6_IMAGE}" run performance/k6/steady_3k.js

echo "[gate] running peak 4k scenario against ${BASE_URL}"
docker run --rm --network host \
  -e BASE_URL="${BASE_URL}" \
  -v "$PWD:/work" -w /work \
  "${K6_IMAGE}" run performance/k6/peak_4k.js

echo "[gate] PASS: both concurrency gates met"
