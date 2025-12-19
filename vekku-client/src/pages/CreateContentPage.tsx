import { useState } from 'react';
import { Layout } from '../components/ui/Layout';
import { Button, Paper, Group, Title, SegmentedControl, Textarea, Text } from '@mantine/core';
import Markdown from 'react-markdown';
import { api } from '../api/api';
import { useNavigate } from 'react-router-dom';

export default function CreateContentPage() {
    const navigate = useNavigate();
    const [content, setContent] = useState('');
    const [contentType, setContentType] = useState<'TEXT' | 'MARKDOWN'>('TEXT');
    const [loading, setLoading] = useState(false);

    const handleSave = async () => {
        if (!content.trim()) return;

        setLoading(true);
        try {
            await api.createContent({
                text: content,
                type: contentType
            });
            navigate('/docs'); // Redirect to docs list
        } catch (error) {
            console.error(error);
            alert('Failed to save content');
        } finally {
            setLoading(false);
        }
    };

    return (
        <Layout>
            <div style={{ padding: '2rem', maxWidth: '1200px', margin: '0 auto' }}>
                <Group justify="space-between" mb="xl">
                    <Title order={2} style={{ color: '#fff' }}>Create New Content</Title>
                    <Group>
                        <SegmentedControl
                            value={contentType}
                            onChange={(value) => setContentType(value as 'TEXT' | 'MARKDOWN')}
                            data={[
                                { label: 'Plain Text', value: 'TEXT' },
                                { label: 'Markdown', value: 'MARKDOWN' },
                            ]}
                        />
                        <Button onClick={handleSave} loading={loading} color="violet">
                            Save Content
                        </Button>
                    </Group>
                </Group>

                <div style={{ display: 'grid', gridTemplateColumns: contentType === 'MARKDOWN' ? '1fr 1fr' : '1fr', gap: '2rem', height: 'calc(100vh - 200px)' }}>
                    {/* Editor */}
                    <Paper shadow="sm" radius="md" p="md" style={{ display: 'flex', flexDirection: 'column' }}>
                        <Textarea
                            placeholder="Type your content here..."
                            value={content}
                            onChange={(event) => setContent(event.currentTarget.value)}
                            styles={{
                                wrapper: { height: '100%' },
                                input: { height: '100%', fontFamily: 'monospace' }
                            }}
                            resize="vertical"
                        />
                    </Paper>

                    {/* Preview (only for Markdown) */}
                    {contentType === 'MARKDOWN' && (
                        <Paper shadow="sm" radius="md" p="md" style={{ overflowY: 'auto', background: '#1A1B1E', color: '#C1C2C5' }}>
                            <Text size="sm" c="dimmed" mb="md" fw={500}>PREVIEW</Text>
                            <div className="markdown-preview">
                                <Markdown>{content}</Markdown>
                            </div>
                        </Paper>
                    )}
                </div>
            </div>
        </Layout>
    );
}
