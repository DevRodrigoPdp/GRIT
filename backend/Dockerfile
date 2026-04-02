# ETAPA 1: Construcción (Usamos JDK 17)
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

# 1. Copiamos solo lo necesario para descargar dependencias
# Esto permite que Docker cachee las librerías si el pom.xml no cambia
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw

# Descargamos las dependencias (Este paso tarda la primera vez, luego es instantáneo)
RUN ./mvnw dependency:go-offline

# 2. Copiamos el código fuente y compilamos
COPY src ./src
RUN ./mvnw clean package -DskipTests

# ETAPA 2: Ejecución (JRE ligero para producción)
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copiamos el JAR generado en la etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Variable dinámica (útil para Railway/Heroku o Docker Compose)
ENV PORT=8080
EXPOSE ${PORT}

ENTRYPOINT ["java", "-jar", "app.jar"]