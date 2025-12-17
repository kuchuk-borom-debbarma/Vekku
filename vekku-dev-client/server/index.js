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
    client: { process: null, pid: null, status: 'stopped' }
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
        client: processes.client.status
    };
}

function streamLog(service, data, isError = false) {
    const text = data.toString();
    // Broadcast log to all connected clients
    broadcast('log', { service, text, isError });
}

function startService(name) {
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
        // Use tree-kill or negative PID for process groups if needed, but for now simple kill
        // Since shell:true spawns a shell, killing the process might just kill the shell.
        // For simplicity in this dev tool, we try standard kill.
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

// Auto-start all on server launch
setTimeout(() => {
    startService('brain');
    startService('server');
    startService('client');
}, 1000);

app.listen(PORT, () => {
    console.log(`Dev Server running on http://localhost:${PORT}`);
    console.log(`WebSocket Server running on ws://localhost:3002`);
});
