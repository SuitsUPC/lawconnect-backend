# Instrucciones: Configurar Cloudflare Tunnel

## ⚠️ Si estás viendo la pantalla "Select zone"

**NO necesitas seleccionar una zona.** Tienes 3 opciones:

### Opción 1: Cancelar y usar método simple (RECOMENDADO)

1. **Presiona Ctrl+C** en la terminal donde ejecutaste el comando
2. Ejecuta este comando (método simple sin autenticación):

```bash
gcloud compute ssh lawconnect-vm --zone=southamerica-east1-a --command="nohup cloudflared tunnel --url http://localhost:8080 > /tmp/cloudflared.log 2>&1 & sleep 8 && grep -o 'https://[a-z0-9-]*\.trycloudflare\.com' /tmp/cloudflared.log | head -1"
```

Esto te dará una URL como: `https://xxxxx.trycloudflare.com`

### Opción 2: Buscar botón "Skip" o "Cancel"

En la pantalla de Cloudflare, busca un botón que diga:
- "Skip"
- "Cancel" 
- "Try without domain"
- "Continue without domain"

### Opción 3: Cerrar la ventana

Simplemente cierra la ventana del navegador. El proceso se cancelará.

## ✅ Después de obtener la URL

1. **Copia la URL** que te dio cloudflared (ej: `https://xxxxx.trycloudflare.com`)
2. **Actualiza tu frontend:**
   ```
   NEXT_PUBLIC_API_URL=https://xxxxx.trycloudflare.com
   ```
3. **Prueba desde tu frontend** - debería funcionar sin errores de certificado

## 🔄 Para hacer el tunnel permanente

Si quieres que el tunnel se mantenga después de reiniciar la VM, necesitas configurarlo como servicio. Pero primero, cancela el proceso actual.

