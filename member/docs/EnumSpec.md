# Member Enum Spec

## 도메인 Enum

### `MemberStatus`
| 값 | 의미 |
| --- | --- |
| `ACTIVE` | 정상 활동 가능 회원 |
| `SUSPENDED` | 운영 정책에 의해 제한된 회원 |
| `DELETED` | 탈퇴 또는 비활성 처리된 회원 |

### `ReportStatus`
| 값 | 의미 |
| --- | --- |
| `PENDING` | 검토 대기 |
| `APPROVED` | 신고 승인 완료 |
| `REJECTED` | 신고 반려 완료 |

### `ReportType`
| 값 | 의미 |
| --- | --- |
| `ABUSE` | 폭언, 괴롭힘 |
| `FRAUD` | 사기성 행위 |
| `SPAM` | 스팸, 반복성 행위 |
| `ETC` | 기타 사유 |

### `RestrictionType`
| 값 | 의미 |
| --- | --- |
| `TRADE_BAN` | 거래 기능 제한 |
| `LOGIN_BAN` | 로그인 제한 |
| `CHAT_BAN` | 채팅 제한 |

## 외부 공통 Enum 의존

### `MemberRole`
소스는 공통 보안 모듈에 있으며, member 모듈에서 주요하게 사용하는 값은 아래와 같다.

| 값 | 의미 |
| --- | --- |
| `USER` | 일반 회원 |
| `SELLER` | 판매자 권한 보유 회원 |
| `ADMIN` | 관리자 |

## 사용 지점
- `Member.status` -> `MemberStatus`
- `Member.role` -> `MemberRole`
- `MemberReport.reportType`, `CreateMemberReportRequest.reportType` -> `ReportType`
- `MemberReport.status` -> `ReportStatus`
- `MemberRestriction.restrictionType`, `ReviewMemberReportRequest.restrictionType` -> `RestrictionType`
