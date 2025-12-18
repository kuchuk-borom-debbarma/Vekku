import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import ReactMarkdown from 'react-markdown';
import { Layout } from '../components/ui/Layout';
import { Button } from '../components/ui/Button';
import { Card } from '../components/ui/Card';

interface DocData {
    id: string;
    content: string;
    type: 'TEXT' | 'MARKDOWN';
    tags: {
        tagId: string;
        score: number;
    }[];
}

export default function ViewDoc() {
    const { id } = useParams();
    const [doc, setDoc] = useState<DocData | null>(null);
    const [loading, setLoading] = useState(true);
    const navigate = useNavigate();

    useEffect(() => {
        if (!id) return;
        fetch(`http://localhost:8080/api/docs/${id}`)
            .then(res => res.json())
            .then(data => {
                setDoc(data);
                setLoading(false);
            })
            .catch(err => {
                console.error(err);
                setLoading(false);
            });
    }, [id]);

    if (loading) return (
        <Layout>
            <div style={{ textAlign: 'center', marginTop: '4rem', color: 'var(--color-text-secondary)' }}>
                Loading document...
            </div>
        </Layout>
    );

    if (!doc) return (
        <Layout>
            <div style={{ textAlign: 'center', marginTop: '4rem' }}>
                <h2 style={{ color: 'var(--color-text-primary)' }}>Document Not Found</h2>
                <Button onClick={() => navigate('/docs')} style={{ marginTop: '1rem' }}>Back to List</Button>
            </div>
        </Layout>
    );

    return (
        <Layout>
            <div style={{ marginBottom: '1.5rem', display: 'flex', alignItems: 'center', gap: '1rem' }}>
                <Button variant="ghost" onClick={() => navigate('/docs')} style={{ paddingLeft: 0 }}>
                    ← Back to List
                </Button>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'minmax(0, 1fr) 300px', gap: '2rem', alignItems: 'start' }}>
                {/* Content Area */}
                <Card style={{ padding: '0', overflow: 'hidden' }}>
                    <div style={{
                        padding: '0.8rem 1.2rem',
                        borderBottom: '1px solid var(--color-border)',
                        backgroundColor: 'rgba(255,255,255,0.02)',
                        display: 'flex',
                        justifyContent: 'space-between'
                    }}>
                        <span style={{ fontWeight: 600, fontSize: '0.9rem', color: 'var(--color-text-secondary)' }}>DOCUMENT CONTENT</span>
                        <span style={{ fontSize: '0.8rem', color: 'var(--color-text-tertiary)' }}>{doc.type}</span>
                    </div>
                    <div style={{ padding: '2rem' }}>
                        {doc.type === 'MARKDOWN' ? (
                            <ReactMarkdown>{doc.content}</ReactMarkdown>
                        ) : (
                            <p style={{ whiteSpace: 'pre-wrap', lineHeight: 1.6 }}>{doc.content}</p>
                        )}
                    </div>
                </Card>

                {/* Tags Sidebar */}
                <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem', position: 'sticky', top: '100px' }}>
                    <Card>
                        <h3 style={{ fontSize: '1.1rem', marginBottom: '1rem', color: 'var(--color-text-primary)' }}>AI Analysis</h3>
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.8rem' }}>
                            <div style={{ fontSize: '0.85rem', color: 'var(--color-text-secondary)', marginBottom: '0.5rem' }}>
                                Suggested Tags
                            </div>
                            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.6rem' }}>
                                {doc.tags.map(tag => (
                                    <div
                                        key={tag.tagId}
                                        style={{
                                            padding: '0.3rem 0.8rem',
                                            backgroundColor: 'rgba(var(--hue-brand), 200, 200, 0.1)',
                                            border: '1px solid var(--color-brand-primary)',
                                            borderRadius: '20px',
                                            fontSize: '0.85rem',
                                            color: 'var(--color-text-primary)',
                                            display: 'flex',
                                            alignItems: 'center',
                                            gap: '0.5rem'
                                        }}
                                        title={`Confidence: ${(tag.score * 100).toFixed(1)}%`}
                                    >
                                        #{tag.tagId}
                                        <span style={{
                                            fontSize: '0.7em',
                                            opacity: 0.7,
                                            backgroundColor: 'rgba(255,255,255,0.1)',
                                            padding: '1px 4px',
                                            borderRadius: '4px'
                                        }}>
                                            {(tag.score * 100).toFixed(0)}%
                                        </span>
                                    </div>
                                ))}
                            </div>
                        </div>
                    </Card>

                    <Card style={{ backgroundColor: 'transparent', border: '1px dashed var(--color-border)', textAlign: 'center', padding: '1.5rem' }}>
                        <p style={{ color: 'var(--color-text-tertiary)', fontSize: '0.9rem' }}>
                            More AI features like <br /> summarization coming soon.
                        </p>
                    </Card>
                </div>
            </div>
        </Layout>
    );
}
