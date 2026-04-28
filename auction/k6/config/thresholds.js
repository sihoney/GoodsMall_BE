export const BID_THRESHOLDS = {
  http_req_duration: ['p(90)<500', 'p(99)<2000'],
  http_req_failed: ['rate<0.01'],
};

export const READ_THRESHOLDS = {
  http_req_duration: ['p(90)<300', 'p(99)<1000'],
  http_req_failed: ['rate<0.01'],
};

export const STRESS_THRESHOLDS = {
  http_req_duration: ['p(90)<1000', 'p(99)<5000'],
  http_req_failed: ['rate<0.05'],
};

export const BASE_URL = 'http://localhost:8090';
