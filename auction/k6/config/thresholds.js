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

// K8s ClusterIP 직접 접근 (EC2 노드에서 실행 시)
// 로컬 실행 시: http://localhost:8090
export const BASE_URL = 'http://10.43.214.112:8090';
export const MEMBER_URL = 'http://10.43.207.139:8083';
