# Stage 1: Builder - Compile and build the application
FROM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /app

# Copy gradle wrapper and build files
COPY gradle gradle
COPY gradlew .
COPY build.gradle .
COPY settings.gradle .

# Copy source code
COPY src src

# Build the application
RUN chmod +x gradlew && \
    ./gradlew clean build -x test --no-daemon && \
    ls -la build/libs/

# Stage 2: Runtime - Minimal runtime image
FROM eclipse-temurin:17-jre-jammy

LABEL maintainer="your-email@example.com" \
      description="Quora-like Application Backend" \
      version="1.0.0"

# Install additional utilities for production
RUN apt-get update && apt-get install -y \
    curl \
    ca-certificates && \
    rm -rf /var/lib/apt/lists/*

# Create non-root user for security
RUN useradd -m -u 1000 appuser

WORKDIR /app

# Copy only the built jar from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Set ownership to non-root user
RUN chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:1004/actuator/health || exit 1

# Expose port
EXPOSE 1004

# JVM optimization flags for container environments
ENV JAVA_OPTS="-XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:InitialRAMPercentage=50.0 \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=200 \
    -Dfile.encoding=UTF-8 \
    -Duser.timezone=UTC"

# Entry point
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]