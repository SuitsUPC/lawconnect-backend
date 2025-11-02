#!/bin/bash

# Script de despliegue automático para LawConnect Backend en Azure VM
echo "🚀 Iniciando despliegue de LawConnect Backend..."

set -e  # Salir si hay algún error

# Colores para output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

# Paso 1: Instalar dependencias base
echo -e "${GREEN}▶ Instalando dependencias base...${NC}"
sudo apt install -y openjdk-17-jdk maven nginx git

# Paso 2: Instalar Docker
echo -e "${GREEN}▶ Instalando Docker...${NC}"
sudo apt install -y docker.io docker-compose || sudo apt install -y docker-compose

# Paso 3: Iniciar Docker
echo -e "${GREEN}▶ Configurando Docker...${NC}"
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -aG docker $USER

# Paso 4: Navegar al directorio del proyecto
echo -e "${GREEN}▶ Navegando al directorio del proyecto...${NC}"
cd ~/lawconnect-backend || { echo -e "${RED}✖ No se encontró el directorio lawconnect-backend${NC}"; exit 1; }
PROJECT_ROOT=$(pwd)
echo -e "${GREEN}✓ Directorio encontrado: $PROJECT_ROOT${NC}"

# Paso 5: Construir los JARs (con output silencioso para Maven)
echo -e "${GREEN}▶ Construyendo IAM Service...${NC}"
cd $PROJECT_ROOT/microservices/iam && mvn clean package spring-boot:repackage -DskipTests -q && echo -e "${GREEN}✓ IAM construido${NC}"

echo -e "${GREEN}▶ Construyendo Profiles Service...${NC}"
cd $PROJECT_ROOT/microservices/profiles && mvn clean package spring-boot:repackage -DskipTests -q && echo -e "${GREEN}✓ Profiles construido${NC}"

echo -e "${GREEN}▶ Construyendo Cases Service...${NC}"
cd $PROJECT_ROOT/microservices/cases && mvn clean package spring-boot:repackage -DskipTests -q && echo -e "${GREEN}✓ Cases construido${NC}"

echo -e "${GREEN}▶ Construyendo API Gateway...${NC}"
cd $PROJECT_ROOT/microservices/api-gateway && mvn clean package spring-boot:repackage -DskipTests -q && echo -e "${GREEN}✓ API Gateway construido${NC}"

echo -e "${GREEN}✓ Todos los JARs construidos correctamente${NC}"

# Paso 6: Limpiar contenedores existentes
echo -e "${GREEN}▶ Limpiando contenedores Docker existentes...${NC}"
cd $PROJECT_ROOT/microservices
if sudo docker-compose ps | grep -q "Up\|Restarting"; then
    echo -e "${YELLOW}⚠ Se encontraron contenedores corriendo. Deteniéndolos...${NC}"
    sudo docker-compose down
    echo -e "${GREEN}✓ Contenedores detenidos${NC}"
else
    echo -e "${GREEN}✓ No hay contenedores corriendo${NC}"
fi

# Paso 7: Configurar Nginx
echo -e "${GREEN}▶ Configurando Nginx...${NC}"
sudo cp $PROJECT_ROOT/microservices/nginx/nginx.conf /etc/nginx/sites-available/lawconnect
sudo ln -sf /etc/nginx/sites-available/lawconnect /etc/nginx/sites-enabled/
sudo rm -f /etc/nginx/sites-enabled/default
sudo systemctl restart nginx
sudo systemctl enable nginx

# Paso 8: Verificar que los JARs existen
echo -e "${GREEN}▶ Verificando que los JARs se construyeron...${NC}"
if [ ! -f $PROJECT_ROOT/microservices/iam/target/iam-service-0.0.1-SNAPSHOT.jar ]; then
    echo -e "${RED}✖ Error: No se encontró iam-service-0.0.1-SNAPSHOT.jar${NC}"
    exit 1
fi
if [ ! -f $PROJECT_ROOT/microservices/profiles/target/profiles-service-0.0.1-SNAPSHOT.jar ]; then
    echo -e "${RED}✖ Error: No se encontró profiles-service-0.0.1-SNAPSHOT.jar${NC}"
    exit 1
fi
if [ ! -f $PROJECT_ROOT/microservices/cases/target/cases-service-0.0.1-SNAPSHOT.jar ]; then
    echo -e "${RED}✖ Error: No se encontró cases-service-0.0.1-SNAPSHOT.jar${NC}"
    exit 1
fi
if [ ! -f $PROJECT_ROOT/microservices/api-gateway/target/api-gateway-0.0.1-SNAPSHOT.jar ]; then
    echo -e "${RED}✖ Error: No se encontró api-gateway-0.0.1-SNAPSHOT.jar${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Todos los JARs existen${NC}"

# Paso 9: Levantar servicios Docker
echo -e "${GREEN}▶ Levantando servicios con Docker Compose...${NC}"
cd $PROJECT_ROOT/microservices
sudo docker-compose up -d --build

# Esperar a que servicios inicien
echo -e "${GREEN}▶ Esperando que servicios inicien (15 segundos)...${NC}"
sleep 15

