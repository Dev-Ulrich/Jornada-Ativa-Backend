# === Etapa 1: Build com Maven ===
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copiamos só o pom primeiro para cachear dependências
COPY pom.xml .
# Baixa dependências (com logs completos) – sem testes e sem compilar testes
RUN mvn -B -e -DskipTests -Dmaven.test.skip=true dependency:go-offline

# Agora copiamos o código
COPY src ./src

# Build do jar (sem testes e com logs)
RUN mvn -B -e -DskipTests -Dmaven.test.skip=true package

# === Etapa 2: Runtime ===
FROM eclipse-temurin:21-jre
WORKDIR /app
ENV SPRING_PROFILES_ACTIVE=prod
ENV PORT=8080
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
