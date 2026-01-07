import React from 'react';

export default function GameOverModal({ open, message, onClose }) {
  if (!open) return null;
  return (
    <div style={{
      position: 'fixed',
      inset: 0,
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      background: 'rgba(0,0,0,0.8)',
      backdropFilter: 'blur(8px)',
      zIndex: 1000,
      animation: 'fadeIn 0.3s ease-out'
    }}>
      <div className="card" style={{
        background: 'var(--card-bg)',
        padding: '32px 48px',
        borderRadius: 24,
        minWidth: 320,
        textAlign: 'center',
        border: '1px solid rgba(255,255,255,0.1)',
        boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.5)',
        transform: 'scale(1)',
        animation: 'popIn 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275)'
      }}>
        <div style={{ fontSize: 48, marginBottom: 16 }}>🏆</div>
        <h2 style={{ margin: 0, fontSize: 24, fontWeight: 800, color: 'var(--text)' }}>Kết thúc trận đấu</h2>
        <div style={{
          marginTop: 16,
          fontSize: 18,
          fontWeight: 600,
          color: 'var(--accent)',
          background: 'rgba(99,102,241,0.1)',
          padding: '12px 24px',
          borderRadius: 12,
          display: 'inline-block'
        }}>
          {message}
        </div>
        <div style={{ marginTop: 32 }}>
          <button onClick={onClose} style={{
            width: '100%',
            height: 48,
            fontSize: 16,
            fontWeight: 700,
            borderRadius: 12,
            background: 'linear-gradient(135deg, var(--accent) 0%, #4f46e5 100%)',
            color: 'white',
            border: 'none',
            cursor: 'pointer',
            transition: 'transform 0.2s'
          }}
            onMouseDown={e => e.currentTarget.style.transform = 'scale(0.95)'}
            onMouseUp={e => e.currentTarget.style.transform = 'scale(1)'}
          >
            Quay lại sảnh
          </button>
        </div>
      </div>
      <style>{`
        @keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
        @keyframes popIn { from { transform: scale(0.8); opacity: 0; } to { transform: scale(1); opacity: 1; } }
      `}</style>
    </div>
  );
}


