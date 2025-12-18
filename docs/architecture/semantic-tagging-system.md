# 2. Semantic Tagging System (Alias vs. Concept)

**Date:** 2025-12-19
**Status:** Proposal / Design Phase

## 1. Overview
We are fundamentally refactoring the Tagging System to decouple the **Visual Representation** (Alias) from the **Semantic Meaning** (Concept). This change addresses the core limitation where a user's preferred naming convention (e.g., "Stuff") historically obscured the semantic value needed for AI auto-tagging.

### The Problem
- **Ambiguity & Personalization Conflict**: Users want to name tags anything (e.g., "Daily Grind"), but the AI needs specific terms (e.g., "Software Engineering") to perform accurate vector matching.
- **Polysemy**: A single word like "Bat" can mean "Sports Equipment" or "Flying Mammal". Storing them as a single string causes vector pollution.
- **Lack of Precision**: Loose semantic buckets (merging "React" and "Vue" into "Frontend") dilute the vector quality and make future hierarchical reasoning impossible.

### The Solution: "Decoupled Synonyms"
We propose treating every Tag as a **Single Semantic Concept** defined by a list of strict **Synonyms**, while allowing an arbitrary **Alias** for display.

---

## 2. Core Architecture

### Phase 1: The Separation
We introduce two distinct properties for every Tag:
1.  **Alias (Display Name)**: The human-readable label used in the UI.
    *   *Example*: "My Tech Stack"
2.  **Synonyms (The Anchors)**: A list of synonyms that anchor the tag to a specific concept in the vector space.
    *   *Example*: `["Java", "JDK", "JVM"]`

### Phase 2: "OR" Logic (Max Score)
When scoring content against a tag, we follow a **"Bucket" Strategy** using **Max Score** logic.
*   **Logic**: If the content matches *any* of the defined synonyms, the Tag is a match.
*   **Scoring**: The Tag's score is equal to the **highest matching synonym score**.
    *   *Match("Java") = 0.95*
    *   *Match("JDK") = 0.40*
    *   *Tag Score = 0.95*

### Phase 3: Mutability (Bucket Management)
The Semantic Bucket is **Mutable**. Users can refine the definition of a tag over time.
-   **Add Synonym**: "ReactJS" -> Brain embeds and adds a new point linked to "React".
-   **Remove Synonym**: "Angular" (oops) -> Brain deletes the specific point for "Angular".
-   **Rename Alias**: "My React Stuff" -> Server updates name. Brain updates payload `alias` (for display).

---

## 3. Structural Constraints

### "One Tag = One Concept"
To enable future hierarchical reasoning, we enforce that a Tag must represent a **Single Concept**.
-   **Bad**: Tag "Frontend" with synonyms `["React", "Vue", "CSS"]`. (These are related but distinct concepts).
-   **Good**: Tag "React" with synonyms `["React.js", "ReactJS"]`. Tag "Vue" with synonyms `["Vue.js"]`.

-   **Future Value**: This allows us to mathematically compost vectors in a hierarchy:
    *   `Vector(Parent) + Vector(Child) = Contextualized Child`

---

## 4. Data Structure

### Server (Postgres)
We formally introduce the `Tag` entity.

| Column | Type | Description |
| :--- | :--- | :--- |
| `id` | UUID | Unique Identifier |
| `name` | VARCHAR | The Alias ("My Tech Stack") |
| `synonyms` | TEXT[] | Array of synonym triggers |
| `user_id` | UUID | Owner |

### Brain (Qdrant)
We move from a 1:1 Tag:Point mapping to a 1:N mapping.

**Point Structure:**
*   **Vector**: Embedding of a single *Synonym Term*.
*   **Payload**:
    *   `tag_id`: UUID of the parent Tag.
    *   `alias`: The display name (for retrieval).
    *   `synonym_term`: The specific term used for this point (for debugging).
    *   `type`: "TAG"

---

## 5. Workflow

### 1. Learning (Indexing)
1.  **User Input**: Alias="Bank", Synonyms=`["Financial Institution", "Money Lender"]`.
2.  **Server**: Persists `Tag` entity. Sends `learn` request to Brain.
3.  **Brain**:
    *   Embeds "Financial Institution" -> Saves Point A.
    *   Embeds "Money Lender" -> Saves Point B.

### 2. Auto-Tagging (Retrieval)
1.  **Input**: Document mentioning "loans and interest rates".
2.  **Brain**: Embeds document chunk.
3.  **Search**: Finds Point A ("Financial Institution") with high similarity.
4.  **Result**: Returns `TagScore(alias="Bank", score=0.92)`.
