---
description: Generates a detailed, educational Commit Journal entry explaining technical implementation details.
---

1.  **Analyze Context**: usage `git status` or `git diff` (if available) or review the files modified in the current session to understand the scope of work.
2.  **Identify Topic**: Determine the main technical theme (e.g., "Neo4j Graph Implementation", "Spring Security Setup", "React Component Lifecycle").
3.  **Draft Journal Entry**: Create or append to a file named `Commit_Journal.MD` in the project root.
    *   **Header**: Date and Topic.
    *   **Overview**: High-level summary of the architectural change.
    *   **Component Deep Dive**: For each modified component (Data Model, Repo, Service, Controller, Config):
        *   Cite the file path.
        *   Explain the *purpose* of the component.
        *   **Crucial**: Explain the *underlying concepts*. Do not just say "Added @Node". Say "Added @Node to define this class as a vertex in the graph database...".
        *   Explain specific implementation details (e.g., "Why use a Set for parents?", "How does the recursive query work?").
    *   **Infrastructure**: Document any changes to `pom.xml`, `docker-compose.yaml`, or properties files and *why* they were needed.
4.  **Review**: Ensure the tone is educational—aimed at the "future self" who needs to re-learn the system.
5.  **Write/Update**: Use `write_to_file` to save the content.
