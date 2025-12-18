import { useState } from 'react';
import ReactMarkdown from 'react-markdown';
import { useNavigate } from 'react-router-dom';
import { Layout } from '../components/ui/Layout';
import { Button } from '../components/ui/Button';
import { Card } from '../components/ui/Card';

export default function CreateContent() {
    const [content, setContent] = useState('');
    const [mode, setMode] = useState<'TEXT' | 'MARKDOWN'>('MARKDOWN');
    const [saving, setSaving] = useState(false);
    const navigate = useNavigate();

    const handleSave = async () => {
        if (!content.trim()) return;
        setSaving(true);
        try {
            await fetch('http://localhost:8080/api/docs', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
                },
                body: JSON.stringify({ content, type: mode })
            });
            // Go to docs list or view the new doc? Let's go to list for now.
            navigate('/docs');
        } catch (error) {
            console.error(error);
            alert('Failed to save document. Check console.');
        } finally {
            setSaving(false);
        }
    };

    return (
        <Layout>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
                <h1 style={{ fontSize: '1.8rem' }}>Create New Content</h1>
                <div style={{ display: 'flex', gap: '1rem' }}>
                    <Button
                        variant="secondary"
                        onClick={() => navigate('/')}
                    >
                        Cancel
                    </Button>
                    <Button
                        disabled={saving || !content.trim()}
                        onClick={handleSave}
                    >
                        {saving ? 'Saving...' : 'Save Content'}
                    </Button>
                </div>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '2rem', height: 'calc(100vh - 200px)' }}>
                {/* Editor Panel */}
                <Card style={{ display: 'flex', flexDirection: 'column', height: '100%', padding: '0', overflow: 'hidden' }}>
                    <div style={{
                        padding: '0.8rem 1.2rem',
                        borderBottom: '1px solid var(--color-border)',
                        backgroundColor: 'rgba(255,255,255,0.02)',
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center'
                    }}>
                        <span style={{ fontWeight: 600, fontSize: '0.9rem', color: 'var(--color-text-secondary)' }}>EDITOR</span>
                        <div style={{ display: 'flex', gap: '0.5rem' }}>
                            <button
                                onClick={() => setMode('TEXT')}
                                style={{
                                    opacity: mode === 'TEXT' ? 1 : 0.5,
                                    fontWeight: mode === 'TEXT' ? 'bold' : 'normal',
                                    color: mode === 'TEXT' ? 'var(--color-brand-primary)' : 'inherit',
                                    borderBottom: mode === 'TEXT' ? '2px solid var(--color-brand-primary)' : 'none',
                                    paddingBottom: '2px',
                                    fontSize: '0.8rem'
                                }}
                            >
                                Plain Text
                            </button>
                            <button
                                onClick={() => setMode('MARKDOWN')}
                                style={{
                                    opacity: mode === 'MARKDOWN' ? 1 : 0.5,
                                    fontWeight: mode === 'MARKDOWN' ? 'bold' : 'normal',
                                    color: mode === 'MARKDOWN' ? 'var(--color-brand-primary)' : 'inherit',
                                    borderBottom: mode === 'MARKDOWN' ? '2px solid var(--color-brand-primary)' : 'none',
                                    paddingBottom: '2px',
                                    fontSize: '0.8rem'
                                }}
                            >
                                Markdown
                            </button>
                        </div>
                    </div>
                    <textarea
                        value={content}
                        onChange={(e) => setContent(e.target.value)}
                        placeholder="Start typing your content here..."
                        style={{
                            flex: 1,
                            width: '100%',
                            backgroundColor: 'transparent',
                            color: 'var(--color-text-primary)',
                            border: 'none',
                            resize: 'none',
                            padding: '1.2rem',
                            fontSize: '1rem',
                            fontFamily: 'var(--font-mono)',
                            outline: 'none',
                            lineHeight: '1.6'
                        }}
                    />
                </Card>

                {/* Preview Panel */}
                <Card style={{ display: 'flex', flexDirection: 'column', height: '100%', padding: '0', overflow: 'hidden' }}>
                    <div style={{
                        padding: '0.8rem 1.2rem',
                        borderBottom: '1px solid var(--color-border)',
                        backgroundColor: 'rgba(255,255,255,0.02)',
                    }}>
                        <span style={{ fontWeight: 600, fontSize: '0.9rem', color: 'var(--color-text-secondary)' }}>PREVIEW</span>
                    </div>
                    <div style={{
                        flex: 1,
                        overflowY: 'auto',
                        padding: '1.5rem',
                        backgroundColor: mode === 'MARKDOWN' ? 'var(--color-bg-app)' : 'transparent'
                    }}>
                        {mode === 'MARKDOWN' ? (
                            <div className="markdown-body" style={{ color: 'var(--color-text-primary)' }}>
                                <ReactMarkdown>{content || '*Preview will appear here*'}</ReactMarkdown>
                            </div>
                        ) : (
                            <div style={{ whiteSpace: 'pre-wrap', color: 'var(--color-text-primary)' }}>
                                {content || 'Preview will appear here'}
                            </div>
                        )}
                    </div>
                </Card>
            </div>
        </Layout>
    );
}
