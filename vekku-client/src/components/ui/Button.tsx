import { ButtonHTMLAttributes, ReactNode } from 'react';

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
    variant?: 'primary' | 'secondary' | 'ghost' | 'danger';
    size?: 'sm' | 'md' | 'lg';
    children: ReactNode;
}

export function Button({
    variant = 'primary',
    size = 'md',
    className = '',
    style,
    children,
    ...props
}: ButtonProps) {

    const baseStyles = {
        display: 'inline-flex',
        alignItems: 'center',
        justifyContent: 'center',
        borderRadius: 'var(--radius-sm)',
        fontWeight: 500,
        transition: 'all 0.2s ease',
        cursor: props.disabled ? 'not-allowed' : 'pointer',
        opacity: props.disabled ? 0.6 : 1,
        border: '1px solid transparent',
        fontFamily: 'var(--font-sans)',
    };

    const variants = {
        primary: {
            backgroundColor: 'var(--color-brand-primary)',
            color: '#000', // Better contrast on bright purple
            border: 'none',
        },
        secondary: {
            backgroundColor: 'var(--color-bg-surface-hover)',
            color: 'var(--color-text-primary)',
            border: '1px solid var(--color-border)',
        },
        ghost: {
            backgroundColor: 'transparent',
            color: 'var(--color-text-secondary)',
        },
        danger: {
            backgroundColor: 'hsl(var(--hue-danger), 20%, 20%)',
            color: 'hsl(var(--hue-danger), 80%, 70%)',
            border: '1px solid hsl(var(--hue-danger), 30%, 30%)',
        }
    };

    const sizes = {
        sm: { padding: '0.4rem 0.8rem', fontSize: '0.85rem' },
        md: { padding: '0.6rem 1.2rem', fontSize: '0.95rem' },
        lg: { padding: '0.8rem 1.6rem', fontSize: '1.1rem' },
    };

    return (
        <button
            style={{
                ...baseStyles,
                ...variants[variant],
                ...sizes[size],
                ...style
            }}
            {...props}
        >
            {children}
        </button>
    );
}
