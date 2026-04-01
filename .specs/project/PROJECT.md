# Project Vision: Ground Control

## Goal
Ground Control is a high-performance feature management and experimentation platform designed to be "Agent-Native," high-craft (Notion/Resend style), and infrastructure-aware.

## Core Values
- **Sub-5ms Evaluation**: Leveraging Java 21 + GraalVM for near-instant policy assessment.
- **Agent-Native**: Designed for autonomous control and instant feedback loops.
- **Atomic MVP**: Focusing on the core engine and high-performance edge before complex UI.

## Tech Stack
- **Language**: Java 21 (Virtual Threads)
- **Framework**: Spring Boot 4.0.4 + Spring Modulith
- **Runtime**: GraalVM Native Image
- **Persistence**: PostgreSQL (JSONB for rules)
- **Caching**: Caffeine
- **Communication**: REST + gRPC
