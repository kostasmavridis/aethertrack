# Multi-stage build for supplement-domain-service
# Stage 1: build with Maven (installed via apk – avoids mvnw download in airgapped Docker builds)
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace

# Install Maven from Alpine repos (fast, no wrapper download needed)
RUN apk add --no-cache maven

# Copy parent pom and all module poms first for layer caching
COPY pom.xml ./
COPY supplement-domain-service/pom.xml supplement-domain-service/
COPY scheduling-service/pom.xml       scheduling-service/
COPY fhir-service/pom.xml             fhir-service/
COPY intake-service/pom.xml           intake-service/

# Download dependencies (cached unless poms change)
RUN mvn dependency:go-offline -pl supplement-domain-service -am -q

# Copy full source for the module we're building
COPY supplement-domain-service/src supplement-domain-service/src

RUN mvn package -pl supplement-domain-service -am -DskipTests -q

# Stage 2: minimal JRE runtime
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

ARG JAR_FILE=supplement-domain-service/target/*.jar
COPY --from=build /workspace/${JAR_FILE} app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
