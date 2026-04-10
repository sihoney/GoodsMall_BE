# Member Auth Design

## 목적
- 회원 가입, 로그인, 토큰 재발급, 로그아웃을 담당한다.
- 액세스 토큰은 게이트웨이와 각 서비스의 인증 컨텍스트 생성에 사용된다.
- 리프레시 토큰은 Redis 에 저장해 세션성 갱신을 제어한다.

## 구성 요소
| 컴포넌트 | 역할 |
| --- | --- |
| `AuthController` | 인증 API 진입점 |
| `AuthService` | 로그인, 재발급, 로그아웃 처리 |
| `JwtTokenProvider` | 액세스/리프레시 토큰 생성 및 검증 |
| `RedisRefreshTokenStore` | 회원별 리프레시 토큰 저장 |
| `MemberRestrictionService` | 로그인 금지 제재 여부 확인 |

## 토큰 설계
| 항목 | Access Token | Refresh Token |
| --- | --- | --- |
| `tokenType` | `ACCESS` | `REFRESH` |
| `memberId` | 포함 | 포함 |
| `email` | 포함 | 미포함 |
| `role` | 포함 | 미포함 |
| 저장 위치 | 클라이언트 | 클라이언트 + Redis |
| 용도 | 인가 헤더 전달 | 액세스 토큰 재발급 |

## 로그인 시퀀스
1. 클라이언트가 `POST /api/auth/login` 호출
2. `AuthService` 가 이메일로 회원 조회
3. 비밀번호 비교 실패 시 `InvalidLoginException`
4. `MemberRestrictionService` 로 `LOGIN_BAN` 활성 여부 확인
5. 액세스 토큰, 리프레시 토큰 발급
6. 리프레시 토큰을 Redis 에 `memberId` 기준으로 저장
7. 토큰 정보 응답 반환

## 재발급 시퀀스
1. 클라이언트가 `POST /api/auth/refresh` 호출
2. 리프레시 토큰 형식과 서명 검증
3. 토큰에서 `memberId` 추출
4. 해당 회원의 로그인 제재 여부 재검사
5. Redis 저장 토큰과 요청 토큰 일치 여부 확인
6. 회원 실존 확인 후 새 액세스 토큰 발급
7. 기존 리프레시 토큰은 그대로 유지

## 로그아웃
- `logout(memberId)` 호출 시 Redis 의 리프레시 토큰 키를 삭제한다.
- 현재 구현은 액세스 토큰 블랙리스트는 관리하지 않는다.

## 예외 정책
- 잘못된 이메일 또는 비밀번호: `InvalidLoginException`
- 리프레시 토큰 없음: `RefreshTokenNotFoundException`
- 토큰 위변조 또는 타입 불일치: `InvalidTokenException`
- 로그인 금지 제재 활성: `MemberRestrictedException`

## 설계 메모
- 게이트웨이는 `member-service` 와 동일한 `JWT secret`, `issuer` 를 사용해야 한다.
- 로그아웃 API 가 게이트웨이 공개 경로 정책과 상충하지 않는지 운영 전에 확인하는 것이 좋다.
