# Suggestion Feature

## Core Responsibility

To enhance content by generating intelligent suggestions, such as tags or keywords.

## Boundaries

- This feature's responsibility begins when it is notified that a piece of content has been created or changed.
- Its primary role is to orchestrate the analysis of content, which may involve communicating with external, intelligent services (e.g., an AI).
- After generating suggestions, its responsibility is to delegate the *application* of these suggestions to the appropriate feature (e.g., it asks the `tag` feature to apply its suggested tags).
- It is **not** responsible for storing the content or the tags themselves. It is a stateless processing feature that acts as a bridge between the system's core data and its external intelligence.
