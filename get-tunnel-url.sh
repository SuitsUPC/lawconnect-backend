#!/bin/bash
# Script para obtener la URL del tunnel después de reiniciar la VM

VM_NAME="lawconnect-vm"
ZONE="southamerica-east1-a"

echo "🔍 Obteniendo URL del Cloudflare Tunnel..."
gcloud compute ssh $VM_NAME --zone=$ZONE --command="
    # Intentar leer desde archivo guardado
    if [ -f /tmp/tunnel_url.txt ]; then
        TUNNEL_URL=\$(cat /tmp/tunnel_url.txt 2>/dev/null)
    fi
    
    # Si no está en el archivo, buscar en los logs
    if [ -z \"\$TUNNEL_URL\" ]; then
        TUNNEL_URL=\$(sudo journalctl -u cloudflared -n 200 --no-pager 2>/dev/null | grep -o 'https://[a-z0-9-]*\.trycloudflare\.com' | tail -1)
    fi
    
    # Si aún no está, buscar en el log de startup
    if [ -z \"\$TUNNEL_URL\" ]; then
        TUNNEL_URL=\$(grep -o 'https://[a-z0-9-]*\.trycloudflare\.com' /var/log/lawconnect-startup.log 2>/dev/null | tail -1)
    fi
    
    if [ ! -z \"\$TUNNEL_URL\" ]; then
        echo \"\$TUNNEL_URL\" | sudo tee /tmp/tunnel_url.txt > /dev/null
        echo \"✅ URL del tunnel:\"
        echo \"   \$TUNNEL_URL\"
        echo \"\"
        echo \"📝 Actualiza tu frontend en Vercel:\"
        echo \"   NEXT_PUBLIC_API_URL=\$TUNNEL_URL\"
    else
        echo \"⚠️  URL no encontrada. Espera unos segundos y vuelve a intentar.\"
        echo \"   O verifica que cloudflared esté corriendo:\"
        echo \"   sudo systemctl status cloudflared\"
        echo \"   sudo journalctl -u cloudflared -f\"
    fi
"
