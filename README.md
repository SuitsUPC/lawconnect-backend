# LawConnect Backend

Backend de LawConnect - Plataforma para conectar abogados y clientes en la gestión de casos legales.

## 🏗️ Arquitectura

El proyecto ha migrado de una arquitectura monolítica a **microservicios independientes**:

```
LawConnect Backend
├── iam-service          (Identity & Access Management) - Puerto 8081
├── profiles-service     (Lawyers & Clients Profiles) - Puerto 8082
├── cases-service        (Cases Management) - Puerto 8083
└── api-gateway          (Request Routing & API Gateway) - Puerto 8080
```

## 📋 Requisitos Previos

Antes de clonar y ejecutar este proyecto, asegúrate de tener instalado:

1. **Java 17** o superior
   - Verifica: `java -version`
   - Descarga: https://adoptium.net/

2. **Docker Desktop** (con WSL2 en Windows)
   - Descarga: https://www.docker.com/products/docker-desktop/
   - **Importante en Windows**: Ejecuta `wsl --update` antes de iniciar Docker Desktop

3. **Maven** (opcional, el proyecto incluye Maven Wrapper)
   - Verifica: `./mvnw -v`

## 🚀 Instalación y Ejecución

### Usando Docker (Recomendado)

1. **Clona el repositorio**:
   ```bash
   git clone https://github.com/tu-usuario/lawconnect-backend.git
   cd lawconnect-backend
   ```

2. **Compila los microservicios**:
   ```bash
   # IAM Service
   cd microservices/iam
   ../../mvnw clean package spring-boot:repackage -DskipTests
   
   # Profiles Service
   cd ../profiles
   ../../mvnw clean package spring-boot:repackage -DskipTests
   
   # Cases Service
   cd ../cases
   ../../mvnw clean package spring-boot:repackage -DskipTests
   
   # API Gateway
   cd ../api-gateway
   ../../mvnw clean package spring-boot:repackage -DskipTests
   
   # Volver a la raíz
   cd ../..
   ```

3. **Levanta todos los servicios con Docker**:
   ```bash
   docker compose -f microservices/docker-compose.yml up --build -d
   ```

4. **Verifica que todos los contenedores estén corriendo**:
   ```bash
   docker compose -f microservices/docker-compose.yml ps
   ```

5. **Accede a Swagger UI**:
   - **API Gateway (punto de entrada principal)**: http://localhost:8080/swagger-ui.html
   - IAM Service: http://localhost:8081/swagger-ui.html
   - Profiles Service: http://localhost:8082/swagger-ui.html
   - Cases Service: http://localhost:8083/swagger-ui.html

## 🎯 Bounded Contexts

### 1. Identity & Access Management (IAM)
Responsable de seguridad, usuarios y roles.
- Registro de usuarios (Sign-Up)
- Inicio de sesión (Sign-In) y emisión de JWT
- Gestión de usuarios y roles
- Autorización mediante Spring Security

### 2. Profiles
Gestión de perfiles de abogados y clientes.
- CRUD de **ClientProfile** y **LawyerProfile**
- Gestión de especialidades legales
- Búsquedas por DNI y especialidad

### 3. Cases
Núcleo de la plataforma para gestión de casos legales.
- **Creación de casos** por clientes
- **Invitaciones** a abogados para unirse a casos
- **Aplicaciones** de abogados a casos abiertos
- **Aceptación/Rechazo** de invitaciones y aplicaciones
- **Estados de caso**: Abierto → En evaluación → Aceptado → Cerrado/Cancelado
- **Comentarios** (generales y finales) con validación de flujo
- **Consultas**: casos por cliente, abogado, estado, timeline, comentarios e invitaciones

### 4. API Gateway
Punto de entrada único para todas las peticiones.
- Enrutamiento de requests a los microservicios
- Balanceo de carga
- Documentación unificada de APIs

## 📚 Documentación API

Cada microservicio expone su documentación OpenAPI/Swagger:
- `/swagger-ui.html` - Interfaz interactiva
- `/v3/api-docs` - Especificación JSON

### Endpoints Principales

**A través del API Gateway (Puerto 8080)**:

#### Autenticación (IAM)
- `POST /api/v1/authentication/sign-up` - Registro de usuario
- `POST /api/v1/authentication/sign-in` - Inicio de sesión
- `GET /api/v1/users` - Listar usuarios
- `GET /api/v1/roles` - Listar roles

#### Perfiles (Profiles)
- `GET /api/v1/lawyers` - Listar abogados
- `POST /api/v1/lawyers` - Crear abogado
- `GET /api/v1/clients` - Listar clientes
- `POST /api/v1/clients` - Crear cliente
- `GET /api/v1/lawyer-specialties` - Listar especialidades

