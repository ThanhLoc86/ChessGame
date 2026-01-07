import React, { useEffect, useState } from 'react';
import { getProfile } from '../api/user';
import { useNavigate } from 'react-router-dom';

export default function Lobby() {
  const [profile, setProfile] = useState(null);
  const nav = useNavigate();
  useEffect(() => {
    const token = sessionStorage.getItem('token') || localStorage.getItem('token');
    if (!token) { nav('/login'); return; }
    getProfile(token).then(setProfile).catch(() => setProfile(null));
  }, []);
  function createRoom() {
    // prefer passing intent via URL param to avoid cross-tab/localStorage races
    // do NOT set localStorage flag to avoid races across tabs
    localStorage.removeItem('wsCreate');
    localStorage.removeItem('wsJoin');
    nav('/game?create=1');
  }
  function playVsBot() {
    localStorage.removeItem('wsCreate');
    localStorage.removeItem('wsJoin');
    nav('/game?bot=1');
  }
  function joinRoom() {
    const rid = document.getElementById('roomId').value.trim();
    if (!rid) return alert('Vui lòng nhập mã phòng');
    // clear any stray flags and navigate with explicit join param only
    localStorage.removeItem('wsCreate');
    localStorage.removeItem('wsJoin');
    nav('/game?join=' + encodeURIComponent(rid));
  }
  function logout() { localStorage.removeItem('token'); nav('/login'); }
  return (
    <div className="site-container lobby-page">
      <div className="lobby-header">
        <div>
          <h1 style={{ margin: 0, fontSize: 32 }}>Chess Royale</h1>
          <p style={{ color: 'var(--text-dim)', margin: 0 }}>Trang web chơi cờ vua trực tuyến</p>
        </div>
        <div style={{ display: 'flex', gap: 12, alignItems: 'center' }}>
          {profile && (
            <div onClick={() => nav('/profile')} className="card" style={{ padding: '8px 16px', display: 'flex', alignItems: 'center', gap: 10, cursor: 'pointer', border: '1px solid var(--accent)' }}>
              <div style={{ width: 32, height: 32, background: 'var(--accent)', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 14, fontWeight: 800 }}>
                {profile.username[0].toUpperCase()}
              </div>
              <div>
                <div style={{ fontSize: 13, fontWeight: 700 }}>{profile.username}</div>
                <div style={{ fontSize: 11, color: 'var(--accent)' }}>ELO: {profile.elo}</div>
              </div>
            </div>
          )}
          <button onClick={logout} className="secondary small-btn">Đăng xuất</button>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 24, marginTop: 32 }}>
        <div className="card" style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
          <div style={{ fontSize: 24 }}>🎮</div>
          <h2 style={{ margin: 0 }}>Chơi trực tuyến</h2>
          <p style={{ color: 'var(--text-dim)', margin: 0, fontSize: 14 }}>Tạo phòng riêng và mời bạn bè tham gia những trận đấu kịch tính.</p>
          <button onClick={createRoom} style={{ marginTop: 'auto' }}>Tạo phòng ngay</button>
        </div>

        <div className="card" style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
          <div style={{ fontSize: 24 }}>🤖</div>
          <h2 style={{ margin: 0 }}>Đấu với máy</h2>
          <p style={{ color: 'var(--text-dim)', margin: 0, fontSize: 14 }}>Rèn luyện kỹ năng cờ vua với máy.</p>
          <button onClick={playVsBot} className="secondary" style={{ marginTop: 'auto' }}>Đánh với máy</button>
        </div>

        <div className="card" style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
          <div style={{ fontSize: 24 }}>🔑</div>
          <h2 style={{ margin: 0 }}>Vào phòng chơi</h2>
          <p style={{ color: 'var(--text-dim)', margin: 0, fontSize: 14 }}>Nhập mã phòng (Room ID) do đối thủ cung cấp để tham gia trận đấu.</p>
          <div style={{ display: 'flex', gap: 8, marginTop: 'auto' }}>
            <input id="roomId" placeholder="Mã phòng" style={{ flex: 1, height: 44, padding: '0 12px' }} />
            <button onClick={joinRoom} className="secondary" style={{ padding: '0 16px' }}>Vào</button>
          </div>
        </div>
      </div>

      <div className="card" style={{ marginTop: 32 }}>
        <h3 style={{ marginTop: 0 }}>Giải đấu nổi bật</h3>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 }}>
          <div className="card" style={{ background: 'rgba(255,255,255,0.02)', padding: 12, display: 'flex', justifyContent: 'space-between' }}>
            <div>
              <div style={{ fontWeight: 600 }}>Giải vô địch Chớp nhoáng</div>
              <div style={{ fontSize: 12, color: 'var(--text-dim)' }}>5 phút • Xếp hạng</div>
            </div>
            <button className="secondary small-btn">Xem ngay</button>
          </div>
          <div className="card" style={{ background: 'rgba(255,255,255,0.02)', padding: 12, display: 'flex', justifyContent: 'space-between' }}>
            <div>
              <div style={{ fontWeight: 600 }}>Thử thách hàng ngày</div>
              <div style={{ fontSize: 12, color: 'var(--text-dim)' }}>Chiếu bí sau 2 nước</div>
            </div>
            <button className="secondary small-btn">Giải đố</button>
          </div>
        </div>
      </div>
    </div>
  );
}


