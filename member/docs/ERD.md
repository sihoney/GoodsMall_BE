# Member ERD

## Mermaid Diagram
```mermaid
erDiagram
    MEMBER {
        UUID member_id PK
        VARCHAR email UK
        VARCHAR password
        VARCHAR nickname
        VARCHAR phone
        VARCHAR address
        VARCHAR profile_image_key
        VARCHAR role
        VARCHAR status
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    SELLER {
        UUID seller_id PK
        UUID member_id UK
        VARCHAR bank_name
        VARCHAR account
        TIMESTAMP approved_at
    }

    MEMBER_REPORT {
        UUID report_id PK
        UUID reporter_id FK
        UUID reported_member_id FK
        VARCHAR reason
        VARCHAR report_type
        VARCHAR status
        VARCHAR review_comment
        UUID reviewed_by
        TIMESTAMP reviewed_at
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    MEMBER_RESTRICTION {
        UUID restriction_id PK
        UUID member_id FK
        UUID admin_id FK
        VARCHAR reason
        VARCHAR restriction_type
        INTEGER duration_hours
        TIMESTAMP end_at
        BOOLEAN is_active
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    MEMBER ||--o| SELLER : "owns seller profile"
    MEMBER ||--o{ MEMBER_REPORT : "reporter_id creates"
    MEMBER ||--o{ MEMBER_REPORT : "reported_member_id receives"
    MEMBER ||--o{ MEMBER_RESTRICTION : "member_id is restricted"
    MEMBER ||--o{ MEMBER_RESTRICTION : "admin_id manages"
```

## 관계 해설
- `MEMBER -> SELLER`: 회원은 0개 또는 1개의 판매자 프로필을 가진다.
- `MEMBER -> MEMBER_REPORT (reporter_id)`: 회원은 여러 신고를 생성할 수 있다.
- `MEMBER -> MEMBER_REPORT (reported_member_id)`: 회원은 여러 신고의 대상이 될 수 있다.
- `MEMBER -> MEMBER_RESTRICTION (member_id)`: 회원은 여러 제재 이력을 가질 수 있다.
- `MEMBER -> MEMBER_RESTRICTION (admin_id)`: 관리자 회원은 여러 제재를 생성할 수 있다.

## 엔티티

### `member_service.member`
| 컬럼 | 타입 | 제약 |
| --- | --- | --- |
| `member_id` | UUID | PK |
| `email` | VARCHAR(255) | NOT NULL, UNIQUE |
| `password` | VARCHAR(255) | NOT NULL |
| `nickname` | VARCHAR(100) | NOT NULL |
| `phone` | VARCHAR(50) | NULL |
| `address` | VARCHAR(255) | NULL |
| `profile_image_key` | VARCHAR(500) | NULL |
| `role` | VARCHAR(30) | NOT NULL |
| `status` | VARCHAR(30) | NOT NULL |
| `created_at` | TIMESTAMP | NOT NULL |
| `updated_at` | TIMESTAMP | NOT NULL |

인덱스: `uk_member_email`, `idx_member_nickname`, `idx_member_role`, `idx_member_status`

### `member_service.seller`
| 컬럼 | 타입 | 제약 |
| --- | --- | --- |
| `seller_id` | UUID | PK |
| `member_id` | UUID | NOT NULL, UNIQUE |
| `bank_name` | VARCHAR(100) | NULL |
| `account` | VARCHAR(100) | NULL |
| `approved_at` | TIMESTAMP | NULL |

인덱스: `uk_seller_member_id`, `idx_seller_approved_at`

### `member_service.member_report`
| 컬럼 | 타입 | 제약 |
| --- | --- | --- |
| `report_id` | UUID | PK |
| `reporter_id` | UUID | NOT NULL |
| `reported_member_id` | UUID | NOT NULL |
| `reason` | VARCHAR(255) | NOT NULL |
| `report_type` | VARCHAR(30) | NOT NULL |
| `status` | VARCHAR(30) | NOT NULL |
| `review_comment` | VARCHAR(255) | NULL |
| `reviewed_by` | UUID | NULL |
| `reviewed_at` | TIMESTAMP | NULL |
| `created_at` | TIMESTAMP | NOT NULL |
| `updated_at` | TIMESTAMP | NOT NULL |

인덱스: `idx_member_report_reporter_id`, `idx_member_report_reported_member_id`, `idx_member_report_status`

### `member_service.member_restriction`
| 컬럼 | 타입 | 제약 |
| --- | --- | --- |
| `restriction_id` | UUID | PK |
| `member_id` | UUID | NOT NULL |
| `admin_id` | UUID | NOT NULL |
| `reason` | VARCHAR(255) | NOT NULL |
| `restriction_type` | VARCHAR(30) | NOT NULL |
| `duration_hours` | INTEGER | NOT NULL, `> 0` |
| `end_at` | TIMESTAMP | NOT NULL |
| `is_active` | BOOLEAN | NOT NULL |
| `created_at` | TIMESTAMP | NOT NULL |
| `updated_at` | TIMESTAMP | NULL |

인덱스: `idx_member_restriction_member_id`, `idx_member_restriction_active_lookup`

## 관계
- `member 1 : 0..1 seller`
- `member 1 : N member_report` as reporter
- `member 1 : N member_report` as reported member
- `member 1 : N member_restriction` as restricted target
- `member 1 : N member_restriction` as admin actor
