# Weighted Hierarchical Auto-Tagging: Continuous Learning from User Feedback

## 💔 The Problem: Correcting Semantic Mistakes and Learning Speed

The final and most crucial challenge is building an **Active Learning** loop that uses user corrections to continuously adapt the SBERT model, ensuring its semantic vector space aligns with human domain expertise, and to do so quickly and efficiently.

### 1. The Slow Learning Problem (Triplet Loss Limitation)

* **Binary Correction:** Traditional methods like Triplet Loss only train on a simple relative ranking: **Anchor** is closer to **Positive** than to **Negative**.
* **Wasted Nuance:** This method forces the system to treat all corrections as binary (right vs. wrong), wasting the opportunity to capture the user's more nuanced feedback, such as a tag being "Highly Related" versus "Moderately Related."
* **Slow Convergence:** Relying solely on relative distance can be slow, as the model doesn't get a strong signal about the *absolute* desired similarity score for a pair, slowing down the alignment of the SBERT vector space to the domain.

### 2. Semantic Drift and Misalignment

* **Domain-Specific Errors:** General-purpose SBERT models can make domain-specific errors (e.g., mistakenly placing a new term "Zenith" closer to the "Software Engineering" vector when it should be closer to "Data Science").
* **Stagnant Embeddings:** Without an active feedback loop, the system remains ignorant of its past errors, leading to repeated misclassifications and requiring constant, costly manual intervention.

---

## 💡 Potential Solution: Incremental Fine-Tuning with Cosine Similarity Loss

The solution is to utilize the user's explicit feedback on the degree of relatedness to train the SBERT model directly on **absolute relevance scores** using a **Regression Loss**.

### 1. Data Collection: Graded Relationship Injection

User corrections are transformed from a binary event into a **Graded Score** (e.g., a float between 0.0 and 1.0) based on how related two terms are.

* **Training Data Format:** Every correction becomes a pair of terms and a target similarity score: $(\text{Text}_A, \text{Text}_B, \text{Target Score})$.
    * *Example:* If a user indicates "Zenith" is highly related to "Data Science," the pair is $(\text{"Zenith"}, \text{"Data Science"}, \text{Target Score} = 0.95)$.
* **Input Efficiency:** This allows the user to quickly provide a set of words and their degree of relatedness, maximizing the value of each piece of human input.

### 2. Model Fine-Tuning: Cosine Similarity Loss (Regression)

The core SBERT model is periodically fine-tuned using the collected graded pairs and a **Cosine Similarity Loss** function (often implemented as a Mean Squared Error on the similarity scores).

* **Goal:** The loss function directly trains the model to minimize the difference between its predicted cosine similarity, $S(\text{Text}_A, \text{Text}_B)$, and the user-provided target score.
    $$\text{Loss} = \sum (\text{Target Score} - S(\text{Text}_A, \text{Text}_B))^2$$
* **Fast Learning:** This forces the embeddings to be adjusted immediately to yield the desired absolute score. This directly optimizes the SBERT model for your weighted tagging system, leading to **faster convergence** on domain-specific relevance.

### 3. System Adaptation

* **Global Correction:** The fine-tuned SBERT model's updated weights ensure that the semantic space is globally corrected.
* **Embedding Recalculation:** All stored **Level 1** and **Level 2** Tag Embeddings are recalculated using the updated model, making the entire system adapt to the learned relationships.
* **Accurate Weights:** When new content arrives, the corrected SBERT model places the chunk's vector correctly, ensuring the resulting **Cosine Similarity score (weight)** is highly accurate and reflects the user-defined relevance.
