# 🚀 Despliegue LawConnect en Azure VM

## ✅ Listo: Todo configurado

Solo ejecuta esto en tu VM:

```bash
cd ~/lawconnect-backend
git pull

# Detener servicios actuales
cd microservices
sudo docker-compose down

# Reconstruir JARs correctamente  
cd ~/lawconnect-backend/microservices/iam
mvn clean package spring-boot:repackage -DskipTests

cd ../profiles
mvn clean package spring-boot:repackage -DskipTests

cd ../cases
mvn clean package spring-boot:repackage -DskipTests

cd ../api-gateway
mvn clean package spring-boot:repackage -DskipTests

cd ..

# Levantar servicios
sudo docker-compose up -d --build

# Esperar y verificar
sleep 60
sudo docker-compose ps
```

## 🎯 ¿Las bases de datos?

**SÍ**, se crean automáticamente:
- MySQL crea las databases: `iam-db`, `profiles-db`, `cases-db` 
- Hibernate crea las tablas: `spring.jpa.hibernate.ddl-auto=update`

## ✅ Verificar

```bash
# Estado de servicios
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

**Después de ejecutar los comandos, espera 60 segundos y verifica con `sudo docker-compose ps`**
