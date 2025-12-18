import { useState } from 'react';
import ReactMarkdown from 'react-markdown';
import { useNavigate } from 'react-router-dom';

export default function CreateContent() {
    const [content, setContent] = useState('');
    const [mode, setMode] = useState<'TEXT' | 'MARKDOWN'>('MARKDOWN');
    const [status, setStatus] = useState<'idle' | 'saving' | 'error'>('idle');
    const [savedId, setSavedId] = useState<string | null>(null);
    const navigate = useNavigate();

    const handleSave = async () => {
        setStatus('saving');
        try {
            const res = await fetch('http://localhost:8080/api/docs', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ content, type: mode }),
            });
            if (!res.ok) throw new Error('Failed to save');
            const data = await res.json();
            setSavedId(data.id);
            setStatus('idle');
            // Optional: Navigate to view page
            navigate(`/docs/${data.id}`);
        } catch (e) {
            console.error(e);
            setStatus('error');
        }
    };

    return (
        <div style={{ maxWidth: '1000px', margin: '0 auto' }}>
            <h1>Create Content</h1>

            <div style={{ marginBottom: '1rem' }}>
                <button
                    onClick={() => setMode('TEXT')}
                    style={{ opacity: mode === 'TEXT' ? 1 : 0.5, marginRight: '1rem' }}
                >
                    Text Mode
                </button>
                <button
                    onClick={() => setMode('MARKDOWN')}
                    style={{ opacity: mode === 'MARKDOWN' ? 1 : 0.5 }}
                >
                    Markdown Mode
                </button>
            </div>

            <div style={{ display: 'flex', gap: '2rem', height: '600px' }}>
                {/* Editor */}
                <div style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
                    <label style={{ marginBottom: '0.5rem' }}>Input</label>
                    <textarea
                        value={content}
                        onChange={(e) => setContent(e.target.value)}
                        style={{
                            flex: 1,
                            padding: '1rem',
                            borderRadius: '8px',
                            border: '1px solid #333',
                            backgroundColor: 'rgba(0,0,0,0.2)',
                            color: 'white',
                            fontFamily: 'monospace',
                            fontSize: '14px',
                            resize: 'none'
                        }}
                        placeholder="Type your content here..."
                    />
                </div>

                {/* Preview (Only for Markdown) */}
                {mode === 'MARKDOWN' && (
                    <div style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
                        <label style={{ marginBottom: '0.5rem' }}>Preview</label>
                        <div style={{
                            flex: 1,
                            padding: '1rem',
                            borderRadius: '8px',
                            border: '1px solid #333',
                            backgroundColor: 'rgba(255,255,255,0.05)',
                            overflowY: 'auto'
                        }}>
                            <ReactMarkdown>{content}</ReactMarkdown>
                        </div>
                    </div>
                )}
            </div>

            <div style={{ marginTop: '1rem', display: 'flex', justifyContent: 'flex-end', gap: '1rem' }}>
                {status === 'error' && <span style={{ color: 'red' }}>Error saving!</span>}
                <button onClick={handleSave} disabled={status === 'saving' || !content}>
                    {status === 'saving' ? 'Saving...' : 'Save Content'}
                </button>
            </div>
        </div>
    );
}
