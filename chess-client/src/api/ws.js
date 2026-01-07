// Minimal WebSocket helper using native WebSocket.
import { WS_BASE } from '../config';

export function createGameSocket(token) {
  // Normalize WS_BASE:
  // - convert http/https -> ws/wss
  // - trim trailing slashes
  function normalize(raw) {
    if (!raw) return '';
    let s = raw.trim();
    // convert http(s) to ws(s)
    if (/^https?:\/\//i.test(s)) {
      s = s.replace(/^http/i, 'ws');
    }
    // trim trailing slashes
    s = s.replace(/\/+$/, '');
    return s;
  }

  const base = normalize(WS_BASE);
  // build url, avoid duplicate /ws
  const url = base.endsWith('/ws') ? `${base}/game?token=${token}` : `${base}/ws/game?token=${token}`;
  console.log('[WS] connecting to', url);
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


