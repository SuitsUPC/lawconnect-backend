#!/bin/bash

# Script de despliegue automático para LawConnect Backend en Azure VM
# Este script automatiza la mayoría de los pasos de despliegue

set -e  # Salir si hay algún error

echo "🚀 Iniciando despliegue de LawConnect Backend..."

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Funciones de ayuda
print_step() {
    echo -e "${GREEN}▶ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

print_error() {
    echo -e "${RED}✖ $1${NC}"
}

# Verificar si se está ejecutando como root
if [ "$EUID" -eq 0 ]; then 
    print_error "Por favor, ejecuta este script con un usuario normal (no root)"
    exit 1
fi

# Paso 1: Actualizar sistema
print_step "Actualizando sistema..."
sudo apt update && sudo apt upgrade -y

# Paso 2: Instalar dependencias
print_step "Instalando dependencias..."
sudo apt install -y openjdk-17-jdk maven docker.io docker-compose nginx git

# Agregar usuario al grupo docker
print_step "Agregando usuario al grupo docker..."
sudo usermod -aG docker $USER

print_warning "Reinicia la sesión SSH y ejecuta este script de nuevo después de reiniciar"
print_warning "O ejecuta: newgrp docker"

# Verificar si Docker está disponible
if ! docker info &> /dev/null; then
    print_error "Docker no está disponible. Por favor, reinicia la sesión SSH y ejecuta este script de nuevo"
    exit 1
fi

# Paso 3: Navegar al directorio del proyecto
print_step "Navegando al directorio del proyecto..."
cd ~/lawconnect-backend || { print_error "No se encontró el directorio lawconnect-backend"; exit 1; }

# Paso 4: Construir los JARs
print_step "Construyendo microservicios..."
cd microservices/iam
mvn clean package -DskipTests -q
print_step "✓ IAM Service construido"

cd ../profiles
mvn clean package -DskipTests -q
print_step "✓ Profiles Service construido"

cd ../cases
mvn clean package -DskipTests -q
print_step "✓ Cases Service construido"

cd ../api-gateway
mvn clean package -DskipTests -q
print_step "✓ API Gateway construido"

cd ..

# Paso 5: Configurar Nginx
print_step "Configurando Nginx..."
sudo cp nginx/nginx.conf /etc/nginx/sites-available/lawconnect
sudo ln -sf /etc/nginx/sites-available/lawconnect /etc/nginx/sites-enabled/
sudo rm -f /etc/nginx/sites-enabled/default

# Verificar configuración de Nginx
print_step "Verificando configuración de Nginx..."
if sudo nginx -t; then
    print_step "✓ Configuración de Nginx válida"
    sudo systemctl restart nginx
    sudo systemctl enable nginx
else
    print_error "Error en configuración de Nginx"
    exit 1
fi

# Paso 6: Levantar servicios Docker
print_step "Levantando servicios con Docker Compose..."
docker-compose up -d --build

# Esperar un poco para que los servicios inicien
sleep 10

# Paso 7: Verificar estado de servicios
print_step "Verificando estado de servicios..."
docker-compose ps

# Paso 8: Verificar salud
print_step "Verificando salud de los servicios..."
sleep 5

if curl -s http://localhost/health > /dev/null; then
    print_step "✓ Health check OK"
else
    print_warning "Health check no responde todavía, espera un poco más"
fi

# Paso 9: Crear servicio systemd para Docker Compose
print_step "Configurando inicio automático..."
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
ExecStart=/usr/bin/docker-compose up -d
ExecStop=/usr/bin/docker-compose down
TimeoutStartSec=0
User=$USER
Group=docker

[Install]
WantedBy=multi-user.target
EOF

sudo systemctl daemon-reload
sudo systemctl enable lawconnect.service

# Paso 10: Configurar firewall
print_step "Configurando firewall..."
sudo ufw --force enable || true
sudo ufw allow 22/tcp
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp

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
echo "  • Ver logs: cd ~/lawconnect-backend/microservices && docker-compose logs -f"
echo "  • Reiniciar servicios: cd ~/lawconnect-backend/microservices && docker-compose restart"
echo "  • Ver estado: docker-compose ps"
echo "  • Ver logs Nginx: sudo tail -f /var/log/nginx/lawconnect_access.log"
echo ""

