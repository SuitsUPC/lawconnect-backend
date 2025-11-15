#!/bin/bash

# Script para configurar HTTPS en GCP usando Load Balancer con certificado SSL gestionado
# Requiere un dominio configurado

set -e

PROJECT_ID=$(gcloud config get-value project)
ZONE="southamerica-east1-a"
INSTANCE_NAME="lawconnect-vm"
DOMAIN="${1:-}"  # Dominio como primer argumento

if [ -z "$DOMAIN" ]; then
    echo "❌ Error: Se requiere un dominio"
    echo "Uso: ./setup-https-gcp.sh tu-dominio.com"
    echo ""
    echo "Si no tienes dominio, puedes usar una de estas opciones:"
    echo "1. Registrar un dominio (ej: en Google Domains, Namecheap, etc.)"
    echo "2. Usar un subdominio gratuito (ej: noip.com, duckdns.org)"
    echo "3. Usar Nginx con certificado autofirmado (solo para desarrollo)"
    exit 1
fi

echo "🚀 Configurando HTTPS para dominio: $DOMAIN"
echo "📋 Proyecto: $PROJECT_ID"
echo ""

# Obtener IP de la instancia
INSTANCE_IP=$(gcloud compute instances describe $INSTANCE_NAME --zone=$ZONE --format="get(networkInterfaces[0].accessConfigs[0].natIP)")
echo "📍 IP de la instancia: $INSTANCE_IP"
echo ""

# Verificar que el dominio apunta a la IP
echo "⚠️  IMPORTANTE: Asegúrate de que el dominio $DOMAIN apunta a la IP $INSTANCE_IP"
echo "   Configura un registro A en tu DNS: $DOMAIN -> $INSTANCE_IP"
read -p "¿El dominio ya está configurado? (s/n): " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Ss]$ ]]; then
    echo "❌ Configura el DNS primero y vuelve a ejecutar el script"
    exit 1
fi

# Crear health check
echo "🏥 Creando health check..."
gcloud compute health-checks create http lawconnect-health-check \
    --port=8080 \
    --request-path=/actuator/health \
    --check-interval=10s \
    --timeout=5s \
    --healthy-threshold=2 \
    --unhealthy-threshold=3 \
    --global || echo "Health check ya existe"

# Crear backend service
echo "🔧 Creando backend service..."
gcloud compute backend-services create lawconnect-backend \
    --protocol=HTTP \
    --health-checks=lawconnect-health-check \
    --global || echo "Backend service ya existe"

# Agregar instancia al backend service
echo "➕ Agregando instancia al backend service..."
gcloud compute backend-services add-backend lawconnect-backend \
    --instance=$INSTANCE_NAME \
    --instance-zone=$ZONE \
    --global || echo "Instancia ya agregada"

# Crear URL map
echo "🗺️  Creando URL map..."
gcloud compute url-maps create lawconnect-url-map \
    --default-service=lawconnect-backend || echo "URL map ya existe"

# Crear certificado SSL gestionado
echo "🔒 Creando certificado SSL gestionado..."
gcloud compute ssl-certificates create lawconnect-ssl-cert \
    --domains=$DOMAIN \
    --global || echo "Certificado ya existe"

# Crear target HTTPS proxy
echo "🎯 Creando target HTTPS proxy..."
gcloud compute target-https-proxies create lawconnect-https-proxy \
    --url-map=lawconnect-url-map \
    --ssl-certificates=lawconnect-ssl-cert || echo "HTTPS proxy ya existe"

# Crear forwarding rule
echo "🌐 Creando forwarding rule..."
gcloud compute forwarding-rules create lawconnect-https-forwarding-rule \
    --global \
    --target-https-proxy=lawconnect-https-proxy \
    --ports=443 || echo "Forwarding rule ya existe"

# Obtener IP del Load Balancer
LB_IP=$(gcloud compute forwarding-rules describe lawconnect-https-forwarding-rule --global --format="get(IPAddress)")
echo ""
echo "✅ Configuración completada!"
echo ""
echo "📋 Próximos pasos:"
echo "1. El certificado SSL puede tardar hasta 1 hora en aprovisionarse"
echo "2. Verifica el estado del certificado:"
echo "   gcloud compute ssl-certificates describe lawconnect-ssl-cert --global"
echo "3. Actualiza el DNS del dominio para que apunte a la IP del Load Balancer:"
echo "   $DOMAIN -> $LB_IP"
echo "4. Una vez que el certificado esté ACTIVO, podrás acceder a:"
echo "   https://$DOMAIN"
echo ""
echo "🔍 Verificar estado del certificado:"
echo "   gcloud compute ssl-certificates describe lawconnect-ssl-cert --global --format='get(managed.status)'"

