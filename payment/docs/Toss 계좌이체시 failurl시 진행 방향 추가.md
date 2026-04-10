# Toss 계좌이체 failUrl 처리 방향

## 1) 배경
- Toss 계좌이체 결제 과정에서 사용자가 결제창에서 취소하거나 오류가 발생하여 `failUrl`로 리디렉션되는 경우가 있습니다.
- 현재 시스템에서는 `failUrl` 수신 시 별도의 상태 전이가 없어, 해당 charge가 여전히 `PENDING` 상태로 남아있게 됩니다.
- 이로 인해 사용자와 관리자 모두 결제 실패 여부를 명확히 알기 어려운 상황이 발생하고 있었습니다.

## 2) 최종 정책 요약
| 항목 | 정책 |
|---|---|
| `failUrl` 수신 시 | 해당 charge를 `PENDING -> REDIRECT_FAILED`로 전이하고 실패 사유/시간 저장 |
| `REDIRECT_FAILED` 이후 | 해당 charge는 종료 상태로 간주, 추가 상태 전이 없음 |
| `confirm` 성공 시 | `PENDING -> CONFIRM_SUCCESS` |
| `confirm` 실패 시 | `PENDING -> CONFIRM_FAILED` |
| 재시도 | 기존 charge 재사용이 아니라 새 charge 생성 후 새 결제 시도 |

## 3) 상태 정의
| 상태 | 의미 | 전이 가능 |
|---|---|---|
| `PENDING` | 결제 요청 생성 완료, 승인 대기 | `REDIRECT_FAILED`, `CONFIRM_SUCCESS`, `CONFIRM_FAILED`, `CANCELLED` |
| `REDIRECT_FAILED` | Toss 결제창 단계에서 실패 리디렉션 수신 | 없음(종료) |
| `CONFIRM_SUCCESS` | 서버 confirm 성공 | 없음(종료) |
| `CONFIRM_FAILED` | 서버 confirm 실패 | 없음(종료) |
| `CANCELLED` | 승인 전 취소 | 없음(종료) |

## 4) API 역할
| API                              | 역할 | 입력 핵심값 | 결과 |
|----------------------------------|---|---|---|
| `POST /api/payments/confirm`     | Toss 승인(confirm) 수행 | `chargeId`, `paymentKey`, `orderId`, `amount` | 성공 시 `CONFIRM_SUCCESS`, 실패 시 `CONFIRM_FAILED` + 예외 응답 |
| `POST /api/payments/charge/fail` | failUrl 실패 기록 | `orderId`, `code`, `message` | `PENDING`이면 `REDIRECT_FAILED` 저장, 이미 `REDIRECT_FAILED`면 멱등 반환 |

## 5) 코드 반영 사항
| 변경 영역 | 내용 |
|---|---|
| `ChargeStatus` | `REDIRECT_FAILED`, `CONFIRM_SUCCESS`, `CONFIRM_FAILED` 상태 사용 |
| `Charge` 엔티티 | `failAtRedirect(...)` 추가, `approve()`는 `CONFIRM_SUCCESS`, `fail()`은 `CONFIRM_FAILED`로 저장 |
| 실패 저장 서비스 | `ConfirmChargeFailureService`에서 `orderId` 기준 조회 후 redirect 실패 전이 |
| 저장 조회 경로 | `ChargeRepository.findByPgOrderId(...)` 추가 |

## 6) 프론트 처리 가이드
1. Toss `failUrl`로 돌아오면 `orderId`, `code`, `message`를 추출한다.
2. `POST /api/payments/charge/fail` 호출로 백엔드에 실패를 기록한다.
3. 응답 상태가 `REDIRECT_FAILED`이면 해당 charge는 종료된 것으로 처리한다.
4. 사용자가 다시 충전하면 새 charge를 생성해서 다시 결제 시도한다.

---
