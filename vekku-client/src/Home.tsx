import { useAuth } from './components/auth/AuthProvider';

export default function Home() {
    const { user, logout } = useAuth();

    return (
        <div className="app-container">
            <header className="app-header">
                <h1>🌌 Vekku</h1>
                {user && (
                    <div className="user-controls">
                        <span>Welcome, <strong>{user.firstName || user.email}</strong></span>
                        <button onClick={logout} className="logout-button">Logout</button>
                    </div>
                )}
            </header>

            <div style={{
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                justifyContent: 'center',
                height: '60vh'
            }}>
                <p style={{ color: 'var(--color-text-dim)', marginBottom: '3rem', fontSize: '1.2rem' }}>
                    The Knowledge Graph Taxonomy Engine
                </p>

                <div style={{ padding: '2rem', background: 'white', borderRadius: '8px', boxShadow: '0 4px 6px -1px rgba(0,0,0,0.1)' }}>
                    <h3>Protected Content</h3>
                    <p>You are successfully logged in as: {user?.email}</p>
                </div>
            </div>
        </div>
    );
}
