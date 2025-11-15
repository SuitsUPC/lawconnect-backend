#!/bin/bash

# Script COMPLETO para probar TODOS los 32+ endpoints del sistema
# Uso: ./test-todos-endpoints.sh [local|cloud] [base-url]

# No usar set -e para que continúe aunque haya errores

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
RED='\033[0;31m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

ENV=${1:-local}
BASE_URL=${2:-http://localhost:8080}

if [ "$ENV" = "cloud" ]; then
    BASE_URL=${2:-https://garcia-guardian-yields-editorial.trycloudflare.com}
fi

echo -e "${CYAN}╔════════════════════════════════════════════════════════════════╗${NC}"
echo -e "${CYAN}║   🧪 TEST COMPLETO - TODOS LOS ENDPOINTS (32+)              ║${NC}"
echo -e "${CYAN}╚════════════════════════════════════════════════════════════════╝${NC}"
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
CASE_ID=""
APPLICATION_ID=""
INVITATION_ID=""
COMMENT_ID=""
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
    local content_type=${6:-application/json}
    
    local headers=()
    headers+=("-H" "Content-Type: $content_type")
    
    if [ "$use_auth" = "true" ] && [ ! -z "$token" ]; then
        headers+=("-H" "Authorization: Bearer $token")
    fi
    
    if [ "$method" = "GET" ]; then
        curl -s -w "\n%{http_code}" -X GET "$url" "${headers[@]}" 2>/dev/null || echo -e "\n000"
    elif [ "$method" = "POST" ]; then
        if [ "$content_type" = "multipart/form-data" ]; then
            curl -s -w "\n%{http_code}" -X POST "$url" "${headers[@]}" -F "$data" 2>/dev/null || echo -e "\n000"
        else
            curl -s -w "\n%{http_code}" -X POST "$url" "${headers[@]}" -d "$data" 2>/dev/null || echo -e "\n000"
        fi
    elif [ "$method" = "PUT" ]; then
        curl -s -w "\n%{http_code}" -X PUT "$url" "${headers[@]}" -d "$data" 2>/dev/null || echo -e "\n000"
    elif [ "$method" = "DELETE" ]; then
        curl -s -w "\n%{http_code}" -X DELETE "$url" "${headers[@]}" 2>/dev/null || echo -e "\n000"
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
    local content_type=${7:-application/json}
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    echo -e "${BLUE}[Test #${TOTAL_TESTS}] ${description}${NC}"
    echo -e "  ${YELLOW}${method} ${url}${NC}"
    
    local response
    response=$(make_request "$method" "$url" "$data" "$use_auth" "$JWT_TOKEN" "$content_type")
    
    local http_code=$(echo "$response" | tail -n1)
    local body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" = "$expected_status" ] || ([ "$expected_status" = "200" ] && [ "$http_code" = "201" ]) || ([ "$expected_status" = "200" ] && [ "$http_code" = "204" ]); then
        PASSED_TESTS=$((PASSED_TESTS + 1))
        echo -e "  ${GREEN}✅ PASS - Status: $http_code${NC}"
        if [ ! -z "$body" ] && [ "$body" != "null" ] && [ "$body" != "[]" ] && [ ${#body} -lt 150 ]; then
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
# SECCIÓN 1: IAM SERVICE - ROLES Y AUTENTICACIÓN
# ============================================
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${CYAN}SECCIÓN 1: IAM SERVICE - ROLES Y AUTENTICACIÓN${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

# 1. GET /api/v1/roles
test_endpoint "GET" "${BASE_URL}/api/v1/roles" "GET /api/v1/roles - Listar todos los roles" "" "false"

response=$(make_request "GET" "${BASE_URL}/api/v1/roles")
body=$(echo "$response" | sed '$d')
ROLE_LAWYER=$(echo "$body" | grep -o '"name":"[^"]*LAWYER[^"]*"' | head -1 | cut -d'"' -f4 || echo "ROLE_LAWYER")
ROLE_CLIENT=$(echo "$body" | grep -o '"name":"[^"]*CLIENT[^"]*"' | head -1 | cut -d'"' -f4 || echo "ROLE_CLIENT")

# 2. POST /api/v1/authentication/sign-up (CLIENT)
TIMESTAMP=$(date +%s)
CLIENT_USERNAME="test_client_${TIMESTAMP}"
LAWYER_USERNAME="test_lawyer_${TIMESTAMP}"
PASSWORD="test123456"

signup_data="{\"username\":\"${CLIENT_USERNAME}\",\"password\":\"${PASSWORD}\",\"role\":\"${ROLE_CLIENT}\"}"
response=$(make_request "POST" "${BASE_URL}/api/v1/authentication/sign-up" "$signup_data" "false" "")
http_code=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$d')
CLIENT_USER_ID=$(echo "$body" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4 || echo "")

if [ "$http_code" = "201" ]; then
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    PASSED_TESTS=$((PASSED_TESTS + 1))
    echo -e "${BLUE}[Test #${TOTAL_TESTS}] POST /api/v1/authentication/sign-up - Crear usuario CLIENT${NC}"
    echo -e "  ${GREEN}✅ PASS - Status: $http_code${NC}"
    echo ""
else
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    FAILED_TESTS=$((FAILED_TESTS + 1))
    echo -e "${BLUE}[Test #${TOTAL_TESTS}] POST /api/v1/authentication/sign-up - Crear usuario CLIENT${NC}"
    echo -e "  ${RED}❌ FAIL - Status: $http_code${NC}"
    echo ""
fi

# 3. POST /api/v1/authentication/sign-up (LAWYER)
signup_data="{\"username\":\"${LAWYER_USERNAME}\",\"password\":\"${PASSWORD}\",\"role\":\"${ROLE_LAWYER}\"}"
response=$(make_request "POST" "${BASE_URL}/api/v1/authentication/sign-up" "$signup_data" "false" "")
http_code=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$d')
LAWYER_USER_ID=$(echo "$body" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4 || echo "")

if [ "$http_code" = "201" ]; then
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    PASSED_TESTS=$((PASSED_TESTS + 1))
    echo -e "${BLUE}[Test #${TOTAL_TESTS}] POST /api/v1/authentication/sign-up - Crear usuario LAWYER${NC}"
    echo -e "  ${GREEN}✅ PASS - Status: $http_code${NC}"
    echo ""
else
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    FAILED_TESTS=$((FAILED_TESTS + 1))
    echo -e "${BLUE}[Test #${TOTAL_TESTS}] POST /api/v1/authentication/sign-up - Crear usuario LAWYER${NC}"
    echo -e "  ${RED}❌ FAIL - Status: $http_code${NC}"
    echo ""
fi

# Esperar a que se creen los perfiles
sleep 3

# 4. POST /api/v1/authentication/sign-in
login_data="{\"username\":\"${CLIENT_USERNAME}\",\"password\":\"${PASSWORD}\"}"
test_endpoint "POST" "${BASE_URL}/api/v1/authentication/sign-in" "POST /api/v1/authentication/sign-in - Login CLIENT" "$login_data" "false" "200"

response=$(make_request "POST" "${BASE_URL}/api/v1/authentication/sign-in" "$login_data" "false" "")
body=$(echo "$response" | sed '$d')
JWT_TOKEN=$(echo "$body" | grep -o '"token":"[^"]*' | cut -d'"' -f4 || echo "")

# 5. POST /api/v1/authentication/sign-up (duplicado - debe fallar)
duplicate_data="{\"username\":\"${CLIENT_USERNAME}\",\"password\":\"${PASSWORD}\",\"role\":\"${ROLE_CLIENT}\"}"
test_endpoint "POST" "${BASE_URL}/api/v1/authentication/sign-up" "POST /api/v1/authentication/sign-up - Usuario duplicado (debe fallar)" "$duplicate_data" "false" "409"

echo ""

# ============================================
# SECCIÓN 2: IAM SERVICE - USUARIOS
# ============================================
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${CYAN}SECCIÓN 2: IAM SERVICE - USUARIOS${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

# 6. GET /api/v1/users
test_endpoint "GET" "${BASE_URL}/api/v1/users" "GET /api/v1/users - Listar todos los usuarios" "" "false"

# 7. GET /api/v1/users/{userId} - CLIENT
if [ ! -z "$CLIENT_USER_ID" ]; then
    test_endpoint "GET" "${BASE_URL}/api/v1/users/${CLIENT_USER_ID}" "GET /api/v1/users/{userId} - Obtener usuario CLIENT" "" "false"
fi

# 8. GET /api/v1/users/{userId} - LAWYER
if [ ! -z "$LAWYER_USER_ID" ]; then
    test_endpoint "GET" "${BASE_URL}/api/v1/users/${LAWYER_USER_ID}" "GET /api/v1/users/{userId} - Obtener usuario LAWYER" "" "false"
fi

# 9. GET /api/v1/users/{userId} - Usuario inexistente
test_endpoint "GET" "${BASE_URL}/api/v1/users/00000000-0000-0000-0000-000000000000" "GET /api/v1/users/{userId} - Usuario inexistente (404)" "" "false" "404"

echo ""

# ============================================
# SECCIÓN 3: PROFILES SERVICE - CLIENTES
# ============================================
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${CYAN}SECCIÓN 3: PROFILES SERVICE - CLIENTES${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

# 10. GET /api/v1/clients
test_endpoint "GET" "${BASE_URL}/api/v1/clients" "GET /api/v1/clients - Listar todos los clientes" "" "true"

# 11. GET /api/v1/clients/{userId}
if [ ! -z "$CLIENT_USER_ID" ]; then
    test_endpoint "GET" "${BASE_URL}/api/v1/clients/${CLIENT_USER_ID}" "GET /api/v1/clients/{userId} - Obtener perfil CLIENT" "" "true"
    
    # 12. PUT /api/v1/clients/{userId} - Usar DNI único basado en timestamp
    UNIQUE_DNI="$(date +%s | tail -c 8)A"
    update_data="{\"firstname\":\"Juan\",\"lastname\":\"Pérez\",\"dni\":\"${UNIQUE_DNI}\",\"contactInfo\":{\"phoneNumber\":\"+51987654321\",\"address\":\"Av. Principal 123\"}}"
    test_endpoint "PUT" "${BASE_URL}/api/v1/clients/${CLIENT_USER_ID}" "PUT /api/v1/clients/{userId} - Actualizar perfil CLIENT" "$update_data" "true" "200"
fi

# 13. GET /api/v1/clients/{userId} - Cliente inexistente
test_endpoint "GET" "${BASE_URL}/api/v1/clients/00000000-0000-0000-0000-000000000000" "GET /api/v1/clients/{userId} - Cliente inexistente (404)" "" "true" "404"

echo ""

# ============================================
# SECCIÓN 4: PROFILES SERVICE - ABOGADOS
# ============================================
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${CYAN}SECCIÓN 4: PROFILES SERVICE - ABOGADOS${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

# 14. GET /api/v1/lawyers
test_endpoint "GET" "${BASE_URL}/api/v1/lawyers" "GET /api/v1/lawyers - Listar todos los abogados" "" "true"

# 15. GET /api/v1/lawyers/{userId}
if [ ! -z "$LAWYER_USER_ID" ]; then
    test_endpoint "GET" "${BASE_URL}/api/v1/lawyers/${LAWYER_USER_ID}" "GET /api/v1/lawyers/{userId} - Obtener perfil LAWYER" "" "true"
    
    # 16. PUT /api/v1/lawyers/{userId} - Usar DNI único basado en timestamp
    UNIQUE_DNI_LAWYER="$(date +%s | tail -c 8)B"
    update_data="{\"firstname\":\"María\",\"lastname\":\"González\",\"dni\":\"${UNIQUE_DNI_LAWYER}\",\"contactInfo\":{\"phoneNumber\":\"+51912345678\",\"address\":\"Calle Secundaria 456\"},\"description\":\"Abogada especializada\"}"
    test_endpoint "PUT" "${BASE_URL}/api/v1/lawyers/${LAWYER_USER_ID}" "PUT /api/v1/lawyers/{userId} - Actualizar perfil LAWYER" "$update_data" "true" "200"
fi

# 17. GET /api/v1/lawyers/{userId} - Abogado inexistente
test_endpoint "GET" "${BASE_URL}/api/v1/lawyers/00000000-0000-0000-0000-000000000000" "GET /api/v1/lawyers/{userId} - Abogado inexistente (404)" "" "true" "404"

echo ""

# ============================================
# SECCIÓN 5: PROFILES SERVICE - ESPECIALIDADES
# ============================================
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${CYAN}SECCIÓN 5: PROFILES SERVICE - ESPECIALIDADES${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

# 18. GET /api/v1/lawyer-specialties
test_endpoint "GET" "${BASE_URL}/api/v1/lawyer-specialties" "GET /api/v1/lawyer-specialties - Listar especialidades" "" "false"

# Obtener especialidades para actualizar
response=$(make_request "GET" "${BASE_URL}/api/v1/lawyer-specialties")
body=$(echo "$response" | sed '$d')
SPECIALTY1=$(echo "$body" | grep -o '"name":"[^"]*"' | head -1 | cut -d'"' -f4 || echo "")
SPECIALTY2=$(echo "$body" | grep -o '"name":"[^"]*"' | head -2 | tail -1 | cut -d'"' -f4 || echo "")

# 19. PUT /api/v1/lawyers/{userId}/specialties
if [ ! -z "$LAWYER_USER_ID" ] && [ ! -z "$SPECIALTY1" ] && [ ! -z "$SPECIALTY2" ]; then
    update_specialties_data="{\"specialties\":[\"${SPECIALTY1}\",\"${SPECIALTY2}\"]}"
    test_endpoint "PUT" "${BASE_URL}/api/v1/lawyers/${LAWYER_USER_ID}/specialties" "PUT /api/v1/lawyers/{userId}/specialties - Actualizar especialidades" "$update_specialties_data" "true" "200"
fi

echo ""

# ============================================
# SECCIÓN 6: CASES SERVICE - CASOS
# ============================================
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${CYAN}SECCIÓN 6: CASES SERVICE - CASOS${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

# 20. POST /api/v1/cases
if [ ! -z "$CLIENT_USER_ID" ]; then
    case_data="{\"title\":\"Caso de prueba\",\"description\":\"Descripción del caso\",\"clientId\":\"${CLIENT_USER_ID}\",\"status\":\"OPEN\"}"
    response=$(make_request "POST" "${BASE_URL}/api/v1/cases" "$case_data" "true" "$JWT_TOKEN")
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    CASE_ID=$(echo "$body" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4 || echo "")
    
    if [ "$http_code" = "201" ]; then
        TOTAL_TESTS=$((TOTAL_TESTS + 1))
        PASSED_TESTS=$((PASSED_TESTS + 1))
        echo -e "${BLUE}[Test #${TOTAL_TESTS}] POST /api/v1/cases - Crear caso${NC}"
        echo -e "  ${GREEN}✅ PASS - Status: $http_code${NC}"
        echo ""
    else
        TOTAL_TESTS=$((TOTAL_TESTS + 1))
        FAILED_TESTS=$((FAILED_TESTS + 1))
        echo -e "${BLUE}[Test #${TOTAL_TESTS}] POST /api/v1/cases - Crear caso${NC}"
        echo -e "  ${RED}❌ FAIL - Status: $http_code${NC}"
        echo ""
    fi
fi

# 21. GET /api/v1/cases
test_endpoint "GET" "${BASE_URL}/api/v1/cases" "GET /api/v1/cases - Listar todos los casos" "" "true"

# 22. GET /api/v1/cases/{caseId}
if [ ! -z "$CASE_ID" ]; then
    test_endpoint "GET" "${BASE_URL}/api/v1/cases/${CASE_ID}" "GET /api/v1/cases/{caseId} - Obtener caso por ID" "" "true"
fi

# 23. GET /api/v1/cases/clients/{clientId}
if [ ! -z "$CLIENT_USER_ID" ]; then
    test_endpoint "GET" "${BASE_URL}/api/v1/cases/clients/${CLIENT_USER_ID}" "GET /api/v1/cases/clients/{clientId} - Obtener casos por cliente" "" "true"
fi

# 24. GET /api/v1/cases/lawyer/{lawyerId}
if [ ! -z "$LAWYER_USER_ID" ]; then
    test_endpoint "GET" "${BASE_URL}/api/v1/cases/lawyer/${LAWYER_USER_ID}" "GET /api/v1/cases/lawyer/{lawyerId} - Obtener casos por abogado" "" "true"
fi

# 25. GET /api/v1/cases/suggested
if [ ! -z "$LAWYER_USER_ID" ]; then
    test_endpoint "GET" "${BASE_URL}/api/v1/cases/suggested?lawyerId=${LAWYER_USER_ID}" "GET /api/v1/cases/suggested - Obtener casos sugeridos" "" "true"
fi

# 26. GET /api/v1/cases/status
test_endpoint "GET" "${BASE_URL}/api/v1/cases/status?status=OPEN" "GET /api/v1/cases/status - Obtener casos por estado" "" "true"

# 27. PUT /api/v1/cases/{caseId}/close
if [ ! -z "$CASE_ID" ] && [ ! -z "$CLIENT_USER_ID" ]; then
    test_endpoint "PUT" "${BASE_URL}/api/v1/cases/${CASE_ID}/close?clientId=${CLIENT_USER_ID}" "PUT /api/v1/cases/{caseId}/close - Cerrar caso" "" "true" "200"
fi

# 28. PUT /api/v1/cases/{caseId}/cancel (crear otro caso primero)
if [ ! -z "$CLIENT_USER_ID" ]; then
    case_data="{\"title\":\"Caso para cancelar\",\"description\":\"Descripción\",\"clientId\":\"${CLIENT_USER_ID}\",\"status\":\"OPEN\"}"
    response=$(make_request "POST" "${BASE_URL}/api/v1/cases" "$case_data" "true" "$JWT_TOKEN")
    body=$(echo "$response" | sed '$d')
    CANCEL_CASE_ID=$(echo "$body" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4 || echo "")
    
    if [ ! -z "$CANCEL_CASE_ID" ]; then
        test_endpoint "PUT" "${BASE_URL}/api/v1/cases/${CANCEL_CASE_ID}/cancel?clientId=${CLIENT_USER_ID}" "PUT /api/v1/cases/{caseId}/cancel - Cancelar caso" "" "true" "200"
    fi
fi

echo ""

# ============================================
# SECCIÓN 7: CASES SERVICE - APLICACIONES
# ============================================
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${CYAN}SECCIÓN 7: CASES SERVICE - APLICACIONES${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

# 29. POST /api/v1/applications
if [ ! -z "$CASE_ID" ] && [ ! -z "$LAWYER_USER_ID" ]; then
    application_data="{\"caseId\":\"${CASE_ID}\",\"lawyerId\":\"${LAWYER_USER_ID}\",\"message\":\"Solicitud de aplicación\"}"
    response=$(make_request "POST" "${BASE_URL}/api/v1/applications" "$application_data" "true" "$JWT_TOKEN")
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    APPLICATION_ID=$(echo "$body" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2 || echo "")
    
    if [ "$http_code" = "201" ]; then
        TOTAL_TESTS=$((TOTAL_TESTS + 1))
        PASSED_TESTS=$((PASSED_TESTS + 1))
        echo -e "${BLUE}[Test #${TOTAL_TESTS}] POST /api/v1/applications - Crear aplicación${NC}"
        echo -e "  ${GREEN}✅ PASS - Status: $http_code${NC}"
        echo ""
    else
        TOTAL_TESTS=$((TOTAL_TESTS + 1))
        FAILED_TESTS=$((FAILED_TESTS + 1))
        echo -e "${BLUE}[Test #${TOTAL_TESTS}] POST /api/v1/applications - Crear aplicación${NC}"
        echo -e "  ${RED}❌ FAIL - Status: $http_code${NC}"
        echo ""
    fi
fi

# 30. GET /api/v1/applications
if [ ! -z "$CASE_ID" ]; then
    test_endpoint "GET" "${BASE_URL}/api/v1/applications?caseId=${CASE_ID}" "GET /api/v1/applications - Listar aplicaciones por caso" "" "true"
fi

# 31. PUT /api/v1/applications/{applicationId}/accept
if [ ! -z "$APPLICATION_ID" ] && [ ! -z "$CLIENT_USER_ID" ]; then
    test_endpoint "PUT" "${BASE_URL}/api/v1/applications/${APPLICATION_ID}/accept?clientId=${CLIENT_USER_ID}" "PUT /api/v1/applications/{applicationId}/accept - Aceptar aplicación" "" "true" "200"
fi

# 32. PUT /api/v1/applications/{applicationId}/reject (crear otra aplicación)
if [ ! -z "$CASE_ID" ] && [ ! -z "$LAWYER_USER_ID" ]; then
    application_data="{\"caseId\":\"${CASE_ID}\",\"lawyerId\":\"${LAWYER_USER_ID}\",\"message\":\"Aplicación para rechazar\"}"
    response=$(make_request "POST" "${BASE_URL}/api/v1/applications" "$application_data" "true" "$JWT_TOKEN")
    body=$(echo "$response" | sed '$d')
    REJECT_APP_ID=$(echo "$body" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2 || echo "")
    
    if [ ! -z "$REJECT_APP_ID" ] && [ ! -z "$CLIENT_USER_ID" ]; then
        test_endpoint "PUT" "${BASE_URL}/api/v1/applications/${REJECT_APP_ID}/reject?clientId=${CLIENT_USER_ID}" "PUT /api/v1/applications/{applicationId}/reject - Rechazar aplicación" "" "true" "200"
    fi
fi

echo ""

# ============================================
# SECCIÓN 8: CASES SERVICE - INVITACIONES
# ============================================
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${CYAN}SECCIÓN 8: CASES SERVICE - INVITACIONES${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

# 33. POST /api/v1/invitations
if [ ! -z "$CASE_ID" ] && [ ! -z "$LAWYER_USER_ID" ] && [ ! -z "$CLIENT_USER_ID" ]; then
    invitation_data="{\"caseId\":\"${CASE_ID}\",\"lawyerId\":\"${LAWYER_USER_ID}\",\"clientId\":\"${CLIENT_USER_ID}\",\"message\":\"Invitación de prueba\"}"
    response=$(make_request "POST" "${BASE_URL}/api/v1/invitations" "$invitation_data" "true" "$JWT_TOKEN")
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    INVITATION_ID=$(echo "$body" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2 || echo "")
    
    if [ "$http_code" = "201" ]; then
        TOTAL_TESTS=$((TOTAL_TESTS + 1))
        PASSED_TESTS=$((PASSED_TESTS + 1))
        echo -e "${BLUE}[Test #${TOTAL_TESTS}] POST /api/v1/invitations - Crear invitación${NC}"
        echo -e "  ${GREEN}✅ PASS - Status: $http_code${NC}"
        echo ""
    else
        TOTAL_TESTS=$((TOTAL_TESTS + 1))
        FAILED_TESTS=$((FAILED_TESTS + 1))
        echo -e "${BLUE}[Test #${TOTAL_TESTS}] POST /api/v1/invitations - Crear invitación${NC}"
        echo -e "  ${RED}❌ FAIL - Status: $http_code${NC}"
        echo ""
    fi
fi

# 34. GET /api/v1/invitations
test_endpoint "GET" "${BASE_URL}/api/v1/invitations" "GET /api/v1/invitations - Listar todas las invitaciones" "" "true"

# 35. GET /api/v1/invitations/case
if [ ! -z "$CASE_ID" ]; then
    test_endpoint "GET" "${BASE_URL}/api/v1/invitations/case?caseId=${CASE_ID}" "GET /api/v1/invitations/case - Listar invitaciones por caso" "" "true"
fi

# 36. PUT /api/v1/invitations/{invitationId}/accept
if [ ! -z "$INVITATION_ID" ] && [ ! -z "$LAWYER_USER_ID" ]; then
    test_endpoint "PUT" "${BASE_URL}/api/v1/invitations/${INVITATION_ID}/accept?lawyerId=${LAWYER_USER_ID}" "PUT /api/v1/invitations/{invitationId}/accept - Aceptar invitación" "" "true" "200"
fi

# 37. PUT /api/v1/invitations/{invitationId}/reject (crear otra invitación)
if [ ! -z "$CASE_ID" ] && [ ! -z "$LAWYER_USER_ID" ] && [ ! -z "$CLIENT_USER_ID" ]; then
    invitation_data="{\"caseId\":\"${CASE_ID}\",\"lawyerId\":\"${LAWYER_USER_ID}\",\"clientId\":\"${CLIENT_USER_ID}\",\"message\":\"Invitación para rechazar\"}"
    response=$(make_request "POST" "${BASE_URL}/api/v1/invitations" "$invitation_data" "true" "$JWT_TOKEN")
    body=$(echo "$response" | sed '$d')
    REJECT_INV_ID=$(echo "$body" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2 || echo "")
    
    if [ ! -z "$REJECT_INV_ID" ] && [ ! -z "$LAWYER_USER_ID" ]; then
        test_endpoint "PUT" "${BASE_URL}/api/v1/invitations/${REJECT_INV_ID}/reject?lawyerId=${LAWYER_USER_ID}" "PUT /api/v1/invitations/{invitationId}/reject - Rechazar invitación" "" "true" "200"
    fi
fi

echo ""

# ============================================
# SECCIÓN 9: CASES SERVICE - COMENTARIOS
# ============================================
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${CYAN}SECCIÓN 9: CASES SERVICE - COMENTARIOS${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

# 38. GET /api/v1/comments
if [ ! -z "$CASE_ID" ]; then
    test_endpoint "GET" "${BASE_URL}/api/v1/comments?caseId=${CASE_ID}" "GET /api/v1/comments - Listar comentarios por caso" "" "true"
fi

# 39. GET /api/v1/comments/lawyer/{lawyerId}/final
if [ ! -z "$LAWYER_USER_ID" ]; then
    test_endpoint "GET" "${BASE_URL}/api/v1/comments/lawyer/${LAWYER_USER_ID}/final" "GET /api/v1/comments/lawyer/{lawyerId}/final - Obtener comentarios finales" "" "true"
fi

# 40. POST /api/v1/comments/general
if [ ! -z "$CASE_ID" ] && [ ! -z "$CLIENT_USER_ID" ]; then
    comment_data="{\"caseId\":\"${CASE_ID}\",\"authorId\":\"${CLIENT_USER_ID}\",\"content\":\"Comentario general de prueba\"}"
    response=$(make_request "POST" "${BASE_URL}/api/v1/comments/general" "$comment_data" "true" "$JWT_TOKEN")
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    COMMENT_ID=$(echo "$body" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2 || echo "")
    
    if [ "$http_code" = "201" ]; then
        TOTAL_TESTS=$((TOTAL_TESTS + 1))
        PASSED_TESTS=$((PASSED_TESTS + 1))
        echo -e "${BLUE}[Test #${TOTAL_TESTS}] POST /api/v1/comments/general - Crear comentario general${NC}"
        echo -e "  ${GREEN}✅ PASS - Status: $http_code${NC}"
        echo ""
    else
        TOTAL_TESTS=$((TOTAL_TESTS + 1))
        FAILED_TESTS=$((FAILED_TESTS + 1))
        echo -e "${BLUE}[Test #${TOTAL_TESTS}] POST /api/v1/comments/general - Crear comentario general${NC}"
        echo -e "  ${RED}❌ FAIL - Status: $http_code${NC}"
        echo ""
    fi
fi

# 41. POST /api/v1/comments/final
if [ ! -z "$CASE_ID" ] && [ ! -z "$LAWYER_USER_ID" ]; then
    comment_data="{\"caseId\":\"${CASE_ID}\",\"lawyerId\":\"${LAWYER_USER_ID}\",\"content\":\"Comentario final de prueba\"}"
    test_endpoint "POST" "${BASE_URL}/api/v1/comments/final" "POST /api/v1/comments/final - Crear comentario final" "$comment_data" "true" "201"
fi

# 42. DELETE /api/v1/comments/{commentId}
if [ ! -z "$COMMENT_ID" ] && [ ! -z "$CLIENT_USER_ID" ]; then
    test_endpoint "DELETE" "${BASE_URL}/api/v1/comments/${COMMENT_ID}?authorId=${CLIENT_USER_ID}" "DELETE /api/v1/comments/{commentId} - Eliminar comentario" "" "true" "204"
fi

echo ""

# ============================================
# SECCIÓN 10: CASES SERVICE - MENSAJES
# ============================================
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${CYAN}SECCIÓN 10: CASES SERVICE - MENSAJES${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

# 43. POST /api/v1/cases/{caseId}/messages
if [ ! -z "$CASE_ID" ] && [ ! -z "$CLIENT_USER_ID" ]; then
    message_data="{\"content\":\"Mensaje de prueba\"}"
    test_endpoint "POST" "${BASE_URL}/api/v1/cases/${CASE_ID}/messages?senderId=${CLIENT_USER_ID}" "POST /api/v1/cases/{caseId}/messages - Enviar mensaje" "$message_data" "true" "201"
fi

# 44. GET /api/v1/cases/{caseId}/messages
if [ ! -z "$CASE_ID" ]; then
    test_endpoint "GET" "${BASE_URL}/api/v1/cases/${CASE_ID}/messages" "GET /api/v1/cases/{caseId}/messages - Listar mensajes del caso" "" "true"
fi

echo ""

# ============================================
# SECCIÓN 11: CASES SERVICE - DOCUMENTOS
# ============================================
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${CYAN}SECCIÓN 11: CASES SERVICE - DOCUMENTOS${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

# 45. GET /api/v1/cases/{caseId}/documents
if [ ! -z "$CASE_ID" ]; then
    test_endpoint "GET" "${BASE_URL}/api/v1/cases/${CASE_ID}/documents" "GET /api/v1/cases/{caseId}/documents - Listar documentos del caso" "" "true"
fi

# 46. POST /api/v1/cases/{caseId}/documents (metadata)
if [ ! -z "$CASE_ID" ] && [ ! -z "$CLIENT_USER_ID" ]; then
    doc_data="{\"filename\":\"documento.pdf\",\"fileUrl\":\"data:application/pdf;base64,JVBERi0xLjQKJdPr6eEKMSAwIG9iago8PAovVHlwZSAvQ2F0YWxvZwovUGFnZXMgMiAwIFIKPj4KZW5kb2JqCjIgMCBvYmoKPDwKL1R5cGUgL1BhZ2VzCi9LaWRzIFszIDAgUl0KL0NvdW50IDEKL01lZGlhQm94IFswIDAgNjEyIDc5Ml0KPj4KZW5kb2JqCjMgMCBvYmoKPDwKL1R5cGUgL1BhZ2UKL1BhcmVudCAyIDAgUgovUmVzb3VyY2VzIDw8Ci9Gb250IDw8Ci9GMSA0IDAgUgo+Pgo+PgovQ29udGVudHMgNSAwIFIKPj4KZW5kb2JqCjQgMCBvYmoKPDwKL1R5cGUgL0ZvbnQKL1N1YnR5cGUgL1R5cGUxCi9CYXNlRm9udCAvSGVsdmV0aWNhCj4+CmVuZG9iago1IDAgb2JqCjw8Ci9MZW5ndGggNDQKPj4Kc3RyZWFtCkJUCi9GMSAxMiBUZgooVGVzdCBQREYpIFRqCkVUCmVuZHN0cmVhbQplbmRvYmoKeHJlZgowIDYKMDAwMDAwMDAwMCA2NTUzNSBmIAowMDAwMDAwMDA5IDAwMDAwIG4gCjAwMDAwMDAwNTggMDAwMDAgbiAKMDAwMDAwMDEzMyAwMDAwMCBuIAowMDAwMDAwMjA4IDAwMDAwIG4gCjAwMDAwMDAyODMgMDAwMDAgbiAKdHJhaWxlcgo8PAovU2l6ZSA2Ci9Sb290IDEgMCBSCj4+CnN0YXJ0eHJlZgozNzMKJSVFT0Y=\",\"fileSize\":500,\"fileType\":\"application/pdf\"}"
    test_endpoint "POST" "${BASE_URL}/api/v1/cases/${CASE_ID}/documents?uploadedBy=${CLIENT_USER_ID}" "POST /api/v1/cases/{caseId}/documents - Subir documento (metadata)" "$doc_data" "true" "201"
fi

echo ""

# ============================================
# RESUMEN FINAL
# ============================================
echo -e "${GREEN}╔════════════════════════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║              ✅ PRUEBAS COMPLETADAS                          ║${NC}"
echo -e "${GREEN}╚════════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${CYAN}📊 RESUMEN FINAL:${NC}"
echo -e "  Total de pruebas ejecutadas: ${TOTAL_TESTS}"
echo -e "  ${GREEN}✅ Exitosas: ${PASSED_TESTS}${NC}"
echo -e "  ${RED}❌ Fallidas: ${FAILED_TESTS}${NC}"
echo ""

if [ "$FAILED_TESTS" -eq 0 ]; then
    echo -e "${GREEN}🎉 ¡TODAS LAS PRUEBAS PASARON!${NC}"
    exit 0
else
    echo -e "${YELLOW}⚠️  Algunas pruebas fallaron. Revisa los errores arriba.${NC}"
    exit 1
fi

