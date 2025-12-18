import { ReactNode } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../auth/AuthProvider';

interface LayoutProps {
    children: ReactNode;
}

export function Layout({ children }: LayoutProps) {
    const { user, logout } = useAuth();
    const location = useLocation();

    return (
        <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
            {/* Navbar */}
            <header style={{
                height: '64px',
                borderBottom: '1px solid var(--color-border)',
                backgroundColor: 'var(--color-bg-surface)',
                display: 'flex',
                alignItems: 'center',
                padding: '0 2rem',
                justifyContent: 'space-between',
                position: 'sticky',
                top: 0,
                zIndex: 10
            }}>
                <Link to="/" style={{
                    fontSize: '1.25rem',
                    fontWeight: 700,
                    background: 'linear-gradient(135deg, #fff 0%, #aaa 100%)',
                    WebkitBackgroundClip: 'text',
                    WebkitTextFillColor: 'transparent',
                    letterSpacing: '-0.5px'
                }}>
                    🌌 Vekku
                </Link>

                <nav style={{ display: 'flex', gap: '1.5rem', alignItems: 'center' }}>
                    {user && (
                        <>
                            <Link to="/docs" style={{
                                color: location.pathname.startsWith('/docs') ? 'var(--color-text-primary)' : 'var(--color-text-secondary)',
                                fontSize: '0.95rem',
                                fontWeight: 500
                            }}>Documents</Link>

                            <div style={{ width: '1px', height: '20px', background: 'var(--color-border)' }} />

                            <span style={{ fontSize: '0.85rem', color: 'var(--color-text-tertiary)' }}>
                                {user.email}
                            </span>
                            <button
                                onClick={logout}
                                style={{
                                    fontSize: '0.85rem',
                                    padding: '0.4rem 0.8rem',
                                    borderRadius: 'var(--radius-sm)',
                                    color: 'var(--color-text-secondary)',
                                    backgroundColor: 'var(--color-brand-secondary)'
                                }}
                            >
                                Logout
                            </button>
                        </>
                    )}
                </nav>
            </header>

            {/* Main Content */}
            <main style={{
                flex: 1,
                width: '100%',
                maxWidth: '1200px',
                margin: '0 auto',
                padding: '2rem'
            }}>
                {children}
            </main>
        </div>
    );
}
