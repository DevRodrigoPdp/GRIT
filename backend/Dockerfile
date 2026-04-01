# ETAPA 1: Construcción (Maven + JDK 17)
FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app
# Copiamos el pom y descargamos dependencias para aprovechar el caché de Docker
COPY pom.xml .
RUN mvn dependency:go-offline

# Copiamos el código y generamos el JAR
COPY src ./src
RUN mvn clean package -DskipTests

# ETAPA 2: Ejecución (Solo JRE ligero)
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
# Copiamos solo el ejecutable generado en la etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Variable de puerto dinámica para Railway
EXPOSE ${PORT}

# Comando de ejecución profesional
ENTRYPOINT ["java", "-jar", "app.jar"]