@echo off
setlocal EnableDelayedExpansion

echo ============================================================
echo  BusApp - Pull Images from Docker Hub and Run All Services
echo ============================================================
echo.

REM ── Hardcoded image config — no input needed ──────────────────────────────────
set DOCKER_HUB_USER=kongming16
set TAG=v1.0

echo Docker Hub User : %DOCKER_HUB_USER%
echo Tag             : %TAG%
echo.

REM ── Check .env file — auto-create from example if missing ───────────────────
if not exist ".env" (
    echo .env not found — creating from .env.example...
    copy .env.example .env >nul
    echo.
    echo IMPORTANT: Open .env and fill in JWT_PRIVATE_KEY and JWT_PUBLIC_KEY
    echo            before the services can start properly.
    echo.
    pause
)

REM ── Pull all service images ───────────────────────────────────────────────────
echo [1/2] Pulling images from Docker Hub...
echo.

set SERVICES=eureka-server user-service bus-service booking-service gateway-service
set FAILED=

for %%S in (%SERVICES%) do (
    echo Pulling %DOCKER_HUB_USER%/busapp-%%S:%TAG% ...
    docker pull %DOCKER_HUB_USER%/busapp-%%S:%TAG%
    if errorlevel 1 (
        echo ERROR: Failed to pull %%S
        set FAILED=!FAILED! %%S
    )
)

if not "%FAILED%"=="" (
    echo.
    echo ERROR: Failed to pull:%FAILED%
    echo Aborting. Check your Docker Hub username and tag.
    exit /b 1
)

echo.
echo All images pulled successfully.
echo.

REM ── Start all containers ──────────────────────────────────────────────────────
echo [2/2] Starting all containers...
echo.

set DOCKER_HUB_USER=%DOCKER_HUB_USER%
set TAG=%TAG%

docker compose -f docker-compose.prod.yml --env-file .env up -d

if errorlevel 1 (
    echo.
    echo ERROR: Failed to start containers.
    exit /b 1
)

echo.
echo ============================================================
echo  All services are running!
echo.
echo  Service URLs:
echo    Gateway   : http://localhost:8080
echo    Eureka    : http://localhost:8761
echo    User      : http://localhost:8081
echo    Bus       : http://localhost:8082
echo    Booking   : http://localhost:8083
echo    Redis UI  : http://localhost:5540
echo ============================================================

endlocal
