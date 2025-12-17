import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

interface TagScore {
    name: string;
    score: number;
}

interface ContentRegionTags {
    regionContent: string;
    regionStartIndex: number;
    regionEndIndex: number;
    tagScores: TagScore[];
}

export default function TagSuggestion() {
    const navigate = useNavigate();
    const [content, setContent] = useState('');
    const [rawTags, setRawTags] = useState<TagScore[]>([]);
    const [regionTags, setRegionTags] = useState<ContentRegionTags[]>([]);
    const [loading, setLoading] = useState(false);
    const [hoveredRegionIndex, setHoveredRegionIndex] = useState<number | null>(null);

    const [combinedTags, setCombinedTags] = useState<TagScore[]>([]);

    // Optional parameters
    const [threshold, setThreshold] = useState(0.3);
    const [topK, setTopK] = useState(50);

    const handleGetRawTags = () => {
        if (!content.trim()) return;

        setLoading(true);
        setRawTags([]); // Clear previous
        fetch('/api/brain/raw', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ content, threshold, topK })
        })
            .then(res => {
                if (!res.ok) throw new Error('Failed to fetch raw tags');
                return res.json();
            })
            // server returns list directly based on controller: return brainService.getRawTagsByEmbedding(...)
            // but wait, BrainController.java returns List<TagScore> directly.
            // Let's verify if Spring wraps it or if it is just a JSON array.
            // "return brainService.getRawTagsByEmbedding(...)" -> returns List.
            // So it is an array.
            .then((data: TagScore[]) => {
                setRawTags(data);
                setLoading(false);
            })
            .catch(err => {
                console.error(err);
                setLoading(false);
            });
    };

    const handleGetRegionTags = () => {
        if (!content.trim()) return;

        setLoading(true);
        setRegionTags([]); // Clear previous
        fetch('/api/brain/regions', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ content, threshold })
        })
            .then(res => {
                if (!res.ok) throw new Error('Failed to fetch region tags');
                return res.json();
            })
            .then((data: ContentRegionTags[]) => {
                setRegionTags(data);
                setLoading(false);
            })
            .catch(err => {
                console.error(err);
                setLoading(false);
            });
    };

    const handleGetCombinedTags = () => {
        if (!content.trim()) return;

        setLoading(true);
        setCombinedTags([]); // Clear previous
        fetch('/api/brain/suggest-combined', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ content, threshold, topK })
        })
            .then(res => {
                if (!res.ok) throw new Error('Failed to fetch combined tags');
                return res.json();
            })
            .then((data: TagScore[]) => {
                setCombinedTags(data);
                setLoading(false);
            })
            .catch(err => {
                console.error(err);
                setLoading(false);
            });
    };

    return (
        <div style={{ padding: '2rem', maxWidth: '1200px', margin: '0 auto', color: '#fff' }}>
            <button
                onClick={() => navigate('/')}
                style={{
                    padding: '0.5rem 1rem',
                    marginBottom: '1rem',
                    background: 'rgba(255,255,255,0.1)',
                    border: 'none',
                    borderRadius: '4px',
                    color: 'white',
                    cursor: 'pointer'
                }}
            >
                ← Back
            </button>

            <h1>Tagging Playground (Embedding IO)</h1>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem', marginBottom: '2rem' }}>
                <div style={{ display: 'flex', gap: '1rem' }}>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', flex: 1 }}>
                        <label style={{ fontSize: '0.9rem', color: '#aaa' }}>Threshold (0.0 - 1.0)</label>
                        <input
                            type="number"
                            step="0.05"
                            min="0"
                            max="1"
                            value={threshold}
                            onChange={(e) => setThreshold(parseFloat(e.target.value))}
                            style={{
                                padding: '0.5rem',
                                background: '#1a1a1a',
                                border: '1px solid #333',
                                borderRadius: '4px',
                                color: '#eee'
                            }}
                        />
                    </div>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', flex: 1 }}>
                        <label style={{ fontSize: '0.9rem', color: '#aaa' }}>Top K (Search Limit)</label>
                        <input
                            type="number"
                            value={topK}
                            onChange={(e) => setTopK(parseInt(e.target.value))}
                            style={{
                                padding: '0.5rem',
                                background: '#1a1a1a',
                                border: '1px solid #333',
                                borderRadius: '4px',
                                color: '#eee'
                            }}
                        />
                    </div>
                </div>

                <textarea
                    value={content}
                    onChange={(e) => setContent(e.target.value)}
                    placeholder="Enter text to analyze..."
                    style={{
                        width: '100%',
                        minHeight: '150px',
                        padding: '1rem',
                        background: '#1a1a1a',
                        border: '1px solid #333',
                        borderRadius: '8px',
                        color: '#eee',
                        fontSize: '1rem',
                        fontFamily: 'inherit'
                    }}
                />

                <div style={{ display: 'flex', gap: '1rem' }}>
                    <button
                        onClick={handleGetRawTags}
                        disabled={loading}
                        style={{
                            padding: '0.8rem 2rem',
                            background: '#ff9800',
                            border: 'none',
                            borderRadius: '6px',
                            color: 'white',
                            fontWeight: 'bold',
                            cursor: loading ? 'not-allowed' : 'pointer',
                            opacity: loading ? 0.7 : 1,
                            flex: 1
                        }}
                    >
                        Get Raw Tags
                    </button>
                    <button
                        onClick={handleGetRegionTags}
                        disabled={loading}
                        style={{
                            padding: '0.8rem 2rem',
                            background: '#00bcd4',
                            border: 'none',
                            borderRadius: '6px',
                            color: 'white',
                            fontWeight: 'bold',
                            cursor: loading ? 'not-allowed' : 'pointer',
                            opacity: loading ? 0.7 : 1,
                            flex: 1
                        }}
                    >
                        Get Region Tags
                    </button>
                    <button
                        onClick={handleGetCombinedTags}
                        disabled={loading}
                        style={{
                            padding: '0.8rem 2rem',
                            background: '#4CAF50',
                            border: 'none',
                            borderRadius: '6px',
                            color: 'white',
                            fontWeight: 'bold',
                            cursor: loading ? 'not-allowed' : 'pointer',
                            opacity: loading ? 0.7 : 1,
                            flex: 1
                        }}
                    >
                        Get Combined Tags
                    </button>
                </div>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
                {/* COMBINED TAGS SECTION */}
                {combinedTags.length > 0 && (
                    <div style={{
                        background: 'linear-gradient(145deg, #1e251e, #252525)',
                        border: '1px solid #4CAF50',
                        borderRadius: '8px',
                        padding: '1.5rem',
                    }}>
                        <h3 style={{ marginTop: 0, marginBottom: '1rem', color: '#81c784' }}>
                            🌌 Combined Tags (Overall + Regions)
                        </h3>
                        <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.8rem' }}>
                            {combinedTags.map((tag, index) => (
                                <span
                                    key={index}
                                    style={{
                                        display: 'flex',
                                        alignItems: 'center',
                                        gap: '0.5rem',
                                        padding: '0.5rem 1rem',
                                        background: '#333',
                                        borderRadius: '6px',
                                        fontSize: '0.9rem',
                                        border: '1px solid #555'
                                    }}
                                >
                                    <span style={{ fontWeight: 'bold', color: '#fff' }}>{tag.name}</span>
                                    <span style={{
                                        color: '#aaa',
                                        fontSize: '0.85rem'
                                    }}>
                                        {tag.score.toFixed(3)}
                                    </span>
                                </span>
                            ))}
                        </div>
                    </div>
                )}
                {/* RAW TAGS SECTION */}
                {rawTags.length > 0 && (
                    <div style={{
                        background: 'linear-gradient(145deg, #251e1e, #252525)',
                        border: '1px solid #444',
                        borderRadius: '8px',
                        padding: '1.5rem',
                    }}>
                        <h3 style={{ marginTop: 0, marginBottom: '1rem', color: '#ffcc80' }}>
                            🏷️ Raw Tags (Embedding Similarity)
                        </h3>
                        <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.8rem' }}>
                            {rawTags.map((tag, index) => (
                                <span
                                    key={index}
                                    style={{
                                        display: 'flex',
                                        alignItems: 'center',
                                        gap: '0.5rem',
                                        padding: '0.5rem 1rem',
                                        background: '#333',
                                        borderRadius: '6px',
                                        fontSize: '0.9rem',
                                        border: '1px solid #555'
                                    }}
                                >
                                    <span style={{ fontWeight: 'bold', color: '#fff' }}>{tag.name}</span>
                                    <span style={{
                                        color: '#aaa',
                                        fontSize: '0.85rem'
                                    }}>
                                        {tag.score.toFixed(3)}
                                    </span>
                                </span>
                            ))}
                        </div>
                    </div>
                )}

                {/* REGION TAGS SECTION */}
                {regionTags.length > 0 && (
                    <div>
                        <h3 style={{ color: '#80deea' }}>🧩 Content Regions</h3>
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                            {regionTags.map((region, index) => (
                                <div
                                    key={index}
                                    style={{
                                        background: '#1a1a1a',
                                        border: '1px solid #333',
                                        borderRadius: '8px',
                                        padding: '1.5rem',
                                        borderLeft: hoveredRegionIndex === index ? '4px solid #00bcd4' : '4px solid transparent',
                                        transition: 'all 0.2s'
                                    }}
                                >
                                    <div style={{
                                        marginBottom: '1rem',
                                        padding: '1rem',
                                        background: hoveredRegionIndex === index ? 'rgba(0, 188, 212, 0.1)' : '#252525',
                                        borderRadius: '4px',
                                        fontStyle: 'italic'
                                    }}>
                                        "{region.regionContent}"
                                    </div>

                                    <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem' }}>
                                        {region.tagScores.map((tag, tIndex) => (
                                            <span
                                                key={tIndex}
                                                onMouseEnter={() => setHoveredRegionIndex(index)}
                                                onMouseLeave={() => setHoveredRegionIndex(null)}
                                                style={{
                                                    display: 'flex',
                                                    alignItems: 'center',
                                                    gap: '0.5rem',
                                                    padding: '0.4rem 0.8rem',
                                                    background: '#333',
                                                    borderRadius: '20px',
                                                    fontSize: '0.9rem',
                                                    cursor: 'default',
                                                    border: '1px solid #444'
                                                }}
                                            >
                                                <span style={{ fontWeight: 'bold', color: '#fff' }}>{tag.name}</span>
                                                <span style={{ color: '#aaa', fontSize: '0.8rem' }}>
                                                    {tag.score.toFixed(2)}
                                                </span>
                                            </span>
                                        ))}
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
}
