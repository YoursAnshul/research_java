
# Stage 1: Build the Java application
FROM public.ecr.aws/docker/library/maven:3.9.9-eclipse-temurin-17-focal AS builder

# Set the working directory
WORKDIR /app

# Copy the pom.xml and download dependencies
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Copy the entire project source code
COPY . .

# Build the application
RUN mvn clean package -DskipTests


FROM public.ecr.aws/docker/library/ubuntu:22.04
 
# Set non-interactive frontend to avoid timezone prompts
ENV DEBIAN_FRONTEND=noninteractive
 
# Install necessary packages and dependencies
RUN apt-get update && \
    apt-get install -y \
    openjdk-17-jdk \
    mysql-client \
    wget \
    gnupg \
    ca-certificates \
    fonts-liberation \
    libappindicator3-1 \
    libasound2 \
    libx11-xcb1 \
    libxcomposite1 \
    libxdamage1 \
    libxrandr2 \
    libxss1 \
    libxtst6 \
    libnss3 \
    libgbm1 \
    xdg-utils \
    x11-utils \
    xvfb \
    x11vnc \
    fluxbox \
    dbus-x11 \
    chromium-browser \
    && apt-get clean
 
# Copy the DisputeUpdate folder to the container and build the project
WORKDIR /app
# Copy the built JAR file from the builder stage
COPY --from=builder /app/target/*.jar dfo.jar

COPY ./start.sh start.sh
RUN chmod +x start.sh