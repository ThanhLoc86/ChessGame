import React, { useState } from 'react';

// Square renders either an image (if provided) or a fallback unicode piece symbol.
export default function Square({ coord, pieceSrc, pieceChar, isLight, onClick, selected, possible, isFrom, isTo }) {
  const [imgVisible, setImgVisible] = useState(true);
  const pieceColor = pieceChar ? (/[A-Z]/.test(pieceChar) ? 'white' : 'black') : null;
  const cls = 'square' + (isLight ? ' light' : ' dark') +
    (selected ? ' selected' : '') +
    (isFrom ? ' move-from' : '') +
    (isTo ? ' move-to' : '');

  const symbolMap = {
    'P': '♙', 'R': '♖', 'N': '♘', 'B': '♗', 'Q': '♕', 'K': '♔',
    'p': '♟', 'r': '♜', 'n': '♞', 'b': '♝', 'q': '♛', 'k': '♚'
  };
  const sym = pieceChar ? (symbolMap[pieceChar] || '') : '';

  return (
    <div className={cls} data-coord={coord} data-piece-color={pieceColor} onClick={() => onClick && onClick(coord)}
      style={{ position: 'relative', width: '100%', height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: (pieceChar || possible) ? 'pointer' : 'default' }}>
      {pieceSrc && imgVisible && (
        <img src={pieceSrc} alt="" style={{ width: '80%', height: '80%', pointerEvents: 'none', zIndex: 2 }} onError={() => setImgVisible(false)} />
      )}
      {(!pieceSrc || !imgVisible) && sym && (
        <span style={{
          fontSize: 44,
          zIndex: 2,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          color: pieceColor === 'white' ? '#f0f0f0' : '#1a1a1a',
          textShadow: pieceColor === 'white' 
            ? '1px 1px 2px #333333' 
            : '1px 1px 2px #cccccc',
          fontWeight: 'bold'
        }}>
          {sym}
        </span>
      )}

      {possible && (
        <div style={{
          position: 'absolute',
          width: pieceChar ? '80%' : 16,
          height: pieceChar ? '80%' : 16,
          borderRadius: pieceChar ? '50%' : '50%',
          border: pieceChar ? '4px solid rgba(0,0,0,0.1)' : 'none',
          background: pieceChar ? 'none' : 'rgba(0,0,0,0.15)',
          zIndex: 1
        }}></div>
      )}
    </div>
  );
}


