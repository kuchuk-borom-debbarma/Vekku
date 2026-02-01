# Vekku

> **Transforming raw data into a structured semantic web.**

Vekku is an intelligent digital knowledge base designed to help you organize your thoughts, projects, and ideas with the power of AI. Unlike traditional note-taking apps that rely on manual organization, Vekku uses semantic analysis to automatically suggest tags, discover connections between concepts, and help you build a graph of knowledge effortlessly.

## 🚀 Why Vekku?

-   **🧠 Semantic Intelligence**: Automatically analyzes your content to suggest relevant tags and categorize information based on meaning, not just keywords.
-   **⚡ High Performance**: Built on the **Bun** runtime and **React 19**, ensuring instant interactions and lightning-fast data processing.
-   **💎 Glassmorphism UI**: A beautiful, modern interface featuring liquid-glass aesthetics that provides a focused and pleasant user experience.
-   **🌐 Platform Agnostic Backend**: Architected to run anywhere — from a local high-performance server to edge networks like Cloudflare Workers.

## 🛠️ Tech Stack

Vekku is a full-stack application composed of two main parts:

### Frontend (`/web`)
-   **Framework**: React 19 (via Vite)
-   **Language**: TypeScript
-   **Styling**: Tailwind CSS v4, shadcn/ui (Radix UI)
-   **State**: React Context & Hooks
-   **Design**: Modern glassmorphism with responsive layouts

### Backend (`/backend`)
-   **Runtime**: Bun (Optimized for speed)
-   **Framework**: Hono (Platform agnostic adapter pattern)
-   **Database**: PostgreSQL (via Neon Serverless)
-   **ORM**: Drizzle ORM
-   **AI**: Cloudflare AI (Text embedding models)
-   **Architecture**: Event-driven, functional modules

## 📂 Project Structure

```bash
Vekku-Meta/
├── backend/    # The API service (Bun/Hono/Drizzle)
├── web/        # The Frontend application (React/Vite)
├── docs/       # Documentation and architectural decisions
└── README.md   # This file
```

## 📚 Documentation

For deeper dives into specific parts of the system:
-   **[Backend Guide](./backend/GEMINI.md)**: Detailed API flows, auth strategy, and pagination logic.
-   **[Frontend Guide](./web/GEMINI.md)**: UI architecture, component structure, and state management.
-   **[Docs Folder](./docs)**: Architectural decisions and future roadmap.

---

## 💻 Local Development Setup

*Vekku is already deployed and ready to use. However, if you wish to run a local instance or contribute to development, follow the steps below.*

### Prerequisites
-   **Node.js** & **npm** (for Frontend)
-   **Bun** (for Backend)
-   **PostgreSQL** Database (e.g., Neon)
-   **Cloudflare Workers AI** (for Embeddings)
-   **Upstash Redis** (for Caching/Rate Limiting)

### 1. Backend Setup

Navigate to the backend directory:

```bash
cd backend
bun install
```

**Environment Variables:**
Copy `.env.example` to `.env` and populate the following secrets:

| Variable | Description |
| :--- | :--- |
| `DATABASE_URL` | PostgreSQL connection string (Neon Tech). |
| `JWT_SECRET` | Secret key for signing authentication tokens. |
| `CLOUDFLARE_WORKER_AI_API_KEY` | API Key for Cloudflare Workers AI. |
| `CLOUDFLARE_WORKER_ACCOUNT_ID` | Cloudflare Account ID. |
| `CLOUDFLARE_AI_MODEL` | Embedding model (e.g., `@cf/baai/bge-small-en-v1.5`). |
| `EMBEDDING_THRESHOLD` | Similarity threshold for suggestions (e.g., `0.4`). |
| `EMBEDDING_MATCH_COUNT` | Number of suggestions to return (e.g., `10`). |
| `NOTIFICATION_API_CLIENT_ID` | Client ID for notification service (optional). |
| `NOTIFICATION_API_CLIENT_SECRET` | Secret for notification service (optional). |
| `UPSTASH_REDIS_REST_URL` | URL for Upstash Redis instance. |
| `UPSTASH_REDIS_REST_TOKEN` | Auth token for Upstash Redis. |
| `FRONTEND_URL` | URL of the frontend (e.g., `http://localhost:5173`). |

Run the development server:

```bash
bun run dev
```

### 2. Frontend Setup

Navigate to the web directory:

```bash
cd web
npm install
```

**Environment Variables:**
Create a `.env` file with the following configurations:

| Variable | Description |
| :--- | :--- |
| `VITE_API_URL` | Full URL to the backend API (e.g., `http://localhost:3000/api`). |

Start the frontend development server:

```bash
npm run dev
```

The application should now be running at `http://localhost:5173`.

---

*Created by **Kuchuk Borom Debbarma***
