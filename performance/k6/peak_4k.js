import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8082';

export const options = {
  scenarios: {
    peak_mix: {
      executor: 'ramping-vus',
      startVUs: 500,
      stages: [
        { duration: '5m', target: 4000 },
        { duration: '10m', target: 4000 },
        { duration: '5m', target: 500 },
        { duration: '5m', target: 0 },
      ],
      gracefulRampDown: '30s',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<500'],
    'http_req_duration{name:write_duration}': ['p(95)<900'],
  },
};

function readTraffic() {
  const endpoints = [
    '/actuator/health',
    '/api/v1/public/products',
    '/api/v1/public/offers/hero',
    '/api/v1/public/reviews',
  ];

  const path = endpoints[Math.floor(Math.random() * endpoints.length)];
  const res = http.get(`${BASE_URL}${path}`);
  check(res, { 'peak read status <500': (r) => r.status < 500 });
}

function writeTraffic() {
  const payload = JSON.stringify({
    userName: `peak-${__VU}-${__ITER}`,
    phone: `${7100000000 + ((__VU + __ITER) % 899999999)}`.slice(0, 10),
    loanAmount: 750000,
    loanType: 'LAP',
    metadata: {
      source: 'k6-peak',
      cibilScore: 710,
      monthlyIncome: 120000,
    },
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
      'Idempotency-Key': `peak-${__VU}-${__ITER}`,
    },
    tags: { name: 'write_duration' },
  };

  const res = http.post(`${BASE_URL}/api/v1/public/leads`, payload, params);
  check(res, { 'peak write status <500': (r) => r.status < 500 });
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
  sleep(0.08);
}
