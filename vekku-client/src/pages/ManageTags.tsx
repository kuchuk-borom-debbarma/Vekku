import { useState, useEffect, useCallback } from 'react';
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
    const [tags, setTags] = useState<Tag[]>([]);
    const [loading, setLoading] = useState(false);
    const [page, setPage] = useState(1);

    const [modalOpened, { open: openModal, close: closeModal }] = useDisclosure(false);

    const fetchTags = useCallback(async (currentOffset?: string | number) => {
        setLoading(true);
        try {
            const token = localStorage.getItem('accessToken');
            const url = new URL('http://localhost:8080/api/tags');
            url.searchParams.append('limit', LIMIT.toString());
            if (currentOffset) {
                url.searchParams.append('offset', currentOffset.toString());
            }

            const res = await fetch(url.toString(), {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            if (!res.ok) throw new Error('Failed to fetch tags');
            const data: TagListResponse = await res.json();
            setTags(data.tags);

            // We only know the "next" offset from the response.
            // If we want to go "next", we need this value.
            // If data.nextOffset is present, we can go to next page.

            return data.nextOffset;

        } catch (error) {
            console.error(error);
            return undefined;
        } finally {
            setLoading(false);
        }
    }, []);

    // Initial load
    useEffect(() => {
        // Load first page
        fetchTags(undefined);
    }, [fetchTags]);

    // Handle Pagination
    // Note: This is an infinite scroll style pagination or "Load More" logic adapted to pages
    // because Qdrant doesn't give total count easily or random access by index.
    // We will trust the server returns `nextOffset`.

    // Actually, simpler logic:
    // Page 1: offset=undefined
    // Page 2: offset=nextOffset_from_page1

    // We need to store map of Page -> Offset
    const [pageOffsets, setPageOffsets] = useState<Record<number, string | number | undefined>>({ 1: undefined });
    const [nextPageOffset, setNextPageOffset] = useState<string | number | undefined>(undefined);

    useEffect(() => {
        const currentOffset = pageOffsets[page];
        fetchTags(currentOffset).then(next => {
            setNextPageOffset(next);
            if (next) {
                setPageOffsets(prev => ({ ...prev, [page + 1]: next }));
            }
        });
    }, [page, fetchTags]); // pageOffsets is dependency but we read from it

    const handleNext = () => {
        if (nextPageOffset) {
            setPage(p => p + 1);
        }
    };

    const handlePrev = () => {
        if (page > 1) {
            setPage(p => p - 1);
        }
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
                // Refresh current page
                const currentOffset = pageOffsets[page];
                fetchTags(currentOffset);
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
        const currentOffset = pageOffsets[page];
        fetchTags(currentOffset);
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
                                <Button variant="default" onClick={handlePrev} disabled={page === 1 || loading}>
                                    Previous
                                </Button>
                                <Button variant="default" style={{ pointerEvents: 'none', border: 'none' }}>
                                    Page {page}
                                </Button>
                                <Button variant="default" onClick={handleNext} disabled={!nextPageOffset || loading}>
                                    Next
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
