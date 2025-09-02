FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

# Copy application jar
COPY build/libs/*.jar app.jar

# Set default JVM options (matching gradle.properties)
# These can be overridden by setting JAVA_OPTS environment variable
ENV JAVA_OPTS_DEFAULT="-Xmx512m -Xms256m -XX:+UseG1GC -XX:+UseStringDeduplication -Dfile.encoding=UTF-8 -Djava.security.egd=file:/dev/./urandom"
ENV JAVA_OPTS=""

# Use custom JAVA_OPTS if provided, otherwise use defaults
ENTRYPOINT exec java ${JAVA_OPTS:-$JAVA_OPTS_DEFAULT} -jar app.jar