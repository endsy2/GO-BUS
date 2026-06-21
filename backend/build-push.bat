@echo off
setlocal EnableDelayedExpansion

echo ============================================================
echo  BusApp - Build and Push All Services to Docker Hub
echo ============================================================
echo.

REM ── Parameters ────────────────────────────────────────────────────────────────
if "%~1"=="" (
    set /p DOCKER_HUB_USER="Enter your Docker Hub username: "
) else (
    set DOCKER_HUB_USER=%~1
)

if "%~2"=="" (
    set TAG=latest
) else (
    set TAG=%~2
)

echo.
echo Docker Hub User : %DOCKER_HUB_USER%
echo Tag             : %TAG%
echo.

REM ── Login ─────────────────────────────────────────────────────────────────────
echo [*] Logging in to Docker Hub...
docker login
if errorlevel 1 (
    echo ERROR: Docker login failed. Aborting.
    exit /b 1
)
echo.

REM ── Services to build ─────────────────────────────────────────────────────────
set SERVICES=eureka-server user-service bus-service booking-service gateway-service
set FAILED=

for %%S in (%SERVICES%) do (
    echo ────────────────────────────────────────────────────────────
    echo Building: %%S
    echo Image   : %DOCKER_HUB_USER%/busapp-%%S:%TAG%
    echo ────────────────────────────────────────────────────────────

    docker build -t %DOCKER_HUB_USER%/busapp-%%S:%TAG% .\%%S
    if errorlevel 1 (
        echo ERROR: Build failed for %%S
        set FAILED=!FAILED! %%S
    ) else (
        echo Pushing: %DOCKER_HUB_USER%/busapp-%%S:%TAG%
        docker push %DOCKER_HUB_USER%/busapp-%%S:%TAG%
        if errorlevel 1 (
            echo ERROR: Push failed for %%S
            set FAILED=!FAILED! %%S
        ) else (
            echo OK: %%S pushed successfully.
        )
    )
    echo.
)

REM ── Summary ───────────────────────────────────────────────────────────────────
echo ============================================================
if "%FAILED%"=="" (
    echo  All services built and pushed successfully!
    echo.
    echo  Images on Docker Hub:
    for %%S in (%SERVICES%) do (
        echo    %DOCKER_HUB_USER%/busapp-%%S:%TAG%
    )
) else (
    echo  Build/push FAILED for:%FAILED%
    echo  All other services completed.
)
echo ============================================================

endlocal
