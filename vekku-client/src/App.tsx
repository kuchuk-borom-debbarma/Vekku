import { useState, useEffect } from 'react'
import './App.css'

function App() {
  const [tags, setTags] = useState([])
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    fetch('/api/taxonomy/tree')
      .then(res => {
        if (!res.ok) throw new Error(`HTTP error! Status: ${res.status}`);
        return res.json();
      })
      .then(data => setTags(data))
      .catch(err => {
        console.error("Failed to fetch taxonomy:", err);
        setError(err.message);
      })
  }, [])

  return (
    <div style={{ textAlign: 'left' }}>
      <h1>🌌 Vekku Taxonomy</h1>
      <p style={{ color: 'var(--color-text-dim)' }}>
        Exploration of knowledge graph.
      </p>

      {error ? (
        <div style={{ padding: '1rem', border: '1px solid red', borderRadius: '8px', color: '#ff6b6b' }}>
          ⚠️ Error connecting to server: {error}
        </div>
      ) : (
        <div style={{ marginTop: '2rem' }}>
          <h3>Raw Response ({tags.length} nodes)</h3>
          <pre style={{
            background: 'var(--color-surface)',
            padding: '1rem',
            borderRadius: '8px',
            overflow: 'auto',
            maxHeight: '500px'
          }}>
            {JSON.stringify(tags, null, 2)}
          </pre>
        </div>
      )}
    </div>
  )
}

export default App
