import React, { useEffect, useMemo, useState } from 'react';
import Square from './Square';
import PromotionModal from './PromotionModal';
import { Chess } from 'chess.js';
import { API_BASE } from '../config';

// map FEN char to asset path
const pieceMap = {
  'P': '/assets/pieces/wp.png', 'R': '/assets/pieces/wr.png', 'N': '/assets/pieces/wn.png', 'B': '/assets/pieces/wb.png', 'Q': '/assets/pieces/wq.png', 'K': '/assets/pieces/wk.png',
  'p': '/assets/pieces/bp.png', 'r': '/assets/pieces/br.png', 'n': '/assets/pieces/bn.png', 'b': '/assets/pieces/bb.png', 'q': '/assets/pieces/bq.png', 'k': '/assets/pieces/bk.png'
};

function parseFEN(fen) {
  if (!fen) return Array(8).fill(0).map(() => Array(8).fill(null));
  const [boardPart] = fen.split(' ');
  const rows = boardPart.split('/');
  const out = [];
  for (let r = 0; r < 8; r++) {
    const row = [];
    for (const ch of rows[r]) {
      if (/\d/.test(ch)) {
        const n = parseInt(ch, 10);
        for (let i = 0; i < n; i++) row.push(null);
      } else row.push(ch);
    }
    out.push(row);
  }
  return out;
}

export default function ChessBoard({ fen, orientation = 'w', onMove, myColor, disabled, lastMove }) {
  const chess = useMemo(() => new Chess(), []);
  const [selected, setSelected] = useState(null);
  const [possible, setPossible] = useState([]);
  const [pendingPromotion, setPendingPromotion] = useState(null);

  useEffect(() => { if (fen) chess.load(fen); }, [fen, chess]);

  const board = useMemo(() => parseFEN(fen), [fen]);

  function coord(r, c) { return String.fromCharCode(97 + c) + (8 - r); }

  function onSquareClick(coordStr) {
    if (disabled || pendingPromotion) return;

    if (!selected) {
      const mv = chess.moves({ square: coordStr, verbose: true });
      if (!mv || mv.length === 0) return;
      const piece = chess.get(coordStr);
      if (!piece) return;
      const isWhite = piece.color === 'w';
      if ((myColor === 'WHITE' && !isWhite) || (myColor === 'BLACK' && isWhite)) return;
      setSelected(coordStr);
      setPossible(mv.map(m => m.to));
      return;
    }

    const move = chess.moves({ square: selected, verbose: true }).find(m => m.to === coordStr);

    if (move) {
      if (move.flags.includes('p')) {
        setPendingPromotion({ from: selected, to: coordStr });
      } else {
        onMove && onMove(selected, coordStr);
        setSelected(null); setPossible([]);
      }
    } else {
      setSelected(null); setPossible([]);
    }
  }

  function handlePromotionSelect(pieceType) {
    if (pendingPromotion) {
      onMove && onMove(pendingPromotion.from, pendingPromotion.to, pieceType);
      setPendingPromotion(null);
      setSelected(null); setPossible([]);
    }
  }

  // render rows with orientation
  const rows = orientation === 'w' ? [...Array(8).keys()] : [...Array(8).keys()].reverse();
  const cols = orientation === 'w' ? [...Array(8).keys()] : [...Array(8).keys()].reverse();

  return (
    <div style={{ position: 'relative', width: '100%', maxWidth: '80vh', margin: '0 auto' }}>
      <div style={{
        width: '100%',
        display: 'grid',
        gridTemplateRows: 'repeat(8,1fr)',
        gridTemplateColumns: 'repeat(8,1fr)',
        aspectRatio: '1/1'
      }}>
        {rows.map(r => cols.map(c => {
          const piece = board[r][c];
          const coordStr = coord(r, c);
          const isLight = (r + c) % 2 === 0;
          const pieceSrc = piece ? pieceMap[piece] : null;
          const isSelected = selected === coordStr;
          const isPossible = possible.includes(coordStr);
          const isFrom = lastMove && lastMove.from === coordStr;
          const isTo = lastMove && lastMove.to === coordStr;
          return (
            <Square
              key={coordStr}
              coord={coordStr}
              pieceSrc={pieceSrc}
              pieceChar={piece}
              isLight={isLight}
              onClick={onSquareClick}
              selected={isSelected}
              possible={isPossible}
              isFrom={isFrom}
              isTo={isTo}
            />
          );
        }))}
      </div>
      <PromotionModal open={!!pendingPromotion} color={myColor} onSelect={handlePromotionSelect} />
    </div>
  );
}