# Paso 10: Verificar estado
echo -e "${GREEN}▶ Verificando estado de servicios...${NC}"
sudo docker-compose ps

# Ver logs si hay errores
echo -e "${YELLOW}▶ Revisando logs por errores...${NC}"
ERRORS=$(sudo docker-compose logs | grep -i "error\|exception\|failed" | wc -l)
if [ $ERRORS -gt 0 ]; then
    echo -e "${YELLOW}⚠ Se encontraron $ERRORS mensajes de error/excepción en los logs${NC}"
    echo -e "${YELLOW}⚠ Ejecuta: sudo docker-compose logs para ver detalles${NC}"
fi

# Mostrar logs recientes
echo -e "${GREEN}▶ Mostrando últimas líneas de logs de cada servicio...${NC}"
echo -e "${YELLOW}━━━━ IAM Service ━━━━${NC}"
sudo docker-compose logs iam-service | tail -20
echo ""
echo -e "${YELLOW}━━━━ Profiles Service ━━━━${NC}"
sudo docker-compose logs profiles-service | tail -20
echo ""
echo -e "${YELLOW}━━━━ Cases Service ━━━━${NC}"
sudo docker-compose logs cases-service | tail -20
echo ""
echo -e "${YELLOW}━━━━ API Gateway ━━━━${NC}"
sudo docker-compose logs api-gateway | tail -20
echo ""

# Paso 11: Configurar inicio automático
echo -e "${GREEN}▶ Configurando inicio automático...${NC}"
USER_HOME=$(eval echo ~$USER)
sudo tee /etc/systemd/system/lawconnect.service > /dev/null <<EOF
[Unit]
Description=LawConnect Microservices
Requires=docker.service
After=docker.service

[Service]
Type=oneshot
RemainAfterExit=yes
WorkingDirectory=$USER_HOME/lawconnect-backend/microservices
ExecStart=/usr/bin/sudo /usr/bin/docker-compose up -d
ExecStop=/usr/bin/sudo /usr/bin/docker-compose down
TimeoutStartSec=0

[Install]
WantedBy=multi-user.target
EOF

sudo systemctl daemon-reload
sudo systemctl enable lawconnect.service

# Paso 12: Firewall básico
echo -e "${GREEN}▶ Configurando firewall...${NC}"
sudo ufw allow 22/tcp || true
sudo ufw allow 80/tcp || true
sudo ufw allow 443/tcp || true
sudo ufw --force enable || true

# Mostrar resumen
echo ""
echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${GREEN}✅ Despliegue completado exitosamente!${NC}"
echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""

# Obtener IPs
VM_IP=$(hostname -I | awk '{print $1}')
LOCALHOST_IP="localhost"

echo "📊 Información de puertos y URLs:"
echo "  • Puerto Nginx: 80"
echo "  • Puerto API Gateway: 8080"
echo "  • Puerto IAM: 8081"
echo "  • Puerto Profiles: 8082"
echo "  • Puerto Cases: 8083"
echo "  • Puertos MySQL: 3307, 3308, 3309"
echo ""
echo "🌐 URLs de acceso:"
echo "  • Desde localhost:"
echo "    - API: http://$LOCALHOST_IP/api/v1/"
echo "    - Swagger: http://$LOCALHOST_IP/swagger-ui.html"
echo "    - Health: http://$LOCALHOST_IP/health"
echo ""
echo "  • Desde Internet (necesitas configurar Azure NSG puerto 80):"
echo "    - API: http://$VM_IP/api/v1/"
echo "    - Swagger: http://$VM_IP/swagger-ui.html"
echo "    - Health: http://$VM_IP/health"
echo ""

# Probar endpoints
echo "🧪 Probando endpoints..."
echo -e "${GREEN}▶ Probando health check desde localhost...${NC}"
if curl -s http://localhost/health > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Health check OK desde localhost${NC}"
else
    echo -e "${YELLOW}⚠ Health check no responde desde localhost (puede tardar un poco más)${NC}"
fi

echo -e "${GREEN}▶ Probando desde IP $VM_IP...${NC}"
if curl -s http://$VM_IP/health > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Health check OK desde IP $VM_IP${NC}"
else
    echo -e "${YELLOW}⚠ Health check no responde desde IP (verificar Azure NSG)${NC}"
fi

echo ""
echo -e "${YELLOW}ℹ Si hay servicios en 'Restarting', espera 60 segundos más y verifica:${NC}"
echo -e "${YELLOW}  sudo docker-compose logs -f [nombre-servicio]${NC}"
echo ""
echo "📝 Comandos útiles:"
echo "  • Ver logs: cd ~/lawconnect-backend/microservices && sudo docker-compose logs -f"
echo "  • Reiniciar: cd ~/lawconnect-backend/microservices && sudo docker-compose restart"
echo "  • Ver estado: sudo docker-compose ps"
echo "  • Ver logs Nginx: sudo tail -f /var/log/nginx/lawconnect_access.log"
echo ""
