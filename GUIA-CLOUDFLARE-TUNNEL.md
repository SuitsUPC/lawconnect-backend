# Guía: Configurar Cloudflare Tunnel para el Backend

## ⚠️ IMPORTANTE

**NO uses el dominio de Vercel** (`lawconnect-frontend.vercel.app`) - ese dominio pertenece a Vercel.

**Cloudflare Tunnel NO requiere dominio propio** - crea una URL automáticamente.

## 🚀 Opción 1: Cloudflare Tunnel (Recomendado - Sin dominio)

### Paso 1: Crear cuenta en Cloudflare (si no tienes)
1. Ve a: https://dash.cloudflare.com/sign-up
2. Crea una cuenta gratuita

### Paso 2: Instalar cloudflared en la VM
```bash
gcloud compute ssh lawconnect-vm --zone=southamerica-east1-a --command="
    curl -L https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-linux-amd64 -o /tmp/cloudflared
    sudo mv /tmp/cloudflared /usr/local/bin/cloudflared
    sudo chmod +x /usr/local/bin/cloudflared
"
```

### Paso 3: Autenticarse con Cloudflare
```bash
gcloud compute ssh lawconnect-vm --zone=southamerica-east1-a --command="cloudflared tunnel login"
```
Esto abrirá una ventana del navegador para autenticarte.

### Paso 4: Crear el tunnel
```bash
gcloud compute ssh lawconnect-vm --zone=southamerica-east1-a --command="cloudflared tunnel create lawconnect-backend"
```

### Paso 5: Configurar el tunnel
```bash
gcloud compute ssh lawconnect-vm --zone=southamerica-east1-a --command="
    TUNNEL_ID=\$(cloudflared tunnel list | grep lawconnect-backend | awk '{print \$1}')
    sudo mkdir -p /etc/cloudflared
    sudo tee /etc/cloudflared/config.yml > /dev/null <<EOF
tunnel: \$TUNNEL_ID
credentials-file: /root/.cloudflared/\$TUNNEL_ID.json

ingress:
  - service: http://localhost:8080
EOF
"
```

### Paso 6: Ejecutar el tunnel
```bash
gcloud compute ssh lawconnect-vm --zone=southamerica-east1-a --command="
    sudo cloudflared service install
    sudo systemctl start cloudflared
    sudo systemctl enable cloudflared
"
```

### Paso 7: Obtener la URL
```bash
gcloud compute ssh lawconnect-vm --zone=southamerica-east1-a --command="cloudflared tunnel route dns list"
```

O ve al dashboard: https://dash.cloudflare.com -> Zero Trust -> Access -> Tunnels

## 🌐 Opción 2: Si quieres usar un dominio propio

Si realmente quieres usar un dominio propio con Cloudflare:

1. **Registra un dominio** (ej: en Namecheap, Google Domains, etc.)
2. **En Cloudflare**, cuando te pida el dominio, ingresa el que registraste
3. **Configura los DNS** para que apunten a tu IP: `35.247.254.52`
4. **Usa Let's Encrypt** en la VM (ya tenemos el script)

Pero esto es más complejo y requiere pagar por un dominio.

## ✅ Recomendación

**Usa Cloudflare Tunnel** - es más fácil, gratis, y no requiere dominio.

