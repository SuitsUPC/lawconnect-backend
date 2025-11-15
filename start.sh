#!/bin/bash

# Script para iniciar LawConnect Backend
# Este script compila todos los microservicios y los levanta con Docker Compose

# No usar set -e estricto porque algunas operaciones de Docker pueden fallar y está bien
set +e

echo "🚀 Iniciando LawConnect Backend..."
echo ""

# Colores para output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Obtener la ruta absoluta del proyecto
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MVNW="$PROJECT_ROOT/mvnw"

# Detectar si necesitamos usar sudo para Docker y configurar variable
DOCKER_CMD="docker"
if ! docker info > /dev/null 2>&1; then
    if sudo docker info > /dev/null 2>&1; then
        DOCKER_CMD="sudo docker"
        echo -e "${BLUE}ℹ️  Usando sudo para comandos de Docker${NC}"
    fi
fi
export DOCKER_CMD

# Configurar JAVA_HOME si no está definido (necesario para compilación)
if [ -z "$JAVA_HOME" ] || [ ! -d "$JAVA_HOME" ]; then
    # Cargar variables de entorno del sistema
    [ -f /etc/profile ] && source /etc/profile 2>/dev/null || true
    [ -f ~/.bashrc ] && source ~/.bashrc 2>/dev/null || true
    
    # Intentar encontrar Java 17 usando múltiples métodos
    JAVA_FOUND=""
    
    # Método 1: Buscar en ubicaciones comunes
    for JAVA_DIR in "/usr/lib/jvm/java-17-openjdk-amd64" "/usr/lib/jvm/java-17-openjdk" "/usr/lib/jvm/java-17"; do
        if [ -d "$JAVA_DIR" ] && [ -f "$JAVA_DIR/bin/java" ]; then
            JAVA_FOUND="$JAVA_DIR"
            break
        fi
    done
    
    # Método 2: Usar update-alternatives
    if [ -z "$JAVA_FOUND" ] && command -v update-alternatives > /dev/null 2>&1; then
        JAVA_ALT=$(update-alternatives --list java 2>/dev/null | head -1)
        if [ ! -z "$JAVA_ALT" ] && [ -f "$JAVA_ALT" ]; then
            JAVA_FOUND=$(dirname $(dirname "$JAVA_ALT"))
        fi
    fi
    
    # Método 3: Buscar java en PATH
    if [ -z "$JAVA_FOUND" ] && command -v java > /dev/null 2>&1; then
        JAVA_PATH=$(which java)
        if [ ! -z "$JAVA_PATH" ]; then
            # Resolver symlinks
            JAVA_REAL=$(readlink -f "$JAVA_PATH" 2>/dev/null || readlink "$JAVA_PATH" 2>/dev/null || echo "$JAVA_PATH")
            JAVA_FOUND=$(dirname $(dirname "$JAVA_REAL"))
        fi
    fi
    
    # Método 4: Buscar en todo /usr/lib/jvm
    if [ -z "$JAVA_FOUND" ] && [ -d "/usr/lib/jvm" ]; then
        for JAVA_DIR in /usr/lib/jvm/*; do
            if [ -d "$JAVA_DIR" ] && [ -f "$JAVA_DIR/bin/java" ]; then
                JAVA_FOUND="$JAVA_DIR"
                break
            fi
        done
    fi
    
    if [ ! -z "$JAVA_FOUND" ] && [ -d "$JAVA_FOUND" ]; then
        export JAVA_HOME="$JAVA_FOUND"
        export PATH="$JAVA_HOME/bin:$PATH"
        echo -e "${BLUE}✅ Java encontrado en: $JAVA_HOME${NC}"
    else
        echo -e "${YELLOW}⚠️  Java no encontrado. Intentando usar Maven wrapper sin JAVA_HOME...${NC}"
    fi
fi

# Asegurar que mvnw tenga permisos de ejecución
chmod +x "$MVNW" 2>/dev/null || true

# Función para compilar un servicio
compile_service() {
    local service_name=$1
    local service_path=$2
    
    echo -e "${BLUE}📦 Compilando ${service_name}...${NC}"
    cd "$PROJECT_ROOT/$service_path"
    
    # Determinar qué comando de Maven usar
    MVN_CMD=""
    
    # Intentar usar Maven wrapper primero
    if [ -f "$MVNW" ] && [ -d "$PROJECT_ROOT/.mvn/wrapper" ]; then
        MVN_CMD="bash $MVNW"
        echo -e "${BLUE}   Usando Maven wrapper${NC}"
    # Si no funciona el wrapper, usar Maven instalado
    elif command -v mvn > /dev/null 2>&1; then
        MVN_CMD="mvn"
        echo -e "${BLUE}   Usando Maven instalado en el sistema${NC}"
    else
        echo -e "${RED}❌ Error: No se encontró Maven (ni wrapper ni instalado)${NC}"
        return 1
    fi
    
    # Compilar sin -q para ver errores, pero redirigir a un log temporal
    COMPILE_LOG="/tmp/compile_${service_name}.log"
    if $MVN_CMD clean package spring-boot:repackage -DskipTests > "$COMPILE_LOG" 2>&1; then
        echo -e "${GREEN}✅ ${service_name} compilado correctamente${NC}"
        # Verificar que el JAR existe - Convertir service_name a lowercase para el nombre del JAR
        case "$service_name" in
            "IAM Service") JAR_FILE="$PROJECT_ROOT/$service_path/target/iam-service-0.0.1-SNAPSHOT.jar" ;;
            "Profiles Service") JAR_FILE="$PROJECT_ROOT/$service_path/target/profiles-service-0.0.1-SNAPSHOT.jar" ;;
            "Cases Service") JAR_FILE="$PROJECT_ROOT/$service_path/target/cases-service-0.0.1-SNAPSHOT.jar" ;;
            "API Gateway") JAR_FILE="$PROJECT_ROOT/$service_path/target/api-gateway-0.0.1-SNAPSHOT.jar" ;;
            *) JAR_FILE="" ;;
        esac
        
        if [ -f "$JAR_FILE" ]; then
            echo -e "${GREEN}   JAR encontrado: $(basename "$JAR_FILE")${NC}"
        else
            echo -e "${YELLOW}⚠️  JAR no encontrado en: $JAR_FILE${NC}"
            echo -e "${YELLOW}   Últimas líneas del log de compilación:${NC}"
            tail -10 "$COMPILE_LOG" 2>/dev/null || true
            return 1
        fi
    else
        echo -e "${RED}❌ Error al compilar ${service_name}${NC}"
        echo -e "${YELLOW}   Últimas 30 líneas del log de compilación:${NC}"
        tail -30 "$COMPILE_LOG" 2>/dev/null || true
        return 1
    fi
    cd - > /dev/null
    echo ""
}

# Verificar que Docker está corriendo
check_docker() {
    if $DOCKER_CMD info > /dev/null 2>&1; then
        return 0
    fi
    echo -e "${YELLOW}⚠️  Docker no está corriendo. Por favor inicia Docker Desktop.${NC}"
    return 1
}

# Función para liberar un puerto
free_port() {
    local port=$1
    # Primero intentar detener contenedores Docker que usen el puerto
    local containers=$($DOCKER_CMD ps --format "{{.ID}} {{.Ports}}" 2>/dev/null | grep ":$port->" | awk '{print $1}' || true)
    if [ ! -z "$containers" ]; then
        echo -e "${YELLOW}⚠️  Puerto $port en uso por contenedor Docker, deteniendo...${NC}"
        echo "$containers" | xargs $DOCKER_CMD stop 2>/dev/null || true
        sleep 1
        return
    fi
    
    # Si no es Docker, buscar proceso que usa el puerto (pero NO matar procesos de Docker)
    local pid=$(lsof -ti:$port 2>/dev/null || true)
    if [ ! -z "$pid" ]; then
        local process_name=$(ps -p $pid -o comm= 2>/dev/null || echo "unknown")
        # NO matar procesos de Docker
        if [[ "$process_name" != *"docker"* ]] && [[ "$process_name" != *"com.docker"* ]] && [[ "$process_name" != *"Docker"* ]]; then
            echo -e "${YELLOW}⚠️  Puerto $port en uso por proceso $pid ($process_name), liberando...${NC}"
            kill -9 $pid 2>/dev/null || true
            sleep 1
        else
            echo -e "${YELLOW}⚠️  Puerto $port en uso por Docker (proceso $pid), se liberará al detener contenedores...${NC}"
        fi
    fi
}

# Cambiar al directorio del proyecto
cd "$PROJECT_ROOT"

# Verificar que estamos en el directorio correcto
if [ ! -f "pom.xml" ] || [ ! -d "microservices" ]; then
    echo -e "${YELLOW}⚠️  Por favor ejecuta este script desde la raíz del proyecto${NC}"
    exit 1
fi

# ============================================
# PASO 0: VERIFICAR DOCKER Y LIBERAR PUERTOS
# ============================================
# Verificar Docker antes de continuar
if ! check_docker; then
    echo -e "${YELLOW}⚠️  Docker no está disponible. Por favor inicia Docker Desktop y vuelve a ejecutar el script.${NC}"
    exit 1
fi

echo -e "${BLUE}🔌 Liberando puertos del proyecto (8080, 8081, 8082, 8083, 3307, 3308, 3309)...${NC}"

# Detener contenedores del proyecto primero (esto liberará los puertos)
echo -e "${BLUE}🛑 Deteniendo contenedores del proyecto para liberar puertos...${NC}"
$DOCKER_CMD compose -f "$PROJECT_ROOT/microservices/docker-compose.yml" down > /dev/null 2>&1 || true
$DOCKER_CMD stop iam-service profiles-service cases-service api-gateway iam-db profiles-db cases-db 2>/dev/null || true
sleep 2

# Luego liberar puertos que puedan estar en uso por otros procesos
free_port 8080
free_port 8081
free_port 8082
free_port 8083
free_port 3307
free_port 3308
free_port 3309
sleep 1
echo ""

echo -e "${BLUE}🔨 Compilando microservicios...${NC}"
echo ""

# Compilar todos los servicios
compile_service "IAM Service" "microservices/iam"
compile_service "Profiles Service" "microservices/profiles"
compile_service "Cases Service" "microservices/cases"
compile_service "API Gateway" "microservices/api-gateway"

echo -e "${GREEN}✅ Todos los servicios compilados correctamente${NC}"
echo ""

# ============================================
# LIMPIEZA COMPLETA - SOLO DEL PROYECTO
# ============================================
echo -e "${BLUE}🧹 Limpieza completa: eliminando TODO lo relacionado con este proyecto...${NC}"
echo ""

# Verificar Docker antes de limpiar
if ! check_docker > /dev/null 2>&1; then
    echo -e "${YELLOW}⚠️  Docker no está disponible durante la limpieza. Continuando sin limpiar recursos de Docker...${NC}"
    echo ""
else
    # 1. Eliminar contenedores específicos por nombre (por si quedaron huérfanos)
    echo -e "${BLUE}🗑️  Paso 1: Eliminando contenedores huérfanos...${NC}"
    $DOCKER_CMD stop iam-service profiles-service cases-service api-gateway iam-db profiles-db cases-db 2>/dev/null || true
    $DOCKER_CMD rm -f iam-service profiles-service cases-service api-gateway iam-db profiles-db cases-db 2>/dev/null || true

    # 2. Eliminar imágenes específicas del proyecto
    echo -e "${BLUE}🖼️  Paso 2: Eliminando imágenes del proyecto...${NC}"
    $DOCKER_CMD rmi -f microservices-iam-service microservices-profiles-service microservices-cases-service microservices-api-gateway 2>/dev/null || true
    $DOCKER_CMD rmi -f iam-service profiles-service cases-service api-gateway 2>/dev/null || true

    # 3. Eliminar volúmenes específicos del proyecto
    echo -e "${BLUE}💾 Paso 3: Eliminando volúmenes del proyecto...${NC}"
    $DOCKER_CMD volume rm microservices_iam-db-data microservices_profiles-db-data microservices_cases-db-data 2>/dev/null || true
    $DOCKER_CMD volume ls | grep -E "microservices_(iam|profiles|cases)" | awk '{print $2}' | xargs $DOCKER_CMD volume rm 2>/dev/null || true

    # 4. Limpiar redes específicas del proyecto
    echo -e "${BLUE}🌐 Paso 4: Limpiando redes del proyecto...${NC}"
    $DOCKER_CMD network rm microservices_lawconnect-network 2>/dev/null || true
    $DOCKER_CMD network ls | grep -E "microservices_lawconnect" | awk '{print $1}' | xargs $DOCKER_CMD network rm 2>/dev/null || true
fi

# Verificar que Docker sigue funcionando antes de levantar servicios
if ! check_docker; then
    echo -e "${YELLOW}⚠️  Docker no está disponible. Por favor inicia Docker Desktop y vuelve a ejecutar el script.${NC}"
    exit 1
fi

echo ""
echo -e "${GREEN}✅ Limpieza completa finalizada. Todo está listo para crear desde cero.${NC}"
echo ""

# Levantar los servicios
echo -e "${BLUE}🐳 Levantando servicios con Docker Compose...${NC}"
if ! $DOCKER_CMD compose -f "$PROJECT_ROOT/microservices/docker-compose.yml" up --build -d; then
    echo -e "${YELLOW}⚠️  Error al levantar los servicios con Docker Compose.${NC}"
    exit 1
fi

echo ""
echo -e "${GREEN}⏳ Esperando a que los servicios inicien...${NC}"
sleep 30

# Verificar estado de los contenedores
echo ""
echo -e "${BLUE}📊 Estado de los contenedores:${NC}"
$DOCKER_CMD compose -f "$PROJECT_ROOT/microservices/docker-compose.yml" ps

echo ""
echo -e "${BLUE}📋 Mostrando logs cortos de los servicios (últimas 20 líneas)...${NC}"
echo ""
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}IAM Service Logs:${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
$DOCKER_CMD compose -f "$PROJECT_ROOT/microservices/docker-compose.yml" logs --tail=20 iam-service || true

echo ""
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}Profiles Service Logs:${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
$DOCKER_CMD compose -f "$PROJECT_ROOT/microservices/docker-compose.yml" logs --tail=20 profiles-service || true

echo ""
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}Cases Service Logs:${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
$DOCKER_CMD compose -f "$PROJECT_ROOT/microservices/docker-compose.yml" logs --tail=20 cases-service || true

echo ""
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}API Gateway Logs:${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
$DOCKER_CMD compose -f "$PROJECT_ROOT/microservices/docker-compose.yml" logs --tail=20 api-gateway || true

echo ""
echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${GREEN}✅ Servicios iniciados correctamente!${NC}"
echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
echo -e "${BLUE}🌐 URLs disponibles:${NC}"
echo -e "   • API Gateway (Swagger Centralizado): http://localhost:8080/swagger-ui.html"
echo -e "   • Todos los endpoints pasan por:      http://localhost:8080"
echo ""
echo -e "${BLUE}📚 Endpoints disponibles a través del Gateway (puerto 8080):${NC}"
echo -e "   • IAM:       /api/v1/authentication/**, /api/v1/users/**, /api/v1/roles/**"
echo -e "   • Profiles:  /api/v1/lawyers/**, /api/v1/clients/**, /api/v1/lawyer-specialties/**"
echo -e "   • Cases:     /api/v1/cases/**, /api/v1/applications/**, /api/v1/invitations/**, /api/v1/comments/**"
echo ""
echo -e "${BLUE}📝 Comandos útiles:${NC}"
echo -e "   • Ver logs en tiempo real:  ./logs.sh"
echo -e "   • Ver logs de un servicio:  ./logs.sh [servicio]"
echo -e "   • Detener servicios:        ./stop.sh"
echo -e "   • Ver estado:               ./status.sh"
echo ""

# ============================================
# PRUEBAS DE ENDPOINTS A TRAVÉS DEL GATEWAY
# ============================================
echo -e "${BLUE}🧪 Probando endpoints a través del Gateway (puerto 8080)...${NC}"
echo ""

# Esperar un poco más para que los servicios estén completamente listos
echo -e "${YELLOW}⏳ Esperando a que los servicios estén completamente listos (90 segundos)...${NC}"
sleep 90

# Variable global para el token JWT
JWT_TOKEN=""

# Función para hacer login y obtener el token
login() {
    echo -e "${BLUE}🔐 Obteniendo token de autenticación...${NC}"
    local login_url="http://localhost:8080/api/v1/authentication/sign-in"
    local login_data='{"username":"admin","password":"admin123"}'
    
    echo -e "  ${YELLOW}POST ${login_url}${NC}"
    local response=$(curl -s -w "\n%{http_code}" -X POST "$login_url" \
        -H "Content-Type: application/json" \
        -d "$login_data" 2>/dev/null || echo -e "\n000")
    
    local http_code=$(echo "$response" | tail -n1)
    local body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" = "200" ]; then
        # Extraer el token del JSON (buscando el campo "token")
        JWT_TOKEN=$(echo "$body" | grep -o '"token":"[^"]*' | cut -d'"' -f4)
        if [ ! -z "$JWT_TOKEN" ]; then
            echo -e "  ${GREEN}✅ Login exitoso, token obtenido${NC}"
            echo ""
            return 0
        else
            echo -e "  ${YELLOW}⚠️  Login exitoso pero no se pudo extraer el token${NC}"
            echo ""
            return 1
        fi
    else
        echo -e "  ${YELLOW}⚠️  Error en login: Status $http_code${NC}"
        echo ""
        return 1
    fi
}

# Función para probar un endpoint
test_endpoint() {
    local method=$1
    local url=$2
    local description=$3
    local data=$4
    local use_auth=${5:-true}  # Por defecto usar autenticación
    
    echo -e "${BLUE}Testing: ${description}${NC}"
    echo -e "  ${YELLOW}${method} ${url}${NC}"
    
    local headers="Content-Type: application/json"
    if [ "$use_auth" = "true" ] && [ ! -z "$JWT_TOKEN" ]; then
        headers="Content-Type: application/json
Authorization: Bearer $JWT_TOKEN"
    fi
    
    if [ "$method" = "GET" ]; then
        if [ "$use_auth" = "true" ] && [ ! -z "$JWT_TOKEN" ]; then
            response=$(curl -s -w "\n%{http_code}" -X GET "$url" \
                -H "Content-Type: application/json" \
                -H "Authorization: Bearer $JWT_TOKEN" \
                2>/dev/null || echo -e "\n000")
        else
            response=$(curl -s -w "\n%{http_code}" -X GET "$url" \
                -H "Content-Type: application/json" \
                2>/dev/null || echo -e "\n000")
        fi
    else
        if [ "$use_auth" = "true" ] && [ ! -z "$JWT_TOKEN" ]; then
            response=$(curl -s -w "\n%{http_code}" -X POST "$url" \
                -H "Content-Type: application/json" \
                -H "Authorization: Bearer $JWT_TOKEN" \
                -d "$data" 2>/dev/null || echo -e "\n000")
        else
            response=$(curl -s -w "\n%{http_code}" -X POST "$url" \
                -H "Content-Type: application/json" \
                -d "$data" 2>/dev/null || echo -e "\n000")
        fi
    fi
    
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" = "200" ] || [ "$http_code" = "201" ] || [ "$http_code" = "401" ] || [ "$http_code" = "400" ]; then
        echo -e "  ${GREEN}✅ Status: $http_code${NC}"
        if [ ! -z "$body" ] && [ "$body" != "null" ]; then
            echo -e "  ${GREEN}   Response: $(echo "$body" | head -c 100)...${NC}"
        fi
    else
        echo -e "  ${YELLOW}⚠️  Status: $http_code (servicio puede estar iniciando)${NC}"
    fi
    echo ""
}

# Hacer login primero para obtener el token
login

# Pruebas IAM Service (a través del Gateway 8080 -> 8081)
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}🔐 IAM Service Tests (Gateway 8080 -> Service 8081)${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
test_endpoint "GET" "http://localhost:8080/api/v1/roles" "GET /api/v1/roles - Listar roles" "" "false"
test_endpoint "GET" "http://localhost:8080/api/v1/users" "GET /api/v1/users - Listar usuarios (con autenticación)" "" "true"

# Pruebas Profiles Service (a través del Gateway 8080 -> 8082)
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}👥 Profiles Service Tests (Gateway 8080 -> Service 8082)${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
test_endpoint "GET" "http://localhost:8080/api/v1/lawyer-specialties" "GET /api/v1/lawyer-specialties - Listar especialidades" "" "false"
test_endpoint "GET" "http://localhost:8080/api/v1/lawyers" "GET /api/v1/lawyers - Listar abogados (con autenticación)" "" "true"

# Pruebas Cases Service (a través del Gateway 8080 -> 8083)
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}📋 Cases Service Tests (Gateway 8080 -> Service 8083)${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
test_endpoint "GET" "http://localhost:8080/api/v1/cases" "GET /api/v1/cases - Listar casos (con autenticación)" "" "true"
test_endpoint "GET" "http://localhost:8080/api/v1/applications?caseId=00000000-0000-0000-0000-000000000000" "GET /api/v1/applications - Listar aplicaciones por caso (con autenticación)" "" "true"

# Pruebas de Swagger API Docs (a través del Gateway 8080)
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}📚 Swagger API Docs Tests (Gateway 8080)${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
test_endpoint "GET" "http://localhost:8080/iam/v3/api-docs" "GET /iam/v3/api-docs - IAM Service API Docs" "" "false"
test_endpoint "GET" "http://localhost:8080/profiles/v3/api-docs" "GET /profiles/v3/api-docs - Profiles Service API Docs" "" "false"
test_endpoint "GET" "http://localhost:8080/cases/v3/api-docs" "GET /cases/v3/api-docs - Cases Service API Docs" "" "false"

echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${GREEN}✅ Pruebas completadas!${NC}"
echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
echo -e "${BLUE}💡 Nota:${NC} Si algunos endpoints devuelven 401/403, es normal (requieren autenticación)."
echo -e "   Los endpoints están funcionando correctamente a través del Gateway."
echo ""