#### Casos (Cases)
- `GET /api/v1/cases` - Listar casos
- `POST /api/v1/cases` - Crear caso
- `GET /api/v1/applications` - Listar aplicaciones
- `POST /api/v1/invitations` - Invitar abogado
- `POST /api/v1/comments` - Crear comentario

## 🛠️ Comandos Útiles

### Docker Compose

```bash
# Ver logs de todos los servicios
docker compose -f microservices/docker-compose.yml logs -f

# Ver logs de un servicio específico
docker compose -f microservices/docker-compose.yml logs -f iam-service

# Detener todos los servicios
docker compose -f microservices/docker-compose.yml down

# Detener y eliminar volúmenes (resetear bases de datos)
docker compose -f microservices/docker-compose.yml down -v

# Reiniciar un servicio específico
docker compose -f microservices/docker-compose.yml restart iam-service
```

### Maven

```bash
# Compilar todos los microservicios desde la raíz
./mvnw clean install -DskipTests

# Compilar un microservicio específico
cd microservices/iam
../../mvnw clean package spring-boot:repackage -DskipTests

# Ejecutar tests
../../mvnw test
```

## 🐛 Solución de Problemas

### Docker Desktop no arranca
**Error**: `Docker Desktop is unable to start`

**Solución**:
1. Abre PowerShell como Administrador
2. Ejecuta: `wsl --update`
3. Reinicia tu PC
4. Abre Docker Desktop

### Error "no main manifest attribute"
**Causa**: El JAR no fue empaquetado correctamente como Spring Boot fat jar.

**Solución**:
```bash
cd microservices/[servicio]
../../mvnw clean package spring-boot:repackage -DskipTests
```

### Los contenedores se reinician constantemente
1. Revisa los logs: `docker compose -f microservices/docker-compose.yml logs [servicio]`
2. Verifica que la base de datos esté levantada y saludable
3. Asegúrate de que los JARs estén correctamente empaquetados

### No puedo acceder a Swagger
1. Verifica que el servicio esté corriendo: `docker compose -f microservices/docker-compose.yml ps`
2. Prueba la URL alternativa sin `.html`: http://localhost:8080/swagger-ui/
3. Revisa los logs del servicio para ver si hay errores

## 🔧 Tecnologías

- **Java 17**
- **Spring Boot 3.4.3**
- **Spring Cloud Gateway 2024.0.0** (API Gateway)
- **Spring Security** (Autenticación y autorización)
- **JWT** (JSON Web Tokens)
- **Spring Data JPA** (Persistencia)
- **MySQL 8.0** (Base de datos - una instancia por servicio)
- **Docker & Docker Compose** (Containerización)
- **Maven** (Gestión de dependencias)
- **Swagger/OpenAPI** (Documentación de API)

## 🗄️ Base de Datos

Cada microservicio tiene su propia base de datos (Database per Service Pattern):
- `iam-db` - Puerto 3306
- `profiles-db` - Puerto 3307
- `cases-db` - Puerto 3308

Las bases de datos se crean automáticamente al levantar Docker Compose. Si necesitas resetearlas:

```bash
docker compose -f microservices/docker-compose.yml down -v
docker compose -f microservices/docker-compose.yml up -d
```

## 🧪 Testing

```bash
# Ejecutar todos los tests
./mvnw test

# Ejecutar tests de un microservicio específico
cd microservices/iam
../../mvnw test
```

## 📦 Build para Producción

```bash
# Compilar todos los microservicios
./mvnw clean package -DskipTests

# Construir imágenes Docker
docker compose -f microservices/docker-compose.yml build

# Opcional: Etiquetar y subir a Docker Registry
docker tag microservices-iam-service:latest tu-usuario/iam-service:latest
docker push tu-usuario/iam-service:latest
```

## 🤝 Contribución

1. Haz fork del proyecto
2. Crea una rama para tu feature: `git checkout -b feature/nueva-funcionalidad`
3. Commit tus cambios: `git commit -am 'Añade nueva funcionalidad'`
4. Push a la rama: `git push origin feature/nueva-funcionalidad`
5. Crea un Pull Request

## 👥 Equipo

Desarrollado por **LawConnect Team**.

## 📞 Soporte

Si tienes problemas al configurar el proyecto:
1. Revisa la sección "Solución de Problemas" arriba
2. Verifica los logs de Docker: `docker compose -f microservices/docker-compose.yml logs`
3. Asegúrate de tener Docker Desktop corriendo
4. Abre un issue en GitHub

---

**¡Listo para empezar!** 🎉

Una vez que hayas seguido los pasos de instalación, tu backend estará corriendo en:
- **API Gateway**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
