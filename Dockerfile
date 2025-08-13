FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY build/libs/*.jar app.jar

# Use JAVA_OPTS environment variable if provided
ENV JAVA_OPTS=""
ENTRYPOINT sh -c "java $JAVA_OPTS -jar app.jar"