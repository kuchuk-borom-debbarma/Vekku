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

## 🏁 Getting Started

### Prerequisites
-   **Node.js** & **npm** (for Frontend)
-   **Bun** (for Backend)
-   **PostgreSQL** Database (e.g., Neon)

### 1. Backend Setup

Navigate to the backend directory and install dependencies:

```bash
cd backend
bun install
```

Configure your environment variables (copy `.env.example` to `.env`) and set up your database URL.

Run the development server:

```bash
bun run dev
```

### 2. Frontend Setup

Navigate to the web directory and install dependencies:

```bash
cd web
npm install
```

Start the frontend development server:

```bash
npm run dev
```

The application should now be running at `http://localhost:5173` (by default).

## 📚 Documentation

For deeper dives into specific parts of the system:
-   **[Backend Guide](./backend/GEMINI.md)**: Detailed API flows, auth strategy, and pagination logic.
-   **[Frontend Guide](./web/GEMINI.md)**: UI architecture, component structure, and state management.
-   **[Docs Folder](./docs)**: Architectural decisions and future roadmap.

---

*Created by **Kuchuk Borom Debbarma***