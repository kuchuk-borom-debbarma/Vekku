# Project Architecture & Agent Rules

These rules define the required architectural patterns for the **Vekku** backend (`vekku-server` and `vekku-brain-service`). All future code changes must adhere to these principles.

## 1. Service Layer Architecture

We follow a **Strictly Decoupled Service Architecture** with an **Orchestrator Pattern**.

### A. Core Services (Independent)
- **Definition**: Services that manage a single domain or entity (e.g., `TagService`, `EmbeddingService`).
- **Rule**: Core Services MUST NOT depend on other Core Services.
    - ❌ `TagService` calls `EmbeddingService` directly.
    - ✅ `TagService` only talks to `TagRepository`.
- **Naming**: `[Entity]Service` (e.g., `TagService`).
- **Location**: `services/core/[domain]/`.

### B. Orchestrators (Coordinators)
- **Definition**: Classes responsible for workflows that span multiple domains.
- **Rule**: Orchestrators contain business logic that requires coordination between independent services.
- **Dependencies**: Can inject multiple Core Services.
- **Naming**: `[Workflow]Orchestrator` or `[Entity]Orchestrator` (e.g., `TagOrchestrator`).
- **Location**: `services/orchestration/`.
- **Transaction**: Orchestrators should typically define the `@Transactional` boundary for composite writes.

### C. Controllers
- **Rule**:
    - For **Read** operations (simple independent logic): Controller MAY call a Core Service directly.
    - For **Write** operations (complex logic): Controller MUST call an Orchestrator.
- **Goal**: Keep controllers thin; logic belongs in Orchestrators.

## 2. External Integrations
- External microservices (like the Brain/AI service) should be wrapped in a Core Service.
- **Naming**: Use logical names, not implementation names.
    - ❌ `BrainService` (too specific to implementation).
    - ✅ `EmbeddingService` (describes the capability).

## 3. Directory Structure
```
src/main/java/dev/kbd/vekku_server/
├── controllers/          # REST Endpoints
├── services/
│   ├── core/
│   │   ├── tag/          # Pure DB Service
│   │   └── embedding/    # Pure AI Interface
│   └── orchestration/    # Coordination Logic
```

## 4. Coding Standards
- **Interfaces**: Core external integrations must be defined as Interfaces in `services/core/` and implemented separately (e.g., `RemoteEmbeddingService`).
- **DTOs**: Use specific DTOs for external service communication to decouple internal domain models from external API contracts.
