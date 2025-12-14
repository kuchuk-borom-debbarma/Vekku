# Commit Journal - Implementing Hierarchical Tagging with Neo4j

**Date**: December 15, 2025
**Topic**: Graph-Based Tagging System Implementation

## Overview
This entry documents the implementation of a hierarchical tagging system for **Vekku**. We transitioned from a simple list-based approach to a **Graph-Based** approach using **Neo4j**. This allows tags to have relationships (specifically `CHILD_OF`), enabling us to build complex taxonomies (e.g., `Java` -> `Backend` -> `Software`).

## 1. The Data Model: Defining nodes in the Graph
**File**: `src/main/java/dev/kbd/vekku_server/services/independent/tagService/neo4jTagService/models/Tag.java`

We defined the `Tag` class as a **Node** in our Neo4j graph.

### Key Annotations & Concepts:
- **`@Node`**: Marks this class as a graph node. Neo4j will create a label called `Tag`.
- **`@Id @GeneratedValue`**: Neo4j handles identity internally. We use a `Long` for the ID.
- **`@Relationship`**: This is the most critical part. 
    ```java
    @Relationship(type = "CHILD_OF", direction = Relationship.Direction.OUTGOING)
    private Set<Tag> parents = new HashSet<>();
    ```
    - **Type (`CHILD_OF`)**: Defines the name of the edge connecting two nodes.
    - **Direction (`OUTGOING`)**: Defines the arrow's direction. `(This Tag) -> [CHILD_OF] -> (Parent Tag)`.
    - **Set<Tag>**: allows a tag to potentially have multiple parents (Polyhierarchy), essentially creating a Directed Acyclic Graph (DAG) rather than just a tree.

## 2. The Repository Layer: Talking to Neo4j
**File**: `src/main/java/dev/kbd/vekku_server/services/independent/tagService/neo4jTagService/Neo4jTagRepo.java`

We utilized **Spring Data Neo4j**, which works very similarly to Spring Data JPA but for graphs.

### Key Features:
- **Extends `Neo4jRepository<Tag, Long>`**: Gives us CRUD operations for free (`save`, `findById`, etc.).
- **Custom Cypher Queries**:
    ```java
    @Query("MATCH (t:Tag {name: $name})-[:CHILD_OF*]->(parent:Tag) RETURN parent")
    List<Tag> findAllAncestors(String name);
    ```
    - **`MATCH`**: The SQL "SELECT" equivalent for graphs.
    - **`(t:Tag {name: $name})`**: Finds the starting node where `name` matches our input.
    - **`-[:CHILD_OF*]->`**: This is **Variable Length Path Matching**.
        - The `*` implies "recurse continuously".
        - It hops from the start tag to its parent, then that parent's parent, and so on.
    - **`RETURN parent`**: Returns all nodes found along that path.

## 3. The Service Layer: Business Logic
**File**: `src/main/java/dev/kbd/vekku_server/services/independent/tagService/neo4jTagService/Neo4jTagService.java`

This service orchestrates the creation and retrieval of tags.

### The `createTag` Workflow:
1.  **Find or Create Child**: We first check if the tag (e.g., "Java") exists. If not, we instantiate it.
2.  **Parent Linking (The Edge)**:
    - If a `parentTagName` is provided (e.g., "Backend"), we look it up.
    - **Crucial Step**: We ensure the parent *also* exists (Find or Create).
    - We then simply add the parent object to the child's `parents` set:
        ```java
        tag.getParents().add(parent);
        ```
3.  **Persistence**:
    - We call `repo.save(tag)`.
    - **Spring Data Neo4j is smart**: When we save the *Child*, it detects the relationship to the *Parent* and automatically creates the `[:CHILD_OF]` edge in the database.

## 4. The CLI Interface: Interactive Testing
**File**: `src/main/java/dev/kbd/vekku_server/cli/VekkuCommands.java`

To interact with our backend without a frontend, we added **Spring Shell**.

### Key Annotations:
- **`@ShellComponent`**: Registers the class as a CLI command provider.
- **`@ShellMethod`**: Maps a Java method to a command line instruction (e.g., `tag create`).
- **`@ShellOption`**: Maps arguments (e.g., `--parent`).

