# Configurar Cloudflare como Proxy HTTPS (Solución Rápida Sin Dominio)

## Opción 1: Usar Cloudflare Tunnel (Recomendado - Gratis)

Cloudflare Tunnel permite exponer tu backend sin necesidad de un dominio propio ni abrir puertos.

### Pasos:

1. **Registrarse en Cloudflare** (gratis): https://dash.cloudflare.com/sign-up

2. **Instalar cloudflared en la VM:**
```bash
gcloud compute ssh lawconnect-vm --zone=southamerica-east1-a --command="
    curl -L https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-linux-amd64 -o /tmp/cloudflared
    sudo mv /tmp/cloudflared /usr/local/bin/cloudflared
    sudo chmod +x /usr/local/bin/cloudflared
"
```

3. **Autenticarse con Cloudflare:**
```bash
gcloud compute ssh lawconnect-vm --zone=southamerica-east1-a --command="cloudflared tunnel login"
```

4. **Crear un tunnel:**
```bash
gcloud compute ssh lawconnect-vm --zone=southamerica-east1-a --command="cloudflared tunnel create lawconnect-backend"
```

5. **Configurar el tunnel:**
```bash
gcloud compute ssh lawconnect-vm --zone=southamerica-east1-a --command="
    sudo mkdir -p /etc/cloudflared
    sudo tee /etc/cloudflared/config.yml > /dev/null <<'EOF'
tunnel: <TUNNEL_ID>
credentials-file: /root/.cloudflared/<TUNNEL_ID>.json

ingress:
  - hostname: lawconnect-backend.your-account.workers.dev
    service: http://localhost:8080
  - service: http_status:404
EOF
"
```

6. **Instalar como servicio:**
```bash
gcloud compute ssh lawconnect-vm --zone=southamerica-east1-a --command="
    sudo cloudflared service install
    sudo systemctl start cloudflared
    sudo systemctl enable cloudflared
"
```

7. **Obtener la URL:**
```bash
gcloud compute ssh lawconnect-vm --zone=southamerica-east1-a --command="cloudflared tunnel route dns lawconnect-backend"
```

## Opción 2: Usar un Dominio Temporal Gratis

### Usar DuckDNS (Gratis):

1. **Registrarse en DuckDNS**: https://www.duckdns.org/

2. **Crear un subdominio**: ej. `lawconnect-backend.duckdns.org`

3. **Configurar DNS para que apunte a tu IP**: `35.247.254.52`

4. **Configurar Let's Encrypt en la VM:**
```bash
gcloud compute ssh lawconnect-vm --zone=southamerica-east1-a --command="
    sudo certbot --nginx -d lawconnect-backend.duckdns.org --non-interactive --agree-tos --email tu-email@example.com
"
```

## Opción 3: Usar un Dominio Propio

Si tienes un dominio propio:

1. **Configurar DNS**: Apunta un subdominio (ej. `api.tudominio.com`) a `35.247.254.52`

2. **Configurar Let's Encrypt:**
```bash
gcloud compute ssh lawconnect-vm --zone=southamerica-east1-a --command="
    sudo certbot --nginx -d api.tudominio.com --non-interactive --agree-tos --email tu-email@example.com
"
```

3. **Actualizar Nginx** para usar el certificado de Let's Encrypt (se hace automáticamente)

4. **Actualizar frontend**: `NEXT_PUBLIC_API_URL=https://api.tudominio.com`

