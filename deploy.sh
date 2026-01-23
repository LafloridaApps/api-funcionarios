#!/bin/bash

# =========================================================
# CONFIGURACIÓN DEL MICROSERVICIO (Actualizado a GHCR)
# =========================================================
NOMBRE_APP="api-funcionarios"
PUERTO="8084"
# Cambiamos la ruta de Docker Hub a GitHub Container Registry
IMAGEN_HUB="ghcr.io/lafloridaapps/api-funcionarios"
# =========================================================

OPCION=${1:-"dev"}

case $OPCION in
    "prod")
        echo "--- MODO PRODUCCIÓN: Bajando de GitHub Packages ($IMAGEN_HUB) ---"
        # Ahora el pull se hace desde el registro privado de GitHub
        docker pull $IMAGEN_HUB:latest
        TARGET_IMAGE="$IMAGEN_HUB:latest"
        ;;
    *)
        echo "--- MODO DESARROLLO: Compilando localmente ($NOMBRE_APP) ---"
        ./mvnw clean package -DskipTests
        docker build -t $NOMBRE_APP:local .
        TARGET_IMAGE="$NOMBRE_APP:local"
        ;;
esac

echo "--- Limpiando contenedor anterior ---"
docker stop ${NOMBRE_APP}-container 2>/dev/null
docker rm ${NOMBRE_APP}-container 2>/dev/null

echo "--- Iniciando contenedor en puerto $PUERTO ---"
docker run \
           --restart always \
           -d -p ${PUERTO}:${PUERTO} \
           --env-file .env \
           --network appx \
           --add-host=host.docker.internal:host-gateway \
           --name ${NOMBRE_APP}-container \
           $TARGET_IMAGE

# Limpia imágenes antiguas para no llenar el disco del PC de la oficina
docker image prune -f
echo "--- Proceso Terminado ($OPCION) ---"
