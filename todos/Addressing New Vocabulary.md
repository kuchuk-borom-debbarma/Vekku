# Weighted Hierarchical Auto-Tagging: Addressing New Vocabulary

## 💔 The Problem: Vocabulary Mismatch and Semantic Drift

The system currently relies on a fixed set of SBERT embeddings for all its tags. This architecture fails when new terminology emerges in the content, leading to missed opportunities and system drift.

### 1. Vocabulary Mismatch

* **Fixed Tag Set:** The pre-calculated Level 1 Child Tag Embeddings (e.g., 'LLMs', 'Finetuning') do not include new, emerging terms (e.g., 'RAG', 'Vector Database').
* **Failed Semantic Match:** When a segmented chunk discusses a new term, the chunk's SBERT vector is compared to the fixed tag embeddings. Since the new term is not present in the taxonomy, the highest similarity score is only moderate (e.g., 0.55 - 0.70).
* **Result:** The system fails to assign the most specific, high-value tag (e.g., 'RAG') because the calculated weight falls below the confident tagging threshold (e.g., 0.75). The system effectively misses the most relevant keyword for the content.

### 2. Semantic Drift and Maintenance Burden

* **Over-reliance on Parent Tags:** The system may correctly assign the broad Level 2 Parent Tag (e.g., 'AI/ML'), but this is often too general for effective search and retrieval.
* **Manual Upkeep:** Human operators must manually monitor content for new terms, manually define their taxonomy placement, and trigger the recalculation of the Parent Embeddings. This process is slow, reactive, and not scalable.

---

## 💡 Potential Solution: KeyBERT-Powered Unsupervised Detection

The solution is to automate the discovery and placement of new, highly relevant terms using a semantic-aware keyphrase extraction technique: **KeyBERT**.

### 1. New Tag Candidate Extraction (KeyBERT)

This step uses SBERT to find the most representative keyphrases within a topically coherent content chunk.

* **Algorithm of Choice:** **KeyBERT** is selected because it uses the exact same **SBERT/Sentence-Transformers** models already employed in the system, ensuring seamless integration and consistent vector space analysis.
* **Process:** KeyBERT is run on the input chunk to generate the top $N$ keyphrases that are most semantically central to the chunk's overall embedding.
* **Output:** A list of high-quality candidate phrases (e.g., "RAG", "Prompt Engineering").

### 2. Candidate Filtering and Flagging

* **Similarity Check:** Each candidate keyphrase is encoded with SBERT, and its vector is compared against **ALL existing Level 1 Child Tag Embeddings**.
* **New Term Detection:** If the maximum similarity score to *any* existing tag is **below a low threshold** (e.g., < 0.60), the phrase is flagged as a genuine **"New Tag Candidate."** This confirms it represents a concept currently unrepresented in the taxonomy.

### 3. Automated Hierarchical Placement

The SBERT architecture is used to automatically propose the placement of the new term within the Level 2 Parent Taxonomy.

* **Parent Comparison:** The new tag's SBERT embedding is compared against the pre-calculated **Level 2 Parent Embeddings** (the average vectors for 'AI/ML', 'Software Engineering', etc.).
* **Placement Proposal:** The Parent Tag with the highest cosine similarity is the proposed category.
* **Result:** The system presents a semi-automated action to the operator: "NEW TAG FOUND: 'RAG'. PROPOSED PARENT: 'AI/ML'." This dramatically reduces the human maintenance effort and keeps the system current.
