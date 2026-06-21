# PowerShell script to create all railway.json files

Write-Host "Creating railway.json files for all services..." -ForegroundColor Green
Write-Host ""

# Eureka Server
Write-Host "Creating backend/eureka-server/railway.json..."
@"
{
  "`$schema": "https://railway.app/railway.schema.json",
  "build": {
    "builder": "NIXPACKS",
    "buildCommand": "./gradlew :eureka-server:build -x test"
  },
  "deploy": {
    "startCommand": "java -Dserver.port=`$PORT -jar eureka-server/build/libs/eureka-server-1.0.0.jar",
    "healthcheckPath": "/actuator/health",
    "healthcheckTimeout": 300,
    "restartPolicyType": "ON_FAILURE",
    "restartPolicyMaxRetries": 10
  }
}
"@ | Out-File -FilePath "backend/eureka-server/railway.json" -Encoding UTF8

# Gateway Service
Write-Host "Creating backend/gateway-service/railway.json..."
@"
{
  "`$schema": "https://railway.app/railway.schema.json",
  "build": {
    "builder": "NIXPACKS",
    "buildCommand": "./gradlew :gateway-service:build -x test"
  },
  "deploy": {
    "startCommand": "java -Dserver.port=`$PORT -Xmx512m -Xms256m -jar gateway-service/build/libs/gateway-service.jar",
    "healthcheckPath": "/actuator/health",
    "healthcheckTimeout": 300,
    "restartPolicyType": "ON_FAILURE",
    "restartPolicyMaxRetries": 10
  }
}
"@ | Out-File -FilePath "backend/gateway-service/railway.json" -Encoding UTF8

# User Service
Write-Host "Creating backend/user-service/railway.json..."
@"
{
  "`$schema": "https://railway.app/railway.schema.json",
  "build": {
    "builder": "NIXPACKS",
    "buildCommand": "./gradlew :user-service:build -x test"
  },
  "deploy": {
    "startCommand": "java -Dserver.port=`$PORT -Xmx512m -Xms256m -jar user-service/build/libs/user-service-1.0.0.jar",
    "healthcheckPath": "/actuator/health",
    "healthcheckTimeout": 300,
    "restartPolicyType": "ON_FAILURE",
    "restartPolicyMaxRetries": 10
  }
}
"@ | Out-File -FilePath "backend/user-service/railway.json" -Encoding UTF8

# Booking Service
Write-Host "Creating backend/booking-service/railway.json..."
@"
{
  "`$schema": "https://railway.app/railway.schema.json",
  "build": {
    "builder": "NIXPACKS",
    "buildCommand": "./gradlew :booking-service:build -x test"
  },
  "deploy": {
    "startCommand": "java -Dserver.port=`$PORT -Xmx512m -Xms256m -jar booking-service/build/libs/booking-service-1.0.0.jar",
    "healthcheckPath": "/actuator/health",
    "healthcheckTimeout": 300,
    "restartPolicyType": "ON_FAILURE",
    "restartPolicyMaxRetries": 10
  }
}
"@ | Out-File -FilePath "backend/booking-service/railway.json" -Encoding UTF8

# Bus Service
Write-Host "Creating backend/bus-service/railway.json..."
@"
{
  "`$schema": "https://railway.app/railway.schema.json",
  "build": {
    "builder": "NIXPACKS",
    "buildCommand": "./gradlew :bus-service:build -x test"
  },
  "deploy": {
    "startCommand": "java -Dserver.port=`$PORT -Xmx512m -Xms256m -jar bus-service/build/libs/bus-service-1.0.0.jar",
    "healthcheckPath": "/actuator/health",
    "healthcheckTimeout": 300,
    "restartPolicyType": "ON_FAILURE",
    "restartPolicyMaxRetries": 10
  }
}
"@ | Out-File -FilePath "backend/bus-service/railway.json" -Encoding UTF8

Write-Host ""
Write-Host "✓ All railway.json files created successfully!" -ForegroundColor Green
Write-Host ""
Write-Host "Files created:"
Write-Host "  - backend/eureka-server/railway.json"
Write-Host "  - backend/gateway-service/railway.json"
Write-Host "  - backend/user-service/railway.json"
Write-Host "  - backend/booking-service/railway.json"
Write-Host "  - backend/bus-service/railway.json"
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "1. In Railway dashboard, set Root Directory to 'backend' for each service"
Write-Host "2. Railway will automatically use railway.json for deployment"
Write-Host "3. Delete or ignore Dockerfile if you want to use railway.json"
Write-Host ""
