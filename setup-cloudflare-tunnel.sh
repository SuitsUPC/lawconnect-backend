#!/bin/bash

# Script para configurar Cloudflare Tunnel (solución rápida sin dominio)
# Esto crea una URL HTTPS válida automáticamente

set -e

VM_NAME="lawconnect-vm"
ZONE="southamerica-east1-a"

echo "🚀 Configurando Cloudflare Tunnel..."
echo ""
echo "⚠️  IMPORTANTE: Necesitas una cuenta de Cloudflare (gratis)"
echo "    Regístrate en: https://dash.cloudflare.com/sign-up"
echo ""
read -p "¿Ya tienes cuenta de Cloudflare? (s/n): " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Ss]$ ]]; then
    echo "Por favor regístrate primero y vuelve a ejecutar el script"
    exit 1
fi

echo ""
echo "📥 Instalando cloudflared..."

# Instalar cloudflared
gcloud compute ssh $VM_NAME --zone=$ZONE --command="
    if ! command -v cloudflared &> /dev/null; then
        curl -L https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-linux-amd64 -o /tmp/cloudflared
        sudo mv /tmp/cloudflared /usr/local/bin/cloudflared
        sudo chmod +x /usr/local/bin/cloudflared
        echo '✅ cloudflared instalado'
    else
        echo '✅ cloudflared ya está instalado'
    fi
"

echo ""
echo "🔐 Autenticación con Cloudflare..."
echo "   Se abrirá una ventana del navegador para autenticarte"
echo ""

# Autenticar (esto abrirá el navegador)
gcloud compute ssh $VM_NAME --zone=$ZONE --command="cloudflared tunnel login" || {
    echo "❌ Error en la autenticación. Por favor ejecuta manualmente:"
    echo "   gcloud compute ssh $VM_NAME --zone=$ZONE --command='cloudflared tunnel login'"
    exit 1
}

echo ""
echo "🏗️  Creando tunnel..."

# Crear tunnel
TUNNEL_NAME="lawconnect-backend-$(date +%s)"
gcloud compute ssh $VM_NAME --zone=$ZONE --command="cloudflared tunnel create $TUNNEL_NAME" || {
    echo "⚠️  Tunnel puede que ya exista, continuando..."
}

# Obtener el ID del tunnel
TUNNEL_ID=$(gcloud compute ssh $VM_NAME --zone=$ZONE --command="cloudflared tunnel list | grep $TUNNEL_NAME | awk '{print \$1}' | head -1" | tr -d '\r\n')

if [ -z "$TUNNEL_ID" ]; then
    echo "❌ No se pudo obtener el ID del tunnel"
    echo "   Ejecuta manualmente: gcloud compute ssh $VM_NAME --zone=$ZONE --command='cloudflared tunnel list'"
    exit 1
fi

echo "✅ Tunnel creado: $TUNNEL_ID"

# Configurar el tunnel
gcloud compute ssh $VM_NAME --zone=$ZONE --command="
    sudo mkdir -p /etc/cloudflared
    sudo tee /etc/cloudflared/config.yml > /dev/null <<EOF
tunnel: $TUNNEL_ID
credentials-file: /root/.cloudflared/$TUNNEL_ID.json

ingress:
  - hostname: lawconnect-backend-\$(whoami).cloudflareaccess.com
    service: http://localhost:8080
  - service: http_status:404
EOF

    # Instalar como servicio
    sudo cloudflared service install
    sudo systemctl start cloudflared
    sudo systemctl enable cloudflared
    sudo systemctl status cloudflared --no-pager | head -10
"

echo ""
echo "✅ Cloudflare Tunnel configurado!"
echo ""
echo "🌐 Obtén tu URL ejecutando:"
echo "   gcloud compute ssh $VM_NAME --zone=$ZONE --command='cloudflared tunnel route dns list'"
echo ""
echo "O verifica en el dashboard de Cloudflare:"
echo "   https://dash.cloudflare.com -> Zero Trust -> Access -> Tunnels"

