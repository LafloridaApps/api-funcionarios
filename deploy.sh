#!/bin/bash

# =========================================================
# CONFIGURACIÓN DEL MICROSERVICIO Y SERVIDOR
# =========================================================
NOMBRE_APP="api-funcionarios"
PUERTO="8084"
TARGET_IMAGE="$NOMBRE_APP:v1.0.0"
NETWORK="laflorida"
# =========================================================

echo "Eliminando imagen antigua..."
docker rmi -f $TARGET_IMAGE 2>/dev/null

# 1. Construcción local
echo "--- 1. Creando archivo JAR y construyendo imagen local ---"
./mvnw clean package -DskipTests && \
docker build -t $TARGET_IMAGE .



if [ $? -ne 0 ]; then
    echo "❌ Error en la construcción local. Abortando."
    exit 1
fi

# 2. Preparación (Limpiar imagen y contenedor antiguo)
echo "--- 2. Limpiando entorno"
#ssh $REMOTO << EOF
    echo "Deteniendo contenedor antiguo si existe..."
    docker stop "${NOMBRE_APP}-container" 2>/dev/null
    docker rm "${NOMBRE_APP}-container" 2>/dev/null 
    
    
    
    echo "Asegurando que la red $NETWORK exista..."
    docker network inspect $NETWORK >/dev/null 2>&1 || docker network create $NETWORK
#EOF

# 3. Transferencia de la imagen
#echo "--- 3. Transfiriendo y cargando nueva imagen ($TARGET_IMAGE) ---"
#docker save $TARGET_IMAGE | gzip | ssh $REMOTO "gunzip | docker load"

# 4. Despliegue en el servidor remoto
echo "--- 4. Iniciando nuevo contenedor en puerto $PUERTO ---"
#ssh $REMOTO << EOF
    docker run \
        --restart always \
        -d \
        -p "${PUERTO}:${PUERTO}" \
        --env-file docker.env \
        --network $NETWORK \
        --add-host=host.docker.internal:host-gateway \
        --name "${NOMBRE_APP}-container" \
        "$TARGET_IMAGE"
#EOF

# 5. Limpieza local
echo "--- 5. Limpieza de imágenes locales ---"
docker image prune -f

echo "✅ ¡Proceso completado! La aplicación $NOMBRE_APP ya está corriendo en $REMOTO:$PUERTO"