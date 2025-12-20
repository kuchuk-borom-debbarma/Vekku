import type { ReactNode } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../auth/AuthProvider';
import { AppShell, Group, Button, Text, Container } from '@mantine/core';

interface LayoutProps {
    children: ReactNode;
}

export function Layout({ children }: LayoutProps) {
    const { user, logout } = useAuth();
    const location = useLocation();

    return (
        <AppShell
            header={{ height: 60 }}
            padding="md"
        >
            <AppShell.Header>
                <Container size="lg" h="100%">
                    <Group h="100%" px="md" justify="space-between">
                        <Link to="/" style={{ textDecoration: 'none' }}>
                            <Text
                                size="xl"
                                fw={900}
                                variant="gradient"
                                gradient={{ from: 'violet', to: 'cyan', deg: 45 }}
                            >
                                🌌 Vekku
                            </Text>
                        </Link>

                        <Group gap="md" visibleFrom="xs">
                            {user && (
                                <>
                                    <Button
                                        component={Link}
                                        to="/docs"
                                        variant={location.pathname.startsWith('/docs') ? 'light' : 'subtle'}
                                    >
                                        Documents
                                    </Button>

                                    <Text size="sm" c="dimmed">
                                        {user.email}
                                    </Text>
                                    <Button onClick={logout} variant="default" size="xs">
                                        Logout
                                    </Button>
                                </>
                            )}
                        </Group>

                        {/* Mobile menu could go here */}
                    </Group>
                </Container>
            </AppShell.Header>

            <AppShell.Main>
                <Container size="lg">
                    {children}
                </Container>
            </AppShell.Main>
        </AppShell>
    );
}
