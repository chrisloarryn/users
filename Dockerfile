# Etapa de construcción
FROM maven:3-eclipse-temurin-21-alpine AS build
WORKDIR /app

COPY src ./src

COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN mvn clean package -B

# log the output of the build
RUN ls -la /app/target

# Etapa de ejecución
FROM openjdk:18-slim
WORKDIR /app
COPY --from=build /app/target/*.jar users.jar

# Exponer el puerto 8080
EXPOSE 8080
RUN ls -la /app

# Ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "users.jar"]