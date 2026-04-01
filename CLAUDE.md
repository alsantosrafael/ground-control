# Project Instructions

- **Framework:** We use Java with Spring Boot and GraalVM.
- **Testing:** Mockito and Junit for testing.
- **Project name:** `ground-control`
- **Base package:** `com.product.ground_control`

## Infrastructure

- **Build tool:** Gradle wrapper (`gradlew`) using Gradle `9.4.0`
- **Java version:** Java `21` toolchain
- **Spring Boot version:** `4.0.4`
- **Runtime style:** Spring Boot Web MVC application (`spring-boot-starter-webmvc`)
- **API docs:** SpringDoc OpenAPI UI is included (`springdoc-openapi-starter-webmvc-ui`)
- **Modularity:** Spring Modulith is enabled and the project contains a modulith verification/documentation test in `src/test/java/com/product/ground_control/ModularityTest.java`
- **Native support:** GraalVM Native Build Tools plugin is configured (`org.graalvm.buildtools.native`)
- **Application name:** `spring.application.name=ground-control`

## Current Infrastructure Boundaries

- No database dependency is currently declared in `build.gradle.kts`
- No messaging broker dependency is currently declared in `build.gradle.kts`
- No Spring Security dependency is currently declared in `build.gradle.kts`
- No infrastructure-specific runtime properties beyond the application name are currently declared in `src/main/resources/application.properties`

## Agent Guidance

- Prefer Java + Spring Boot + Spring Modulith patterns when generating code or docs
- Prefer JUnit 5-compatible tests (`useJUnitPlatform()` is enabled)
- Keep infrastructure assumptions minimal unless the repository explicitly adds them
- Do not assume database, messaging, or security infrastructure exists unless it is introduced in the build/configuration
