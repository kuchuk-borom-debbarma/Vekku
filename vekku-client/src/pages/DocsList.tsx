import { useEffect, useState, useCallback } from 'react';
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

interface ContentPageResponse {
    content: any[]; // Using any[] temporarily, then mapping to DocSummary
    nextCursor: string | null;
}

export default function DocsList() {
    const [docs, setDocs] = useState<DocSummary[]>([]);
    const [loading, setLoading] = useState(false);
    const [nextCursor, setNextCursor] = useState<string | null>(null);
    const navigate = useNavigate();

    const [cursorStack, setCursorStack] = useState<(string | null)[]>([null]);
    const [currentIndex, setCurrentIndex] = useState(0);

    const fetchDocs = useCallback(async (cursor: string | null) => {
        setLoading(true);
        try {
            const token = localStorage.getItem('accessToken');
            const baseUrl = 'http://localhost:8080/api/content';
            const url = new URL(baseUrl);

            if (cursor) {
                url.searchParams.append('cursor', cursor);
            }
            url.searchParams.append('limit', '20');

            const res = await fetch(url.toString(), {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            if (!res.ok) throw new Error('Failed to fetch documents');
            const data: ContentPageResponse = await res.json();

            const mappedDocs = data.content.map((d: any) => ({
                ...d,
                content: d.text || d.content
            }));

            setDocs(mappedDocs);
            setNextCursor(data.nextCursor);

        } catch (error) {
            console.error(error);
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        // Initial load
        fetchDocs(null);
    }, [fetchDocs]);

    const handleNext = () => {
        if (nextCursor) {
            const newIndex = currentIndex + 1;
            setCursorStack(prev => {
                const newStack = prev.slice(0, newIndex);
                newStack.push(nextCursor);
                return newStack;
            });
            setCurrentIndex(newIndex);
            fetchDocs(nextCursor);
        }
    };

    const handlePrev = () => {
        if (currentIndex > 0) {
            const newIndex = currentIndex - 1;
            setCurrentIndex(newIndex);
            fetchDocs(cursorStack[newIndex]);
        }
    };

    return (
        <Layout>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
                <h1 style={{ fontSize: '2rem' }}>My Documents</h1>
                <Button onClick={() => navigate('/create')}>New Document</Button>
            </div>

            {loading && docs.length === 0 ? (
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
                    <Button onClick={() => navigate('/create')}>Create your first doc</Button>
                </div>
            ) : (
                <>
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
                                    {(doc.content || '').slice(0, 150)}...
                                </div>

                                <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
                                    {(doc.tags || []).slice(0, 3).map(tag => (
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
                                    {doc.tags && doc.tags.length > 3 && (
                                        <span style={{ fontSize: '0.75rem', color: 'var(--color-text-tertiary)', alignSelf: 'center' }}>
                                            +{doc.tags.length - 3}
                                        </span>
                                    )}
                                </div>
                            </Card>
                        ))}
                    </div>

                    <div style={{ display: 'flex', justifyContent: 'center', gap: '1rem', marginTop: '2rem' }}>
                        <Button
                            onClick={handlePrev}
                            disabled={loading || currentIndex === 0}
                            variant="secondary"
                        >
                            Previous
                        </Button>
                        <Button
                            onClick={handleNext}
                            disabled={loading || !nextCursor}
                            variant="secondary"
                        >
                            Next
                        </Button>
                    </div>
                </>
            )}
        </Layout>
    );
}
