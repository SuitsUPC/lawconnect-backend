#!/bin/bash

# Script SIMPLE para desplegar en GCP usando una VM con Docker preinstalado
# Ejecuta tu start.sh directamente

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
VM_NAME="lawconnect-vm"
MACHINE_TYPE="e2-medium"  # 2 vCPUs, 4GB RAM (suficiente para los servicios)

echo -e "${BLUE}🚀 Desplegando LawConnect Backend en GCP (VM con Docker)...${NC}"
echo ""

# Verificar proyecto
if [ -z "$PROJECT_ID" ]; then
    echo -e "${RED}❌ Error: No hay proyecto de GCP configurado${NC}"
    exit 1
fi

echo -e "${BLUE}📋 Configuración:${NC}"
echo -e "   Proyecto: ${GREEN}$PROJECT_ID${NC}"
echo -e "   Zona: ${GREEN}$ZONE${NC}"
echo -e "   VM: ${GREEN}$VM_NAME${NC}"
echo ""

# Verificar si la VM existe
if gcloud compute instances describe "$VM_NAME" --zone="$ZONE" >/dev/null 2>&1; then
    echo -e "${YELLOW}⚠️  La VM ya existe. Eliminándola...${NC}"
    gcloud compute instances delete "$VM_NAME" --zone="$ZONE" --quiet
    sleep 5
fi

# Crear script de startup
STARTUP_SCRIPT="/tmp/lawconnect-startup.sh"
cat > "$STARTUP_SCRIPT" << 'EOF'
#!/bin/bash
set -e

# Instalar Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sh get-docker.sh
systemctl start docker
systemctl enable docker
usermod -aG docker $USER

# Instalar Java 17
apt-get update
apt-get install -y openjdk-17-jdk maven git curl
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH=$PATH:$JAVA_HOME/bin
echo "export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64" >> /etc/profile
echo "export PATH=\$PATH:\$JAVA_HOME/bin" >> /etc/profile

# Instalar Docker Compose
curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose

# Crear directorio para el proyecto
mkdir -p /app
EOF

# Crear VM con Ubuntu (tiene apt-get)
echo -e "${BLUE}🖥️  Creando VM con Ubuntu y Docker...${NC}"
gcloud compute instances create "$VM_NAME" \
    --zone="$ZONE" \
    --machine-type="$MACHINE_TYPE" \
    --image-family=ubuntu-2204-lts \
    --image-project=ubuntu-os-cloud \
    --boot-disk-size=50GB \
    --boot-disk-type=pd-standard \
    --tags=http-server \
    --metadata-from-file=startup-script="$STARTUP_SCRIPT"

if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Error al crear la VM${NC}"
    exit 1
fi

echo -e "${GREEN}✅ VM creada${NC}"
echo -e "${YELLOW}⏳ Esperando a que la VM esté lista (60 segundos)...${NC}"
sleep 60

# Obtener IP
VM_IP=$(gcloud compute instances describe "$VM_NAME" --zone="$ZONE" --format="get(networkInterfaces[0].accessConfigs[0].natIP)")

if [ -z "$VM_IP" ]; then
    echo -e "${RED}❌ No se pudo obtener la IP${NC}"
    exit 1
fi

echo -e "${GREEN}✅ VM IP: $VM_IP${NC}"

# Configurar firewall
echo -e "${BLUE}🔥 Configurando firewall...${NC}"
gcloud compute firewall-rules create lawconnect-ports \
    --allow tcp:8080,tcp:8081,tcp:8082,tcp:8083 \
    --source-ranges 0.0.0.0/0 \
    --description "LawConnect Backend ports" 2>/dev/null || echo "Firewall ya configurado"

# Subir proyecto
echo -e "${BLUE}📤 Subiendo proyecto a la VM...${NC}"
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

cd "$PROJECT_ROOT/.."
tar -czf /tmp/lawconnect-backend.tar.gz \
    --exclude='lawconnect-backend/target' \
    --exclude='lawconnect-backend/.git' \
    --exclude='lawconnect-backend/.idea' \
    --exclude='lawconnect-backend/.vscode' \
    --exclude='lawconnect-backend/*.iml' \
    --exclude='lawconnect-backend/.DS_Store' \
    --exclude='lawconnect-backend/node_modules' \
    lawconnect-backend/

gcloud compute scp /tmp/lawconnect-backend.tar.gz "$VM_NAME:/tmp/" --zone="$ZONE"

# Extraer y ejecutar
echo -e "${BLUE}📦 Preparando proyecto en la VM...${NC}"
gcloud compute ssh "$VM_NAME" --zone="$ZONE" --command="
    sudo mkdir -p /app
    cd /tmp
    sudo tar -xzf lawconnect-backend.tar.gz -C /app --strip-components=1
    cd /app
    sudo chmod +x start.sh stop.sh logs.sh status.sh mvnw
    sudo chown -R \$USER:\$USER /app
    echo '✅ Proyecto listo'
"

# Ejecutar start.sh
echo -e "${BLUE}🚀 Ejecutando start.sh en la VM...${NC}"
echo -e "${YELLOW}⚠️  Esto tardará varios minutos (compilación + Docker build)...${NC}"

gcloud compute ssh "$VM_NAME" --zone="$ZONE" --command="
    cd /app
    export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
    export PATH=\$PATH:\$JAVA_HOME/bin
    nohup bash start.sh > /tmp/lawconnect.log 2>&1 &
    echo '✅ start.sh ejecutado en segundo plano'
    sleep 10
    echo ''
    echo '📋 Últimas líneas del log:'
    tail -30 /tmp/lawconnect.log || echo 'Log aún no disponible, espera unos segundos...'
"

echo ""
echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${GREEN}✅ Despliegue completado!${NC}"
echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
echo -e "${BLUE}🌐 URLs (espera 5-10 minutos para que estén listas):${NC}"
echo -e "   • API Gateway: ${GREEN}http://$VM_IP:8080${NC}"
echo -e "   • Swagger UI: ${GREEN}http://$VM_IP:8080/swagger-ui.html${NC}"
echo ""
echo -e "${BLUE}📝 Ver logs:${NC}"
echo -e "   ${YELLOW}gcloud compute ssh $VM_NAME --zone=$ZONE --command='tail -f /tmp/lawconnect.log'${NC}"
echo ""

