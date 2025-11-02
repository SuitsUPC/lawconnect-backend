# 🚀 Despliegue LawConnect en Azure VM

## ✅ Despliegue automático (RECOMENDADO)

```bash
# Desde la raíz del proyecto
cd ~/lawconnect-backend
bash deploy-azure.sh
```

**¡Eso es todo!** El script hace todo automáticamente.

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
