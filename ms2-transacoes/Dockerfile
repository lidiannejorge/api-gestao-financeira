# ==========================================
# ETAPA 1 - BUILD DA APLICACAO
# ==========================================
FROM maven:3.9.16-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests
# ==========================================
# ETAPA 2 - EXECUCAO DA APLICACAO
# ==========================================
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]