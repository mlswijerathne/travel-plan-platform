FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /workspace

COPY pom.xml .
COPY common-lib common-lib
COPY vehicle-service/pom.xml vehicle-service/pom.xml
COPY vehicle-service/src vehicle-service/src

RUN --mount=type=cache,target=/root/.m2 \
    mvn install -N -DskipTests && \
    mvn -f common-lib/pom.xml install -DskipTests && \
    mvn -f vehicle-service/pom.xml package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -g 1001 spring && adduser -u 1001 -G spring -s /bin/sh -D spring
USER spring:spring

COPY --from=build /workspace/vehicle-service/target/*.jar app.jar

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "app.jar"]
