# syntax=docker/dockerfile:1
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B -q dependency:go-offline
COPY src/ src/
# Tests need Docker (Testcontainers) and run in CI / via ./mvnw verify, not inside the image build.
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B -q package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --uid 1001 berlin
COPY --from=build /workspace/target/berlin-clock-*.jar app.jar
USER berlin
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
