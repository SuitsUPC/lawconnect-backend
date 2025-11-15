#!/bin/bash

# Script para detener LawConnect Backend

BLUE='\033[0;34m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Obtener la ruta absoluta del proyecto
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo -e "${BLUE}🛑 Deteniendo servicios de LawConnect Backend...${NC}"
echo ""

docker compose -f "$PROJECT_ROOT/microservices/docker-compose.yml" down

echo ""
echo -e "${GREEN}✅ Servicios detenidos correctamente${NC}"
echo ""

