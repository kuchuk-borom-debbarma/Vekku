import { useEffect, useState, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import ReactMarkdown from 'react-markdown';
import { Layout } from '../components/ui/Layout';
import { Button } from '../components/ui/Button';
import { Card } from '../components/ui/Card';
import { Select, Loader, Badge, ActionIcon } from '@mantine/core';
import { notifications } from '@mantine/notifications';
import { IconPlus, IconX, IconSparkles } from '@tabler/icons-react';
import { api } from '../api/api';
import { useDisclosure } from '@mantine/hooks';
import { TeachTagModal } from '../components/TeachTagModal';
import { SuggestedTagsCard } from '../components/SuggestedTagsCard';
import { SuggestedKeywordsCard } from '../components/SuggestedKeywordsCard';

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
            const json = await api.fetchContent(id);
            setData(json);
        } catch (err) {
            console.error(err);
        } finally {
            setLoading(false);
        }
    }, [id]);

    const fetchTags = useCallback(async () => {
        try {
            const json = await api.fetchTags();
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
            await api.refreshTagSuggestions(id);

            notifications.show({
                title: 'Tags Refresh Queued',
                message: 'The brain is looking for new tags...',
                color: 'violet',
                icon: <IconSparkles size={16} />
            });

            setTimeout(() => {
                fetchContent();
                setRefreshing(false);
            }, 2000);
        } catch (err) {
            console.error(err);
            notifications.show({
                title: 'Error',
                message: 'Failed to refresh tags',
                color: 'red'
            });
            setRefreshing(false);
        }
    };

    const handleRefreshKeywords = async () => {
        if (!id) return;
        setRefreshing(true);
        try {
            await api.refreshKeywordSuggestions(id);

            notifications.show({
                title: 'Keywords Refresh Queued',
                message: 'Extracting keywords with KeyBERT...',
                color: 'grape',
                icon: <IconSparkles size={16} />
            });

            setTimeout(() => {
                fetchKeywords();
                setRefreshing(false);
            }, 2000);
        } catch (err) {
            console.error(err);
            notifications.show({
                title: 'Error',
                message: 'Failed to refresh keywords',
                color: 'red'
            });
            setRefreshing(false);
        }
    };

    const handleAddTag = async () => {
        if (!id || !selectedTagToAdd) return;
        try {
            await api.updateContentTags(id, [selectedTagToAdd], []);
            setSelectedTagToAdd(null);
            fetchContent();
        } catch (err) {
            console.error(err);
        }
    };

    const handleRemoveTag = async (tagId: string) => {
        if (!id) return;
        try {
            await api.updateContentTags(id, [], [tagId]);
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
                    <SuggestedTagsCard
                        suggestedTags={data.suggestedTags}
                        loading={refreshing}
                        onRefresh={handleRefreshSuggestions}
                        onAddTag={(tagId) => setSelectedTagToAdd(tagId)}
                    />

                    {/* Suggested Keywords (KeyBERT) */}
                    <SuggestedKeywordsCard
                        keywords={keywords}
                        loading={refreshing || keywordsLoading}
                        onRefresh={handleRefreshKeywords}
                        onTeach={(keyword) => {
                            setKeywordToTeach(keyword);
                            openModal();
                        }}
                    />
                </div>

                <TeachTagModal
                    opened={modalOpened}
                    onClose={() => {
                        closeModal();
                        fetchTags();
                    }}
                    initialData={{ id: '', name: keywordToTeach, synonyms: [] }}
                />
            </div>
        </Layout>
    );
}
