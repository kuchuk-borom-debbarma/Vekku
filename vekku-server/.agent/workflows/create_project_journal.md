---
description: Generates a high-level, narrative Project Journal entry in docs/journal/ to document architectural decisions.
---

1.  **Analyze Context**: Understand the "Big Picture" milestone just achieved (e.g., "Finished Tag Service", "Defined Service Triad").
2.  **Determine Metadata**:
    *   **Next Entry ID**: strict increment from the last file in `docs/journal/` (e.g., if `001...` exists, this is `002`).
    *   **Phase**: Current project phase (e.g., "Architecture", "Implementation", "Refining").
    *   **Topic**: A short, punchy title.
3.  **Draft Content**:
    *   **File Name**: `docs/journal/[ID].[Topic].md`.
    *   **Header**:
        ```markdown
        # 📓 Vekku Project Journal
        **Entry:** [ID]
        **Date:** [YYYY-MM-DD]
        **Phase:** [Phase]
        ```
    *   **Voice**: First-person ("I decided...", "We moved..."). Use analogies ("The Librarian", "The Brain").
    *   **Sections**:
        *   **Context**: What was the goal?
        *   **Decisions**: Why did we choose X over Y? (e.g., "Why Neo4j instead of SQL?").
        *   **Implementation**: High-level summary of what was built.
        *   **Next Steps**: Where does this lead us?
4.  **Save**: Write the file to `docs/journal/`.
