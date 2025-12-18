import { Paper } from '@mantine/core';
import type { PaperProps } from '@mantine/core';
import { ReactNode } from 'react';

import { ComponentPropsWithoutRef } from 'react';

interface CardProps extends PaperProps, ComponentPropsWithoutRef<'div'> {
    children: ReactNode;
}

export function Card({ children, ...props }: CardProps) {
    return (
        <Paper withBorder p="xl" radius="md" shadow="sm" {...props}>
            {children}
        </Paper>
    );
}
