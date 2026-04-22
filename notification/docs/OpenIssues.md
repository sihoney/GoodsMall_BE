# Notification Open Issues

## 목적
- 현재 merge 범위에 포함된 것과 아직 남아 있는 것을 분리해서 정리한다.

## 이번 merge에 포함된 내용
- `MEMBER_SIGNED_UP` 이벤트 처리
- `ORDER_PAYMENT_RESULT` 이벤트 처리
- 단일 listener + handler registry 기반 consumer 구조
- consumer failure classifier 도입
- Kafka DLQ publisher 도입
- 알림 저장
- 목록 조회 API
- 미읽음 개수 API
- 읽음 처리 API
- SSE 구독 API
- 저장 후 `afterCommit` 기반 SSE push
- 최소 운영 지표 도입
- SSE emitter stale callback cleanup 방지

## 아직 남아 있는 과제

### 1. retry 메커니즘 미구현
- 상태 전이 문서에는 `FAILED -> RETRYING -> PUSHED` 흐름이 정리돼 있다.
- 하지만 실제 retry job table + scheduler는 아직 구현되지 않았다.

### 2. delivery retry 정책 미확정
- emitter 부재 시 현재는 `STORED` 유지가 기본 흐름이다.
- 이후 이것을
  - 즉시 retry 대상
  - 일정 시간 후 retry 대상
중 어떤 정책으로 볼지 확정이 필요하다.

### 3. DLQ replay 도구 미구현
- 현재는 DLQ topic 발행까지만 들어갔다.
- DLQ 적재 메시지를 재분석하거나 재처리하는 운영 도구는 아직 없다.

### 4. DLQ 메시지 스키마 고도화 필요
- 현재 DLQ 메시지는 기본 메타데이터를 담지만, 더 보강할 수 있다.
- 후보:
  - `eventId`
  - `traceId`
  - `eventType`
  - 원본 topic

### 5. displayed ack 없음
- 현재 `PUSHED`는 서버 기준 `SseEmitter.send(...)` 성공이다.
- 브라우저 렌더링 완료나 사용자 확인까지는 보장하지 않는다.

### 6. 추가 이벤트 미연결
- `AUTO_PURCHASE_CONFIRMED`
- `SELLER_SETTLEMENT_PAYOUT_SUCCEEDED`
- `SELLER_SETTLEMENT_PAYOUT_FAILED`

### 7. metrics 고도화 미완료
- 최소 counter는 들어갔다.
- gauge, timer, dashboard, alert rule은 후속 작업이다.

## 현재 운영 관점 핵심 체크포인트
1. DLQ topic에 어떤 reason이 누적되는가
2. `STORED`가 오래 남는 알림이 많은가
3. `FAILED`가 특정 이벤트 타입에 집중되는가
4. emitter missing이 지속적으로 증가하는가

## merge 범위 밖 항목
- retry scheduler
- retry job table
- DLQ replay tooling
- displayed ack
- 추가 event handler

## 추천 다음 단계
1. retry job table + scheduler 구현
2. DLQ message metadata 확장
3. DLQ replay 정책 수립
4. metrics 고도화 및 dashboard 정의

## 관련 문서
- [RetryStrategy.md](C:/my_project/beadv5_2_TodayLunchMenu_BE/notification/docs/RetryStrategy.md)
- [RetryJobTableSchedulerDesign.md](C:/my_project/beadv5_2_TodayLunchMenu_BE/notification/docs/RetryJobTableSchedulerDesign.md)
- [OperationalMetrics.md](C:/my_project/beadv5_2_TodayLunchMenu_BE/notification/docs/OperationalMetrics.md)
- [DLQStrategy.md](C:/my_project/beadv5_2_TodayLunchMenu_BE/notification/docs/DLQStrategy.md)
- [MergeScope.md](C:/my_project/beadv5_2_TodayLunchMenu_BE/notification/docs/MergeScope.md)
