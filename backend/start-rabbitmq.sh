#!/bin/bash

# ============================================================================
# RabbitMQ Quick Start Script for BusApp
# ============================================================================

echo "🚀 Starting RabbitMQ for BusApp Notification System..."
echo ""

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Error: Docker is not running. Please start Docker first."
    exit 1
fi

# Start RabbitMQ container
echo "📦 Starting RabbitMQ container..."
docker-compose -f docker-compose-rabbitmq.yml up -d

# Wait for RabbitMQ to be ready
echo "⏳ Waiting for RabbitMQ to be ready..."
sleep 10

# Check if RabbitMQ is running
if docker ps | grep -q busapp-rabbitmq; then
    echo "✅ RabbitMQ is running!"
    echo ""
    echo "📊 RabbitMQ Management UI:"
    echo "   URL: http://localhost:15672"
    echo "   Username: guest"
    echo "   Password: guest"
    echo ""
    echo "🔌 AMQP Connection:"
    echo "   Host: localhost"
    echo "   Port: 5672"
    echo ""
    echo "📝 Next Steps:"
    echo "   1. Open Management UI: http://localhost:15672"
    echo "   2. Start booking-service: cd booking-service && ./gradlew bootRun"
    echo "   3. Test notification: POST http://localhost:8083/api/v1/test/notifications/custom?message=Test"
    echo ""
    echo "📖 For more details, see: RABBITMQ_SETUP.md"
else
    echo "❌ Failed to start RabbitMQ. Check Docker logs:"
    echo "   docker logs busapp-rabbitmq"
    exit 1
fi
