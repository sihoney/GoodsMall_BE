# =========================================================================
# 운영 환경(Production) 상품 및 이미지 초기 데이터(Seed Data) 생성 및 관리 스크립트
# =========================================================================

# -------------------------------------------------------------------------
# 1. 스크립트 입력 매개변수 (Default Paths)
# - 매니페스트 CSV, 소스 미디어, S3 아티팩트 빌드 경로, 출력 SQL 경로를 지정합니다.
# -------------------------------------------------------------------------
param(
    [string]$ManifestPath = "docs_private\seed-images\seed_product_manifest.csv",
    [string]$ImageSourceDir = "docs_private\seed-images",
    [string]$UploadRoot = "docs_private\seed-upload",
    [string]$ProductSeedPath = "db-migration\src\main\resources\db\seed\prod_seed_product.sql",
    [string]$ProductImageSeedPath = "db-migration\src\main\resources\db\seed\prod_seed_product_image.sql"
)

# 스크립트 실행 중 에러 발생 시 즉시 중단 및 트랩 처리 (안전성 확보)
$ErrorActionPreference = "Stop"

# -------------------------------------------------------------------------
# 2. 유틸리티 함수 정의 (Utility Functions)
# - Escape-Sql: SQL 인젝션 방지 및 홑따옴표(') 탈출 처리 함수
# - Write-Utf8NoBom: 바이트 순서 표식(BOM)이 없는 순수 UTF-8 텍스트 파일을 안전하게 기록하는 함수 (Flyway 호환성 확보)
# -------------------------------------------------------------------------

function Escape-Sql([string]$Value) {
    if ($null -eq $Value) { return "" }
    return $Value.Replace("'", "''")
}

