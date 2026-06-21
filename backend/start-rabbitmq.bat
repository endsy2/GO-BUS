@echo off
REM ============================================================================
REM RabbitMQ Quick Start Script for BusApp (Windows)
REM ============================================================================

echo Starting RabbitMQ for BusApp Notification System...
echo.

REM Check if Docker is running
docker info >nul 2>&1
if errorlevel 1 (
    echo Error: Docker is not running. Please start Docker first.
    exit /b 1
)

REM Start RabbitMQ container
echo Starting RabbitMQ container...
docker-compose -f docker-compose-rabbitmq.yml up -d

REM Wait for RabbitMQ to be ready
echo Waiting for RabbitMQ to be ready...
timeout /t 10 /nobreak >nul

REM Check if RabbitMQ is running
docker ps | findstr busapp-rabbitmq >nul
if errorlevel 1 (
    echo Failed to start RabbitMQ. Check Docker logs:
    echo    docker logs busapp-rabbitmq
    exit /b 1
)

echo RabbitMQ is running!
echo.
echo RabbitMQ Management UI:
echo    URL: http://localhost:15672
echo    Username: guest
echo    Password: guest
echo.
echo AMQP Connection:
echo    Host: localhost
echo    Port: 5672
echo.
echo Next Steps:
echo    1. Open Management UI: http://localhost:15672
echo    2. Start booking-service: cd booking-service ^&^& gradlew bootRun
echo    3. Test notification: POST http://localhost:8083/api/v1/test/notifications/custom?message=Test
echo.
echo For more details, see: RABBITMQ_SETUP.md
