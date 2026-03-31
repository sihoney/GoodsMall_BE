param(
    [string]$Action = "",
    [switch]$DryRun
)
Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"
function Invoke-ComposeCommand {
    param(
        [string[]]$CmdArgs
    )
    $display = "docker compose " + ($CmdArgs -join " ")
    Write-Host "> $display" -ForegroundColor Cyan
    if ($DryRun) {
        return
    }
    & docker compose @CmdArgs
}
# No action argument: show menu
if ($Action -eq "") {
    Write-Host ""
    Write-Host "=====================================" -ForegroundColor Yellow
    Write-Host "  compose-dev Menu" -ForegroundColor Yellow
    Write-Host "=====================================" -ForegroundColor Yellow
    Write-Host "  1) status       - Check container status"
    Write-Host "  2) restart-app  - Restart app containers (code-only change)"
    Write-Host "  3) rebuild-app  - Rebuild images and restart (dependency/config change)"
    Write-Host "  4) reset-soft   - Down all, rebuild and up (keep DB data)"
    Write-Host "  5) reset-hard   - Full reset including DB volume (WARNING: data loss)"
    Write-Host "  0) Cancel"
    Write-Host ""
    $choice = Read-Host "Enter number"
    switch ($choice) {
        "1" { $Action = "status" }
        "2" { $Action = "restart-app" }
        "3" { $Action = "rebuild-app" }
        "4" { $Action = "reset-soft" }
        "5" {
            Write-Host ""
            Write-Host "[WARNING] reset-hard will delete DB volume (goods_mall_postgres_data)." -ForegroundColor Red
            $confirm = Read-Host "Are you sure? (y/N)"
            if ($confirm -ne "y" -and $confirm -ne "Y") {
                Write-Host "Cancelled." -ForegroundColor Yellow
                exit 0
            }
            $Action = "reset-hard"
        }
        default {
            Write-Host "Cancelled." -ForegroundColor Yellow
            exit 0
        }
    }
}
Write-Host ""
Write-Host "[compose-dev] Action: $Action" -ForegroundColor Green
if ($DryRun) {
    Write-Host "[compose-dev] DryRun mode: print commands only." -ForegroundColor Yellow
}
switch ($Action) {
    "status" {
        Invoke-ComposeCommand -CmdArgs @("ps")
        break
    }
    "restart-app" {
        Write-Host "[Note] If you changed Java code, run Gradle bootJar first:" -ForegroundColor Yellow
        Write-Host "  .\gradlew :payment:bootJar :settlement:bootJar :gateway:bootJar" -ForegroundColor Yellow
        Write-Host ""
        Invoke-ComposeCommand -CmdArgs @("up", "-d", "payment", "settlement", "gateway")
        break
    }
    "rebuild-app" {
        Invoke-ComposeCommand -CmdArgs @("up", "-d", "--build", "payment", "settlement", "gateway")
        break
    }
    "reset-soft" {
        Invoke-ComposeCommand -CmdArgs @("down", "--remove-orphans")
        Invoke-ComposeCommand -CmdArgs @("up", "-d", "--build")
        break
    }
    "reset-hard" {
        Invoke-ComposeCommand -CmdArgs @("down", "-v", "--remove-orphans")
        Invoke-ComposeCommand -CmdArgs @("up", "-d", "--build")
        break
    }
}