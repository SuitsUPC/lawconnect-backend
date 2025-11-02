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
echo -e "${GREEN}✓ Directorio encontrado: $(pwd)${NC}"

# Paso 5: Construir los JARs
echo -e "${GREEN}▶ Construyendo IAM Service...${NC}"
cd microservices/iam && mvn clean package spring-boot:repackage -DskipTests && echo -e "${GREEN}✓ IAM construido${NC}"

echo -e "${GREEN}▶ Construyendo Profiles Service...${NC}"
cd ../profiles && mvn clean package spring-boot:repackage -DskipTests && echo -e "${GREEN}✓ Profiles construido${NC}"

echo -e "${GREEN}▶ Construyendo Cases Service...${NC}"
cd ../cases && mvn clean package spring-boot:repackage -DskipTests && echo -e "${GREEN}✓ Cases construido${NC}"

echo -e "${GREEN}▶ Construyendo API Gateway...${NC}"
cd ../api-gateway && mvn clean package spring-boot:repackage -DskipTests && echo -e "${GREEN}✓ API Gateway construido${NC}"

cd ..
echo -e "${GREEN}✓ Todos los JARs construidos correctamente${NC}"

# Paso 6: Limpiar contenedores existentes
echo -e "${GREEN}▶ Limpiando contenedores Docker existentes...${NC}"
cd microservices
if sudo docker-compose ps | grep -q "Up\|Restarting"; then
    echo -e "${YELLOW}⚠ Se encontraron contenedores corriendo. Deteniéndolos...${NC}"
    sudo docker-compose down
    echo -e "${GREEN}✓ Contenedores detenidos${NC}"
else
    echo -e "${GREEN}✓ No hay contenedores corriendo${NC}"
fi
cd ..

# Paso 7: Configurar Nginx
echo -e "${GREEN}▶ Configurando Nginx...${NC}"
sudo cp nginx/nginx.conf /etc/nginx/sites-available/lawconnect
sudo ln -sf /etc/nginx/sites-available/lawconnect /etc/nginx/sites-enabled/
sudo rm -f /etc/nginx/sites-enabled/default
sudo systemctl restart nginx
sudo systemctl enable nginx

# Paso 8: Verificar que los JARs existen
echo -e "${GREEN}▶ Verificando que los JARs se construyeron...${NC}"
if [ ! -f microservices/iam/target/iam-service-0.0.1-SNAPSHOT.jar ]; then
    echo -e "${RED}✖ Error: No se encontró iam-service-0.0.1-SNAPSHOT.jar${NC}"
    exit 1
fi
if [ ! -f microservices/profiles/target/profiles-service-0.0.1-SNAPSHOT.jar ]; then
    echo -e "${RED}✖ Error: No se encontró profiles-service-0.0.1-SNAPSHOT.jar${NC}"
    exit 1
fi
if [ ! -f microservices/cases/target/cases-service-0.0.1-SNAPSHOT.jar ]; then
    echo -e "${RED}✖ Error: No se encontró cases-service-0.0.1-SNAPSHOT.jar${NC}"
    exit 1
fi
if [ ! -f microservices/api-gateway/target/api-gateway-0.0.1-SNAPSHOT.jar ]; then
    echo -e "${RED}✖ Error: No se encontró api-gateway-0.0.1-SNAPSHOT.jar${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Todos los JARs existen${NC}"

# Paso 9: Levantar servicios Docker
echo -e "${GREEN}▶ Levantando servicios con Docker Compose...${NC}"
cd microservices
sudo docker-compose up -d --build

# Esperar a que servicios inicien
echo -e "${GREEN}▶ Esperando que servicios inicien (30 segundos)...${NC}"
sleep 30

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
sudo docker-compose logs iam-service --tail=10
echo -e "${YELLOW}━━━━ Profiles Service ━━━━${NC}"
sudo docker-compose logs profiles-service --tail=10
echo -e "${YELLOW}━━━━ Cases Service ━━━━${NC}"
sudo docker-compose logs cases-service --tail=10
echo -e "${YELLOW}━━━━ API Gateway ━━━━${NC}"
sudo docker-compose logs api-gateway --tail=10

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
echo "📊 Información de servicios:"
echo "  • API disponible en: http://$(hostname -I | awk '{print $1}')/"
echo "  • Swagger UI: http://$(hostname -I | awk '{print $1}')/swagger-ui.html"
echo "  • Health Check: http://$(hostname -I | awk '{print $1}')/health"
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
