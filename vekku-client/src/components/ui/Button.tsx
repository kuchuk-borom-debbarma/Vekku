import { Button as MantineButton } from '@mantine/core';
import type { ButtonProps as MantineButtonProps } from '@mantine/core';
import type { ReactNode } from 'react';

interface ButtonProps extends MantineButtonProps {
    variant?: 'primary' | 'secondary' | 'ghost' | 'danger';
    children: ReactNode;
    onClick?: (e: any) => void;
    type?: "submit" | "reset" | "button";
}

export function Button({ variant = 'primary', children, ...props }: ButtonProps) {
    // Map our variants to Mantine variants/colors
    let mantineVariant = 'filled';
    let color: string | undefined = undefined;

    switch (variant) {
        case 'primary':
            mantineVariant = 'filled';
            break;
        case 'secondary':
            mantineVariant = 'default';
            break;
        case 'ghost':
            mantineVariant = 'subtle';
            break;
        case 'danger':
            color = 'red';
            mantineVariant = 'light';
            break;
    }

    return (
        <MantineButton
            variant={mantineVariant}
            color={color}
            {...props}
        >
            {children}
        </MantineButton>
    );
}
