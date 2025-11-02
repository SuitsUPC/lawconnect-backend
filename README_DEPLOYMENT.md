# 🚀 Despliegue LawConnect en Azure VM

## ✅ Despliegue automático (RECOMENDADO)

```bash
# Desde la raíz del proyecto
cd ~/lawconnect-backend
bash deploy-azure.sh
```

**¡Eso es todo!** El script hace todo automáticamente.

## 🌐 Configurar Azure Network Security Group (REQUERIDO)

**URGENTE**: Sin esta configuración NO podrás acceder desde Internet:

### Opción 1: Desde Azure Portal
1. Ve a https://portal.azure.com
2. Busca tu VM: `vm-lawconnectbackend-prod-001`
3. Click en "Networking" → "Add inbound port rule"
4. Configura:
   - **Source**: Any
   - **Source port ranges**: *
   - **Destination**: Any
   - **Destination port ranges**: 80
   - **Protocol**: TCP
   - **Action**: Allow
   - **Priority**: 1000
   - **Name**: AllowHTTP80
5. Click "Add"

### Opción 2: Desde terminal (si tienes Azure CLI)
```bash
az network nsg rule create \
  --resource-group tu-resource-group \
  --nsg-name tu-nsg-name \
  --name AllowHTTP \
  --priority 1000 \
  --protocol Tcp \
  --destination-port-ranges 80 \
  --access Allow
```

**TU IP PÚBLICA**: `20.150.212.89`

**URLs para probar**:
- http://20.150.212.89/swagger-ui.html
- http://20.150.212.89/api/v1/users
- http://20.150.212.89/health

## 🎯 ¿Las bases de datos?

**SÍ**, se crean automáticamente:
- MySQL crea las databases: `iam-db`, `profiles-db`, `cases-db` 
- Hibernate crea las tablas: `spring.jpa.hibernate.ddl-auto=update`

## ✅ Verificar después del despliegue

```bash
# Estado de servicios
cd ~/lawconnect-backend/microservices
sudo docker-compose ps

# Logs
sudo docker-compose logs -f

# Probar API
curl http://localhost/api/v1/users

# Swagger
http://TU_IP_AZURE/swagger-ui.html
```

## 🔍 Si hay problemas

```bash
# Ver logs de un servicio
sudo docker-compose logs iam-service | tail -100

# Reiniciar
sudo docker-compose restart iam-service

# Ver errores
sudo docker-compose logs | grep -i error
```

---

**Después de ejecutar el script, espera 60 segundos y verifica con `sudo docker-compose ps`**
