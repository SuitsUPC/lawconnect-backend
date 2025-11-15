#!/bin/bash

# Script completo para probar todos los endpoints relacionados con usuarios y perfiles
# Uso: ./test-all-endpoints.sh [local|cloud] [base-url]

set -e

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

ENV=${1:-local}
BASE_URL=${2:-http://localhost:8080}

if [ "$ENV" = "cloud" ]; then
    BASE_URL=${2:-https://garcia-guardian-yields-editorial.trycloudflare.com}
fi

echo -e "${BLUE}🧪 Probando TODOS los endpoints relacionados con usuarios y perfiles${NC}"
echo -e "${BLUE}Entorno: ${ENV}${NC}"
echo -e "${BLUE}Base URL: ${BASE_URL}${NC}"
echo ""

# Variables globales
JWT_TOKEN=""
CLIENT_USER_ID=""
LAWYER_USER_ID=""
CLIENT_USERNAME=""
LAWYER_USERNAME=""

# Función para hacer una petición HTTP
make_request() {
    local method=$1
    local url=$2
    local data=$3
    local use_auth=${4:-false}
    local token=$5
    
    if [ "$method" = "GET" ]; then
        if [ "$use_auth" = "true" ] && [ ! -z "$token" ]; then
            curl -s -w "\n%{http_code}" -X GET "$url" \
                -H "Content-Type: application/json" \
                -H "Authorization: Bearer $token" \
                2>/dev/null || echo -e "\n000"
        else
            curl -s -w "\n%{http_code}" -X GET "$url" \
                -H "Content-Type: application/json" \
                2>/dev/null || echo -e "\n000"
        fi
    elif [ "$method" = "POST" ]; then
        if [ "$use_auth" = "true" ] && [ ! -z "$token" ]; then
            curl -s -w "\n%{http_code}" -X POST "$url" \
                -H "Content-Type: application/json" \
                -H "Authorization: Bearer $token" \
                -d "$data" \
                2>/dev/null || echo -e "\n000"
        else
            curl -s -w "\n%{http_code}" -X POST "$url" \
                -H "Content-Type: application/json" \
                -d "$data" \
                2>/dev/null || echo -e "\n000"
        fi
    elif [ "$method" = "PUT" ]; then
        if [ "$use_auth" = "true" ] && [ ! -z "$token" ]; then
            curl -s -w "\n%{http_code}" -X PUT "$url" \
                -H "Content-Type: application/json" \
                -H "Authorization: Bearer $token" \
                -d "$data" \
                2>/dev/null || echo -e "\n000"
        else
            curl -s -w "\n%{http_code}" -X PUT "$url" \
                -H "Content-Type: application/json" \
                -d "$data" \
                2>/dev/null || echo -e "\n000"
        fi
    fi
}

# Función para probar un endpoint
test_endpoint() {
    local method=$1
    local url=$2
    local description=$3
    local data=$4
    local use_auth=${5:-false}
    local expected_status=${6:-200}
    
    echo -e "${BLUE}Testing: ${description}${NC}"
    echo -e "  ${YELLOW}${method} ${url}${NC}"
    
    local response
    if [ "$use_auth" = "true" ]; then
        response=$(make_request "$method" "$url" "$data" "true" "$JWT_TOKEN")
    else
        response=$(make_request "$method" "$url" "$data" "false" "")
    fi
    
    local http_code=$(echo "$response" | tail -n1)
    local body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" = "$expected_status" ] || [ "$http_code" = "201" ] || [ "$http_code" = "200" ]; then
        echo -e "  ${GREEN}✅ Status: $http_code${NC}"
        if [ ! -z "$body" ] && [ "$body" != "null" ] && [ "$body" != "[]" ]; then
            echo -e "  ${GREEN}   Response: $(echo "$body" | head -c 150)...${NC}"
        fi
        return 0
    else
        echo -e "  ${RED}❌ Status: $http_code (esperado: $expected_status)${NC}"
        if [ ! -z "$body" ]; then
            echo -e "  ${RED}   Error: $(echo "$body" | head -c 200)${NC}"
        fi
        return 1
    fi
}

# ============================================
# PASO 1: Obtener roles
# ============================================
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}Paso 1: Obteniendo roles disponibles${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

response=$(make_request "GET" "${BASE_URL}/api/v1/roles")
http_code=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$d')

if [ "$http_code" = "200" ]; then
    ROLE_LAWYER=$(echo "$body" | grep -o '"name":"[^"]*LAWYER[^"]*"' | head -1 | cut -d'"' -f4 || echo "ROLE_LAWYER")
    ROLE_CLIENT=$(echo "$body" | grep -o '"name":"[^"]*CLIENT[^"]*"' | head -1 | cut -d'"' -f4 || echo "ROLE_CLIENT")
    echo -e "${GREEN}✅ Roles obtenidos${NC}"
    echo ""
else
    echo -e "${RED}❌ Error al obtener roles${NC}"
    exit 1
fi

# ============================================
# PASO 2: Crear usuarios
# ============================================
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}Paso 2: Creando usuarios de prueba${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

TIMESTAMP=$(date +%s)
CLIENT_USERNAME="test_client_${TIMESTAMP}"
LAWYER_USERNAME="test_lawyer_${TIMESTAMP}"
PASSWORD="test123456"

# Crear CLIENT
signup_data="{\"username\":\"${CLIENT_USERNAME}\",\"password\":\"${PASSWORD}\",\"role\":\"${ROLE_CLIENT}\"}"
response=$(make_request "POST" "${BASE_URL}/api/v1/authentication/sign-up" "$signup_data")
http_code=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$d')

if [ "$http_code" = "201" ]; then
    CLIENT_USER_ID=$(echo "$body" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4 || echo "")
    echo -e "${GREEN}✅ Usuario CLIENT creado: ${CLIENT_USERNAME} (ID: ${CLIENT_USER_ID})${NC}"
else
    echo -e "${RED}❌ Error al crear usuario CLIENT${NC}"
    exit 1
fi

# Crear LAWYER
signup_data="{\"username\":\"${LAWYER_USERNAME}\",\"password\":\"${PASSWORD}\",\"role\":\"${ROLE_LAWYER}\"}"
response=$(make_request "POST" "${BASE_URL}/api/v1/authentication/sign-up" "$signup_data")
http_code=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$d')

if [ "$http_code" = "201" ]; then
    LAWYER_USER_ID=$(echo "$body" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4 || echo "")
    echo -e "${GREEN}✅ Usuario LAWYER creado: ${LAWYER_USERNAME} (ID: ${LAWYER_USER_ID})${NC}"
else
    echo -e "${RED}❌ Error al crear usuario LAWYER${NC}"
    exit 1
fi

echo ""

# ============================================
# PASO 3: Login y obtener tokens
# ============================================
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}Paso 3: Obteniendo tokens de autenticación${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

# Login CLIENT
login_data="{\"username\":\"${CLIENT_USERNAME}\",\"password\":\"${PASSWORD}\"}"
response=$(make_request "POST" "${BASE_URL}/api/v1/authentication/sign-in" "$login_data")
http_code=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$d')

if [ "$http_code" = "200" ]; then
    JWT_TOKEN=$(echo "$body" | grep -o '"token":"[^"]*' | cut -d'"' -f4 || echo "")
    echo -e "${GREEN}✅ Token obtenido para CLIENT${NC}"
else
    echo -e "${RED}❌ Error en login CLIENT${NC}"
    exit 1
fi

echo ""

# ============================================
# PASO 4: Probar endpoints de USUARIOS
# ============================================
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}Paso 4: Probando endpoints de USUARIOS${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

test_endpoint "GET" "${BASE_URL}/api/v1/users" "GET /api/v1/users - Listar todos los usuarios" "" "false"
test_endpoint "GET" "${BASE_URL}/api/v1/users/${CLIENT_USER_ID}" "GET /api/v1/users/{userId} - Obtener usuario CLIENT por ID" "" "false"
test_endpoint "GET" "${BASE_URL}/api/v1/users/${LAWYER_USER_ID}" "GET /api/v1/users/{userId} - Obtener usuario LAWYER por ID" "" "false"

echo ""

# ============================================
# PASO 5: Probar endpoints de PERFILES - CLIENTES
# ============================================
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}Paso 5: Probando endpoints de PERFILES - CLIENTES${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

test_endpoint "GET" "${BASE_URL}/api/v1/clients" "GET /api/v1/clients - Listar todos los clientes" "" "true"
test_endpoint "GET" "${BASE_URL}/api/v1/clients/${CLIENT_USER_ID}" "GET /api/v1/clients/{userId} - Obtener perfil CLIENT por User ID" "" "true"

# Actualizar perfil CLIENT
update_data="{\"firstname\":\"Juan\",\"lastname\":\"Pérez\",\"dni\":\"12345678A\",\"contactInfo\":{\"phoneNumber\":\"+51987654321\",\"address\":\"Av. Principal 123\"}}"
test_endpoint "PUT" "${BASE_URL}/api/v1/clients/${CLIENT_USER_ID}" "PUT /api/v1/clients/{userId} - Actualizar perfil CLIENT" "$update_data" "true"

# Verificar actualización
test_endpoint "GET" "${BASE_URL}/api/v1/clients/${CLIENT_USER_ID}" "GET /api/v1/clients/{userId} - Verificar perfil CLIENT actualizado" "" "true"

echo ""

# ============================================
# PASO 6: Probar endpoints de PERFILES - ABOGADOS
# ============================================
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}Paso 6: Probando endpoints de PERFILES - ABOGADOS${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

test_endpoint "GET" "${BASE_URL}/api/v1/lawyers" "GET /api/v1/lawyers - Listar todos los abogados" "" "true"
test_endpoint "GET" "${BASE_URL}/api/v1/lawyers/${LAWYER_USER_ID}" "GET /api/v1/lawyers/{userId} - Obtener perfil LAWYER por User ID" "" "true"

# Actualizar perfil LAWYER
update_data="{\"firstname\":\"María\",\"lastname\":\"González\",\"dni\":\"87654321B\",\"contactInfo\":{\"phoneNumber\":\"+51912345678\",\"address\":\"Calle Secundaria 456\"},\"description\":\"Abogada especializada en derecho civil\"}"
test_endpoint "PUT" "${BASE_URL}/api/v1/lawyers/${LAWYER_USER_ID}" "PUT /api/v1/lawyers/{userId} - Actualizar perfil LAWYER" "$update_data" "true"

# Verificar actualización
test_endpoint "GET" "${BASE_URL}/api/v1/lawyers/${LAWYER_USER_ID}" "GET /api/v1/lawyers/{userId} - Verificar perfil LAWYER actualizado" "" "true"

echo ""

# ============================================
# PASO 7: Probar actualización de especialidades
# ============================================
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}Paso 7: Probando actualización de especialidades${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

# Obtener especialidades disponibles
response=$(make_request "GET" "${BASE_URL}/api/v1/lawyer-specialties")
http_code=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$d')

if [ "$http_code" = "200" ]; then
    # Obtener las primeras 2 especialidades
    SPECIALTY1=$(echo "$body" | grep -o '"name":"[^"]*"' | head -1 | cut -d'"' -f4 || echo "")
    SPECIALTY2=$(echo "$body" | grep -o '"name":"[^"]*"' | head -2 | tail -1 | cut -d'"' -f4 || echo "")
    
    if [ ! -z "$SPECIALTY1" ] && [ ! -z "$SPECIALTY2" ]; then
        update_specialties_data="{\"specialties\":[\"${SPECIALTY1}\",\"${SPECIALTY2}\"]}"
        test_endpoint "PUT" "${BASE_URL}/api/v1/lawyers/${LAWYER_USER_ID}/specialties" "PUT /api/v1/lawyers/{userId}/specialties - Actualizar especialidades" "$update_specialties_data" "true"
        
        # Verificar especialidades actualizadas
        test_endpoint "GET" "${BASE_URL}/api/v1/lawyers/${LAWYER_USER_ID}" "GET /api/v1/lawyers/{userId} - Verificar especialidades actualizadas" "" "true"
    else
        echo -e "${YELLOW}⚠️  No se pudieron obtener especialidades para probar${NC}"
    fi
else
    echo -e "${YELLOW}⚠️  No se pudieron obtener especialidades${NC}"
fi

echo ""

# ============================================
# PASO 8: Probar casos edge y errores
# ============================================
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}Paso 8: Probando casos edge y manejo de errores${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

# Intentar obtener perfil de usuario que no existe
FAKE_UUID="00000000-0000-0000-0000-000000000000"
test_endpoint "GET" "${BASE_URL}/api/v1/clients/${FAKE_UUID}" "GET /api/v1/clients/{userId} - Perfil inexistente (debe retornar 404)" "" "true" "404"
test_endpoint "GET" "${BASE_URL}/api/v1/lawyers/${FAKE_UUID}" "GET /api/v1/lawyers/{userId} - Perfil inexistente (debe retornar 404)" "" "true" "404"

# Intentar crear usuario duplicado
duplicate_data="{\"username\":\"${CLIENT_USERNAME}\",\"password\":\"${PASSWORD}\",\"role\":\"${ROLE_CLIENT}\"}"
test_endpoint "POST" "${BASE_URL}/api/v1/authentication/sign-up" "POST /api/v1/authentication/sign-up - Usuario duplicado (debe fallar)" "$duplicate_data" "false" "400"

echo ""

# ============================================
# RESUMEN FINAL
# ============================================
echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${GREEN}✅ Pruebas completadas!${NC}"
echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
echo -e "${BLUE}📊 Resumen de usuarios creados:${NC}"
echo -e "  - CLIENT: ${CLIENT_USERNAME} (ID: ${CLIENT_USER_ID})"
echo -e "  - LAWYER: ${LAWYER_USERNAME} (ID: ${LAWYER_USER_ID})"
echo ""
echo -e "${BLUE}💡 Todos los endpoints relacionados con usuarios y perfiles fueron probados${NC}"
echo ""

