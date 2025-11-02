# Microservices Documentation Evidence for Sprint Review

## Introducción

Durante este Sprint, se logró implementar y desplegar exitosamente la documentación completa de todos los microservicios utilizando **OpenAPI 3.0** y **Swagger UI**. Se documentaron todos los endpoints de los tres microservicios principales (IAM, Profiles, y Cases), así como el API Gateway que actúa como punto de entrada único para todas las solicitudes.


### URL de Documentación Desplegada

**Swagger UI Principal:** http://20.150.212.89/webjars/swagger-ui/index.html

Desde esta URL, los usuarios pueden seleccionar entre las siguientes definiciones de API:
- **API Gateway** - Documentación del gateway y enrutamiento
- **IAM Service** - Servicio de Identidad y Gestión de Acceso
- **Profiles Service** - Servicio de Gestión de Perfiles (Abogados y Clientes)
- **Cases Service** - Servicio de Gestión de Casos

---

## Arquitectura de Enrutamiento

La plataforma utiliza **Nginx** como reverse proxy en el puerto 80, recibiendo todas las peticiones externas y redirigiéndolas al **API Gateway** (Spring Cloud Gateway) en el puerto 8080. El API Gateway enruta las peticiones a los microservicios correspondientes según el patrón de ruta.

**Flujo de Enrutamiento:**
```
Cliente → Nginx:80 → API Gateway:8080 → Microservicio (8081/8082/8083)
```

**Puertos de Microservicios:**
- **IAM Service:** Puerto 8081
- **Profiles Service:** Puerto 8082
- **Cases Service:** Puerto 8083
- **API Gateway:** Puerto 8080

**Rutas principales:**
- `/api/v1/authentication/**`, `/api/v1/users/**`, `/api/v1/roles/**` → IAM Service (8081)
- `/api/v1/lawyers/**`, `/api/v1/clients/**`, `/api/v1/lawyer-specialties/**` → Profiles Service (8082)
- `/api/v1/cases/**`, `/api/v1/applications/**`, `/api/v1/invitations/**`, `/api/v1/comments/**` → Cases Service (8083)

---

## Tabla de Endpoints Documentados

