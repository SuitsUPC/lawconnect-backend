# Análisis: Relación entre Usuarios y Perfiles

## Resumen

Este documento explica cómo funciona la relación entre usuarios y perfiles en LawConnect, y los problemas encontrados al desplegar en la nube.

## Arquitectura

### Flujo de Creación de Usuario

1. **Registro de Usuario** (`POST /api/v1/authentication/sign-up`)
   - El usuario se registra con username, password y role
   - El servicio IAM crea el usuario en la base de datos `iam-db`
   - Se genera un `UserAggregate` con un UUID único

2. **Aprovisionamiento Automático de Perfil**
   - Después de crear el usuario, el `UserCommandServiceImpl` llama a `ProfileProvisioningService.provisionProfileFor()`
   - Este servicio hace una llamada HTTP al servicio de perfiles para crear un perfil básico
   - El tipo de perfil depende del rol:
     - `ROLE_LAWYER` → Crea un `LawyerAggregate` en `/api/v1/lawyers`
     - `ROLE_CLIENT` → Crea un `ClientAggregate` en `/api/v1/clients`

### Relación Usuario-Perfil

- **Usuario (IAM Service)**: Almacenado en `iam-db.users`
  - Contiene: `id` (UUID), `username`, `password` (hasheado), `role_id`
  
- **Perfil (Profiles Service)**: Almacenado en `profiles-db`
  - `LawyerAggregate` en `lawyer_profiles`
  - `ClientAggregate` en `client_profiles`
  - Ambos tienen un campo `userId` que referencia al UUID del usuario

**La relación es por UUID**: El `userId` en el perfil es el mismo `id` del usuario.

## Problemas Identificados

### 1. Configuración de URL del Servicio de Perfiles

**Problema**: En `docker-compose.yml`, el servicio IAM no tenía configurada explícitamente la variable de entorno `PROFILES_SERVICE_URL`.

**Solución**: Se agregó la variable de entorno en el servicio IAM:
```yaml
environment:
  PROFILES_SERVICE_URL: http://profiles-service:8082
```

### 2. Dependencias entre Servicios

**Problema**: El servicio IAM no esperaba a que el servicio de perfiles estuviera listo antes de iniciar.

**Solución**: Se agregó una dependencia en `docker-compose.yml`:
```yaml
depends_on:
  iam-db:
    condition: service_healthy
  profiles-service:
    condition: service_started
```

### 3. Logging Insuficiente

**Problema**: Los errores al crear perfiles se silenciaban, dificultando el diagnóstico.

**Solución**: Se mejoró el logging en:
- `ProfileProvisioningService`: Ahora registra intentos, éxitos y errores detallados
- `ProfilesServiceClientConfiguration`: Registra la URL configurada al iniciar

## Configuración Local vs Nube

### Local (Docker Compose)

- **URL del servicio de perfiles**: `http://profiles-service:8082`
- Los servicios se comunican a través de la red Docker `lawconnect-network`
- El nombre del servicio (`profiles-service`) se resuelve por DNS interno de Docker

### Nube (Cloudflare Tunnel)

- **URL del servicio de perfiles**: Debe ser la misma `http://profiles-service:8082` si están en la misma red Docker
- Si están en contenedores separados, puede necesitar la URL externa o IP interna
- **IMPORTANTE**: Verificar que los servicios puedan comunicarse entre sí

## Cómo Probar

### 1. Probar en Local

```bash
# Iniciar servicios
./start.sh

# Ejecutar script de prueba
./test-user-profile.sh local
```

### 2. Verificar Logs

```bash
# Ver logs del servicio IAM (buscar mensajes sobre perfiles)
docker logs iam-service | grep -i profile

# Ver logs del servicio de perfiles
docker logs profiles-service | tail -50
```

### 3. Probar en Nube

```bash
# Ejecutar script de prueba apuntando a la nube
./test-user-profile.sh cloud https://garcia-guardian-yields-editorial.trycloudflare.com
```

## Endpoints Relacionados

### Crear Usuario (automáticamente crea perfil)
```
POST /api/v1/authentication/sign-up
Body: {
  "username": "test_user",
  "password": "password123",
  "role": "ROLE_CLIENT" o "ROLE_LAWYER"
}
```

### Obtener Perfil de Cliente
```
GET /api/v1/clients/{userId}
Headers: Authorization: Bearer {token}
```

### Obtener Perfil de Abogado
```
GET /api/v1/lawyers/{userId}
Headers: Authorization: Bearer {token}
```

## Troubleshooting

### El usuario se crea pero el perfil NO

1. **Verificar logs del IAM Service**:
   ```bash
   docker logs iam-service | grep -i "profile\|Failed to provision"
   ```

2. **Verificar que el Profiles Service esté corriendo**:
   ```bash
   docker ps | grep profiles-service
   ```

3. **Verificar conectividad entre servicios**:
   ```bash
   docker exec iam-service curl -v http://profiles-service:8082/api/v1/lawyer-specialties
   ```

4. **Verificar la URL configurada**:
   ```bash
   docker logs iam-service | grep "Configuring Profiles Service RestClient"
   ```

### Error: Connection refused

- El servicio de perfiles no está disponible
- Verificar que esté corriendo: `docker ps`
- Verificar la red Docker: `docker network inspect microservices_lawconnect-network`

### Error: 404 Not Found

- El endpoint del servicio de perfiles no existe
- Verificar que el Profiles Service esté exponiendo `/api/v1/clients` y `/api/v1/lawyers`

## Cambios Realizados

1. ✅ Agregada variable `PROFILES_SERVICE_URL` en `docker-compose.yml`
2. ✅ Agregada dependencia de `profiles-service` en el servicio IAM
3. ✅ Mejorado logging en `ProfileProvisioningService`
4. ✅ Agregado logging en `ProfilesServiceClientConfiguration`
5. ✅ Creado script de prueba `test-user-profile.sh`

## Próximos Pasos

1. Probar en local con los cambios
2. Verificar logs para confirmar que los perfiles se crean correctamente
3. Desplegar en nube y probar
4. Si persisten problemas en nube, verificar:
   - Configuración de red entre servicios
   - Variables de entorno en el despliegue
   - Health checks de los servicios

