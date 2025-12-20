import { useEffect, useState, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import ReactMarkdown from 'react-markdown';
import { Layout } from '../components/ui/Layout';
import { Button } from '../components/ui/Button';
import { Card } from '../components/ui/Card';
import { Select, Loader, Badge, ActionIcon, Tooltip } from '@mantine/core';
import { notifications } from '@mantine/notifications';
import { IconRefresh, IconPlus, IconX, IconSparkles } from '@tabler/icons-react';
import { api } from '../api/api';
import { useDisclosure } from '@mantine/hooks';
import { TeachTagModal } from '../components/TeachTagModal';

interface Tag {
    id: string;
    name: string;
}

interface ContentTag {
    id: string;
    tag: Tag;
}

interface ContentTagSuggestion {
    id: string;
    tag: Tag;
    score: number;
}

interface ContentDetailDto {
    content: {
        id: string;
        text: string;
        type: 'TEXT' | 'MARKDOWN';
        created: string;
    };
    manualTags: ContentTag[];
    suggestedTags: ContentTagSuggestion[];
}

export default function ViewDoc() {
    const { id } = useParams();
    const [data, setData] = useState<ContentDetailDto | null>(null);
    const [loading, setLoading] = useState(true);
    const [refreshing, setRefreshing] = useState(false);
    const [availableTags, setAvailableTags] = useState<Tag[]>([]);
    const [selectedTagToAdd, setSelectedTagToAdd] = useState<string | null>(null);

    // Keywords state
    const [keywords, setKeywords] = useState<{ keyword: string, score: number }[]>([]);
    const [keywordsLoading, setKeywordsLoading] = useState(false);
    const [modalOpened, { open: openModal, close: closeModal }] = useDisclosure(false);
    const [keywordToTeach, setKeywordToTeach] = useState<string>('');

    const navigate = useNavigate();

    const fetchContent = useCallback(async () => {
        if (!id) return;
        setLoading(true);
        try {
            const token = localStorage.getItem('accessToken');
            const res = await fetch(`http://localhost:8080/api/content/${id}`, {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            if (!res.ok) throw new Error('Failed to fetch content');
            const json = await res.json();
            setData(json);
        } catch (err) {
            console.error(err);
        } finally {
            setLoading(false);
        }
    }, [id]);

    const fetchTags = useCallback(async () => {
        try {
            const token = localStorage.getItem('accessToken');
            const res = await fetch(`http://localhost:8080/api/tags?limit=100`, {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            const json = await res.json();
            setAvailableTags(json.tags || []);
        } catch (err) {
            console.error(err);
        }
    }, []);

    const fetchKeywords = useCallback(async () => {
        if (!id) return;
        setKeywordsLoading(true);
        try {
            const results = await api.getContentKeywords(id);
            // Transform to simple structure for display
            setKeywords(results.map(k => ({ keyword: k.keyword, score: k.score })));
        } catch (err) {
            console.error(err);
        } finally {
            setKeywordsLoading(false);
        }
    }, [id]);

    useEffect(() => {
        fetchContent();
        fetchTags();
        fetchKeywords();
    }, [fetchContent, fetchTags, fetchKeywords]);

    const handleRefreshSuggestions = async () => {
        if (!id) return;
        setRefreshing(true);
        try {
            const token = localStorage.getItem('accessToken');
            await fetch(`http://localhost:8080/api/content/${id}/suggestions/tags/refresh`, {
                method: 'POST',
                headers: { 'Authorization': `Bearer ${token}` }
            });

            // Show notification/feedback
            // Using a simple alert for now, effectively "Toasting"
            // Ideally use Mantine notifications system

            setTimeout(() => {
                fetchContent();
                setRefreshing(false);
            }, 2000);
        } catch (err) {
            console.error(err);
            setRefreshing(false);
        }
    };

    const handleRefreshKeywords = async () => {
        if (!id) return;
        setRefreshing(true);
        try {
            const token = localStorage.getItem('accessToken');
            await fetch(`http://localhost:8080/api/content/${id}/suggestions/keywords/refresh`, {
                method: 'POST',
                headers: { 'Authorization': `Bearer ${token}` }
            });
            setTimeout(() => {
                fetchKeywords();
                setRefreshing(false);
            }, 2000);
        } catch (err) {
            console.error(err);
            setRefreshing(false);
        }
    };

    const handleAddTag = async () => {
        if (!id || !selectedTagToAdd) return;
        try {
            const token = localStorage.getItem('accessToken');
            // Using the batch endpoint for single add
            await fetch(`http://localhost:8080/api/content/tags`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify({
                    contentId: id,
                    toAddTags: [selectedTagToAdd],
                    toRemoveTags: []
                })
            });
            setSelectedTagToAdd(null);
            fetchContent();
        } catch (err) {
            console.error(err);
        }
    };

    const handleRemoveTag = async (tagId: string) => {
        if (!id) return;
        try {
            const token = localStorage.getItem('accessToken');
            await fetch(`http://localhost:8080/api/content/tags`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify({
                    contentId: id,
                    toAddTags: [],
                    toRemoveTags: [tagId] // Note: Backend expects Tag IDs here, not ContentTag IDs
                })
            });
            fetchContent();
        } catch (err) {
            console.error(err);
        }
    };

    if (loading && !data) return (
        <Layout>
            <div style={{ textAlign: 'center', marginTop: '4rem', color: 'var(--color-text-secondary)' }}>
                <Loader color="violet" />
            </div>
        </Layout>
    );

    if (!data) return (
        <Layout>
            <div style={{ textAlign: 'center', marginTop: '4rem' }}>
                <h2>Document Not Found</h2>
                <Button onClick={() => navigate('/docs')}>Back to List</Button>
            </div>
        </Layout>
    );

    return (
        <Layout>
            <div style={{ marginBottom: '1.5rem' }}>
                <Button variant="ghost" onClick={() => navigate('/docs')} style={{ paddingLeft: 0 }}>
                    ← Back to List
                </Button>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 350px', gap: '2rem', alignItems: 'start' }}>
                <Card style={{ padding: '0', overflow: 'hidden' }}>
                    <div style={{
                        padding: '0.8rem 1.2rem',
                        borderBottom: '1px solid var(--color-border)',
                        backgroundColor: 'rgba(255,255,255,0.02)',
                        display: 'flex',
                        justifyContent: 'space-between'
                    }}>
                        <span style={{ fontWeight: 600, color: 'var(--color-text-secondary)' }}>DOCUMENT CONTENT</span>
                        <span style={{ fontSize: '0.8rem', color: 'var(--color-text-tertiary)' }}>{data.content.type}</span>
                    </div>
                    <div style={{ padding: '2rem' }}>
                        {data.content.type === 'MARKDOWN' ? (
                            <ReactMarkdown>{data.content.text}</ReactMarkdown>
                        ) : (
                            <p style={{ whiteSpace: 'pre-wrap', lineHeight: 1.6 }}>{data.content.text}</p>
                        )}
                    </div>
                </Card>

                <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
                    {/* Manual Tags */}
                    <Card>
                        <h3 style={{ fontSize: '1.1rem', marginBottom: '1rem' }}>Tags</h3>
                        <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem', marginBottom: '1rem' }}>
                            {data.manualTags.map(ct => (
                                <Badge
                                    key={ct.id}
                                    variant="filled"
                                    color="violet"
                                    rightSection={
                                        <ActionIcon size="xs" color="violet" radius="xl" variant="transparent" onClick={() => handleRemoveTag(ct.tag.id)}>
                                            <IconX size={10} />
                                        </ActionIcon>
                                    }
                                >
                                    {ct.tag.name}
                                </Badge>
                            ))}
                            {data.manualTags.length === 0 && <span style={{ color: 'var(--color-text-tertiary)', fontSize: '0.9rem' }}>No tags assigned</span>}
                        </div>

                        <div style={{ display: 'flex', gap: '0.5rem' }}>
                            <Select
                                placeholder="Add a tag..."
                                data={availableTags.map(t => ({ value: t.id, label: t.name }))}
                                value={selectedTagToAdd}
                                onChange={setSelectedTagToAdd}
                                searchable
                                clearable
                                style={{ flex: 1 }}
                            />
                            <Button onClick={handleAddTag} disabled={!selectedTagToAdd}>
                                <IconPlus size={16} />
                            </Button>
                        </div>
                    </Card>

                    {/* Suggested Tags */}
                    <Card>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
                            <h3 style={{ fontSize: '1.1rem', margin: 0 }}>Suggested Tags</h3>
                            <Tooltip label="Refresh Suggestions">
                                <ActionIcon onClick={handleRefreshSuggestions} loading={refreshing} variant="subtle" color="gray">
                                    <IconRefresh size={18} />
                                </ActionIcon>
                            </Tooltip>
                        </div>

                        <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem' }}>
                            {data.suggestedTags.map(st => (
                                <Badge
                                    key={st.id}
                                    variant="outline"
                                    color="gray"
                                    style={{ cursor: 'pointer', paddingRight: 0 }}
                                    rightSection={
                                        <span style={{
                                            fontSize: '0.7em',
                                            opacity: 0.7,
                                            padding: '0 6px',
                                            borderLeft: '1px solid var(--color-border)'
                                        }}>
                                            {(st.score * 100).toFixed(0)}%
                                        </span>
                                    }
                                    onClick={() => {
                                        // Quick add from suggestion
                                        setSelectedTagToAdd(st.tag.id);
                                        // Maybe auto-add? Or set select value?
                                        // Using state to trigger add effectively would be better UX
                                        // For now, let's just populate the selector
                                    }}
                                >
                                    {st.tag.name}
                                </Badge>
                            ))}
                            {data.suggestedTags.length === 0 && <div style={{ color: 'var(--color-text-tertiary)', fontSize: '0.9rem' }}>No suggestions available</div>}
                        </div>
                    </Card>

                    {/* Suggested Keywords (KeyBERT) */}
                    <Card>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
                            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                                <IconSparkles size={16} color="var(--mantine-primary-color-filled)" />
                                <h3 style={{ fontSize: '1.1rem', margin: 0 }}>Suggested Keywords</h3>
                            </div>
                            <Tooltip label="Refresh Keywords">
                                <ActionIcon onClick={handleRefreshKeywords} loading={refreshing || keywordsLoading} variant="subtle" color="gray">
                                    <IconRefresh size={18} />
                                </ActionIcon>
                            </Tooltip>
                        </div>

                        <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem' }}>
                            {keywords.map(k => (
                                <Badge
                                    key={k.keyword}
                                    variant="outline"
                                    color="grape"
                                    style={{ cursor: 'pointer', paddingRight: 0 }}
                                    rightSection={
                                        <span style={{
                                            fontSize: '0.7em',
                                            opacity: 0.7,
                                            padding: '0 6px',
                                            borderLeft: '1px solid var(--color-border)'
                                        }}>
                                            {(k.score * 100).toFixed(0)}%
                                        </span>
                                    }
                                    onClick={() => {
                                        setKeywordToTeach(k.keyword);
                                        openModal();
                                    }}
                                >
                                    {k.keyword}
                                </Badge>
                            ))}
                            {keywords.length === 0 && <div style={{ color: 'var(--color-text-tertiary)', fontSize: '0.9rem' }}>No keywords extracted</div>}
                        </div>
                    </Card>
                </div>

                <TeachTagModal
                    opened={modalOpened}
                    onClose={() => {
                        closeModal();
                        // Refresh tags list after teaching new tag
                        fetchTags();
                    }}
                    initialData={{ id: '', name: keywordToTeach, synonyms: [] }}
                />
            </div>
        </Layout>
    );
}
