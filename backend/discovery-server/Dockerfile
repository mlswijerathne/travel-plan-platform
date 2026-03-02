FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /workspace

COPY pom.xml .
COPY discovery-server/pom.xml discovery-server/pom.xml
COPY discovery-server/src discovery-server/src

RUN --mount=type=cache,target=/root/.m2 \
    mvn install -N -DskipTests && \
    mvn -f discovery-server/pom.xml package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -g 1001 spring && adduser -u 1001 -G spring -s /bin/sh -D spring
USER spring:spring

COPY --from=build /workspace/discovery-server/target/*.jar app.jar

EXPOSE 8761

ENTRYPOINT ["java", "-jar", "app.jar"]
