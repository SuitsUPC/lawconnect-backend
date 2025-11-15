#!/bin/bash

# Script para desplegar LawConnect Backend en GCP
# Este script crea una VM, instala Docker, y ejecuta el proyecto

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
ZONE=$(gcloud config get-value compute/zone 2>/dev/null || echo "southamerica-east1-a")
VM_NAME="lawconnect-backend-vm"
MACHINE_TYPE="e2-standard-4"  # 4 vCPUs, 16GB RAM
IMAGE_FAMILY="ubuntu-2204-lts"
IMAGE_PROJECT="ubuntu-os-cloud"

echo -e "${BLUE}🚀 Desplegando LawConnect Backend en GCP...${NC}"
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
echo -e "   Zona: ${GREEN}$ZONE${NC}"
echo -e "   VM: ${GREEN}$VM_NAME${NC}"
echo ""

# Verificar si la VM ya existe
if gcloud compute instances describe "$VM_NAME" --zone="$ZONE" >/dev/null 2>&1; then
    echo -e "${YELLOW}⚠️  La VM $VM_NAME ya existe${NC}"
    read -p "¿Deseas eliminarla y crear una nueva? (s/n): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Ss]$ ]]; then
        echo -e "${BLUE}🗑️  Eliminando VM existente...${NC}"
        gcloud compute instances delete "$VM_NAME" --zone="$ZONE" --quiet
        sleep 5
    else
        echo -e "${YELLOW}Usando VM existente...${NC}"
    fi
fi

# Crear la VM si no existe
if ! gcloud compute instances describe "$VM_NAME" --zone="$ZONE" >/dev/null 2>&1; then
    echo -e "${BLUE}🖥️  Creando VM en GCP...${NC}"
    gcloud compute instances create "$VM_NAME" \
        --zone="$ZONE" \
        --machine-type="$MACHINE_TYPE" \
        --image-family="$IMAGE_FAMILY" \
        --image-project="$IMAGE_PROJECT" \
        --boot-disk-size=50GB \
        --boot-disk-type=pd-standard \
        --tags=http-server,https-server \
        --metadata=startup-script='#!/bin/bash
# Instalar Docker
apt-get update
apt-get install -y docker.io docker-compose
systemctl start docker
systemctl enable docker
usermod -aG docker $USER

# Instalar Java 17
apt-get install -y openjdk-17-jdk
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH=$PATH:$JAVA_HOME/bin

# Instalar Maven
apt-get install -y maven

# Instalar Git
apt-get install -y git curl
'
    
    if [ $? -ne 0 ]; then
        echo -e "${RED}❌ Error al crear la VM${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}✅ VM creada correctamente${NC}"
    echo -e "${YELLOW}⏳ Esperando a que la VM esté lista (60 segundos)...${NC}"
    sleep 60
fi

# Obtener IP externa de la VM
VM_IP=$(gcloud compute instances describe "$VM_NAME" --zone="$ZONE" --format="get(networkInterfaces[0].accessConfigs[0].natIP)")

if [ -z "$VM_IP" ]; then
    echo -e "${RED}❌ No se pudo obtener la IP de la VM${NC}"
    exit 1
fi

echo -e "${GREEN}✅ VM IP: $VM_IP${NC}"
echo ""

# Configurar firewall para permitir los puertos
echo -e "${BLUE}🔥 Configurando firewall...${NC}"
gcloud compute firewall-rules create lawconnect-ports --allow tcp:8080,tcp:8081,tcp:8082,tcp:8083 --source-ranges 0.0.0.0/0 --description "LawConnect Backend ports" 2>/dev/null || echo "Regla de firewall ya existe"
echo -e "${GREEN}✅ Firewall configurado${NC}"
echo ""

# Esperar a que Docker esté listo en la VM
echo -e "${BLUE}⏳ Esperando a que Docker esté listo en la VM...${NC}"
sleep 30

# Subir el proyecto a la VM
echo -e "${BLUE}📤 Subiendo proyecto a la VM...${NC}"
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Crear un tarball del proyecto (excluyendo node_modules, target, etc.)
cd "$PROJECT_ROOT/.."
tar -czf /tmp/lawconnect-backend.tar.gz \
    --exclude='lawconnect-backend/target' \
    --exclude='lawconnect-backend/.git' \
    --exclude='lawconnect-backend/.idea' \
    --exclude='lawconnect-backend/.vscode' \
    --exclude='lawconnect-backend/*.iml' \
    --exclude='lawconnect-backend/.DS_Store' \
    lawconnect-backend/

# Copiar el proyecto a la VM
gcloud compute scp /tmp/lawconnect-backend.tar.gz "$VM_NAME:/tmp/" --zone="$ZONE"

# Extraer y ejecutar en la VM
echo -e "${BLUE}📦 Extrayendo proyecto en la VM...${NC}"
gcloud compute ssh "$VM_NAME" --zone="$ZONE" --command="
    cd /tmp
    rm -rf lawconnect-backend
    tar -xzf lawconnect-backend.tar.gz
    cd lawconnect-backend
    chmod +x start.sh stop.sh logs.sh status.sh mvnw
    echo '✅ Proyecto extraído correctamente'
"

# Ejecutar start.sh en la VM
echo -e "${BLUE}🚀 Ejecutando start.sh en la VM...${NC}"
echo -e "${YELLOW}⚠️  Esto puede tardar varios minutos (compilación y construcción de imágenes Docker)...${NC}"
echo ""

gcloud compute ssh "$VM_NAME" --zone="$ZONE" --command="
    cd /tmp/lawconnect-backend
    nohup bash start.sh > /tmp/lawconnect-startup.log 2>&1 &
    echo '✅ start.sh ejecutado en segundo plano'
    echo '📋 Ver logs con: tail -f /tmp/lawconnect-startup.log'
"

echo ""
echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${GREEN}✅ Despliegue iniciado!${NC}"
echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
echo -e "${BLUE}📋 Información de la VM:${NC}"
echo -e "   Nombre: ${GREEN}$VM_NAME${NC}"
echo -e "   IP Externa: ${GREEN}$VM_IP${NC}"
echo -e "   Zona: ${GREEN}$ZONE${NC}"
echo ""
echo -e "${BLUE}🌐 URLs disponibles (en unos minutos):${NC}"
echo -e "   • API Gateway: ${GREEN}http://$VM_IP:8080${NC}"
echo -e "   • Swagger UI: ${GREEN}http://$VM_IP:8080/swagger-ui.html${NC}"
echo ""
echo -e "${BLUE}📝 Comandos útiles:${NC}"
echo -e "   • Ver logs en la VM: ${YELLOW}gcloud compute ssh $VM_NAME --zone=$ZONE --command='tail -f /tmp/lawconnect-startup.log'${NC}"
echo -e "   • Conectar a la VM: ${YELLOW}gcloud compute ssh $VM_NAME --zone=$ZONE${NC}"
echo -e "   • Ver estado de la VM: ${YELLOW}gcloud compute instances describe $VM_NAME --zone=$ZONE${NC}"
echo -e "   • Detener la VM: ${YELLOW}gcloud compute instances stop $VM_NAME --zone=$ZONE${NC}"
echo -e "   • Eliminar la VM: ${YELLOW}gcloud compute instances delete $VM_NAME --zone=$ZONE${NC}"
echo ""
echo -e "${YELLOW}⏳ Espera 5-10 minutos para que los servicios estén completamente listos...${NC}"
echo ""





