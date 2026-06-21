# PowerShell script to start development environment
# Usage: .\start-dev.ps1

Write-Host "🚀 Starting Go Bus Express Development Environment..." -ForegroundColor Cyan
Write-Host ""

# Check if Docker is running
Write-Host "📋 Checking Docker..." -ForegroundColor Yellow
$dockerRunning = docker ps 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Docker is not running. Please start Docker Desktop first." -ForegroundColor Red
    exit 1
}
Write-Host "✅ Docker is running" -ForegroundColor Green
Write-Host ""

# Check if .env file exists
if (-not (Test-Path ".env")) {
    Write-Host "⚠️  Warning: .env file not found. Using default values." -ForegroundColor Yellow
    Write-Host "   Create a .env file with your configuration for production use." -ForegroundColor Yellow
    Write-Host ""
}

# Step 1: Start infrastructure services
Write-Host "📦 Step 1/4: Starting infrastructure services (PostgreSQL, Redis, RabbitMQ, MinIO)..." -ForegroundColor Cyan
docker-compose -f docker-compose.dev.yml up -d postgres redis rabbitmq minio
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Failed to start infrastructure services" -ForegroundColor Red
    exit 1
}

Write-Host "⏳ Waiting for infrastructure services to be healthy (30 seconds)..." -ForegroundColor Yellow
Start-Sleep -Seconds 30

# Check infrastructure health
$postgresHealth = docker exec busapp-postgres-dev pg_isready -U postgres -d busbooking 2>&1
$redisHealth = docker exec busapp-redis-dev redis-cli ping 2>&1

if ($postgresHealth -match "accepting connections") {
    Write-Host "✅ PostgreSQL is ready" -ForegroundColor Green
} else {
    Write-Host "⚠️  PostgreSQL might not be ready yet" -ForegroundColor Yellow
}

if ($redisHealth -match "PONG") {
    Write-Host "✅ Redis is ready" -ForegroundColor Green
} else {
    Write-Host "⚠️  Redis might not be ready yet" -ForegroundColor Yellow
}
Write-Host ""

# Step 2: Start Eureka Server
Write-Host "📦 Step 2/4: Starting Eureka Server (Service Discovery)..." -ForegroundColor Cyan
docker-compose -f docker-compose.dev.yml up -d eureka-server
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Failed to start Eureka Server" -ForegroundColor Red
    exit 1
}

Write-Host "⏳ Waiting for Eureka Server to be ready (60 seconds)..." -ForegroundColor Yellow
Start-Sleep -Seconds 60

# Check Eureka health
try {
    $eurekaHealth = Invoke-WebRequest -Uri "http://localhost:8761/actuator/health" -UseBasicParsing -TimeoutSec 5
    if ($eurekaHealth.StatusCode -eq 200) {
        Write-Host "✅ Eureka Server is ready" -ForegroundColor Green
    }
} catch {
    Write-Host "⚠️  Eureka Server might not be ready yet" -ForegroundColor Yellow
}
Write-Host ""

# Step 3: Start Gateway Service
Write-Host "📦 Step 3/4: Starting API Gateway..." -ForegroundColor Cyan
docker-compose -f docker-compose.dev.yml up -d gateway-service
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Failed to start Gateway Service" -ForegroundColor Red
    exit 1
}

Write-Host "⏳ Waiting for Gateway to be ready (60 seconds)..." -ForegroundColor Yellow
Start-Sleep -Seconds 60

# Check Gateway health
try {
    $gatewayHealth = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -UseBasicParsing -TimeoutSec 5
    if ($gatewayHealth.StatusCode -eq 200) {
        Write-Host "✅ API Gateway is ready" -ForegroundColor Green
    }
} catch {
    Write-Host "⚠️  API Gateway might not be ready yet" -ForegroundColor Yellow
}
Write-Host ""

# Step 4: Start Microservices
Write-Host "📦 Step 4/4: Starting Microservices (User, Bus, Booking)..." -ForegroundColor Cyan
docker-compose -f docker-compose.dev.yml up -d user-service bus-service booking-service
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Failed to start Microservices" -ForegroundColor Red
    exit 1
}

Write-Host "⏳ Waiting for Microservices to be ready (90 seconds)..." -ForegroundColor Yellow
Start-Sleep -Seconds 90

Write-Host ""
Write-Host "🎉 Development environment started!" -ForegroundColor Green
Write-Host ""
Write-Host "📊 Service URLs:" -ForegroundColor Cyan
Write-Host "   • Eureka Dashboard:    http://localhost:8761" -ForegroundColor White
Write-Host "   • API Gateway:         http://localhost:8080" -ForegroundColor White
Write-Host "   • User Service:        http://localhost:8081" -ForegroundColor White
Write-Host "   • Bus Service:         http://localhost:8082" -ForegroundColor White
Write-Host "   • Booking Service:     http://localhost:8083" -ForegroundColor White
Write-Host "   • RabbitMQ Management: http://localhost:15672 (guest/guest)" -ForegroundColor White
Write-Host "   • MinIO Console:       http://localhost:9001 (minioadmin/minioadmin)" -ForegroundColor White
Write-Host "   • RedisInsight:        http://localhost:5540" -ForegroundColor White
Write-Host ""
Write-Host "📝 Useful commands:" -ForegroundColor Cyan
Write-Host "   • View logs:           docker-compose -f docker-compose.dev.yml logs -f" -ForegroundColor White
Write-Host "   • Check status:        docker-compose -f docker-compose.dev.yml ps" -ForegroundColor White
Write-Host "   • Stop all:            docker-compose -f docker-compose.dev.yml down" -ForegroundColor White
Write-Host "   • Restart service:     docker-compose -f docker-compose.dev.yml restart <service-name>" -ForegroundColor White
Write-Host ""
Write-Host "🔍 Checking service status..." -ForegroundColor Yellow
docker-compose -f docker-compose.dev.yml ps
Write-Host ""
Write-Host "✨ Ready to develop! Check Eureka dashboard to verify all services are registered." -ForegroundColor Green
