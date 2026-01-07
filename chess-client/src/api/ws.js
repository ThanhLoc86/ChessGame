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
    // collapse accidental repeated 's' like 'wsss://' -> 'wss://'
    s = s.replace(/^wss+:\/\//i, 'wss://');
    // trim trailing slashes
    s = s.replace(/\/+$/, '');
    return s;
  }

  const base = normalize(WS_BASE);
  // build url, avoid duplicate /ws
  const url = base.endsWith('/ws') ? `${base}/game?token=${token}` : `${base}/ws/game?token=${token}`;
  console.log('[WS] connecting to', url);
  if (!/^wss?:\/\//i.test(url)) {
    console.error('[WS] invalid websocket URL scheme:', url);
    // return a dummy socket-like object to avoid throwing and crashing the whole UI
    const listeners = new Set();
    return {
      ws: null,
      onMessage(fn){ listeners.add(fn); return () => listeners.delete(fn); },
      send(obj){ console.warn('[WS] send ignored, socket not connected'); },
      close(){ /* noop */ }
    };
  }
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


