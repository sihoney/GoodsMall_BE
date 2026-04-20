# Notification DLQ Implementation Plan

## 목적
- notification 서비스에 DLQ 기준과 구현 순서를 명확히 정리한다.
- 재시도 대상과 DLQ 직행 대상을 코드 구조에서 분리할 수 있도록 준비한다.
- 이후 retry job table + scheduler 구현 전에 실패 분류 기준을 팀 공통 기준으로 맞춘다.

## 배경
- 현재 notification 서비스는 Kafka consumer가 이벤트를 받아 알림을 저장하고, 저장 이후 SSE push를 시도한다.
- 지금 단계에서는 최소 운영 지표가 먼저 도입되었고, 다음 단계로 DLQ 기준을 코드 구조에 반영하려는 상태다.
- 핵심은 "어떤 실패는 바로 DLQ로 보내고, 어떤 실패는 재시도 대상으로 남길 것인가"를 명확히 구분하는 것이다.

## 이번 단계 목표
1. consumer 실패를 `DLQ 직행`과 `재시도 후보`로 분류하는 기준을 확정한다.
2. 이 기준을 코드에서 표현할 수 있는 최소 구조를 추가한다.
3. 실제 Kafka DLQ 토픽 발행 전에도 로깅 기반 기본 구현으로 흐름을 검증할 수 있게 만든다.
4. 이후 retry 전략과 자연스럽게 연결되도록 문서와 테스트를 함께 정리한다.

## 진행 순서

### 1. 현재 consumer 흐름 재확인
- Kafka consumer가 어떤 이벤트를 수신하는지 확인
- 이벤트 검증이 어디서 일어나는지 확인
- 예외가 어디서 발생할 수 있는지 확인
  - 역직렬화
  - payload validation
  - application service 호출
  - DB 저장

산출물:
- 현재 실패 지점 목록
- 실패 지점별 대응 후보 표

### 2. DLQ 분류 기준 확정

#### DLQ 직행 대상
- 역직렬화 실패
- schema mismatch
- 필수 필드 누락
- enum 매핑 불가
- 복구 불가능한 payload 오류
- 계약 자체가 잘못된 이벤트

설명:
- 같은 메시지를 다시 처리해도 성공 가능성이 낮은 실패들이다.
- 재시도보다 격리와 운영 확인이 우선이다.

#### 재시도 후보
- 일시적 DB 오류
- 외부 시스템 일시 오류
- 일시적 application service 처리 오류
- SSE push 실패
- emitter 부재 기반 후속 전달 시도

설명:
- 나중에 다시 시도하면 성공 가능성이 있는 실패들이다.
- 즉시 DLQ로 보내기보다 retry 정책으로 넘기는 쪽이 맞다.

산출물:
- 실패 유형별 `DLQ` / `RETRY` / `IGNORE` 분류표

### 3. 코드 구조 설계
- consumer 예외 분류기 추가
- DLQ 발행 인터페이스 추가
- 기본 DLQ publisher는 로깅 기반으로 시작
- 분류 결과를 표현하는 객체 또는 enum 추가

후보 구조:
- `NotificationConsumerFailureDecision`
- `NotificationConsumerExceptionClassifier`
- `NotificationDlqPublisher`
- `LoggingNotificationDlqPublisher`

목표:
- 지금은 Kafka DLQ 인프라 없이도 구조를 먼저 만든다.
- 이후 실제 DLQ 토픽 발행 구현으로 교체하기 쉽게 만든다.

### 4. 최소 코드 반영
- consumer에서 예외 발생 시 classifier로 분류
- 분류 결과가 `DLQ`이면 publisher로 위임
- 분류 결과가 `RETRY`이면 상위 재시도 정책이 처리할 수 있도록 예외 유지 또는 별도 전환
- 분류 결과가 `IGNORE`이면 중복/정책상 무시 가능한 케이스로 처리

이번 단계 범위:
- 완전한 메시지 재구동 구조까지는 하지 않는다.
- 먼저 "실패 분류 경계가 코드에서 드러나는 상태"를 만든다.

### 5. 문서 동기화
- DLQ 전략 문서 추가 또는 기존 문서 보강
- 아래 문서와 연결
  - [RetryStrategy.md](C:/my_project/beadv5_2_TodayLunchMenu_BE/notification/docs/RetryStrategy.md)
  - [OpenIssues.md](C:/my_project/beadv5_2_TodayLunchMenu_BE/notification/docs/OpenIssues.md)
  - [DeliveryReliabilityRoadmap.md](C:/my_project/beadv5_2_TodayLunchMenu_BE/notification/docs/DeliveryReliabilityRoadmap.md)

추천:
- 별도 문서 `DLQStrategy.md` 추가

### 6. 테스트 및 검증
- DLQ 대상 예외는 publisher가 호출되는지 검증
- 재시도 후보 예외는 DLQ로 보내지지 않는지 검증
- 정상 이벤트는 기존 흐름이 유지되는지 검증
- 마지막에 `:notification:test` 실행

## 이번 단계 산출물
1. consumer 예외 분류 기준 코드
2. DLQ publisher 인터페이스
3. 로깅 기반 기본 DLQ publisher
4. consumer 테스트
5. DLQ 관련 문서

## 구현 범위 제안
- 너무 크게 벌리지 않고 아래까지만 우선 진행한다.
1. exception classifier
2. DLQ publisher interface
3. logging publisher
4. test
5. document update

## 다음 단계 연결
- DLQ 분류 구조가 들어가면 그 다음에는 retry 구현으로 자연스럽게 넘어간다.
- 다음 후보 작업:
1. `notification_retry_job` 엔티티 및 migration
2. retry scheduler
3. `FAILED -> RETRYING -> PUSHED` 자동화

## 참고 문서
- [RetryStrategy.md](C:/my_project/beadv5_2_TodayLunchMenu_BE/notification/docs/RetryStrategy.md)
- [StateTransition.md](C:/my_project/beadv5_2_TodayLunchMenu_BE/notification/docs/StateTransition.md)
- [OperationalMetrics.md](C:/my_project/beadv5_2_TodayLunchMenu_BE/notification/docs/OperationalMetrics.md)
- [DeliveryReliabilityRoadmap.md](C:/my_project/beadv5_2_TodayLunchMenu_BE/notification/docs/DeliveryReliabilityRoadmap.md)
