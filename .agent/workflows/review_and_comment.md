---
description: detailed code review to ensure solidity and add educational comments.
---

1.  **Scope**: Identify the active files or the specific files the user wants checked.
2.  **Solidity Check (Refactor)**:
    *   Scan for **Anti-Patterns**: Internal APIs (e.g., Netty StringUtil), deprecated methods, or raw types.
    *   Scan for **Logic Bugs**: Missing Null checks, incorrect transaction boundaries (`@Transactional`), or potential concurrency issues.
    *   **Action**: If valid issues are found, use `replace_file_content` to fix them immediately. Explain the fix in the tool description.
3.  **Educational Commenting**:
    *   **Target**: Complex logic, annotations (Spring/JPA/Neo4j), or algorithmic sections.
    *   **Style**: Use Javadoc (`/** ... */`) for classes/methods and inline (`//`) for specific lines.
    *   **Content**:
        *   Explain **Concepts**: "Why relies on a Graph traversal here?"
        *   Explain **Mechanics**: "How does this stream reduce work?"
        *   Explain **Annotations**: "What does @Service actually do for Spring?"
    *   **Action**: Use `replace_file_content` to inject these comments.
4.  **Verify**: Ensure the code still compiles (mentally check imports and syntax) and that comments add value, not noise.
