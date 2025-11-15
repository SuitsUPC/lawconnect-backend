#!/bin/bash
# Script para obtener la URL del tunnel después de reiniciar la VM

VM_NAME="lawconnect-vm"
ZONE="southamerica-east1-a"

echo "🔍 Obteniendo URL del Cloudflare Tunnel..."
gcloud compute ssh $VM_NAME --zone=$ZONE --command="
    TUNNEL_URL=\$(sudo journalctl -u cloudflared -n 200 --no-pager | grep -o 'https://[a-z0-9-]*\.trycloudflare\.com' | tail -1)
    
    if [ ! -z \"\$TUNNEL_URL\" ]; then
        echo \"✅ URL del tunnel:\"
        echo \"   \$TUNNEL_URL\"
        echo \"\"
        echo \"📝 Actualiza tu frontend:\"
        echo \"   NEXT_PUBLIC_API_URL=\$TUNNEL_URL\"
    else
        echo \"⚠️  URL no encontrada. Espera unos segundos y vuelve a intentar.\"
        echo \"   O verifica que cloudflared esté corriendo:\"
        echo \"   sudo systemctl status cloudflared\"
    fi
"