| # | Microservicio | Endpoint | Método HTTP | Descripción | URL Documentación |
|---|---------------|----------|-------------|-------------|-------------------|
| 1 | IAM | `/api/v1/authentication/sign-up` | POST | Registrar nuevo usuario | [IAM Service Docs](http://20.150.212.89/webjars/swagger-ui/index.html?urls.primaryName=IAM+Service) |
| 2 | IAM | `/api/v1/authentication/sign-in` | POST | Autenticar usuario | [IAM Service Docs](http://20.150.212.89/webjars/swagger-ui/index.html?urls.primaryName=IAM+Service) |
| 3 | IAM | `/api/v1/users` | GET | Obtener todos los usuarios | [IAM Service Docs](http://20.150.212.89/webjars/swagger-ui/index.html?urls.primaryName=IAM+Service) |
| 4 | IAM | `/api/v1/users/{userId}` | GET | Obtener usuario por ID | [IAM Service Docs](http://20.150.212.89/webjars/swagger-ui/index.html?urls.primaryName=IAM+Service) |
| 5 | IAM | `/api/v1/roles` | GET | Obtener todos los roles | [IAM Service Docs](http://20.150.212.89/webjars/swagger-ui/index.html?urls.primaryName=IAM+Service) |
| 6 | Profiles | `/api/v1/lawyers` | POST | Crear perfil de abogado | [Profiles Service Docs](http://20.150.212.89/webjars/swagger-ui/index.html?urls.primaryName=Profiles+Service) |
| 7 | Profiles | `/api/v1/lawyers/{userId}` | GET | Obtener perfil de abogado por User ID | [Profiles Service Docs](http://20.150.212.89/webjars/swagger-ui/index.html?urls.primaryName=Profiles+Service) |
| 8 | Profiles | `/api/v1/clients` | POST | Crear perfil de cliente | [Profiles Service Docs](http://20.150.212.89/webjars/swagger-ui/index.html?urls.primaryName=Profiles+Service) |
| 9 | Profiles | `/api/v1/clients/{userId}` | GET | Obtener perfil de cliente por User ID | [Profiles Service Docs](http://20.150.212.89/webjars/swagger-ui/index.html?urls.primaryName=Profiles+Service) |
| 10 | Profiles | `/api/v1/lawyer-specialties` | GET | Obtener todas las especialidades | [Profiles Service Docs](http://20.150.212.89/webjars/swagger-ui/index.html?urls.primaryName=Profiles+Service) |
| 11 | Cases | `/api/v1/cases` | POST | Crear un nuevo caso | [Cases Service Docs](http://20.150.212.89/webjars/swagger-ui/index.html?urls.primaryName=Cases+Service) |
| 12 | Cases | `/api/v1/cases` | GET | Obtener todos los casos | [Cases Service Docs](http://20.150.212.89/webjars/swagger-ui/index.html?urls.primaryName=Cases+Service) |
| 13 | Cases | `/api/v1/cases/{caseId}` | GET | Obtener caso por ID | [Cases Service Docs](http://20.150.212.89/webjars/swagger-ui/index.html?urls.primaryName=Cases+Service) |
| 14 | Cases | `/api/v1/cases/{caseId}/close` | PUT | Cerrar un caso | [Cases Service Docs](http://20.150.212.89/webjars/swagger-ui/index.html?urls.primaryName=Cases+Service) |
| 15 | Cases | `/api/v1/cases/{caseId}/cancel` | PUT | Cancelar un caso | [Cases Service Docs](http://20.150.212.89/webjars/swagger-ui/index.html?urls.primaryName=Cases+Service) |
| 16 | Cases | `/api/v1/cases/clients/{clientId}` | GET | Obtener casos por cliente | [Cases Service Docs](http://20.150.212.89/webjars/swagger-ui/index.html?urls.primaryName=Cases+Service) |
| 17 | Cases | `/api/v1/cases/suggested` | GET | Obtener casos sugeridos para abogado | [Cases Service Docs](http://20.150.212.89/webjars/swagger-ui/index.html?urls.primaryName=Cases+Service) |
| 18 | Cases | `/api/v1/applications` | POST | Enviar aplicación para caso | [Cases Service Docs](http://20.150.212.89/webjars/swagger-ui/index.html?urls.primaryName=Cases+Service) |
| 19 | Cases | `/api/v1/applications/{applicationId}/accept` | PUT | Aceptar aplicación | [Cases Service Docs](http://20.150.212.89/webjars/swagger-ui/index.html?urls.primaryName=Cases+Service) |
| 20 | Cases | `/api/v1/applications/{applicationId}/reject` | PUT | Rechazar aplicación | [Cases Service Docs](http://20.150.212.89/webjars/swagger-ui/index.html?urls.primaryName=Cases+Service) |
| 21 | Cases | `/api/v1/invitations` | POST | Enviar invitación | [Cases Service Docs](http://20.150.212.89/webjars/swagger-ui/index.html?urls.primaryName=Cases+Service) |
| 22 | Cases | `/api/v1/invitations/{invitationId}/accept` | PUT | Aceptar invitación | [Cases Service Docs](http://20.150.212.89/webjars/swagger-ui/index.html?urls.primaryName=Cases+Service) |
| 23 | Cases | `/api/v1/comments` | GET | Obtener comentarios | [Cases Service Docs](http://20.150.212.89/webjars/swagger-ui/index.html?urls.primaryName=Cases+Service) |
| 24 | Cases | `/api/v1/comments/general` | POST | Crear comentario general | [Cases Service Docs](http://20.150.212.89/webjars/swagger-ui/index.html?urls.primaryName=Cases+Service) |
| 25 | Cases | `/api/v1/comments/final` | POST | Crear comentario final | [Cases Service Docs](http://20.150.212.89/webjars/swagger-ui/index.html?urls.primaryName=Cases+Service) |

---

## Detalle de Endpoints por Microservicio

### IAM Service - Identidad y Gestión de Acceso

#### 1. POST `/api/v1/authentication/sign-up`
**Descripción:** Registra un nuevo usuario en el sistema.

**Sintaxis:**
```http
POST http://20.150.212.89/api/v1/authentication/sign-up
Content-Type: application/json
```

**Parámetros:**
- **Body (JSON):**
  ```json
  {
    "username": "string",
    "password": "string",
    "role": "ROLE_ADMIN | ROLE_LAWYER | ROLE_CLIENT"
  }
  ```

**Ejemplo de Request:**
```json
{
  "username": "juan.perez",
  "password": "password123",
  "role": "ROLE_CLIENT"
}
```

**Ejemplo de Response (201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "username": "juan.perez",
  "role": "ROLE_CLIENT"
}
```

**Códigos de Respuesta:**
- `201`: Usuario creado exitosamente
- `400`: Datos inválidos

---

#### 2. POST `/api/v1/authentication/sign-in`
**Descripción:** Autentica un usuario existente y retorna un token JWT.

**Sintaxis:**
```http
POST http://20.150.212.89/api/v1/authentication/sign-in
Content-Type: application/json
```

**Parámetros:**
- **Body (JSON):**
  ```json
  {
    "username": "string",
    "password": "string"
  }
  ```

**Ejemplo de Request:**
```json
{
  "username": "juan.perez",
  "password": "password123"
}
```

**Ejemplo de Response (200 OK):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "username": "juan.perez",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Códigos de Respuesta:**
- `200`: Autenticación exitosa
- `404`: Usuario no encontrado

---

#### 3. GET `/api/v1/users`
**Descripción:** Obtiene todos los usuarios registrados en el sistema.

**Sintaxis:**
```http
GET http://20.150.212.89/api/v1/users
```

**Parámetros:** Ninguno

**Ejemplo de Response (200 OK):**
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "username": "juan.perez",
    "role": "ROLE_CLIENT"
  },
  {
    "id": "550e8400-e29b-41d4-a716-446655440001",
    "username": "maria.lopez",
    "role": "ROLE_LAWYER"
  }
]
```

**Códigos de Respuesta:**
- `200`: Lista de usuarios obtenida exitosamente
- `401`: No autorizado (requiere autenticación)

---

#### 4. GET `/api/v1/users/{userId}`
**Descripción:** Obtiene un usuario específico por su ID.

**Sintaxis:**
```http
GET http://20.150.212.89/api/v1/users/{userId}
```

**Parámetros:**
- **Path Parameter:**
  - `userId` (UUID): Identificador único del usuario

**Ejemplo de Request:**
```http
GET http://20.150.212.89/api/v1/users/550e8400-e29b-41d4-a716-446655440000
```

**Ejemplo de Response (200 OK):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "username": "juan.perez",
  "role": "ROLE_CLIENT"
}
```

**Códigos de Respuesta:**
- `200`: Usuario encontrado
- `404`: Usuario no encontrado

---

#### 5. GET `/api/v1/roles`
**Descripción:** Obtiene todos los roles disponibles en el sistema.

**Sintaxis:**
```http
GET http://20.150.212.89/api/v1/roles
```

**Parámetros:** Ninguno

**Ejemplo de Response (200 OK):**
```json
[
  {
    "id": 1,
    "name": "ROLE_ADMIN"
  },
  {
    "id": 2,
    "name": "ROLE_LAWYER"
  },
  {
    "id": 3,
    "name": "ROLE_CLIENT"
  }
]
```

**Códigos de Respuesta:**
- `200`: Roles obtenidos exitosamente

---

### Profiles Service - Gestión de Perfiles

#### 6. POST `/api/v1/lawyers`
**Descripción:** Crea un nuevo perfil de abogado.

**Sintaxis:**
```http
POST http://20.150.212.89/api/v1/lawyers
Content-Type: application/json
```

**Parámetros:**
- **Body (JSON):** Objeto CreateLawyerResource con datos del abogado

**Ejemplo de Response (201 Created):**
```json
{
  "id": "660e8400-e29b-41d4-a716-446655440000",
  "userId": "550e8400-e29b-41d4-a716-446655440001",
  "specialties": ["Criminal Law", "Family Law"]
}
```

**Códigos de Respuesta:**
- `201`: Perfil de abogado creado exitosamente
- `400`: Datos inválidos

---

#### 7. GET `/api/v1/lawyers/{userId}`
**Descripción:** Obtiene el perfil de un abogado por su User ID.

**Sintaxis:**
```http
GET http://20.150.212.89/api/v1/lawyers/{userId}
```

**Parámetros:**
- **Path Parameter:**
  - `userId` (UUID): Identificador del usuario asociado al abogado

**Ejemplo de Response (200 OK):**
```json
{
  "id": "660e8400-e29b-41d4-a716-446655440000",
  "userId": "550e8400-e29b-41d4-a716-446655440001",
  "specialties": ["Criminal Law"]
}
```

**Códigos de Respuesta:**
- `200`: Perfil encontrado
- `404`: Perfil no encontrado

---

#### 8. POST `/api/v1/clients`
**Descripción:** Crea un nuevo perfil de cliente.

**Sintaxis:**
```http
POST http://20.150.212.89/api/v1/clients
Content-Type: application/json
```

**Parámetros:**
- **Body (JSON):** Objeto CreateClientResource con datos del cliente

**Ejemplo de Response (201 Created):**
```json
{
  "id": "770e8400-e29b-41d4-a716-446655440000",
  "userId": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Códigos de Respuesta:**
- `201`: Perfil de cliente creado exitosamente
- `400`: Datos inválidos

---

#### 9. GET `/api/v1/clients/{userId}`
**Descripción:** Obtiene el perfil de un cliente por su User ID.

**Sintaxis:**
```http
GET http://20.150.212.89/api/v1/clients/{userId}
```

**Parámetros:**
- **Path Parameter:**
  - `userId` (UUID): Identificador del usuario asociado al cliente

**Ejemplo de Response (200 OK):**
```json
{
  "id": "770e8400-e29b-41d4-a716-446655440000",
  "userId": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Códigos de Respuesta:**
- `200`: Perfil encontrado
- `404`: Perfil no encontrado

---

#### 10. GET `/api/v1/lawyer-specialties`
**Descripción:** Obtiene todas las especialidades legales disponibles.

**Sintaxis:**
```http
GET http://20.150.212.89/api/v1/lawyer-specialties
```

**Parámetros:** Ninguno

**Ejemplo de Response (200 OK):**
```json
[
  {
    "id": 1,
    "name": "Criminal Law"
  },
  {
    "id": 2,
    "name": "Family Law"
  },
  {
    "id": 3,
    "name": "Corporate Law"
  }
]
```

**Códigos de Respuesta:**
- `200`: Especialidades obtenidas exitosamente

---

### Cases Service - Gestión de Casos

#### 11. POST `/api/v1/cases`
**Descripción:** Crea un nuevo caso legal.

**Sintaxis:**
```http
POST http://20.150.212.89/api/v1/cases
Content-Type: application/json
```

**Parámetros:**
- **Body (JSON):** Objeto CreateCaseResource con datos del caso

**Ejemplo de Request:**
```json
{
  "clientId": "550e8400-e29b-41d4-a716-446655440000",
  "title": "Consulta Legal Familiar",
  "description": "Necesito asesoría sobre divorcio"
}
```

**Ejemplo de Response (201 Created):**
```json
{
  "id": "880e8400-e29b-41d4-a716-446655440000",
  "clientId": "550e8400-e29b-41d4-a716-446655440000",
  "title": "Consulta Legal Familiar",
  "status": "OPEN"
}
```

**Códigos de Respuesta:**
- `201`: Caso creado exitosamente
- `400`: Datos inválidos

---

#### 12. GET `/api/v1/cases`
**Descripción:** Obtiene todos los casos del sistema.

**Sintaxis:**
```http
GET http://20.150.212.89/api/v1/cases
```

**Parámetros:** Ninguno

**Ejemplo de Response (200 OK):**
```json
[
  {
    "id": "880e8400-e29b-41d4-a716-446655440000",
    "clientId": "550e8400-e29b-41d4-a716-446655440000",
    "title": "Consulta Legal Familiar",
    "status": "OPEN"
  }
]
```

**Códigos de Respuesta:**
- `200`: Lista de casos obtenida exitosamente
- `404`: No se encontraron casos

---

#### 13. GET `/api/v1/cases/{caseId}`
**Descripción:** Obtiene un caso específico por su ID.

**Sintaxis:**
```http
GET http://20.150.212.89/api/v1/cases/{caseId}
```

**Parámetros:**
- **Path Parameter:**
  - `caseId` (UUID): Identificador único del caso

**Ejemplo de Response (200 OK):**
```json
{
  "id": "880e8400-e29b-41d4-a716-446655440000",
  "clientId": "550e8400-e29b-41d4-a716-446655440000",
  "title": "Consulta Legal Familiar",
  "status": "OPEN"
}
```

**Códigos de Respuesta:**
- `200`: Caso encontrado
- `404`: Caso no encontrado

---

#### 14. PUT `/api/v1/cases/{caseId}/close`
**Descripción:** Cierra un caso existente.

**Sintaxis:**
```http
PUT http://20.150.212.89/api/v1/cases/{caseId}/close?clientId={clientId}
```

**Parámetros:**
- **Path Parameter:**
  - `caseId` (UUID): Identificador del caso
- **Query Parameter:**
  - `clientId` (UUID): Identificador del cliente que cierra el caso

**Ejemplo de Request:**
```http
PUT http://20.150.212.89/api/v1/cases/880e8400-e29b-41d4-a716-446655440000/close?clientId=550e8400-e29b-41d4-a716-446655440000
```

**Ejemplo de Response (200 OK):**
```json
{
  "id": "880e8400-e29b-41d4-a716-446655440000",
  "status": "CLOSED"
}
```

**Códigos de Respuesta:**
- `200`: Caso cerrado exitosamente
- `404`: Caso no encontrado

---

#### 15. PUT `/api/v1/cases/{caseId}/cancel`
**Descripción:** Cancela un caso existente.

**Sintaxis:**
```http
PUT http://20.150.212.89/api/v1/cases/{caseId}/cancel?clientId={clientId}
```

**Parámetros:**
- **Path Parameter:**
  - `caseId` (UUID): Identificador del caso
- **Query Parameter:**
  - `clientId` (UUID): Identificador del cliente que cancela el caso

**Ejemplo de Response (200 OK):**
```json
{
  "id": "880e8400-e29b-41d4-a716-446655440000",
  "status": "CANCELLED"
}
```

**Códigos de Respuesta:**
- `200`: Caso cancelado exitosamente
- `404`: Caso no encontrado

---

#### 16. GET `/api/v1/cases/clients/{clientId}`
**Descripción:** Obtiene todos los casos de un cliente específico.

**Sintaxis:**
```http
GET http://20.150.212.89/api/v1/cases/clients/{clientId}
```

**Parámetros:**
- **Path Parameter:**
  - `clientId` (UUID): Identificador del cliente

**Ejemplo de Response (200 OK):**
```json
[
  {
    "id": "880e8400-e29b-41d4-a716-446655440000",
    "clientId": "550e8400-e29b-41d4-a716-446655440000",
    "status": "OPEN"
  }
]
```

**Códigos de Respuesta:**
- `200`: Casos encontrados
- `404`: Cliente no encontrado

---

#### 17. GET `/api/v1/cases/suggested`
**Descripción:** Obtiene casos sugeridos para un abogado basado en sus especialidades.

**Sintaxis:**
```http
GET http://20.150.212.89/api/v1/cases/suggested?lawyerId={lawyerId}
```

**Parámetros:**
- **Query Parameter:**
  - `lawyerId` (UUID): Identificador del abogado

**Ejemplo de Response (200 OK):**
```json
[
  {
    "id": "880e8400-e29b-41d4-a716-446655440000",
    "title": "Consulta Legal Familiar",
    "status": "OPEN"
  }
]
```

**Códigos de Respuesta:**
- `200`: Casos sugeridos obtenidos exitosamente

---

#### 18. POST `/api/v1/applications`
**Descripción:** Envía una aplicación de un abogado para un caso.

**Sintaxis:**
```http
POST http://20.150.212.89/api/v1/applications
Content-Type: application/json
```

**Parámetros:**
- **Body (JSON):** Objeto con caseId y lawyerId

**Ejemplo de Response (201 Created):**
```json
{
  "id": "990e8400-e29b-41d4-a716-446655440000",
  "caseId": "880e8400-e29b-41d4-a716-446655440000",
  "lawyerId": "550e8400-e29b-41d4-a716-446655440001",
  "status": "PENDING"
}
```

**Códigos de Respuesta:**
- `201`: Aplicación enviada exitosamente
- `400`: Datos inválidos

---

#### 19. PUT `/api/v1/applications/{applicationId}/accept`
**Descripción:** Acepta una aplicación de abogado para un caso.

**Sintaxis:**
```http
PUT http://20.150.212.89/api/v1/applications/{applicationId}/accept
```

**Parámetros:**
- **Path Parameter:**
  - `applicationId` (UUID): Identificador de la aplicación

**Ejemplo de Response (200 OK):**
```json
{
  "id": "990e8400-e29b-41d4-a716-446655440000",
  "status": "ACCEPTED"
}
```

**Códigos de Respuesta:**
- `200`: Aplicación aceptada exitosamente
- `404`: Aplicación no encontrada

---

#### 20. PUT `/api/v1/applications/{applicationId}/reject`
**Descripción:** Rechaza una aplicación de abogado para un caso.

**Sintaxis:**
```http
PUT http://20.150.212.89/api/v1/applications/{applicationId}/reject
```

**Parámetros:**
- **Path Parameter:**
  - `applicationId` (UUID): Identificador de la aplicación

**Ejemplo de Response (200 OK):**
```json
{
  "id": "990e8400-e29b-41d4-a716-446655440000",
  "status": "REJECTED"
}
```

**Códigos de Respuesta:**
- `200`: Aplicación rechazada exitosamente
- `404`: Aplicación no encontrada

---

#### 21. POST `/api/v1/invitations`
**Descripción:** Envía una invitación a un abogado para un caso.

**Sintaxis:**
```http
POST http://20.150.212.89/api/v1/invitations
Content-Type: application/json
```

**Parámetros:**
- **Body (JSON):** Objeto con caseId y lawyerId

**Ejemplo de Response (201 Created):**
```json
{
  "id": "aa0e8400-e29b-41d4-a716-446655440000",
  "caseId": "880e8400-e29b-41d4-a716-446655440000",
  "lawyerId": "550e8400-e29b-41d4-a716-446655440001",
  "status": "PENDING"
}
```

**Códigos de Respuesta:**
- `201`: Invitación enviada exitosamente
- `400`: Datos inválidos

---

#### 22. PUT `/api/v1/invitations/{invitationId}/accept`
**Descripción:** Acepta una invitación a un caso.

**Sintaxis:**
```http
PUT http://20.150.212.89/api/v1/invitations/{invitationId}/accept
```

**Parámetros:**
- **Path Parameter:**
  - `invitationId` (UUID): Identificador de la invitación

**Ejemplo de Response (200 OK):**
```json
{
  "id": "aa0e8400-e29b-41d4-a716-446655440000",
  "status": "ACCEPTED"
}
```

**Códigos de Respuesta:**
- `200`: Invitación aceptada exitosamente
- `404`: Invitación no encontrada

---

#### 23. GET `/api/v1/comments`
**Descripción:** Obtiene todos los comentarios de un caso.

**Sintaxis:**
```http
GET http://20.150.212.89/api/v1/comments?caseId={caseId}
```

**Parámetros:**
- **Query Parameter:**
  - `caseId` (UUID): Identificador del caso

**Ejemplo de Response (200 OK):**
```json
[
  {
    "id": "bb0e8400-e29b-41d4-a716-446655440000",
    "caseId": "880e8400-e29b-41d4-a716-446655440000",
    "content": "Primera consulta realizada",
    "type": "GENERAL"
  }
]
```

**Códigos de Respuesta:**
- `200`: Comentarios obtenidos exitosamente

---

#### 24. POST `/api/v1/comments/general`
**Descripción:** Crea un comentario general en un caso.

**Sintaxis:**
```http
POST http://20.150.212.89/api/v1/comments/general
Content-Type: application/json
```

**Parámetros:**
- **Body (JSON):** Objeto con caseId y content

**Ejemplo de Response (201 Created):**
```json
{
  "id": "bb0e8400-e29b-41d4-a716-446655440000",
  "caseId": "880e8400-e29b-41d4-a716-446655440000",
  "content": "Revisión inicial completada",
  "type": "GENERAL"
}
```

**Códigos de Respuesta:**
- `201`: Comentario creado exitosamente
- `400`: Datos inválidos

---

#### 25. POST `/api/v1/comments/final`
**Descripción:** Crea un comentario final en un caso.

**Sintaxis:**
```http
POST http://20.150.212.89/api/v1/comments/final
Content-Type: application/json
```

**Parámetros:**
- **Body (JSON):** Objeto con caseId, lawyerId y content

**Ejemplo de Response (201 Created):**
```json
{
  "id": "cc0e8400-e29b-41d4-a716-446655440000",
  "caseId": "880e8400-e29b-41d4-a716-446655440000",
  "lawyerId": "550e8400-e29b-41d4-a716-446655440001",
  "content": "Caso resuelto satisfactoriamente",
  "type": "FINAL"
}
```

**Códigos de Respuesta:**
- `201`: Comentario final creado exitosamente
- `400`: Datos inválidos

---

## Capturas de Pantalla de Interacción

### 1. Swagger UI Principal

![Swagger UI Principal](images/swagger-ui-main.png)
*Captura del Swagger UI principal mostrando el selector de definiciones de API*

**Descripción:** La interfaz principal de Swagger UI permite seleccionar entre las diferentes definiciones de API disponibles (API Gateway, IAM Service, Profiles Service, Cases Service) mediante el dropdown "Select a definition".

---

### 2. IAM Service - Endpoint de Autenticación

![IAM Authentication](images/iam-authentication.png)
*Captura del endpoint de sign-in en IAM Service con ejemplo de request y response*

**Descripción:** Ejemplo de uso del endpoint `/api/v1/authentication/sign-in` utilizando datos de muestra. Se muestra el request body con credenciales de prueba y la respuesta exitosa con el token JWT.

---

### 3. Profiles Service - Crear Perfil de Abogado

![Profiles Create Lawyer](images/profiles-create-lawyer.png)
*Captura del endpoint POST /api/v1/lawyers con datos de ejemplo*

**Descripción:** Interacción con el endpoint para crear un perfil de abogado. Se muestra el cuerpo de la petición con datos de muestra y la respuesta 201 con el perfil creado.

---

### 4. Cases Service - Listado de Casos

![Cases List](images/cases-list.png)
*Captura del endpoint GET /api/v1/cases mostrando la lista de casos*

**Descripción:** Visualización del endpoint que retorna todos los casos disponibles. La respuesta muestra un array con los casos existentes en el sistema.

---

### 5. Prueba de Endpoint desde Swagger

![Test Endpoint](images/test-endpoint.png)
*Captura de prueba de endpoint ejecutada desde Swagger UI con respuesta exitosa*

**Descripción:** Ejemplo de ejecución de prueba de endpoint directamente desde la interfaz de Swagger UI, mostrando el request enviado y la respuesta recibida con código 200.

---

## Información del Repositorio

### URL del Repositorio de Web Services

**Repositorio:** https://github.com/SuitsUPC/lawconnect-backend

**Rama Principal:** `feature/deploy-azure`

---

### Commits Relacionados con Documentación

| Commit ID | Descripción | Fecha |
|-----------|-------------|-------|
| `e1b7851` | Fix: Separate route definitions for each path to avoid predicate OR logic issues | 2025-11-02 |
| `55e7608` | Fix Swagger URLs to use /api-docs instead of /v3/api-docs for microservices | 2025-11-02 |
| `12a83d1` | Add Swagger OpenAPI documentation tests to deployment script | 2025-11-02 |
| `ad44248` | Add multi-service Swagger configuration to API Gateway | 2025-11-02 |
| `36c000e` | Fix API Gateway StripPrefix and update test endpoints | 2025-11-02 |

---




