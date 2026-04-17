# Member Status Design

## 목적
- 회원 상태를 단순 값이 아니라 명확한 상태 머신으로 정의한다.
- 인증, 로그인, 주문, 판매자 전환, 운영 제재에서 동일한 기준으로 회원 상태를 해석한다.
- OAuth, 이메일/휴대폰 인증 도입 전에 회원 활성화 조건을 먼저 정리한다.

## 1차 적용 범위
- 본 문서는 member 모듈의 1차 회원 상태 정의를 다룬다.
- 휴면(`DORMANT`) 상태는 이번 범위에서 제외한다.
- 상태 전이는 member-service 내부 도메인 규칙으로 통제한다.

## 상태 정의

### `PENDING_VERIFICATION`
- 회원 기본 정보는 생성되었지만 계정 활성화가 완료되지 않은 상태
- 이메일 또는 휴대폰 인증이 완료되기 전 단계
- 로그인은 제한한다
- 주문, 결제, 판매자 등록 등 인증 기반 기능 사용을 제한한다

### `ACTIVE`
- 정상적으로 인증 및 활성화가 완료된 상태
- 로그인 가능
- 일반 사용자 기능 사용 가능
- 역할이 `SELLER` 인 경우 판매자 기능도 사용 가능

### `SUSPENDED`
- 운영 정책 위반, 신고 처리, 관리자 제재 등으로 이용이 정지된 상태
- 로그인은 원칙적으로 제한한다
- 기존 세션/토큰은 정책에 따라 즉시 무효화할 수 있다
- 제재 해제 시 `ACTIVE` 로 복귀 가능하다

### `WITHDRAWN`
- 회원이 탈퇴를 요청하여 서비스 이용 종료가 확정된 상태
- 로그인 불가
- 재활성화하지 않는다
- 개인정보 보관 정책에 따라 일정 기간 후 `DELETED` 로 이관할 수 있다

### `DELETED`
- 개인정보 삭제 또는 식별 불가 처리까지 완료된 최종 상태
- 로그인 불가
- 다른 상태로 복귀하지 않는다
- 운영/감사 목적 최소 정보만 별도 정책에 따라 남길 수 있다

## 상태별 정책

| 상태 | 로그인 | 토큰 재발급 | 주문/결제 | 판매자 기능 | 관리자 상태 변경 |
| --- | --- | --- | --- | --- | --- |
| `PENDING_VERIFICATION` | 불가 | 불가 | 불가 | 불가 | 가능 |
| `ACTIVE` | 가능 | 가능 | 가능 | 역할 충족 시 가능 | 가능 |
| `SUSPENDED` | 불가 | 불가 | 불가 | 불가 | 가능 |
| `WITHDRAWN` | 불가 | 불가 | 불가 | 불가 | 제한적 |
| `DELETED` | 불가 | 불가 | 불가 | 불가 | 불가 |

## 상태 전이 규칙

### 허용 전이
- `PENDING_VERIFICATION -> ACTIVE`
  - 이메일 또는 휴대폰 인증 완료
- `ACTIVE -> SUSPENDED`
  - 운영 제재 적용
- `SUSPENDED -> ACTIVE`
  - 제재 해제
- `ACTIVE -> WITHDRAWN`
  - 회원 탈퇴 요청 확정
- `WITHDRAWN -> DELETED`
  - 보관 기간 종료 또는 개인정보 삭제 처리 완료
- `PENDING_VERIFICATION -> DELETED`
  - 인증 미완료 계정 정리 또는 가입 취소 처리

### 금지 전이
- `WITHDRAWN -> ACTIVE`
- `DELETED -> ACTIVE`
- `DELETED -> ANY`
- `SUSPENDED -> PENDING_VERIFICATION`

## 상태 전이표

| 현재 상태 | 이벤트 | 다음 상태 | 비고 |
| --- | --- | --- | --- |
| `PENDING_VERIFICATION` | 인증 완료 | `ACTIVE` | 이메일/휴대폰 인증 성공 |
| `PENDING_VERIFICATION` | 미인증 계정 정리 | `DELETED` | 배치 또는 관리자 정책 |
| `ACTIVE` | 운영 제재 | `SUSPENDED` | 신고 승인, 정책 위반 등 |
| `ACTIVE` | 탈퇴 요청 확정 | `WITHDRAWN` | 사용자 주도 |
| `SUSPENDED` | 제재 해제 | `ACTIVE` | 관리자 처리 |
| `WITHDRAWN` | 개인정보 삭제 완료 | `DELETED` | 최종 종료 |

## 도메인 규칙
- 회원 생성 직후 기본 상태는 `PENDING_VERIFICATION` 이다.
- 인증 성공 전에는 access token 발급 대상이 아니다.
- `SUSPENDED`, `WITHDRAWN`, `DELETED` 상태 회원은 로그인 및 refresh token 재발급을 허용하지 않는다.
- 판매자 역할(`SELLER`) 여부와 회원 상태는 별개로 관리한다.
- 회원 상태 변경은 `changeStatus()` 단순 호출이 아니라 허용 전이 검증을 거쳐야 한다.

## 구현 가이드

### 엔티티
- `MemberStatus` enum 확장
- `Member` 엔티티에 상태 전이 검증 메서드 추가
- 단순 setter 성격의 상태 변경은 지양하고 허용 전이 기반 메서드로 대체

### 인증/인가
- 회원가입 직후 상태를 `PENDING_VERIFICATION` 로 저장
- 로그인 시 `ACTIVE` 상태만 허용
- refresh token 재발급 시에도 상태를 다시 검증
- gateway 에서는 JWT 유효성만 검사하고, 회원 상태 검증 책임은 member-service 에 둔다

### 운영 기능
- 관리자 제재는 `SUSPENDED` 로 처리
- 탈퇴는 즉시 물리 삭제하지 않고 `WITHDRAWN` 을 거친다
- `DELETED` 전환 시 개인정보 파기 정책과 함께 설계한다

## 보류 항목
- `DORMANT` 휴면 상태 도입 여부
- 장기 미인증 계정 자동 삭제 배치 주기
- 탈퇴 후 재가입 시 이메일/휴대폰 재사용 정책
- 상태 변경 이력 테이블 별도 관리 여부

## 권장 후속 작업
1. `MemberStatus` enum 및 DB 스키마 확장
2. 로그인/재발급 서비스에 상태 검증 반영
3. 이메일 또는 휴대폰 인증 완료 시 `PENDING_VERIFICATION -> ACTIVE` 전이 구현
4. 탈퇴 정책 문서와 개인정보 삭제 정책 문서 분리
