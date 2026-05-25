# Multi-stage build for scheduling-service
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace

RUN apk add --no-cache maven

# Copy all module POMs first for layer caching
COPY pom.xml ./
COPY supplement-domain-service/pom.xml supplement-domain-service/
COPY scheduling-service/pom.xml       scheduling-service/
COPY fhir-service/pom.xml             fhir-service/
COPY intake-service/pom.xml           intake-service/

RUN mvn dependency:go-offline -pl scheduling-service -am -q

COPY scheduling-service/src scheduling-service/src

RUN mvn package -pl scheduling-service -am -DskipTests -q

FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

ARG JAR_FILE=scheduling-service/target/*.jar
COPY --from=build /workspace/${JAR_FILE} app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
