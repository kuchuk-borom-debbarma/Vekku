import { Badge, ActionIcon, Tooltip } from '@mantine/core';
import { IconRefresh } from '@tabler/icons-react';
import { Card } from './ui/Card';

interface Tag {
    id: string;
    name: string;
}

interface ContentTagSuggestion {
    id: string;
    tag: Tag;
    score: number;
}

interface SuggestedTagsCardProps {
    suggestedTags: ContentTagSuggestion[];
    loading: boolean;
    onRefresh: () => void;
    onAddTag: (tagId: string) => void;
}

export function SuggestedTagsCard({ suggestedTags, loading, onRefresh, onAddTag }: SuggestedTagsCardProps) {
    return (
        <Card>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
                <h3 style={{ fontSize: '1.1rem', margin: 0 }}>Suggested Tags</h3>
                <Tooltip label="Refresh Suggestions">
                    <ActionIcon onClick={onRefresh} loading={loading} variant="subtle" color="gray">
                        <IconRefresh size={18} />
                    </ActionIcon>
                </Tooltip>
            </div>

            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem' }}>
                {suggestedTags.map(st => (
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
                        onClick={() => onAddTag(st.tag.id)}
                    >
                        {st.tag.name}
                    </Badge>
                ))}
                {suggestedTags.length === 0 && <div style={{ color: 'var(--color-text-tertiary)', fontSize: '0.9rem' }}>No suggestions available</div>}
            </div>
        </Card>
    );
}
