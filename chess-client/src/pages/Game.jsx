import React, { useEffect, useRef, useState } from 'react';
import { createGameSocket } from '../api/ws';
import ChessBoard from '../components/ChessBoard';
import GameOverModal from '../components/GameOverModal';
import ChatBox from '../components/ChatBox';
import { useNavigate } from 'react-router-dom';

export default function Game() {
  const [fen, setFen] = useState(null);
  const [roomId, setRoomId] = useState(null);
  const [color, setColor] = useState(null);
  const [disabled, setDisabled] = useState(true);
  const [gameOver, setGameOver] = useState(null);
  const [lastMove, setLastMove] = useState(null);
  const [players, setPlayers] = useState({ white: null, black: null });
  const [chatMessages, setChatMessages] = useState([]);
  const [drawOffer, setDrawOffer] = useState(false);
  const [clocks, setClocks] = useState({ white: 600000, black: 600000 });

  const wsRef = useRef(null);
  const colorRef = useRef(null);
  const fenRef = useRef(null);
  const clockInterval = useRef(null);
  const nav = useNavigate();

  const mountedRef = useRef(false);

  useEffect(() => {
    if (mountedRef.current) return;
    mountedRef.current = true;

    const token = sessionStorage.getItem('token') || localStorage.getItem('token');
    if (!token) { nav('/login'); return; }
    const sock = createGameSocket(token);
    wsRef.current = sock;
    sock.onMessage(msg => {
      // incoming message
      if (msg.type === 'assigned_color') {
        setRoomId(msg.roomId);
        setColor(msg.color);
        colorRef.current = msg.color;
        // if we already have a fen, decide if it's our turn
        if (fenRef.current) {
          const active = fenRef.current.split(' ')[1];
          const myTurnNow = (active === 'w' && msg.color === 'WHITE') || (active === 'b' && msg.color === 'BLACK');
          setDisabled(!myTurnNow);
        } else {
          setDisabled(true);
        }
      } else if (msg.type === 'state') {
        setFen(msg.fen);
        fenRef.current = msg.fen;
        setPlayers({
          white: { name: msg.whiteName, elo: msg.whiteElo },
          black: { name: msg.blackName, elo: msg.blackElo }
        });
        if (msg.whiteTime !== undefined) {
          setClocks({ white: msg.whiteTime, black: msg.blackTime });
        }
        // only compute turn if we already know assigned color
        const curColor = colorRef.current;
        if (curColor) {
          const active = msg.fen.split(' ')[1];
          const myTurn = (active === 'w' && curColor === 'WHITE') || (active === 'b' && curColor === 'BLACK');
          setDisabled(!myTurn);
        } else {
          setDisabled(true);
        }
      } else if (msg.type === 'chat') {
        setChatMessages(prev => [...prev, { sender: msg.sender, text: msg.text }]);
      } else if (msg.type === 'draw_offer') {
        setDrawOffer(true);
      } else if (msg.type === 'draw_decline') {
        alert('Lời đề nghị hòa đã bị từ chối');
      } else if (msg.type === 'error') {
        const errorText = msg.code === 'not_your_turn' ? 'Chưa đến lượt của bạn!' :
          msg.code === 'illegal_move' ? 'Nước đi không hợp lệ!' :
            msg.code === 'unauthorized' ? 'Phiên làm việc hết hạn hoặc không hợp lệ!' :
              msg.code === 'roomId_required' ? 'Mã phòng không được để trống!' :
                msg.code === 'join_failed' ? 'Tham gia phòng thất bại!' :
                  msg.code || 'Có lỗi xảy ra';
        alert('Lỗi: ' + errorText);
      } else if (msg.type === 'game_over') {
        let resultMsg = '';
        if (msg.result === 'WHITE_WIN') resultMsg = 'Trắng Thắng!';
        else if (msg.result === 'BLACK_WIN') resultMsg = 'Đen Thắng!';
        else if (msg.result === 'DRAW') resultMsg = 'Hòa!';
        else resultMsg = msg.result || 'Trò chơi kết thúc';

        if (msg.reason === 'RESIGNATION') {
          const resigned = msg.resignedColor === 'WHITE' ? 'Trắng' : 'Đen';
          resultMsg += ` (${resigned} đã đầu hàng)`;
        }

        setGameOver(resultMsg);
        setDisabled(true);
      }
    });

    clockInterval.current = setInterval(() => {
      if (!fenRef.current) return;
      // We check the actual state because setInterval closure might see old gameOver
      const active = fenRef.current.split(' ')[1]; // 'w' or 'b'
      setClocks(prev => {
        const next = { ...prev };
        if (active === 'w') next.white = Math.max(0, next.white - 1000);
        else next.black = Math.max(0, next.black - 1000);
        return next;
      });
    }, 1000);

    // on open, send join/create based on localStorage
    let intentSent = false;
    wsRef.current.ws.addEventListener('open', () => {
      const params = new URLSearchParams(window.location.search);
      const joinParam = params.get('join');
      const createParam = params.get('create');
      const botParam = params.get('bot');
      if (intentSent) return;
      if (joinParam) {
        sock.send({ type: 'join', roomId: joinParam });
        intentSent = true;
        return;
      }
      if (createParam) {
        sock.send({ type: 'create' });
        intentSent = true;
        return;
      }
      if (botParam) {
        sock.send({ type: 'create_bot' });
        intentSent = true;
        return;
      }
    });
    return () => {
      sock.close();
      clearInterval(clockInterval.current);
      mountedRef.current = false;
    }
  }, [nav]);

  function formatTime(ms) {
    const s = Math.floor(ms / 1000);
    const m = Math.floor(s / 60);
    const rs = s % 60;
    return `${m}:${rs.toString().padStart(2, '0')}`;
  }

  function handleMove(from, to, promotionPiece) {
    if (wsRef.current) {
      const msg = { type: 'move', from, to };
      if (promotionPiece) msg.promotionPiece = promotionPiece;
      wsRef.current.send(msg);
    }
    setLastMove({ from, to });
  }

  function handleSendChat(text) {
    if (wsRef.current) {
      wsRef.current.send({ type: 'chat', text });
    }
  }

  function handleResign() {
    if (window.confirm('Bạn có chắc chắn muốn đầu hàng? Trận đấu sẽ được xử thua ngay lập tức.') && wsRef.current) {
      wsRef.current.send({ type: 'resign' });
    }
  }

  function handleOfferDraw() {
    if (wsRef.current) {
      wsRef.current.send({ type: 'draw_offer' });
      alert('Đã gửi lời đề nghị hòa');
    }
  }

  function handleDrawResponse(accept) {
    if (wsRef.current) {
      wsRef.current.send({ type: accept ? 'draw_accept' : 'draw_decline' });
      setDrawOffer(false);
    }
  }

  const myTime = color === 'WHITE' ? clocks.white : clocks.black;
  const oppTime = color === 'WHITE' ? clocks.black : clocks.white;
  const opponent = color === 'WHITE' ? players.black : players.white;
  const me = color === 'WHITE' ? players.white : players.black;

  return (
    <div className="site-container">
      <div className="game-top">
        <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
          <button onClick={() => nav('/lobby')} className="secondary small-btn">← Sảnh</button>
          <span style={{ color: 'var(--text-dim)' }}>|</span>
          <span className="room-badge" style={{ fontSize: 13, fontWeight: 600 }}>Phòng: {roomId || '...'}</span>
        </div>
        <div style={{ fontWeight: 700, fontSize: 13, textTransform: 'uppercase', letterSpacing: '0.05em' }}>
          Phe chơi: <span style={{ color: 'var(--accent)', marginLeft: 4 }}>{color === 'WHITE' ? 'TRẮNG' : (color === 'BLACK' ? 'ĐEN' : '...')}</span>
        </div>
      </div>

      <div className="game-wrap">
        <div className="board-wrapper card">
          <ChessBoard
            fen={fen}
            myColor={color}
            orientation={color === 'BLACK' ? 'b' : 'w'}
            onMove={handleMove}
            disabled={disabled}
            lastMove={lastMove}
          />
        </div>

        <div className="game-sidebar">
          {/* Opponent Info */}
          <div className="player-panel card" style={{
            padding: 16,
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            transition: 'background 0.3s',
            background: (disabled && !gameOver ? 'rgba(99,102,241,0.15)' : 'var(--glass)'),
            border: (disabled && !gameOver ? '1px solid var(--accent)' : '1px solid var(--glass-border)')
          }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
              <div style={{ width: 40, height: 40, background: 'var(--secondary)', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 800 }}>
                {opponent?.name?.[0].toUpperCase() || 'O'}
              </div>
              <div>
                <div style={{ fontWeight: 700, fontSize: 15 }}>{opponent?.name || 'Đối thủ'}</div>
                <div style={{ color: 'var(--accent)', fontSize: 12, fontWeight: 600 }}>
                  {color === 'WHITE' ? 'Quân ĐEN' : 'Quân TRẮNG'} • ELO: {opponent?.elo || '1200'}
                </div>
              </div>
            </div>
            <div className="timer" style={{ fontSize: 22, fontWeight: 800, color: (oppTime < 30000 ? 'var(--danger)' : 'var(--text)') }}>
              {formatTime(oppTime)}
            </div>
          </div>

          <div className="game-controls card" style={{ display: 'flex', gap: 12, padding: 12 }}>
            <button onClick={handleResign} className="danger" style={{ flex: 1, height: 44, fontSize: 14 }}>Đầu hàng</button>
            <button onClick={handleOfferDraw} className="secondary" style={{ flex: 1, height: 44, fontSize: 14 }}>Cầu hòa</button>
          </div>

          {drawOffer && (
            <div className="card" style={{ background: 'var(--accent)', color: 'white', padding: 16 }}>
              <p style={{ margin: 0, marginBottom: 12, fontWeight: 600 }}>Đối thủ mời bạn hòa</p>
              <div style={{ display: 'flex', gap: 8 }}>
                <button onClick={() => handleDrawResponse(true)} style={{ flex: 1, background: 'white', color: 'var(--accent)', height: 36 }}>Đồng ý</button>
                <button onClick={() => handleDrawResponse(false)} style={{ flex: 1, background: 'rgba(255,255,255,0.2)', border: '1px solid white', height: 36 }}>Từ chối</button>
              </div>
            </div>
          )}

          <ChatBox messages={chatMessages} onSendMessage={handleSendChat} />

          {/* Your Info */}
          <div className="player-panel card" style={{
            padding: 16,
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            transition: 'background 0.3s',
            background: (!disabled && !gameOver ? 'rgba(99,102,241,0.15)' : 'var(--glass)'),
            border: (!disabled && !gameOver ? '1px solid var(--accent)' : '1px solid var(--glass-border)')
          }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
              <div style={{ width: 40, height: 40, background: 'var(--accent)', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 800 }}>
                {me?.name?.[0].toUpperCase() || 'U'}
              </div>
              <div>
                <div style={{ fontWeight: 700, fontSize: 15 }}>{me?.name || 'Bạn'}</div>
                <div style={{ color: 'var(--accent)', fontSize: 12, fontWeight: 600 }}>
                  {color === 'WHITE' ? 'Quân TRẮNG' : 'Quân ĐEN'} • ELO: {me?.elo || '1200'}
                </div>
              </div>
            </div>
            <div className="timer" style={{ fontSize: 22, fontWeight: 800, color: (myTime < 30000 ? 'var(--danger)' : 'var(--text)') }}>
              {formatTime(myTime)}
            </div>
          </div>
        </div>
      </div>

      <GameOverModal open={!!gameOver} message={gameOver} onClose={() => { nav('/lobby'); }} />
    </div>
  );
}
