import React, { useEffect, useRef } from 'react';
import { Terminal } from 'xterm';
import { FitAddon } from 'xterm-addon-fit';
import 'xterm/css/xterm.css';

interface LogViewerProps {
    service: string;
    wsUrl: string;
    label: string;
}

export const LogViewer: React.FC<LogViewerProps> = ({ service, wsUrl, label }) => {
    const terminalRef = useRef<HTMLDivElement>(null);
    const xtermRef = useRef<Terminal | null>(null);
    const fitAddonRef = useRef<FitAddon | null>(null);

    useEffect(() => {
        // Initialize Terminal
        const term = new Terminal({
            theme: {
                background: '#0d1117',
                foreground: '#c9d1d9',
                cursor: '#58a6ff',
                selectionBackground: '#58a6ff40'
            },
            fontFamily: 'JetBrains Mono, monospace',
            fontSize: 12,
            disableStdin: true, // Read-only
            convertEol: true, // Handle \n vs \r\n
        });

        const fitAddon = new FitAddon();
        term.loadAddon(fitAddon);

        if (terminalRef.current) {
            term.open(terminalRef.current);
            fitAddon.fit();
            xtermRef.current = term;
            fitAddonRef.current = fitAddon;
        }

        // WebSocket Connection
        const ws = new WebSocket(wsUrl);

        ws.onmessage = (event) => {
            try {
                const msg = JSON.parse(event.data);
                if (msg.type === 'log' && msg.data.service === service) {
                    const { text, isError } = msg.data;
                    if (isError) {
                        // ANSI Red for stderr
                        term.write(`\x1b[31m${text}\x1b[0m`);
                    } else {
                        term.write(text);
                    }
                } else if (msg.type === 'log' && msg.data.service === 'system' && service === 'all') {
                    // Maybe show system messages in all?
                    // Currently filtering strictly by service name.
                }
            } catch (e) {
                // Ignore parse errors from non-json messages if any
            }
        };

        // Resize observer
        const resizeObserver = new ResizeObserver(() => {
            fitAddon.fit();
        });

        if (terminalRef.current) {
            resizeObserver.observe(terminalRef.current);
        }

        return () => {
            ws.close();
            term.dispose();
            resizeObserver.disconnect();
        };
    }, [service, wsUrl]);

    return (
        <div className="log-viewer">
            <div className="log-header">{label}</div>
            <div className="terminal-container" ref={terminalRef} style={{ width: '100%', height: '100%' }} />
        </div>
    );
};
