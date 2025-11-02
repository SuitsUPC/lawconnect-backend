# Software Deployment Evidence for Sprint Review
## LawConnect Microservices Platform

---

## Introducción

Durante el presente Sprint, se realizó el despliegue completo de la plataforma de microservicios LawConnect en una infraestructura cloud utilizando Microsoft Azure. El proceso abarcó desde la configuración inicial de recursos en el proveedor de servicios cloud hasta la automatización del despliegue de los Web Services mediante contenedores Docker y orquestación con Docker Compose. Se estableció un entorno de producción operativo con un punto de entrada único a través de Nginx, permitiendo el acceso externo a los microservicios mediante el API Gateway.

Este documento presenta la evidencia visual y técnica de los pasos ejecutados para lograr el despliegue exitoso de los componentes de la plataforma, incluyendo la configuración de la máquina virtual, la instalación de dependencias, la construcción de los microservicios y la puesta en marcha de los servicios contenerizados.

---

## 1. Configuración de Recursos en Azure

### 1.1 Creación de la Máquina Virtual

La infraestructura de despliegue se estableció mediante la creación de una Máquina Virtual en Microsoft Azure, configurada específicamente para alojar la arquitectura de microservicios de LawConnect.

![Captura de pantalla de la creación de una máquina virtual en Azure. Se muestran los detalles del proyecto (Suscripción: Azure for Students, Grupo de recursos: rg-lawconnect) y los detalles de la instancia (Nombre de máquina virtual: vm-lawconnectbackend-prod-001, Región: West US 3, Zona de disponibilidad: Zona 1). Los botones de navegación "Siguiente: Discos >" y "Revisar y crear" están visibles en la parte inferior.](image_placeholder_azure_vm_creation.png)
*Captura 1: Configuración inicial de la máquina virtual en Microsoft Azure.*

**Explicación:**
Esta captura de pantalla documenta la fase de aprovisionamiento de recursos en la infraestructura cloud. Se estableció la suscripción "Azure for Students" como contexto de facturación y se creó un nuevo grupo de recursos denominado "rg-lawconnect" para centralizar la gestión de los recursos del proyecto. La máquina virtual fue configurada con el nombre "vm-lawconnectbackend-prod-001" para identificar su propósito como servidor de producción del backend de LawConnect. La selección de la región "West US 3" y la "Zona 1" de disponibilidad garantiza una configuración estándar con alta disponibilidad y redundancia geográfica dentro del mismo centro de datos.

---

### 1.2 Configuración de Red y Seguridad

Una vez creada la máquina virtual, fue necesario configurar las reglas de firewall del Network Security Group (NSG) para permitir el tráfico HTTP entrante desde Internet hacia el servidor.

