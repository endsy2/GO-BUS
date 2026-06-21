# ─── Stage 1: Build ─────────────────────────────
FROM gradle:8.5-jdk17 AS build
WORKDIR /app

# Copy entire project
COPY . .

# FIX: ensure gradlew is executable
RUN chmod +x gradlew

# Build the application
RUN ./gradlew build -x test --no-daemon


# ─── Stage 2: Runtime ───────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN apk add --no-cache curl

# Copy built jar with explicit name
COPY --from=build /app/build/libs/gateway-service.jar app.jar

# Verify jar exists and list contents
RUN ls -lh /app/ && echo "Java version:" && java -version

EXPOSE 8080

# Add verbose logging to see what's happening
ENTRYPOINT ["sh", "-c", "echo 'Starting Gateway Server on port '${PORT:-8080} && java -Dserver.port=${PORT:-8080} -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-prod} -Xmx768m -Xms512m -jar app.jar"]