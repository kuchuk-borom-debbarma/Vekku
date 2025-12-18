import { useEffect, useState } from 'react';
import ReactMarkdown from 'react-markdown';
import { useParams, Link } from 'react-router-dom';

interface Doc {
    id: string;
    content: string;
    type: 'TEXT' | 'MARKDOWN';
    userId: string;
    tags: {
        tagId: string;
        score: number;
    }[];
}

export default function ViewDoc() {
    const { id } = useParams();
    const [doc, setDoc] = useState<Doc | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
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

    if (loading) return <div>Loading...</div>;
    if (!doc) return <div>Doc not found</div>;

    return (
        <div style={{ maxWidth: '800px', margin: '0 auto' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
                <h1>Document View</h1>
                <Link to="/create">Create New</Link>
            </div>

            <div style={{
                padding: '2rem',
                backgroundColor: 'var(--color-surface)',
                borderRadius: '12px',
                marginBottom: '2rem'
            }}>
                {doc.type === 'MARKDOWN' ? (
                    <ReactMarkdown>{doc.content}</ReactMarkdown>
                ) : (
                    <p style={{ whiteSpace: 'pre-wrap' }}>{doc.content}</p>
                )}
            </div>

            <h3>AI Generated Tags</h3>
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.8rem' }}>
                {doc.tags.map((tag, idx) => (
                    <div key={idx} style={{
                        padding: '0.5rem 1rem',
                        backgroundColor: 'rgba(var(--color-primary-h), 90%, 65%, 0.1)',
                        border: '1px solid var(--color-primary)',
                        borderRadius: '20px',
                        fontSize: '0.9rem',
                        display: 'flex',
                        alignItems: 'center',
                        gap: '0.5rem'
                    }}>
                        <span style={{ fontWeight: 600 }}>{tag.tagId}</span>
                        <span style={{ opacity: 0.7, fontSize: '0.8em' }}>
                            {(tag.score * 100).toFixed(0)}%
                        </span>
                    </div>
                ))}
                {doc.tags.length === 0 && <span style={{ opacity: 0.5 }}>No tags generated.</span>}
            </div>
        </div>
    );
}
