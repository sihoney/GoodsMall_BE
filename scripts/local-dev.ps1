param(
    [Parameter(Position = 0)]
    [string]$Action = "",
    [Parameter(Position = 1, ValueFromRemainingArguments = $true)]
    [string[]]$ModuleName = @(),
    [switch]$DryRun
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$ComposeArgs = @("--env-file", ".env", "-f", "infra/docker/docker-compose.infra.yml")

function Import-RootEnv {
    $envPath = Join-Path (Get-Location) ".env"
    if (-not (Test-Path -LiteralPath $envPath)) {
        Write-Host "[local-dev] .env 파일이 없어 환경변수 로드를 건너뜁니다." -ForegroundColor Yellow
        return
    }

    Get-Content -LiteralPath $envPath | ForEach-Object {
        $line = $_.Trim()
        if ($line -eq "" -or $line.StartsWith("#")) {
            return
        }

        $separatorIndex = $line.IndexOf("=")
        if ($separatorIndex -le 0) {
            return
        }

        $name = $line.Substring(0, $separatorIndex).Trim()
        $value = $line.Substring($separatorIndex + 1).Trim()
        if ($value.Length -ge 2) {
            $first = $value.Substring(0, 1)
            $last = $value.Substring($value.Length - 1, 1)
            if (($first -eq '"' -and $last -eq '"') -or ($first -eq "'" -and $last -eq "'")) {
                $value = $value.Substring(1, $value.Length - 2)
            }
        }

        [Environment]::SetEnvironmentVariable($name, $value, "Process")
    }
}

function Invoke-InfraCommand {
    param(
        [string[]]$CmdArgs
    )

    $display = "docker compose " + (($ComposeArgs + $CmdArgs) -join " ")
    Write-Host "> $display" -ForegroundColor Cyan
    if ($DryRun) {
        return
    }
    & docker compose @ComposeArgs @CmdArgs
}

function Normalize-ModuleName {
    param(
        [string]$TargetModule
    )

    if ([string]::IsNullOrWhiteSpace($TargetModule)) {
        return ""
    }

    $NormalizedModule = $TargetModule.Trim()
    $NormalizedModule = $NormalizedModule -replace '\\', '/'
    $NormalizedModule = $NormalizedModule -replace '^service/', ''
    $NormalizedModule = $NormalizedModule.Trim(':')

    return $NormalizedModule
}

function Get-TargetModules {
    param(
        [string[]]$InputModules
    )

    $modules = @()
    foreach ($moduleToken in $InputModules) {
        if ([string]::IsNullOrWhiteSpace($moduleToken)) {
            continue
        }

        [regex]::Split($moduleToken, "[,\s]+") | Where-Object { $_ -ne "" } | ForEach-Object {
            $normalized = Normalize-ModuleName -TargetModule $_
            if ($normalized -ne "") {
                $modules += $normalized
            }
        }
    }

    return $modules
}

function Invoke-BootRun {
    param(
        [string[]]$TargetModules
    )

    $modules = @(Get-TargetModules -InputModules $TargetModules)
    if ($modules.Count -eq 0) {
        throw "bootRun을 실행할 module-name이 1개 이상 필요합니다."
    }

    $tasks = @($modules | ForEach-Object { ":$_`:bootRun" })
    $gradleArgs = @("--no-daemon")
    if ($tasks.Count -gt 1) {
        $gradleArgs += "--parallel"
    }
    $gradleArgs += $tasks

    $display = "./gradlew " + ($gradleArgs -join " ")
    Write-Host "> $display" -ForegroundColor Cyan
    if ($DryRun) {
        return
    }
    Import-RootEnv
    & ./gradlew @gradleArgs
}

if ($Action -eq "") {
    Write-Host ""
    Write-Host "=====================================" -ForegroundColor Yellow
    Write-Host "  local-dev Menu" -ForegroundColor Yellow
    Write-Host "=====================================" -ForegroundColor Yellow
    Write-Host "  1) infra-status       - 인프라 컨테이너 상태 확인"
    Write-Host "  2) infra-up           - 인프라 컨테이너 실행"
    Write-Host "  3) infra-down         - 인프라 컨테이너 중지"
    Write-Host "  4) infra-reset        - 인프라 컨테이너 재생성"
    Write-Host "  5) bootrun            - Gradle bootRun으로 서비스 실행"
    Write-Host "  6) up-and-bootrun     - 인프라 실행 후 서비스 실행"
    Write-Host "  0) 취소"
    Write-Host ""
    $choice = Read-Host "번호를 입력하세요"
    switch ($choice) {
        "1" { $Action = "infra-status" }
        "2" { $Action = "infra-up" }
        "3" { $Action = "infra-down" }
        "4" { $Action = "infra-reset" }
        "5" {
            $Action = "bootrun"
            $ModuleName = (Read-Host "module-name을 입력하세요 (예: member product 또는 member,product)").Split(" ", [System.StringSplitOptions]::RemoveEmptyEntries)
        }
        "6" {
            $Action = "up-and-bootrun"
            $ModuleName = (Read-Host "module-name을 입력하세요 (예: member product 또는 member,product)").Split(" ", [System.StringSplitOptions]::RemoveEmptyEntries)
        }
        default {
            Write-Host "취소했습니다." -ForegroundColor Yellow
            exit 0
        }
    }
}

Write-Host ""
Write-Host "[local-dev] 실행 작업: $Action" -ForegroundColor Green
$DisplayModules = @(Get-TargetModules -InputModules $ModuleName)
if ($DisplayModules.Count -gt 0) {
    Write-Host "[local-dev] 실행 모듈: $($DisplayModules -join ', ')" -ForegroundColor Green
}
if ($DryRun) {
    Write-Host "[local-dev] DryRun 모드: 명령어만 출력하고 실제 실행하지 않습니다." -ForegroundColor Yellow
}

switch ($Action) {
    "infra-status" {
        Invoke-InfraCommand -CmdArgs @("ps")
        break
    }
    "infra-up" {
        Invoke-InfraCommand -CmdArgs @("up", "-d")
        break
    }
    "infra-down" {
        Invoke-InfraCommand -CmdArgs @("down")
        break
    }
    "infra-reset" {
        Invoke-InfraCommand -CmdArgs @("down", "--remove-orphans")
        Invoke-InfraCommand -CmdArgs @("up", "-d")
        break
    }
    "bootrun" {
        Invoke-BootRun -TargetModules $ModuleName
        break
    }
    "up-and-bootrun" {
        Invoke-InfraCommand -CmdArgs @("up", "-d")
        Invoke-BootRun -TargetModules $ModuleName
        break
    }
    default {
        throw "알 수 없는 작업입니다: $Action"
    }
}
