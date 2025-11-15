#!/bin/bash

# Script para configurar DuckDNS + Let's Encrypt para HTTPS válido
# Requiere: Registrarse en https://www.duckdns.org/ y crear un subdominio

set -e

VM_NAME="lawconnect-vm"
ZONE="southamerica-east1-a"
DOMAIN="${1:-}"  # Ejemplo: lawconnect-backend.duckdns.org
EMAIL="${2:-}"   # Email para Let's Encrypt

if [ -z "$DOMAIN" ] || [ -z "$EMAIL" ]; then
    echo "❌ Error: Se requiere dominio y email"
    echo "Uso: ./setup-duckdns-letsencrypt.sh lawconnect-backend.duckdns.org tu-email@example.com"
    echo ""
    echo "Pasos previos:"
    echo "1. Regístrate en https://www.duckdns.org/"
    echo "2. Crea un subdominio (ej: lawconnect-backend)"
    echo "3. Configura el DNS para que apunte a tu IP: 35.247.254.52"
    exit 1
fi

echo "🚀 Configurando Let's Encrypt para: $DOMAIN"
echo "📧 Email: $EMAIL"
echo ""

# Verificar que el dominio resuelve a la IP correcta
INSTANCE_IP=$(gcloud compute instances describe $VM_NAME --zone=$ZONE --format="get(networkInterfaces[0].accessConfigs[0].natIP)")
RESOLVED_IP=$(dig +short $DOMAIN | tail -1)

if [ "$RESOLVED_IP" != "$INSTANCE_IP" ]; then
    echo "⚠️  ADVERTENCIA: El dominio $DOMAIN resuelve a $RESOLVED_IP, pero la IP de la VM es $INSTANCE_IP"
    echo "   Asegúrate de que el DNS esté configurado correctamente"
    read -p "¿Continuar de todas formas? (s/n): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Ss]$ ]]; then
        exit 1
    fi
fi

# Actualizar configuración de Nginx para usar el dominio
gcloud compute ssh $VM_NAME --zone=$ZONE --command="
    sudo tee /etc/nginx/sites-available/lawconnect > /dev/null <<'EOF'
# HTTP - Redirigir a HTTPS y permitir Let's Encrypt
server {
    listen 80;
    server_name $DOMAIN;

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
    server_name $DOMAIN;
    
    # Certificados Let's Encrypt (se generarán automáticamente)
    ssl_certificate /etc/letsencrypt/live/$DOMAIN/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/$DOMAIN/privkey.pem;
    
    # Configuración SSL moderna
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers 'ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256:ECDHE-ECDSA-AES256-GCM-SHA384:ECDHE-RSA-AES256-GCM-SHA384';
    ssl_prefer_server_ciphers off;
    ssl_session_cache shared:SSL:10m;
    ssl_session_timeout 10m;
    
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

    # Crear directorio para Let's Encrypt
    sudo mkdir -p /var/www/html/.well-known/acme-challenge
    
    # Obtener certificado de Let's Encrypt
    sudo certbot certonly --nginx -d $DOMAIN --non-interactive --agree-tos --email $EMAIL --preferred-challenges http
    
    # Verificar configuración de Nginx
    sudo nginx -t
    
    # Reiniciar Nginx
    sudo systemctl restart nginx
    sudo systemctl status nginx --no-pager | head -15
"

echo ""
echo "✅ Let's Encrypt configurado!"
echo ""
echo "🌐 URL del backend:"
echo "   https://$DOMAIN"
echo ""
echo "📝 Actualiza tu frontend:"
echo "   NEXT_PUBLIC_API_URL=https://$DOMAIN"
echo ""
echo "🔄 Renovación automática:"
echo "   Certbot configuró la renovación automática"
echo "   Verificar: sudo certbot renew --dry-run"

