import { Paper } from '@mantine/core';
import type { PaperProps } from '@mantine/core';
import type { ReactNode } from 'react';

import type { ComponentPropsWithoutRef } from 'react';

interface CardProps extends PaperProps, Omit<ComponentPropsWithoutRef<'div'>, 'style' | 'children'> {
    children: ReactNode;
}

export function Card({ children, ...props }: CardProps) {
    return (
        <Paper withBorder p="xl" radius="md" shadow="sm" {...props}>
            {children}
        </Paper>
    );
}
