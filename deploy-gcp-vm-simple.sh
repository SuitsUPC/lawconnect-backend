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
VM_EXISTS=false
if gcloud compute instances describe "$VM_NAME" --zone="$ZONE" >/dev/null 2>&1; then
    VM_EXISTS=true
    echo -e "${BLUE}ℹ️  La VM ya existe. Limpiando disco y re-desplegando...${NC}"
    
    # Verificar estado de la VM
    VM_STATUS=$(gcloud compute instances describe "$VM_NAME" --zone="$ZONE" --format="get(status)")
    
    if [ "$VM_STATUS" != "RUNNING" ]; then
        echo -e "${BLUE}🔄 La VM está detenida. Iniciándola...${NC}"
        gcloud compute instances start "$VM_NAME" --zone="$ZONE"
        echo -e "${YELLOW}⏳ Esperando a que la VM esté lista (30 segundos)...${NC}"
        sleep 30
    fi
    
    # Obtener IP de la VM existente
    VM_IP=$(gcloud compute instances describe "$VM_NAME" --zone="$ZONE" --format="get(networkInterfaces[0].accessConfigs[0].natIP)")
    
    # Detener servicios y limpiar /app
    echo -e "${BLUE}🧹 Limpiando proyecto anterior...${NC}"
    gcloud compute ssh "$VM_NAME" --zone="$ZONE" --command="
        # Detener servicios
        cd /app 2>/dev/null || true
        if [ -f stop.sh ]; then
            bash stop.sh > /dev/null 2>&1 || true
        fi
        sudo docker compose -f microservices/docker-compose.yml down > /dev/null 2>&1 || true
        sudo docker stop \$(sudo docker ps -aq) > /dev/null 2>&1 || true
        
        # Limpiar /app (solo archivos del proyecto, no configuraciones del sistema)
        sudo rm -rf /app/* /app/.* 2>/dev/null || true
        sudo mkdir -p /app
        echo '✅ Disco limpiado'
    " 2>/dev/null || echo "⚠️  No se pudo limpiar, continuando..."
else
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

# Ejecutar start.sh y monitorear progreso
echo -e "${BLUE}🚀 Ejecutando start.sh en la VM...${NC}"
echo -e "${YELLOW}⚠️  Esto tardará varios minutos (compilación + Docker build)...${NC}"
echo ""

# Ejecutar start.sh en background y monitorear logs
gcloud compute ssh "$VM_NAME" --zone="$ZONE" --command="
    cd /app
    export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
    export PATH=\$PATH:\$JAVA_HOME/bin
    
    # Limpiar log anterior
    > /tmp/lawconnect.log
    
    # Ejecutar start.sh en background
    nohup bash start.sh > /tmp/lawconnect.log 2>&1 &
    START_PID=\$!
    echo \"START_PID=\$START_PID\" > /tmp/start_pid.txt
    echo \"✅ start.sh iniciado (PID: \$START_PID)\"
" 2>/dev/null

# Monitorear logs en tiempo real
echo -e "${BLUE}📋 Monitoreando logs de start.sh (presiona Ctrl+C para detener el monitoreo, el proceso continuará)...${NC}"
echo ""

# Función para mostrar logs
show_logs() {
    gcloud compute ssh "$VM_NAME" --zone="$ZONE" --command="
        if [ -f /tmp/lawconnect.log ]; then
            # Mostrar últimas 30 líneas, pero si hay errores, mostrar más contexto
            tail -50 /tmp/lawconnect.log 2>/dev/null | tail -30
        else
            echo 'Log aún no disponible...'
        fi
    " 2>/dev/null
}

# Función para verificar si hay errores en los logs
check_for_errors() {
    gcloud compute ssh "$VM_NAME" --zone="$ZONE" --command="
        if [ -f /tmp/lawconnect.log ]; then
            # Buscar errores comunes
            if grep -iE '(error|failed|exception|not found)' /tmp/lawconnect.log | tail -5; then
                echo 'ERRORS_FOUND'
            else
                echo 'NO_ERRORS'
            fi
        else
            echo 'NO_LOG'
        fi
    " 2>/dev/null | grep -q "ERRORS_FOUND"
}

# Mostrar logs cada 15 segundos durante 5 minutos o hasta que termine
LOG_MONITOR_TIME=300  # 5 minutos
LOG_ELAPSED=0
LOG_INTERVAL=15

while [ $LOG_ELAPSED -lt $LOG_MONITOR_TIME ]; do
    # Verificar si start.sh terminó
    IS_RUNNING=$(gcloud compute ssh "$VM_NAME" --zone="$ZONE" --command="
        if [ -f /tmp/start_pid.txt ]; then
            source /tmp/start_pid.txt
            if ps -p \$START_PID > /dev/null 2>&1; then
                echo 'RUNNING'
            else
                echo 'FINISHED'
            fi
        else
            echo 'UNKNOWN'
        fi
    " 2>/dev/null | grep -o "FINISHED")
    
    if [ "$IS_RUNNING" = "FINISHED" ]; then
        echo -e "${GREEN}✅ start.sh terminó${NC}"
        echo ""
        show_logs
        break
    fi
    
    # Mostrar logs cada intervalo
    show_logs
    echo ""
    
    # Verificar si hay errores
    if check_for_errors; then
        echo -e "${RED}⚠️  Se detectaron errores en los logs. Mostrando más detalles...${NC}"
        gcloud compute ssh "$VM_NAME" --zone="$ZONE" --command="
            echo '📋 Últimas líneas con errores:'
            grep -iE '(error|failed|exception|not found)' /tmp/lawconnect.log | tail -10
        " 2>/dev/null
        echo ""
    fi
    
    echo -e "${YELLOW}⏳ Esperando... (${LOG_ELAPSED}s / ${LOG_MONITOR_TIME}s) - start.sh aún ejecutándose...${NC}"
    echo ""
    
    sleep $LOG_INTERVAL
    LOG_ELAPSED=$((LOG_ELAPSED + LOG_INTERVAL))
done

# Mostrar logs finales y verificar compilación
echo ""
echo -e "${BLUE}📋 Logs finales de start.sh:${NC}"
show_logs
echo ""

# Verificar que los JARs se compilaron correctamente
echo -e "${BLUE}🔍 Verificando que los JARs se compilaron correctamente...${NC}"
JARS_MISSING=false
JAR_CHECK=$(gcloud compute ssh "$VM_NAME" --zone="$ZONE" --command="
    cd /app
    MISSING=0
    echo 'Verificando JARs compilados:'
    for service in iam profiles cases api-gateway; do
        JAR_PATH=\"microservices/\${service}/target/\${service}-service-0.0.1-SNAPSHOT.jar\"
        if [ -f \"\$JAR_PATH\" ]; then
            echo \"  ✅ \${service}: \$(du -h \$JAR_PATH | cut -f1)\"
        else
            echo \"  ❌ \${service}: NO ENCONTRADO\"
            MISSING=1
        fi
    done
    if [ \$MISSING -eq 1 ]; then
        echo 'JARS_MISSING'
    else
        echo 'JARS_OK'
    fi
" 2>/dev/null)

echo "$JAR_CHECK"

if echo "$JAR_CHECK" | grep -q "JARS_MISSING"; then
    echo ""
    echo -e "${RED}❌ ERROR: Algunos JARs no se compilaron correctamente.${NC}"
    echo -e "${YELLOW}📋 Revisando logs de compilación...${NC}"
    gcloud compute ssh "$VM_NAME" --zone="$ZONE" --command="
        echo '📋 Buscando errores de compilación en el log:'
        grep -iE '(BUILD FAILURE|COMPILATION ERROR|mvn.*failed)' /tmp/lawconnect.log | tail -10 || echo 'No se encontraron errores específicos de Maven'
        echo ''
        echo '📋 Últimas líneas del log relacionadas con compilación:'
        grep -A 5 -B 5 -iE '(compilando|building|mvn)' /tmp/lawconnect.log | tail -20
    " 2>/dev/null
    echo ""
    echo -e "${YELLOW}💡 Solución:${NC}"
    echo -e "   1. Revisa los logs de compilación arriba"
    echo -e "   2. Verifica que Java y Maven estén instalados correctamente"
    echo -e "   3. Intenta ejecutar start.sh manualmente en la VM para ver más detalles"
    echo ""
    JARS_MISSING=true
fi
echo ""

# Si faltan JARs, no continuar con la verificación de servicios
if [ "$JARS_MISSING" = "true" ]; then
    echo -e "${RED}❌ No se puede continuar sin los JARs compilados.${NC}"
    echo -e "${YELLOW}   Por favor, corrige los errores de compilación y vuelve a intentar.${NC}"
    exit 1
fi

# Verificar si start.sh terminó correctamente y esperar a que los servicios estén listos
echo ""
echo -e "${BLUE}⏳ Esperando a que los servicios estén listos...${NC}"
echo ""

# Función para verificar si los servicios están listos
check_services_ready() {
    gcloud compute ssh "$VM_NAME" --zone="$ZONE" --command="
        cd /app
        export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
        export PATH=\$PATH:\$JAVA_HOME/bin
        
        # Detectar comando de Docker
        DOCKER_CMD=\"docker\"
        if ! docker info > /dev/null 2>&1; then
            if sudo docker info > /dev/null 2>&1; then
                DOCKER_CMD=\"sudo docker\"
            fi
        fi
        
        # Verificar que los contenedores estén corriendo
        CONTAINERS=\$(\$DOCKER_CMD ps --filter \"name=iam-service\|profiles-service\|cases-service\|api-gateway\" --format \"{{.Names}}\" 2>/dev/null | wc -l)
        
        if [ \"\$CONTAINERS\" -ge 4 ]; then
            # Verificar que el API Gateway responda
            sleep 5
            HTTP_CODE=\$(curl -s -o /dev/null -w \"%{http_code}\" http://localhost:8080/actuator/health 2>/dev/null || echo \"000\")
            if [ \"\$HTTP_CODE\" = \"200\" ] || [ \"\$HTTP_CODE\" = \"404\" ]; then
                echo \"READY\"
                exit 0
            fi
        fi
        echo \"NOT_READY\"
        exit 1
    " 2>/dev/null | grep -q "READY"
}

# Esperar hasta 10 minutos para que los servicios estén listos
MAX_WAIT=600  # 10 minutos
ELAPSED=0
INTERVAL=10

while [ $ELAPSED -lt $MAX_WAIT ]; do
    if check_services_ready; then
        echo -e "${GREEN}✅ Servicios listos!${NC}"
        break
    fi
    
    # Mostrar progreso cada 30 segundos
    if [ $((ELAPSED % 30)) -eq 0 ]; then
        echo -e "${YELLOW}⏳ Esperando servicios... (${ELAPSED}s / ${MAX_WAIT}s)${NC}"
        # Mostrar estado de contenedores
        gcloud compute ssh "$VM_NAME" --zone="$ZONE" --command="
            cd /app
            DOCKER_CMD=\"docker\"
            if ! docker info > /dev/null 2>&1; then
                if sudo docker info > /dev/null 2>&1; then
                    DOCKER_CMD=\"sudo docker\"
                fi
            fi
            echo '📊 Estado de contenedores:'
            \$DOCKER_CMD ps --filter \"name=iam-service\|profiles-service\|cases-service\|api-gateway\" --format \"table {{.Names}}\\t{{.Status}}\" 2>/dev/null || echo 'Docker no disponible'
        " 2>/dev/null | tail -5
    fi
    
    sleep $INTERVAL
    ELAPSED=$((ELAPSED + INTERVAL))
done

if [ $ELAPSED -ge $MAX_WAIT ]; then
    echo -e "${YELLOW}⚠️  Tiempo de espera agotado. Los servicios pueden estar aún iniciando.${NC}"
fi

# Mostrar logs finales
echo ""
echo -e "${BLUE}📋 Últimas líneas del log de start.sh:${NC}"
gcloud compute ssh "$VM_NAME" --zone="$ZONE" --command="tail -30 /tmp/lawconnect.log 2>/dev/null || echo 'Log no disponible'" 2>/dev/null
echo ""

# Mostrar estado final de servicios
echo -e "${BLUE}📊 Estado final de servicios:${NC}"
gcloud compute ssh "$VM_NAME" --zone="$ZONE" --command="
    cd /app
    DOCKER_CMD=\"docker\"
    if ! docker info > /dev/null 2>&1; then
        if sudo docker info > /dev/null 2>&1; then
            DOCKER_CMD=\"sudo docker\"
        fi
    fi
    \$DOCKER_CMD ps --filter \"name=iam-service\|profiles-service\|cases-service\|api-gateway\" --format \"table {{.Names}}\\t{{.Status}}\\t{{.Ports}}\" 2>/dev/null || echo 'No se pudo obtener estado'
" 2>/dev/null
echo ""

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
echo -e "   ✅ La nueva URL del tunnel se guardará en /tmp/tunnel_url.txt"
echo ""
echo -e "${BLUE}💡 TIP:${NC}"
echo -e "   • Para re-desplegar con cambios del repo, solo ejecuta este script de nuevo"
echo -e "   • El script limpiará /app y clonará la versión más reciente (no borra la VM)"
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

