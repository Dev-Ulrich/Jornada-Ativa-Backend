# === Etapa 1: Build com Maven (Java 21) ===
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Cache de dependências
COPY pom.xml .
RUN mvn -U -B -e -DskipTests -Dmaven.test.skip=true dependency:go-offline

# Código-fonte
COPY src ./src

# Build do JAR com log detalhado (-X)
RUN mvn -U -B -e -X -DskipTests -Dmaven.test.skip=true -DskipITs=true package

# === Etapa 2: Runtime ===
FROM eclipse-temurin:21-jre
WORKDIR /app
ENV SPRING_PROFILES_ACTIVE=prod
ENV PORT=8080
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
