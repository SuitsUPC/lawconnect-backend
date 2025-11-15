#!/bin/bash

# Script COMPLETO para probar TODOS los endpoints relacionados con usuarios y perfiles
# Uso: ./test-completo.sh [local|cloud] [base-url]

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

echo -e "${BLUE}🧪 PROBANDO TODOS LOS ENDPOINTS - TEST COMPLETO${NC}"
echo -e "${BLUE}Entorno: ${ENV}${NC}"
echo -e "${BLUE}Base URL: ${BASE_URL}${NC}"
echo ""

# Variables globales
JWT_TOKEN=""
CLIENT_USER_ID=""
LAWYER_USER_ID=""
CLIENT_USERNAME=""
LAWYER_USERNAME=""
ROLE_LAWYER=""
ROLE_CLIENT=""
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

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
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    echo -e "${BLUE}Test #${TOTAL_TESTS}: ${description}${NC}"
    echo -e "  ${YELLOW}${method} ${url}${NC}"
    
    local response
    if [ "$use_auth" = "true" ]; then
        response=$(make_request "$method" "$url" "$data" "true" "$JWT_TOKEN")
    else
        response=$(make_request "$method" "$url" "$data" "false" "")
    fi
    
    local http_code=$(echo "$response" | tail -n1)
    local body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" = "$expected_status" ] || ([ "$expected_status" = "200" ] && [ "$http_code" = "201" ]); then
        PASSED_TESTS=$((PASSED_TESTS + 1))
        echo -e "  ${GREEN}✅ PASS - Status: $http_code${NC}"
        if [ ! -z "$body" ] && [ "$body" != "null" ] && [ "$body" != "[]" ] && [ ${#body} -lt 200 ]; then
            echo -e "  ${GREEN}   Response: $(echo "$body" | head -c 100)...${NC}"
        fi
        echo ""
        return 0
    else
        FAILED_TESTS=$((FAILED_TESTS + 1))
        echo -e "  ${RED}❌ FAIL - Status: $http_code (esperado: $expected_status)${NC}"
        if [ ! -z "$body" ]; then
            echo -e "  ${RED}   Error: $(echo "$body" | head -c 150)${NC}"
        fi
        echo ""
        return 1
    fi
}

# ============================================
# SECCIÓN 1: ROLES
# ============================================
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}SECCIÓN 1: ROLES${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

test_endpoint "GET" "${BASE_URL}/api/v1/roles" "GET /api/v1/roles - Listar todos los roles" "" "false"

response=$(make_request "GET" "${BASE_URL}/api/v1/roles")
body=$(echo "$response" | sed '$d')
ROLE_LAWYER=$(echo "$body" | grep -o '"name":"[^"]*LAWYER[^"]*"' | head -1 | cut -d'"' -f4 || echo "ROLE_LAWYER")
ROLE_CLIENT=$(echo "$body" | grep -o '"name":"[^"]*CLIENT[^"]*"' | head -1 | cut -d'"' -f4 || echo "ROLE_CLIENT")

echo ""

# ============================================
# SECCIÓN 2: AUTENTICACIÓN
# ============================================
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}SECCIÓN 2: AUTENTICACIÓN${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

TIMESTAMP=$(date +%s)
CLIENT_USERNAME="test_client_${TIMESTAMP}"
LAWYER_USERNAME="test_lawyer_${TIMESTAMP}"
PASSWORD="test123456"

# Crear usuario CLIENT
signup_data="{\"username\":\"${CLIENT_USERNAME}\",\"password\":\"${PASSWORD}\",\"role\":\"${ROLE_CLIENT}\"}"
response=$(make_request "POST" "${BASE_URL}/api/v1/authentication/sign-up" "$signup_data" "false" "")
http_code=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$d')
CLIENT_USER_ID=$(echo "$body" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4 || echo "")

if [ "$http_code" = "201" ]; then
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    PASSED_TESTS=$((PASSED_TESTS + 1))
    echo -e "${BLUE}Test #${TOTAL_TESTS}: POST /api/v1/authentication/sign-up - Crear usuario CLIENT${NC}"
    echo -e "  ${YELLOW}POST ${BASE_URL}/api/v1/authentication/sign-up${NC}"
    echo -e "  ${GREEN}✅ PASS - Status: $http_code${NC}"
    echo ""
else
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    FAILED_TESTS=$((FAILED_TESTS + 1))
    echo -e "${BLUE}Test #${TOTAL_TESTS}: POST /api/v1/authentication/sign-up - Crear usuario CLIENT${NC}"
    echo -e "  ${RED}❌ FAIL - Status: $http_code${NC}"
    echo ""
fi

# Crear usuario LAWYER
signup_data="{\"username\":\"${LAWYER_USERNAME}\",\"password\":\"${PASSWORD}\",\"role\":\"${ROLE_LAWYER}\"}"
response=$(make_request "POST" "${BASE_URL}/api/v1/authentication/sign-up" "$signup_data" "false" "")
http_code=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$d')
LAWYER_USER_ID=$(echo "$body" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4 || echo "")

if [ "$http_code" = "201" ]; then
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    PASSED_TESTS=$((PASSED_TESTS + 1))
    echo -e "${BLUE}Test #${TOTAL_TESTS}: POST /api/v1/authentication/sign-up - Crear usuario LAWYER${NC}"
    echo -e "  ${YELLOW}POST ${BASE_URL}/api/v1/authentication/sign-up${NC}"
    echo -e "  ${GREEN}✅ PASS - Status: $http_code${NC}"
    echo ""
else
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    FAILED_TESTS=$((FAILED_TESTS + 1))
    echo -e "${BLUE}Test #${TOTAL_TESTS}: POST /api/v1/authentication/sign-up - Crear usuario LAWYER${NC}"
    echo -e "  ${RED}❌ FAIL - Status: $http_code${NC}"
    echo ""
fi

# Login CLIENT
login_data="{\"username\":\"${CLIENT_USERNAME}\",\"password\":\"${PASSWORD}\"}"
test_endpoint "POST" "${BASE_URL}/api/v1/authentication/sign-in" "POST /api/v1/authentication/sign-in - Login CLIENT" "$login_data" "false" "200"

response=$(make_request "POST" "${BASE_URL}/api/v1/authentication/sign-in" "$login_data" "false" "")
body=$(echo "$response" | sed '$d')
JWT_TOKEN=$(echo "$body" | grep -o '"token":"[^"]*' | cut -d'"' -f4 || echo "")

# Intentar crear usuario duplicado (debe fallar con 409 Conflict)
duplicate_data="{\"username\":\"${CLIENT_USERNAME}\",\"password\":\"${PASSWORD}\",\"role\":\"${ROLE_CLIENT}\"}"
test_endpoint "POST" "${BASE_URL}/api/v1/authentication/sign-up" "POST /api/v1/authentication/sign-up - Usuario duplicado (debe fallar)" "$duplicate_data" "false" "409"

echo ""

# ============================================
# SECCIÓN 3: USUARIOS
# ============================================
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}SECCIÓN 3: USUARIOS${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

test_endpoint "GET" "${BASE_URL}/api/v1/users" "GET /api/v1/users - Listar todos los usuarios" "" "false"

if [ ! -z "$CLIENT_USER_ID" ]; then
    test_endpoint "GET" "${BASE_URL}/api/v1/users/${CLIENT_USER_ID}" "GET /api/v1/users/{userId} - Obtener usuario CLIENT por ID" "" "false"
fi

if [ ! -z "$LAWYER_USER_ID" ]; then
    test_endpoint "GET" "${BASE_URL}/api/v1/users/${LAWYER_USER_ID}" "GET /api/v1/users/{userId} - Obtener usuario LAWYER por ID" "" "false"
fi

# Usuario inexistente
FAKE_UUID="00000000-0000-0000-0000-000000000000"
test_endpoint "GET" "${BASE_URL}/api/v1/users/${FAKE_UUID}" "GET /api/v1/users/{userId} - Usuario inexistente (debe retornar 404)" "" "false" "404"

echo ""

# ============================================
# SECCIÓN 4: PERFILES - CLIENTES
# ============================================
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}SECCIÓN 4: PERFILES - CLIENTES${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

test_endpoint "GET" "${BASE_URL}/api/v1/clients" "GET /api/v1/clients - Listar todos los clientes" "" "true"

if [ ! -z "$CLIENT_USER_ID" ]; then
    test_endpoint "GET" "${BASE_URL}/api/v1/clients/${CLIENT_USER_ID}" "GET /api/v1/clients/{userId} - Obtener perfil CLIENT por User ID" "" "true"
    
    # Actualizar perfil CLIENT
    update_data="{\"firstname\":\"Juan\",\"lastname\":\"Pérez\",\"dni\":\"12345678A\",\"contactInfo\":{\"phoneNumber\":\"+51987654321\",\"address\":\"Av. Principal 123\"}}"
    test_endpoint "PUT" "${BASE_URL}/api/v1/clients/${CLIENT_USER_ID}" "PUT /api/v1/clients/{userId} - Actualizar perfil CLIENT" "$update_data" "true" "200"
    
    # Verificar actualización
    test_endpoint "GET" "${BASE_URL}/api/v1/clients/${CLIENT_USER_ID}" "GET /api/v1/clients/{userId} - Verificar perfil CLIENT actualizado" "" "true"
fi

# Cliente inexistente
test_endpoint "GET" "${BASE_URL}/api/v1/clients/${FAKE_UUID}" "GET /api/v1/clients/{userId} - Cliente inexistente (debe retornar 404)" "" "true" "404"

echo ""

# ============================================
# SECCIÓN 5: PERFILES - ABOGADOS
# ============================================
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}SECCIÓN 5: PERFILES - ABOGADOS${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

test_endpoint "GET" "${BASE_URL}/api/v1/lawyers" "GET /api/v1/lawyers - Listar todos los abogados" "" "true"

if [ ! -z "$LAWYER_USER_ID" ]; then
    test_endpoint "GET" "${BASE_URL}/api/v1/lawyers/${LAWYER_USER_ID}" "GET /api/v1/lawyers/{userId} - Obtener perfil LAWYER por User ID" "" "true"
    
    # Actualizar perfil LAWYER
    update_data="{\"firstname\":\"María\",\"lastname\":\"González\",\"dni\":\"87654321B\",\"contactInfo\":{\"phoneNumber\":\"+51912345678\",\"address\":\"Calle Secundaria 456\"},\"description\":\"Abogada especializada en derecho civil\"}"
    test_endpoint "PUT" "${BASE_URL}/api/v1/lawyers/${LAWYER_USER_ID}" "PUT /api/v1/lawyers/{userId} - Actualizar perfil LAWYER" "$update_data" "true" "200"
    
    # Verificar actualización
    test_endpoint "GET" "${BASE_URL}/api/v1/lawyers/${LAWYER_USER_ID}" "GET /api/v1/lawyers/{userId} - Verificar perfil LAWYER actualizado" "" "true"
fi

# Abogado inexistente
test_endpoint "GET" "${BASE_URL}/api/v1/lawyers/${FAKE_UUID}" "GET /api/v1/lawyers/{userId} - Abogado inexistente (debe retornar 404)" "" "true" "404"

echo ""

# ============================================
# SECCIÓN 6: ESPECIALIDADES
# ============================================
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}SECCIÓN 6: ESPECIALIDADES${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

test_endpoint "GET" "${BASE_URL}/api/v1/lawyer-specialties" "GET /api/v1/lawyer-specialties - Listar especialidades" "" "false"

# Obtener especialidades para actualizar
response=$(make_request "GET" "${BASE_URL}/api/v1/lawyer-specialties")
body=$(echo "$response" | sed '$d')
SPECIALTY1=$(echo "$body" | grep -o '"name":"[^"]*"' | head -1 | cut -d'"' -f4 || echo "")
SPECIALTY2=$(echo "$body" | grep -o '"name":"[^"]*"' | head -2 | tail -1 | cut -d'"' -f4 || echo "")

if [ ! -z "$LAWYER_USER_ID" ] && [ ! -z "$SPECIALTY1" ] && [ ! -z "$SPECIALTY2" ]; then
    update_specialties_data="{\"specialties\":[\"${SPECIALTY1}\",\"${SPECIALTY2}\"]}"
    test_endpoint "PUT" "${BASE_URL}/api/v1/lawyers/${LAWYER_USER_ID}/specialties" "PUT /api/v1/lawyers/{userId}/specialties - Actualizar especialidades" "$update_specialties_data" "true" "200"
    
    # Verificar especialidades actualizadas
    test_endpoint "GET" "${BASE_URL}/api/v1/lawyers/${LAWYER_USER_ID}" "GET /api/v1/lawyers/{userId} - Verificar especialidades actualizadas" "" "true"
fi

echo ""

# ============================================
# SECCIÓN 7: VERIFICACIÓN DE CREACIÓN AUTOMÁTICA DE PERFILES
# ============================================
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}SECCIÓN 7: VERIFICACIÓN DE CREACIÓN AUTOMÁTICA DE PERFILES${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

# Crear un nuevo usuario CLIENT para verificar que se crea el perfil automáticamente
NEW_CLIENT_USERNAME="auto_test_client_${TIMESTAMP}"
signup_data="{\"username\":\"${NEW_CLIENT_USERNAME}\",\"password\":\"${PASSWORD}\",\"role\":\"${ROLE_CLIENT}\"}"
response=$(make_request "POST" "${BASE_URL}/api/v1/authentication/sign-up" "$signup_data" "false" "")
http_code=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$d')
NEW_CLIENT_USER_ID=$(echo "$body" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4 || echo "")

if [ "$http_code" = "201" ] && [ ! -z "$NEW_CLIENT_USER_ID" ]; then
    echo -e "${GREEN}✅ Usuario CLIENT creado: ${NEW_CLIENT_USERNAME} (ID: ${NEW_CLIENT_USER_ID})${NC}"
    
    # Esperar un segundo para que se cree el perfil
    sleep 2
    
    # Login para obtener token
    login_data="{\"username\":\"${NEW_CLIENT_USERNAME}\",\"password\":\"${PASSWORD}\"}"
    login_response=$(make_request "POST" "${BASE_URL}/api/v1/authentication/sign-in" "$login_data" "false" "")
    login_body=$(echo "$login_response" | sed '$d')
    NEW_TOKEN=$(echo "$login_body" | grep -o '"token":"[^"]*' | cut -d'"' -f4 || echo "")
    
    if [ ! -z "$NEW_TOKEN" ]; then
        JWT_TOKEN="$NEW_TOKEN"
        test_endpoint "GET" "${BASE_URL}/api/v1/clients/${NEW_CLIENT_USER_ID}" "GET /api/v1/clients/{userId} - Verificar que perfil CLIENT se creó automáticamente" "" "true"
    fi
fi

# Crear un nuevo usuario LAWYER para verificar que se crea el perfil automáticamente
NEW_LAWYER_USERNAME="auto_test_lawyer_${TIMESTAMP}"
signup_data="{\"username\":\"${NEW_LAWYER_USERNAME}\",\"password\":\"${PASSWORD}\",\"role\":\"${ROLE_LAWYER}\"}"
response=$(make_request "POST" "${BASE_URL}/api/v1/authentication/sign-up" "$signup_data" "false" "")
http_code=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$d')
NEW_LAWYER_USER_ID=$(echo "$body" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4 || echo "")

if [ "$http_code" = "201" ] && [ ! -z "$NEW_LAWYER_USER_ID" ]; then
    echo -e "${GREEN}✅ Usuario LAWYER creado: ${NEW_LAWYER_USERNAME} (ID: ${NEW_LAWYER_USER_ID})${NC}"
    
    # Esperar un segundo para que se cree el perfil
    sleep 2
    
    # Login para obtener token
    login_data="{\"username\":\"${NEW_LAWYER_USERNAME}\",\"password\":\"${PASSWORD}\"}"
    login_response=$(make_request "POST" "${BASE_URL}/api/v1/authentication/sign-in" "$login_data" "false" "")
    login_body=$(echo "$login_response" | sed '$d')
    NEW_TOKEN=$(echo "$login_body" | grep -o '"token":"[^"]*' | cut -d'"' -f4 || echo "")
    
    if [ ! -z "$NEW_TOKEN" ]; then
        JWT_TOKEN="$NEW_TOKEN"
        test_endpoint "GET" "${BASE_URL}/api/v1/lawyers/${NEW_LAWYER_USER_ID}" "GET /api/v1/lawyers/{userId} - Verificar que perfil LAWYER se creó automáticamente" "" "true"
    fi
fi

echo ""

# ============================================
# RESUMEN FINAL
# ============================================
echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${GREEN}✅ PRUEBAS COMPLETADAS${NC}"
echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
echo -e "${BLUE}📊 RESUMEN:${NC}"
echo -e "  Total de pruebas: ${TOTAL_TESTS}"
echo -e "  ${GREEN}✅ Exitosas: ${PASSED_TESTS}${NC}"
echo -e "  ${RED}❌ Fallidas: ${FAILED_TESTS}${NC}"
echo ""

if [ "$FAILED_TESTS" -eq 0 ]; then
    echo -e "${GREEN}🎉 ¡TODAS LAS PRUEBAS PASARON!${NC}"
    exit 0
else
    echo -e "${RED}⚠️  Algunas pruebas fallaron. Revisa los errores arriba.${NC}"
    exit 1
fi

