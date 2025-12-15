/**
 * Helper to find where 'search' appears in 'text' starting from 'fromIndex',
 * ignoring differences in whitespace sequences.
 * 
 * @param text The full text to search in
 * @param search The substring to find
 * @param fromIndex The index to start searching from
 * @returns Object with start/end indices or null if not found
 */
export function findFuzzyMatch(text: string, search: string, fromIndex: number): { start: number, end: number } | null {
    let tIdx = fromIndex;
    let sIdx = 0;
    let matchStart = -1;

    while (tIdx < text.length && sIdx < search.length) {
        const tChar = text[tIdx];
        const sChar = search[sIdx];

        // If characters match, proceed
        if (tChar === sChar) {
            if (matchStart === -1) matchStart = tIdx;
            tIdx++;
            sIdx++;
            continue;
        }

        // If mismatch, check if it's due to whitespace
        const tIsSpace = /\s/.test(tChar);
        const sIsSpace = /\s/.test(sChar);

        if (tIsSpace && sIsSpace) {
            // Both are whitespace, consume both until non-whitespace
            while (tIdx < text.length && /\s/.test(text[tIdx])) tIdx++;
            while (sIdx < search.length && /\s/.test(search[sIdx])) sIdx++;
            continue;
        } else if (tIsSpace) {
            // Text has extra whitespace, skip it
            tIdx++;
            continue;
        } else if (sIsSpace) {
            // Search has extra whitespace?
            sIdx++;
            continue;
        }

        // Check if search char is punctuation (likely injected by us) and text char is space/other
        if (/[.,!?;]/.test(sChar) && !/[.,!?;]/.test(tChar)) {
            // Skip the injected punctuation in search
            sIdx++;
            continue;
        }

        // Real mismatch
        // Reset search (naive backtracking - if we were matching)
        if (matchStart !== -1) {
            // Reset to character after matchStart
            tIdx = matchStart + 1;
            sIdx = 0;
            matchStart = -1;
        } else {
            tIdx++;
        }
    }

    if (sIdx >= search.length) {
        // Found complete match
        return { start: matchStart, end: tIdx };
    }

    return null;
}
