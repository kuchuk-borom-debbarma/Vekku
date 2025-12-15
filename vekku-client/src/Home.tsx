import { useNavigate } from 'react-router-dom';

export default function Home() {
    const navigate = useNavigate();

    return (
        <div style={{
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            justifyContent: 'center',
            height: '80vh'
        }}>
            <h1 style={{ fontSize: '3rem', marginBottom: '1rem' }}>🌌 Vekku</h1>
            <p style={{ color: 'var(--color-text-dim)', marginBottom: '3rem' }}>
                The Knowledge Graph Taxonomy Engine
            </p>

            <button
                onClick={() => navigate('/tag-hierarchy')}
                style={{
                    fontSize: '1.2rem',
                    padding: '1rem 2rem',
                    cursor: 'pointer'
                }}
            >
                Show Tag Hierarchy
            </button>
        </div>
    );
}
