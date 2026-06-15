# Report Package

## 목차

- [1. 책임](#1-책임)
- [2. 도메인 모델](#2-도메인-모델)
- [3. 주요 서비스](#3-주요-서비스)
- [4. 포트](#4-포트)
- [5. 인프라 어댑터](#5-인프라-어댑터)
- [6. 주요 흐름](#6-주요-흐름)
- [7. 관련 파일](#7-관련-파일)

---

## 1. 책임

`report` 패키지는 회원 신고를 관리한다.

주요 책임:

- 회원 신고 생성
- 내 신고 내역 조회
- 관리자 신고 검토
- 중복 신고 방지
- 자기 자신 신고 방지

---

## 2. 도메인 모델

| 엔티티 / Enum | 설명 |
|---|---|
| `MemberReport` | 회원 신고 |
| `ReportStatus` | 신고 처리 상태 |
| `ReportType` | 신고 유형 |

---

## 3. 주요 서비스

| 클래스 | 책임 |
|---|---|
| `MemberReportService` | 신고 생성/조회/검토 |

---

## 4. 포트

| 포트 | 방향 | 설명 |
|---|---|---|
| `MemberReportUsecase` | in | 회원 신고 유스케이스 |
| `MemberReportPersistencePort` | out | 회원 신고 저장소 접근 |

---

## 5. 인프라 어댑터

| 어댑터 | 설명 |
|---|---|
| `MemberReportJpaAdapter` | 신고 JPA 저장소 adapter |
| `MemberReportJpaRepository` | 신고 JPA repository |

---

## 6. 주요 흐름

- 회원 신고 생성
- 신고 중복 여부 검증
- 관리자 신고 승인/반려

---

## 7. 관련 파일

- `service/member/src/main/java/com/example/member/report/**`