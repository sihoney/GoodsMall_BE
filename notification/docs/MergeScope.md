# Notification Merge Scope

## 목적
- 이번 merge에 포함되는 구조 변화와 기능 범위를 한 장으로 요약한다.

## 포함 사항
- notification REST API
  - 목록 조회
  - 미읽음 개수 조회
  - 읽음 처리
- SSE 구독 API
- 알림 저장 후 `afterCommit` 기반 SSE push
- `MEMBER_SIGNED_UP` 이벤트 처리
- `ORDER_PAYMENT_RESULT` 이벤트 처리
- `eventId` 기반 중복 방지
- 상태 전이 안정화
  - same-status no-op
  - invalid transition 차단
  - already pushed no-op
- 최소 운영 지표
- 단일 listener + handler registry 기반 consumer 구조
- Kafka DLQ publisher 도입
- SSE emitter stale callback cleanup 방지

## 미포함 사항
- retry job table
- retry scheduler
- DLQ replay 도구
- displayed ack
- 추가 event handler
  - `AUTO_PURCHASE_CONFIRMED`
  - `SELLER_SETTLEMENT_PAYOUT_SUCCEEDED`
  - `SELLER_SETTLEMENT_PAYOUT_FAILED`

## merge 이후 기대 효과
1. consumer 구조 단순화
2. 새 이벤트 추가 시 handler 단위 확장 가능
3. consumer 실패의 DLQ / RETRY / IGNORE 분리
4. SSE 재연결 시 emitter 정리 안정성 향상

## merge 이후 바로 이어질 작업
1. retry 메커니즘 구현
2. DLQ message metadata 확장
3. DLQ replay / 운영 도구 설계
4. metrics 고도화
