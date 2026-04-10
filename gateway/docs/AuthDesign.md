# Gateway Auth Design

## 목적
- `/api/**` 요청에 대해 공통 인증 진입점을 제공한다.
- 토큰 검증 결과를 downstream 서비스가 재사용할 수 있도록 헤더로 변환한다.

## 구성 요소
| 컴포넌트 | 역할 |
| --- | --- |
| `JwtAuthenticationFilter` | 전역 JWT 검사 및 헤더 주입 |
| `GatewayJwtValidator` | JWT issuer, signature, tokenType 검증 |
| `GatewayAuthProperties` | 공개 경로 및 기능 토글 |
| `JwtProperties` | `secret`, `issuer` 설정 |

## 인증 흐름
1. 요청이 들어오면 `JwtAuthenticationFilter` 실행
2. `OPTIONS` 요청은 그대로 통과
3. `/api/` 로 시작하지 않는 경로는 통과
4. 공개 경로(`publicPaths`) 는 통과
5. `jwtValidationEnabled=false` 면 통과
6. `Authorization: Bearer <token>` 형식 검사
7. `GatewayJwtValidator` 가 아래 항목 검증
   - issuer 일치
   - signature 유효
   - 만료 여부
   - `tokenType == ACCESS`
8. 검증 성공 시 `X-Member-Id`, `X-Member-Role` 헤더 추가
9. 실패 시 `401 Unauthorized`

## 공개 경로
- `/api/auth`
- `/api/auth/login`
- `/api/auth/refresh`
- `/api/auth/profile-images/presign`
- `/swagger/**`
- `/swagger-ui.html`

## 한계와 메모
- 블랙리스트 검증은 TODO 상태다.
- 로그아웃 후 액세스 토큰 즉시 무효화는 지원하지 않는다.
- member-service 와 동일한 JWT secret/issuer 설정을 공유해야 한다.
