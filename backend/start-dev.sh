#!/bin/bash

echo "========================================"
echo "Starting Go Bus Backend - Development"
echo "========================================"
echo ""

echo "Building and starting all services..."
docker-compose -f docker-compose.dev.yml up --build
