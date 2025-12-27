# Content Feature

## Core Responsibility

To serve as the single source of truth for all user-generated content. Its sole concern is the integrity, storage, and lifecycle of the content itself.

## Boundaries

- This feature's responsibility begins when content is submitted and ends when it is deleted.
- It is responsible for persisting and retrieving content.
- It must announce significant changes to a piece of content (e.g., creation, major update) to the rest of the system, without knowing which other features will react.
- It is **not** responsible for what other features do with the content's data (e.g., analyzing it, deriving new information, or tagging it).
