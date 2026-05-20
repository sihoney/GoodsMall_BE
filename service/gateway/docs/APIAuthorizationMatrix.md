# Gateway API Authorization Matrix

## 개요

- 이 문서는 `gateway-service` 에서 적용할 API 권한 정책의 기준표다.
- 기준은 `HTTP method + path pattern + allowed roles` 이다.
- 게이트웨이는 1차 접근 제어를 담당하고, 세부 소유권/상태 검증은 각 서비스가 최종 수행한다.

## 권한 원칙

- `PUBLIC`: 인증 없이 허용
- `AUTHENTICATED`: 로그인 사용자 전체 허용 (`USER`, `SELLER`, `ADMIN`)
- `SELLER_OR_ADMIN`: 판매자 또는 관리자 허용
- `ADMIN_ONLY`: 관리자만 허용

## 공개 API

| Method | Path Pattern                       | Access Policy | 비고                            |
| ------ | ---------------------------------- | ------------- | ------------------------------- |
| `POST` | `/api/auth`                        | `PUBLIC`      | 회원가입                        |
| `POST` | `/api/auth/login`                  | `PUBLIC`      | 로그인                          |
| `POST` | `/api/auth/refresh`                | `PUBLIC`      | 토큰 재발급                     |
| `POST` | `/api/auth/profile-images/presign` | `PUBLIC`      | 회원가입/프로필 업로드 사전 URL |
| `GET`  | `/swagger/**`                      | `PUBLIC`      | Swagger 문서                    |
| `GET`  | `/swagger-ui.html`                 | `PUBLIC`      | Swagger UI                      |

## 인증 필요 API

| Method   | Path Pattern             | Access Policy   | 비고                                                |
| -------- | ------------------------ | --------------- | --------------------------------------------------- |
| `GET`    | `/api/members/me`        | `AUTHENTICATED` | 본인 정보 조회                                      |
| `PUT`    | `/api/members/me`        | `AUTHENTICATED` | 본인 정보 수정                                      |
| `POST`   | `/api/auth/logout/**`    | `AUTHENTICATED` | 현재는 path 기반, 추후 본인 로그아웃 방식 정리 필요 |
| `POST`   | `/api/member-reports/**` | `AUTHENTICATED` | 신고 생성                                           |
| `GET`    | `/api/member-reports/**` | `AUTHENTICATED` | 내 신고 조회 중심, 세부 권한은 서비스 검증          |
| `GET`    | `/api/notifications/**`  | `AUTHENTICATED` | 본인 알림 조회                                      |
| `PATCH`  | `/api/notifications/**`  | `AUTHENTICATED` | 본인 알림 읽음 처리                                 |
| `GET`    | `/api/carts/**`          | `AUTHENTICATED` | 장바구니 조회                                       |
| `POST`   | `/api/carts/**`          | `AUTHENTICATED` | 장바구니 담기                                       |
| `PUT`    | `/api/carts/**`          | `AUTHENTICATED` | 장바구니 수정                                       |
| `DELETE` | `/api/carts/**`          | `AUTHENTICATED` | 장바구니 삭제                                       |
| `GET`    | `/api/wishes/**`         | `AUTHENTICATED` | 찜 조회                                             |
| `POST`   | `/api/wishes/**`         | `AUTHENTICATED` | 찜 추가                                             |
| `DELETE` | `/api/wishes/**`         | `AUTHENTICATED` | 찜 삭제                                             |
| `GET`    | `/api/orders/**`         | `AUTHENTICATED` | 주문 조회, 소유권은 서비스 검증                     |
| `POST`   | `/api/orders/**`         | `AUTHENTICATED` | 주문 생성                                           |
| `POST`   | `/api/payments/**`       | `AUTHENTICATED` | 결제 생성/진행                                      |
| `GET`    | `/api/payments/**`       | `AUTHENTICATED` | 결제 조회, 소유권은 서비스 검증                     |

## 판매자/관리자 API

| Method   | Path Pattern          | Access Policy     | 비고                                       |
| -------- | --------------------- | ----------------- | ------------------------------------------ |
| `POST`   | `/api/sellers/**`     | `SELLER_OR_ADMIN` | 현재 회원의 판매자 기능 사용 기준으로 가정 |
| `GET`    | `/api/sellers/**`     | `SELLER_OR_ADMIN` | 판매자 정보 조회                           |
| `PUT`    | `/api/sellers/**`     | `SELLER_OR_ADMIN` | 판매자 정보 수정                           |
| `GET`    | `/api/product/**`     | `PUBLIC`          | 상품 조회 공개                             |
| `POST`   | `/api/product/**`     | `SELLER_OR_ADMIN` | 상품 등록                                  |
| `PUT`    | `/api/product/**`     | `SELLER_OR_ADMIN` | 상품 수정                                  |
| `PATCH`  | `/api/product/**`     | `SELLER_OR_ADMIN` | 상품 상태 변경                             |
| `DELETE` | `/api/product/**`     | `SELLER_OR_ADMIN` | 상품 삭제                                  |
| `POST`   | `/api/settlements/**` | `SELLER_OR_ADMIN` | TODO: 정산 생성 주체 정책 확정 필요        |
| `GET`    | `/api/settlements/**` | `SELLER_OR_ADMIN` | 판매자/관리자 정산 조회                    |

## 관리자 전용 API

| Method  | Path Pattern                        | Access Policy | 비고                |
| ------- | ----------------------------------- | ------------- | ------------------- |
| `GET`   | `/api/admin/member-reports/**`      | `ADMIN_ONLY`  | 신고 조회/상세      |
| `PATCH` | `/api/admin/member-reports/**`      | `ADMIN_ONLY`  | 신고 상태 처리      |
| `POST`  | `/api/admin/member-restrictions/**` | `ADMIN_ONLY`  | 제재 생성           |
| `PATCH` | `/api/admin/member-restrictions/**` | `ADMIN_ONLY`  | 제재 해제/상태 변경 |
| `GET`   | `/api/admin/member-restrictions/**` | `ADMIN_ONLY`  | 제재 조회           |

## 게이트웨이 적용 원칙

1. `PUBLIC` 경로는 JWT 없이 통과
2. `AUTHENTICATED` 이상은 JWT 검증 성공이 필요
3. 게이트웨이는 JWT claim 의 `role` 로 1차 차단
4. 각 서비스는 `RoleGuard` 와 도메인 검증으로 최종 인가 수행
5. 소유권, 상태, 리소스 존재 여부는 게이트웨이가 아니라 서비스가 판단

## 구현 메모

- `GatewayAuthProperties` 에 `roleRules` 설정 추가 ✅
- 각 rule 은 `methods`, `pattern`, `allowedRoles` 를 가진다 ✅
- rule 미매칭 보호 API 는 기본적으로 `AUTHENTICATED` 또는 명시적 거부 중 하나로 정책을 통일해야 한다 ✅
- TODO: 이메일 인증 구현 시 `/api/auth/email-verifications/**` 공개 경로 추가 여부 검토

## 관련 문서

- API 스펙: [APISpec.md](/c:/my_project/beadv5_2_TodayLunchMenu_BE/gateway/docs/APISpec.md)
- 인증 설계: [AuthDesign.md](/c:/my_project/beadv5_2_TodayLunchMenu_BE/gateway/docs/AuthDesign.md)
- 프로젝트 인증/인가 설계: [AuthDesign_Project.md](/c:/my_project/beadv5_2_TodayLunchMenu_BE/gateway/docs/AuthDesign_Project.md)
