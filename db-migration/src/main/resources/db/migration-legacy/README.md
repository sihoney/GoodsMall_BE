# Migration Legacy Archive

이 디렉터리는 기존 `db/migration/V*.sql` 원본을 보관하기 위한 archive 디렉터리다.

목적:

- squash 이전 원본 SQL을 비교 기준으로 남긴다.
- baseline migration 재구성 중 빠진 컬럼, 제약, index를 다시 확인할 수 있게 한다.
- 문제가 생겼을 때 기존 migration 이력을 바로 참조할 수 있게 한다.

주의:

- 이 디렉터리는 현재 Flyway scan 경로가 아니다.
- 실제 실행 대상은 계속 `classpath:db/migration`이다.
- baseline migration 작성이 끝나기 전까지는 원본 `db/migration`도 유지한다.
