import { Counter } from 'k6/metrics';

export const bidSuccess    = new Counter('bid_success');
export const bidRejected   = new Counter('bid_rejected');
export const bidServerError = new Counter('bid_server_error');

/**
 * 입찰 응답을 분류하여 메트릭에 기록한다.
 *
 * 201 → bid_success
 * 4xx → bid_rejected (status 태그로 구분: 400/409/422)
 * 5xx → bid_server_error + 콘솔 출력
 */
export function recordBidResult(res) {
  if (res.status === 201) {
    bidSuccess.add(1);
  } else if (res.status >= 400 && res.status < 500) {
    bidRejected.add(1, { status: String(res.status) });
  } else if (res.status >= 500) {
    bidServerError.add(1, { status: String(res.status) });
    console.error(`SERVER ERROR: status=${res.status} body=${res.body?.substring(0, 200)}`);
  }
}
