FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace/app

COPY ../pom.xml ../pom.xml
COPY ../common-lib ../common-lib
COPY pom.xml .
COPY src src

RUN --mount=type=cache,target=/root/.m2 \
    cd .. && ./mvnw install -DskipTests -pl common-lib && \
    cd hotel-service && ../mvnw package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -g 1001 spring && adduser -u 1001 -G spring -s /bin/sh -D spring
USER spring:spring

COPY --from=build /workspace/app/target/*.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]
