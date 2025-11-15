#!/bin/bash

# Script para configurar TODO para que se inicie automáticamente al reiniciar la VM

set -e

VM_NAME="lawconnect-vm"
ZONE="southamerica-east1-a"

echo "🚀 Configurando inicio automático completo..."
echo ""

# 1. Asegurar que Docker se inicia automáticamente
echo "1️⃣ Configurando Docker..."
gcloud compute ssh $VM_NAME --zone=$ZONE --command="
    sudo systemctl enable docker
    sudo systemctl start docker
    echo '✅ Docker configurado'
"

# 2. Asegurar que Nginx se inicia automáticamente
echo ""
echo "2️⃣ Configurando Nginx..."
gcloud compute ssh $VM_NAME --zone=$ZONE --command="
    sudo systemctl enable nginx
    sudo systemctl start nginx
    echo '✅ Nginx configurado'
"

# 3. Configurar cloudflared como servicio permanente
echo ""
echo "3️⃣ Configurando Cloudflare Tunnel..."
gcloud compute ssh $VM_NAME --zone=$ZONE --command="
    # Detener procesos anteriores
    pkill -f cloudflared || true
    sudo systemctl stop cloudflared 2>/dev/null || true
    
    # Crear servicio systemd
    sudo tee /etc/systemd/system/cloudflared.service > /dev/null <<'EOF'
[Unit]
Description=Cloudflare Tunnel
After=network.target docker.service

[Service]
Type=simple
User=root
ExecStart=/usr/local/bin/cloudflared tunnel --url http://localhost:8080
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
EOF

    sudo systemctl daemon-reload
    sudo systemctl enable cloudflared
    sudo systemctl start cloudflared
    sleep 5
    echo '✅ Cloudflare Tunnel configurado'
"

# 4. Verificar servicio de LawConnect
echo ""
echo "4️⃣ Verificando servicio LawConnect..."
gcloud compute ssh $VM_NAME --zone=$ZONE --command="
    if sudo systemctl is-enabled lawconnect.service > /dev/null 2>&1; then
        echo '✅ LawConnect service ya está configurado'
    else
        echo '⚠️  LawConnect service no encontrado, pero Docker se iniciará automáticamente'
    fi
"

# 5. Obtener URL del tunnel
echo ""
echo "5️⃣ Obteniendo URL del tunnel..."
sleep 5
gcloud compute ssh $VM_NAME --zone=$ZONE --command="
    TUNNEL_URL=\$(sudo journalctl -u cloudflared -n 100 --no-pager | grep -o 'https://[a-z0-9-]*\.trycloudflare\.com' | tail -1)
    
    if [ ! -z \"\$TUNNEL_URL\" ]; then
        echo \"✅ URL del tunnel:\"
        echo \"   \$TUNNEL_URL\"
        echo \"\$TUNNEL_URL\" > /tmp/tunnel_url.txt
        echo \"\"
        echo \"📝 Actualiza tu frontend:\"
        echo \"   NEXT_PUBLIC_API_URL=\$TUNNEL_URL\"
    else
        echo \"⚠️  URL no encontrada aún. Espera 30 segundos y ejecuta:\"
        echo \"   sudo journalctl -u cloudflared -n 50 | grep trycloudflare\"
    fi
"

echo ""
echo "✅ Configuración completada!"
echo ""
echo "📋 Resumen de servicios que se iniciarán automáticamente:"
echo "   ✅ Docker"
echo "   ✅ Nginx (HTTPS proxy)"
echo "   ✅ Cloudflare Tunnel"
echo "   ✅ LawConnect Services (vía Docker)"
echo ""
echo "🔄 Para verificar después de reiniciar:"
echo "   gcloud compute ssh $VM_NAME --zone=$ZONE --command='sudo systemctl status cloudflared'"
echo "   gcloud compute ssh $VM_NAME --zone=$ZONE --command='docker ps'"
echo ""
echo "🌐 Para obtener la URL del tunnel después de reiniciar:"
echo "   gcloud compute ssh $VM_NAME --zone=$ZONE --command='sudo journalctl -u cloudflared -n 50 | grep trycloudflare'"

