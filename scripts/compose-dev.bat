@echo off
chcp 65001 > nul
setlocal

rem DEPRECATED:
rem 이 스크립트는 infra/docker/docker-compose.yml 기반의
rem 구형 전체 애플리케이션 docker compose 실행 흐름을 대상으로 합니다.
rem
rem 현재 권장 로컬 개발 방식은 아래와 같습니다.
rem 1) docker compose --env-file .env -f infra/docker/docker-compose.infra.yml up -d
rem 2) ./gradlew :{module-name}:bootRun
rem
rem payment / settlement / gateway를 컨테이너로 함께 실행하려는 경우에만
rem 이 스크립트를 사용하세요.

set ACTION=%1

if "%ACTION%"=="" goto :SHOW_MENU
goto :RUN_ACTION

:SHOW_MENU
echo.
echo =====================================
echo   compose-dev Menu
echo =====================================
echo   1) status       - Check container status
echo   2) restart-app  - Restart app containers (code-only change)
echo   3) rebuild-app  - Rebuild images and restart (dependency/config change)
echo   4) reset-soft   - Down all, rebuild and up (keep DB data)
echo   5) reset-hard   - Full reset including DB volume (WARNING: data loss)
echo   0) Cancel
echo.
set /p CHOICE="Enter number: "

if "%CHOICE%"=="1" set ACTION=status
if "%CHOICE%"=="2" set ACTION=restart-app
if "%CHOICE%"=="3" set ACTION=rebuild-app
if "%CHOICE%"=="4" set ACTION=reset-soft
if "%CHOICE%"=="5" goto :CONFIRM_HARD
if "%CHOICE%"=="0" goto :CANCEL
goto :CANCEL

:CONFIRM_HARD
echo.
echo [WARNING] reset-hard will delete DB volume (goods_mall_postgres_data).
set /p CONFIRM="Are you sure? (y/N): "
if /i "%CONFIRM%"=="y" set ACTION=reset-hard
if /i "%CONFIRM%"=="y" goto :RUN_ACTION
goto :CANCEL

:RUN_ACTION
echo.
echo [compose-dev] Action: %ACTION%
echo.

if "%ACTION%"=="status" (
    echo ^> docker compose ps
    docker compose ps
    goto :END
)

if "%ACTION%"=="restart-app" (
    echo [Note] If you changed Java code, run Gradle bootJar first:
    echo   gradlew :payment:bootJar :settlement:bootJar :gateway:bootJar
    echo.
    echo ^> docker compose up -d payment settlement gateway
    docker compose up -d payment settlement gateway
    goto :END
)

if "%ACTION%"=="rebuild-app" (
    echo ^> docker compose up -d --build payment settlement gateway
    docker compose up -d --build payment settlement gateway
    goto :END
)

if "%ACTION%"=="reset-soft" (
    echo ^> docker compose down --remove-orphans
    docker compose down --remove-orphans
    echo ^> docker compose up -d --build
    docker compose up -d --build
    goto :END
)

if "%ACTION%"=="reset-hard" (
    echo ^> docker compose down -v --remove-orphans
    docker compose down -v --remove-orphans
    echo ^> docker compose up -d --build
    docker compose up -d --build
    goto :END
)

echo Unknown action: %ACTION%
echo Valid actions: status, restart-app, rebuild-app, reset-soft, reset-hard
goto :END

:CANCEL
echo Cancelled.
goto :END

:END
endlocal
