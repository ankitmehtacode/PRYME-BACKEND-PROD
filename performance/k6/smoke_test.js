import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8082';

export const options = {
  scenarios: {
    smoke_test: {
      executor: 'constant-vus',
      vus: 100,
      duration: '30s',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<400'],
    'http_req_duration{name:write_duration}': ['p(95)<800'],
  },
};

function readTraffic() {
  const endpoints = [
    '/api/v1/public/products',
    '/api/v1/public/offers/hero',
    '/api/v1/public/reviews',
    '/api/v1/public/banks/partners',
  ];

  const path = endpoints[Math.floor(Math.random() * endpoints.length)];
  const res = http.get(`${BASE_URL}${path}`);
  check(res, { 'read status is <500': (r) => r.status < 500 });
}

function writeTraffic() {
  const payload = JSON.stringify({
    userName: `loadtest-${__VU}-${__ITER}`,
    phone: `${7000000000 + ((__VU + __ITER) % 999999999)}`.slice(0, 10),
    loanAmount: 500000,
    loanType: 'HOME_LOAN',
    metadata: {
      source: 'k6-smoke',
      email: `vu${__VU}_iter${__ITER}@example.com`,
      cibilScore: 742,
    },
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
      'Idempotency-Key': `smoke-${__VU}-${__ITER}`,
    },
    tags: { name: 'write_duration' },
  };

  const res = http.post(`${BASE_URL}/api/v1/public/leads`, payload, params);
  check(res, { 'write status is <500': (r) => r.status < 500 });
}

export default function () {
  const distribution = Math.random();
  if (distribution < 0.7) {
    readTraffic();
  } else if (distribution < 0.9) {
    writeTraffic();
  } else {
    readTraffic();
    writeTraffic();
  }
  sleep(0.1);
}
