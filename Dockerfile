# Build: Maven + JDK từ image chính thức (không dùng mvnw/.mvn trong repo)
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Cache layer: chỉ pom — tải dependency từ Maven Central / mirrors
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -DskipTests package

# Run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /app/target/cloud-*.jar /app/app.jar

EXPOSE 8080

ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -jar /app/app.jar"]
