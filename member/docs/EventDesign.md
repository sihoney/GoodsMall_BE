# Member Event Design

## 발행 이벤트

### `MemberSignedUpEvent`
| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `eventId` | UUID | 이벤트 식별자 |
| `memberId` | UUID | 가입한 회원 ID |
| `email` | String | 가입 이메일 |
| `occurredAt` | Instant | 발생 시각 |

## 발행 흐름
1. `MemberService.createMember` 에서 회원 저장
2. 저장 완료 후 `MemberEventPublisher.publishMemberSignedUp` 호출
3. Spring `ApplicationEventPublisher` 로 애플리케이션 이벤트 발행
4. `MemberSignedUpEventListener` 가 `AFTER_COMMIT` 단계에서 수신
5. `MemberEventKafkaProducer` 가 JSON 직렬화 후 Kafka 토픽으로 발행

## Kafka 설계
| 항목 | 값 |
| --- | --- |
| Producer | `MemberEventKafkaProducer` |
| Topic property | `member.kafka.topic.signed-up` |
| Key | `memberId` |
| Payload | `MemberSignedUpEvent` JSON |
| 보장 | DB 트랜잭션 커밋 이후 발행 |

## 목적
- 다른 서비스가 신규 회원 가입 사실을 비동기로 구독할 수 있게 한다.
- 가입 후 환영 알림, 초기 리소스 생성, 마케팅 자동화 같은 후속 처리를 느슨하게 연결한다.

## 실패 처리
- JSON 직렬화 실패 시 에러 로그 기록
- Kafka 발행 실패 시 콜백에서 에러 로그 기록
- 현재 구현에는 재시도, DLQ, outbox 패턴은 없다

## 후속 확장 포인트
- 회원 탈퇴 이벤트
- 판매자 전환 이벤트
- 제재 생성/해제 이벤트
