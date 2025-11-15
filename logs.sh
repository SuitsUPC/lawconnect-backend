#!/bin/bash

# Script para ver logs de los servicios

BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Obtener la ruta absoluta del proyecto
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

SERVICE=$1

if [ -z "$SERVICE" ]; then
    echo -e "${BLUE}📋 Mostrando logs de todos los servicios (Ctrl+C para salir)...${NC}"
    echo ""
    docker compose -f "$PROJECT_ROOT/microservices/docker-compose.yml" logs -f
else
    echo -e "${BLUE}📋 Mostrando logs de ${SERVICE} (Ctrl+C para salir)...${NC}"
    echo ""
    docker compose -f "$PROJECT_ROOT/microservices/docker-compose.yml" logs -f "$SERVICE"
fi

