# Guía Rápida - Pruebas con Postman

**URL Base:** `http://20.150.212.89`

## 1. Obtener Token

### Sign Up - Crear Usuario
```
POST http://20.150.212.89/api/v1/authentication/sign-up
Content-Type: application/json

{
  "username": "test.user",
  "password": "password123",
  "role": "ROLE_CLIENT"
}
```

> [CAPTURA: Sign Up request y response]

---

### Sign In - Obtener Token
```
POST http://20.150.212.89/api/v1/authentication/sign-in
Content-Type: application/json

{
  "username": "test.user",
  "password": "password123"
}
```

Response incluye el `token` que se usa en los siguientes requests.

> [CAPTURA: Sign In request y response con token]

---

## 2. Configurar Token en Postman

En Postman, crear variable de entorno `token` y usarla en header:
```
Authorization: Bearer {{token}}
```

> [CAPTURA: Configuración de variable token]

---

## 3. Endpoints

### IAM Service

#### GET /api/v1/users
```
GET http://20.150.212.89/api/v1/users
Authorization: Bearer {{token}}
```

> [CAPTURA: GET /api/v1/users]

---

#### GET /api/v1/users/{userId}
```
GET http://20.150.212.89/api/v1/users/550e8400-e29b-41d4-a716-446655440000
Authorization: Bearer {{token}}
```

> [CAPTURA: GET /api/v1/users/{userId}]

---

#### GET /api/v1/roles
```
GET http://20.150.212.89/api/v1/roles
```

> [CAPTURA: GET /api/v1/roles]

---

### Profiles Service

#### GET /api/v1/lawyer-specialties
```
GET http://20.150.212.89/api/v1/lawyer-specialties
```

> [CAPTURA: GET /api/v1/lawyer-specialties]

---

#### POST /api/v1/clients
```
POST http://20.150.212.89/api/v1/clients
Content-Type: application/json
Authorization: Bearer {{token}}

{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "dni": "12345678",
  "phoneNumber": "+51987654321",
  "address": "Av. Principal 123"
}
```

> [CAPTURA: POST /api/v1/clients]

---

#### POST /api/v1/lawyers
```
POST http://20.150.212.89/api/v1/lawyers
Content-Type: application/json
Authorization: Bearer {{token}}

{
  "userId": "550e8400-e29b-41d4-a716-446655440001",
  "dni": "87654321",
  "phoneNumber": "+51912345678",
  "specialtyIds": [1, 2]
}
```

> [CAPTURA: POST /api/v1/lawyers]

---

### Cases Service

#### POST /api/v1/cases
```
POST http://20.150.212.89/api/v1/cases
Content-Type: application/json
Authorization: Bearer {{token}}

{
  "clientId": "550e8400-e29b-41d4-a716-446655440000",
  "title": "Consulta Legal sobre Divorcio",
  "description": "Necesito asesoría legal para proceso de divorcio",
  "specialtyId": 2
}
```

> [CAPTURA: POST /api/v1/cases]

---

#### GET /api/v1/cases
```
GET http://20.150.212.89/api/v1/cases
Authorization: Bearer {{token}}
```

> [CAPTURA: GET /api/v1/cases]

---

#### GET /api/v1/cases/{caseId}
```
GET http://20.150.212.89/api/v1/cases/880e8400-e29b-41d4-a716-446655440000
Authorization: Bearer {{token}}
```

> [CAPTURA: GET /api/v1/cases/{caseId}]

---

#### POST /api/v1/applications
```
POST http://20.150.212.89/api/v1/applications
Content-Type: application/json
Authorization: Bearer {{token}}

{
  "caseId": "880e8400-e29b-41d4-a716-446655440000",
  "lawyerId": "550e8400-e29b-41d4-a716-446655440001",
  "message": "Tengo experiencia en casos de familia"
}
```

> [CAPTURA: POST /api/v1/applications]

---
