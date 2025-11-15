#!/bin/bash

# Script simplificado para Cloudflare Tunnel SIN dominio
# Usa un tunnel público que no requiere dominio

set -e

VM_NAME="lawconnect-vm"
ZONE="southamerica-east1-a"

echo "🚀 Configurando Cloudflare Tunnel (método simple sin dominio)..."
echo ""

# Verificar si cloudflared está instalado
echo "📥 Verificando cloudflared..."
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
echo "🔐 Método alternativo: Tunnel público (no requiere autenticación con zona)"
echo "   Esto creará una URL temporal que puedes usar"
echo ""

# Crear un tunnel público temporal
echo "🌐 Iniciando tunnel público..."
echo "   Esto creará una URL como: https://xxxxx.trycloudflare.com"
echo ""

# Ejecutar tunnel en modo público (no requiere autenticación)
gcloud compute ssh $VM_NAME --zone=$ZONE --command="
    # Detener cualquier tunnel anterior
    sudo systemctl stop cloudflared 2>/dev/null || true
    
    # Ejecutar tunnel público (esto mostrará la URL)
    nohup cloudflared tunnel --url http://localhost:8080 > /tmp/cloudflared.log 2>&1 &
    sleep 5
    
    # Obtener la URL del log
    if [ -f /tmp/cloudflared.log ]; then
        TUNNEL_URL=\$(grep -o 'https://[a-z0-9-]*\.trycloudflare\.com' /tmp/cloudflared.log | head -1)
        if [ ! -z \"\$TUNNEL_URL\" ]; then
            echo \"✅ Tunnel creado: \$TUNNEL_URL\"
            echo \"\$TUNNEL_URL\" > /tmp/tunnel_url.txt
        else
            echo \"⚠️  URL no encontrada en el log, revisa: /tmp/cloudflared.log\"
        fi
    fi
"

echo ""
echo "📋 Para ver la URL del tunnel, ejecuta:"
echo "   gcloud compute ssh $VM_NAME --zone=$ZONE --command='cat /tmp/tunnel_url.txt 2>/dev/null || tail -20 /tmp/cloudflared.log'"
echo ""
echo "💡 Nota: Este método crea una URL temporal. Para una URL permanente,"
echo "   necesitas autenticarte con Cloudflare (pero puedes hacerlo después)"

