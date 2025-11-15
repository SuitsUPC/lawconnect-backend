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

# Clonar proyecto desde GitHub y configurar servicio de auto-start
echo -e "${BLUE}📥 Clonando proyecto desde GitHub y configurando auto-start...${NC}"
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
    
    # Guardar configuración en archivo persistente
    echo \"GITHUB_REPO=\\\"$GITHUB_REPO\\\"\" | sudo tee /etc/lawconnect-config > /dev/null
    echo \"GITHUB_BRANCH=\\\"$GITHUB_BRANCH\\\"\" | sudo tee -a /etc/lawconnect-config > /dev/null
    echo '✅ Configuración guardada en /etc/lawconnect-config'
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
        
        # Crear script de startup completo
        sudo tee /usr/local/bin/lawconnect-startup.sh > /dev/null <<'EOFSCRIPT'
#!/bin/bash
set -e

# Cargar configuración
if [ -f /etc/lawconnect-config ]; then
    source /etc/lawconnect-config
else
    GITHUB_REPO="https://github.com/SuitsUPC/lawconnect-backend.git"
    GITHUB_BRANCH="feature/deploy-gcp"
fi

LOG_FILE="/var/log/lawconnect-startup.log"
echo \"\$(date): Iniciando LawConnect startup...\" >> \"\$LOG_FILE\"

# Esperar a que Docker esté listo
echo \"\$(date): Esperando Docker...\" >> \"\$LOG_FILE\"
for i in {1..60}; do
    if sudo docker info > /dev/null 2>&1; then
        echo \"\$(date): Docker está listo\" >> \"\$LOG_FILE\"
        break
    fi
    sleep 2
done

# Esperar a que la red esté lista
sleep 5

