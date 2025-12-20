import { useState, useEffect } from 'react';
import { Modal, TextInput, Button, Group, Text, Notification, TagsInput } from '@mantine/core';


interface TeachTagModalProps {
    opened: boolean;
    onClose: () => void;
    initialData?: { id: string; name: string; synonyms: string[] } | null;
}

export function TeachTagModal({ opened, onClose, initialData }: TeachTagModalProps) {
    const [tagName, setTagName] = useState('');
    const [synonyms, setSynonyms] = useState<string[]>([]);
    const [loading, setLoading] = useState(false);
    const [status, setStatus] = useState<{ type: 'success' | 'error', message: string } | null>(null);

    // Initialize form when opened or data changes
    useEffect(() => {
        if (initialData) {
            setTagName(initialData.name);
            setSynonyms(initialData.synonyms || []);
        } else {
            setTagName('');
            setSynonyms([]);
        }
        setStatus(null);
    }, [initialData, opened]);

    const handleSave = async () => {
        if (!tagName.trim()) return;

        setLoading(true);
        setStatus(null);

        try {
            const token = localStorage.getItem('accessToken');
            const isEdit = !!initialData?.id;

            const url = isEdit
                ? `http://localhost:8080/api/tags/${initialData!.id}`
                : 'http://localhost:8080/api/tags';

            const method = isEdit ? 'PUT' : 'POST';

            const res = await fetch(url, {
                method: method,
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify({ alias: tagName, synonyms: synonyms }),
            });

            if (!res.ok) throw new Error(isEdit ? 'Failed to update tag' : 'Failed to create tag');

            setStatus({
                type: 'success',
                message: isEdit ? `Successfully updated "${tagName}"` : `Successfully created "${tagName}"`
            });

            if (!isEdit) {
                setTagName('');
                setSynonyms([]);
            }

            // Close after short delay? Or let user close.
            // onClose(); 
        } catch (error) {
            console.error(error);
            setStatus({ type: 'error', message: 'Operation failed. Please try again.' });
        } finally {
            setLoading(false);
        }
    };

    return (
        <Modal opened={opened} onClose={onClose} title={initialData ? "✏️ Edit Tag" : "🧠 Teach New Tag"} centered>
            <Text size="sm" c="dimmed" mb="md">
                {initialData
                    ? "Update the alias or synonyms for this concept. The brain will re-learn the new definitions."
                    : "Add a new concept. The brain will recognize content related to this tag and its synonyms."
                }
            </Text>

            {status && (
                <Notification
                    color={status.type === 'success' ? 'green' : 'red'}
                    onClose={() => setStatus(null)}
                    mb="md"
                    title={status.type === 'success' ? 'Success' : 'Error'}
                >
                    {status.message}
                </Notification>
            )}

            <TextInput
                label="Tag Alias"
                placeholder="e.g. Artificial Intelligence"
                value={tagName}
                onChange={(e) => setTagName(e.currentTarget.value)}
                data-autofocus
                mb="md"
            />

            <TagsInput
                label="Synonyms"
                placeholder="Press Enter to add"
                description="Alternative terms (e.g. 'AI', 'Machine Learning')"
                value={synonyms}
                onChange={setSynonyms}
                mb="lg"
            />

            <Group justify="flex-end">
                <Button variant="default" onClick={onClose} disabled={loading}>
                    Done
                </Button>
                <Button onClick={handleSave} loading={loading} disabled={!tagName.trim()}>
                    {initialData ? 'Save Changes' : 'Teach Brain'}
                </Button>
            </Group>
        </Modal>
    );
}
