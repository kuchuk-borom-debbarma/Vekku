import { cosineSimilarity } from './mathUtils';

/**
 * Calculates Maximal Marginal Relevance (MMR) to diversify search results.
 * 
 * @param candidateVectors - Vectors of candidate keywords
 * @param docVector - Vector of the document
 * @param candidates - List of candidate strings (parallel to vectors)
 * @param topK - Number of keywords to select
 * @param diversity - Diversity parameter (0.0 to 1.0)
 * @returns Ranking of selected indices
 */
export function calculateMMR(
    candidateVectors: number[][],
    docVector: number[],
    topK: number,
    diversity: number
): { index: number, score: number }[] {
    // 1. Calculate Cosine Similarities (Content vs Candidates)
    const similarities = candidateVectors.map(vec => cosineSimilarity(docVector, vec));

    // 2. Select Top Candidates based on Similarity (pre-filter for MMR)
    // Sort indices by score desc
    const sortedIndices = similarities
        .map((score, index) => ({ score, index }))
        .sort((a, b) => b.score - a.score)
        .slice(0, 50); // Take top 50 mostly relevant to reduce MMR compute

    const selectedIndices: number[] = [];
    const top50Indices = sortedIndices.map(s => s.index);

    while (selectedIndices.length < topK && top50Indices.length > 0) {
        let bestNextIndex = -1;
        let bestMMRScore = -Infinity;

        for (const candidateIdx of top50Indices) {
            const simToDoc = similarities[candidateIdx];
            let maxSimToSelected = 0;

            for (const selectedIdx of selectedIndices) {
                const sim = cosineSimilarity(candidateVectors[candidateIdx], candidateVectors[selectedIdx]);
                if (sim > maxSimToSelected) maxSimToSelected = sim;
            }

            // MMR Formula: (1-diversity) * Sim(Doc) - diversity * MaxSim(Selected)
            const mmrScore = (1 - diversity) * simToDoc - (diversity * maxSimToSelected);

            if (mmrScore > bestMMRScore) {
                bestMMRScore = mmrScore;
                bestNextIndex = candidateIdx;
            }
        }

        if (bestNextIndex !== -1) {
            selectedIndices.push(bestNextIndex);
            // Remove from pool
            const poolIdx = top50Indices.indexOf(bestNextIndex);
            if (poolIdx > -1) top50Indices.splice(poolIdx, 1);
        } else {
            break;
        }
    }

    return selectedIndices.map(idx => ({
        index: idx,
        score: similarities[idx]
    }));
}
