import { useEffect, useState, useRef } from 'react';
import './App.css';
import { LogViewer } from './components/LogViewer';
import { useHotkeys } from 'react-hotkeys-hook';

interface ServiceStatus {
  brain: string;
  server: string;
  client: string;
}

function App() {
  const [status, setStatus] = useState<ServiceStatus>({ brain: 'stopped', server: 'stopped', client: 'stopped' });
  const [activeService, setActiveService] = useState<'brain' | 'server' | 'client' | 'all'>('all');

  // WebSocket Connection
  const wsRef = useRef<WebSocket | null>(null);

  useEffect(() => {
    wsRef.current = new WebSocket('ws://localhost:3002');
    wsRef.current.onmessage = (event) => {
      const msg = JSON.parse(event.data);
      if (msg.type === 'status') {
        setStatus(msg.data);
      }
    };
    return () => wsRef.current?.close();
  }, []);

  const controlService = async (service: string, action: 'start' | 'stop' | 'restart') => {
    try {
      await fetch(`http://localhost:3001/api/${action}/${service}`, { method: 'POST' });
    } catch (e) {
      console.error(e);
    }
  };

  // Keybindings
  useHotkeys('b', () => controlService('brain', 'restart'));
  useHotkeys('s', () => controlService('server', 'restart'));
  useHotkeys('c', () => controlService('client', 'restart'));
  // Focus View Toggles
  useHotkeys('1', () => setActiveService('brain'));
  useHotkeys('2', () => setActiveService('server'));
  useHotkeys('3', () => setActiveService('client'));
  useHotkeys('0', () => setActiveService('all'));

  return (
    <div className="app-container">
      <header className="header">
        <h1>Vekku Dev Client</h1>
        <div className="controls">
          {['brain', 'server', 'client'].map(svc => (
            <div key={svc} className="control-group">
              <span className={`status-indicator ${status[svc as keyof ServiceStatus]}`}></span>
              <strong>{svc.toUpperCase()} <span style={{ opacity: 0.5, fontSize: '0.8em' }}>({svc[0]})</span></strong>
              <button onClick={() => controlService(svc, 'restart')} title="Restart">↺</button>
              <button onClick={() => controlService(svc, 'stop')} title="Stop">⏹</button>
            </div>
          ))}
          <div className="view-controls">
            <button onClick={() => setActiveService('all')} className={activeService === 'all' ? 'active' : ''}>All (0)</button>
            <button onClick={() => setActiveService('brain')} className={activeService === 'brain' ? 'active' : ''}>Brain (1)</button>
            <button onClick={() => setActiveService('server')} className={activeService === 'server' ? 'active' : ''}>Server (2)</button>
            <button onClick={() => setActiveService('client')} className={activeService === 'client' ? 'active' : ''}>Client (3)</button>
          </div>
        </div>
      </header>

      <div className={`log-grid layout-${activeService}`}>
        {(activeService === 'all' || activeService === 'brain') && (
          <LogViewer service="brain" wsUrl="ws://localhost:3002" label="Brain Service" />
        )}
        {(activeService === 'all' || activeService === 'server') && (
          <LogViewer service="server" wsUrl="ws://localhost:3002" label="Spring Server" />
        )}
        {(activeService === 'all' || activeService === 'client') && (
          <LogViewer service="client" wsUrl="ws://localhost:3002" label="React Client" />
        )}
      </div>
    </div>
  );
}

export default App;
