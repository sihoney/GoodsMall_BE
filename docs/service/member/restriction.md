# Restriction Package

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

`restriction` 패키지는 회원 제재를 관리한다.

주요 책임:

- 관리자 제재 생성
- 활성 제재 조회
- 제재 비활성화
- 중복 활성 제재 방지
- 로그인 가능 여부 판단에 사용

---

## 2. 도메인 모델

| 엔티티 / Enum | 설명 |
|---|---|
| `MemberRestriction` | 회원 제재 |
| `RestrictionType` | 제재 유형 |

---

## 3. 주요 서비스

| 클래스 | 책임 |
|---|---|
| `MemberRestrictionService` | 제재 생성/조회/비활성화 |

---

## 4. 포트

| 포트 | 방향 | 설명 |
|---|---|---|
| `MemberRestrictionUsecase` | in | 회원 제재 유스케이스 |
| `MemberRestrictionPersistencePort` | out | 회원 제재 저장소 접근 |

---

## 5. 인프라 어댑터

| 어댑터 | 설명 |
|---|---|
| `MemberRestrictionJpaAdapter` | 제재 JPA 저장소 adapter |
| `MemberRestrictionJpaRepository` | 제재 JPA repository |

---

## 6. 주요 흐름

- 관리자 제재 생성
- 제재 해제
- 로그인 시 제재 여부 검증

---

## 7. 관련 파일

- `service/member/src/main/java/com/example/member/restriction/**`