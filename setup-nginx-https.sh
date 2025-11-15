#!/bin/bash

# Script para configurar Nginx con HTTPS (certificado autofirmado) en la VM
# Esto permite HTTPS inmediatamente, aunque mostrará una advertencia en el navegador

set -e

VM_NAME="lawconnect-vm"
ZONE="southamerica-east1-a"

echo "🚀 Configurando Nginx con HTTPS en la VM..."

# Instalar Nginx y certbot
gcloud compute ssh $VM_NAME --zone=$ZONE --command="
    sudo apt-get update
    sudo apt-get install -y nginx certbot python3-certbot-nginx
    sudo systemctl enable nginx
"

# Crear configuración de Nginx
gcloud compute ssh $VM_NAME --zone=$ZONE --command="
    sudo tee /etc/nginx/sites-available/lawconnect > /dev/null <<'EOF'
# HTTP - Redirigir a HTTPS
server {
    listen 80;
    server_name _;
    
    # Permitir Let's Encrypt challenges
    location /.well-known/acme-challenge/ {
        root /var/www/html;
    }
    
    # Redirigir todo lo demás a HTTPS
    location / {
        return 301 https://\$host\$request_uri;
    }
}

# HTTPS - Proxy a Spring Boot
server {
    listen 443 ssl http2;
    server_name _;
    
    # Certificado autofirmado (temporal)
    ssl_certificate /etc/nginx/ssl/nginx-selfsigned.crt;
    ssl_certificate_key /etc/nginx/ssl/nginx-selfsigned.key;
    
    # Configuración SSL
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;
    
    # Proxy a Spring Boot
    location / {
        # Manejar preflight requests
        if (\$request_method = 'OPTIONS') {
            add_header Access-Control-Allow-Origin 'https://lawconnect-frontend.vercel.app' always;
            add_header Access-Control-Allow-Methods 'GET, POST, PUT, DELETE, OPTIONS, PATCH' always;
            add_header Access-Control-Allow-Headers 'Content-Type, Authorization, X-Requested-With' always;
            add_header Access-Control-Max-Age 3600;
            add_header Content-Type 'text/plain charset=UTF-8';
            add_header Content-Length 0;
            return 204;
        }
        
        # Headers para CORS
        add_header Access-Control-Allow-Origin 'https://lawconnect-frontend.vercel.app' always;
        add_header Access-Control-Allow-Methods 'GET, POST, PUT, DELETE, OPTIONS, PATCH' always;
        add_header Access-Control-Allow-Headers 'Content-Type, Authorization, X-Requested-With' always;
        add_header Access-Control-Allow-Credentials 'true' always;
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade \$http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_cache_bypass \$http_upgrade;
    }
}
EOF

    # Crear directorio para certificados
    sudo mkdir -p /etc/nginx/ssl
    
    # Generar certificado autofirmado
    sudo openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
        -keyout /etc/nginx/ssl/nginx-selfsigned.key \
        -out /etc/nginx/ssl/nginx-selfsigned.crt \
        -subj '/CN=lawconnect-backend/O=LawConnect/C=PE'
    
    # Habilitar sitio
    sudo ln -sf /etc/nginx/sites-available/lawconnect /etc/nginx/sites-enabled/
    sudo rm -f /etc/nginx/sites-enabled/default
    
    # Verificar configuración
    sudo nginx -t
    
    # Reiniciar Nginx
    sudo systemctl restart nginx
    sudo systemctl status nginx --no-pager
"

echo ""
echo "✅ Nginx configurado con HTTPS!"
echo ""
echo "⚠️  IMPORTANTE:"
echo "   - Se usó un certificado autofirmado (temporal)"
echo "   - El navegador mostrará una advertencia de seguridad"
echo "   - Para producción, configura un dominio y usa Let's Encrypt:"
echo "     sudo certbot --nginx -d tu-dominio.com"
echo ""
echo "🔍 Verificar que Nginx está corriendo:"
echo "   gcloud compute ssh $VM_NAME --zone=$ZONE --command='sudo systemctl status nginx'"
echo ""
echo "🌐 Acceder a:"
INSTANCE_IP=$(gcloud compute instances describe $VM_NAME --zone=$ZONE --format="get(networkInterfaces[0].accessConfigs[0].natIP)")
echo "   https://$INSTANCE_IP"
echo "   (El navegador mostrará una advertencia - haz clic en 'Avanzado' -> 'Continuar')"

