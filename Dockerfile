# --- ETAPA 1: Construcción (Build) ---
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copiamos solo lo necesario para descargar las dependencias
# Esto permite que Docker cachee las librerías si el pom.xml no cambia
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline

# Ahora copiamos el código fuente y compilamos el proyecto
COPY src ./src
RUN ./mvnw clean package -DskipTests

# --- ETAPA 2: Ejecución (Runtime) ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copiamos solo el archivo JAR resultante de la etapa anterior
# Esto hace que la imagen final sea liviana (solo el JRE y tu App)
COPY --from=build /app/target/*.jar app.jar

# Exponemos el puerto (ajústalo si tu app usa otro, ej: 8081)
EXPOSE 8084

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
