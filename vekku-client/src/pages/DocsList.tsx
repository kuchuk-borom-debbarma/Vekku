import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Layout } from '../components/ui/Layout';
import { Button } from '../components/ui/Button';
import { Card } from '../components/ui/Card';

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
    const navigate = useNavigate();

    useEffect(() => {
        const token = localStorage.getItem('accessToken');
        fetch('http://localhost:8080/api/docs', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        })
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

    return (
        <Layout>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
                <h1 style={{ fontSize: '2rem' }}>My Documents</h1>
                <Button onClick={() => navigate('/create')}>+ New Doc</Button>
            </div>

            {loading ? (
                <div style={{ textAlign: 'center', padding: '4rem', color: 'var(--color-text-secondary)' }}>
                    Loading documents...
                </div>
            ) : docs.length === 0 ? (
                <div style={{
                    textAlign: 'center',
                    padding: '4rem',
                    border: '2px dashed var(--color-border)',
                    borderRadius: 'var(--radius-md)',
                    color: 'var(--color-text-secondary)'
                }}>
                    <p style={{ marginBottom: '1rem' }}>No documents found yet.</p>
                    <Button variant="secondary" onClick={() => navigate('/create')}>Create your first doc</Button>
                </div>
            ) : (
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '1.5rem' }}>
                    {docs.map(doc => (
                        <Card
                            key={doc.id}
                            onClick={() => navigate(`/docs/${doc.id}`)}
                            style={{
                                cursor: 'pointer',
                                transition: 'transform 0.2s, border-color 0.2s',
                                height: '200px',
                                display: 'flex',
                                flexDirection: 'column'
                            }}
                            onMouseEnter={(e) => {
                                e.currentTarget.style.borderColor = 'var(--color-brand-primary)';
                                e.currentTarget.style.transform = 'translateY(-2px)';
                            }}
                            onMouseLeave={(e) => {
                                e.currentTarget.style.borderColor = 'var(--color-border)';
                                e.currentTarget.style.transform = 'translateY(0)';
                            }}
                        >
                            <div style={{
                                flex: 1,
                                marginBottom: '1rem',
                                overflow: 'hidden',
                                color: 'var(--color-text-secondary)',
                                fontSize: '0.9rem',
                                lineHeight: '1.5'
                            }}>
                                {doc.content.slice(0, 150)}...
                            </div>

                            <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
                                {doc.tags.slice(0, 3).map(tag => (
                                    <span key={tag.tagId} style={{
                                        fontSize: '0.75rem',
                                        padding: '0.2rem 0.6rem',
                                        backgroundColor: 'var(--color-bg-app)',
                                        borderRadius: '12px',
                                        color: 'var(--color-text-primary)',
                                        border: '1px solid var(--color-border)'
                                    }}>
                                        #{tag.tagId}
                                    </span>
                                ))}
                                {doc.tags.length > 3 && (
                                    <span style={{ fontSize: '0.75rem', color: 'var(--color-text-tertiary)', alignSelf: 'center' }}>
                                        +{doc.tags.length - 3}
                                    </span>
                                )}
                            </div>
                        </Card>
                    ))}
                </div>
            )}
        </Layout>
    );
}
