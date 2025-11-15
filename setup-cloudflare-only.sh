#!/bin/bash

# Script para configurar SOLO Cloudflare Tunnel (sin re-desplegar)
# Úsalo cuando los servicios ya están corriendo y solo necesitas el tunnel

set +e

# Colores
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m'

# Configuración
VM_NAME="lawconnect-vm"
ZONE="southamerica-east1-a"

echo -e "${BLUE}🌐 Configurando Cloudflare Tunnel...${NC}"
echo ""

# Verificar que la VM existe
if ! gcloud compute instances describe "$VM_NAME" --zone="$ZONE" >/dev/null 2>&1; then
    echo -e "${RED}❌ Error: La VM '$VM_NAME' no existe${NC}"
    exit 1
fi

# Verificar que la VM está corriendo
VM_STATUS=$(gcloud compute instances describe "$VM_NAME" --zone="$ZONE" --format="get(status)")
if [ "$VM_STATUS" != "RUNNING" ]; then
    echo -e "${YELLOW}⚠️  La VM está detenida. Iniciándola...${NC}"
    gcloud compute instances start "$VM_NAME" --zone="$ZONE"
    echo -e "${YELLOW}⏳ Esperando 30 segundos para que la VM esté lista...${NC}"
    sleep 30
fi

# Obtener IP
VM_IP=$(gcloud compute instances describe "$VM_NAME" --zone="$ZONE" --format="get(networkInterfaces[0].accessConfigs[0].natIP)")
echo -e "${GREEN}✅ VM IP: $VM_IP${NC}"
echo ""

# Configurar Cloudflare Tunnel
echo -e "${BLUE}📥 Instalando y configurando cloudflared...${NC}"

# Paso 1: Instalar cloudflared si no está instalado
echo -e "${BLUE}   Paso 1: Verificando cloudflared...${NC}"
gcloud compute ssh "$VM_NAME" --zone="$ZONE" --command="
    if ! command -v cloudflared &> /dev/null; then
        echo '📥 Instalando cloudflared...'
        curl -L https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-linux-amd64 -o /tmp/cloudflared
        sudo mv /tmp/cloudflared /usr/local/bin/cloudflared
        sudo chmod +x /usr/local/bin/cloudflared
        echo '✅ cloudflared instalado'
    else
        echo '✅ cloudflared ya está instalado'
    fi
" 2>&1

if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Error al verificar/instalar cloudflared${NC}"
    exit 1
fi

# Paso 2: Detener procesos anteriores
echo -e "${BLUE}   Paso 2: Deteniendo procesos anteriores...${NC}"
gcloud compute ssh "$VM_NAME" --zone="$ZONE" --command="
    pkill -f cloudflared || true
    sudo systemctl stop cloudflared 2>/dev/null || true
    echo '✅ Procesos anteriores detenidos'
" 2>&1

# Paso 3: Crear servicio systemd
echo -e "${BLUE}   Paso 3: Creando servicio systemd...${NC}"
gcloud compute ssh "$VM_NAME" --zone="$ZONE" --command="
    sudo tee /etc/systemd/system/cloudflared.service > /dev/null <<'EOF'
[Unit]
Description=Cloudflare Tunnel
After=network.target docker.service
Wants=docker.service

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
    echo '✅ Servicio systemd creado'
" 2>&1

if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Error al crear servicio systemd${NC}"
    exit 1
fi

# Paso 4: Habilitar y iniciar servicio
echo -e "${BLUE}   Paso 4: Iniciando servicio...${NC}"
gcloud compute ssh "$VM_NAME" --zone="$ZONE" --command="
    sudo systemctl daemon-reload
    sudo systemctl enable cloudflared
    sudo systemctl start cloudflared
    echo '✅ Cloudflare Tunnel iniciado'
    sleep 10
" 2>&1

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ Cloudflare Tunnel configurado correctamente${NC}"
    echo ""
    
    # Obtener URL del tunnel
    echo -e "${BLUE}🔍 Obteniendo URL del Cloudflare Tunnel...${NC}"
    sleep 15  # Esperar a que el tunnel se establezca
    
    TUNNEL_URL=""
    for attempt in {1..5}; do
        TUNNEL_URL=$(gcloud compute ssh "$VM_NAME" --zone="$ZONE" --command="
            # Buscar URL en los logs de systemd
            TUNNEL_URL=\$(sudo journalctl -u cloudflared -n 200 --no-pager 2>/dev/null | grep -o 'https://[a-z0-9-]*\.trycloudflare\.com' | tail -1)
            if [ ! -z \"\$TUNNEL_URL\" ]; then
                echo \"\$TUNNEL_URL\"
                echo \"\$TUNNEL_URL\" | sudo tee /tmp/tunnel_url.txt > /dev/null
            fi
        " 2>/dev/null | grep -o 'https://[a-z0-9-]*\.trycloudflare\.com' | head -1)
        
        if [ ! -z "$TUNNEL_URL" ]; then
            break
        fi
        
        if [ $attempt -lt 5 ]; then
            echo -e "${YELLOW}   Intento $attempt/5: URL aún no disponible, esperando 10 segundos más...${NC}"
            sleep 10
        fi
    done
    
    echo ""
    echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${GREEN}✅ Cloudflare Tunnel configurado!${NC}"
    echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo ""
    
    if [ ! -z "$TUNNEL_URL" ]; then
        echo -e "${GREEN}🌐 URL HTTPS del Backend (para tu frontend en Vercel):${NC}"
        echo -e "   ${GREEN}$TUNNEL_URL${NC}"
        echo ""
        echo -e "${BLUE}📝 Actualiza tu variable de entorno en Vercel:${NC}"
        echo -e "   ${YELLOW}NEXT_PUBLIC_API_URL=$TUNNEL_URL${NC}"
        echo ""
    else
        echo -e "${YELLOW}⚠️  URL del tunnel aún no disponible. Espera 30 segundos y ejecuta:${NC}"
        echo -e "   ${YELLOW}./get-tunnel-url.sh${NC}"
        echo ""
    fi
    
    echo -e "${BLUE}📝 Comandos útiles:${NC}"
    echo -e "   • Ver logs del tunnel: ${YELLOW}gcloud compute ssh $VM_NAME --zone=$ZONE --command='sudo journalctl -u cloudflared -f'${NC}"
    echo -e "   • Ver estado del tunnel: ${YELLOW}gcloud compute ssh $VM_NAME --zone=$ZONE --command='sudo systemctl status cloudflared'${NC}"
    echo -e "   • Reiniciar tunnel: ${YELLOW}gcloud compute ssh $VM_NAME --zone=$ZONE --command='sudo systemctl restart cloudflared'${NC}"
    echo -e "   • Obtener URL: ${YELLOW}./get-tunnel-url.sh${NC}"
    echo ""
else
    echo -e "${RED}❌ Error al configurar Cloudflare Tunnel${NC}"
    exit 1
fi

