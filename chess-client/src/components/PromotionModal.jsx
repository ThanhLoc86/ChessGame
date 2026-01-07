import React from 'react';

const symbolMap = {
  'Q': '♕', 'R': '♖', 'B': '♗', 'N': '♘',
  'q': '♛', 'r': '♜', 'b': '♝', 'n': '♞'
};

export default function PromotionModal({ open, color, onSelect }) {
  if (!open) return null;

  const isWhite = color === 'WHITE';
  const options = [
    { type: 'QUEEN', char: isWhite ? 'Q' : 'q' },
    { type: 'ROOK', char: isWhite ? 'R' : 'r' },
    { type: 'BISHOP', char: isWhite ? 'B' : 'b' },
    { type: 'KNIGHT', char: isWhite ? 'N' : 'n' }
  ];

  return (
    <div className="modal-overlay">
      <div className="modal-content promotion-modal">
        <h3>Chọn quân cờ phong cấp</h3>
        <div className="promotion-options">
          {options.map(opt => (
            <div key={opt.type} className="promotion-option" onClick={() => onSelect(opt.type)}>
              <span style={{ fontSize: 44, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                {symbolMap[opt.char]}
              </span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
