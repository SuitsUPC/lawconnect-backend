#!/bin/bash

# Script para desplegar LawConnect Backend en GCP usando Cloud Run
# Más simple: construye las imágenes y las despliega en Cloud Run

set +e

# Colores
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m'

# Configuración
PROJECT_ID=$(gcloud config get-value project 2>/dev/null)
REGION=$(gcloud config get-value compute/region 2>/dev/null || echo "southamerica-east1")
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo -e "${BLUE}🚀 Desplegando LawConnect Backend en GCP Cloud Run...${NC}"
echo ""

# Verificar que gcloud está configurado
if [ -z "$PROJECT_ID" ]; then
    echo -e "${RED}❌ Error: No hay proyecto de GCP configurado${NC}"
    echo -e "${YELLOW}Ejecuta: gcloud config set project TU_PROJECT_ID${NC}"
    exit 1
fi

echo -e "${BLUE}📋 Configuración:${NC}"
echo -e "   Proyecto: ${GREEN}$PROJECT_ID${NC}"
echo -e "   Región: ${GREEN}$REGION${NC}"
echo ""

# Habilitar APIs necesarias
echo -e "${BLUE}🔧 Habilitando APIs de GCP...${NC}"
gcloud services enable cloudbuild.googleapis.com
gcloud services enable run.googleapis.com
gcloud services enable artifactregistry.googleapis.com
gcloud services enable sqladmin.googleapis.com
echo -e "${GREEN}✅ APIs habilitadas${NC}"
echo ""

# Crear Artifact Registry (si no existe)
REPO_NAME="lawconnect-repo"
echo -e "${BLUE}📦 Configurando Artifact Registry...${NC}"
gcloud artifacts repositories create "$REPO_NAME" \
    --repository-format=docker \
    --location="$REGION" \
    --description="LawConnect Backend Docker images" 2>/dev/null || echo "Repositorio ya existe"

# Configurar Docker para usar Artifact Registry
gcloud auth configure-docker "${REGION}-docker.pkg.dev" --quiet

echo -e "${GREEN}✅ Artifact Registry configurado${NC}"
echo ""

# Construir y subir imágenes Docker
echo -e "${BLUE}🔨 Construyendo y subiendo imágenes Docker...${NC}"
echo -e "${YELLOW}⚠️  Esto puede tardar varios minutos...${NC}"
echo ""

cd "$PROJECT_ROOT/microservices"

# Construir IAM Service
echo -e "${BLUE}📦 Construyendo IAM Service...${NC}"
gcloud builds submit --tag "${REGION}-docker.pkg.dev/${PROJECT_ID}/${REPO_NAME}/iam-service:latest" ./iam

# Construir Profiles Service
echo -e "${BLUE}📦 Construyendo Profiles Service...${NC}"
gcloud builds submit --tag "${REGION}-docker.pkg.dev/${PROJECT_ID}/${REPO_NAME}/profiles-service:latest" ./profiles

# Construir Cases Service
echo -e "${BLUE}📦 Construyendo Cases Service...${NC}"
gcloud builds submit --tag "${REGION}-docker.pkg.dev/${PROJECT_ID}/${REPO_NAME}/cases-service:latest" ./cases

# Construir API Gateway
echo -e "${BLUE}📦 Construyendo API Gateway...${NC}"
gcloud builds submit --tag "${REGION}-docker.pkg.dev/${PROJECT_ID}/${REPO_NAME}/api-gateway:latest" ./api-gateway

echo -e "${GREEN}✅ Imágenes construidas y subidas${NC}"
echo ""

echo -e "${YELLOW}⚠️  Nota: Cloud Run requiere configuración adicional para servicios con bases de datos.${NC}"
echo -e "${YELLOW}   Para una solución más simple, usa una VM con Docker.${NC}"
echo ""





