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

# Paso 5: Construir los JARs
echo -e "${GREEN}▶ Construyendo microservicios...${NC}"
cd microservices/iam && mvn clean package -DskipTests -q && echo "✓ IAM"
cd ../profiles && mvn clean package -DskipTests -q && echo "✓ Profiles"
cd ../cases && mvn clean package -DskipTests -q && echo "✓ Cases"
cd ../api-gateway && mvn clean package -DskipTests -q && echo "✓ API Gateway"
cd ..

# Paso 6: Configurar Nginx
echo -e "${GREEN}▶ Configurando Nginx...${NC}"
sudo cp nginx/nginx.conf /etc/nginx/sites-available/lawconnect
sudo ln -sf /etc/nginx/sites-available/lawconnect /etc/nginx/sites-enabled/
sudo rm -f /etc/nginx/sites-enabled/default
sudo systemctl restart nginx
sudo systemctl enable nginx

# Paso 7: Levantar servicios Docker (usando sudo para evitar problemas de permisos)
echo -e "${GREEN}▶ Levantando servicios con Docker Compose...${NC}"
sudo docker-compose up -d --build

# Esperar a que servicios inicien
echo -e "${GREEN}▶ Esperando que servicios inicien...${NC}"
sleep 15

# Paso 8: Verificar estado
echo -e "${GREEN}▶ Verificando estado de servicios...${NC}"
sudo docker-compose ps

# Paso 9: Configurar inicio automático
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

# Paso 10: Firewall básico
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
echo "📝 Comandos útiles:"
echo "  • Ver logs: cd ~/lawconnect-backend/microservices && sudo docker-compose logs -f"
echo "  • Reiniciar: cd ~/lawconnect-backend/microservices && sudo docker-compose restart"
echo "  • Ver estado: sudo docker-compose ps"
echo "  • Ver logs Nginx: sudo tail -f /var/log/nginx/lawconnect_access.log"
echo ""
