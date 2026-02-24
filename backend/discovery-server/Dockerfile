FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace/app

COPY ../pom.xml ../pom.xml
COPY pom.xml .
COPY src src

RUN --mount=type=cache,target=/root/.m2 \
    cd .. && ./mvnw install -DskipTests -pl discovery-server && \
    cd discovery-server && ../mvnw package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -g 1001 spring && adduser -u 1001 -G spring -s /bin/sh -D spring
USER spring:spring

COPY --from=build /workspace/app/target/*.jar app.jar

EXPOSE 8761

ENTRYPOINT ["java", "-jar", "app.jar"]
