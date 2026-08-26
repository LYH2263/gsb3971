FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /workspace
COPY .mvn .mvn
COPY mvnw pom.xml checkstyle.xml ./
RUN chmod +x mvnw
RUN ./mvnw -q -DskipTests dependency:go-offline
COPY src src
RUN ./mvnw -q -DskipTests package

FROM maven:3.9.9-eclipse-temurin-21
WORKDIR /app
COPY --from=builder /workspace/target/backend-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
