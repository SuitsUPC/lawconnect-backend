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
echo -e "${GREEN}▶ Esperando que servicios inicien (60 segundos)...${NC}"
sleep 60

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
LOCALHOST_IP="localhost"
# Intentar obtener IP pública, si falla usar la primera IP del hostname
PUBLIC_IP=$(curl -s https://api.ipify.org 2>/dev/null || hostname -I | awk '{print $1}')

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
echo "    - API: http://$PUBLIC_IP/api/v1/"
echo "    - Swagger: http://$PUBLIC_IP/swagger-ui.html"
echo "    - Health: http://$PUBLIC_IP/health"
echo ""

# Probar endpoints
echo "🧪 Probando endpoints..."

# Probar puerto 80 (Nginx)
echo -e "${GREEN}▶ Probando Puerto 80 (Nginx)...${NC}"
if curl -s http://localhost/health > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Puerto 80 OK desde localhost${NC}"
else
    echo -e "${YELLOW}⚠ Puerto 80 no responde desde localhost${NC}"
fi

# Probar puerto 8080 (API Gateway directo)
echo -e "${GREEN}▶ Probando Puerto 8080 (API Gateway directo)...${NC}"
if curl -s http://localhost:8080/api/v1/users > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Puerto 8080 OK - API Gateway funcionando${NC}"
else
    echo -e "${YELLOW}⚠ Puerto 8080 no responde desde localhost${NC}"
fi

# Probar endpoint IAM
echo -e "${GREEN}▶ Probando Endpoint IAM (/api/v1/users)...${NC}"
IAM_RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost/api/v1/users 2>/dev/null)
if [ "$IAM_RESPONSE" = "200" ] || [ "$IAM_RESPONSE" = "401" ]; then
    echo -e "${GREEN}✓ Endpoint IAM OK (HTTP $IAM_RESPONSE)${NC}"
else
    echo -e "${YELLOW}⚠ Endpoint IAM no responde (HTTP $IAM_RESPONSE)${NC}"
fi

# Probar endpoint Profiles  
echo -e "${GREEN}▶ Probando Endpoint Profiles (/api/v1/lawyer-specialties)...${NC}"
PROFILES_RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost/api/v1/lawyer-specialties 2>/dev/null)
if [ "$PROFILES_RESPONSE" = "200" ] || [ "$PROFILES_RESPONSE" = "401" ]; then
    echo -e "${GREEN}✓ Endpoint Profiles OK (HTTP $PROFILES_RESPONSE)${NC}"
else
    echo -e "${YELLOW}⚠ Endpoint Profiles no responde (HTTP $PROFILES_RESPONSE)${NC}"
fi

# Probar endpoint Cases
echo -e "${GREEN}▶ Probando Endpoint Cases (/api/v1/cases)...${NC}"
CASES_RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost/api/v1/cases 2>/dev/null)
if [ "$CASES_RESPONSE" = "200" ] || [ "$CASES_RESPONSE" = "401" ]; then
    echo -e "${GREEN}✓ Endpoint Cases OK (HTTP $CASES_RESPONSE)${NC}"
else
    echo -e "${YELLOW}⚠ Endpoint Cases no responde (HTTP $CASES_RESPONSE)${NC}"
fi

echo ""
echo -e "${GREEN}🧪 Probando documentación Swagger (OpenAPI JSON)...${NC}"
echo ""
GATEWAY_DOC_RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost/v3/api-docs 2>/dev/null)
IAM_DOC_RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost/iam-api-docs/v3/api-docs 2>/dev/null)
PROFILES_DOC_RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost/profiles-api-docs/v3/api-docs 2>/dev/null)
CASES_DOC_RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost/cases-api-docs/v3/api-docs 2>/dev/null)

echo -e "${GREEN}▶ API Gateway OpenAPI...${NC}"
if [ "$GATEWAY_DOC_RESPONSE" = "200" ]; then
    echo -e "${GREEN}✓ OK (HTTP $GATEWAY_DOC_RESPONSE)${NC}"
else
    echo -e "${YELLOW}⚠ No responde (HTTP $GATEWAY_DOC_RESPONSE)${NC}"
fi

echo -e "${GREEN}▶ IAM Service OpenAPI...${NC}"
if [ "$IAM_DOC_RESPONSE" = "200" ]; then
    echo -e "${GREEN}✓ OK (HTTP $IAM_DOC_RESPONSE)${NC}"
else
    echo -e "${YELLOW}⚠ No responde (HTTP $IAM_DOC_RESPONSE)${NC}"
fi

echo -e "${GREEN}▶ Profiles Service OpenAPI...${NC}"
if [ "$PROFILES_DOC_RESPONSE" = "200" ]; then
    echo -e "${GREEN}✓ OK (HTTP $PROFILES_DOC_RESPONSE)${NC}"
else
    echo -e "${YELLOW}⚠ No responde (HTTP $PROFILES_DOC_RESPONSE)${NC}"
fi

echo -e "${GREEN}▶ Cases Service OpenAPI...${NC}"
if [ "$CASES_DOC_RESPONSE" = "200" ]; then
    echo -e "${GREEN}✓ OK (HTTP $CASES_DOC_RESPONSE)${NC}"
else
    echo -e "${YELLOW}⚠ No responde (HTTP $CASES_DOC_RESPONSE)${NC}"
fi

echo ""
echo -e "${GREEN}📊 Resumen de pruebas de endpoints:${NC}"
echo "  • Puerto 80 (Nginx): http://localhost/"
echo "  • Puerto 8080 (API Gateway): http://localhost:8080/"
echo "  • Endpoint IAM: http://localhost/api/v1/users"
echo "  • Endpoint Profiles: http://localhost/api/v1/lawyer-specialties"
echo "  • Endpoint Cases: http://localhost/api/v1/cases"
echo ""
echo -e "${GREEN}🌐 URLs desde Internet (después de configurar Azure NSG puerto 80):${NC}"
echo "  • Swagger UI: http://$PUBLIC_IP/swagger-ui.html"
echo "  • API: http://$PUBLIC_IP/api/v1/"
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
