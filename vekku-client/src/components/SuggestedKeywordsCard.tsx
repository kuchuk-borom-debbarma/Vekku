import { Badge, ActionIcon, Tooltip } from '@mantine/core';
import { IconRefresh, IconSparkles } from '@tabler/icons-react';
import { Card } from './ui/Card';

interface SuggestedKeywordsCardProps {
    keywords: { keyword: string, score: number }[];
    loading: boolean;
    onRefresh: () => void;
    onTeach: (keyword: string) => void;
}

export function SuggestedKeywordsCard({ keywords, loading, onRefresh, onTeach }: SuggestedKeywordsCardProps) {
    return (
        <Card>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                    <IconSparkles size={16} color="var(--mantine-primary-color-filled)" />
                    <h3 style={{ fontSize: '1.1rem', margin: 0 }}>Suggested Keywords</h3>
                </div>
                <Tooltip label="Refresh Keywords">
                    <ActionIcon onClick={onRefresh} loading={loading} variant="subtle" color="gray">
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
                        onClick={() => onTeach(k.keyword)}
                    >
                        {k.keyword}
                    </Badge>
                ))}
                {keywords.length === 0 && <div style={{ color: 'var(--color-text-tertiary)', fontSize: '0.9rem' }}>No keywords extracted</div>}
            </div>
        </Card>
    );
}
