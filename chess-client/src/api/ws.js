// Minimal WebSocket helper using native WebSocket.
import { WS_BASE } from '../config';

export function createGameSocket(token) {
  const base = WS_BASE.replace(/^http/, 'ws');
  const url = `${base}/ws/game?token=${token}`;
  const ws = new WebSocket(url);
  const listeners = new Set();
  ws.addEventListener('message', (e) => {
    try { const msg = JSON.parse(e.data); listeners.forEach(fn => fn(msg)); } catch(e){ /* parse error ignored */ }
  });
  return {
    ws,
    onMessage(fn){ listeners.add(fn); return () => listeners.delete(fn); },
    send(obj){ if (ws.readyState === 1) ws.send(JSON.stringify(obj)); },
    close(){ ws.close(); }
  };
}


