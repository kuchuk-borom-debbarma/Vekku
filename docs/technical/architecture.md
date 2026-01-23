# Vekku: Technical Design & Architecture Deep Dive

Vekku is an AI-native digital knowledge base designed for high-performance semantic organization. This document outlines the architectural decisions, data models, and specialized algorithms that power the platform.

## 1. System Overview

Vekku is architected as a **decoupled full-stack application**:
- **Backend**: A high-concurrency Bun runtime service optimized for edge deployment (Cloudflare Workers).
- **Frontend**: A React-based SPA utilizing modern Glassmorphism principles and atomic state management.
- **AI Core**: A semantic engine leveraging pgvector and LLM-based embedding models for automated categorization.

---

## 2. Backend Architecture (Main Focus)

### 2.1 Runtime & Framework Selection
- **Bun**: Chosen for its high-performance HTTP server, native TypeScript support, and extremely fast startup times (critical for serverless cold-starts).
- **Hono**: A lightweight, middleware-centric framework used as a **Universal Adapter**. This allows Vekku to run interchangeably on Bun, Node.js, or Cloudflare Workers without code changes.

### 2.2 Functional Modular Design
The backend rejects the traditional Class-based "Service/Controller" pattern in favor of **Functional Composition**:
- **Dependency Injection via Adapters**: Environment-specific configurations (DB strings, AI keys) are injected through a global adapter layer in `index.ts`.
- **Pure Logic Services**: Services are collections of exported functions that accept dependencies (like `db`) as arguments, making them 100% testable and side-effect-free.

### 2.3 Database Strategy (Neon + Drizzle)
- **Neon (PostgreSQL)**: Utilized for its serverless branching and native `pgvector` support.
- **Drizzle ORM**: Selected for its "TypeScript-first" philosophy and near-zero runtime overhead compared to heavy ORMs like Prisma.
- **Deterministic Concept Identification**: To prevent tag duplication across millions of users, Vekku uses **UUID v5 (Namespace UUID)**. Tags are hashed into deterministic IDs based on their normalized semantic meaning, ensuring that "TypeScript" and "typescript" always resolve to the same global concept.

---

## 3. The Semantic Suggestion Engine

The "Smart Suggestions" system is the core innovation of Vekku. It operates on two distinct levels:

### 3.1 Suggested Existing Tags (Semantic Similarity)
1. **Embedding**: When content is created, the system generates a high-dimensional vector (384d via BGE-Small) representing the text.
2. **Vector Search**: Using the `<=>` cosine distance operator in `pgvector`, the system performs a nearest-neighbor search against the user's existing tag library.
3. **Scoring**: Results are returned with a distance-based accuracy score, where lower distance indicates higher semantic relevance.

### 3.2 New Tag Suggestions (KeyBERT Strategy)
Vekku implements a local version of the KeyBERT algorithm:
1. **Candidate Generation**: The system extracts 1-gram and 2-gram fragments (n-grams) from the text.
2. **Cross-Reference**: These candidates are embedded and compared against the original document vector.
3. **Discovery**: Candidates with high similarity to the document but no existing tag match are suggested as "New Potential Tags."

### 3.3 Unified Text-Hash Caching
To optimize cost and performance:
- AI results are expensive and rate-limited.
- Vekku generates a **SHA-256 hash** of the content body.
- This hash serves as the cache anchor in **Upstash (Redis)**.
- If a user edits a title but not the body, or creates content with identical text, the AI step is bypassed entirely for a sub-5ms cache hit.

---

## 4. Specialized Systems

### 4.1 Chunk-Based Two-Step Pagination
Standard `OFFSET/LIMIT` pagination degrades linearly ($O(N)$) as datasets grow. Vekku solves this with a **Cursor-Anchored Hybrid Strategy**:
1. **Segment Fetching**: The system fetches a "Chunk" (e.g., 20 items) based on a timestamp cursor.
2. **Local Pagination**: Within that 20-item chunk, the frontend performs $O(1)$ slicing.
3. **Seamless Transition**: When the user hits the end of a segment, the system uses the last item's ID as the `nextChunkId` to fetch the next contiguous block.

### 4.2 Event-Driven Background Learning
To prevent API latency, tag learning is asynchronous:
- When a new tag is created, the `TagService` publishes a `TAG.CREATED` event to an **Internal Event Bus**.
- A **Background Listener** catches this, interacts with the AI model to generate the embedding, and updates the database.
- On Cloudflare, this utilizes `ctx.waitUntil`, ensuring the user request completes while the heavy lifting continues in the background.

---

## 5. Security & Performance

- **Stateless Auth**: Custom JWT implementation with Bun's native password hashing (Argon2/bcrypt).
- **CORS Hardening**: Strict origin-locking to the production frontend URL via environment variables.
- **Rate Limiting**: Tiered limiting (API-wide and AI-specific) using Upstash fixed-window algorithms to prevent cost explosions and abuse.

---

## 6. Technical Stack Summary

- **Language**: TypeScript (End-to-End)
- **Runtime**: Bun / Cloudflare Workers
- **Database**: PostgreSQL (Neon) + pgvector
- **ORM**: Drizzle
- **Cache/Infra**: Upstash Redis, Upstash Ratelimit
- **AI**: Cloudflare Workers AI (@cf/baai/bge-small-en-v1.5)
- **Frontend**: React 19, Vite, Tailwind CSS v4, Lucide Icons

---

*Architected and developed by Kuchuk Borom Debbarma.*
