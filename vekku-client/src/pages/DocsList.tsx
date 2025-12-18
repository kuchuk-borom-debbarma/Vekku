import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';

interface DocSummary {
    id: string;
    content: string;
    type: 'TEXT' | 'MARKDOWN';
    tags: {
        tagId: string;
        score: number;
    }[];
}

export default function DocsList() {
    const [docs, setDocs] = useState<DocSummary[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetch('http://localhost:8080/api/docs')
            .then(res => res.json())
            .then(data => {
                setDocs(data);
                setLoading(false);
            })
            .catch(err => {
                console.error(err);
                setLoading(false);
            });
    }, []);

    if (loading) return <div style={{ textAlign: 'center', marginTop: '2rem' }}>Loading docs...</div>;

    return (
        <div style={{ maxWidth: '800px', margin: '0 auto' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
                <h1>My Documents</h1>
                <Link to="/create" style={{
                    padding: '0.6rem 1.2rem',
                    backgroundColor: 'var(--color-primary)',
                    color: 'white',
                    textDecoration: 'none',
                    borderRadius: '6px',
                    fontWeight: 600
                }}>+ New Doc</Link>
            </div>

            {docs.length === 0 ? (
                <div style={{ textAlign: 'center', opacity: 0.6 }}>No documents found. Create one!</div>
            ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                    {docs.map(doc => (
                        <Link key={doc.id} to={`/docs/${doc.id}`} style={{
                            textDecoration: 'none',
                            color: 'inherit',
                            display: 'block'
                        }}>
                            <div style={{
                                padding: '1.5rem',
                                backgroundColor: 'var(--color-surface)',
                                borderRadius: '8px',
                                border: '1px solid transparent',
                                transition: 'border-color 0.2s'
                            }}
                                onMouseEnter={(e) => e.currentTarget.style.borderColor = 'var(--color-primary)'}
                                onMouseLeave={(e) => e.currentTarget.style.borderColor = 'transparent'}
                            >
                                <div style={{
                                    marginBottom: '0.5rem',
                                    fontWeight: 500,
                                    whiteSpace: 'nowrap',
                                    overflow: 'hidden',
                                    textOverflow: 'ellipsis'
                                }}>
                                    {doc.content.slice(0, 100).replace(/\n/g, ' ')}
                                </div>
                                <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
                                    {doc.tags.slice(0, 5).map(tag => (
                                        <span key={tag.tagId} style={{
                                            fontSize: '0.8rem',
                                            padding: '0.2rem 0.6rem',
                                            backgroundColor: 'rgba(255,255,255,0.1)',
                                            borderRadius: '12px'
                                        }}>
                                            #{tag.tagId}
                                        </span>
                                    ))}
                                    {doc.tags.length > 5 && <span style={{ fontSize: '0.8rem', opacity: 0.7 }}>+{doc.tags.length - 5} more</span>}
                                </div>
                            </div>
                        </Link>
                    ))}
                </div>
            )}
        </div>
    );
}
