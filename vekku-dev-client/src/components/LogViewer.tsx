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
            disableStdin: false, // Enable input
            convertEol: true, // Handle \n vs \r\n
            cursorBlink: true,
        });

        const fitAddon = new FitAddon();
        term.loadAddon(fitAddon);

        if (terminalRef.current) {
            term.open(terminalRef.current);
            xtermRef.current = term;
            fitAddonRef.current = fitAddon;
            // Trigger initial fit
            setTimeout(() => {
                fitAddon.fit();
            }, 0);
        }

        // Handle Input
        term.onData((data) => {
            // Send data to server
            fetch(`http://localhost:3001/api/write/${service}`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ data })
            }).catch(err => {
                console.error("Failed to send input", err);
            });
        });

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

    const clearLogs = () => {
        xtermRef.current?.clear();
    };

    return (
        <div className="log-viewer">
            <div className="log-header">
                <span>{label}</span>
                <div className="log-controls">
                    <button onClick={clearLogs} title="Clear Log Output">Clear</button>
                </div>
            </div>
            <div className="terminal-container" ref={terminalRef} style={{ width: '100%' }} />
        </div>
    );
};
