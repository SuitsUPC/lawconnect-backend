#!/bin/bash

# Script para probar la creación de usuarios y verificar que se crean los perfiles
# Uso: ./test-user-profile.sh [local|cloud] [base-url]

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

echo -e "${BLUE}🧪 Probando creación de usuario y perfil${NC}"
echo -e "${BLUE}Entorno: ${ENV}${NC}"
echo -e "${BLUE}Base URL: ${BASE_URL}${NC}"
echo ""

# Función para hacer una petición HTTP
make_request() {
    local method=$1
    local url=$2
    local data=$3
    local use_auth=${4:-false}
    local token=$5
    
    local headers="Content-Type: application/json"
    if [ "$use_auth" = "true" ] && [ ! -z "$token" ]; then
        headers="Content-Type: application/json
Authorization: Bearer $token"
    fi
    
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
    else
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
    fi
}

# 1. Obtener roles disponibles
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}Paso 1: Obteniendo roles disponibles${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

response=$(make_request "GET" "${BASE_URL}/api/v1/roles")
http_code=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$d')

if [ "$http_code" = "200" ]; then
    echo -e "${GREEN}✅ Roles obtenidos correctamente${NC}"
    echo "$body" | python3 -m json.tool 2>/dev/null || echo "$body"
    echo ""
    
    # Extraer nombres de roles
    ROLE_LAWYER=$(echo "$body" | grep -o '"name":"[^"]*LAWYER[^"]*"' | head -1 | cut -d'"' -f4 || echo "ROLE_LAWYER")
    ROLE_CLIENT=$(echo "$body" | grep -o '"name":"[^"]*CLIENT[^"]*"' | head -1 | cut -d'"' -f4 || echo "ROLE_CLIENT")
    
    echo -e "${BLUE}Roles encontrados:${NC}"
    echo -e "  - Lawyer: ${ROLE_LAWYER}"
    echo -e "  - Client: ${ROLE_CLIENT}"
    echo ""
else
    echo -e "${RED}❌ Error al obtener roles: Status $http_code${NC}"
    echo "$body"
    exit 1
fi

# 2. Crear usuario CLIENT
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}Paso 2: Creando usuario CLIENT${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

TIMESTAMP=$(date +%s)
CLIENT_USERNAME="test_client_${TIMESTAMP}"
CLIENT_PASSWORD="test123456"

signup_data="{\"username\":\"${CLIENT_USERNAME}\",\"password\":\"${CLIENT_PASSWORD}\",\"role\":\"${ROLE_CLIENT}\"}"

echo -e "${BLUE}Datos de registro:${NC}"
echo -e "  Username: ${CLIENT_USERNAME}"
echo -e "  Role: ${ROLE_CLIENT}"
echo ""

response=$(make_request "POST" "${BASE_URL}/api/v1/authentication/sign-up" "$signup_data")
http_code=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$d')

if [ "$http_code" = "201" ]; then
    echo -e "${GREEN}✅ Usuario CLIENT creado correctamente${NC}"
    echo "$body" | python3 -m json.tool 2>/dev/null || echo "$body"
    echo ""
    
    # Extraer userId
    CLIENT_USER_ID=$(echo "$body" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4 || echo "")
    
    if [ ! -z "$CLIENT_USER_ID" ]; then
        echo -e "${GREEN}✅ User ID extraído: ${CLIENT_USER_ID}${NC}"
        echo ""
    else
        echo -e "${YELLOW}⚠️  No se pudo extraer el User ID${NC}"
    fi
else
    echo -e "${RED}❌ Error al crear usuario CLIENT: Status $http_code${NC}"
    echo "$body"
    exit 1
fi

# 3. Verificar que se creó el perfil CLIENT
if [ ! -z "$CLIENT_USER_ID" ]; then
    echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${BLUE}Paso 3: Verificando perfil CLIENT creado${NC}"
    echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    
    # Primero hacer login para obtener token
    login_data="{\"username\":\"${CLIENT_USERNAME}\",\"password\":\"${CLIENT_PASSWORD}\"}"
    login_response=$(make_request "POST" "${BASE_URL}/api/v1/authentication/sign-in" "$login_data")
    login_http_code=$(echo "$login_response" | tail -n1)
    login_body=$(echo "$login_response" | sed '$d')
    
    if [ "$login_http_code" = "200" ]; then
        TOKEN=$(echo "$login_body" | grep -o '"token":"[^"]*' | cut -d'"' -f4 || echo "")
        
        if [ ! -z "$TOKEN" ]; then
            echo -e "${GREEN}✅ Login exitoso, token obtenido${NC}"
            echo ""
            
            # Buscar el perfil del cliente
            response=$(make_request "GET" "${BASE_URL}/api/v1/clients/${CLIENT_USER_ID}" "" "true" "$TOKEN")
            http_code=$(echo "$response" | tail -n1)
            body=$(echo "$response" | sed '$d')
            
            if [ "$http_code" = "200" ]; then
                echo -e "${GREEN}✅ Perfil CLIENT encontrado correctamente${NC}"
                echo "$body" | python3 -m json.tool 2>/dev/null || echo "$body"
                echo ""
            else
                echo -e "${RED}❌ Perfil CLIENT NO encontrado: Status $http_code${NC}"
                echo "$body"
                echo ""
                echo -e "${YELLOW}⚠️  El usuario se creó pero el perfil NO se creó automáticamente${NC}"
            fi
        else
            echo -e "${YELLOW}⚠️  No se pudo obtener el token${NC}"
        fi
    else
        echo -e "${YELLOW}⚠️  Error en login: Status $login_http_code${NC}"
    fi
fi

# 4. Crear usuario LAWYER
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}Paso 4: Creando usuario LAWYER${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

LAWYER_USERNAME="test_lawyer_${TIMESTAMP}"
LAWYER_PASSWORD="test123456"

signup_data="{\"username\":\"${LAWYER_USERNAME}\",\"password\":\"${LAWYER_PASSWORD}\",\"role\":\"${ROLE_LAWYER}\"}"

echo -e "${BLUE}Datos de registro:${NC}"
echo -e "  Username: ${LAWYER_USERNAME}"
echo -e "  Role: ${ROLE_LAWYER}"
echo ""

response=$(make_request "POST" "${BASE_URL}/api/v1/authentication/sign-up" "$signup_data")
http_code=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$d')

if [ "$http_code" = "201" ]; then
    echo -e "${GREEN}✅ Usuario LAWYER creado correctamente${NC}"
    echo "$body" | python3 -m json.tool 2>/dev/null || echo "$body"
    echo ""
    
    # Extraer userId
    LAWYER_USER_ID=$(echo "$body" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4 || echo "")
    
    if [ ! -z "$LAWYER_USER_ID" ]; then
        echo -e "${GREEN}✅ User ID extraído: ${LAWYER_USER_ID}${NC}"
        echo ""
    else
        echo -e "${YELLOW}⚠️  No se pudo extraer el User ID${NC}"
    fi
else
    echo -e "${RED}❌ Error al crear usuario LAWYER: Status $http_code${NC}"
    echo "$body"
    exit 1
fi

# 5. Verificar que se creó el perfil LAWYER
if [ ! -z "$LAWYER_USER_ID" ]; then
    echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${BLUE}Paso 5: Verificando perfil LAWYER creado${NC}"
    echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    
    # Hacer login para obtener token
    login_data="{\"username\":\"${LAWYER_USERNAME}\",\"password\":\"${LAWYER_PASSWORD}\"}"
    login_response=$(make_request "POST" "${BASE_URL}/api/v1/authentication/sign-in" "$login_data")
    login_http_code=$(echo "$login_response" | tail -n1)
    login_body=$(echo "$login_response" | sed '$d')
    
    if [ "$login_http_code" = "200" ]; then
        TOKEN=$(echo "$login_body" | grep -o '"token":"[^"]*' | cut -d'"' -f4 || echo "")
        
        if [ ! -z "$TOKEN" ]; then
            echo -e "${GREEN}✅ Login exitoso, token obtenido${NC}"
            echo ""
            
            # Buscar el perfil del abogado
            response=$(make_request "GET" "${BASE_URL}/api/v1/lawyers/${LAWYER_USER_ID}" "" "true" "$TOKEN")
            http_code=$(echo "$response" | tail -n1)
            body=$(echo "$response" | sed '$d')
            
            if [ "$http_code" = "200" ]; then
                echo -e "${GREEN}✅ Perfil LAWYER encontrado correctamente${NC}"
                echo "$body" | python3 -m json.tool 2>/dev/null || echo "$body"
                echo ""
            else
                echo -e "${RED}❌ Perfil LAWYER NO encontrado: Status $http_code${NC}"
                echo "$body"
                echo ""
                echo -e "${YELLOW}⚠️  El usuario se creó pero el perfil NO se creó automáticamente${NC}"
            fi
        else
            echo -e "${YELLOW}⚠️  No se pudo obtener el token${NC}"
        fi
    else
        echo -e "${YELLOW}⚠️  Error en login: Status $login_http_code${NC}"
    fi
fi

echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${GREEN}✅ Pruebas completadas!${NC}"
echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
echo -e "${BLUE}💡 Resumen:${NC}"
echo -e "  - Usuario CLIENT creado: ${CLIENT_USERNAME}"
echo -e "  - Usuario LAWYER creado: ${LAWYER_USERNAME}"
echo ""
echo -e "${BLUE}📝 Para ver los logs del servicio IAM:${NC}"
if [ "$ENV" = "local" ]; then
    echo -e "  docker logs iam-service | grep -i profile"
else
    echo -e "  Revisa los logs del servicio IAM en la nube"
fi
echo ""

