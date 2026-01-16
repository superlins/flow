# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Flow is a Spring Boot 4.0.1 application built with Java 25 that provides a declarative API service platform. It follows Domain-Driven Design (DDD) principles with a multi-module Maven architecture.

## Module Structure

- **flow-core**: Core plugin system using PF4J and JSON schema validation
- **flow-api**: API module (currently empty, for future API definitions)
- **flow-domain**: Domain models and business logic for ApiDatasource and ApiService
- **flow-app**: Application layer (currently empty, for future application services)
- **flow-connector**: Connector framework for various data sources (HTTP, R2DBC, etc.)

## Common Commands

### Build and Run
```bash
# Build the entire project
mvn clean install

# Run the application
mvn spring-boot:run

# Run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Start PostgreSQL (required)
docker-compose up -d
```

### Testing
```bash
# Run all tests
mvn test

# Run tests for specific module
mvn test -pl flow-domain

# Run a single test class
mvn test -Dtest=FlowApplicationTests
```

### Development
```bash
# Clean and compile
mvn clean compile

# Generate IDE files (if needed)
mvn idea:idea

# Check dependencies
mvn dependency:tree
```

## Architecture Highlights

### Domain Model Design

The system is built around two core aggregates:

1. **ApiDatasource**: Represents a configurable data source with contracts, operations, and connections
2. **ApiService**: A user-facing service that wraps an ApiDatasource with mapping rules

Key design principles from the domain documentation:
- ApiDatasource has version support (`(key, version)` identifier)
- Contract-first approach with JSON Schema validation
- Strict business rules (DS-1 through DS-5) governing datasource lifecycle
- Event-driven architecture with domain events
- No deletion allowed - only disable operations

### Plugin System

Uses PF4J for plugin extensibility:
- SpringPluginManager for Spring integration
- Plugins can extend datasource capabilities
- Extension declarations in datasources without affecting contracts

### Connector Framework

Abstract connector system supporting:
- HTTP connectors with REST capabilities
- R2DBC connectors for reactive database access
- Cassandra connector support
- Filter chain for cross-cutting concerns (logging, metrics, caching, security)

### Data Flow Context

The system uses a unified context variable approach:
- `#serviceInput`: Incoming request data
- `#dsInput`: Mapped input for datasource
- `#dsOutput`: Raw datasource output
- `#serviceOutput`: Final mapped response
- SpEL expressions for field mappings

## Database Setup

The application uses PostgreSQL with R2DBC:
- Database: `mydatabase`
- User: `myuser`
- Password: `secret`
- Port: 5432 (exposed via Docker Compose)

## Key Implementation Notes

### JSON Schema Validation
- Uses NetworkNT JSON Schema Validator
- Schemas are part of domain logic, not just validation
- Strict validation required for execution

### Reactive Programming
- Built on Spring WebFlux
- R2DBC for reactive database access
- Mono/Flux throughout the codebase

### Error Handling
- Domain-specific exceptions in flow-domain
- Business rule validation in domain models
- Clear separation between domain exceptions and technical errors

## Testing Strategy

- Unit tests for domain models
- Integration tests for connectors
- Spring Boot tests for application context
- JSON Schema validation tests in flow-core

## Development Guidelines

- Follow DDD patterns when extending domain models
- New datasource types should extend `DatasourceConnection` and `DatasourceOperation`
- New connectors should extend `AbstractConnector`
- Always maintain backward compatibility for datasource versions
- Use SpEL for field mappings in ApiService configurations