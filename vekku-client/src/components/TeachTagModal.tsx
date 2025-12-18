import { useState } from 'react';
import { Modal, TextInput, Button, Group, Text, Notification } from '@mantine/core';


interface TeachTagModalProps {
    opened: boolean;
    onClose: () => void;
}

export function TeachTagModal({ opened, onClose }: TeachTagModalProps) {
    const [tagName, setTagName] = useState('');
    const [loading, setLoading] = useState(false);
    const [status, setStatus] = useState<{ type: 'success' | 'error', message: string } | null>(null);

    const handleLearn = async () => {
        if (!tagName.trim()) return;

        setLoading(true);
        setStatus(null);

        try {
            const token = localStorage.getItem('accessToken');
            const res = await fetch('http://localhost:8080/api/tags/learn', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify({ tagName }),
            });

            if (!res.ok) throw new Error('Failed to learn tag');

            setStatus({ type: 'success', message: `Successfully taught brain: "${tagName}"` });
            setTagName('');
            // Optional: Close modal after delay
            // setTimeout(onClose, 1500); 
        } catch (error) {
            console.error(error);
            setStatus({ type: 'error', message: 'Failed to teach tag. Please try again.' });
        } finally {
            setLoading(false);
        }
    };

    return (
        <Modal opened={opened} onClose={onClose} title="🧠 Teach Brain New Tag" centered>
            <Text size="sm" c="dimmed" mb="md">
                Add a new concept to the Knowledge Graph. The brain will begin to recognize and organize content related to this tag.
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
                label="Tag Name"
                placeholder="e.g. Artificial Intelligence, Legal Contracts"
                value={tagName}
                onChange={(e) => setTagName(e.currentTarget.value)}
                data-autofocus
                mb="lg"
            />

            <Group justify="flex-end">
                <Button variant="default" onClick={onClose} disabled={loading}>
                    Cancel
                </Button>
                <Button onClick={handleLearn} loading={loading} disabled={!tagName.trim()}>
                    Teach Brain
                </Button>
            </Group>
        </Modal>
    );
}
