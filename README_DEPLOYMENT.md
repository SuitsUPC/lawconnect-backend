# 🚀 Despliegue LawConnect en Azure VM

## 📋 Paso 1: Ejecutar script de despliegue

```bash
cd ~/lawconnect-backend
bash deploy-azure.sh
```

**¡Listo!** El script instala todo automáticamente.

## ✅ Paso 2: Probar

```bash
# Ver contenedores
cd ~/lawconnect-backend/microservices
sudo docker-compose ps

# Probar API
curl http://localhost/api/v1/users

# Swagger en navegador
http://TU_IP_AZURE/swagger-ui.html
```

## 📊 Arquitectura

```
Internet → Nginx:80 → API Gateway:8080 → Microservicios
                                    ├─ IAM:8081
                                    ├─ Profiles:8082
                                    └─ Cases:8083
```

## 🔄 Comandos útiles

```bash
# Ver logs
cd ~/lawconnect-backend/microservices
sudo docker-compose logs -f

# Reiniciar
sudo docker-compose restart

# Reconstruir
sudo docker-compose down
sudo docker-compose up -d --build
```

## 🆘 Problemas

```bash
# Ver qué falla
sudo docker-compose logs -f
sudo tail -f /var/log/nginx/lawconnect_error.log

# Reiniciar Docker
sudo systemctl restart docker
```

---

**¡Listo!** Accede a `http://TU_IP_AZURE/swagger-ui.html` 🎉

