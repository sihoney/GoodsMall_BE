# Member Service

`member` 모듈은 Today Lunch Mall의 회원 계정 생명주기와 인증 보조 기능을 담당하는 서비스입니다.

현재 구현 범위는 다음과 같습니다.

- 회원 가입과 기본 프로필 관리
- 이메일 인증 기반 계정 활성화
- 로그인, 토큰 재발급, 세션 관리, 로그아웃
- 프로필 이미지 업로드 준비
- 판매자 등록과 역할 전환
- 외부 OAuth 계정 연동 및 해제
- 회원 신고와 관리자 제재 관리

이 README는 공개 진입 문서 역할만 담당합니다. 상세 설계, API, 상태 정책, 운영 검토 문서는 `docs_private` 아래에 정리되어 있습니다.

## 1. 담당 역할

| 기능 | 설명 |
|---|---|
| 회원 가입 | 이메일 중복 검사, 비밀번호 암호화, 회원 생성 |
| 계정 활성화 | 이메일 인증을 통해 `PENDING_VERIFICATION` 에서 `ACTIVE` 로 전환 |
| 인증 보조 | 로그인, 토큰 재발급, 세션 조회, 현재/전체 로그아웃 |
| 회원 정보 관리 | 내 정보 조회 및 수정, 비밀번호 변경 |
| 프로필 이미지 | S3 presigned URL 발급과 프로필 이미지 key 검증 |
| 판매자 전환 | 계좌 인증 이후 `SELLER` 역할 부여 |
| 외부 계정 연동 | OAuth 계정 조회, 연결, 해제 |
| 신고 및 제재 | 회원 신고 접수, 관리자 검토, 제재 적용 |

## 2. 주요 API 범주

| 범주 | 예시 경로 |
|---|---|
| 회원 | `/api/members`, `/api/members/me` |
| 인증 | `/api/auth/login`, `/api/auth/refresh` |
| 세션 | `/api/auth/sessions`, `/api/auth/logout/current`, `/api/auth/logout/all` |
| 이메일 인증 | `/api/auth/email-verifications/...` |
| 프로필 이미지 | `/api/auth/profile-images/presign` |
| 판매자 | `/api/sellers/...` |
| 신고/제재 | `/api/member-reports/...`, `/api/member-restrictions/...` |

세부 요청/응답 규격은 `docs_private` 의 API 문서를 참고합니다.

## 3. 핵심 상태와 규칙

- 기본 역할은 `USER` 이며, 판매자 등록 완료 시 `SELLER` 로 전환됩니다.
- 기본 상태는 `PENDING_VERIFICATION` 또는 설정에 따라 `ACTIVE` 입니다.
- 로그인과 토큰 재발급은 `ACTIVE` 회원만 허용합니다.
- 관리자 제재가 활성화된 회원은 로그인 제한을 받을 수 있습니다.
- 회원탈퇴는 상태 전이와 세션 만료를 포함하는 정책성 기능으로 관리합니다.

## 4. 이벤트 연계

이 모듈은 회원 관련 주요 이벤트를 발행합니다.

- `MEMBER_SIGNED_UP`
- `SELLER_PROMOTED`
- `ACCOUNT_VERIFICATION_EXPIRED`
- `ACCOUNT_VERIFICATION_FAILED`
- `MEMBER_OAUTH_LINKED`

이벤트 상세와 연계 현황은 `docs_private` 문서를 참고합니다.

## 5. 문서 위치

상세 문서는 `member/docs_private` 아래에 정리되어 있습니다.

대표 문서:

- `APISpec_Member.md`
- `APISpec_Auth.md`
- `FeatureSpec.md`
- `MemberStatusDesign.md`
- `MemberWithdrawalPolicy.md`
- `EventDesign.md`
