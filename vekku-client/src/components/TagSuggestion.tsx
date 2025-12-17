import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

interface TagScore {
    name: string;
    score: number;
}

interface TagPath {
    path: TagScore[]; // Detailed path with scores per node
    finalScore: number;
}

interface ContentRegionTags {
    regionContent: string;
    regionStartIndex: number;
    regionEndIndex: number;
    tagScores: TagScore[];
    taxonomyPaths: TagPath[];
}

interface SuggestTagsResponse {
    regions: ContentRegionTags[];
    overallTags: TagScore[];
}

export default function TagSuggestion() {
    const navigate = useNavigate();
    const [content, setContent] = useState('');
    const [suggestions, setSuggestions] = useState<ContentRegionTags[]>([]);
    const [overallTags, setOverallTags] = useState<TagScore[]>([]);
    const [loading, setLoading] = useState(false);
    const [hoveredRegionIndex, setHoveredRegionIndex] = useState<number | null>(null);

    // Optional parameters
    const [threshold, setThreshold] = useState(0.3);
    const [topK, setTopK] = useState(50);

    const handleSuggest = () => {
        if (!content.trim()) return;

        setLoading(true);
        fetch('/api/brain/suggest', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ content, threshold, topK })
        })
            .then(res => {
                if (!res.ok) throw new Error('Failed to fetch suggestions');
                return res.json();
            })
            .then((data: SuggestTagsResponse) => {
                setSuggestions(data.regions);
                setOverallTags(data.overallTags);
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

            <h1>Tag Suggestion Playground</h1>

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
                <button
                    onClick={handleSuggest}
                    disabled={loading}
                    style={{
                        alignSelf: 'flex-start',
                        padding: '0.8rem 2rem',
                        background: 'var(--color-primary, #646cff)',
                        border: 'none',
                        borderRadius: '6px',
                        color: 'white',
                        fontWeight: 'bold',
                        cursor: loading ? 'not-allowed' : 'pointer',
                        opacity: loading ? 0.7 : 1
                    }}
                >
                    {loading ? 'Analyzing...' : 'Suggest Tags'}
                </button>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
                {overallTags.length > 0 && (
                    <div style={{
                        background: 'linear-gradient(145deg, #1e1e1e, #252525)',
                        border: '1px solid #444',
                        borderRadius: '8px',
                        padding: '1.5rem',
                        marginBottom: '1rem'
                    }}>
                        <h3 style={{ marginTop: 0, marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                            🌍 Overall Topics
                            <span style={{ fontSize: '0.8rem', fontWeight: 'normal', color: '#aaa' }}>(Weighted Consensus)</span>
                        </h3>
                        <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.8rem' }}>
                            {overallTags.map((tag, index) => (
                                <span
                                    key={index}
                                    style={{
                                        display: 'flex',
                                        alignItems: 'center',
                                        gap: '0.5rem',
                                        padding: '0.5rem 1rem',
                                        background: '#333',
                                        borderRadius: '6px',
                                        fontSize: '1rem',
                                        border: '1px solid #555',
                                        boxShadow: '0 2px 4px rgba(0,0,0,0.2)'
                                    }}
                                >
                                    <span style={{ fontWeight: 'bold', color: '#fff' }}>{tag.name}</span>
                                    <span style={{
                                        color: tag.score > 2.0 ? '#4caf50' : tag.score > 1.0 ? '#ff9800' : '#f44336',
                                        fontSize: '0.85rem',
                                        fontWeight: 'bold'
                                    }}>
                                        {tag.score.toFixed(2)}
                                    </span>
                                </span>
                            ))}
                        </div>
                    </div>
                )}

                {suggestions.map((region, index) => (
                    <div
                        key={index}
                        style={{
                            background: '#1a1a1a',
                            border: '1px solid #333',
                            borderRadius: '8px',
                            padding: '1.5rem',
                            borderLeft: hoveredRegionIndex === index ? '4px solid #646cff' : '4px solid transparent',
                            transition: 'all 0.2s'
                        }}
                    >
                        {/* Region Content */}
                        <div style={{
                            marginBottom: '1rem',
                            padding: '1rem',
                            background: hoveredRegionIndex === index ? 'rgba(100, 108, 255, 0.1)' : '#252525',
                            borderRadius: '4px',
                            fontStyle: 'italic',
                            transition: 'background 0.2s'
                        }}>
                            "{region.regionContent}"
                        </div>

                        {/* Tag Scores */}
                        <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem', marginBottom: '1rem' }}>
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
                                    <span style={{
                                        color: tag.score > 0.8 ? '#4caf50' : tag.score > 0.5 ? '#ff9800' : '#f44336',
                                        fontSize: '0.8rem'
                                    }}>
                                        {Math.round(tag.score * 100)}%
                                    </span>
                                </span>
                            ))}
                        </div>

                        {/* Taxonomy Paths */}
                        <div style={{ fontSize: '0.9rem', color: '#aaa' }}>
                            <div style={{ marginBottom: '0.5rem', textTransform: 'uppercase', fontSize: '0.75rem', letterSpacing: '1px' }}>Taxonomy Paths</div>
                            {(!region.taxonomyPaths || region.taxonomyPaths.length === 0) ? (
                                <div>No robust paths found.</div>
                            ) : (
                                region.taxonomyPaths.map((pathObj, pIndex) => (
                                    <div key={pIndex} style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.25rem' }}>
                                        {pathObj.path.map((node, nIndex) => (
                                            <span key={nIndex} style={{ display: 'flex', alignItems: 'center' }}>
                                                <span style={{ color: nIndex === pathObj.path.length - 1 ? '#fff' : '#888' }}>
                                                    {node.name}
                                                </span>
                                                {nIndex < pathObj.path.length - 1 && (
                                                    <span style={{ margin: '0 0.5rem', color: '#444' }}>›</span>
                                                )}
                                            </span>
                                        ))}
                                    </div>
                                ))
                            )}
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
}
