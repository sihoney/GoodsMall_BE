#!/usr/bin/env bash
set -euo pipefail

# ==========================
# 1. 임시 계산용 변수 초기화
# ==========================

# diff 기준 ref
BASE_REF="${1:-}"
HEAD_REF="${2:-HEAD}"

# 애플리케이션 서비스 목록
APP_SERVICES=(
  gateway
  member
  product
  auction
#  order
#  payment
#  notification
#  settlement
#  cart
#  ai
)

# 빌드 대상 모듈
BUILD_MODULES=()
# 코드 변경 서비스
SERVICES=()
# k8s 매니페스트 변경 서비스
K8S_SERVICES=()
# DB 마이그레이션 변경 여부
MIGRATION_CHANGED=false
# 공통 인프라 변경 여부
INFRA_CHANGED=false

# ==========================
# 2. 유팉리티 함수
# - add_unique: 배열에 중복없이 값 추가하기
# - join_by_space: 배열을 공백으로 구분된 문자열로 만들기
# - json_matrix: Github Actions용 Matrix JSON 생성하기
# ==========================

# 중복 제거 추가
add_unique() {
  local item="$1"
  shift
  local -n target="$1"

  for existing in "${target[@]}"; do
    if [[ "$existing" == "$item" ]]; then
      return
    fi
  done

  target+=("$item")
}

# 공백 구분 문자열
join_by_space() {
  local IFS=" "
  echo "$*"
}

# GitHub Actions matrix JSON
json_matrix() {
  local first=true
  printf '{"include":['
  for module in "$@"; do
    if [[ "$first" == true ]]; then
      first=false
    else
      printf ','
    fi
    printf '{"module_name":"%s"}' "$module"
  done
  printf ']}'
}

# GitHub Actions output
write_output() {
  local key="$1"
  local value="$2"

  if [[ -n "${GITHUB_OUTPUT:-}" ]]; then
    echo "$key=$value" >> "$GITHUB_OUTPUT"
  else
    echo "$key=$value"
  fi
}

# ==========================
# 3. 변경 파일 목록 추출(git diff)
# ==========================

# [0] 변경 파일 목록
if [[ -z "$BASE_REF" ]]; then
  CHANGED_FILES="$(git diff --name-only HEAD~1 "$HEAD_REF")"
elif [[ "$BASE_REF" == "0000000000000000000000000000000000000000" ]]; then
  CHANGED_FILES="$(git diff --name-only HEAD~1 "$HEAD_REF")"
else
  CHANGED_FILES="$(git diff --name-only "$BASE_REF" "$HEAD_REF")"
fi

echo "changed files:"
echo "$CHANGED_FILES"

# ==========================
# 4. 조건별 변경 감지 및 분류 (핵심 로직)
# ==========================

# [1] 서비스 코드, k8s 매니페스트 변경 감지
for service in "${APP_SERVICES[@]}"; do
  if grep -q "^service/$service/" <<< "$CHANGED_FILES"; then
    add_unique "$service" SERVICES
    add_unique "$service" BUILD_MODULES
  fi

  if grep -q "^infra/k8s/$service/" <<< "$CHANGED_FILES"; then
    add_unique "$service" K8S_SERVICES
  fi
done

# [2] DB 마이그레이션 변경 감지
if grep -q '^db-migration/' <<< "$CHANGED_FILES"; then
  MIGRATION_CHANGED=true
  add_unique "db-migration" BUILD_MODULES
fi

# [3] 공통 인프라 변경 감지
if grep -q '^infra/k8s/infra/' <<< "$CHANGED_FILES"; then
  INFRA_CHANGED=true
fi

# [4] 공통 모듈 변경: 전체 서비스 영향
if grep -Eq '^(common-security/|common-monitoring/)' <<< "$CHANGED_FILES"; then
  for service in "${APP_SERVICES[@]}"; do
    add_unique "$service" SERVICES
    add_unique "$service" BUILD_MODULES
  done
fi

# [5] Gradle 설정 변경: 전체 서비스 영향
if grep -Eq '^(build\.gradle|settings\.gradle|gradle\.properties|gradle/)' <<< "$CHANGED_FILES"; then
  for service in "${APP_SERVICES[@]}"; do
    add_unique "$service" SERVICES
    add_unique "$service" BUILD_MODULES
  done
  add_unique "db-migration" BUILD_MODULES
fi

# ==========================
# 5. 최종 배포 대상 취합 및 빌드 여부 판정
# - 코드 변경과 k8s 변경을 종합하여 최종 배포할 서비스 목록을 만듭니다.
# - 빌드 대상 모듈이 존재할 경우 빌드 여부(HAS_CHANGES)를 true로 설정합니다.
# ==========================

# 최종 배포 서비스: 코드 변경 + 매니페스트 변경
DEPLOY_SERVICES=()
for service in "${SERVICES[@]}"; do
  add_unique "$service" DEPLOY_SERVICES
done
for service in "${K8S_SERVICES[@]}"; do
  add_unique "$service" DEPLOY_SERVICES
done

# 빌드 여부
HAS_CHANGES=false
if [[ ${#BUILD_MODULES[@]} -gt 0 ]]; then
  HAS_CHANGES=true
fi

# 빌드 matrix
MATRIX="$(json_matrix "${BUILD_MODULES[@]}")"

# ==========================
# 6. 최종 결과 출력 (GitHub Actions Output 등록)
# - 수집 및 가공된 데이터를 차기 Step/Job에서 참조할 수 있도록
#   GitHub Actions 시스템 변수($GITHUB_OUTPUT)로 내보냅니다.
# ==========================

# workflow output
write_output "has_changes" "$HAS_CHANGES"
write_output "matrix" "$MATRIX"
write_output "services" "$(join_by_space "${SERVICES[@]}")"
write_output "deploy_services" "$(join_by_space "${DEPLOY_SERVICES[@]}")"
write_output "k8s_services" "$(join_by_space "${K8S_SERVICES[@]}")"
write_output "migration_changed" "$MIGRATION_CHANGED"
write_output "infra_changed" "$INFRA_CHANGED"
