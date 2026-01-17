# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Flow is a Spring Boot 4.0.1 application built with Java 25 that provides a declarative API service platform. It follows Domain-Driven Design (DDD) principles with a multi-module Maven architecture.

## Module Structure

- **flow-core**: Core plugin system using PF4J and JSON schema validation
- **flow-api**: API module (currently empty, for future API definitions)
- **flow-domain**: Domain models and business logic for ApiDatasource, ApiService, and Workflow
- **flow-app**: Application layer with REST APIs and Spring Boot configuration
- **flow-connector**: Connector framework for various data sources (HTTP, R2DBC, etc.)
- **flow-frontend**: React + TypeScript frontend with Tailwind CSS + DaisyUI

## Common Commands

### Build and Run
```bash
# Build the entire project (backend)
mvn clean install

# Run the backend application
mvn spring-boot:run

# Run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Start PostgreSQL (required)
docker-compose up -d

# Frontend commands
cd flow-frontend
npm install              # Install dependencies
npm run dev             # Start dev server (http://localhost:5173)
npm run build           # Build for production
npm run preview         # Preview production build
```

### Workflow System API

The backend provides REST API for workflow management at `/api/workflows`:

- `POST /api/workflows` - Create new workflow
- `GET /api/workflows` - List workflows (supports `?key=` and `?status=` filters)
- `GET /api/workflows/{key}/{version}` - Get workflow details
- `POST /api/workflows/{key}/{version}/enable` - Enable workflow
- `POST /api/workflows/{key}/{version}/disable` - Disable workflow
- `POST /api/workflows/{key}/{version}/archive` - Archive workflow
- `POST /api/workflows/{key}/{version}/execute` - Execute workflow with JSON input
- `GET /api/workflows/executions/{executionId}` - Get execution details
- `POST /api/workflows/executions/{executionId}/cancel` - Cancel running execution

### Workflow Persistence

The workflow system now supports full R2DBC persistence:

**WorkflowEntity** (`flow-domain/src/main/java/com/zwtech/flow/domain/model/workflow/r2dbc/WorkflowEntity.java`):
- Stores workflow metadata (key, version, name, description, status)
- Serializes nodes and connections as JSONB
- Supports CRUD operations through `WorkflowRepository`

**WorkflowExecutionEntity** (`flow-domain/src/main/java/com/zwtech/flow/domain/model/workflow/r2dbc/WorkflowExecutionEntity.java`):
- Tracks execution state (PENDING, RUNNING, SUCCESS, FAILED, TIMEOUT, CANCELED)
- Stores input/output as JSONB
- Tracks node-level execution status
- Records timing information (startedAt, finishedAt, durationMs)

**Controllers**:
- `WorkflowController` - In-memory storage (demo/legacy)
- `PersistentWorkflowController` - Full R2DBC persistence with domain model integration

### Workflow Execution Features

**DatasourceExecutionService** (`flow-app/src/main/java/com/zwtech/flow/app/service/DefaultDatasourceExecutionService.java`):
- Coordinates Repository and ConnectorAdapter
- Executes datasource operations through HTTP connector
- Supports timeout control

**WorkflowExecutionService** (`flow-app/src/main/java/com/zwtech/flow/app/service/DefaultWorkflowExecutionService.java`):
- DAG execution with field mapping
- Support for datasource and simple node types
- Execution cancellation with state persistence
- Timeout handling (default 30 minutes)

**Field Mapping**:
- Supports dot-notation paths (e.g., `"data.user.id"`)
- Smart field extraction from source node outputs
- Flexible field assignment to target node inputs
- JSON node merging for complex data structures

### CORS Configuration

The backend implements CORS configuration to allow frontend access from:
- `http://localhost:5173` (default Vite dev server)
- `http://localhost:5174`

Configure in `flow-app/src/main/java/com/zwtech/flow/app/config/CorsConfig.java`

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

The system is built around three core aggregates:

1. **ApiDatasource**: Represents a configurable data source with contracts, operations, and connections
2. **ApiService**: A user-facing service that wraps an ApiDatasource with mapping rules
3. **Workflow**: A declarative workflow system with nodes, connections, and execution tracking

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

### Workflow Domain Architecture

The Workflow system follows DDD patterns with:
- **WorkflowAggregate**: Identified by `(key, version)` composite key
- **WorkflowStatus**: DRAFT, ENABLED, DISABLED, ARCHIVED
- **Node/Connection**: Graph-based workflow structure
- **WorkflowExecution**: Tracks execution with status (SUCCESS, FAILED, RUNNING, TIMEOUT, CANCELED)
- **Domain Events**: WorkflowCreatedEvent, WorkflowStatusChangedEvent, WorkflowExecutionCompletedEvent, etc.

### Frontend Architecture

The React frontend follows standard patterns:
- **React Query** for server state management and caching
- **Axios** for HTTP client with interceptors
- **DaisyUI** components for consistent UI
- **Component structure**:
  - `App.tsx` - Main layout and query client provider
  - `WorkflowList.tsx` - Display all workflows
  - `CreateWorkflowModal.tsx` - Form to create new workflows
  - `ExecuteWorkflowModal.tsx` - Execute workflow with JSON input

Frontend API base URL configured in `flow-frontend/.env`: `VITE_API_BASE_URL`

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

### Workflow Persistence Layer Architecture
```
flow-domain (Domain Layer)
├── WorkflowRepository (interface)
├── WorkflowExecutionStore (interface)
└── r2dbc/
    ├── WorkflowEntity (R2DBC mapping)
    ├── WorkflowEntityRepository (Spring Data R2DBC)
    ├── R2dbcWorkflowRepository (repository implementation)
    ├── WorkflowExecutionEntity (R2DBC mapping)
    ├── WorkflowExecutionEntityRepository (Spring Data R2DBC)
    └── R2dbcWorkflowExecutionStore (store implementation)

flow-app (Application Layer)
├── DefaultDatasourceExecutionService (coordinates datasource execution)
└── DefaultWorkflowExecutionService (orchestrates workflow execution)
```

### Service Layer Separation
- **flow-domain**: Contains service interfaces (`DatasourceExecutionService`, `WorkflowExecutionService`)
- **flow-app**: Contains service implementations (`DefaultDatasourceExecutionService`, `DefaultWorkflowExecutionService`)
- This avoids circular dependencies between flow-domain and flow-connector

### Workflow Execution Flow
1. Controller receives request
2. Validates workflow is ENABLED
3. Creates WorkflowExecutionId (UUID)
4. Calls WorkflowExecutionService.execute()
5. Service orchestrates DAG execution with field mapping
6. For DATASOURCE nodes: calls DatasourceExecutionService → executes via ConnectorAdapter
7. Saves execution state through WorkflowExecutionStore
8. Returns execution result with input/output/timing

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
- React components should use DaisyUI patterns for consistency
- All API calls should go through React Query for automatic caching and refetching
- New endpoints should be added to both backend WorkflowController and frontend workflowApi

## Quick Start

To run the complete workflow system:

```bash
# Terminal 1: Start backend
mvn spring-boot:run

# Terminal 2: Start frontend
cd flow-frontend
npm run dev
```

Access the frontend at http://localhost:5173 and the backend API at http://localhost:8080