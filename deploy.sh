#!/bin/bash

# =========================================================
# CONFIGURACIÓN DEL MICROSERVICIO Y SERVIDOR
# =========================================================
VERSIONS_FILE="versions.json"
NOMBRE_APP="api-funcionarios"
PUERTO="8084"
NETWORK="laflorida"
# =========================================================

# --- Control de versiones ---
if [ ! -f "$VERSIONS_FILE" ]; then
    echo "❌ No se encuentra $VERSIONS_FILE. Creando uno nuevo..."
    echo '{"currentVersion":"0.0.0","history":[]}' > "$VERSIONS_FILE"
fi

CURRENT_VER=$(jq -r '.currentVersion' "$VERSIONS_FILE")
IFS='.' read -r MAJOR MINOR PATCH <<< "$CURRENT_VER"
NEXT_PATCH=$((PATCH + 1))
NEXT_VER="${MAJOR}.${MINOR}.${NEXT_PATCH}"

echo "Versión actual: v$CURRENT_VER"
echo "Nueva versión:  v$NEXT_VER"
echo ""

read -p "Descripción del deploy (o Enter para 'Deploy v$NEXT_VER'): " DESC
DESC="${DESC:-Deploy v$NEXT_VER}"

TARGET_IMAGE="$NOMBRE_APP:v$NEXT_VER"

# =========================================================

echo "Eliminando imagen antigua..."
docker rmi -f "$TARGET_IMAGE" 2>/dev/null

# 1. Construcción local
echo "--- 1. Creando archivo JAR y construyendo imagen local ---"
./mvnw clean package -DskipTests && \
docker build -t "$TARGET_IMAGE" .

if [ $? -ne 0 ]; then
    echo "❌ Error en la construcción local. Abortando."
    exit 1
fi

# 2. Preparación (Limpiar imagen y contenedor antiguo)
echo "--- 2. Limpiando entorno"
    echo "Deteniendo contenedor antiguo si existe..."
    docker stop "${NOMBRE_APP}-container" 2>/dev/null
    docker rm "${NOMBRE_APP}-container" 2>/dev/null 
    
    echo "Asegurando que la red $NETWORK exista..."
    docker network inspect $NETWORK >/dev/null 2>&1 || docker network create $NETWORK

# 4. Despliegue en el servidor remoto
echo "--- 3. Iniciando nuevo contenedor en puerto $PUERTO ---"
    docker run \
        --restart always \
        -d \
        -p "${PUERTO}:${PUERTO}" \
        --env-file docker.env \
        --network $NETWORK \
        --add-host=host.docker.internal:host-gateway \
        --name "${NOMBRE_APP}-container" \
        "$TARGET_IMAGE"

# 5. Limpieza local
echo "--- 4. Limpieza de imágenes locales ---"
docker image prune -f

# --- Registrar versión ---
TODAY=$(date +%Y-%m-%d)
jq --arg ver "$NEXT_VER" \
   --arg date "$TODAY" \
   --arg desc "$DESC" \
   '.currentVersion = $ver | .history += [{"version": $ver, "date": $date, "description": $desc}]' \
   "$VERSIONS_FILE" > "${VERSIONS_FILE}.tmp" && mv "${VERSIONS_FILE}.tmp" "$VERSIONS_FILE"

echo ""
echo "✅ ¡Proceso completado! Versión v$NEXT_VER desplegada."
echo "   Imagen: $TARGET_IMAGE"
echo "   Puerto: $PUERTO"
