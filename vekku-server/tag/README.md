# Tag Feature

## Core Responsibility

To manage the concept of "tags" and establish the relationship between tags and content.

## Boundaries

- This feature is responsible for maintaining a consistent dictionary of all available tags.
- It provides the functionality to link tags to a piece of content and to query those links.
- It exposes its capabilities to be used by other features (e.g., allowing the `suggestion` feature to apply new tags to content).
- It is **not** responsible for generating or suggesting which tags should be applied. It only manages their application, removal, and retrieval.
