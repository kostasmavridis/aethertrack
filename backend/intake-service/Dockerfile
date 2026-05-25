# Multi-stage build for intake-service
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace

RUN apk add --no-cache maven

# Copy all module POMs first for layer caching
COPY pom.xml ./
COPY supplement-domain-service/pom.xml supplement-domain-service/
COPY scheduling-service/pom.xml       scheduling-service/
COPY fhir-service/pom.xml             fhir-service/
COPY intake-service/pom.xml           intake-service/

RUN mvn dependency:go-offline -pl intake-service -am -q

COPY intake-service/src intake-service/src

RUN mvn package -pl intake-service -am -DskipTests -q

FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

ARG JAR_FILE=intake-service/target/*.jar
COPY --from=build /workspace/${JAR_FILE} app.jar

EXPOSE 8084

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
