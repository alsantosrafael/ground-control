# Ground Control 🚀

A Spring Boot application for managing feature flags with rollout rules and dynamic configuration.

## 📋 Overview

Ground Control is a feature flag management system built with Kotlin and Spring Boot. It enables dynamic feature rollouts, configuration management, and supports multiple value types for enterprise applications.

## 🔧 Tech Stack

- **Language**: Kotlin
- **Framework**: Spring Boot
- **Build Tool**: Gradle 8.x
- **Container**: Docker / docker-compose
- **Monitoring**: Prometheus & Grafana
- **Architecture**: Domain-Driven Design (DDD)
- **Testing**: JUnit 5

## 🏗️ Architecture

The application follows Domain-Driven Design principles with clear separation of concerns:

![DDD Architecture Layers](assets/layers.png)

### Domain Layer Structure
- **Value Objects**: `FeatureFlag`, `FeatureFlagCode`, `FeatureFlagId`, `FeatureFlagName`, `RolloutRule`
- **Entities**: `FeatureFlagEntity` with JPA annotations
- **Mappers**: Bidirectional conversion between domain objects and entities
- **Enums**: `FlagType` for different value types (INT, STRING, PERCENTAGE, BOOLEAN)

## 🎯 Features

### Feature Flag Management
- ✅ Create, update, and delete feature flags
- 🎛️ Enable/disable flags dynamically
- ⏰ Schedule flag activation with timestamps
- 📝 Rich metadata support (name, description, audit trails)

### Supported Value Types
- 🔢 **INT**: Integer values for numeric configurations
- 📝 **STRING**: Text values for string configurations
- 📊 **PERCENTAGE**: Percentage-based gradual rollouts (0-100%)
- ✅ **BOOLEAN**: Simple on/off toggle flags

### Rollout Rules
- 🎯 Advanced targeting with custom rollout rules
- 🔄 Runtime rule modification support
- 🔗 Rules linked to parent feature flags
- 📊 Flexible rule evaluation system

## 🚀 Getting Started

### Prerequisites
- Java 11+
- Docker (optional)
- Gradle 8+ (included via wrapper)

### Quick Start

```bash
# Clone the repository
git clone <repository-url>
cd groundcontrol

# Run with Gradle
./gradlew bootRun

# Or run with Docker
docker-compose up
```

### Building 

```bash
# Build the application
./gradlew build

# Run tests
./gradlew test

# Create JAR
./gradlew bootJar
```

##  📊Monitoring & Observability
- Metrics: Prometheus endpoint at /actuator/prometheus
- Logging: Structured JSON logs in logs/app-json.log
- Health Checks: Spring Boot Actuator endpoints
- Test Reports: Generated in build/reports/tests/test/index.html

## 🗂️ Project Structure
```
groundcontrol/
├── src/
│   ├── main/
│   │   ├── kotlin/com/platform/groundcontrol/
│   │   │   └── domain/
│   │   │       ├── entities/     # JPA entities
│   │   │       ├── enums/        # FlagType definitions
│   │   │       ├── mappers/      # Domain ↔ Entity conversion
│   │   │       └── valueobjects/ # Domain models
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── logback-spring.xml
│   │       ├── static/           # Static web assets
│   │       └── templates/        # Template files
│   └── test/kotlin/              # Test sources
├── assets/
│   └── layers.png               # Architecture diagram
├── logs/
│   └── app-json.log             # Application logs
├── build/
│   ├── libs/                   # Generated JARs
│   └── reports/                # Test and coverage reports
├── gradle/wrapper/             # Gradle wrapper files
├── docker-compose.yaml         # Container orchestration
├── prometheus.yml              # Metrics collection
├── Dockerfile                  # Container definition
└── build.gradle                # Build configuration
```

## 🔧 Configuration

### Key Configuration Files
- **application.properties** - Spring Boot application settings
- **docker-compose.yaml** - Multi-container Docker setup
- **prometheus.yml** - Metrics scraping configuration
- **logback-spring.xml** - Structured logging setup
- **Dockerfile** - Container build instructions

## 🧪 Testing

```
# Run all tests
./gradlew test

# View test reports
open build/reports/tests/test/index.html

# Check test results
ls build/test-results/test/

```
The project includes comprehensive test coverage with JUnit 5 and generates detailed HTML reports.


## 📈 Development

### Development Features
- 🔄 Incremental Kotlin compilation with caching
- 📦 Gradle wrapper for consistent builds
- 🐳 Docker support for containerized development
- 📊 Built-in metrics and health monitoring
- 🧪 Automated test execution and reporting

### Code Quality
- Domain-driven design patterns
- Clean architecture principles
- Type-safe Kotlin implementations
- Comprehensive error handling and logging
