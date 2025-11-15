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

# Agregar usuario actual al grupo docker (funciona para el usuario que ejecuta el script)
USERNAME=$(whoami)
if [ "$USERNAME" != "root" ]; then
    usermod -aG docker $USERNAME
    # Aplicar cambios de grupo sin necesidad de cerrar sesión
    newgrp docker << EONG || true
echo "Usuario agregado al grupo docker"
EONG
fi

# Instalar Java 17, Git y otras herramientas
apt-get update
apt-get install -y openjdk-17-jdk maven git curl wget
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

# Clonar proyecto desde GitHub
echo -e "${BLUE}📥 Clonando proyecto desde GitHub...${NC}"
GITHUB_REPO="https://github.com/SuitsUPC/lawconnect-backend.git"
GITHUB_BRANCH="feature/deploy-gcp"

gcloud compute ssh "$VM_NAME" --zone="$ZONE" --command="
    # Asegurar que Git esté instalado
    if ! command -v git &> /dev/null; then
        echo '📥 Instalando Git...'
        sudo apt-get update
        sudo apt-get install -y git
    fi
    
    sudo mkdir -p /app
    cd /tmp
    rm -rf lawconnect-backend
    echo '📥 Clonando repositorio desde GitHub...'
    REPO_URL=\"$GITHUB_REPO\"
    BRANCH=\"$GITHUB_BRANCH\"
    git clone -b \"\$BRANCH\" \"\$REPO_URL\" lawconnect-backend 2>&1 || {
        echo '⚠️  Error al clonar rama \$BRANCH, intentando con rama main...'
        git clone -b main \"\$REPO_URL\" lawconnect-backend 2>&1 || {
            echo '⚠️  Intentando sin especificar rama (default)...'
            git clone \"\$REPO_URL\" lawconnect-backend 2>&1
        }
    }
    
    if [ -d lawconnect-backend ]; then
        echo '✅ Repositorio clonado correctamente'
        sudo cp -r lawconnect-backend/* /app/ 2>/dev/null || true
        sudo cp -r lawconnect-backend/.git /app/ 2>/dev/null || true
        cd /app
        sudo chmod +x start.sh stop.sh logs.sh status.sh mvnw 2>/dev/null || true
        sudo chown -R \$USER:\$USER /app
        echo '✅ Proyecto listo en /app'
    else
        echo '❌ Error: No se pudo clonar el repositorio'
        exit 1
    fi
"

# Esperar a que Docker esté completamente listo y configurar permisos
echo -e "${BLUE}⏳ Esperando a que Docker esté completamente listo...${NC}"
gcloud compute ssh "$VM_NAME" --zone="$ZONE" --command="
    echo 'Esperando a que Docker esté listo...'
    for i in {1..30}; do
        if sudo docker info > /dev/null 2>&1; then
            echo '✅ Docker está listo!'
            break
        fi
        echo \"Intento \$i/30...\"
        sleep 2
    done
    
    # Asegurar que el usuario tenga acceso a Docker
    USERNAME=\$(whoami)
    if [ \"\$USERNAME\" != \"root\" ]; then
        sudo usermod -aG docker \$USERNAME
    fi
    
    # Verificar que Docker funciona con sudo (start.sh puede necesitarlo)
    if sudo docker info > /dev/null 2>&1; then
        echo '✅ Docker verificado y listo'
    else
        echo '⚠️  Docker aún no está listo, pero continuando...'
    fi
"

# Ejecutar start.sh (ahora detecta automáticamente si necesita sudo)
echo -e "${BLUE}🚀 Ejecutando start.sh en la VM...${NC}"
echo -e "${YELLOW}⚠️  Esto tardará varios minutos (compilación + Docker build)...${NC}"

gcloud compute ssh "$VM_NAME" --zone="$ZONE" --command="
    cd /app
    export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
    export PATH=\$PATH:\$JAVA_HOME/bin
    
    # Ejecutar start.sh en background (ahora detecta automáticamente si necesita sudo)
    bash start.sh > /tmp/lawconnect.log 2>&1 &
    START_PID=\$!
    echo \"✅ start.sh ejecutado en segundo plano (PID: \$START_PID)\"
    sleep 20
    echo ''
    echo '📋 Últimas líneas del log:'
    tail -50 /tmp/lawconnect.log || echo 'Log aún no disponible, espera unos segundos...'
    echo ''
    echo '📊 Verificando procesos:'
    ps aux | grep -E '(start.sh|mvn|docker)' | grep -v grep | head -5 || echo 'Procesos aún iniciando...'
"

# Instalar y configurar Cloudflare Tunnel
echo ""
echo -e "${BLUE}🌐 Configurando Cloudflare Tunnel...${NC}"
sleep 10  # Esperar un poco antes de intentar SSH de nuevo

# Intentar con reintentos si falla
CLOUDFLARE_CONFIGURED=false
for attempt in {1..3}; do
    if gcloud compute ssh "$VM_NAME" --zone="$ZONE" --command="
        # Instalar cloudflared si no está instalado
        if ! command -v cloudflared &> /dev/null; then
            echo '📥 Instalando cloudflared...'
            curl -L https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-linux-amd64 -o /tmp/cloudflared
            sudo mv /tmp/cloudflared /usr/local/bin/cloudflared
            sudo chmod +x /usr/local/bin/cloudflared
            echo '✅ cloudflared instalado'
        else
            echo '✅ cloudflared ya está instalado'
        fi
        
        # Detener procesos anteriores
        pkill -f cloudflared || true
        sudo systemctl stop cloudflared 2>/dev/null || true
        
        # Crear servicio systemd para Cloudflare Tunnel
        sudo tee /etc/systemd/system/cloudflared.service > /dev/null <<'EOF'
[Unit]
Description=Cloudflare Tunnel
After=network.target docker.service

[Service]
Type=simple
User=root
ExecStart=/usr/local/bin/cloudflared tunnel --url http://localhost:8080
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
EOF

        sudo systemctl daemon-reload
        sudo systemctl enable cloudflared
        sudo systemctl start cloudflared
        echo '✅ Cloudflare Tunnel iniciado'
        sleep 8
    " 2>/dev/null; then
        CLOUDFLARE_CONFIGURED=true
        break
    else
        echo -e "${YELLOW}⚠️  Intento $attempt/3 falló, reintentando en 5 segundos...${NC}"
        sleep 5
    fi
done

if [ "$CLOUDFLARE_CONFIGURED" = "false" ]; then
    echo -e "${YELLOW}⚠️  No se pudo configurar Cloudflare Tunnel automáticamente.${NC}"
    echo -e "${YELLOW}   Puedes configurarlo manualmente después con: ./setup-cloudflare-tunnel-simple.sh${NC}"
fi

# Obtener URL del tunnel
if [ "$CLOUDFLARE_CONFIGURED" = "true" ]; then
    echo ""
    echo -e "${BLUE}🔍 Obteniendo URL del Cloudflare Tunnel...${NC}"
    sleep 15  # Esperar más tiempo para que el tunnel se establezca
    
    # Intentar obtener la URL con reintentos
    TUNNEL_URL=""
    for attempt in {1..3}; do
        TUNNEL_URL=$(gcloud compute ssh "$VM_NAME" --zone="$ZONE" --command="
            TUNNEL_URL=\$(sudo journalctl -u cloudflared -n 200 --no-pager 2>/dev/null | grep -o 'https://[a-z0-9-]*\.trycloudflare\.com' | tail -1)
            if [ ! -z \"\$TUNNEL_URL\" ]; then
                echo \"\$TUNNEL_URL\"
                echo \"\$TUNNEL_URL\" > /tmp/tunnel_url.txt
            else
                # Intentar desde el log directo si existe
                if [ -f /tmp/cloudflared.log ]; then
                    TUNNEL_URL=\$(grep -o 'https://[a-z0-9-]*\.trycloudflare\.com' /tmp/cloudflared.log 2>/dev/null | head -1)
                    if [ ! -z \"\$TUNNEL_URL\" ]; then
                        echo \"\$TUNNEL_URL\"
                        echo \"\$TUNNEL_URL\" > /tmp/tunnel_url.txt
                    fi
                fi
            fi
        " 2>/dev/null | grep -o 'https://[a-z0-9-]*\.trycloudflare\.com' | head -1)
        
        if [ ! -z "$TUNNEL_URL" ]; then
            break
        fi
        if [ $attempt -lt 3 ]; then
            echo -e "${YELLOW}   Intento $attempt/3: URL aún no disponible, esperando 10 segundos más...${NC}"
            sleep 10
        fi
    done
else
    TUNNEL_URL=""
fi

echo ""
echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${GREEN}✅ Despliegue completado!${NC}"
echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""

if [ ! -z "$TUNNEL_URL" ]; then
    echo -e "${GREEN}🌐 URL HTTPS del Backend (para tu frontend en Vercel):${NC}"
    echo -e "   ${GREEN}$TUNNEL_URL${NC}"
    echo ""
    echo -e "${BLUE}📝 Actualiza tu variable de entorno en Vercel:${NC}"
    echo -e "   ${YELLOW}NEXT_PUBLIC_API_URL=$TUNNEL_URL${NC}"
    echo ""
else
    echo -e "${YELLOW}⚠️  URL del tunnel aún no disponible. Espera 30 segundos y ejecuta:${NC}"
    echo -e "   ${YELLOW}./get-tunnel-url.sh${NC}"
    echo ""
fi

echo -e "${BLUE}🌐 URLs locales (solo para pruebas):${NC}"
echo -e "   • API Gateway: ${GREEN}http://$VM_IP:8080${NC}"
echo -e "   • Swagger UI: ${GREEN}http://$VM_IP:8080/swagger-ui.html${NC}"
echo ""
echo -e "${BLUE}📝 Comandos útiles:${NC}"
echo -e "   • Ver logs del backend: ${YELLOW}gcloud compute ssh $VM_NAME --zone=$ZONE --command='tail -f /tmp/lawconnect.log'${NC}"
echo -e "   • Ver logs del tunnel: ${YELLOW}gcloud compute ssh $VM_NAME --zone=$ZONE --command='sudo journalctl -u cloudflared -f'${NC}"
echo -e "   • Obtener URL del tunnel: ${YELLOW}./get-tunnel-url.sh${NC}"
echo ""

