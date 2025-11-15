#!/bin/bash

# Script para ver el estado de los servicios

BLUE='\033[0;34m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Obtener la ruta absoluta del proyecto
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo -e "${BLUE}📊 Estado de los servicios:${NC}"
echo ""
docker compose -f "$PROJECT_ROOT/microservices/docker-compose.yml" ps

echo ""
echo -e "${BLUE}📋 Logs cortos de cada servicio (últimas 10 líneas):${NC}"
echo ""

SERVICES=("iam-service" "profiles-service" "cases-service" "api-gateway")

for service in "${SERVICES[@]}"; do
    echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${BLUE}${service}:${NC}"
    echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    docker compose -f "$PROJECT_ROOT/microservices/docker-compose.yml" logs --tail=10 "$service" 2>/dev/null || echo "Servicio no disponible"
    echo ""
done

