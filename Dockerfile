FROM ubuntu:latest
# Stage 1: Build Angular application
FROM node:18.19.1 as angular-build
WORKDIR /app
COPY pro-ui/package.json pro-ui/package-lock.json ./
RUN npm ci
COPY pro-ui/ .
RUN npm run build:prod

# Stage 2: Build Spring Boot application
FROM maven:3.8.4-openjdk-17 as spring-build
WORKDIR /app
COPY pro-api/pom.xml .
# Download project dependencies
RUN mvn dependency:go-offline -B
COPY pro-api/src ./src
# Copy built Angular application
COPY --from=angular-build /app/dist/pro-ui /app/src/main/resources/static
RUN mvn install -DskipTests

# Stage 3: Run the application
FROM openjdk:17-jdk-alpine
WORKDIR /app
COPY --from=spring-build /app/target/pro-api-0.0.1-SNAPSHOT.jar ./
EXPOSE 8080

CMD ["java", "-jar", "pro-api-0.0.1-SNAPSHOT.jar"]