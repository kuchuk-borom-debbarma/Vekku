import { useState, useEffect, useCallback } from 'react';
import { useSearchParams } from 'react-router-dom';
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
}

interface TagListResponse {
    tags: Tag[];
    nextOffset?: string | number;
}

const LIMIT = 20;

export default function ManageTags() {
    const [searchParams, setSearchParams] = useSearchParams();
    const cursor = searchParams.get('cursor');

    const [tags, setTags] = useState<Tag[]>([]);
    const [loading, setLoading] = useState(false);
    const [nextCursor, setNextCursor] = useState<string | number | undefined>(undefined);

    const [modalOpened, { open: openModal, close: closeModal }] = useDisclosure(false);

    const fetchTags = useCallback(async (currentCursor?: string | null) => {
        setLoading(true);
        try {
            const token = localStorage.getItem('accessToken');
            const url = new URL('http://localhost:8080/api/tags');
            url.searchParams.append('limit', LIMIT.toString());
            if (currentCursor) {
                url.searchParams.append('offset', currentCursor);
            }

            const res = await fetch(url.toString(), {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            if (!res.ok) throw new Error('Failed to fetch tags');
            const data: TagListResponse = await res.json();
            setTags(data.tags);
            setNextCursor(data.nextOffset);

        } catch (error) {
            console.error(error);
            setTags([]);
        } finally {
            setLoading(false);
        }
    }, []);

    const [cursorHistory, setCursorHistory] = useState<(string | null)[]>([]);

    // Fetch when cursor URL param changes
    useEffect(() => {
        fetchTags(cursor);
    }, [cursor, fetchTags]);

    const handleNext = () => {
        if (nextCursor) {
            // Push current cursor (or null if currently on first page) to history
            setCursorHistory(prev => [...prev, cursor]);
            setSearchParams({ cursor: nextCursor.toString() });
        }
    };

    const handlePrev = () => {
        if (cursorHistory.length > 0) {
            const prevCursor = cursorHistory[cursorHistory.length - 1];
            const newHistory = cursorHistory.slice(0, -1);
            setCursorHistory(newHistory);

            if (prevCursor) {
                setSearchParams({ cursor: prevCursor });
            } else {
                setSearchParams({});
            }
        }
    };

    const handleStartOver = () => {
        setSearchParams({});
        setCursorHistory([]);
    };

    const handleDelete = async (tagName: string) => {
        if (!confirm(`Are you sure you want to delete tag "${tagName}"?`)) return;

        try {
            const token = localStorage.getItem('accessToken');
            const res = await fetch(`http://localhost:8080/api/tags/${tagName}`, {
                method: 'DELETE',
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            if (res.ok) {
                // Refresh current view
                fetchTags(cursor);
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
        fetchTags(cursor);
    }
    return (
        <Layout>
            <div style={{ padding: '2rem', maxWidth: '1000px', margin: '0 auto' }}>
                <Group justify="space-between" mb="xl">
                    <Title order={2} style={{ color: '#fff' }}>🏷️ Manage Logic Tags</Title>
                    <Button onClick={openModal} color="blue" size="md">
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
                                        <Table.Th style={{ color: '#aaa' }}>Tag Name</Table.Th>
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
                                            <Table.Td style={{ color: '#666', fontFamily: 'monospace', fontSize: '0.85rem' }}>
                                                {tag.id}
                                            </Table.Td>
                                            <Table.Td style={{ textAlign: 'right' }}>
                                                <Button
                                                    variant="subtle"
                                                    color="red"
                                                    size="xs"
                                                    onClick={() => handleDelete(tag.name)}
                                                >
                                                    Delete
                                                </Button>
                                            </Table.Td>
                                        </Table.Tr>
                                    ))}
                                </Table.Tbody>
                            </Table>

                            {tags.length === 0 && !loading && (
                                <Text c="dimmed" ta="center" py="xl">No tags found. Teach the brain some concepts!</Text>
                            )}

                            <Group justify="center" mt="xl" gap="md">
                                <Button variant="default" onClick={handleStartOver} disabled={!cursor || loading}>
                                    First Page
                                </Button>
                                <Button variant="default" onClick={handlePrev} disabled={cursorHistory.length === 0 || loading}>
                                    Previous Page
                                </Button>
                                <Button variant="default" onClick={handleNext} disabled={!nextCursor || loading}>
                                    Next Page
                                </Button>
                            </Group>
                        </>
                    )}
                </Paper>
            </div>

            <TeachTagModal opened={modalOpened} onClose={handleModalClose} />
        </Layout>
    );
}