function Write-Utf8NoBom([string]$Path, [string[]]$Lines) {
    $directory = Split-Path -Parent $Path
    if ($directory -and -not (Test-Path $directory)) {
        New-Item -ItemType Directory -Path $directory | Out-Null
    }
    [System.IO.File]::WriteAllLines((Resolve-Path $directory).Path + "\" + (Split-Path -Leaf $Path), $Lines, [System.Text.UTF8Encoding]::new($false))
}

# -------------------------------------------------------------------------
# 3. 데이터 무결성 검증 단계 (Validation Stage)
# -------------------------------------------------------------------------

# [1] 소스 매니페스트 파일 존재 여부 검증
if (-not (Test-Path $ManifestPath)) {
    throw "Manifest not found: $ManifestPath"
}

# [2] CSV 파일 데이터 적재 확인
$rows = Import-Csv -Path $ManifestPath
if (-not $rows -or $rows.Count -eq 0) {
    throw "Manifest is empty: $ManifestPath"
}

# [3] 데이터베이스 맵핑에 필수적인 스키마 컬럼 정합성 검증
$requiredColumns = @(
    "product_no", "product_id", "seller_email", "category_id", "slug", "title", "description",
    "price", "stock_quantity", "status", "thumbnail_file", "detail_file"
)
$actualColumns = $rows[0].PSObject.Properties.Name
foreach ($column in $requiredColumns) {
    if ($actualColumns -notcontains $column) {
        throw "Manifest is missing required column: $column"
    }
}

# -------------------------------------------------------------------------
# 4. S3 업로드 디렉터리 준비 (Build Workspace Initialization)
# - 중복 방지를 위해 기존 산출물 폴더를 정리하고 빈 작업 공간을 초기화합니다.
# -------------------------------------------------------------------------
if (Test-Path $UploadRoot) {
    Remove-Item -Path $UploadRoot -Recurse -Force
}
New-Item -ItemType Directory -Path $UploadRoot | Out-Null

# -------------------------------------------------------------------------
# 5. 메타데이터 변환 및 SQL 파이프라인 빌드 (Data Transformation)
#
# [1] 상품 정보 SQL 정의문 구축 (Product Metadata SQL Generator)
# - 판매자 이메일을 기반으로 member 테이블과 조인(JOIN) 및 멱등성 보장(ON CONFLICT) 로직을 포함합니다.
# [2] 이미지 미디어 처리 및 이미지 SQL 정의문 구축 (Media Processing & Image SQL)
# - 로컬 원본 이미지를 고유 슬러그(Slug) 하위 디렉터리로 이관하고, 가상 UUID 조합 및 S3 Key 매핑을 수행합니다.
# -------------------------------------------------------------------------
$productLines = @(
    "-- ============================================",
    "-- Production Seed Data - Products",
    "-- ============================================",
    "-- Generated from docs_private/seed-images/seed_product_manifest.csv.",
    "-- Edit the manifest and rerun scripts/generate-product-seed-from-images.ps1 instead of editing this file by hand.",
    "",
    "INSERT INTO product.product (",
    "    product_id,",
    "    seller_id,",
    "    category_id,",
    "    title,",
    "    description,",
    "    price,",
    "    stock_quantity,",
    "    status,",
    "    type,",
    "    view_count,",
    "    created_at,",
    "    updated_at",
    ")",
    "SELECT",
    "    seed.product_id::UUID,",
    "    seller_member.member_id,",
    "    seed.category_id::UUID,",
    "    seed.title,",
    "    seed.description,",
    "    seed.price::DECIMAL(10, 2),",
    "    seed.stock_quantity::INTEGER,",
    "    seed.status,",
    "    'GENERAL',",
    "    0,",
    "    NOW(),",
    "    NOW()",
    "FROM (",
    "    VALUES"
)

# CSV 행 데이터를 순회하며 대량 멀티-밸류(Multi-Values) INSERT 문자열 생성
$productValues = @()
foreach ($row in $rows) {
    $productValues += "        ('$($row.product_id)', '$((Escape-Sql $row.seller_email))', '$($row.category_id)', '$((Escape-Sql $row.title))', '$((Escape-Sql $row.description))', $($row.price), $($row.stock_quantity), '$((Escape-Sql $row.status))')"
}
for ($i = 0; $i -lt $productValues.Count; $i++) {
    $productLines += $productValues[$i] + $(if ($i -eq $productValues.Count - 1) { "" } else { "," })
}
$productLines += @(
    ") AS seed(product_id, seller_email, category_id, title, description, price, stock_quantity, status)",
    "JOIN member.member seller_member",
    "    ON seller_member.email = seed.seller_email",
    "ON CONFLICT (product_id) DO UPDATE SET",
    "    seller_id = EXCLUDED.seller_id,",
    "    category_id = EXCLUDED.category_id,",
    "    title = EXCLUDED.title,",
    "    description = EXCLUDED.description,",
    "    price = EXCLUDED.price,",
    "    stock_quantity = EXCLUDED.stock_quantity,",
    "    status = EXCLUDED.status,",
    "    type = EXCLUDED.type,",
    "    updated_at = NOW();"
)

$imageLines = @(
    "-- ============================================",
    "-- Production Seed Data - Product Images",
    "-- ============================================",
    "-- Generated from docs_private/seed-images/seed_product_manifest.csv.",
    "-- S3 objects must be uploaded from docs_private/seed-upload before these rows can render images.",
    "",
    "WITH seed_images(product_id, image_id, s3_key, sort_order, is_thumbnail) AS (",
    "    VALUES"
)

$imageValues = @()
foreach ($row in $rows) {
    # 정밀 식별용 패딩 시퀀스 문자열 생성 (ex: 000000000001)
    $n = "{0:D12}" -f [int]$row.product_no
    $slug = $row.slug
    $targetDir = Join-Path $UploadRoot ("products\seed\" + $slug)
    New-Item -ItemType Directory -Path $targetDir -Force | Out-Null

    # 썸네일 이미지 가공 및 저장소 버킷 주소 맵핑
    $thumbnailSource = Join-Path $ImageSourceDir $row.thumbnail_file
    if (-not (Test-Path $thumbnailSource)) {
        throw "Thumbnail file not found for product $($row.product_id): $thumbnailSource"
    }
    Copy-Item -Path $thumbnailSource -Destination (Join-Path $targetDir "thumbnail.jpg") -Force

    $thumbnailImageId = "95200000-0000-0000-0000-$n"
    $thumbnailS3Key = "products/seed/$slug/thumbnail.jpg"
    $imageValues += "        -- $($row.title)"
    $imageValues += "        ('$($row.product_id)', '$thumbnailImageId', '$thumbnailS3Key', 0, true)"

    # 상세 정보(Detail) 본문 이미지 가공 및 버킷 주소 맵핑 (선택 사항 검증)
    if ($row.detail_file -and -not [string]::IsNullOrWhiteSpace($row.detail_file)) {
        $detailSource = Join-Path $ImageSourceDir $row.detail_file
        if (-not (Test-Path $detailSource)) {
            throw "Detail file not found for product $($row.product_id): $detailSource"
        }
        Copy-Item -Path $detailSource -Destination (Join-Path $targetDir "detail-1.jpg") -Force

        $detailImageId = "95300000-0000-0000-0000-$n"
        $detailS3Key = "products/seed/$slug/detail-1.jpg"
        $imageValues += "        ('$($row.product_id)', '$detailImageId', '$detailS3Key', 1, false)"
    }
}

# 주석 데이터 구분 및 SQL 구문 포맷(쉼표 콤마 조율) 특수 처리 순회
for ($i = 0; $i -lt $imageValues.Count; $i++) {
    $line = $imageValues[$i]
    if ($line.TrimStart().StartsWith("--")) {
        $imageLines += $line
        continue
    }
    $nextDataExists = $false
    for ($j = $i + 1; $j -lt $imageValues.Count; $j++) {
        if (-not $imageValues[$j].TrimStart().StartsWith("--")) {
            $nextDataExists = $true
            break
        }
    }
    $imageLines += $line + $(if ($nextDataExists) { "," } else { "" })
}

$imageLines += @(
    ")",
    "INSERT INTO product.product_image (",
    "    image_id,",
    "    product_id,",
    "    s3_key,",
    "    sort_order,",
    "    is_thumbnail,",
    "    created_at",
    ")",
    "SELECT",
    "    seed.image_id::UUID,",
    "    seed.product_id::UUID,",
    "    seed.s3_key,",
    "    seed.sort_order,",
    "    seed.is_thumbnail,",
    "    NOW()",
    "FROM seed_images seed",
    "JOIN product.product product_row",
    "    ON product_row.product_id = seed.product_id::UUID",
    "ON CONFLICT (image_id) DO UPDATE SET",
    "    s3_key = EXCLUDED.s3_key,",
    "    sort_order = EXCLUDED.sort_order,",
    "    is_thumbnail = EXCLUDED.is_thumbnail;"
)

# -------------------------------------------------------------------------
# 6. 파일 저장 및 최종 결과 출력 (Persistence & Output)
# -------------------------------------------------------------------------
Write-Utf8NoBom -Path $ProductSeedPath -Lines $productLines
Write-Utf8NoBom -Path $ProductImageSeedPath -Lines $imageLines

Write-Host "Generated: $ProductSeedPath"
Write-Host "Generated: $ProductImageSeedPath"
Write-Host "Prepared upload folder: $UploadRoot"
Write-Host "Upload with: aws s3 sync $UploadRoot/ s3://todaylunchmenu/ --region ap-northeast-2"