# Obtener el usuario de la aplicación (no-root)
APP_USER=\$(ls -ld /home 2>/dev/null | awk '{print \$3}' | grep -v root | head -1)
if [ -z \"\$APP_USER\" ]; then
    APP_USER=\$(who 2>/dev/null | awk '{print \$1}' | grep -v root | head -1)
fi
if [ -z \"\$APP_USER\" ]; then
    APP_USER=\"ubuntu\"
fi
echo \"\$(date): Usuario de la aplicación: \$APP_USER\" >> \"\$LOG_FILE\"

# Verificar si el proyecto ya existe en /app (primera vez vs reinicio)
if [ ! -d \"/app/microservices\" ] || [ ! -f \"/app/microservices/docker-compose.yml\" ]; then
    echo \"\$(date): Proyecto no existe en /app, clonando desde GitHub (primera vez)...\" >> \"\$LOG_FILE\"
    # Solo clonar si no existe (primera vez)
    cd /tmp
    rm -rf lawconnect-backend
    git clone -b \"\$GITHUB_BRANCH\" \"\$GITHUB_REPO\" lawconnect-backend 2>&1 >> \"\$LOG_FILE\" || {
        git clone -b main \"\$GITHUB_REPO\" lawconnect-backend 2>&1 >> \"\$LOG_FILE\" || {
            git clone \"\$GITHUB_REPO\" lawconnect-backend 2>&1 >> \"\$LOG_FILE\"
        }
    }
    
    if [ -d lawconnect-backend ]; then
        echo \"\$(date): Copiando proyecto a /app...\" >> \"\$LOG_FILE\"
        sudo cp -r lawconnect-backend/* /app/ 2>/dev/null || true
        sudo cp -r lawconnect-backend/.git /app/ 2>/dev/null || true
        cd /app
        sudo chmod +x start.sh stop.sh logs.sh status.sh mvnw 2>/dev/null || true
        sudo chown -R \$APP_USER:\$APP_USER /app 2>/dev/null || true
        echo \"\$(date): Proyecto copiado a /app\" >> \"\$LOG_FILE\"
    fi
else
    echo \"\$(date): Proyecto ya existe en /app, solo levantando contenedores...\" >> \"\$LOG_FILE\"
fi

# Detectar comando de Docker
DOCKER_CMD=\"docker\"
if ! docker info > /dev/null 2>&1; then
    if sudo docker info > /dev/null 2>&1; then
        DOCKER_CMD=\"sudo docker\"
    fi
fi

# Verificar si los contenedores ya están corriendo
cd /app
CONTAINERS_RUNNING=\$(\$DOCKER_CMD ps --filter \"name=iam-service\|profiles-service\|cases-service\|api-gateway\" --format \"{{.Names}}\" 2>/dev/null | wc -l)

if [ \"\$CONTAINERS_RUNNING\" -gt 0 ]; then
    echo \"\$(date): Contenedores ya están corriendo, no es necesario levantarlos\" >> \"\$LOG_FILE\"
else
    echo \"\$(date): Levantando contenedores Docker existentes (sin rebuild)...\" >> \"\$LOG_FILE\"
    # Solo levantar los contenedores existentes, sin rebuild
    cd /app/microservices
    \$DOCKER_CMD compose up -d >> \"\$LOG_FILE\" 2>&1 || {
        echo \"\$(date): Error al levantar contenedores, intentando con start.sh...\" >> \"\$LOG_FILE\"
        # Si falla, puede ser que no existan las imágenes, entonces sí ejecutar start.sh
        cd /app
        export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
        export PATH=\$PATH:\$JAVA_HOME/bin
        if [ ! -z \"\$APP_USER\" ] && [ \"\$APP_USER\" != \"root\" ]; then
            sudo -u \$APP_USER bash -c \"cd /app && export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 && export PATH=\\\$PATH:\\\$JAVA_HOME/bin && bash start.sh >> /var/log/lawconnect.log 2>&1\" || {
                cd /app
                bash start.sh >> /var/log/lawconnect.log 2>&1
            }
        else
            bash start.sh >> /var/log/lawconnect.log 2>&1
        fi
    }
fi

# Esperar a que los servicios estén listos
echo \"\$(date): Esperando a que los servicios estén listos...\" >> \"\$LOG_FILE\"
sleep 30

# Iniciar Cloudflare Tunnel
echo \"\$(date): Iniciando Cloudflare Tunnel...\" >> \"\$LOG_FILE\"
if command -v cloudflared &> /dev/null; then
    sudo systemctl restart cloudflared || {
        sudo systemctl start cloudflared || true
    }
    echo \"\$(date): Cloudflare Tunnel iniciado\" >> \"\$LOG_FILE\"
    
    # Obtener URL después de unos segundos
    sleep 15
    TUNNEL_URL=\$(sudo journalctl -u cloudflared -n 200 --no-pager 2>/dev/null | grep -o 'https://[a-z0-9-]*\.trycloudflare\.com' | tail -1)
    if [ ! -z \"\$TUNNEL_URL\" ]; then
        echo \"\$TUNNEL_URL\" | sudo tee /tmp/tunnel_url.txt > /dev/null
        echo \"\$(date): URL del tunnel: \$TUNNEL_URL\" >> \"\$LOG_FILE\"
    fi
else
    echo \"\$(date): cloudflared no está instalado\" >> \"\$LOG_FILE\"
fi

echo \"\$(date): Startup completado\" >> \"\$LOG_FILE\"
EOFSCRIPT

        sudo chmod +x /usr/local/bin/lawconnect-startup.sh
        
        # Crear servicio systemd para LawConnect
        sudo tee /etc/systemd/system/lawconnect.service > /dev/null <<'EOFSERVICE'
[Unit]
Description=LawConnect Backend Auto-Start
After=network.target docker.service
Wants=docker.service

[Service]
Type=oneshot
RemainAfterExit=yes
ExecStart=/usr/local/bin/lawconnect-startup.sh
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
EOFSERVICE

        # Crear servicio systemd para Cloudflare Tunnel
        sudo tee /etc/systemd/system/cloudflared.service > /dev/null <<'EOF'
[Unit]
Description=Cloudflare Tunnel
After=network.target docker.service lawconnect.service
Wants=lawconnect.service

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
        sudo systemctl enable lawconnect
        sudo systemctl enable cloudflared
        echo '✅ Servicios systemd configurados para auto-start'
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

echo -e "${GREEN}🔄 AUTO-START CONFIGURADO:${NC}"
echo -e "   ✅ Si apagas la VM y la vuelves a encender, todo se levantará automáticamente"
echo -e "   ✅ Docker, servicios y Cloudflare Tunnel se iniciarán solos"
echo -e "   ✅ El proyecto se actualizará desde GitHub automáticamente"
echo -e "   ✅ La nueva URL del tunnel se guardará en /tmp/tunnel_url.txt"
echo ""

echo -e "${BLUE}🌐 URLs locales (solo para pruebas):${NC}"
echo -e "   • API Gateway: ${GREEN}http://$VM_IP:8080${NC}"
echo -e "   • Swagger UI: ${GREEN}http://$VM_IP:8080/swagger-ui.html${NC}"
echo ""
echo -e "${BLUE}📝 Comandos útiles:${NC}"
echo -e "   • Ver logs del backend: ${YELLOW}gcloud compute ssh $VM_NAME --zone=$ZONE --command='tail -f /var/log/lawconnect.log'${NC}"
echo -e "   • Ver logs del startup: ${YELLOW}gcloud compute ssh $VM_NAME --zone=$ZONE --command='tail -f /var/log/lawconnect-startup.log'${NC}"
echo -e "   • Ver logs del tunnel: ${YELLOW}gcloud compute ssh $VM_NAME --zone=$ZONE --command='sudo journalctl -u cloudflared -f'${NC}"
echo -e "   • Obtener URL del tunnel: ${YELLOW}./get-tunnel-url.sh${NC}"
echo -e "   • Ver estado de servicios: ${YELLOW}gcloud compute ssh $VM_NAME --zone=$ZONE --command='sudo systemctl status lawconnect cloudflared'${NC}"
echo ""

