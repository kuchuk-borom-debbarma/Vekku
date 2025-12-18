import { useState, useEffect, useCallback } from 'react';
// import { useSearchParams } from 'react-router-dom';
import { Layout } from '../components/ui/Layout';
import { Table, Button, Group, Title, Text, Loader, Badge, Paper } from '@mantine/core';
import { useDisclosure } from '@mantine/hooks';
import { TeachTagModal } from '../components/TeachTagModal';

/* Assuming we can use icons from tabler-icons-react or similar if available, 
   otherwise I will use text for now or simple unicode */
// import { IconTrash, IconPlus } from '@tabler/icons-react';

interface Tag {
    id: string;
    name: string;
    synonyms: string[];
}

// Pagination temporarily disabled
// const LIMIT = 20;

export default function ManageTags() {
    interface TagPageResponse {
        tags: Tag[];
        nextCursor: string | null;
    }

    const [tags, setTags] = useState<Tag[]>([]);
    const [loading, setLoading] = useState(false);
    const [nextCursor, setNextCursor] = useState<string | null>(null);

    const [editingTag, setEditingTag] = useState<Tag | null>(null);
    const [modalOpened, { open: openModal, close: closeModal }] = useDisclosure(false);

    const openCreateModal = () => {
        setEditingTag(null);
        openModal();
    };

    const openEditModal = (tag: Tag) => {
        setEditingTag(tag);
        openModal();
    };

    const fetchTags = useCallback(async (reset = false) => {
        setLoading(true);
        try {
            const token = localStorage.getItem('accessToken');
            const baseUrl = 'http://localhost:8080/api/tags';
            const url = new URL(baseUrl);

            // Use current nextCursor unless resetting
            const cursorToUse = reset ? null : nextCursor;
            if (cursorToUse) {
                url.searchParams.append('cursor', cursorToUse);
            }
            url.searchParams.append('limit', '20');

            const res = await fetch(url.toString(), {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            if (!res.ok) throw new Error('Failed to fetch tags');
            const data: TagPageResponse = await res.json();

            if (reset) {
                setTags(data.tags);
            } else {
                setTags(prev => [...prev, ...data.tags]);
            }
            setNextCursor(data.nextCursor);

        } catch (error) {
            console.error(error);
            if (reset) setTags([]);
        } finally {
            setLoading(false);
        }
    }, [nextCursor]);

    // Initial load
    useEffect(() => {
        // Only run once on mount? Or when? 
        // We need a way to trigger initial load.
        // Let's rely on a separate effect or just call it here with reset=true if empty?
        // Actually, to avoid infinite loops with `nextCursor` dependency, we should separate "load more" from "refresh".
    }, []);

    // Better pattern:
    useEffect(() => {
        fetchTags(true);
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    const handleLoadMore = () => {
        if (nextCursor) {
            fetchTags(false);
        }
    };

    const handleDelete = async (tagId: string, tagName: string) => {
        if (!confirm(`Are you sure you want to delete tag "${tagName}"?`)) return;

        try {
            const token = localStorage.getItem('accessToken');
            const res = await fetch(`http://localhost:8080/api/tags/${tagId}`, {
                method: 'DELETE',
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            if (res.ok) {
                // Refresh list from scratch
                fetchTags(true);
            } else {
                alert('Failed to delete tag');
            }
        } catch (error) {
            console.error(error);
            alert('Error deleting tag');
        }
    };

    const handleModalClose = () => {
        closeModal();
        // Refresh list
        fetchTags(true);
    }
    return (
        <Layout>
            <div style={{ padding: '2rem', maxWidth: '1000px', margin: '0 auto' }}>
                <Group justify="space-between" mb="xl">
                    <Title order={2} style={{ color: '#fff' }}>🏷️ Manage Logic Tags</Title>
                    <Button onClick={openCreateModal} color="blue" size="md">
                        + Add New Tag
                    </Button>
                </Group>

                <Paper p="md" radius="md" style={{ background: '#1a1a1a', border: '1px solid #333' }}>
                    {loading && tags.length === 0 ? (
                        <Group justify="center" p="xl">
                            <Loader size="lg" />
                        </Group>
                    ) : (
                        <>
                            <Table verticalSpacing="sm">
                                <Table.Thead>
                                    <Table.Tr>
                                        <Table.Th style={{ color: '#aaa' }}>Tag Name (Alias)</Table.Th>
                                        <Table.Th style={{ color: '#aaa' }}>Synonyms</Table.Th>
                                        <Table.Th style={{ color: '#aaa' }}>ID</Table.Th>
                                        <Table.Th style={{ textAlign: 'right', color: '#aaa' }}>Actions</Table.Th>
                                    </Table.Tr>
                                </Table.Thead>
                                <Table.Tbody>
                                    {tags.map((tag) => (
                                        <Table.Tr key={tag.id}>
                                            <Table.Td>
                                                <Badge size="lg" variant="gradient" gradient={{ from: 'indigo', to: 'cyan' }}>
                                                    {tag.name}
                                                </Badge>
                                            </Table.Td>
                                            <Table.Td>
                                                <Group gap="xs">
                                                    {tag.synonyms?.map(s => (
                                                        <Badge key={s} size="sm" variant="outline" color="gray">{s}</Badge>
                                                    ))}
                                                </Group>
                                            </Table.Td>
                                            <Table.Td style={{ color: '#666', fontFamily: 'monospace', fontSize: '0.85rem' }}>
                                                {tag.id}
                                            </Table.Td>
                                            <Table.Td style={{ textAlign: 'right' }}>
                                                <Group gap="xs" justify="flex-end">
                                                    <Button variant="light" size="xs" onClick={() => openEditModal(tag)}>
                                                        Edit
                                                    </Button>
                                                    <Button
                                                        variant="subtle"
                                                        color="red"
                                                        size="xs"
                                                        onClick={() => handleDelete(tag.id, tag.name)}
                                                    >
                                                        Delete
                                                    </Button>
                                                </Group>
                                            </Table.Td>
                                        </Table.Tr>
                                    ))}
                                </Table.Tbody>
                            </Table>

                            {tags.length === 0 && !loading && (
                                <Text c="dimmed" ta="center" py="xl">No tags found. Teach the brain some concepts!</Text>
                            )}

                            <Text c="dimmed" size="xs" ta="center" mt="md">
                                Showing {tags.length} tags
                            </Text>

                            {nextCursor && (
                                <Group justify="center" mt="md">
                                    <Button onClick={handleLoadMore} loading={loading} variant="subtle">
                                        Load More
                                    </Button>
                                </Group>
                            )}
                        </>
                    )}
                </Paper>
            </div>

            <TeachTagModal
                opened={modalOpened}
                onClose={handleModalClose}
                initialData={editingTag}
            />
        </Layout>
    );
}
