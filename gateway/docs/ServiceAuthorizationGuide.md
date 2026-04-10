# Service Authorization Guide

## 개요

- 이 문서는 각 서비스 모듈에서 권한 검증을 구현하는 공통 방법을 설명한다.
- 프로젝트의 기본 원칙은 다음과 같다.
  - 게이트웨이: JWT 검증과 1차 접근 제어
  - 각 서비스: 역할 검증, 소유자 검증, 도메인 정책 검증

## 기본 원칙

- `401 Unauthorized`: 인증 실패
  - 예: 토큰 없음, 토큰 파싱 실패, 인증 헤더 해석 실패
- `403 Forbidden`: 권한 부족
  - 예: 관리자 권한 없음, 판매자 권한 없음, 소유자 아님
- 서비스 내부 권한 검증은 공통 `RoleGuard` 를 사용한다.
- 권한 부족 예외는 공통 `AuthorizationDeniedException` 으로 처리한다.
- 서비스별 `ExceptionHandler` 는 `AuthorizationDeniedException` 을 받아 `ACCESS_DENIED` 를 반환한다.

## 사용 구성 요소

| 구성 요소 | 위치 | 역할 |
| --- | --- | --- |
| `AuthenticatedMember` | `common-security` | 현재 인증 사용자 정보 |
| `@CurrentMember` | `common-security` | 컨트롤러에서 현재 사용자 주입 |
| `RoleGuard` | `common-security` | 역할/소유권 검증 |
| `AuthorizationDeniedException` | `common-security` | 권한 부족 공통 예외 |

## 처리 흐름

1. 게이트웨이가 JWT 를 검증하고 `X-Member-Id`, `X-Member-Role` 헤더를 전달한다.
2. 각 서비스는 `@CurrentMember AuthenticatedMember` 로 현재 사용자를 주입받는다.
3. 서비스 메서드에서 `RoleGuard` 로 권한을 검증한다.
4. 권한이 부족하면 `AuthorizationDeniedException` 이 발생한다.
5. 서비스별 `ExceptionHandler` 가 이를 받아 `403 + ACCESS_DENIED` 로 응답한다.

## 컨트롤러 사용법

```java
@GetMapping("/me")
public ResponseEntity<ApiResponse<MemberResponse>> getCurrentMember(
        @CurrentMember AuthenticatedMember authenticatedMember
) {
    return ResponseEntity.ok(ApiResponse.success(
            memberUsecase.getCurrentMember(authenticatedMember.memberId())
    ));
}
```

- 컨트롤러에서는 가능하면 권한 검증 로직을 직접 두지 않는다.
- 현재 사용자 식별 정보는 서비스 계층으로 전달한다.

## 서비스에서 권한 검증하는 방법

### 1. 관리자 전용 기능

```java
public MemberRestrictionResponse createRestriction(
        AuthenticatedMember authenticatedMember,
        CreateMemberRestrictionRequest request
) {
    RoleGuard.requireAdmin(authenticatedMember);
    validateCreateRequest(request);
    return memberRestrictionAppender.create(authenticatedMember, request);
}
```

사용 시점:
- 관리자 전용 API
- 신고 검토, 제재 생성/해제, 운영 기능

### 2. 판매자 또는 관리자 기능

```java
public ProductResponse createProduct(
        AuthenticatedMember authenticatedMember,
        CreateProductRequest request
) {
    RoleGuard.requireSellerOrAdmin(authenticatedMember);
    return productAppender.create(authenticatedMember, request);
}
```

사용 시점:
- 판매자 도메인 기능
- 상품 등록/수정/삭제
- 정산 조회/생성

### 3. 본인 또는 관리자 기능

```java
public NotificationResponse readNotification(
        AuthenticatedMember authenticatedMember,
        UUID ownerId
) {
    RoleGuard.requireOwnerOrAdmin(authenticatedMember, ownerId);
    return notificationReader.read(ownerId);
}
```

사용 시점:
- 본인 자원 접근
- 알림 조회/읽음 처리
- 내 주문 조회
- 내 프로필 수정

### 4. 인증 사용자 여부만 확인

```java
public void someAuthenticatedOnlyAction(AuthenticatedMember authenticatedMember) {
    RoleGuard.requireAuthenticated(authenticatedMember);
}
```

사용 시점:
- 역할 구분 없이 로그인만 필요할 때

## ExceptionHandler 규칙

각 서비스 모듈은 아래 핸들러를 포함해야 한다.

```java
@ExceptionHandler(AuthorizationDeniedException.class)
public ResponseEntity<ApiResponse<Object>> handleAuthorizationDenied(
        AuthorizationDeniedException exception
) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ApiResponse.fail("ACCESS_DENIED", exception.getMessage()));
}
```

## 하지 말아야 할 패턴

- 서비스마다 `AdminAccessDeniedException`, `SellerAccessDeniedException` 같은 역할 전용 예외를 새로 만들지 않는다.
- 서비스 코드에서 직접 `authenticatedMember.role() == ...` 비교를 반복하지 않는다.
- 컨트롤러에서 비즈니스 권한 검증을 직접 수행하지 않는다.
- 인증 실패와 인가 실패를 같은 예외/응답 코드로 섞지 않는다.

## 권장 구현 순서

1. 컨트롤러에서 `@CurrentMember AuthenticatedMember` 를 받는다.
2. 서비스 메서드 첫 부분에서 `RoleGuard.require...()` 를 호출한다.
3. 이후 도메인 소유권, 상태, 존재 여부를 검증한다.
4. 서비스 `ExceptionHandler` 에 `AuthorizationDeniedException` 처리를 추가한다.

## 예시 기준

- 관리자 기능 예시: [MemberRestrictionService.java](/c:/my_project/beadv5_2_TodayLunchMenu_BE/member/src/main/java/com/example/member/application/service/MemberRestrictionService.java)
- 관리자 기능 예시: [MemberReportService.java](/c:/my_project/beadv5_2_TodayLunchMenu_BE/member/src/main/java/com/example/member/application/service/MemberReportService.java)
- 공통 예외 처리 예시: [MemberExceptionHandler.java](/c:/my_project/beadv5_2_TodayLunchMenu_BE/member/src/main/java/com/example/member/presentation/exception/MemberExceptionHandler.java)
- 공통 권한 검증 유틸: [RoleGuard.java](/c:/my_project/beadv5_2_TodayLunchMenu_BE/common-security/src/main/java/com/todaylunch/common/security/auth/util/RoleGuard.java)
