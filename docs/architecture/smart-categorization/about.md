# Project Specification: Smart Hierarchical Categorization (v2)

## 1. System Overview
**Current State:**
Auto-tagging via vector embeddings is functional.

**New Objective:**
Implement **Smart Categorization** that places documents into a directory structure. The system must be capable of:
1.  **Disambiguation:** Distinguishing between identical words in different contexts (Polysemy).
2.  **Contextual Meaning:** Giving meaning to generic folders via their ancestry.
3.  **Adaptive Depth:** Deciding whether to place a file deep in a leaf node or stop at a higher-level parent folder.

---

## 2. The Core Problems

### A. The Polysemy Problem (Context Conflict)
* **Scenario:** "Java" exists in `Tech` and `Food`.
* **Challenge:** A document must be routed based on the surrounding context (coding terms vs. coffee terms), not just the keyword "Java".

### B. The Ancestry Dependency Problem (Generic Leaves)
* **Scenario:** "Uncensored" exists in `News` and `Comedy`.
* **Challenge:** The folder `Uncensored` is meaningless on its own. The classifier must treat the target as the **full path** (e.g., `Comedy -> Uncensored`) to capture the semantic nuance (Shock Value vs. Graphic Reality).

### C. The Depth Dilemma (Stopping Early)
* **Scenario (Tech):** A document discusses "The rise of tech stocks and general software trends."
    * Path: `Tech -> Software -> Programming -> Java`.
* **The Issue:** While the document is about `Tech` and `Software`, it is **not** about `Java`. Forcing it into the deepest node (`Java`) is a misclassification.
* **Requirement:** The system must recognize that the document is "General" relative to the deeper nodes and stop at `Tech -> Software`.

### D. The Latent Attribute Paradox (The "Uncensored" nuance)
* **Scenario (Comedy):** A stand-up transcript contains explicit jokes.
* **The Issue:**
    * The embedding for `Comedy` (Parent) matches well (Score: 0.85).
    * The embedding for `Comedy -> Uncensored` (Child) might score *lower* (Score: 0.82) or identical.
    * *Why?* "Uncensored" is often a **stylistic attribute** (tone/restriction), not just a semantic topic. Standard embeddings might not weigh the "dirtiness" of the joke heavily enough to push it into the specific "Uncensored" bucket over the generic "Comedy" bucket.
* **Requirement:** The system needs a way to detect when a subtle attribute *should* force a deeper classification, even if the raw similarity score doesn't explicitly scream "Uncensored".

---

## 3. Functional Requirements & Logic

### 1. Adaptive Depth Logic (The "Delta" Threshold)
The system should not just look for the "highest score across all nodes." It must traverse and evaluate the **Delta (change in score)**.

* **Logic:** Calculate similarity for the Parent and the Child.
* **Rule:** Only descend to the Child if `Score(Child) > Score(Parent) + Threshold`.
    * *Example (General Tech Doc):*
        * `Tech`: 0.85
        * `Tech -> Java`: 0.82 (Lower or marginally higher)
        * **Action:** Stop at `Tech`.
    * *Example (Specific Java Doc):*
        * `Tech`: 0.80
        * `Tech -> Java`: 0.92 (Significant jump)
        * **Action:** Descend to `Java`.

### 2. Full Path Embedding
To solve the "Java" and "Uncensored" location issues, we never classify against a node name. We classify against the **Semantic Path String**.
* `Node A`: "Technology Software Programming Java"
* `Node B`: "Food Beverages Coffee Java"

### 3. Handling "Hard-to-Detect" Attributes (The Comedy/Uncensored Fix)
Since "Uncensored" is hard for a standard semantic vector to catch without specific keywords, we have two options:

* **Option A: Enriched Descriptions (Ghost Prompting)**
    * Don't just embed the path `Comedy -> Uncensored`.
    * Embed a description: *"Comedy routines containing explicit language, adult themes, dark humor, and restricted content."*
    * This forces the vector closer to the actual *content* of an uncensored doc.
* **Option B: The "Specifics" Bonus**
    * If a parent node (Comedy) has a generic catch-all nature, and a child node (Uncensored) is a "restriction" or "type," we might relax the threshold for descending.

---

## 4. Summary of Strategy

1.  **Flatten Paths:** Convert the tree into a list of descriptive strings (Ancestry included).
2.  **Calculate Scores:** Get similarity scores for the document against the relevant paths.
3.  **Apply Hierarchy Check:**
    * Start at Root.
    * Look at children.
    * If `Best_Child_Score` is significantly better than `Current_Node_Score`, go deeper.
    * Otherwise, **Stop and File Here.**