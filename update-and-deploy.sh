#!/bin/bash

# Script para actualizar el código en la VM y desplegar
# Uso: ./update-and-deploy.sh

set +e

# Colores
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m'

# Configuración
ZONE=$(gcloud config get-value compute/zone 2>/dev/null || echo "southamerica-east1-a")
VM_NAME="lawconnect-vm"

echo -e "${BLUE}🔄 Actualizando y desplegando LawConnect Backend...${NC}"
echo ""

# Verificar que la VM existe
if ! gcloud compute instances describe "$VM_NAME" --zone="$ZONE" >/dev/null 2>&1; then
    echo -e "${RED}❌ La VM no existe. Ejecuta primero: ./deploy-gcp-vm-simple.sh${NC}"
    exit 1
fi

# Verificar estado de la VM
VM_STATUS=$(gcloud compute instances describe "$VM_NAME" --zone="$ZONE" --format="get(status)")

if [ "$VM_STATUS" != "RUNNING" ]; then
    echo -e "${BLUE}🔄 La VM está detenida. Iniciándola...${NC}"
    gcloud compute instances start "$VM_NAME" --zone="$ZONE"
    echo -e "${YELLOW}⏳ Esperando a que la VM esté lista (30 segundos)...${NC}"
    sleep 30
fi

# Obtener IP de la VM
VM_IP=$(gcloud compute instances describe "$VM_NAME" --zone="$ZONE" --format="get(networkInterfaces[0].accessConfigs[0].natIP)")

if [ -z "$VM_IP" ]; then
    echo -e "${RED}❌ No se pudo obtener la IP de la VM${NC}"
    exit 1
fi

echo -e "${GREEN}✅ VM IP: $VM_IP${NC}"
echo ""

# Detener servicios actuales
echo -e "${BLUE}🛑 Deteniendo servicios actuales...${NC}"
gcloud compute ssh "$VM_NAME" --zone="$ZONE" --command="
    cd /app 2>/dev/null || exit 0
    if [ -f stop.sh ]; then
        bash stop.sh > /dev/null 2>&1 || true
    fi
    sudo docker compose -f microservices/docker-compose.yml down > /dev/null 2>&1 || true
    echo '✅ Servicios detenidos'
" 2>/dev/null || echo "⚠️  No se pudieron detener servicios (puede ser normal si no estaban corriendo)"

echo ""

# Actualizar código desde GitHub
echo -e "${BLUE}📥 Actualizando código desde GitHub...${NC}"
gcloud compute ssh "$VM_NAME" --zone="$ZONE" --command="
    cd /app 2>/dev/null || {
        echo '❌ Directorio /app no existe. Ejecuta primero: ./deploy-gcp-vm-simple.sh'
        exit 1
    }
    
    # Hacer pull del repositorio
    echo '📥 Haciendo git pull...'
    git pull origin feature/deploy-gcp 2>&1 || {
        echo '⚠️  Error al hacer pull, intentando con main...'
        git pull origin main 2>&1 || {
            echo '⚠️  Error al hacer pull de main, intentando cualquier rama...'
            git pull 2>&1
        }
    }
    
    # Asegurar permisos
    sudo chmod +x start.sh stop.sh logs.sh status.sh mvnw test-user-profile.sh 2>/dev/null || true
    sudo chown -R \$USER:\$USER /app 2>/dev/null || true
    
    echo '✅ Código actualizado'
" || {
    echo -e "${RED}❌ Error al actualizar código${NC}"
    exit 1
}

echo ""

# Ejecutar start.sh
echo -e "${BLUE}🚀 Ejecutando start.sh en la VM...${NC}"
echo -e "${YELLOW}⚠️  Esto puede tardar varios minutos (compilación y construcción de imágenes Docker)...${NC}"
echo ""

gcloud compute ssh "$VM_NAME" --zone="$ZONE" --command="
    cd /app
    
    # Configurar Java
    export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
    export PATH=\$PATH:\$JAVA_HOME/bin
    
    # Ejecutar start.sh en background
    nohup bash start.sh > /tmp/lawconnect-startup.log 2>&1 &
    START_PID=\$!
    echo \"✅ start.sh iniciado (PID: \$START_PID)\"
    echo \"📋 Ver logs con: tail -f /tmp/lawconnect-startup.log\"
" || {
    echo -e "${RED}❌ Error al ejecutar start.sh${NC}"
    exit 1
}

echo ""
echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${GREEN}✅ Despliegue iniciado!${NC}"
echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
echo -e "${BLUE}📋 Comandos útiles:${NC}"
echo -e "   Ver logs en tiempo real:"
echo -e "   ${YELLOW}gcloud compute ssh $VM_NAME --zone=$ZONE --command='tail -f /tmp/lawconnect-startup.log'${NC}"
echo ""
echo -e "   Ver logs de servicios:"
echo -e "   ${YELLOW}gcloud compute ssh $VM_NAME --zone=$ZONE --command='cd /app && bash logs.sh'${NC}"
echo ""
echo -e "   Ver estado de servicios:"
echo -e "   ${YELLOW}gcloud compute ssh $VM_NAME --zone=$ZONE --command='cd /app && bash status.sh'${NC}"
echo ""
echo -e "${BLUE}🌐 Una vez que los servicios estén listos, obtén la URL de Cloudflare Tunnel:${NC}"
echo -e "   ${YELLOW}gcloud compute ssh $VM_NAME --zone=$ZONE --command='cd /app && bash get-tunnel-url.sh'${NC}"
echo ""

