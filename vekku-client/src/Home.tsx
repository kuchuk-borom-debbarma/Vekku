import { useNavigate } from 'react-router-dom';
import { useAuth } from './components/auth/AuthProvider';
import { Layout } from './components/ui/Layout';
import { Button } from './components/ui/Button';
import { Card } from './components/ui/Card';
export default function Home() {
    const { user } = useAuth();
    const navigate = useNavigate();

    return (
        <Layout>
            <div style={{
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                justifyContent: 'center',
                minHeight: '60vh',
                textAlign: 'center'
            }}>
                <div style={{ marginBottom: '2rem' }}>
                    <h1 style={{
                        fontSize: '3.5rem',
                        marginBottom: '1rem',
                        background: 'linear-gradient(135deg, var(--color-brand-primary) 0%, #fff 100%)',
                        WebkitBackgroundClip: 'text',
                        WebkitTextFillColor: 'transparent',
                    }}>
                        Organize your <br /> Knowledge
                    </h1>
                    <p style={{
                        color: 'var(--color-text-secondary)',
                        fontSize: '1.2rem',
                        maxWidth: '600px',
                        margin: '0 auto'
                    }}>
                        AI-powered taxonomy engine that automatically tags and categorizes your content.
                    </p>
                </div>

                <Card style={{ width: '100%', maxWidth: '400px' }}>
                    <h3 style={{ marginBottom: '1rem', color: 'var(--color-text-primary)' }}>Quick Actions</h3>
                    <p style={{ marginBottom: '2rem', color: 'var(--color-text-tertiary)', fontSize: '0.9rem' }}>
                        Logged in as {user?.email}
                    </p>

                    <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                        <Button
                            variant="primary"
                            size="lg"
                            onClick={() => navigate('/create')}
                            style={{ width: '100%' }}
                        >
                            + Create New Content
                        </Button>
                        <Button
                            variant="secondary"
                            size="lg"
                            onClick={() => navigate('/docs')}
                            style={{ width: '100%' }}
                        >
                            View All Documents
                        </Button>
                        <Button
                            variant="secondary"
                            size="lg"
                            onClick={() => navigate('/tags')}
                            style={{ width: '100%' }}
                        >
                            🏷️ Manage Tags
                        </Button>
                    </div>
                </Card>
            </div>
        </Layout>
    );
}
