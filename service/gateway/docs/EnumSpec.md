# Gateway Enum Spec

## 자체 Enum
- gateway 모듈에는 별도 Java enum 이 없다.

## 상수 규약
| 이름 | 값 | 설명 |
| --- | --- | --- |
| `TOKEN_TYPE_CLAIM` | `tokenType` | JWT 타입 claim |
| `MEMBER_ID_CLAIM` | `memberId` | 회원 식별 claim |
| `ROLE_CLAIM` | `role` | 권한 claim |
| `ACCESS` | `ACCESS` | 허용 토큰 타입 |
| `X-Member-Id` | Header | downstream 회원 ID 전달 |
| `X-Member-Role` | Header | downstream 회원 역할 전달 |

## 공개 경로 패턴
`GatewayAuthProperties.publicPaths` 는 `AntPathMatcher` 패턴으로 비교된다.
