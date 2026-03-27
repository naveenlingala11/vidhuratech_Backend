FROM openjdk:17-jdk-slim

LABEL maintainer="Naveen"

WORKDIR /app

# Copy jar
COPY target/*.jar app.jar

# Run as non-root (security best practice)
RUN useradd -m appuser
USER appuser

EXPOSE 8080

# JVM optimization (important for cloud)
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]