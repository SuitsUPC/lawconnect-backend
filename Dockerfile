# Dockerfile para ejecutar el proyecto completo con start.sh
FROM ubuntu:22.04

# Evitar prompts interactivos durante la instalación
ENV DEBIAN_FRONTEND=noninteractive

# Instalar dependencias necesarias
RUN apt-get update && apt-get install -y \
    openjdk-17-jdk \
    maven \
    docker.io \
    docker-compose \
    curl \
    git \
    bash \
    && rm -rf /var/lib/apt/lists/*

# Configurar Java
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
ENV PATH=$PATH:$JAVA_HOME/bin

# Crear directorio de trabajo
WORKDIR /app

# Copiar todo el proyecto
COPY . .

# Dar permisos de ejecución a los scripts
RUN chmod +x start.sh stop.sh logs.sh status.sh mvnw

# Exponer puertos
EXPOSE 8080 8081 8082 8083 3307 3308 3309

# Ejecutar start.sh al iniciar el contenedor
CMD ["/bin/bash", "start.sh"]





