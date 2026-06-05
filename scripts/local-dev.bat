@echo off
chcp 65001 > nul
setlocal

set ACTION=%1
set MODULE_NAME=%2

if "%ACTION%"=="" goto :SHOW_MENU
goto :RUN_ACTION

:SHOW_MENU
echo.
echo =====================================
echo   local-dev Menu
echo =====================================
echo   1) infra-status       - Check infra container status
echo   2) infra-up           - Start infra containers
echo   3) infra-down         - Stop infra containers
echo   4) infra-reset        - Recreate infra containers
echo   5) bootrun            - Run one service with Gradle bootRun
echo   6) up-and-bootrun     - Start infra, then run one service
echo   0) Cancel
echo.
set /p CHOICE="Enter number: "

if "%CHOICE%"=="1" set ACTION=infra-status
if "%CHOICE%"=="2" set ACTION=infra-up
if "%CHOICE%"=="3" set ACTION=infra-down
if "%CHOICE%"=="4" set ACTION=infra-reset
if "%CHOICE%"=="5" (
    set ACTION=bootrun
    set /p MODULE_NAME="Enter module-name: "
)
if "%CHOICE%"=="6" (
    set ACTION=up-and-bootrun
    set /p MODULE_NAME="Enter module-name: "
)
if "%CHOICE%"=="0" goto :CANCEL
if "%ACTION%"=="" goto :CANCEL

:RUN_ACTION
echo.
echo [local-dev] Action: %ACTION%
if not "%MODULE_NAME%"=="" echo [local-dev] Module: %MODULE_NAME%
echo.

if "%ACTION%"=="infra-status" (
    echo ^> docker compose --env-file .env -f infra/docker/docker-compose.infra.yml ps
    docker compose --env-file .env -f infra/docker/docker-compose.infra.yml ps
    goto :END
)

if "%ACTION%"=="infra-up" (
    echo ^> docker compose --env-file .env -f infra/docker/docker-compose.infra.yml up -d
    docker compose --env-file .env -f infra/docker/docker-compose.infra.yml up -d
    goto :END
)

if "%ACTION%"=="infra-down" (
    echo ^> docker compose --env-file .env -f infra/docker/docker-compose.infra.yml down
    docker compose --env-file .env -f infra/docker/docker-compose.infra.yml down
    goto :END
)

if "%ACTION%"=="infra-reset" (
    echo ^> docker compose --env-file .env -f infra/docker/docker-compose.infra.yml down --remove-orphans
    docker compose --env-file .env -f infra/docker/docker-compose.infra.yml down --remove-orphans
    echo ^> docker compose --env-file .env -f infra/docker/docker-compose.infra.yml up -d
    docker compose --env-file .env -f infra/docker/docker-compose.infra.yml up -d
    goto :END
)

if "%ACTION%"=="bootrun" (
    if "%MODULE_NAME%"=="" goto :MISSING_MODULE
    echo ^> gradlew :%MODULE_NAME%:bootRun
    gradlew :%MODULE_NAME%:bootRun
    goto :END
)

if "%ACTION%"=="up-and-bootrun" (
    if "%MODULE_NAME%"=="" goto :MISSING_MODULE
    echo ^> docker compose --env-file .env -f infra/docker/docker-compose.infra.yml up -d
    docker compose --env-file .env -f infra/docker/docker-compose.infra.yml up -d
    echo ^> gradlew :%MODULE_NAME%:bootRun
    gradlew :%MODULE_NAME%:bootRun
    goto :END
)

echo Unknown action: %ACTION%
goto :END

:MISSING_MODULE
echo module-name is required.
goto :END

:CANCEL
echo Cancelled.
goto :END

:END
endlocal