### Commands Added:
- `tag create <name> --parent <parent>`: Calls the service to build the graph.
- `tag ancestors <name>`: Visualizes the hierarchy by printing the list of ancestors returned by our recursive Cypher query.

## 5. Infrastructure & Configuration
### Dependencies (`pom.xml`)
- **`spring-boot-starter-data-neo4j`**: Core driver and object mapping for Neo4j.
- **`spring-shell-starter`**: For the CLI capabilities.
- **`spring-ai-starter-model-transformers`**: (Added) To enable local embedding models for future vector operations.

### Docker & Properties (`docker-compose.yaml` & `application.properties`)
- We configured a **Neo4j 5.x** container exposed on port `7687` (Bolt protocol).
- We set memory limits to keep the container lightweight (`512M` pagecache/heap).
- We configured Spring Boot to talk to Neo4j using the `bolt://localhost:7687` URI and credentials.

---
**Summary**: We have successfully built the skeleton of a semantic knowledge graph. Tags are no longer isolated strings; they are interconnected nodes, allowing Vekku to understand that `Java` is a type of `Backend` technology, which is a type of `Software`.

---

# Commit Journal - AI Brain Service Refactoring & RAG

**Date**: December 15, 2025
**Topic**: Qdrant Vector Search & Solidity Refactoring

## Overview
This entry documents the refinement of the `QdrantBrainService`, which serves as the AI-powered suggestion engine for Vekku. We improved code solidity by replacing console outputs with proper logging and enhanced the user experience by expanding the search result limit. We also documented the underlying **RAG (Retrieval-Augmented Generation)** mechanics.

## 1. Solidity & Refactoring
**File**: `src/main/java/dev/kbd/vekku_server/services/independent/brainService/qdrantBrain/QdrantBrainService.java`

We addressed several technical debt items to make the service production-ready.

### Changes:
- **Logging**: Replaced `System.out.println` with **SLF4J** (`log.info`).
    - *Why?* `System.out` is synchronous and unmanageable. SLF4J allow us to control log levels (INFO, DEBUG, ERROR) and output formats (JSON, file, console) via configuration without changing code.
- **Search Experience**: Increased `topK` from `1` to `5`.
    - *Why?* When a user asks for "cooking", a single result might be miss "Baking" or "Recipes". Providing top 5 nearest neighbors gives the user better options.

## 2. Educational Deep Dive: The AI "Brain"
We added comprehensive Javadocs to explain *how* the magic happens.

### Key Concepts:
1.  **Vector Embeddings (The "Thought" Representation)**:
    - We interpret user text not as strings, but as **Vectors** (arrays of floats, e.g., `[0.1, -0.9, 0.45...]`).
    - We use **Spring AI** with `ONNX` transformers (BGE-Small model) to perform this translation locally.
    - *Insight*: "Apple" (Fruit) and "Apple" (Tech) would have different vectors if the context is clear, but "Apple" and "Pear" will be mathematically "close" in the vector space because they are both fruits.

2.  **RAG (Retrieval-Augmented Generation)**:
    - Although we aren't "Generating" text (like ChatGPT) yet, we are doing the "Retrieval" part.
    - **Step 1 (Ingest)**: When a tag is created, we embed it and save it to **Qdrant** with metadata `type=TAG`.
    - **Step 2 (Retrieve)**: When a user types content, we embed that query and ask Qdrant for the "Nearest Neighbors" that match `type=TAG`.

### Code Spotlight:
```java
SearchRequest request = SearchRequest.builder()
    .query(content)
    .topK(5) // Find 5 closest matches
    .similarityThreshold(0.3) // 0.0 (Different) to 1.0 (Identical). 0.3 allows fuzzy matches.
    .filterExpression("type == 'TAG'") // Metadata filtering
    .build();
```

## 3. Infrastructure
- We rely on **Qdrant** running in Docker to store these vectors.
- We rely on **Spring AI** to abstract away the HTTP calls to Qdrant and the complex math of embeddings.
