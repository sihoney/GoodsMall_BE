# Notification Auth Design

## 목적
- 알림 조회 API 는 로그인 회원만 접근 가능해야 한다.
- notification 서비스는 직접 JWT 를 검증하기보다 공통 인증 컨텍스트를 사용하는 구조를 전제로 한다.

## 인증 흐름
1. 클라이언트가 게이트웨이에 Bearer 토큰과 함께 요청
2. 게이트웨이에서 JWT 검증
3. 게이트웨이가 `X-Member-Id`, `X-Member-Role` 헤더 추가
4. 공통 보안 모듈이 notification 서비스 내부에서 `AuthenticatedMember` 생성
5. `@CurrentMember` 로 컨트롤러에서 현재 회원 식별

## 보호 대상 API
| Path | 설명 |
| --- | --- |
| `/api/notifications` | 내 알림 조회 |
| `/api/notifications/unread-count` | 미읽음 개수 조회 |
| `/api/notifications/{notificationId}/read` | 읽음 처리 |

## 설계 원칙
- 알림은 항상 요청자 본인 `memberId` 기준으로 조회한다.
- 읽음 처리 시에도 소유자 검증을 다시 수행한다.
- 관리자 전용 알림 API 는 아직 없다.