![The image displays a screenshot of the Microsoft Azure portal, specifically the "Network configuration" section for a Virtual Machine named "vm-lawconnectbackend-prod-001". The screenshot shows the network settings of an Azure Virtual Machine. The main content area is focused on the network interface details and the inbound security rules configured for the associated Network Security Group (NSG). The top of the page indicates the user is navigating within the Azure portal, viewing the VM's network configuration.](image_placeholder_azure_network_config.png)
*Captura 2: Configuración de red y reglas de firewall en Azure.*

**Explicación:**
Esta imagen presenta la configuración de red de la máquina virtual, destacando aspectos críticos para el acceso externo a los microservicios. La sección "Essentials" muestra que la máquina virtual posee una dirección IP pública `20.150.212.89`, la cual es accesible desde Internet y sirve como punto de entrada único para todos los clientes de la API.

La tabla de "Reglas de puerto de entrada" del Network Security Group `vm-lawconnectbackend-prod-001-nsg` contiene cinco reglas de seguridad configuradas:

1. **Prioridad 300 - SSH:** Permite conexiones SSH en el puerto 22 para administración remota del servidor.
2. **Prioridad 1010 - AllowHTTP80:** Regla crítica que autoriza el tráfico HTTP en el puerto 80 desde cualquier origen (`Cualquiera`), permitiendo que las aplicaciones cliente accedan a los microservicios a través de Nginx.
3. **Prioridad 65000 - AllowVnetInBound:** Facilita la comunicación entre recursos dentro de la misma red virtual.
4. **Prioridad 65001 - AllowAzureLoadBalancerInBound:** Habilita el tráfico desde balanceadores de carga de Azure.
5. **Prioridad 65500 - DenyAllInBound:** Regla de denegación por defecto que bloquea todo el tráfico no explícitamente permitido por las reglas anteriores.

La configuración de estas reglas es fundamental para garantizar que los microservicios desplegados en la VM sean accesibles desde Internet mediante la IP pública, mientras se mantiene un nivel apropiado de seguridad mediante la restricción de acceso a puertos específicos.

---

## 2. Proceso de Despliegue y Comandos Clave

El despliegue de los microservicios se ejecutó mediante una secuencia de comandos en la consola de la máquina virtual Ubuntu 24.04. A continuación, se documentan los comandos más relevantes utilizados durante el proceso.

### 2.1 Instalación de Dependencias Esenciales

Como primer paso, se instalaron las herramientas necesarias para construir, empaquetar y ejecutar los microservicios en el entorno de la máquina virtual.

```bash
sudo apt install -y openjdk-17-jdk maven nginx git docker.io docker-compose
```

**Explicación:**
Este comando instala múltiples paquetes fundamentales para el despliegue:
- **openjdk-17-jdk:** Proporciona el entorno de ejecución Java (JRE) y el kit de desarrollo (JDK) versión 17, requeridos para ejecutar aplicaciones Spring Boot.
- **maven:** Herramienta de automatización de construcción que gestiona la compilación, empaquetado y gestión de dependencias de los proyectos Java.
- **nginx:** Servidor web de alto rendimiento que se configuró como reverse proxy para recibir todas las peticiones HTTP entrantes y redirigirlas al API Gateway.
- **git:** Sistema de control de versiones para clonar el repositorio del proyecto desde GitHub.
- **docker.io y docker-compose:** Plaforma de contenedores Docker para aislar la ejecución de cada microservicio y sus bases de datos, facilitando el despliegue y la gestión del ciclo de vida de los servicios.

---

### 2.2 Configuración de Docker

Tras la instalación, se configuró Docker para iniciarse automáticamente con el sistema y se agregó el usuario al grupo de docker para permitir la ejecución de comandos sin prefijo `sudo`.

```bash
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -aG docker $USER
```

**Explicación:**
- El comando `systemctl start docker` inicia el servicio Docker de forma inmediata.
- `systemctl enable docker` configura Docker para iniciarse automáticamente cada vez que la máquina virtual se reinicie, garantizando la disponibilidad del servicio tras reinicios o apagados del servidor.
- `usermod -aG docker $USER` agrega el usuario actual al grupo `docker`, permitiendo la ejecución de comandos Docker sin requerir permisos de administrador. Esto mejora la seguridad y facilita el trabajo con contenedores.

---

### 2.3 Construcción de Microservicios

Cada uno de los microservicios fue compilado y empaquetado como un archivo JAR ejecutable utilizando Maven. Este proceso se realizó para cada servicio individualmente.

```bash
cd ~/lawconnect-backend/microservices/iam && mvn clean package spring-boot:repackage -DskipTests
```

**Explicación:**
Este comando se ejecutó secuencialmente para cada microservicio (IAM, Profiles, Cases y API Gateway). La estructura del comando comprende:
- `cd` navega al directorio específico del microservicio donde se encuentra el archivo `pom.xml`.
- `mvn clean` elimina artefactos de compilaciones anteriores para asegurar una construcción limpia.
- `package` compila el código fuente, resuelve dependencias y genera el archivo JAR en el directorio `target/`.
- `spring-boot:repackage` transforma el JAR estándar en un "fat JAR" ejecutable que incluye todas las dependencias anidadas, permitiendo su ejecución independiente con `java -jar`.
- `-DskipTests` omite la ejecución de pruebas unitarias durante la construcción para acelerar el proceso de despliegue.

---

### 2.4 Configuración de Nginx

Nginx se configuró para actuar como proxy inverso, recibiendo todo el tráfico HTTP en el puerto 80 y redirigiéndolo al API Gateway en el puerto 8080.

```bash
sudo cp ~/lawconnect-backend/microservices/nginx/nginx.conf /etc/nginx/sites-available/lawconnect
sudo ln -sf /etc/nginx/sites-available/lawconnect /etc/nginx/sites-enabled/
sudo rm -f /etc/nginx/sites-enabled/default
sudo systemctl restart nginx
sudo systemctl enable nginx
```

**Explicación:**
Este conjunto de comandos configura Nginx para enrutar las peticiones entrantes:
- El primer comando copia el archivo de configuración personalizado del proyecto a la ubicación estándar de configuraciones de Nginx.
- `ln -sf` crea un enlace simbólico desde `sites-available` a `sites-enabled`, activando la configuración.
- `rm -f` elimina la configuración por defecto de Nginx para evitar conflictos.
- `systemctl restart nginx` recarga la configuración de Nginx sin detener el servicio.
- `systemctl enable nginx` asegura que Nginx se inicie automáticamente con el sistema.

---

### 2.5 Despliegue de Servicios con Docker Compose

Finalmente, todos los microservicios, sus bases de datos MySQL asociadas y el API Gateway fueron desplegados simultáneamente utilizando Docker Compose.

```bash
cd ~/lawconnect-backend/microservices && sudo docker-compose up -d --build
```

**Explicación:**
Este comando es fundamental para el despliegue de toda la arquitectura:
- `cd` navega al directorio que contiene el archivo `docker-compose.yml`, el cual define todos los servicios y sus dependencias.
- `docker-compose up` lee el archivo de composición y crea e inicia todos los contenedores definidos.
- La opción `-d` ejecuta los contenedores en modo "detached" (segundo plano), permitiendo que la terminal quede libre para otros comandos.
- La opción `--build` reconstruye las imágenes Docker basándose en los Dockerfiles actualizados, asegurando que las últimas versiones de los microservicios estén en ejecución.

El archivo `docker-compose.yml` configura ocho contenedores:
- Tres bases de datos MySQL (iam-db, profiles-db, cases-db) en los puertos 3307, 3308 y 3309 respectivamente
- Tres microservicios Spring Boot (iam-service, profiles-service, cases-service) en los puertos 8081, 8082 y 8083
- Un API Gateway en el puerto 8080
- Todos los contenedores conectados a una red Docker privada para comunicación interna

---

### 2.6 Verificación de Estado de Servicios

Tras el despliegue, se verificó el estado de todos los contenedores para confirmar que se encontraban en ejecución correctamente.

```bash
sudo docker-compose ps
```

**Explicación:**
Este comando despliega una tabla con el estado de cada contenedor definido en `docker-compose.yml`, mostrando información como el nombre del contenedor, la imagen utilizada, los puertos mapeados, el estado actual (Up, Restarting, Exit, etc.) y el nombre del servicio. Un estado "Up" indica que el contenedor se inició exitosamente y se encuentra funcionando correctamente.

---

### 2.7 Configuración de Inicio Automático

Para garantizar que los microservicios se reinicien automáticamente tras un reinicio de la máquina virtual, se configuró un servicio systemd.

```bash
sudo systemctl enable lawconnect.service
```

**Explicación:**
Este comando registra el servicio `lawconnect.service` para que se ejecute automáticamente al arrancar el sistema. El archivo de servicio systemd fue previamente creado y define que Docker Compose debe ejecutar `docker-compose up -d` en el directorio de microservicios, asegurando la disponibilidad continua de la plataforma incluso después de reinicios no planificados de la VM.

---

## 3. Repositorio y Commits Relacionados

**URL del Repositorio:** `https://github.com/[tu-usuario]/lawconnect-backend`

**IDs de Commits relacionados con Deployment para este Sprint:**
- `[ID_COMMIT_1]` - Implementación del script de despliegue deploy-azure.sh
- `[ID_COMMIT_2]` - Configuración de docker-compose.yml para microservicios
- `[ID_COMMIT_3]` - Configuración de Nginx como reverse proxy
- `[ID_COMMIT_4]` - Configuración de API Gateway con Spring Cloud Gateway
- `[ID_COMMIT_5]` - Configuración de servicio systemd para inicio automático

---

## Conclusión

El despliegue de la plataforma LawConnect se completó exitosamente en la infraestructura de Microsoft Azure, estableciendo un entorno de producción funcional con alta disponibilidad y escalabilidad. La arquitectura implementada utiliza contenedores Docker para aislar cada microservicio, Nginx como punto de entrada único y el API Gateway de Spring Cloud para el enrutamiento interno.

Los pasos documentados, desde la configuración inicial de recursos en Azure hasta la automatización del despliegue mediante Docker Compose, demuestran la implementación exitosa de una plataforma de microservicios robusta y operativa, lista para pruebas de integración y validación funcional por parte de los stakeholders del proyecto.

---


**URL Pública:** http://20.150.212.89

