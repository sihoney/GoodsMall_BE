# Notification Retry Strategy

## 목적
- SSE 푸시 실패 또는 미전달 상황에서 어떤 기준으로 재시도할지 정리한다.
- 재시도 도입 전에 필요한 선행 조건과 운영 기준을 명확히 한다.

## 현재 상태
- 알림은 저장 후 `afterCommit` 시점에 SSE push를 시도한다.
- `emitter.send(...)`가 예외 없이 끝나면 `PUSHED`로 본다.
- 예외가 발생하면 `FAILED`로 기록한다.
- emitter가 없는 경우는 현재 `STORED`에 머무를 수 있다.
- `RETRYING` enum은 존재하지만 아직 본격적인 재시도 파이프라인은 없다.

## 재시도 대상 상태
| 상태 | 재시도 대상 여부 | 설명 |
| --- | --- | --- |
| `PUSHED` | 아니오 | 이미 서버 관점 성공으로 기록된 상태 |
| `FAILED` | 예 | 가장 명확한 재시도 후보 |
| `RETRYING` | 예 | 재시도 워커가 계속 관리하는 상태 |
| `STORED` | 정책 결정 필요 | emitter 부재 등으로 미전달 가능성이 있는 상태 |

## 권장 상태 흐름
### 실패 후 재시도
- `STORED -> FAILED -> RETRYING -> PUSHED`

### 반복 실패
- `STORED -> FAILED -> RETRYING -> FAILED`

### emitter 부재 기반 재시도
- 선택지 A
  - `STORED` 유지
  - 스케줄러가 오래된 `STORED`를 찾아 `RETRYING`으로 전환 후 재시도
- 선택지 B
  - emitter 부재를 바로 감지하면 `STORED -> RETRYING`
  - 재시도 큐 또는 작업 테이블로 즉시 적재

현재 프로젝트에는 선택지 A가 더 자연스럽다.
- 기존 상태 모델을 크게 바꾸지 않아도 된다.
- 운영자가 DB에서 미전달 후보를 직접 확인하기 쉽다.

## 재시도 도입 전 선행 순서
1. 멱등성 규칙 확정
2. 운영 지표 수집
3. DLQ 기준 분리
4. 재시도 메커니즘 도입
5. backoff, 최대 횟수, 알림 노출 기준 고도화

## 왜 이 순서가 필요한가

### 1. 멱등성 먼저
- 재시도는 같은 작업을 다시 실행하는 구조다.
- `eventId`와 `notificationId` 기준이 정리되지 않으면 중복 저장, 중복 푸시, 잘못된 상태 전이가 생긴다.

### 2. 운영 지표 다음
- 실패 원인이 emitter 부재인지 send 예외인지 모르면 재시도 정책 효과를 평가할 수 없다.
- 지표 없이 재시도를 붙이면 부하만 늘고 개선 여부를 알기 어렵다.

### 3. DLQ 기준 분리
- 복구 가능한 실패와 복구 불가능한 실패를 구분해야 한다.
- 스키마 불일치, 필수 필드 누락 같은 소비 실패는 DLQ로 보내야 한다.
- 일시적 send 실패는 재시도 대상으로 보는 것이 맞다.

## 재시도 메커니즘 후보

### 1. 작업 테이블 + 스케줄러
- 장점
  - DB 중심 운영 가시성이 좋다.
  - 현재 notification 서비스 구조와 잘 맞는다.
  - 초기 구현 난이도가 비교적 낮다.
- 단점
  - 폴링 비용이 있다.
  - 재시도 트래픽이 커지면 확장성 한계가 빨리 온다.

### 2. 재시도 큐
- 장점
  - 비동기 분리가 명확하다.
  - 대량 재시도 처리에 유리하다.
  - retry worker를 독립적으로 확장하기 쉽다.
- 단점
  - 운영 복잡성이 커진다.
  - 큐, DLQ, 지연 재시도 정책을 함께 설계해야 한다.

### 3. 단순 스케줄러 직접 조회
- 장점
  - 가장 빠르게 시작할 수 있다.
- 단점
  - retry metadata 관리가 약하다.
  - 장기적으로 운영 추적성이 떨어진다.

## 현재 프로젝트 권장안
- 1차 도입은 `작업 테이블 + 스케줄러`
- 이후 규모가 커지면 `재시도 큐`로 확장

이유:
- 지금은 상태 전이와 운영 기준을 먼저 안정화하는 단계다.
- 즉시 큐를 도입하는 것보다, 실패 분류와 지표를 먼저 확보하는 편이 리스크가 낮다.

## 기본 정책 제안
1. `PUSHED`는 재시도하지 않는다.
2. `FAILED`는 재시도 후보로 본다.
3. `STORED`가 일정 시간 이상 유지되면 미전달 후보로 집계한다.
4. 재시도 시작 시 `RETRYING`으로 바꾼다.
5. 재시도 성공 시 `PUSHED`, 재시도 실패 시 `FAILED`로 되돌린다.
6. 최대 재시도 횟수 초과 시 DLQ 또는 별도 운영 점검 대상으로 넘긴다.

## 함께 봐야 할 운영 지표
- `notification_push_success_total`
- `notification_push_failure_total`
- `notification_push_emitter_missing_total`
- `notification_retry_started_total`
- `notification_retry_success_total`
- `notification_retry_failure_total`
- `notification_status_stored_total`
- `notification_status_failed_total`

자세한 지표 정의는 [OperationalMetrics.md](C:/my_project/beadv5_2_TodayLunchMenu_BE/notification/docs/OperationalMetrics.md)에서 관리한다.

## 참고 문서
- [StateTransition.md](C:/my_project/beadv5_2_TodayLunchMenu_BE/notification/docs/StateTransition.md)
- [IdempotencyRules.md](C:/my_project/beadv5_2_TodayLunchMenu_BE/notification/docs/IdempotencyRules.md)
- [OpenIssues.md](C:/my_project/beadv5_2_TodayLunchMenu_BE/notification/docs/OpenIssues.md)
- [DeliveryReliabilityRoadmap.md](C:/my_project/beadv5_2_TodayLunchMenu_BE/notification/docs/DeliveryReliabilityRoadmap.md)
- [RetryJobTableSchedulerDesign.md](C:/my_project/beadv5_2_TodayLunchMenu_BE/notification/docs/RetryJobTableSchedulerDesign.md)
