#!/usr/bin/env bash
set -euo pipefail

echo "============================================================"
echo " BusApp - Build and Push All Services to Docker Hub"
echo "============================================================"
echo

# ── Parameters ────────────────────────────────────────────────────────────────
if [[ -n "${1:-}" ]]; then
    DOCKER_HUB_USER="$1"
else
    read -r -p "Enter your Docker Hub username: " DOCKER_HUB_USER
fi

TAG="${2:-latest}"

echo
echo "Docker Hub User : $DOCKER_HUB_USER"
echo "Tag             : $TAG"
echo

# ── Login ─────────────────────────────────────────────────────────────────────
echo "[*] Logging in to Docker Hub..."
docker login
echo

# ── Services to build ─────────────────────────────────────────────────────────
SERVICES=(
    "eureka-server"
    "user-service"
    "bus-service"
    "booking-service"
    "gateway-service"
)

FAILED=()

for SERVICE in "${SERVICES[@]}"; do
    echo "────────────────────────────────────────────────────────────"
    echo "Building: $SERVICE"
    echo "Image   : $DOCKER_HUB_USER/busapp-$SERVICE:$TAG"
    echo "────────────────────────────────────────────────────────────"

    if docker build -t "$DOCKER_HUB_USER/busapp-$SERVICE:$TAG" "./$SERVICE"; then
        echo "Pushing: $DOCKER_HUB_USER/busapp-$SERVICE:$TAG"
        if docker push "$DOCKER_HUB_USER/busapp-$SERVICE:$TAG"; then
            echo "OK: $SERVICE pushed successfully."
        else
            echo "ERROR: Push failed for $SERVICE"
            FAILED+=("$SERVICE")
        fi
    else
        echo "ERROR: Build failed for $SERVICE"
        FAILED+=("$SERVICE")
    fi
    echo
done

# ── Summary ───────────────────────────────────────────────────────────────────
echo "============================================================"
if [[ ${#FAILED[@]} -eq 0 ]]; then
    echo " All services built and pushed successfully!"
    echo
    echo " Images on Docker Hub:"
    for SERVICE in "${SERVICES[@]}"; do
        echo "   $DOCKER_HUB_USER/busapp-$SERVICE:$TAG"
    done
else
    echo " Build/push FAILED for: ${FAILED[*]}"
    echo " All other services completed."
fi
echo "============================================================"
