import express from 'express';
import { spawn } from 'child_process';
import { WebSocketServer } from 'ws';
import cors from 'cors';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const PROJECT_ROOT = path.resolve(__dirname, '../../');

const app = express();
app.use(cors());
app.use(express.json());

const PORT = 3001;

// Stores running processes
const processes = {
    brain: { process: null, pid: null, status: 'stopped' },
    server: { process: null, pid: null, status: 'stopped' },
    client: { process: null, pid: null, status: 'stopped' },
    docker: { process: null, pid: null, status: 'stopped' }
};

// WebSocket for logs
const wss = new WebSocketServer({ port: 3002 });
const clients = new Set();

wss.on('connection', (ws) => {
    clients.add(ws);
    ws.on('close', () => clients.delete(ws));
    // Send initial status
    ws.send(JSON.stringify({ type: 'status', data: getStatus() }));
});

function broadcast(type, data) {
    const msg = JSON.stringify({ type, data });
    clients.forEach(client => {
        if (client.readyState === 1) client.send(msg);
    });
}

function getStatus() {
    return {
        brain: processes.brain.status,
        server: processes.server.status,
        client: processes.client.status,
        docker: processes.docker.status
    };
}

function streamLog(service, data, isError = false) {
    const text = data.toString();
    // Broadcast log to all connected clients
    broadcast('log', { service, text, isError });
}

function startService(name) {
    // If it's already running (and not null), don't restart unless we are sure
    if (processes[name].process) {
        broadcast('log', { service: 'system', text: `Service ${name} is already running.` });
        return;
    }

    let cmd, args, cwd;

    if (name === 'brain') {
        cmd = 'npm';
        args = ['run', 'dev'];
        cwd = path.join(PROJECT_ROOT, 'vekku-brain-service');
    } else if (name === 'server') {
        cmd = './mvnw';
        args = ['spring-boot:run'];
        cwd = path.join(PROJECT_ROOT, 'vekku-server');
    } else if (name === 'client') {
        cmd = 'npm';
        args = ['run', 'dev'];
        cwd = path.join(PROJECT_ROOT, 'vekku-client');
    } else if (name === 'docker') {
        cmd = 'docker-compose';
        args = ['up'];
        cwd = PROJECT_ROOT; // Contains docker-compose.yaml
    } else {
        return;
    }

    broadcast('log', { service: 'system', text: `Starting ${name}...` });

    const proc = spawn(cmd, args, { cwd, shell: true });
    processes[name].process = proc;
    processes[name].pid = proc.pid;
    processes[name].status = 'running';

    broadcast('status', getStatus());

    proc.stdout.on('data', (data) => streamLog(name, data));
    proc.stderr.on('data', (data) => streamLog(name, data, true));

    proc.on('close', (code) => {
        processes[name].process = null;
        processes[name].pid = null;
        processes[name].status = 'stopped';
        broadcast('status', getStatus());
        broadcast('log', { service: 'system', text: `Service ${name} exited with code ${code}` });
    });
}

function stopService(name) {
    if (processes[name].process) {
        broadcast('log', { service: 'system', text: `Stopping ${name}...` });

        if (name === 'docker') {
            // For docker, we want to ensure containers match the process state better
            // Killing 'docker-compose up' usually stops containers but let's just kill the process for now.
            // The user can use "Reset" to force down -v.
            // Optionally we could spawn 'docker-compose stop' here.
        }

        processes[name].process.kill();
        // Force update just in case it doesn't fire close immediately
        processes[name].status = 'stopping';
        broadcast('status', getStatus());
    }
}

// REST API
app.post('/api/start/:service', (req, res) => {
    const { service } = req.params;
    if (processes[service]) {
        startService(service);
        res.json({ success: true });
    } else {
        res.status(404).json({ error: 'Service not found' });
    }
});

app.post('/api/stop/:service', (req, res) => {
    const { service } = req.params;
    if (processes[service]) {
        stopService(service);
        res.json({ success: true });
    } else {
        res.status(404).json({ error: 'Service not found' });
    }
});

app.post('/api/reset/docker', (req, res) => {
    // Special handler to reset docker volumes
    if (processes.docker.process) {
        stopService('docker');
    }

    broadcast('log', { service: 'system', text: 'Executing Docker Reset (down -v)...' });

    // Wait a moment for stop to register if it was running
    setTimeout(() => {
        const cwd = PROJECT_ROOT;
        const resetProc = spawn('docker-compose', ['down', '-v'], { cwd, shell: true });

        resetProc.stdout.on('data', d => streamLog('docker', d));
        resetProc.stderr.on('data', d => streamLog('docker', d, true));

        resetProc.on('close', (code) => {
            broadcast('log', { service: 'system', text: `Docker reset completed with code ${code}` });
            // Auto restart after reset
            startService('docker');
        });

        res.json({ success: true, message: 'Resetting docker volumes...' });
    }, 1000);
});

app.post('/api/restart/:service', (req, res) => {
    const { service } = req.params;
    if (processes[service]) {
        stopService(service);
        // Wait a bit for it to stop then start
        setTimeout(() => startService(service), 1500);
        res.json({ success: true });
    } else {
        res.status(404).json({ error: 'Service not found' });
    }
});

app.post('/api/write/:service', (req, res) => {
    const { service } = req.params;
    const { data } = req.body;

    if (processes[service] && processes[service].process) {
        try {
            processes[service].process.stdin.write(data);
            res.json({ success: true });
        } catch (e) {
            console.error(`Failed to write to ${service}:`, e);
            res.status(500).json({ error: e.message });
        }
    } else {
        res.status(404).json({ error: 'Service not running' });
    }
});

// Auto-start all on server launch
setTimeout(() => {
    startService('docker'); // Start DBs first
    setTimeout(() => {
        startService('brain');
        startService('server');
        startService('client');
    }, 5000); // Give docker a head start
}, 1000);

app.listen(PORT, () => {
    console.log(`Dev Server running on http://localhost:${PORT}`);
    console.log(`WebSocket Server running on ws://localhost:3002`);
});
