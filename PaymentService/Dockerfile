FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

COPY /pom.xml ./
COPY /src ./src

RUN mvn clean package -DskipTests=true

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN apk add --no-cache curl

COPY --from=builder /app/target/PaymentService-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "--enable-preview", "-jar", "app.jar"]
